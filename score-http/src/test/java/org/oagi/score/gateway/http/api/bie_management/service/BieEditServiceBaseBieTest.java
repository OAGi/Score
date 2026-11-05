package org.oagi.score.gateway.http.api.bie_management.service;

import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BieEditServiceBaseBieTest {

    private static TopLevelAsbiepId id(long value) {
        return new TopLevelAsbiepId(BigInteger.valueOf(value));
    }

    @Test
    void a_bie_is_based_on_itself() {
        assertTrue(BieEditService.isBasedOn(id(1), id(1), e -> null));
    }

    @Test
    void a_base_chain_that_reaches_the_edited_bie_is_found() {
        // 3 is based on 2, which is based on 1: using 3 as the base of 1 would make a cycle
        Map<TopLevelAsbiepId, TopLevelAsbiepId> basedOf = new HashMap<>();
        basedOf.put(id(3), id(2));
        basedOf.put(id(2), id(1));

        assertTrue(BieEditService.isBasedOn(id(3), id(1), basedOf::get));
    }

    @Test
    void an_unrelated_base_chain_is_accepted() {
        Map<TopLevelAsbiepId, TopLevelAsbiepId> basedOf = new HashMap<>();
        basedOf.put(id(3), id(2));

        assertFalse(BieEditService.isBasedOn(id(3), id(1), basedOf::get));
    }

    @Test
    void an_existing_cycle_does_not_make_the_walk_endless() {
        Map<TopLevelAsbiepId, TopLevelAsbiepId> basedOf = new HashMap<>();
        basedOf.put(id(2), id(3));
        basedOf.put(id(3), id(2));

        assertFalse(BieEditService.isBasedOn(id(2), id(1), basedOf::get));
    }
}
