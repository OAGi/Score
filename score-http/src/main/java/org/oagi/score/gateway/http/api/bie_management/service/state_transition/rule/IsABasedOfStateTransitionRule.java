package org.oagi.score.gateway.http.api.bie_management.service.state_transition.rule;

import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.BieStateLevel;
import org.oagi.score.gateway.http.api.bie_management.service.state_transition.BieStateTransitionDependency;
import org.springframework.stereotype.Component;

/**
 * Validates backward compatibility for a
 * {@link BieStateTransitionDependency#IS_A_BASED_OF} edge.
 *
 * <p>The source BIE is a base of the target BIE, so the source may not end at
 * a lower state than the inherited target.</p>
 */
@Component
public class IsABasedOfStateTransitionRule implements BieStateTransitionRule {

    @Override
    public void validate(FutureStateCarrier<?, ?> source,
                         FutureStateCarrier<?, ?> target,
                         BieStateTransitionDependency dependency)
            throws BieStateTransitionRuleViolationException {
        if (dependency != BieStateTransitionDependency.IS_A_BASED_OF ||
                !(source instanceof BieFutureStateCarrier bieSource) ||
                !(target instanceof BieFutureStateCarrier bieTarget) ||
                bieSource.record() == null ||
                bieTarget.record() == null ||
                bieSource.futureState() == null ||
                bieTarget.futureState() == null) {
            return;
        }

        // Case: A is a base of B. If the derived B is discarded, the base A may still exist.
        if (bieTarget.futureState() == BieState.Discard) {
            return;
        }
        // Case: A is a base of B. If the base A is discarded while B survives, block it.
        if (bieSource.futureState() == BieState.Discard) {
            throw new BieStateTransitionRuleViolationException();
        }

        if (!BieStateLevel.isCompatible(bieTarget.futureState(), bieSource.futureState())) {
            throw new BieStateTransitionRuleViolationException();
        }
    }
}
