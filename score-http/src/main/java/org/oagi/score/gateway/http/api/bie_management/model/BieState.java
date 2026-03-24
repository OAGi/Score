package org.oagi.score.gateway.http.api.bie_management.model;

/**
 * Lifecycle states for one BIE.
 *
 * <p>{@link #Initiating}, {@link #WIP}, {@link #QA}, and
 * {@link #Production} are persisted states. {@link #Discard} is a virtual
 * transition target used only while validating discard operations; discarded
 * BIEs are deleted from the database rather than stored in that state.</p>
 */
public enum BieState {

    /**
     * Virtual delete target used during dependency validation for discard.
     */
    Discard(-1),

    /**
     * Initial placeholder state before the BIE enters the normal workflow.
     */
    Initiating(0),

    /**
     * Editable working state. A WIP BIE may move to {@link #QA} or be discarded.
     */
    WIP(1),

    /**
     * Review state. A QA BIE may move back to {@link #WIP} or forward to
     * {@link #Production}.
     */
    QA(2),

    /**
     * Final published state. Production BIEs cannot move to any other state.
     */
    Production(3);

    private final int level;

    BieState(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    /**
     * Returns whether this state may transition directly to the requested state.
     */
    public boolean canMove(BieState nextState) {
        if (nextState == null) {
            return false;
        }

        return switch (this) {
            case WIP -> nextState == QA || nextState == Discard;
            case QA -> nextState == WIP || nextState == Production;
            default -> false;
        };
    }

}
