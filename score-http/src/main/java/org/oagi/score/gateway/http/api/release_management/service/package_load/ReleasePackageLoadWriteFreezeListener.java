package org.oagi.score.gateway.http.api.release_management.service.package_load;

import org.jooq.ExecuteContext;
import org.jooq.ExecuteListener;
import org.jooq.ExecuteListenerProvider;
import org.jooq.ExecuteType;
import org.jooq.impl.DefaultExecuteListener;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Before a statement of this process writes, goes through the gate (see {@link ReleasePackageLoadWriteGate}): when a load has stopped writing it waits
 * there, and when it waits for longer than it may it is refused with a {@link ReleasePackageLoadWriteFrozenException}. Inside a transaction the shared lock on the
 * gate lasts until the transaction ends, which is what lets the load wait for the writers that are at work. Reading never goes through it.
 * Other processes that write to the same database (the connect-center-api) go through the same gate on their own.
 */
@Component
public class ReleasePackageLoadWriteFreezeListener implements ExecuteListenerProvider {

    private static final Pattern WRITE_STATEMENT = Pattern.compile(
            "^\\s*(?:insert\\s+(?:ignore\\s+)?into|replace\\s+into|update(?:\\s+ignore)?|delete(?:\\s+ignore)?\\s+from)\\s+"
                    + "(?:[`\"]?\\w+[`\"]?\\s*\\.\\s*)?[`\"]?(\\w+)[`\"]?",
            Pattern.CASE_INSENSITIVE);

    private final ObjectProvider<ReleasePackageLoadDatabaseWriteFreeze> freeze;
    private final ObjectProvider<ReleasePackageLoadSettings> settings;

    /** Lazy: this listener is a part of the DSLContext that the freeze's own beans may be read with. */
    public ReleasePackageLoadWriteFreezeListener(@Lazy ObjectProvider<ReleasePackageLoadDatabaseWriteFreeze> freeze, @Lazy ObjectProvider<ReleasePackageLoadSettings> settings) {
        this.freeze = freeze;
        this.settings = settings;
    }

    @Override
    public ExecuteListener provide() {
        return new DefaultExecuteListener() {
            @Override
            public void executeStart(ExecuteContext ctx) {
                if (ctx.type() == ExecuteType.READ || ctx.type() == ExecuteType.DDL || !keptByTheGate(ctx)) {
                    return;
                }
                ReleasePackageLoadDatabaseWriteFreeze own = freeze.getIfAvailable();
                if (own != null && own.isOwnedBy(Thread.currentThread())) {
                    return;
                }
                try {
                    ReleasePackageLoadWriteGate.enter(ctx.connection(), settings.getObject().writeWait());
                } catch (SQLException e) {
                    if (ReleasePackageLoadWriteGate.isTimeout(e)) {
                        throw new ReleasePackageLoadWriteFrozenException(ReleasePackageLoadMessages.WRITING_PAUSED, e);
                    }
                    throw new org.jooq.exception.DataAccessException("The gate of the writers could not be read: " + e.getMessage(), e);
                }
            }
        };
    }

    /** Whether the statement writes to a table that the gate is for: the statements on the tables that stay in place are the load's own. */
    private static boolean keptByTheGate(ExecuteContext ctx) {
        String[] batch = ctx.batchSQL();
        if (batch != null && batch.length > 0) {
            for (String sql : batch) {
                if (sql != null && keptByTheGate(sql)) {
                    return true;
                }
            }
            return false;
        }
        return keptByTheGate(ctx.sql());
    }

    static boolean keptByTheGate(String sql) {
        if (sql == null) {
            return false;
        }
        Matcher matcher = WRITE_STATEMENT.matcher(sql);
        // A statement that is not recognised as a plain write (a call, a statement made of several) is gated: refusing too much is safe.
        return !matcher.find() || !ReleasePackageLoadTables.STAYS_IN_PLACE.contains(matcher.group(1).toLowerCase(Locale.ROOT));
    }
}
