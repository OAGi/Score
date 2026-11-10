package org.oagi.score.gateway.http.common.async;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.event.ReleaseCleanupEvent;
import org.oagi.score.gateway.http.common.model.event.Event;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AfterCommitAsyncJobPublisherTest {

    private final AsyncJobEventsTest.LoopbackTransport transport = new AsyncJobEventsTest.LoopbackTransport();
    private final AsyncJobCodec codec = new AsyncJobCodec();
    private final ReleaseCleanupEvent event =
            new ReleaseCleanupEvent(new UserId(BigInteger.valueOf(7)), new ReleaseId(BigInteger.valueOf(12)));

    @AfterEach
    void clear() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void sendsAtOnceOutsideATransactionWithTheKey() {
        new AfterCommitAsyncJobPublisher(codec, transport).publish("cleanup", "12", event);

        assertEquals(List.of(new AsyncJobMessage("cleanup", "12", codec.encode(event))), transport.sent);
    }

    @Test
    void sendsOnlyOnceTheTransactionHasCommitted() {
        TransactionSynchronizationManager.initSynchronization();

        new AfterCommitAsyncJobPublisher(codec, transport).publish("cleanup", "12", event);
        assertTrue(transport.sent.isEmpty());

        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
        assertEquals(1, transport.sent.size());
    }

    @Test
    void sendsNothingWhenTheTransactionIsRolledBack() {
        TransactionSynchronizationManager.initSynchronization();

        new AfterCommitAsyncJobPublisher(codec, transport).publish("cleanup", "12", event);
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(s -> s.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));

        assertTrue(transport.sent.isEmpty());
    }

    /**
     * What an outbox is to the publisher: it keeps the message with the transaction, and something else reads what is kept and
     * has the transport send it. Nothing but the message crosses, so the codec, the transport and the code that publishes
     * and handles stay as they are.
     */
    @Test
    void aPublisherThatKeepsTheMessageForLaterNeedsNothingElseThanTheMessage() {
        List<AsyncJobMessage> outbox = new ArrayList<>();
        AsyncJobPublisher keeping = (channel, key, e) -> outbox.add(new AsyncJobMessage(channel, key, codec.encode(e)));
        List<Event> handled = new ArrayList<>();
        new AsyncJobEvents(transport, codec).subscribe("cleanup", ReleaseCleanupEvent.class, (Consumer<ReleaseCleanupEvent>) handled::add);

        keeping.publish("cleanup", "12", event);
        assertTrue(handled.isEmpty());
        outbox.forEach(transport::send); // the relay

        assertEquals(List.of(event), handled);
    }
}
