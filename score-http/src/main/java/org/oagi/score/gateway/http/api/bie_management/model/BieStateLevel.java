package org.oagi.score.gateway.http.api.bie_management.model;

import java.util.Arrays;
import java.util.List;

/**
 * Compatibility rules between requested BIE target states and acceptable BIE
 * states for related dependencies.
 *
 * <p>A related BIE may satisfy a requested target state when it is already in
 * the same state or a higher state:
 * WIP -> WIP, QA, Production
 * QA -> QA, Production
 * Production -> Production</p>
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

        return Arrays.stream(BieState.values())
                .filter(state -> state != BieState.Initiating)
                .filter(state -> state.getLevel() >= bieState.getLevel())
                .toList();
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
