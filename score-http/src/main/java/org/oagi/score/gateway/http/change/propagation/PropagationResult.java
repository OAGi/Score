package org.oagi.score.gateway.http.change.propagation;

import org.oagi.score.gateway.http.change.ChangeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * What a run of the engine found.
 *
 * @param source the events the run started from
 * @param waves  the effects of each wave; wave 1 comes from the source events, wave 2 from the events wave 1 derived, and so on
 */
public record PropagationResult(List<ChangeEvent> source, List<List<Effect>> waves) {

    /** Every record the run affected, apart from the source records. */
    public List<ChangeEvent> affected() {
        List<ChangeEvent> events = new ArrayList<>();
        waves.forEach(wave -> wave.forEach(effect -> events.add(effect.event())));
        return events;
    }

    /** The run as text: who caused what, wave by wave. */
    public String describe() {
        return describe(event -> Set.of());
    }

    /** The run as text, with what {@code detail} says about each affected record. */
    public String describe(Function<ChangeEvent, Set<String>> detail) {
        StringBuilder text = new StringBuilder("changed: ");
        text.append(String.join(", ", source.stream().map(ChangeEvent::ref).toList())).append('\n');
        for (int i = 0; i < waves.size(); i++) {
            text.append("wave ").append(i + 1).append('\n');
            for (Effect effect : waves.get(i)) {
                text.append("  ").append(effect.cause().ref()).append(" -> ").append(effect.event().ref());
                Set<String> more = detail.apply(effect.event());
                text.append(more.isEmpty() ? "" : "   " + more).append('\n');
            }
        }
        if (waves.isEmpty()) {
            text.append("  nothing depends on it\n");
        }
        return text.toString();
    }
}
