package org.oagi.score.gateway.http.api.release_management.model;

import org.oagi.score.gateway.http.api.library_management.model.LibraryId;

/**
 * Record representing a summary of a release.
 * Includes essential release details such as release ID, number, state, and associated library.
 */
public record ReleaseSummaryRecord(ReleaseId releaseId, LibraryId libraryId, String releaseNum,
                                   ReleaseState state) implements ReleaseBaseRecord {
}
