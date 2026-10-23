package org.oagi.score.gateway.http.api.cc_management.propagation;

import org.oagi.score.gateway.http.api.tag_management.model.TagRecordTypes;

import org.oagi.score.gateway.http.cache.RecordChangeKeyResolver;
import org.oagi.score.gateway.http.cache.TestRecordKeys;


import org.oagi.score.gateway.http.api.cc_management.propagation.CcDependencyLookup;
import org.oagi.score.gateway.http.api.cc_management.propagation.Dependent;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.change.ChangeEvent;
import org.oagi.score.gateway.http.common.model.Id;
import org.oagi.score.gateway.http.change.propagation.Effect;
import org.oagi.score.gateway.http.change.propagation.PropagationEngine;
import org.oagi.score.gateway.http.change.propagation.PropagationHandler;
import org.oagi.score.gateway.http.change.propagation.PropagationResult;
import org.junit.jupiter.api.Test;
import static org.oagi.score.gateway.http.change.TestIds.event;
import static org.oagi.score.gateway.http.change.TestIds.id;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.gateway.http.api.cc_management.model.CcRecordTypes.*;

/**
 * The propagation rules on a small graph held in memory (no database, no transport).
 * <pre>
 *   ACC 1 --ASCC 2--> ASCCP 4 (role of ACC 6)
 *   ACC 1 --BCC 3---> BCCP 5 (data type DT 7)
 *   ACC 6 --ASCC 10-> ASCCP 11 (role of ACC 1)
 * </pre>
 */
class CcPropagationTest {

    private static final RecordChangeKeyResolver KEYS = TestRecordKeys.resolver(null);


    private record Edge(long from, long to, Long parent) {
    }

    /** Edges of the graph; every lookup is a filter over them. */
    private static final class Graph implements CcDependencyLookup {
        final List<Edge> roleOf = new ArrayList<>();      // acc -> asccp
        final List<Edge> asccToAsccp = new ArrayList<>(); // asccp -> ascc (parent: acc)
        final List<Edge> asccInAcc = new ArrayList<>();   // acc -> ascc (parent: acc)
        final List<Edge> bccToBccp = new ArrayList<>();
        final List<Edge> bccInAcc = new ArrayList<>();
        final List<Edge> bccpOfDt = new ArrayList<>();
        int queries;

        private <S extends Id, D extends Id> List<Dependent<S, D>> find(List<Edge> edges, Collection<S> ids, Function<BigInteger, D> idOf) {
            queries++;
            List<Dependent<S, D>> found = new ArrayList<>();
            for (S id : ids) {
                for (Edge e : edges) {
                    if (e.from == id.value().longValue()) {
                        found.add(new Dependent<>(id, idOf.apply(BigInteger.valueOf(e.to)),
                                e.parent == null ? null : new AccManifestId(BigInteger.valueOf(e.parent)), null));
                    }
                }
            }
            return found;
        }

        public List<Dependent<AccManifestId, AsccpManifestId>> asccpsRoleOf(Collection<AccManifestId> ids) { return find(roleOf, ids, AsccpManifestId::new); }
        public List<Dependent<AsccpManifestId, AsccManifestId>> asccsToAsccp(Collection<AsccpManifestId> ids) { return find(asccToAsccp, ids, AsccManifestId::new); }
        public List<Dependent<AccManifestId, AsccManifestId>> asccsFromAcc(Collection<AccManifestId> ids) { return find(asccInAcc, ids, AsccManifestId::new); }
        public List<Dependent<BccpManifestId, BccManifestId>> bccsToBccp(Collection<BccpManifestId> ids) { return find(bccToBccp, ids, BccManifestId::new); }
        public List<Dependent<AccManifestId, BccManifestId>> bccsFromAcc(Collection<AccManifestId> ids) { return find(bccInAcc, ids, BccManifestId::new); }
        public List<Dependent<DtManifestId, BccpManifestId>> bccpsOfDt(Collection<DtManifestId> ids) { return find(bccpOfDt, ids, BccpManifestId::new); }
    }

    private static Graph graph() {
        Graph g = new Graph();
        g.roleOf.add(new Edge(6, 4, null));
        g.asccToAsccp.add(new Edge(4, 2, 1L));
        g.asccInAcc.add(new Edge(1, 2, 1L));
        g.bccToBccp.add(new Edge(5, 3, 1L));
        g.bccInAcc.add(new Edge(1, 3, 1L));
        g.bccpOfDt.add(new Edge(7, 5, null));
        // ACC 6 has ASCC 10 to ASCCP 11, whose role-of is ACC 1: ACC 1 -> ASCCP 11 -> ASCC 10 (in ACC 6)
        g.roleOf.add(new Edge(1, 11, null));
        g.asccToAsccp.add(new Edge(11, 10, 6L));
        g.asccInAcc.add(new Edge(6, 10, 6L));
        return g;
    }

    private static PropagationResult run(Graph g, ChangeEvent... source) {
        PropagationResult result = new PropagationEngine(CcHandlers.of(g)).propagate(List.of(source));
        System.out.println(result.describe(event -> KEYS.keysOfRecords(List.of(event))));
        return result;
    }

    /** The cache keys of the dependents (not those of the source records: the change log reader forgets those). */
    private static Set<String> keys(PropagationResult result) {
        return KEYS.keysOfRecords(result.affected());
    }

    private static List<String> refs(PropagationResult result) {
        return result.affected().stream().map(ChangeEvent::ref).toList();
    }

    @Test
    void anAsccpChangeForgetsTheAsccThatPointsToItAndTheListsOfItsAcc() {
        PropagationResult result = run(graph(), event(ASCCP, 4));

        assertEquals(List.of("ASCC 2"), refs(result));
        assertEquals(Set.of("ascc:2", "ascc-from-acc:1", "bcc-from-acc:1", "seq-key-from-acc:1"), keys(result));
        assertFalse(keys(result).contains("acc:1"), "the ACC record holds no value of the ASCCP");
    }

    @Test
    void anAccTermChangeReachesAsccpThenAsccAndTheAssociationsItOwns() {
        PropagationResult result = run(graph(), event(ACC, 6));

        // wave 1: ASCCP 4 (role of 6) and ASCC 10 (in 6); wave 2: ASCC 2 (points to ASCCP 4)
        assertEquals(2, result.waves().size());
        assertEquals(Set.of("ASCCP 4", "ASCC 10"), Set.copyOf(refs(result).subList(0, 2)));
        assertEquals("ASCC 2", refs(result).get(2));
        assertTrue(keys(result).containsAll(Set.of("asccp:4", "ascc:10", "ascc:2", "ascc-from-acc:1")));
        assertFalse(keys(result).contains("acc:6"), "the record's own keys are the change log reader's job");
    }

    @Test
    void aDtChangeReachesBccpAndBcc() {
        PropagationResult result = run(graph(), new ChangeEvent(DT, id(7)));

        // wave 1: BCCP 5; wave 2: BCC 3 (to BCCP 5)
        assertEquals(List.of("BCCP 5", "BCC 3"), refs(result));
        assertEquals(2, result.waves().size());
        assertTrue(keys(result).containsAll(Set.of("bccp:5", "bcc:3", "bcc-from-acc:1")));
    }

    @Test
    void aRecordReachedTwiceIsHandledOnce() {
        Graph g = graph();
        // ACC 1 and ASCCP 4 changed together: ASCC 2 is reached from both (from ACC 1, and to ASCCP 4)
        PropagationResult result = run(g, event(ACC, 1), event(ASCCP, 4));

        assertEquals(1, refs(result).stream().filter("ASCC 2"::equals).count());
    }

    @Test
    void aSourceEventIsNotReportedAsAffected() {
        // ACC 1 changes, ASCCP 11 (role of 1) is affected; ASCCP 11 is itself in the batch, so it is the reader's job
        PropagationResult result = run(graph(), event(ACC, 1), event(ASCCP, 11));

        assertFalse(refs(result).contains("ASCCP 11"));
        assertFalse(keys(result).contains("asccp:11"));
    }

    @Test
    void aBatchIsOneQueryPerLookupPerWave() {
        Graph g = graph();
        PropagationResult result = run(g, event(ACC, 1), event(ACC, 6));

        // wave 1: ASCCP, ASCC, BCC handlers ask once each (3 lookups: roleOf, asccsFromAcc, bccsFromAcc)
        // wave 2: the ASCC handler asks once for the ASCCPs, the BCC handler hears nothing
        assertEquals(4, g.queries);
        assertFalse(result.affected().isEmpty());
    }

    @Test
    void aRecordNothingDependsOnSpreadsNowhere() {
        PropagationResult result = run(graph(), new ChangeEvent(ASCC, id(2), id(1), null), event(TagRecordTypes.TAG, 1));

        assertTrue(result.waves().isEmpty());
        assertTrue(keys(result).isEmpty());
    }

    @Test
    void aChangeOfAnAccThatIsNotItsTermReachesNothing() {
        PropagationResult result = run(graph(), event(ACC, 6, "definition"), event(ACC, 1, "definition", "deprecated"));

        assertTrue(result.waves().isEmpty(), "a definition is not part of a DEN");
        assertTrue(keys(result).isEmpty());
    }

    @Test
    void aChangeOfTheTermOfAnAccReachesTheRecordsThatShowItsDenAndTheirsInTurn() {
        PropagationResult result = run(graph(), event(ACC, 6, "object_class_term", "definition"));

        // ASCCP 4 (role of ACC 6) and ASCC 10 (in ACC 6) get a new DEN; so does ASCC 2, which points to ASCCP 4 (a new DEN of an ASCCP)
        assertEquals(Set.of("ASCCP 4", "ASCC 10", "ASCC 2"), Set.copyOf(refs(result)));
        result.affected().forEach(e -> assertEquals(Set.of("den"), e.changed(), e.ref()));
    }

    @Test
    void aChangeOfAnAsccpThatIsNotItsTermOrDenReachesNothing() {
        assertTrue(run(graph(), event(ASCCP, 4, "definition", "nillable")).waves().isEmpty());
        assertEquals(List.of("ASCC 2"), refs(run(graph(), event(ASCCP, 4, "property_term"))));
        assertEquals(List.of("ASCC 2"), refs(run(graph(), event(ASCCP, 4, "den"))));
    }

    @Test
    void aChangeOfADataTypeReachesTheBccpsOnlyWhenWhatTheirDenIsMadeOfChanged() {
        assertTrue(run(graph(), event(DT, 7, "definition")).waves().stream().flatMap(List::stream)
                .noneMatch(e -> e.event().type().equals(BCCP)), "a definition is not part of a DEN");
        assertTrue(refs(run(graph(), event(DT, 7, "qualifier"))).contains("BCCP 5"));
        assertTrue(refs(run(graph(), event(DT, 7, "data_type_term"))).contains("BCCP 5"));
    }

    @Test
    void aRowThatDoesNotSayWhatChangedMeansAnythingMayHave() {
        // a record that was added or deleted: the log says nothing of the columns
        assertEquals(Set.of("ASCCP 4", "ASCC 10", "ASCC 2"), Set.copyOf(refs(run(graph(), event(ACC, 6)))));
    }
}
