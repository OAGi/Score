package org.oagi.score.gateway.http.api.release_management.repository.criteria;

import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseState;

import java.util.Collection;

/**
 * Represents filtering criteria for querying a list of release summaries.
 * Allows filtering by library ID and release states.
 *
 * @param libraryId       the ID of the library to filter releases by.
 * @param releaseStateSet the set of release states to filter by.
 */
public record ReleaseSummaryListFilterCriteria(LibraryId libraryId,
                                               Collection<ReleaseState> releaseStateSet) {
}
