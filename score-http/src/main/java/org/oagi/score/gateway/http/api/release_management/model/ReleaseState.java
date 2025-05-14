package org.oagi.score.gateway.http.api.release_management.model;

/**
 * Enum representing the different states of a release.
 * Each state corresponds to a specific phase in the release lifecycle.
 */
public enum ReleaseState {

    /**
     * Indicates that the release is in the process of being created.
     */
    Processing,

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
    Published

}
