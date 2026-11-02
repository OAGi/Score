package org.oagi.score.gateway.http.cache;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * What the readers of cached records can be told (the core component document is one), under {@code score.cache}. See {@code application.yml} for what each setting does.
 */
@Component
@ConfigurationProperties("score.cache")
public class RecordCacheProperties {

    /** The most ids sent to the database in one {@code IN} list. */
    private int batchSize = 500;

    /**
     * The most times one document reads the dependencies of a component ahead. A document that walks a tree asks for its
     * root first, and the dependencies of the root have everything it will ask for; one that asks for unrelated
     * components, one after the other, would read the dependencies of each of them, and past this many it reads the rest
     * one by one. 0 reads nothing ahead.
     */
    private int maxPrefetchedDependencies = 4;

    /** What the cache does with the records that a change reaches by the dependencies between records. */
    public enum Propagation {
        /** The records reached are forgotten too. */
        ON,
        /** The records reached are only logged, for a comparison with what is forgotten without it. */
        SHADOW,
        /** Only the records that changed are forgotten, as before the propagation existed. */
        OFF
    }

    private Propagation propagation = Propagation.ON;

    public Propagation getPropagation() {
        return propagation;
    }

    public void setPropagation(Propagation propagation) {
        this.propagation = propagation;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        if (batchSize < 1) {
            throw new IllegalArgumentException("score.cache.batch-size must be at least 1: " + batchSize);
        }
        this.batchSize = batchSize;
    }

    public int getMaxPrefetchedDependencies() {
        return maxPrefetchedDependencies;
    }

    public void setMaxPrefetchedDependencies(int maxPrefetchedDependencies) {
        if (maxPrefetchedDependencies < 0) {
            throw new IllegalArgumentException("score.cache.max-prefetched-dependencies must not be negative: " + maxPrefetchedDependencies);
        }
        this.maxPrefetchedDependencies = maxPrefetchedDependencies;
    }
}
