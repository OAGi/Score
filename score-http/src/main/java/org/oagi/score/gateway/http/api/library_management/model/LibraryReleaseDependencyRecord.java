package org.oagi.score.gateway.http.api.library_management.model;

import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseState;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseDepId;

/**
 * Record representing a release dependency as presented within library management views.
 *
 * @param releaseDepId the identifier of the underlying {@code release_dep} row, or {@code null} for selectable candidates.
 * @param releaseId the identifier of the dependency release.
 * @param libraryId the identifier of the library that owns the dependency release.
 * @param libraryName the display name of the library that owns the dependency release.
 * @param releaseNum the release number of the dependency release.
 * @param state the current state of the dependency release.
 * @param workingRelease {@code true} if the dependency release is the working release.
 */
public record LibraryReleaseDependencyRecord(
        ReleaseDepId releaseDepId,
        ReleaseId releaseId,
        LibraryId libraryId,
        String libraryName,
        String releaseNum,
        ReleaseState state,
        boolean workingRelease) {
}
