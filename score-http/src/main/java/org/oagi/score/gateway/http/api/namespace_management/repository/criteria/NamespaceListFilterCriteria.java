package org.oagi.score.gateway.http.api.namespace_management.repository.criteria;

import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.common.model.DateRangeCriteria;

import java.util.Collection;

/**
 * Represents filtering criteria for querying a list of releases.
 * Allows filtering by library ID, release number, exclusion lists, release states,
 * namespaceIds, creators, update timestamps, and more.
 *
 * @param libraryId                 the ID of the library to filter releases by.
 * @param updaterLoginIdSet         the set of updater login IDs to filter by.
 * @param lastUpdatedTimestampRange the date range for filtering by last update timestamp.
 */
public record NamespaceListFilterCriteria(LibraryId libraryId,
                                          String uri, String prefix,
                                          String description, Boolean standard,
                                          Collection<String> ownerLoginIdSet,
                                          Collection<String> updaterLoginIdSet,
                                          DateRangeCriteria lastUpdatedTimestampRange) {

}
