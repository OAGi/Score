package org.oagi.score.gateway.http.api.cc_management.cache;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * What a {@code CachedCcDocument} can be told, under {@code score.cc-cache}. The defaults are the smallest values
 * measured to be enough (see {@code application.yml} and {@code CcCacheTuningBenchmark}).
 */
@Component
@ConfigurationProperties("score.cc-cache")
public class CcCacheProperties {

    /** The most ids sent to the database in one {@code IN} list. */
    private int batchSize = 500;

    /**
     * The most times one document reads the dependencies of a component ahead. A document that walks a tree asks for its
     * root first, and the dependencies of the root have everything it will ask for; one that asks for unrelated
     * components, one after the other, would read the dependencies of each of them, and past this many it reads the rest
     * one by one. 0 reads nothing ahead.
     */
    private int maxPrefetchedDependencies = 4;

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        if (batchSize < 1) {
            throw new IllegalArgumentException("score.cc-cache.batch-size must be at least 1: " + batchSize);
        }
        this.batchSize = batchSize;
    }

    public int getMaxPrefetchedDependencies() {
        return maxPrefetchedDependencies;
    }

    public void setMaxPrefetchedDependencies(int maxPrefetchedDependencies) {
        if (maxPrefetchedDependencies < 0) {
            throw new IllegalArgumentException("score.cc-cache.max-prefetched-dependencies must not be negative: " + maxPrefetchedDependencies);
        }
        this.maxPrefetchedDependencies = maxPrefetchedDependencies;
    }
}
