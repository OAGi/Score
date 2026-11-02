package org.oagi.score.gateway.http.change.propagation;

import org.oagi.score.gateway.http.change.ChangeEvent;
import org.oagi.score.gateway.http.change.ChangeLogEntry;
import org.oagi.score.gateway.http.change.RecordsChangedEvent;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * On a {@link RecordsChangedEvent}, has the {@link PropagationEngine} find the records the changes reach and gives the result to every
 * {@link PropagationConsumer}. A failure fails the event, so a consumer may get the same result again.
 */
@Component
public class ChangePropagator {

    private final PropagationEngine engine;
    private final ObjectProvider<PropagationConsumer> consumers;

    public ChangePropagator(PropagationEngine engine, ObjectProvider<PropagationConsumer> consumers) {
        this.engine = engine;
        this.consumers = consumers;
    }

    @EventListener
    @Order(2)
    public void on(RecordsChangedEvent event) {
        propagate(event.changes());
    }

    public void propagate(List<ChangeLogEntry> changes) {
        List<PropagationConsumer> all = consumers.orderedStream().toList();
        if (changes.isEmpty() || all.isEmpty()) {
            return;
        }
        PropagationResult result = engine.propagate(eventsOf(changes));
        if (!result.waves().isEmpty()) {
            all.forEach(consumer -> consumer.accept(result));
        }
    }

    /**
     * One event for each record: the rows of one record (its component row and its manifest row, say) say which columns changed,
     * and the event says all of them; a row that does not say makes the event say so.
     */
    static List<ChangeEvent> eventsOf(List<ChangeLogEntry> changes) {
        Map<String, ChangeEvent> events = new LinkedHashMap<>();
        for (ChangeLogEntry change : changes) {
            ChangeEvent event = new ChangeEvent(change.type(), change.id(), change.parentId(), change.releaseId(), change.changed());
            events.merge(event.ref(), event, (a, b) -> {
                if (a.changed() == null || b.changed() == null) {
                    return a.withChanged(null);
                }
                Set<String> all = new HashSet<>(a.changed());
                all.addAll(b.changed());
                return a.withChanged(all);
            });
        }
        return List.copyOf(events.values());
    }
}
