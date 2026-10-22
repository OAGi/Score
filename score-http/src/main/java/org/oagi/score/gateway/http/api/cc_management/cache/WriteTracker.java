package org.oagi.score.gateway.http.api.cc_management.cache;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * What both caches do with a write: outside a transaction it counts at once; inside one the thread is marked
 * ("dirty") until the transaction ends, and a commit counts it then, a rollback not at all.
 */
final class WriteTracker {

    private static final Object DIRTY_TRANSACTION = WriteTracker.class.getName() + ".dirty";

    private WriteTracker() {
    }

    /** Whether the current thread's transaction has written without committing yet. */
    static boolean isDirty() {
        return TransactionSynchronizationManager.hasResource(DIRTY_TRANSACTION);
    }

    /** @param committed what to do once the write is visible to everyone */
    static void written(Runnable committed) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            committed.run();
            return;
        }
        if (isDirty()) {
            return;
        }
        TransactionSynchronizationManager.bindResource(DIRTY_TRANSACTION, Boolean.TRUE);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                TransactionSynchronizationManager.unbindResourceIfPossible(DIRTY_TRANSACTION);
                if (status == STATUS_COMMITTED) {
                    committed.run();
                }
            }
        });
    }

    /** For tests: forgets the state of the current thread. */
    static void reset() {
        TransactionSynchronizationManager.unbindResourceIfPossible(DIRTY_TRANSACTION);
    }

}
