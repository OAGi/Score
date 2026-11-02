package org.oagi.score.gateway.http.cache;

import org.oagi.score.gateway.http.change.propagation.PropagationConsumer;
import org.oagi.score.gateway.http.change.propagation.PropagationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Makes the cache forget the records that a propagation reached. {@code score.cache.propagation} chooses whether it does,
 * only logs what it would forget, or does nothing.
 */
@Component
public class CacheInvalidationConsumer implements PropagationConsumer {

    private static final Logger logger = LoggerFactory.getLogger(CacheInvalidationConsumer.class);

    private final RecordCache cache;
    private final RecordChangeKeyResolver resolver;
    private final RecordCacheProperties properties;

    public CacheInvalidationConsumer(ObjectProvider<RecordCache> cache, RecordChangeKeyResolver resolver,
                                     RecordCacheProperties properties) {
        this.cache = cache.getIfAvailable(() -> RecordCache.DISABLED);
        this.resolver = resolver;
        this.properties = properties;
    }

    @Override
    public void accept(PropagationResult result) {
        switch (properties.getPropagation()) {
            case OFF -> {
            }
            case SHADOW -> logger.info("Propagation (shadow) would forget {} keys for {} changes: {}", keysOf(result).size(),
                    result.source().size(), result.describe());
            case ON -> cache.invalidate(keysOf(result));
        }
    }

    private Set<String> keysOf(PropagationResult result) {
        return resolver.keysOfRecords(result.affected());
    }
}
