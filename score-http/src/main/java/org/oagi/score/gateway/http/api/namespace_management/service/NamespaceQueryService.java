package org.oagi.score.gateway.http.api.namespace_management.service;

import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceDetailsRecord;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceListEntryRecord;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceSummaryRecord;
import org.oagi.score.gateway.http.api.namespace_management.repository.NamespaceQueryRepository;
import org.oagi.score.gateway.http.api.namespace_management.repository.criteria.NamespaceListFilterCriteria;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.ResultAndCount;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service class for querying namespace-related information.
 */
@Service
@Transactional(readOnly = true)
public class NamespaceQueryService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    private NamespaceQueryRepository query(ScoreUser requester) {
        return repositoryFactory.namespaceQueryRepository(requester);
    };

    /**
     * Retrieves a list of namespace summaries associated with a given library.
     *
     * @param requester The user making the request.
     * @param libraryId The ID of the library for which namespace summaries are retrieved.
     * @return A list of {@link NamespaceSummaryRecord}.
     */
    public List<NamespaceSummaryRecord> getNamespaceSummaryList(ScoreUser requester, LibraryId libraryId) {
        return query(requester).getNamespaceSummaryList(libraryId);
    }

    /**
     * Retrieves a paginated list of namespaceIds based on filter criteria.
     *
     * @param requester     The user making the request.
     * @param filterCriteria The filter criteria to apply.
     * @param pageRequest    The pagination details.
     * @return A {@link ResultAndCount} containing the list of namespaceIds and total count.
     */
    public ResultAndCount<NamespaceListEntryRecord> getNamespaceList(
            ScoreUser requester, NamespaceListFilterCriteria filterCriteria, PageRequest pageRequest) {
        return query(requester).getNamespaceList(filterCriteria, pageRequest);
    }

    /**
     * Retrieves the details of a specific namespace.
     *
     * @param requester   The user making the request.
     * @param namespaceId The ID of the namespace to retrieve.
     * @return A {@link NamespaceDetailsRecord} containing namespace details.
     */
    public NamespaceDetailsRecord getNamespaceDetails(ScoreUser requester, NamespaceId namespaceId) {
        return query(requester).getNamespaceDetails(namespaceId, requester.userId());
    }
}