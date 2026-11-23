package org.oagi.score.gateway.http.common.async;

import org.oagi.score.gateway.http.common.model.event.Event;
import org.springframework.stereotype.Component;

/** Sends the event through the transport once the transaction has committed. */
@Component
public class AfterCommitAsyncJobPublisher implements AsyncJobPublisher {

    private final AsyncJobCodec codec;
    private final AsyncJobTransport transport;

    public AfterCommitAsyncJobPublisher(AsyncJobCodec codec, AsyncJobTransport transport) {
        this.codec = codec;
        this.transport = transport;
    }

    @Override
    public void publish(String channel, String key, Event event) {
        AsyncJobMessage message = new AsyncJobMessage(channel, key, codec.encode(event));
        AfterCommit.run(() -> transport.send(message));
    }
}
