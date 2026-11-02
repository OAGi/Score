package org.oagi.score.gateway.http.change.propagation;

import org.oagi.score.gateway.http.change.ChangeEvent;
import org.oagi.score.gateway.http.change.RecordType;

import java.util.List;
import java.util.Set;

/**
 * How a change spreads. A handler returns what is affected; it publishes nothing and keeps no state of its own, so a rule is
 * tested without a transport. What it looks up to find the dependents is given to it when it is made.
 */
public interface PropagationHandler {

    String name();

    /** The types of record whose change this handler spreads. */
    Set<RecordType> subscribesTo();

    /** {@code events} are all of a subscribed type; the handler is called once per wave. */
    List<Effect> handle(List<ChangeEvent> events);
}
