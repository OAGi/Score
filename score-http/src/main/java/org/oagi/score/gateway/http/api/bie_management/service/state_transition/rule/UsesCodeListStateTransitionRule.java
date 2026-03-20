package org.oagi.score.gateway.http.api.bie_management.service.state_transition.rule;

import org.oagi.score.gateway.http.api.bie_management.service.state_transition.BieStateTransitionDependency;
import org.oagi.score.gateway.http.api.bie_management.service.state_transition.BieStateTransitionRuleViolationException;
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
public class UsesCodeListStateTransitionRule implements BieStateTransitionRule<BieFutureStateCarrier, CodeListFutureStateCarrier> {

    /**
     * Rejects the edge when the projected code-list state would be below the
     * projected BIE state under the owner-specific compatibility rules.
     */
    @Override
    public void validate(BieFutureStateCarrier source,
                         CodeListFutureStateCarrier target,
                         BieStateTransitionDependency dependency) throws BieStateTransitionRuleViolationException {
        if (dependency != BieStateTransitionDependency.USES_CODE_LIST ||
                source == null ||
                source.record() == null ||
                source.futureState() == null ||
                target == null ||
                target.record() == null ||
                target.futureState() == null) {
            return;
        }

        if (!CodeListStateLevel.isCompatible(
                source.futureState(),
                target.futureState(),
                target.record().owner())) {
            throw new BieStateTransitionRuleViolationException();
        }
    }
}
