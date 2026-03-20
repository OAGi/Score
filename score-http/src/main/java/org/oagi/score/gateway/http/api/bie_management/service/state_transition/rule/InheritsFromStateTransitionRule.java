package org.oagi.score.gateway.http.api.bie_management.service.state_transition.rule;

import org.oagi.score.gateway.http.api.bie_management.model.BieStateLevel;
import org.oagi.score.gateway.http.api.bie_management.service.state_transition.BieStateTransitionDependency;
import org.oagi.score.gateway.http.api.bie_management.service.state_transition.BieStateTransitionRuleViolationException;
import org.springframework.stereotype.Component;

/**
 * Validates forward compatibility for a
 * {@link BieStateTransitionDependency#INHERITS_FROM} edge.
 *
 * <p>The source BIE inherits from the target base BIE, so the source may not
 * end at a higher state than the base.</p>
 */
@Component
public class InheritsFromStateTransitionRule implements BieStateTransitionRule<BieFutureStateCarrier, BieFutureStateCarrier> {

    @Override
    public void validate(BieFutureStateCarrier source,
                         BieFutureStateCarrier target,
                         BieStateTransitionDependency dependency)
            throws BieStateTransitionRuleViolationException {
        if (dependency != BieStateTransitionDependency.INHERITS_FROM ||
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
