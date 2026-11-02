package org.oagi.score.gateway.http.api.cc_management.service.dsl;

import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Chained and/or terms of the Core Component search DSL.
 */
public class DSLInterpreterTest {

    private static boolean eval(String dsl, boolean a, boolean b, boolean c) {
        StringBuilder content = new StringBuilder();
        if (a) content.append("aaa ");
        if (b) content.append("bbb ");
        if (c) content.append("ccc ");
        Predicate<DSLRecord> predicate = DSLInterpreter.parseExpression(dsl);
        return predicate.test(new DSLInterpreter.StringDSLRecord("", content.toString(), 0));
    }

    private static void assertTruthTable(String dsl, TriPredicate expected) {
        for (int bits = 0; bits < 8; bits++) {
            boolean a = (bits & 1) != 0;
            boolean b = (bits & 2) != 0;
            boolean c = (bits & 4) != 0;
            assertEquals(expected.test(a, b, c), eval(dsl, a, b, c), dsl + " for a=" + a + " b=" + b + " c=" + c);
        }
    }

    private interface TriPredicate {
        boolean test(boolean a, boolean b, boolean c);
    }

    private static final String A = "contains(\"aaa\")";
    private static final String B = "contains(\"bbb\")";
    private static final String C = "contains(\"ccc\")";

    @Test
    public void twoTerms() {
        assertTruthTable(A + " and " + B, (a, b, c) -> a && b);
        assertTruthTable(A + " or " + B, (a, b, c) -> a || b);
    }

    @Test
    public void andChain() {
        assertTruthTable(A + " and " + B + " and " + C, (a, b, c) -> a && b && c);
    }

    @Test
    public void orChain() {
        assertTruthTable(A + " or " + B + " or " + C, (a, b, c) -> a || b || c);
    }

    @Test
    public void andThenOr() {
        assertTruthTable(A + " and " + B + " or " + C, (a, b, c) -> (a && b) || c);
    }

    @Test
    public void orThenAnd() {
        assertTruthTable(A + " or " + B + " and " + C, (a, b, c) -> a || (b && c));
    }

    @Test
    public void fourTermsMixed() {
        assertTruthTable(A + " and " + B + " or " + C + " and " + A, (a, b, c) -> (a && b) || (c && a));
    }
}
