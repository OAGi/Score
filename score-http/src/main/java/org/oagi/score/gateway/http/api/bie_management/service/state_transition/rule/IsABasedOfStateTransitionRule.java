package org.oagi.score.gateway.http.api.bie_management.service.state_transition.rule;

import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.service.state_transition.BieStateTransitionDependency;
import org.oagi.score.gateway.http.api.bie_management.service.state_transition.BieStateTransitionRuleViolationException;
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
    public void validate(TopLevelAsbiepSummaryRecord source,
                         TopLevelAsbiepSummaryRecord target,
                         BieStateTransitionDependency dependency,
                         BieState sourceFutureState,
                         BieState targetFutureState) throws BieStateTransitionRuleViolationException {
        if (dependency != BieStateTransitionDependency.IS_A_BASED_OF ||
                source == null ||
                target == null ||
                sourceFutureState == null ||
                targetFutureState == null) {
            return;
        }

        if (sourceFutureState.getLevel() < targetFutureState.getLevel()) {
            throw new BieStateTransitionRuleViolationException();
        }
    }
}
