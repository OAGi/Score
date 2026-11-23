package org.oagi.score.gateway.http.api.bie_management.model;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.cc_management.model.ValueConstraint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Choosing Value Constraint "None" in the BIE editor must clear the stored Default/Fixed Value.
 */
class BieValueConstraintUpdatesTest {

    private final String[] stored = new String[]{"old-default", "old-fixed"};

    private void apply(String defaultValue, String fixedValue) {
        BieValueConstraintUpdates.apply(new ValueConstraint(defaultValue, fixedValue), v -> stored[0] = v, v -> stored[1] = v);
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

    @Test
    void an_unchanged_fixed_or_default_value_is_not_a_changed_value_constraint() {
        assertThat(BieValueConstraintUpdates.changed(new ValueConstraint(null, "ABC"), new ValueConstraint(null, "ABC"))).isFalse();
        assertThat(BieValueConstraintUpdates.changed(new ValueConstraint("D", null), new ValueConstraint("D", null))).isFalse();
        assertThat(BieValueConstraintUpdates.changed(new ValueConstraint(null, null), new ValueConstraint(null, null))).isFalse();
        assertThat(BieValueConstraintUpdates.changed(new ValueConstraint(null, ""), new ValueConstraint("", null))).isFalse();
    }

    @Test
    void an_added_removed_or_changed_fixed_or_default_value_is_a_changed_value_constraint() {
        assertThat(BieValueConstraintUpdates.changed(new ValueConstraint(null, "ABC"), new ValueConstraint(null, "XYZ"))).isTrue();
        assertThat(BieValueConstraintUpdates.changed(new ValueConstraint(null, "ABC"), new ValueConstraint(null, null))).isTrue();
        assertThat(BieValueConstraintUpdates.changed(new ValueConstraint(null, null), new ValueConstraint(null, "ABC"))).isTrue();
        assertThat(BieValueConstraintUpdates.changed(new ValueConstraint("D", null), new ValueConstraint("E", null))).isTrue();
        assertThat(BieValueConstraintUpdates.changed(new ValueConstraint(null, null), new ValueConstraint("D", null))).isTrue();
        assertThat(BieValueConstraintUpdates.changed(new ValueConstraint(null, "ABC"), new ValueConstraint("ABC", null))).isTrue();
        assertThat(BieValueConstraintUpdates.changed(new ValueConstraint("ABC", null), new ValueConstraint(null, "ABC"))).isTrue();
    }
}
