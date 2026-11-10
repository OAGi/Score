package org.oagi.score.gateway.http.common.async;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.event.ReleaseCleanupEvent;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AsyncJobEventsTest {

    /** Delivers what is sent to whoever subscribed, at once. */
    static class LoopbackTransport implements AsyncJobTransport {
        final List<AsyncJobMessage> sent = new ArrayList<>();
        private final java.util.Map<String, List<Consumer<String>>> handlers = new java.util.HashMap<>();

        @Override
        public void send(AsyncJobMessage message) {
            sent.add(message);
            handlers.getOrDefault(message.channel(), List.of()).forEach(h -> h.accept(message.payload()));
        }

        @Override
        public void subscribe(String channel, Consumer<String> handler) {
            handlers.computeIfAbsent(channel, c -> new ArrayList<>()).add(handler);
        }
    }

    private final LoopbackTransport transport = new LoopbackTransport();
    private final AsyncJobCodec codec = new AsyncJobCodec();
    private final AsyncJobEvents events = new AsyncJobEvents(transport, codec);

    private final ReleaseCleanupEvent event =
            new ReleaseCleanupEvent(new UserId(BigInteger.valueOf(7)), new ReleaseId(BigInteger.valueOf(12)));

    @Test
    void handsTheEventOfTheChannelToTheFunctionAsTheTypeItWasSubscribedAs() {
        List<ReleaseCleanupEvent> received = new ArrayList<>();
        events.subscribe("cleanup", ReleaseCleanupEvent.class, received::add);

        new AfterCommitAsyncJobPublisher(codec, transport).publish("cleanup", "12", event);

        assertEquals(List.of(event), received);
    }

    @Test
    void dropsAnEventThatCannotBeReadWithoutStoppingTheOnesThatFollow() {
        List<ReleaseCleanupEvent> received = new ArrayList<>();
        events.subscribe("cleanup", ReleaseCleanupEvent.class, received::add);

        transport.send(new AsyncJobMessage("cleanup", null, "not json"));
        transport.send(new AsyncJobMessage("cleanup", null, codec.encode(event)));

        assertEquals(List.of(event), received);
        assertTrue(transport.sent.size() == 2);
    }
}
