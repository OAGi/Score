package org.oagi.score.gateway.http.api.bie_management.service.state_transition.rule;

import org.oagi.score.gateway.http.api.bie_management.service.state_transition.BieStateTransitionDependency;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListStateLevel;
import org.springframework.stereotype.Component;

/**
 * Validates the owner-specific compatibility between a BIE and one assigned
 * code list.
 *
 * <p>The rule is evaluated from the BIE's point of view: if the projected code
 * list state is not compatible with the projected BIE state, the edge is
 * rejected.</p>
 */
@Component
public class UsesCodeListStateTransitionRule implements BieStateTransitionRule {

    /**
     * Rejects the edge when the projected code-list state would be below the
     * projected BIE state under the owner-specific compatibility rules.
     */
    @Override
    public void validate(FutureStateCarrier<?, ?> source,
                         FutureStateCarrier<?, ?> target,
                         BieStateTransitionDependency dependency) throws BieStateTransitionRuleViolationException {
        if (dependency != BieStateTransitionDependency.USES_CODE_LIST ||
                !(source instanceof BieFutureStateCarrier bieSource) ||
                !(target instanceof CodeListFutureStateCarrier codeListTarget) ||
                bieSource.record() == null ||
                bieSource.futureState() == null ||
                codeListTarget.record() == null ||
                codeListTarget.futureState() == null) {
            return;
        }

        if (!CodeListStateLevel.isCompatible(
                bieSource.futureState(),
                codeListTarget.futureState(),
                codeListTarget.record().owner())) {
            throw new BieStateTransitionRuleViolationException();
        }
    }
}
