package org.oagi.score.gateway.http.api.context_management.business_context.repository;

import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.context_management.business_context.model.*;
import org.oagi.score.gateway.http.api.context_management.business_context.repository.criteria.BusinessContextListFilterCriteria;
import org.oagi.score.gateway.http.api.context_management.business_context.repository.criteria.BusinessContextSummaryListFilterCriteria;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.ResultAndCount;

import java.util.List;

/**
 * Repository interface for querying business contexts and their related data.
 * This interface provides methods for retrieving business context summaries, details, values,
 * and filtered lists of business contexts.
 */
public interface BusinessContextQueryRepository {

    /**
     * Retrieves the list of business context summary records.
     *
     * @return A list of business context summary records.
     */
    List<BusinessContextSummaryRecord> getBusinessContextSummaryList();

    List<BusinessContextSummaryRecord> getBusinessContextSummaryList(TopLevelAsbiepId topLevelAsbiepId);

    /**
     * Retrieves the list of business context summary records filtered by the given criteria.
     *
     * @param filterCriteria The filter criteria to apply to the business context summaries.
     * @return A list of filtered business context summary records.
     */
    List<BusinessContextSummaryRecord> getBusinessContextSummaryList(
            BusinessContextSummaryListFilterCriteria filterCriteria);

    /**
     * Retrieves the details of a specific business context by its ID.
     *
     * @param businessContextId The ID of the business context.
     * @return The details of the specified business context.
     */
    BusinessContextDetailsRecord getBusinessContextDetails(
            BusinessContextId businessContextId);

    /**
     * Retrieves the list of business context values for a specific business context.
     *
     * @param businessContextId The ID of the business context.
     * @return A list of business context value records associated with the business context.
     */
    List<BusinessContextValueRecord> getBusinessContextValueList(
            BusinessContextId businessContextId);

    /**
     * Retrieves a paginated list of business contexts based on the given filter criteria.
     *
     * @param filterCriteria The filter criteria to apply to the list of business contexts.
     * @param pageRequest    The pagination request containing page number and size.
     * @return A paginated list of business context entries along with the total count.
     */
    ResultAndCount<BusinessContextListEntryRecord> getBusinessContextList(
            BusinessContextListFilterCriteria filterCriteria, PageRequest pageRequest);

    /**
     * Checks whether a business context is currently assigned to at least one BIE (top-level ASBIEP).
     *
     * @param businessContextId The ID of the business context.
     * @return true if the business context is in use, false otherwise.
     */
    boolean isBusinessContextUsed(BusinessContextId businessContextId);

    /**
     * Checks whether a specific business context is already assigned to a specific top-level ASBIEP.
     *
     * @param businessContextId The business context ID.
     * @param topLevelAsbiepId  The top-level ASBIEP ID.
     * @return true if the assignment already exists, false otherwise.
     */
    boolean isBusinessContextAssigned(BusinessContextId businessContextId, TopLevelAsbiepId topLevelAsbiepId);

    /**
     * Counts how many business contexts are currently assigned to a given top-level ASBIEP.
     *
     * @param topLevelAsbiepId The top-level ASBIEP ID.
     * @return The number of assigned business contexts.
     */
    int countAssignments(TopLevelAsbiepId topLevelAsbiepId);

}
