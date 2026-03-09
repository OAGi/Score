package org.oagi.score.gateway.http.api.bie_management.service.state_transition.rule;

import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
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
    public void validate(TopLevelAsbiepSummaryRecord source,
                         TopLevelAsbiepSummaryRecord target,
                         BieStateTransitionDependency dependency,
                         BieState sourceFutureState,
                         BieState targetFutureState) throws BieStateTransitionRuleViolationException {
        if (dependency != BieStateTransitionDependency.REUSED_BY ||
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
