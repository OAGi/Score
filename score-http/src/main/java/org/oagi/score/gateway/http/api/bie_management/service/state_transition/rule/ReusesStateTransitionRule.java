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
public class ReusesStateTransitionRule implements BieStateTransitionRule<BieFutureStateCarrier, BieFutureStateCarrier> {

    @Override
    public void validate(BieFutureStateCarrier source,
                         BieFutureStateCarrier target,
                         BieStateTransitionDependency dependency)
            throws BieStateTransitionRuleViolationException {
        if (dependency != BieStateTransitionDependency.REUSES ||
                source == null ||
                source.record() == null ||
                target == null ||
                target.record() == null ||
                source.futureState() == null ||
                target.futureState() == null) {
            return;
        }

        if (!BieStateLevel.isCompatible(source.futureState(), target.futureState())) {
            throw new BieStateTransitionRuleViolationException();
        }
    }
}
