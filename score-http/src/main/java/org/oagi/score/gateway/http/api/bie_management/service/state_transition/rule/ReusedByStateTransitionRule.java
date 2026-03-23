package org.oagi.score.gateway.http.api.bie_management.service.state_transition.rule;

import org.oagi.score.gateway.http.api.bie_management.model.BieStateLevel;
import org.oagi.score.gateway.http.api.bie_management.service.state_transition.BieStateTransitionDependency;
import org.oagi.score.gateway.http.api.bie_management.service.state_transition.BieStateTransitionRuleViolationException;
import org.springframework.stereotype.Component;

/**
 * Validates backward compatibility for a
 * {@link BieStateTransitionDependency#REUSED_BY} edge.
 *
 * <p>The source BIE is reused by the target BIE, so the source may not end at
 * a lower state than the reusing target.</p>
 */
@Component
public class ReusedByStateTransitionRule implements BieStateTransitionRule {

    @Override
    public void validate(FutureStateCarrier<?, ?> source,
                         FutureStateCarrier<?, ?> target,
                         BieStateTransitionDependency dependency)
            throws BieStateTransitionRuleViolationException {
        if (dependency != BieStateTransitionDependency.REUSED_BY ||
                !(source instanceof BieFutureStateCarrier bieSource) ||
                !(target instanceof BieFutureStateCarrier bieTarget) ||
                bieSource.record() == null ||
                bieTarget.record() == null ||
                bieSource.futureState() == null ||
                bieTarget.futureState() == null) {
            return;
        }

        if (!BieStateLevel.isCompatible(bieTarget.futureState(), bieSource.futureState())) {
            throw new BieStateTransitionRuleViolationException();
        }
    }
}
