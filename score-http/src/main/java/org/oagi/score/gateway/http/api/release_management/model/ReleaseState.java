package org.oagi.score.gateway.http.api.release_management.model;

/**
 * Enum representing the different states of a release.
 * Each state corresponds to a specific phase in the release lifecycle.
 */
public enum ReleaseState {

    /**
     * A release that was being processed, before it was told whether it was being drafted or published. Releases of earlier
     * versions may still be in this state; it is treated as being in the middle of a transition.
     */
    Processing,
    /**
     * The release has been asked to become a draft, and the work for it (the copy of what it holds) has not finished.
     * The library is not to be changed in the meantime; when the work fails, the release is 'Initialized' again.
     */
    Drafting,
    /**
     * The draft has been asked to be published, and the work for it has not finished. When it fails, the release is a
     * 'Draft' again.
     */
    Publishing,
    /**
     * The draft has been asked to go back to 'Initialized', and the work for it (the components become candidates again,
     * and what the release holds is removed) has not finished. The library is not to be changed in the meantime; when
     * the work fails, the release is a 'Draft' again.
     */
    Initializing,

    /**
     * Indicates that the release has been initialized.
     */
    Initialized,

    /**
     * Represents a draft version of the release, which may still be under development or review.
     */
    Draft,

    /**
     * Indicates that the release has been finalized and is available for use.
     */
    Published;

    /**
     * Whether the release is in the middle of a transition that is being done, so that its library is not to be changed.
     * {@link #Processing}, which no transition leaves a release in any more, is not: a release that is left in it is a remnant
     * of a transition that failed, and does not hold its library.
     */
    public boolean inProgress() {
        return this == Drafting || this == Publishing || this == Initializing;
    }

    /**
     * The state a release that is in the middle of a transition is given back to when the transition does not end: the
     * state it was in before it asked for the transition.
     */
    public ReleaseState givenBack() {
        return switch (this) {
            case Drafting -> Initialized;
            case Publishing, Initializing -> Draft;
            default -> throw new IllegalStateException("The release in '" + this + "' state is not in the middle of a transition.");
        };
    }
}
