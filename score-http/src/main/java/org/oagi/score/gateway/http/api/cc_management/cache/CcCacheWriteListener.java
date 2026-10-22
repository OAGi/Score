package org.oagi.score.gateway.http.api.cc_management.cache;

import org.jooq.ExecuteContext;
import org.jooq.ExecuteListener;
import org.jooq.ExecuteListenerProvider;
import org.jooq.ExecuteType;
import org.jooq.impl.DefaultExecuteListener;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tells the {@link CcCacheWriteObserver} about every statement that writes to a table the cached records are made of.
 * <p>
 * The records are joined from the component tables and their manifests, the sequence keys, the tags, the revision log,
 * the release and its dependencies, the library and the owner, so a write to any of them can make an entry stale.
 * Every write goes through the {@code DSLContext}, which is why one listener covers all the places that write.
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

    public CcCacheWriteListener(CcCacheWriteObserver cache) {
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
