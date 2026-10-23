package org.oagi.score.gateway.http.change.propagation;

import org.oagi.score.gateway.http.change.ChangeEvent;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Runs the handlers until nothing new is derived. The events a handler derives stay in the engine: the whole cascade is computed
 * here, in waves.
 */
public final class PropagationEngine {

    /** More waves than the handlers have types to hear means a rule loops. */
    private final int maxWaves;

    private final List<PropagationHandler> handlers;

    public PropagationEngine(List<PropagationHandler> handlers) {
        this.handlers = List.copyOf(handlers);
        this.maxWaves = this.handlers.stream().flatMap(handler -> handler.subscribesTo().stream()).distinct().mapToInt(type -> 4).sum() + 4;
    }

    public PropagationResult propagate(Collection<ChangeEvent> source) {
        List<ChangeEvent> sourceEvents = List.copyOf(source);
        // What was handled of each record, so that a record is not handled again for the same columns, whichever way it was reached:
        // this ends duplicates and cycles. A null value means every column.
        Map<String, Set<String>> seen = new HashMap<>();
        List<ChangeEvent> wave = new ArrayList<>();
        for (ChangeEvent event : sourceEvents) {
            ChangeEvent unseen = unseen(seen, event);
            if (unseen != null) {
                wave.add(unseen);
            }
        }
        List<List<Effect>> waves = new ArrayList<>();
        while (!wave.isEmpty()) {
            if (waves.size() >= maxWaves) {
                throw new IllegalStateException("The propagation rules do not end: " + maxWaves + " waves from " + sourceEvents);
            }
            List<Effect> effects = new ArrayList<>();
            List<ChangeEvent> next = new ArrayList<>();
            for (PropagationHandler handler : handlers) {
                List<ChangeEvent> heard = wave.stream().filter(e -> handler.subscribesTo().contains(e.type())).toList();
                if (heard.isEmpty()) {
                    continue;
                }
                for (Effect effect : handler.handle(heard)) {
                    ChangeEvent unseen = unseen(seen, effect.event());
                    if (unseen != null) {
                        effects.add(new Effect(effect.cause(), unseen));
                        next.add(unseen);
                    }
                }
            }
            if (!effects.isEmpty()) {
                waves.add(effects);
            }
            wave = next;
        }
        return new PropagationResult(sourceEvents, waves);
    }

    /** The event restricted to the columns that were not handled yet for its record, or null when there are none. */
    private static ChangeEvent unseen(Map<String, Set<String>> seen, ChangeEvent event) {
        String ref = event.ref();
        if (!seen.containsKey(ref)) {
            seen.put(ref, (event.changed() != null) ? new HashSet<>(event.changed()) : null);
            return event;
        }
        Set<String> handled = seen.get(ref);
        if (handled == null) {
            return null;
        }
        if (event.changed() == null) {
            seen.put(ref, null);
            return event;
        }
        Set<String> added = new HashSet<>(event.changed());
        added.removeAll(handled);
        if (added.isEmpty()) {
            return null;
        }
        handled.addAll(added);
        return event.withChanged(added);
    }
}
