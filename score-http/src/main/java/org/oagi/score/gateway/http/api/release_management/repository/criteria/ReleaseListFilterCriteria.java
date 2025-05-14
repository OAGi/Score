package org.oagi.score.gateway.http.api.release_management.repository.criteria;

import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseState;
import org.oagi.score.gateway.http.common.model.DateRangeCriteria;

import java.util.Collection;

/**
 * Represents filtering criteria for querying a list of releases.
 * Allows filtering by library ID, release number, exclusion lists, release states,
 * namespaceIds, creators, update timestamps, and more.
 *
 * @param libraryId                 the ID of the library to filter releases by.
 * @param releaseNum                the release number to filter by.
 * @param excludeReleaseNumSet     the set of release numbers to exclude from the results.
 * @param releaseStateSet           the set of release states to filter by.
 * @param namespaceIdSet            the set of namespace IDs to filter by.
 * @param creatorLoginIdSet         the set of creator login IDs to filter by.
 * @param createdTimestampRange     the date range for filtering by creation timestamp.
 * @param updaterLoginIdSet         the set of updater login IDs to filter by.
 * @param lastUpdatedTimestampRange the date range for filtering by last update timestamp.
 */
public record ReleaseListFilterCriteria(LibraryId libraryId,
                                        String releaseNum, Collection<String> excludeReleaseNumSet,
                                        Collection<ReleaseState> releaseStateSet,
                                        Collection<NamespaceId> namespaceIdSet,
                                        Collection<String> creatorLoginIdSet, DateRangeCriteria createdTimestampRange,
                                        Collection<String> updaterLoginIdSet,
                                        DateRangeCriteria lastUpdatedTimestampRange) {
}
