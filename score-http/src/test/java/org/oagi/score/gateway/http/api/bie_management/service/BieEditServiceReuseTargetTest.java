package org.oagi.score.gateway.http.api.bie_management.service;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BieEditServiceReuseTargetTest {

    private static final Function<TopLevelAsbiepId, TopLevelAsbiepId> NO_BASE = e -> null;

    private static TopLevelAsbiepId id(long value) {
        return new TopLevelAsbiepId(BigInteger.valueOf(value));
    }

    @Test
    void the_edited_bie_itself_is_refused() {
        assertThrows(IllegalArgumentException.class, () ->
                BieEditService.validateReuseTarget(id(1), id(1), false, false, NO_BASE));
    }

    @Test
    void a_bie_derived_from_the_edited_bie_is_refused() {
        // 3 is based on 2, which is based on 1
        Map<TopLevelAsbiepId, TopLevelAsbiepId> basedOf = new HashMap<>();
        basedOf.put(id(3), id(2));
        basedOf.put(id(2), id(1));

        assertThrows(IllegalArgumentException.class, () ->
                BieEditService.validateReuseTarget(id(1), id(3), false, false, basedOf::get));
    }

    @Test
    void an_unrelated_bie_is_accepted() {
        Map<TopLevelAsbiepId, TopLevelAsbiepId> basedOf = new HashMap<>();
        basedOf.put(id(3), id(2));

        assertDoesNotThrow(() ->
                BieEditService.validateReuseTarget(id(1), id(3), false, false, basedOf::get));
    }

    @Test
    void a_developer_cannot_reuse_the_bie_of_an_end_user() {
        assertThrows(IllegalArgumentException.class, () ->
                BieEditService.validateReuseTarget(id(1), id(2), true, false, NO_BASE));
        assertDoesNotThrow(() ->
                BieEditService.validateReuseTarget(id(1), id(2), true, true, NO_BASE));
    }

    @Test
    void an_end_user_can_reuse_the_bie_of_a_developer() {
        assertDoesNotThrow(() ->
                BieEditService.validateReuseTarget(id(1), id(2), false, true, NO_BASE));
    }
}
