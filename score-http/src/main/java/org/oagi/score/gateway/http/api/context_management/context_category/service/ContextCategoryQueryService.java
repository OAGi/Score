package org.oagi.score.gateway.http.api.context_management.context_category.service;

import org.oagi.score.gateway.http.api.context_management.context_category.model.ContextCategoryDetailsRecord;
import org.oagi.score.gateway.http.api.context_management.context_category.model.ContextCategoryId;
import org.oagi.score.gateway.http.api.context_management.context_category.model.ContextCategoryListEntryRecord;
import org.oagi.score.gateway.http.api.context_management.context_category.model.ContextCategorySummaryRecord;
import org.oagi.score.gateway.http.api.context_management.context_category.repository.ContextCategoryQueryRepository;
import org.oagi.score.gateway.http.api.context_management.context_category.repository.criteria.ContextCategoryListFilterCriteria;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.ResultAndCount;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service class that provides methods for querying context categories.
 * This service handles retrieving context category summaries, details, and lists.
 */
@Service
@Transactional(readOnly = true)
public class ContextCategoryQueryService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    private ContextCategoryQueryRepository query(ScoreUser requester) {
        return repositoryFactory.contextCategoryQueryRepository(requester);
    }

    /**
     * Retrieves the list of context category summaries.
     *
     * @param requester The user requesting the data.
     * @return A list of context category summary records.
     */
    public List<ContextCategorySummaryRecord> getContextCategorySummaryList(ScoreUser requester) {
        return query(requester).getContextCategorySummaryList();
    }

    /**
     * Retrieves the details of a specific context category.
     *
     * @param requester         The user requesting the data.
     * @param contextCategoryId The ID of the context category.
     * @return The details of the specified context category.
     */
    public ContextCategoryDetailsRecord getContextCategoryDetails(
            ScoreUser requester, ContextCategoryId contextCategoryId) {
        return query(requester).getContextCategoryDetails(contextCategoryId);
    }

    /**
     * Retrieves a paginated list of context categories based on the specified filter criteria.
     *
     * @param requester      The user requesting the data.
     * @param filterCriteria The filter criteria to apply to the list.
     * @param pageRequest    The page request for pagination.
     * @return A paginated list of context category entries along with the total count.
     */
    public ResultAndCount<ContextCategoryListEntryRecord> getContextCategoryList(
            ScoreUser requester, ContextCategoryListFilterCriteria filterCriteria, PageRequest pageRequest) {
        return query(requester).getContextCategoryList(filterCriteria, pageRequest);
    }
}
