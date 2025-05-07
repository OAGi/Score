package org.oagi.score.gateway.http.api.namespace_management.repository;

import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceDetailsRecord;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceListEntryRecord;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceSummaryRecord;
import org.oagi.score.gateway.http.api.namespace_management.repository.criteria.NamespaceListFilterCriteria;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.ResultAndCount;

import java.util.List;

/**
 * Repository interface for querying namespace-related data.
 */
public interface NamespaceQueryRepository {

    /**
     * Retrieves the summary information of a namespace by its unique namespace ID.
     *
     * @param namespaceId the unique identifier of the namespace.
     * @return a {@link NamespaceSummaryRecord} object containing the namespace summary.
     */
    NamespaceSummaryRecord getNamespaceSummary(NamespaceId namespaceId);

    /**
     * Retrieves a list of all namespace summaries.
     *
     * @return A list of {@link NamespaceSummaryRecord} objects.
     */
    List<NamespaceSummaryRecord> getNamespaceSummaryList();

    /**
     * Retrieves a list of namespace summaries associated with a specific library.
     *
     * @param libraryId The ID of the library.
     * @return A list of {@link NamespaceSummaryRecord} objects.
     */
    List<NamespaceSummaryRecord> getNamespaceSummaryList(LibraryId libraryId);

    NamespaceSummaryRecord getAnyStandardNamespaceSummary(LibraryId libraryId);

    /**
     * Retrieves a paginated list of namespaceIds based on the given filter criteria.
     *
     * @param filterCriteria The criteria for filtering namespaceIds.
     * @param pageRequest    The pagination request details.
     * @return A {@link ResultAndCount} object containing the results and total count.
     */
    ResultAndCount<NamespaceListEntryRecord> getNamespaceList(NamespaceListFilterCriteria filterCriteria,
                                                              PageRequest pageRequest);

    /**
     * Retrieves detailed information about a namespace.
     *
     * @param namespaceId The ID of the namespace.
     * @param requesterId The ID of the requesting user.
     * @return The {@link NamespaceDetailsRecord} for the namespace.
     */
    NamespaceDetailsRecord getNamespaceDetails(NamespaceId namespaceId, UserId requesterId);

    /**
     * Checks whether a namespace exists.
     *
     * @param namespaceId The ID of the namespace.
     * @return True if the namespace exists, false otherwise.
     */
    boolean exists(NamespaceId namespaceId);

    /**
     * Checks for duplicate namespace URIs within a library.
     *
     * @param libraryId The ID of the library.
     * @param uri       The URI to check.
     * @return True if a duplicate exists, false otherwise.
     */
    boolean hasDuplicateUri(LibraryId libraryId, String uri);

    /**
     * Checks for duplicate namespace URIs, excluding the current namespace.
     *
     * @param namespaceId The ID of the namespace to exclude.
     * @param uri         The URI to check.
     * @return True if a duplicate exists, false otherwise.
     */
    boolean hasDuplicateUriExcludingCurrent(NamespaceId namespaceId, String uri);

    /**
     * Checks for duplicate namespace prefixes within a library.
     *
     * @param libraryId The ID of the library.
     * @param prefix    The prefix to check.
     * @return True if a duplicate exists, false otherwise.
     */
    boolean hasDuplicatePrefix(LibraryId libraryId, String prefix);

    /**
     * Checks for duplicate namespace prefixes, excluding the current namespace.
     *
     * @param namespaceId The ID of the namespace to exclude.
     * @param prefix      The prefix to check.
     * @return True if a duplicate exists, false otherwise.
     */
    boolean hasDuplicatePrefixExcludingCurrent(NamespaceId namespaceId, String prefix);

}
