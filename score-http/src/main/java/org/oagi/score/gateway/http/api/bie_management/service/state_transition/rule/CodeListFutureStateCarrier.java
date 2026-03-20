package org.oagi.score.gateway.http.api.bie_management.service.state_transition.rule;

import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListSummaryRecord;

/**
 * Future-state carrier for one code list participating in a transition rule.
 *
 * <p>This keeps the code list summary together with the projected future code
 * list state, which is clearer than passing an untyped tuple through the rule
 * layer.</p>
 */
public record CodeListFutureStateCarrier(
        CodeListSummaryRecord record,
        CcState futureState) implements FutureStateCarrier<CodeListSummaryRecord, CcState> {
}
