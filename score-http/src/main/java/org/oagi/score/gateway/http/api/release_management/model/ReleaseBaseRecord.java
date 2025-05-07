package org.oagi.score.gateway.http.api.release_management.model;

/**
 * Base interface for all release-related records.
 * Defines common methods for retrieving release details and identifying a "Working" release.
 */
public interface ReleaseBaseRecord {

    /**
     * Retrieves the unique identifier of the release.
     *
     * @return the release ID.
     */
    ReleaseId releaseId();

    /**
     * Retrieves the release number.
     *
     * @return the release number as a string.
     */
    String releaseNum();

    /**
     * Determines whether this release is a "Working" release.
     * A "Working" release represents the latest active development version.
     *
     * @return {@code true} if this is a "Working" release, otherwise {@code false}.
     */
    default boolean isWorkingRelease() {
        return "Working".equals(releaseNum());
    }
}
