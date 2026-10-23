package org.oagi.score.gateway.http.change.propagation;

/**
 * What is done with the result of a propagation. Whoever needs the result registers a consumer as a bean; the propagation does not
 * know what any of them does. Every instance calls its consumers, and a consumer may get the same result again after a failure.
 */
public interface PropagationConsumer {

    /** Called with a result that reached at least one record besides those that changed. */
    void accept(PropagationResult result);
}
