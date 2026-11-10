package org.oagi.score.gateway.http.common.async;

/**
 * What a transport carries: the channel it is for, the key that tells which of the things of that channel it is about
 * (a transport that keeps order, or splits the work by key, uses it; one that does not ignores it), and the event as text.
 * It is all there is to know to send an event later than it was published, so a publisher that keeps it first (see
 * {@link AsyncJobPublisher}) needs nothing else.
 */
public record AsyncJobMessage(String channel, String key, String payload) {
}
