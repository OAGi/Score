package org.oagi.score.gateway.http.common.async;

import org.oagi.score.gateway.http.common.model.event.Event;

/**
 * Hands an event to be handled in the background, in the transaction that asks for it: the event is sent only if that
 * transaction commits, and only once it has (what the handler reads is what has been written). Outside a transaction it
 * is sent at once.
 *
 * <p>When it is sent is this interface's to decide, and how it travels is the {@link AsyncJobTransport}'s. The one that is
 * used, {@link AfterCommitAsyncJobPublisher}, sends it after the commit. One that keeps the {@link AsyncJobMessage} in
 * the transaction (an outbox) and has it sent by something that reads what is kept can take its place without the code
 * that publishes, the codec or the transport changing: the message is all that is kept.</p>
 *
 * @param key tells which of the things of the channel the event is about (the release, the BIE): a transport that keeps
 *            order, or splits the work by it, uses it
 */
public interface AsyncJobPublisher {

    void publish(String channel, String key, Event event);
}
