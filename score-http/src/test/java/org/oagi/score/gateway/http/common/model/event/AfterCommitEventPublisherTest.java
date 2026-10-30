package org.oagi.score.gateway.http.common.model.event;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * An event is sent only after the transaction that wrote the rows it refers to has committed.
 */
class AfterCommitEventPublisherTest {

    private static final String CHANNEL = "someEvent";

    private final Object event = new Object();
    private RedisTemplate redisTemplate;
    private AfterCommitEventPublisher publisher;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(RedisTemplate.class);
        publisher = new AfterCommitEventPublisher();
        ReflectionTestUtils.setField(publisher, "redisTemplate", redisTemplate);
    }

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void theEventIsSentOnlyAfterTheCommit() {
        TransactionSynchronizationManager.initSynchronization();

        publisher.publish(CHANNEL, event);

        verify(redisTemplate, never()).convertAndSend(any(String.class), any(Object.class));
        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
        verify(redisTemplate).convertAndSend(CHANNEL, event);
    }

    @Test
    void theEventIsNotSentWhenTheTransactionRollsBack() {
        TransactionSynchronizationManager.initSynchronization();

        publisher.publish(CHANNEL, event);
        TransactionSynchronizationManager.getSynchronizations().forEach(
                synchronization -> synchronization.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));

        verify(redisTemplate, never()).convertAndSend(any(String.class), any(Object.class));
    }

    @Test
    void theEventIsSentAtOnceOutsideATransaction() {
        publisher.publish(CHANNEL, event);

        verify(redisTemplate).convertAndSend(CHANNEL, event);
    }

}
