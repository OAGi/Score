package org.oagi.score.gateway.http.common.model;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.bie_management.model.bie_search.BieSearchAttribute;
import org.oagi.score.gateway.http.api.bie_management.model.bie_search.BieSearchPredicate;
import org.oagi.score.gateway.http.api.cc_management.model.cc_search.CcSearchAttribute;
import org.oagi.score.gateway.http.api.cc_management.model.cc_search.CcSearchPredicate;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TextSearchPredicatesTest {

    private static BieSearchPredicate predicate(String id, TextSearchOperator operator, String value) {
        return new BieSearchPredicate(id, BieSearchAttribute.DEFINITION, operator, value, false);
    }

    @Test
    void aSearchOfEitherTreeHasAtMostTwentyPredicatesOfAnIdAnOperatorAndAShortText() {
        assertDoesNotThrow(() -> TextSearchPredicates.validate(List.of(predicate("0", TextSearchOperator.CONTAINS, "x"))));
        assertDoesNotThrow(() -> TextSearchPredicates.validate(List.of(
                new CcSearchPredicate("0", CcSearchAttribute.DEFINITION, TextSearchOperator.EQUALS, "x".repeat(1000), true))));
        assertDoesNotThrow(() -> TextSearchPredicates.validate(
                IntStream.range(0, 20).mapToObj(i -> predicate(String.valueOf(i), TextSearchOperator.EQUALS, "x")).toList()));
    }

    @Test
    void aSearchWithTooManyPredicatesOrNoneAtAllIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> TextSearchPredicates.validate(
                IntStream.range(0, 21).mapToObj(i -> predicate(String.valueOf(i), TextSearchOperator.EQUALS, "x")).toList()));
        assertThrows(IllegalArgumentException.class, () -> TextSearchPredicates.validate(null));
    }

    @Test
    void aPredicateWithoutAnIdAnOperatorOrAValueOrWithTooLongATextIsRefused() {
        for (BieSearchPredicate invalid : List.of(
                predicate(null, TextSearchOperator.EQUALS, "x"),
                predicate("", TextSearchOperator.EQUALS, "x"),
                predicate("0", null, "x"),
                predicate("0", TextSearchOperator.EQUALS, null),
                predicate("0", TextSearchOperator.EQUALS, "x".repeat(1001)))) {
            assertThrows(IllegalArgumentException.class, () -> TextSearchPredicates.validate(Collections.singletonList(invalid)));
        }
    }
}
