package org.oagi.score.gateway.http.api.code_list_management.model;

import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;

import java.util.List;

/**
 * Compatibility rules between BIE states and assigned code-list states.
 *
 * <p>Assigned end-user code lists must be at the same state or a higher state
 * than the BIE:
 * WIP -> WIP, QA, Production
 * QA -> QA, Production
 * Production -> Production
 *
 * <p>Developer owned code lists assigned to BIEs must always be Published.</p>
 */
public final class CodeListStateLevel {

    private CodeListStateLevel() {
    }

    /**
     * Checks whether the current persisted state of a code list is compatible
     * with the given BIE state.
     */
    public static boolean isCompatible(BieState bieState, CodeListSummaryRecord codeList) {
        return codeList != null && isCompatible(bieState, codeList.state(), codeList.owner());
    }

    /**
     * Checks whether one code-list state is compatible with a BIE state under
     * the owner-specific compatibility rules.
     */
    public static boolean isCompatible(BieState bieState, CcState ccState, UserSummaryRecord owner) {
        return compatibleStates(bieState, owner).contains(ccState);
    }

    /**
     * Returns all code-list states that are compatible with the given BIE
     * state for the given code list.
     */
    public static List<CcState> compatibleStates(BieState bieState, CodeListSummaryRecord codeList) {
        return compatibleStates(bieState, (codeList != null) ? codeList.owner() : null);
    }

    /**
     * Returns all code-list states that satisfy one BIE state under the
     * owner-specific assignment rules.
     */
    public static List<CcState> compatibleStates(BieState bieState, UserSummaryRecord owner) {
        if (bieState == null) {
            return List.of();
        }

        boolean developerOwned = owner != null && owner.isDeveloper();
        if (developerOwned) {
            return List.of(CcState.Published);
        }

        return switch (bieState) {
            case WIP -> List.of(CcState.WIP, CcState.QA, CcState.Production);
            case QA -> List.of(CcState.QA, CcState.Production);
            case Production -> List.of(CcState.Production);
            default -> List.of();
        };
    }

    /**
     * Returns all BIE states that are compatible with the given code-list
     * state under the owner-specific assignment rules.
     */
    public static List<BieState> compatibleBieStates(CcState ccState, CodeListSummaryRecord codeList) {
        return compatibleBieStates(ccState, (codeList != null) ? codeList.owner() : null);
    }

    /**
     * Returns all BIE states that may assign a code list in the given state.
     */
    public static List<BieState> compatibleBieStates(CcState ccState, UserSummaryRecord owner) {
        if (ccState == null) {
            return List.of();
        }

        boolean developerOwned = owner != null && owner.isDeveloper();
        if (developerOwned) {
            return (ccState == CcState.Published) ?
                    List.of(BieState.WIP, BieState.QA, BieState.Production) :
                    List.of();
        }

        return switch (ccState) {
            case WIP -> List.of(BieState.WIP);
            case QA -> List.of(BieState.WIP, BieState.QA);
            case Production -> List.of(BieState.WIP, BieState.QA, BieState.Production);
            default -> List.of();
        };
    }

    /**
     * Returns the code-list state the UI should target when a same-owner code
     * list is explicitly cascaded together with a BIE transition.
     */
    public static CcState preferredCascadeTargetState(BieState bieState, CodeListSummaryRecord codeList) {
        return preferredCascadeTargetState(bieState, (codeList != null) ? codeList.owner() : null);
    }

    /**
     * Returns the preferred cascade state under the owner-specific assignment
     * rules.
     */
    public static CcState preferredCascadeTargetState(BieState bieState, UserSummaryRecord owner) {
        if (bieState == null) {
            return null;
        }

        boolean developerOwned = owner != null && owner.isDeveloper();
        if (developerOwned) {
            return CcState.Published;
        }

        return switch (bieState) {
            case WIP -> CcState.WIP;
            case QA -> CcState.QA;
            case Production -> CcState.Production;
            default -> null;
        };
    }
}
