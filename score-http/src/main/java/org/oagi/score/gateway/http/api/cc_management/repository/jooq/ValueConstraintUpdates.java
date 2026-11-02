package org.oagi.score.gateway.http.api.cc_management.repository.jooq;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.UpdateSetFirstStep;
import org.jooq.UpdateSetMoreStep;
import org.oagi.score.gateway.http.api.cc_management.model.ValueConstraint;

import static org.apache.commons.lang3.StringUtils.compare;

/**
 * Applies a requested {@link ValueConstraint} to the Fixed/Default Value columns of an UPDATE.
 * A value equal to the stored one is a no-op; the columns are cleared only when the request carries no value.
 */
final class ValueConstraintUpdates {

    private ValueConstraintUpdates() {
    }

    static <R extends Record> UpdateSetMoreStep<R> apply(UpdateSetFirstStep<R> firstStep,
                                                         UpdateSetMoreStep<R> moreStep,
                                                         ValueConstraint stored,
                                                         ValueConstraint requested,
                                                         Field<String> fixedColumn,
                                                         Field<String> defaultColumn) {
        if (requested.hasFixedValue()) {
            if (stored == null || compare(stored.fixedValue(), requested.fixedValue()) != 0) {
                return ((moreStep != null) ? moreStep : firstStep)
                        .set(fixedColumn, requested.fixedValue())
                        .setNull(defaultColumn);
            }
        } else if (requested.hasDefaultValue()) {
            if (stored == null || compare(stored.defaultValue(), requested.defaultValue()) != 0) {
                return ((moreStep != null) ? moreStep : firstStep)
                        .set(defaultColumn, requested.defaultValue())
                        .setNull(fixedColumn);
            }
        } else {
            return ((moreStep != null) ? moreStep : firstStep)
                    .setNull(defaultColumn)
                    .setNull(fixedColumn);
        }
        return moreStep;
    }
}
