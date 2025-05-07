package org.oagi.score.gateway.http.api.release_management.repository;

import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.release_management.model.*;
import org.oagi.score.gateway.http.api.release_management.repository.criteria.ReleaseListFilterCriteria;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.ResultAndCount;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Repository interface for querying release-related information.
 * Provides methods for retrieving release summaries, release lists, and detailed queries
 * on releases based on various filters such as release states, creator, and update timestamps.
 */
public interface ReleaseQueryRepository {

    /**
     * Retrieves the summary information of a release by its unique release ID.
     *
     * @param releaseId the unique identifier of the release.
     * @return a {@link ReleaseSummaryRecord} object containing the release summary.
     */
    ReleaseSummaryRecord getReleaseSummary(ReleaseId releaseId);

    /**
     * Retrieves the summary information of a release by its library ID and release number.
     *
     * @param libraryId  the unique identifier of the library.
     * @param releaseNum the release number associated with the library.
     * @return a {@link ReleaseSummaryRecord} object containing the release summary.
     */
    ReleaseSummaryRecord getReleaseSummary(LibraryId libraryId, String releaseNum);

    List<ReleaseSummaryRecord> getReleaseSummaryList(LibraryId libraryId);

    List<ReleaseSummaryRecord> getReleaseSummaryList(LibraryId libraryId, Collection<ReleaseState> releaseStateSet);

    List<ReleaseSummaryRecord> getDependentReleaseSummaryList(ReleaseId releaseId);

    /**
     * Retrieves a set of all included {@code ReleaseSummaryRecord}s for a given {@code ReleaseId},
     * including itself.
     *
     * <p>This method queries the database to find all releases that are directly or
     * indirectly included by the given {@code ReleaseId}. The retrieval process ensures
     * that each release is added only once to prevent infinite loops in case of cyclic relationships.</p>
     *
     * @param releaseId The root release ID whose included releases should be retrieved.
     * @return A {@code Set<ReleaseSummaryRecord>} containing the given release ID and all its included releases.
     */
    Set<ReleaseSummaryRecord> getIncludedReleaseSummaryList(ReleaseId releaseId);

    /**
     * Retrieves a mapping of ASCCP manifest IDs to their corresponding release IDs.
     *
     * @param asccpManifestIdList a list of ASCCP manifest IDs.
     * @return a map where the key is an {@link AsccpManifestId} and the value is the corresponding {@link ReleaseId}.
     */
    Map<AsccpManifestId, ReleaseId> getReleaseIdMapByAsccpManifestIdList(Collection<AsccpManifestId> asccpManifestIdList);

    /**
     * Retrieves the detailed information of a release by its unique release ID.
     *
     * @param releaseId the unique identifier of the release.
     * @return a {@link ReleaseDetailsRecord} object containing detailed release information.
     */
    ReleaseDetailsRecord getReleaseDetails(ReleaseId releaseId);

    ReleaseDetailsRecord getReleaseDetails(LibraryId libraryId, String releaseNum);

    /**
     * Retrieves a list of all release details.
     *
     * @return a list of {@link ReleaseDetailsRecord} objects containing release details.
     */
    List<ReleaseDetailsRecord> getReleaseDetailsList();

    /**
     * Retrieves a paginated list of release entries based on the provided filter criteria.
     *
     * @param filterCriteria the filtering criteria for retrieving release list entries.
     * @param pageRequest    the pagination request specifying page number and size.
     * @return a {@link ResultAndCount} object containing the filtered release list entries and the total number of results.
     */
    ResultAndCount<ReleaseListEntryRecord> getReleaseList(ReleaseListFilterCriteria filterCriteria,
                                                          PageRequest pageRequest);

    /**
     * Checks whether a release with the given release ID exists in the system.
     *
     * @param releaseId the unique identifier of the release.
     * @return {@code true} if the release exists, otherwise {@code false}.
     */
    boolean exists(ReleaseId releaseId);

    /**
     * Determines whether a release number already exists within the specified library.
     *
     * @param libraryId  the unique identifier of the library.
     * @param releaseNum the release number to check for duplication.
     * @return {@code true} if a release with the same number already exists in the library, otherwise {@code false}.
     */
    boolean hasDuplicateReleaseNumber(LibraryId libraryId, String releaseNum);

    /**
     * Checks whether the given release number is already used by another release in the same library,
     * excluding the current release.
     *
     * @param releaseId  the unique identifier of the current release.
     * @param releaseNum the release number to check for duplication.
     * @return {@code true} if another release in the same library has the same release number, otherwise {@code false}.
     */
    boolean hasDuplicateReleaseNumberExcludingCurrent(ReleaseId releaseId, String releaseNum);

    /**
     * Checks whether there are any records associated with a given namespace ID.
     *
     * @param namespaceId the unique identifier of the namespace.
     * @return {@code true} if records exist for the given namespace, otherwise {@code false}.
     */
    boolean hasRecordsByNamespaceId(NamespaceId namespaceId);

    AssignComponents getAssignComponents(ReleaseId releaseId);
}
