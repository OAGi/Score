package org.oagi.score.gateway.http.cache;

import org.oagi.score.gateway.http.change.ChangeLogSwitch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * What the providers of the cache have in common: a bulk change is run without the change log, and the whole cache is forgotten when it
 * has committed (see {@link #bulkChange}).
 */
abstract class AbstractRecordCache implements RecordCache {

    private static final Logger logger = LoggerFactory.getLogger(AbstractRecordCache.class);

    private static final int ATTEMPTS = 3;

    /** Lazy: the repositories use this cache, so it cannot ask for them when it is made. It is null for a cache that is used alone. */
    private final ObjectProvider<ChangeLogSwitch> changeLogSwitch;

    AbstractRecordCache(ObjectProvider<ChangeLogSwitch> changeLogSwitch) {
        this.changeLogSwitch = changeLogSwitch;
    }

    @Override
    public void bulkChange(Runnable action) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("A bulk change is run in a transaction.");
        }
        ChangeLogSwitch changeLog = (changeLogSwitch == null) ? null : changeLogSwitch.getObject();
        if (changeLog != null) {
            changeLog.stopWriting();
        }
        try {
            action.run();
        } finally {
            // The connection is not the transaction's alone: it goes back to the pool. If this fails the change fails with it.
            if (changeLog != null) {
                changeLog.resumeWriting();
            }
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                forgetEverything();
            }
        });
    }

    /** Never throws: the change has committed, and it is not to be taken for a failure because the cache could not be forgotten. */
    private void forgetEverything() {
        for (int attempt = 1; attempt <= ATTEMPTS; attempt++) {
            try {
                invalidateAll();
                return;
            } catch (RuntimeException e) {
                logger.warn("The cache could not be forgotten after a bulk change, attempt {} of {}.", attempt, ATTEMPTS, e);
            }
        }
        logger.error("The cache still holds what it held before a bulk change; it is stale until its entries expire.");
    }
}
