package org.oagi.score.gateway.http.common.async;

import java.util.function.Consumer;

/**
 * How an event gets from where it is published to where it is handled. It carries text and knows nothing of what is in it,
 * nor of when it is sent (that is for the {@link AsyncJobPublisher}), nor of what the handler does with it (see
 * {@link AsyncJobRunner}).
 *
 * <p>Which transport is used is the setting {@code score.async.transport}. What each one does so that an event is handled
 * once, wherever it is received, is its own business and its own settings: a transport that hands an event to every
 * instance has to see to it, one that hands it to one instance of a group does not.</p>
 */
public interface AsyncJobTransport {

    /** Sends the message. It is not given back to the caller whether anyone received it. */
    void send(AsyncJobMessage message);

    /** Hands the text of what is sent on the channel to the handler, for as long as the application runs. */
    void subscribe(String channel, Consumer<String> handler);
}
