package org.oagi.score.gateway.http.api.release_management.model;

import org.oagi.score.gateway.http.api.library_management.model.LibraryId;

/**
 * Summary record for a direct release dependency, including the relationship row identifier.
 *
 * @param releaseDepId the identifier of the dependency relationship row.
 * @param releaseId the identifier of the dependent release.
 * @param libraryId the identifier of the library that owns the dependent release.
 * @param releaseNum the release number of the dependent release.
 * @param state the current state of the dependent release.
 */
public record ReleaseDependencySummaryRecord(
        ReleaseDepId releaseDepId,
        ReleaseId releaseId,
        LibraryId libraryId,
        String releaseNum,
        ReleaseState state) implements ReleaseBaseRecord {
}
