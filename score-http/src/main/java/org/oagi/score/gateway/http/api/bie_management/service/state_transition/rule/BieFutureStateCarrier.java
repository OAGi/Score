package org.oagi.score.gateway.http.api.bie_management.service.state_transition.rule;

import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;

/**
 * Future-state carrier for one BIE participating in a transition rule.
 *
 * <p>This keeps the current BIE summary together with the projected future
 * BIE state so rule implementations do not need to pass loosely-related values
 * around as separate parameters.</p>
 */
public record BieFutureStateCarrier(
        TopLevelAsbiepSummaryRecord record,
        BieState futureState) implements FutureStateCarrier<TopLevelAsbiepSummaryRecord, BieState> {
}
