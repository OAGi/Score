package org.oagi.score.gateway.http.change.propagation;

import org.oagi.score.gateway.http.api.account_management.model.AccountRecordTypes;
import org.oagi.score.gateway.http.api.cc_management.model.CcRecordTypes;
import org.oagi.score.gateway.http.api.library_management.model.LibraryRecordTypes;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseRecordTypes;
import org.oagi.score.gateway.http.api.tag_management.model.TagRecordTypes;

import org.junit.jupiter.api.Test;
import static org.oagi.score.gateway.http.change.TestIds.event;
import static org.oagi.score.gateway.http.change.TestIds.id;
import org.oagi.score.gateway.http.change.ChangeEvent;
import org.oagi.score.gateway.http.change.RecordType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The engine with handlers of its own: it knows nothing of core components or of a cache. */
class PropagationEngineTest {

    /** Affects the record of {@code affects} with the id plus 100, for every event of {@code hears}. */
    private static PropagationHandler chain(RecordType hears, RecordType affects) {
        return new PropagationHandler() {
            public String name() { return hears + "->" + affects; }
            public Set<RecordType> subscribesTo() { return Set.of(hears); }
            public List<Effect> handle(List<ChangeEvent> events) {
                List<Effect> effects = new ArrayList<>();
                events.forEach(e -> effects.add(new Effect(e, new ChangeEvent(affects, e.id().add(java.math.BigInteger.valueOf(100))))));
                return effects;
            }
        };
    }

    @Test
    void handlersRunInWavesToAFixedPoint() {
        PropagationEngine engine = new PropagationEngine(List.of(chain(TagRecordTypes.TAG, AccountRecordTypes.USER), chain(AccountRecordTypes.USER, LibraryRecordTypes.LIBRARY)));

        PropagationResult result = engine.propagate(List.of(event(TagRecordTypes.TAG, 1)));

        assertEquals(List.of("USER 101", "LIBRARY 201"), result.affected().stream().map(ChangeEvent::ref).toList());
        assertEquals(2, result.waves().size());
    }

    @Test
    void aTypeNobodyHearsSpreadsNowhere() {
        PropagationResult result = new PropagationEngine(List.of(chain(TagRecordTypes.TAG, AccountRecordTypes.USER)))
                .propagate(List.of(event(ReleaseRecordTypes.RELEASE, 1)));

        assertTrue(result.waves().isEmpty());
        assertTrue(result.describe().contains("nothing depends on it"));
    }

    @Test
    void rulesThatLoopAreStopped() {
        // A handler that derives a new record on every call never reaches a fixed point.
        long[] next = {100};
        PropagationHandler endless = new PropagationHandler() {
            public String name() { return "endless"; }
            public Set<RecordType> subscribesTo() { return Set.of(CcRecordTypes.ACC); }
            public List<Effect> handle(List<ChangeEvent> events) {
                return List.of(new Effect(events.get(0), event(CcRecordTypes.ACC, next[0]++)));
            }
        };

        assertThrows(IllegalStateException.class,
                () -> new PropagationEngine(List.of(endless)).propagate(List.of(event(CcRecordTypes.ACC, 1))));
    }

    @Test
    void aRecordIsHandledAgainForAColumnThatWasNotHandledYet() {
        // TAG -> USER with the column "a", and again with "b": the second brings something new, the third nothing
        PropagationHandler producing = new PropagationHandler() {
            public String name() { return "producing"; }
            public Set<RecordType> subscribesTo() { return Set.of(TagRecordTypes.TAG); }
            public List<Effect> handle(List<ChangeEvent> events) {
                List<Effect> effects = new ArrayList<>();
                for (ChangeEvent e : events) {
                    effects.add(new Effect(e, new ChangeEvent(AccountRecordTypes.USER, id(1), null, null, Set.of("a"))));
                    effects.add(new Effect(e, new ChangeEvent(AccountRecordTypes.USER, id(1), null, null, Set.of("a", "b"))));
                    effects.add(new Effect(e, new ChangeEvent(AccountRecordTypes.USER, id(1), null, null, Set.of("b"))));
                }
                return effects;
            }
        };

        PropagationResult result = new PropagationEngine(List.of(producing)).propagate(List.of(event(TagRecordTypes.TAG, 1)));

        assertEquals(List.of(Set.of("a"), Set.of("b")), result.affected().stream().map(ChangeEvent::changed).toList());
    }

    @Test
    void theRowsOfOneRecordAreOneEventWithTheColumnsOfAll() {
        var entries = List.of(
                new org.oagi.score.gateway.http.change.ChangeLogEntry(1, CcRecordTypes.ACC, id(1), null, null, Set.of("den")),
                new org.oagi.score.gateway.http.change.ChangeLogEntry(2, CcRecordTypes.ACC, id(1), null, null, Set.of("object_class_term")),
                new org.oagi.score.gateway.http.change.ChangeLogEntry(3, CcRecordTypes.ASCCP, id(1), null, null, Set.of("den")),
                new org.oagi.score.gateway.http.change.ChangeLogEntry(4, CcRecordTypes.ASCCP, id(1), null, null, null),
                new org.oagi.score.gateway.http.change.ChangeLogEntry(5, CcRecordTypes.ASCCP, id(1), null, null, Set.of("den")));

        List<ChangeEvent> events = ChangePropagator.eventsOf(entries);

        assertEquals(2, events.size());
        assertEquals(Set.of("den", "object_class_term"), events.get(0).changed());
        assertNull(events.get(1).changed(), "one row that does not say makes the event say so");
    }
}
