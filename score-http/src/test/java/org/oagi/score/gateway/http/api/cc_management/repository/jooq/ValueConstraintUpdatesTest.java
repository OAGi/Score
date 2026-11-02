package org.oagi.score.gateway.http.api.cc_management.repository.jooq;

import org.jooq.SQLDialect;
import org.jooq.UpdateSetFirstStep;
import org.jooq.UpdateSetMoreStep;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.cc_management.model.ValueConstraint;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.BccpRecord;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.BCCP;

/**
 * An unchanged Fixed/Default Value must not be cleared by an unrelated save.
 */
class ValueConstraintUpdatesTest {

    private final UpdateSetFirstStep<BccpRecord> first = DSL.using(SQLDialect.MYSQL).update(BCCP);

    private UpdateSetMoreStep<BccpRecord> apply(ValueConstraint stored, ValueConstraint requested) {
        return ValueConstraintUpdates.apply(first, null, stored, requested, BCCP.FIXED_VALUE, BCCP.DEFAULT_VALUE);
    }

    private String sql(UpdateSetMoreStep<BccpRecord> step) {
        return step.getSQL(org.jooq.conf.ParamType.INLINED).toLowerCase();
    }

    @Test
    void unchangedFixedValueIsKept() {
        assertNull(apply(new ValueConstraint(null, "ABC"), new ValueConstraint(null, "ABC")));
    }

    @Test
    void unchangedDefaultValueIsKept() {
        assertNull(apply(new ValueConstraint("x", null), new ValueConstraint("x", null)));
    }

    @Test
    void changedFixedValueIsSetAndDefaultCleared() {
        String sql = sql(apply(new ValueConstraint(null, "ABC"), new ValueConstraint(null, "XYZ")));
        assertTrue(sql.contains("`fixed_value` = 'xyz'"), sql);
        assertTrue(sql.contains("`default_value` = null"), sql);
    }

    @Test
    void switchingFromDefaultToFixedClearsDefault() {
        String sql = sql(apply(new ValueConstraint("x", null), new ValueConstraint(null, "ABC")));
        assertTrue(sql.contains("`default_value` = null"), sql);
    }

    @Test
    void emptyRequestClearsBoth() {
        String sql = sql(apply(new ValueConstraint(null, "ABC"), new ValueConstraint(null, null)));
        assertTrue(sql.contains("`default_value` = null") && sql.contains("`fixed_value` = null"), sql);
    }

    @Test
    void defaultOnlyConstraintIsSaved() {
        String sql = sql(apply(null, new ValueConstraint("x", null)));
        assertTrue(sql.contains("`default_value` = 'x'") && sql.contains("`fixed_value` = null"), sql);
    }
}
