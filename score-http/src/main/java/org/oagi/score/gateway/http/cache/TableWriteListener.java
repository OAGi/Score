package org.oagi.score.gateway.http.cache;


import org.jooq.ExecuteContext;
import org.jooq.ExecuteListener;
import org.jooq.ExecuteListenerProvider;
import org.jooq.ExecuteType;
import org.jooq.impl.DefaultExecuteListener;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Tells the {@link RecordCacheWriteObserver} when a statement writes to a table that a {@link RecordKeyProvider} named. It is
 * only a hint for this instance: what the cache forgets comes from the change log, so a write that is not recognised is still
 * forgotten, a moment later.
 */
@Component
public class TableWriteListener implements ExecuteListenerProvider {

    private static final Pattern WRITE_STATEMENT = Pattern.compile(
            "^\\s*(?:insert\\s+(?:ignore\\s+)?into|replace\\s+into|update(?:\\s+ignore)?|delete(?:\\s+ignore)?\\s+from)\\s+"
                    + "(?:[`\"]?\\w+[`\"]?\\s*\\.\\s*)?[`\"]?(\\w+)[`\"]?",
            Pattern.CASE_INSENSITIVE);

    private final RecordCacheWriteObserver cache;
    private final ObjectProvider<RecordKeyProvider> providers;
    private volatile Set<String> tables;

    /** Lazy: the DSLContext that this listener is part of is what the observer and the providers read with. */
    public TableWriteListener(@Lazy RecordCacheWriteObserver cache, @Lazy ObjectProvider<RecordKeyProvider> providers) {
        this.cache = cache;
        this.providers = providers;
    }

    private Set<String> tables() {
        Set<String> result = tables;
        if (result == null) {
            result = providers.orderedStream().flatMap(provider -> provider.tables().stream()).collect(Collectors.toUnmodifiableSet());
            tables = result;
        }
        return result;
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
                        if (writesTo(tables(), sql)) {
                            cache.written();
                            return;
                        }
                    }
                } else if (writesTo(tables(), ctx.sql())) {
                    cache.written();
                }
            }
        };
    }

    static boolean writesTo(Set<String> tables, String sql) {
        if (sql == null) {
            return false;
        }
        Matcher matcher = WRITE_STATEMENT.matcher(sql);
        return matcher.find() && tables.contains(matcher.group(1).toLowerCase(Locale.ROOT));
    }

}
