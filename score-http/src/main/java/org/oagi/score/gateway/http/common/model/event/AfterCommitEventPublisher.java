package org.oagi.score.gateway.http.common.model.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Publishes a Redis event once the surrounding transaction has committed.
 * <p>
 * The listeners read the rows the publishing transaction wrote, so an event sent before the commit can be handled
 * against rows they cannot see yet, and an event sent before a rollback refers to rows that never existed.
 * Outside a transaction the event is sent at once.
 */
@Component
public class AfterCommitEventPublisher {

    @Autowired
    private RedisTemplate redisTemplate;

    public void publish(String channel, Object event) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    redisTemplate.convertAndSend(channel, event);
                }
            });
        } else {
            redisTemplate.convertAndSend(channel, event);
        }
    }

}
