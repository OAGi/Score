package org.oagi.score.gateway.http.api.bie_management.repository.jooq;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Choosing Value Constraint "None" in the BIE editor must clear the stored Default/Fixed Value.
 */
class BieValueConstraintUpdatesTest {

    private final String[] stored = new String[]{"old-default", "old-fixed"};

    private void apply(String defaultValue, String fixedValue) {
        BieValueConstraintUpdates.apply(defaultValue, fixedValue, v -> stored[0] = v, v -> stored[1] = v);
    }

    @Test
    void noneClearsBothValues() {
        apply("", "");
        assertNull(stored[0]);
        assertNull(stored[1]);
    }

    @Test
    void absentValuesClearBothValues() {
        apply(null, null);
        assertNull(stored[0]);
        assertNull(stored[1]);
    }

    @Test
    void defaultValueReplacesFixedValue() {
        apply("x", "");
        assertEquals("x", stored[0]);
        assertNull(stored[1]);
    }

    @Test
    void fixedValueReplacesDefaultValue() {
        apply("", "y");
        assertNull(stored[0]);
        assertEquals("y", stored[1]);
    }
}
