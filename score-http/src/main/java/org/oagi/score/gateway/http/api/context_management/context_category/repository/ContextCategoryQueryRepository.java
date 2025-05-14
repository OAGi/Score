package org.oagi.score.gateway.http.api.context_management.context_category.repository;

import org.oagi.score.gateway.http.api.context_management.context_category.model.ContextCategoryDetailsRecord;
import org.oagi.score.gateway.http.api.context_management.context_category.model.ContextCategoryId;
import org.oagi.score.gateway.http.api.context_management.context_category.model.ContextCategoryListEntryRecord;
import org.oagi.score.gateway.http.api.context_management.context_category.model.ContextCategorySummaryRecord;
import org.oagi.score.gateway.http.api.context_management.context_category.repository.criteria.ContextCategoryListFilterCriteria;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.ResultAndCount;

import java.util.List;

/**
 * Repository interface for querying context categories and their related data.
 * This interface includes methods for retrieving context category summaries, details,
 * and filtered lists of context categories.
 */
public interface ContextCategoryQueryRepository {

    /**
     * Retrieves the list of context category summary records.
     *
     * @return A list of context category summary records.
     */
    List<ContextCategorySummaryRecord> getContextCategorySummaryList();

    /**
     * Retrieves the details of a specific context category by its ID.
     *
     * @param contextCategoryId The ID of the context category.
     * @return The details of the specified context category.
     */
    ContextCategoryDetailsRecord getContextCategoryDetails(ContextCategoryId contextCategoryId);

    /**
     * Retrieves a paginated list of context category entries based on the given filter criteria.
     *
     * @param filterCriteria The criteria to filter the context categories.
     * @param pageRequest    The pagination request containing page number and size.
     * @return A paginated list of context category entries along with the total count.
     */
    ResultAndCount<ContextCategoryListEntryRecord> getContextCategoryList(
            ContextCategoryListFilterCriteria filterCriteria, PageRequest pageRequest);

}
