package org.oagi.score.gateway.http.api.oas_management.service;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.oas_management.model.OperationErrorResponseAssignment;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Issue #1347: unit tests for the pure document-level Error Response Body Type policy — the bulk-apply
 * targeting (release-scoped for CONFIRM_MESSAGE) and new-operation inheritance rules.
 */
class ErrorResponseBulkPolicyTest {

    private static BigInteger n(long v) {
        return BigInteger.valueOf(v);
    }

    private static Map<BigInteger, Set<BigInteger>> releasesByOp(Object... pairs) {
        Map<BigInteger, Set<BigInteger>> map = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            map.put(n((Long) pairs[i]), (Set<BigInteger>) pairs[i + 1]);
        }
        return map;
    }

    // ---------------------------------------------------------------- bulk assignments

    @Test
    void bulk_none_and_problemDetails_targetEveryOperation_regardlessOfRelease() {
        Map<BigInteger, Set<BigInteger>> ops = releasesByOp(
                1L, Set.of(n(11)), 2L, Set.of(n(12)), 3L, Set.of()); // op3 bodyless

        for (String type : List.of("NONE", "PROBLEM_DETAILS")) {
            List<OperationErrorResponseAssignment> out =
                    ErrorResponseBulkPolicy.resolveBulkAssignments(ops, type, null, null);
            assertEquals(3, out.size(), type + " applies to every operation");
            out.forEach(a -> {
                assertEquals(type, a.getErrorResponseBodyType());
                assertNull(a.getConfirmTopLevelAsbiepId(), "no ConfirmMessage BIE for " + type);
            });
        }
    }

    @Test
    void bulk_confirmMessage_targetsChosenReleaseAndBodyless_leavesOtherReleasesUntouched() {
        Map<BigInteger, Set<BigInteger>> ops = releasesByOp(
                1L, Set.of(n(11)),   // matches release 11
                2L, Set.of(n(12)),   // other release -> untouched
                3L, Set.of());       // bodyless -> always included

        List<OperationErrorResponseAssignment> out = ErrorResponseBulkPolicy.resolveBulkAssignments(
                ops, "CONFIRM_MESSAGE", n(11), n(500));

        assertEquals(2, out.size(), "release 11 op + bodyless op only");
        assertTrue(out.stream().anyMatch(a -> a.getOasOperationId().equals(n(1))), "release-11 op included");
        assertTrue(out.stream().anyMatch(a -> a.getOasOperationId().equals(n(3))), "bodyless op included");
        assertTrue(out.stream().noneMatch(a -> a.getOasOperationId().equals(n(2))), "release-12 op left unchanged");
        out.forEach(a -> {
            assertEquals("CONFIRM_MESSAGE", a.getErrorResponseBodyType());
            assertEquals(n(500), a.getConfirmTopLevelAsbiepId());
        });
    }

    // ---------------------------------------------------------------- inheritance

    @Test
    void inherit_allProblemDetails_newOpInheritsProblemDetails() {
        Map<BigInteger, String> types = Map.of(n(1), "PROBLEM_DETAILS", n(2), "PROBLEM_DETAILS");
        ErrorResponseBulkPolicy.Inherited inherited = ErrorResponseBulkPolicy.computeInherited(
                types, Map.of(), releasesByOp(1L, Set.of(n(11)), 2L, Set.of(n(12))), Set.of(n(13)));
        assertEquals("PROBLEM_DETAILS", inherited.bodyType());
        assertNull(inherited.confirmTopLevelAsbiepId());
    }

    @Test
    void inherit_allConfirmMessage_newBieMatchesItsReleaseConfirmMessage() {
        // release 11 ops use CM 501; release 12 ops use CM 502
        Map<BigInteger, String> types = Map.of(n(1), "CONFIRM_MESSAGE", n(2), "CONFIRM_MESSAGE");
        Map<BigInteger, BigInteger> confirms = Map.of(n(1), n(501), n(2), n(502));
        Map<BigInteger, Set<BigInteger>> rels = releasesByOp(1L, Set.of(n(11)), 2L, Set.of(n(12)));

        // adding a release-11 BIE -> inherits 11's CM (501)
        ErrorResponseBulkPolicy.Inherited r11 =
                ErrorResponseBulkPolicy.computeInherited(types, confirms, rels, Set.of(n(11)));
        assertEquals("CONFIRM_MESSAGE", r11.bodyType());
        assertEquals(n(501), r11.confirmTopLevelAsbiepId());

        // adding a release-13 BIE -> no CM for release 13 -> No Response Body
        ErrorResponseBulkPolicy.Inherited r13 =
                ErrorResponseBulkPolicy.computeInherited(types, confirms, rels, Set.of(n(13)));
        assertEquals("NONE", r13.bodyType());
        assertNull(r13.confirmTopLevelAsbiepId());
    }

    @Test
    void inherit_bodylessNewOp_takesSingleDocWideConfirmMessage_elseNone() {
        Map<BigInteger, String> types = Map.of(n(1), "CONFIRM_MESSAGE", n(2), "CONFIRM_MESSAGE");
        Map<BigInteger, Set<BigInteger>> rels = releasesByOp(1L, Set.of(n(11)), 2L, Set.of(n(12)));

        // one CM used across the whole doc -> a new bodyless op inherits it
        ErrorResponseBulkPolicy.Inherited single = ErrorResponseBulkPolicy.computeInherited(
                types, Map.of(n(1), n(501), n(2), n(501)), rels, Set.of());
        assertEquals("CONFIRM_MESSAGE", single.bodyType());
        assertEquals(n(501), single.confirmTopLevelAsbiepId());

        // two different CMs -> ambiguous -> No Response Body
        ErrorResponseBulkPolicy.Inherited ambiguous = ErrorResponseBulkPolicy.computeInherited(
                types, Map.of(n(1), n(501), n(2), n(502)), rels, Set.of());
        assertEquals("NONE", ambiguous.bodyType());
    }

    @Test
    void inherit_mixedOrEmptyOrAllNone_isNone() {
        Map<BigInteger, Set<BigInteger>> rels = releasesByOp(1L, Set.of(n(11)), 2L, Set.of(n(11)));
        // no other operations
        assertEquals("NONE", ErrorResponseBulkPolicy.computeInherited(Map.of(), Map.of(), Map.of(), Set.of(n(11))).bodyType());
        // all NONE
        assertEquals("NONE", ErrorResponseBulkPolicy.computeInherited(
                Map.of(n(1), "NONE", n(2), "NONE"), Map.of(), rels, Set.of(n(11))).bodyType());
        // mixed
        assertEquals("NONE", ErrorResponseBulkPolicy.computeInherited(
                Map.of(n(1), "PROBLEM_DETAILS", n(2), "CONFIRM_MESSAGE"),
                Map.of(n(2), n(501)), rels, Set.of(n(11))).bodyType());
    }
}
