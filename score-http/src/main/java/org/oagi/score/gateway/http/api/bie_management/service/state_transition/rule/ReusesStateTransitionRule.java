package org.oagi.score.gateway.http.api.bie_management.service.state_transition.rule;

import org.oagi.score.gateway.http.api.bie_management.model.BieStateLevel;
import org.oagi.score.gateway.http.api.bie_management.service.state_transition.BieStateTransitionDependency;
import org.oagi.score.gateway.http.api.bie_management.service.state_transition.BieStateTransitionRuleViolationException;
import org.springframework.stereotype.Component;

/**
 * Validates forward compatibility for a {@link BieStateTransitionDependency#REUSES}
 * edge.
 *
 * <p>The source BIE reuses the target BIE, so the source may not end at a
 * higher state than the reused target.</p>
 */
@Component
public class ReusesStateTransitionRule implements BieStateTransitionRule {

    @Override
    public void validate(FutureStateCarrier<?, ?> source,
                         FutureStateCarrier<?, ?> target,
                         BieStateTransitionDependency dependency)
            throws BieStateTransitionRuleViolationException {
        if (dependency != BieStateTransitionDependency.REUSES ||
                !(source instanceof BieFutureStateCarrier bieSource) ||
                !(target instanceof BieFutureStateCarrier bieTarget) ||
                bieSource.record() == null ||
                bieTarget.record() == null ||
                bieSource.futureState() == null ||
                bieTarget.futureState() == null) {
            return;
        }

        if (!BieStateLevel.isCompatible(bieSource.futureState(), bieTarget.futureState())) {
            throw new BieStateTransitionRuleViolationException();
        }
    }
}
