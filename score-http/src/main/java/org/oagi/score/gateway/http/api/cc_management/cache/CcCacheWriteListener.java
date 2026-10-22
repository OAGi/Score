package org.oagi.score.gateway.http.api.cc_management.cache;

import org.jooq.ExecuteContext;
import org.jooq.ExecuteListener;
import org.jooq.ExecuteListenerProvider;
import org.jooq.ExecuteType;
import org.jooq.impl.DefaultExecuteListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tells the {@link CcCacheWriteObserver} about every statement that writes to a table the cached records are made of.
 * <p>
 * It is only a hint, for this instance: the thread that wrote does not use the cache until its transaction ends, and the
 * change log is read at once when it commits. What the cache forgets comes from the change log, which the triggers on these
 * tables write whoever makes the change, so a write this listener does not recognise (a statement through other means, one
 * whose first table is not listed) is still forgotten, only a moment later.
 */
@Component
public class CcCacheWriteListener implements ExecuteListenerProvider {

    static final Set<String> WATCHED_TABLES = Set.of(
            "acc", "acc_manifest", "ascc", "ascc_manifest", "bcc", "bcc_manifest",
            "asccp", "asccp_manifest", "bccp", "bccp_manifest",
            "dt", "dt_manifest", "dt_sc", "dt_sc_manifest",
            "seq_key", "tag", "acc_manifest_tag", "asccp_manifest_tag", "bccp_manifest_tag", "dt_manifest_tag",
            "log", "release", "release_dep", "library", "app_user");

    private static final Pattern WRITE_STATEMENT = Pattern.compile(
            "^\\s*(?:insert\\s+(?:ignore\\s+)?into|replace\\s+into|update(?:\\s+ignore)?|delete(?:\\s+ignore)?\\s+from)\\s+"
                    + "(?:[`\"]?\\w+[`\"]?\\s*\\.\\s*)?[`\"]?(\\w+)[`\"]?",
            Pattern.CASE_INSENSITIVE);

    private final CcCacheWriteObserver cache;

    /** Lazy: the DSLContext that this listener is part of is what the observer reads the change log with. */
    public CcCacheWriteListener(@Lazy CcCacheWriteObserver cache) {
        this.cache = cache;
    }

    @Override
    public ExecuteListener provide() {
        return new DefaultExecuteListener() {
            @Override
            public void executeEnd(ExecuteContext ctx) {
                if (ctx.type() == ExecuteType.READ || ctx.type() == ExecuteType.DDL) {
                    return;
                }
                if (ctx.batchSQL() != null && ctx.batchSQL().length > 0) {
                    for (String sql : ctx.batchSQL()) {
                        if (writesToWatchedTable(sql)) {
                            cache.written();
                            return;
                        }
                    }
                } else if (writesToWatchedTable(ctx.sql())) {
                    cache.written();
                }
            }
        };
    }

    static boolean writesToWatchedTable(String sql) {
        if (sql == null) {
            return false;
        }
        Matcher matcher = WRITE_STATEMENT.matcher(sql);
        return matcher.find() && WATCHED_TABLES.contains(matcher.group(1).toLowerCase(Locale.ROOT));
    }

}
