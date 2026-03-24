package org.oagi.score.gateway.http.api.bie_management.model;

import java.util.List;

/**
 * Compatibility rules between requested BIE target states and acceptable BIE
 * states for related dependencies.
 *
 * <p>A related BIE may satisfy a requested target state when it is already in
 * the same state or a higher state:
 * WIP -> WIP, QA, Production
 * QA -> QA, Production
 * Production -> Production
 *
 * <p>Discard is a virtual transition target used only during dependency
 * evaluation. It is not a persisted BIE state in the database. Discard is
 * handled by the dependency-specific rule implementations rather than by this
 * generic level comparison.</p>
 */
public final class BieStateLevel {

    private BieStateLevel() {
    }

    /**
     * Returns all BIE states that satisfy the requested target state.
     */
    public static List<BieState> compatibleStates(BieState bieState) {
        if (bieState == null) {
            return List.of();
        }

        return switch (bieState) {
            case WIP -> List.of(BieState.WIP, BieState.QA, BieState.Production);
            case QA -> List.of(BieState.QA, BieState.Production);
            case Production -> List.of(BieState.Production);
            default -> List.of();
        };
    }

    /**
     * Returns whether one candidate BIE state satisfies the requested target
     * state.
     */
    public static boolean isCompatible(BieState requestedState, BieState candidateState) {
        return requestedState != null &&
                candidateState != null &&
                compatibleStates(requestedState).contains(candidateState);
    }
}
