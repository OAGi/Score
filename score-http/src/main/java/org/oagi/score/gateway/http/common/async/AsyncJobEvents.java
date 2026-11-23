package org.oagi.score.gateway.http.common.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * Hands the events of a channel to a function, as the type that the one who subscribes says they are: what the function
 * calls is not changed by the proxy of anything, so what it does is what it says (see {@link AsyncJobRunner}). Events are
 * published with {@link AsyncJobPublisher}.
 *
 * <p>An event that cannot be read as that type is logged and let go: it would be no better the next time it was sent.</p>
 */
@Component
public class AsyncJobEvents {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AsyncJobTransport transport;
    private final AsyncJobCodec codec;

    public AsyncJobEvents(AsyncJobTransport transport, AsyncJobCodec codec) {
        this.transport = transport;
        this.codec = codec;
    }

    public <E> void subscribe(String channel, Class<E> type, Consumer<E> handler) {
        transport.subscribe(channel, payload -> {
            E event;
            try {
                event = codec.decode(payload, type);
            } catch (RuntimeException e) {
                logger.error("An event on " + channel + " could not be read as a " + type.getSimpleName()
                        + " and is dropped: " + e.getMessage(), e);
                return;
            }
            handler.accept(event);
        });
    }
}
