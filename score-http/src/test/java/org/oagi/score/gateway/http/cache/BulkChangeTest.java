package org.oagi.score.gateway.http.cache;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.oagi.score.gateway.http.change.ChangeLogSwitch;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A bulk change is run without the change log, the session is given back as it was whatever happens, and the whole cache is forgotten
 * when the transaction has committed, and only then.
 */
class BulkChangeTest {

    private ChangeLogSwitch changeLog;
    private AtomicInteger forgotten;
    private AtomicInteger failuresLeft;
    private CaffeineRecordCache cache;

    @BeforeEach
    void setUp() {
        changeLog = Mockito.mock(ChangeLogSwitch.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<ChangeLogSwitch> provider = Mockito.mock(ObjectProvider.class);
        Mockito.when(provider.getObject()).thenReturn(changeLog);
        forgotten = new AtomicInteger();
        failuresLeft = new AtomicInteger();
        cache = new CaffeineRecordCache("maximumSize=100", RecordCacheStatus.alwaysReady(), provider) {
            @Override
            public void invalidateAll() {
                if (failuresLeft.getAndDecrement() > 0) {
                    throw new IllegalStateException("redis is down");
                }
                forgotten.incrementAndGet();
                super.invalidateAll();
            }
        };
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.setActualTransactionActive(false);
        TransactionSynchronizationManager.clearSynchronization();
    }

    private void commit() {
        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
    }

    @Test
    void theChangeRunsWithoutTheChangeLogAndTheCacheIsForgottenOnlyAfterTheCommit() {
        cache.put(cache.epoch(), "acc:1", "a");
        cache.bulkChange(() -> {
            Mockito.verify(changeLog).stopWriting();
            Mockito.verify(changeLog, Mockito.never()).resumeWriting();
            assertEquals("a", cache.get("acc:1"));
        });
        InOrder inOrder = Mockito.inOrder(changeLog);
        inOrder.verify(changeLog).stopWriting();
        inOrder.verify(changeLog).resumeWriting();
        assertEquals(0, forgotten.get(), "not before the commit");
        assertEquals("a", cache.get("acc:1"));

        commit();

        assertEquals(1, forgotten.get());
        assertNull(cache.get("acc:1"));
    }

    @Test
    void theSessionIsGivenBackWhenTheChangeFailsAndNothingIsForgotten() {
        IllegalStateException failure = new IllegalStateException("the change failed");
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> cache.bulkChange(() -> {
            throw failure;
        }));
        assertSame(failure, thrown);
        InOrder inOrder = Mockito.inOrder(changeLog);
        inOrder.verify(changeLog).stopWriting();
        inOrder.verify(changeLog).resumeWriting();
        assertTrue(TransactionSynchronizationManager.getSynchronizations().isEmpty(), "nothing waits for a commit that will not come");
        assertEquals(0, forgotten.get());
    }

    @Test
    void aCacheThatCannotBeForgottenIsTriedAgainAndNeverFailsTheCommittedChange() {
        failuresLeft.set(2);
        cache.bulkChange(() -> {
        });
        assertDoesNotThrow(this::commit);
        assertEquals(1, forgotten.get(), "the third attempt did it");

        failuresLeft.set(Integer.MAX_VALUE);
        forgotten.set(0);
        TransactionSynchronizationManager.clearSynchronization();
        TransactionSynchronizationManager.initSynchronization();
        cache.bulkChange(() -> {
        });
        assertDoesNotThrow(this::commit, "a cache that is never forgotten does not make the change a failure");
        assertEquals(0, forgotten.get());
    }

    @Test
    void aBulkChangeIsRunInATransaction() {
        TransactionSynchronizationManager.setActualTransactionActive(false);
        assertThrows(IllegalStateException.class, () -> cache.bulkChange(() -> {
        }));
        Mockito.verifyNoInteractions(changeLog);
    }

    @Test
    void aCacheThatIsUsedAloneRunsTheChangeAndForgetsAfterTheCommit() {
        AtomicInteger runs = new AtomicInteger();
        CaffeineRecordCache alone = new CaffeineRecordCache("maximumSize=100", RecordCacheStatus.alwaysReady());
        alone.put(alone.epoch(), "acc:1", "a");
        alone.bulkChange(runs::incrementAndGet);
        commit();
        assertEquals(1, runs.get());
        assertNull(alone.get("acc:1"));
    }
}
