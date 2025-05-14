package org.oagi.score.gateway.http.api.context_management.context_scheme.repository;

import org.oagi.score.gateway.http.api.context_management.context_category.model.ContextCategoryId;
import org.oagi.score.gateway.http.api.context_management.context_scheme.model.*;
import org.oagi.score.gateway.http.api.context_management.context_scheme.repository.criteria.ContextSchemeListFilterCriteria;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.ResultAndCount;

import java.util.List;

/**
 * Repository interface for querying context schemes and their related data.
 * This interface includes methods for retrieving context scheme summaries, details, values,
 * and checking for duplicates.
 */
public interface ContextSchemeQueryRepository {

    /**
     * Retrieves the list of context scheme summary records.
     *
     * @return A list of context scheme summary records.
     */
    List<ContextSchemeSummaryRecord> getContextSchemeSummaryList();

    /**
     * Retrieves the list of context scheme summary records filtered by the given context category ID.
     *
     * @param contextCategoryId The ID of the context category to filter by.
     * @return A list of filtered context scheme summary records.
     */
    List<ContextSchemeSummaryRecord> getContextSchemeSummaryList(
            ContextCategoryId contextCategoryId);

    /**
     * Retrieves the details of a specific context scheme by its ID.
     *
     * @param contextSchemeId The ID of the context scheme.
     * @return The details of the specified context scheme.
     */
    ContextSchemeDetailsRecord getContextSchemeDetails(
            ContextSchemeId contextSchemeId);

    /**
     * Retrieves a paginated list of context scheme entries based on the given filter criteria.
     *
     * @param filterCriteria The criteria to filter the context schemes.
     * @param pageRequest    The pagination request containing page number and size.
     * @return A paginated list of context scheme entries along with the total count.
     */
    ResultAndCount<ContextSchemeListEntryRecord> getContextSchemeList(
            ContextSchemeListFilterCriteria filterCriteria, PageRequest pageRequest);

    /**
     * Retrieves the list of context scheme values for a specific context scheme ID.
     *
     * @param contextSchemeId The ID of the context scheme.
     * @return A list of context scheme value details records.
     */
    List<ContextSchemeValueDetailsRecord> getContextSchemeValueList(
            ContextSchemeId contextSchemeId);

    /**
     * Retrieves a list of context scheme value summary records.
     *
     * @return A list of context scheme value summary records.
     */
    List<ContextSchemeValueSummaryRecord> getContextSchemeValueSummaryList();

    ContextSchemeValueSummaryRecord getContextSchemeValueSummary(ContextSchemeValueId contextSchemeValueId);

    /**
     * Checks if a context scheme with the given scheme ID, agency ID, and version ID already exists.
     *
     * @param schemeId        The scheme ID to check.
     * @param schemeAgencyId  The agency ID of the scheme.
     * @param schemeVersionId The version ID of the scheme.
     * @return true if a duplicate context scheme exists, false otherwise.
     */
    boolean hasDuplicate(String schemeId, String schemeAgencyId, String schemeVersionId);

    /**
     * Checks if a context scheme with the given scheme ID, agency ID, and version ID already exists,
     * excluding the current context scheme.
     *
     * @param contextSchemeId The ID of the current context scheme to exclude from the check.
     * @param schemeId        The scheme ID to check.
     * @param schemeAgencyId  The agency ID of the scheme.
     * @param schemeVersionId The version ID of the scheme.
     * @return true if a duplicate context scheme exists (excluding the current one), false otherwise.
     */
    boolean hasDuplicateExcludingCurrent(ContextSchemeId contextSchemeId,
                                         String schemeId, String schemeAgencyId, String schemeVersionId);

    /**
     * Checks if a context scheme with the given name, scheme ID, agency ID, and version ID already exists.
     *
     * @param schemeName      The scheme name to check.
     * @param schemeId        The scheme ID to check.
     * @param schemeAgencyId  The agency ID of the scheme.
     * @param schemeVersionId The version ID of the scheme.
     * @return true if a context scheme with the same name exists, false otherwise.
     */
    boolean hasDuplicateName(String schemeName, String schemeId, String schemeAgencyId, String schemeVersionId);

    /**
     * Checks if a context scheme with the given name, scheme ID, agency ID, and version ID already exists,
     * excluding the current context scheme.
     *
     * @param contextSchemeId The ID of the current context scheme to exclude from the check.
     * @param schemeName      The scheme name to check.
     * @param schemeId        The scheme ID to check.
     * @param schemeAgencyId  The agency ID of the scheme.
     * @param schemeVersionId The version ID of the scheme.
     * @return true if a context scheme with the same name exists (excluding the current one), false otherwise.
     */
    boolean hasDuplicateNameExcludingCurrent(ContextSchemeId contextSchemeId,
                                             String schemeName, String schemeId, String schemeAgencyId, String schemeVersionId);

}
