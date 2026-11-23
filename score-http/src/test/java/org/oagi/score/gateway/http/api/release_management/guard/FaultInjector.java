package org.oagi.score.gateway.http.api.release_management.guard;

import org.jooq.ExecuteContext;
import org.jooq.ExecuteListener;
import org.jooq.ExecuteListenerProvider;
import org.jooq.ExecuteType;
import org.jooq.impl.DefaultExecuteListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Watches the statements the thread that has a {@link Probe} executes: counts the writes, fails after the n-th of them, and
 * runs a hook before or after a statement (to hold a thread at a statement, so that another one can be let in at a chosen
 * moment). A thread without a probe is not touched.
 */
public final class FaultInjector implements ExecuteListenerProvider {

    /** What the statement is, as a hook sees it. */
    public record Stmt(String sql, List<Object> params) {

        /** Whether the statement updates the table. */
        public boolean updates(String table) {
            return Pattern.compile("^\\s*update\\s+(?:[`\"]?\\w+[`\"]?\\s*\\.\\s*)?[`\"]?" + table + "[`\"]?\\s+set\\b",
                    Pattern.CASE_INSENSITIVE).matcher(sql).find();
        }

        public boolean hasParam(Object value) {
            return params.stream().anyMatch(p -> p != null && p.toString().equals(value.toString()));
        }
    }

    private record Hook(boolean before, Predicate<Stmt> when, Runnable action, AtomicBoolean fired) {
    }

    /** The failure that is injected. */
    public static final class InjectedFailure extends RuntimeException {
        InjectedFailure(String message) {
            super(message);
        }
    }

    public static final class Probe {
        private final AtomicInteger writes = new AtomicInteger();
        private volatile int failAfter = -1;
        private final List<String> trace = Collections.synchronizedList(new ArrayList<>());
        private final List<Hook> hooks = new CopyOnWriteArrayList<>();

        /** Throws once, right after the n-th write (counted from 1) has been executed. */
        public Probe failAfterWrite(int n) {
            this.failAfter = n;
            return this;
        }

        /** Runs the action once, before (or after) the first statement that matches. */
        public Probe hook(boolean before, Predicate<Stmt> when, Runnable action) {
            hooks.add(new Hook(before, when, action, new AtomicBoolean()));
            return this;
        }

        public int writes() {
            return writes.get();
        }

        public List<String> trace() {
            return new ArrayList<>(trace);
        }
    }

    private static final ThreadLocal<Probe> CURRENT = new ThreadLocal<>();

    public static void attach(Probe probe) {
        CURRENT.set(probe);
    }

    public static void detach() {
        CURRENT.remove();
    }

    private static Stmt stmt(ExecuteContext ctx) {
        List<Object> params = new ArrayList<>();
        if (ctx.query() != null) {
            params.addAll(ctx.query().getBindValues());
        }
        return new Stmt(ctx.sql() == null ? "" : ctx.sql(), params);
    }

    private static boolean isWrite(ExecuteContext ctx) {
        return ctx.type() != ExecuteType.READ && ctx.type() != ExecuteType.DDL;
    }

    private static void runHooks(Probe probe, boolean before, Stmt stmt) {
        for (Hook hook : probe.hooks) {
            if (hook.before() == before && !hook.fired().get() && hook.when().test(stmt)
                    && hook.fired().compareAndSet(false, true)) {
                hook.action().run();
            }
        }
    }

    @Override
    public ExecuteListener provide() {
        return new DefaultExecuteListener() {
            @Override
            public void executeStart(ExecuteContext ctx) {
                Probe probe = CURRENT.get();
                if (probe != null) {
                    runHooks(probe, true, stmt(ctx));
                }
            }

            @Override
            public void executeEnd(ExecuteContext ctx) {
                Probe probe = CURRENT.get();
                if (probe == null) {
                    return;
                }
                Stmt stmt = stmt(ctx);
                if (isWrite(ctx)) {
                    int n = probe.writes.incrementAndGet();
                    probe.trace.add(n + ": " + stmt.sql().replaceAll("\\s+", " "));
                    if (n == probe.failAfter) {
                        throw new InjectedFailure("injected after write " + n + ": " + stmt.sql());
                    }
                }
                runHooks(probe, false, stmt);
            }
        };
    }
}
