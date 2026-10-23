package org.oagi.score.gateway.http.change;

import org.oagi.score.gateway.http.cache.ChangeKeyResolver;
import org.oagi.score.gateway.http.cache.RecordCache;
import org.oagi.score.gateway.http.cache.RecordCacheInvalidator;
import org.oagi.score.gateway.http.cache.RecordCacheStatus;
import org.oagi.score.gateway.http.change.propagation.ChangePropagator;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

/** What Spring wires in the application, by hand for a test: the watchdog publishes, the cache forgets, the propagation spreads. */
public final class ChangeFeed {

    private ChangeFeed() {
    }

    /** @param propagator the propagation to run after the cache has forgotten what changed, or null for none */
    public static RecordChangeWatchdog watchdog(RepositoryFactory repositoryFactory, List<ChangeKeyResolver> resolvers,
                                                ChangePropagator propagator, RecordCache cache, RecordCacheStatus status) {
        RecordCacheInvalidator invalidator = new RecordCacheInvalidator(cache, resolvers);
        ApplicationEventPublisher publisher = event -> {
            if (event instanceof RecordsChangedEvent changed) {
                invalidator.on(changed);
                if (propagator != null) {
                    propagator.on(changed);
                }
            } else if (event instanceof ChangeLogReadEvent read) {
                status.on(read);
            }
        };
        RecordChangeWatchdog watchdog = new RecordChangeWatchdog(repositoryFactory, publisher);
        status.bind(watchdog);
        return watchdog;
    }

    public static RecordChangeWatchdog watchdog(RepositoryFactory repositoryFactory, List<ChangeKeyResolver> resolvers,
                                                RecordCache cache, RecordCacheStatus status) {
        return watchdog(repositoryFactory, resolvers, null, cache, status);
    }
}
