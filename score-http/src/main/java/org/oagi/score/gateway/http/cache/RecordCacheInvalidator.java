package org.oagi.score.gateway.http.cache;

import org.oagi.score.gateway.http.change.RecordsChangedEvent;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * On a {@link RecordsChangedEvent}, forgets the keys that the registered {@link ChangeKeyResolver}s name. It does not know what a
 * key stands for or why a change makes it stale.
 */
@Component
public class RecordCacheInvalidator {

    private final RecordCache cache;
    private final List<ChangeKeyResolver> resolvers;

    @Autowired
    public RecordCacheInvalidator(ObjectProvider<RecordCache> cache, List<ChangeKeyResolver> resolvers) {
        this(cache.getIfAvailable(() -> RecordCache.DISABLED), resolvers);
    }

    public RecordCacheInvalidator(RecordCache cache, List<ChangeKeyResolver> resolvers) {
        this.cache = cache;
        this.resolvers = List.copyOf(resolvers);
    }

    @EventListener
    @Order(1)
    public void on(RecordsChangedEvent event) {
        Set<String> keys = new HashSet<>();
        resolvers.forEach(resolver -> keys.addAll(resolver.keysOf(event.changes())));
        cache.invalidate(keys);
    }
}
