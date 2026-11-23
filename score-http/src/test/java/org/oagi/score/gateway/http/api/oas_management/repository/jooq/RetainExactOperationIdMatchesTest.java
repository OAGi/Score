package org.oagi.score.gateway.http.api.oas_management.repository.jooq;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The operation_id column is utf8mb3_general_ci, so the database matches "getOrder" with "GetOrder" (and
 * "a" with "a "). The OpenAPI operationId is case-sensitive, so only an exact match is a duplicate.
 */
class RetainExactOperationIdMatchesTest {

    @Test
    void caseDifferingCandidate_isNotAMatch() {
        assertEquals(Set.of(), JooqOasDocCommandRepository.retainExactMatches(
                List.of("getOrder"), List.of("GetOrder")));
    }

    @Test
    void trailingSpaceAndAccentDifferingCandidates_areNotAMatch() {
        assertEquals(Set.of(), JooqOasDocCommandRepository.retainExactMatches(
                List.of("a", "cafe"), List.of("a ", "café")));
    }

    @Test
    void identicalCandidate_isAMatch() {
        assertEquals(Set.of("getOrder"), JooqOasDocCommandRepository.retainExactMatches(
                List.of("getOrder", "putOrder"), List.of("GetOrder", "getOrder")));
    }
}
