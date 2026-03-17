package org.oagi.score.gateway.http.api.bie_management.service.state_transition.rule;

import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
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
public class InheritsFromStateTransitionRule implements BieStateTransitionRule {

    @Override
    public void validate(TopLevelAsbiepSummaryRecord source,
                         TopLevelAsbiepSummaryRecord target,
                         BieStateTransitionDependency dependency,
                         BieState sourceFutureState,
                         BieState targetFutureState) throws BieStateTransitionRuleViolationException {
        if (dependency != BieStateTransitionDependency.INHERITS_FROM ||
                source == null ||
                target == null ||
                sourceFutureState == null ||
                targetFutureState == null) {
            return;
        }

        if (sourceFutureState.getLevel() > targetFutureState.getLevel()) {
            throw new BieStateTransitionRuleViolationException();
        }
    }
}
