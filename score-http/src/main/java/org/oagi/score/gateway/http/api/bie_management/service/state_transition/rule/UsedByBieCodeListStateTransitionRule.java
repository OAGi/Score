package org.oagi.score.gateway.http.api.bie_management.service.state_transition.rule;

import org.oagi.score.gateway.http.api.bie_management.service.state_transition.BieStateTransitionDependency;
import org.oagi.score.gateway.http.api.bie_management.service.state_transition.BieStateTransitionRuleViolationException;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListStateLevel;
import org.springframework.stereotype.Component;

/**
 * Validates the inverse compatibility between one code list and the BIEs that
 * assign it.
 *
 * <p>The rule is evaluated from the code-list's point of view: if the
 * projected BIE state is not compatible with the projected code-list state,
 * the edge is rejected.</p>
 */
@Component
public class UsedByBieCodeListStateTransitionRule implements BieStateTransitionRule {

    @Override
    public void validate(FutureStateCarrier<?, ?> source,
                         FutureStateCarrier<?, ?> target,
                         BieStateTransitionDependency dependency)
            throws BieStateTransitionRuleViolationException {
        if (dependency != BieStateTransitionDependency.USED_BY_BIE ||
                !(source instanceof CodeListFutureStateCarrier codeListSource) ||
                !(target instanceof BieFutureStateCarrier bieTarget) ||
                codeListSource.record() == null ||
                codeListSource.futureState() == null ||
                bieTarget.record() == null ||
                bieTarget.futureState() == null) {
            return;
        }

        if (!CodeListStateLevel.compatibleBieStates(
                codeListSource.futureState(),
                codeListSource.record()).contains(bieTarget.futureState())) {
            throw new BieStateTransitionRuleViolationException();
        }
    }
}
