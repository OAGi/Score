package org.oagi.score.gateway.http.api.bie_management.service.state_transition.rule;

import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.BieStateLevel;
import org.oagi.score.gateway.http.api.bie_management.service.state_transition.BieStateTransitionDependency;
import org.springframework.stereotype.Component;

/**
 * Validates forward compatibility for a
 * {@link BieStateTransitionDependency#INHERITS_FROM} edge.
 *
 * <p>The source BIE inherits from the target base BIE, so the source may not
 * end at a higher state than the base.</p>
 */
@Component
public class InheritsFromStateTransitionRule implements BieStateTransitionRule {

    @Override
    public void validate(FutureStateCarrier<?, ?> source,
                         FutureStateCarrier<?, ?> target,
                         BieStateTransitionDependency dependency)
            throws BieStateTransitionRuleViolationException {
        if (dependency != BieStateTransitionDependency.INHERITS_FROM ||
                !(source instanceof BieFutureStateCarrier bieSource) ||
                !(target instanceof BieFutureStateCarrier bieTarget) ||
                bieSource.record() == null ||
                bieTarget.record() == null ||
                bieSource.futureState() == null ||
                bieTarget.futureState() == null) {
            return;
        }

        // Case: A inherits from B. If the derived A is discarded, the base B may still exist.
        if (bieSource.futureState() == BieState.Discard) {
            return;
        }
        // Case: A inherits from B. If the base B is discarded while A survives, block it.
        if (bieTarget.futureState() == BieState.Discard) {
            throw new BieStateTransitionRuleViolationException();
        }

        if (!BieStateLevel.isCompatible(bieSource.futureState(), bieTarget.futureState())) {
            throw new BieStateTransitionRuleViolationException();
        }
    }
}
