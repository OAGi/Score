package org.oagi.score.gateway.http.api.context_management.context_scheme.service;

import org.oagi.score.gateway.http.api.context_management.context_category.model.ContextCategoryId;
import org.oagi.score.gateway.http.api.context_management.context_scheme.model.*;
import org.oagi.score.gateway.http.api.context_management.context_scheme.repository.ContextSchemeQueryRepository;
import org.oagi.score.gateway.http.api.context_management.context_scheme.repository.criteria.ContextSchemeListFilterCriteria;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.ResultAndCount;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service class for querying context schemes.
 */
@Service
@Transactional(readOnly = true)
public class ContextSchemeQueryService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    private ContextSchemeQueryRepository query(ScoreUser requester) {
        return repositoryFactory.contextSchemeQueryRepository(requester);
    }

    /**
     * Retrieves a list of context scheme summaries.
     *
     * @param requester The user making the request.
     * @return A list of {@link ContextSchemeSummaryRecord}.
     */
    public List<ContextSchemeSummaryRecord> getContextSchemeSummaryList(ScoreUser requester) {
        return query(requester).getContextSchemeSummaryList();
    }

    /**
     * Retrieves a list of context scheme summaries filtered by context category ID.
     *
     * @param requester     The user making the request.
     * @param ctxCategoryId The ID of the context category to filter by.
     * @return A list of {@link ContextSchemeSummaryRecord}.
     */
    public List<ContextSchemeSummaryRecord> getContextSchemeSummaryList(
            ScoreUser requester, ContextCategoryId ctxCategoryId) {
        return query(requester).getContextSchemeSummaryList(ctxCategoryId);
    }

    /**
     * Retrieves the details of a specific context scheme.
     *
     * @param requester       The user making the request.
     * @param contextSchemeId The ID of the context scheme to retrieve.
     * @return The details of the context scheme as a {@link ContextSchemeDetailsRecord}.
     */
    public ContextSchemeDetailsRecord getContextSchemeDetails(
            ScoreUser requester, ContextSchemeId contextSchemeId) {
        return query(requester).getContextSchemeDetails(contextSchemeId);
    }

    /**
     * Retrieves a paginated list of context schemes based on filter criteria.
     *
     * @param requester      The user making the request.
     * @param filterCriteria The filtering criteria for retrieving context schemes.
     * @param pageRequest    The pagination request details.
     * @return A {@link ResultAndCount} containing the list of {@link ContextSchemeListEntryRecord} and the total count.
     */
    public ResultAndCount<ContextSchemeListEntryRecord> getContextSchemeList(
            ScoreUser requester, ContextSchemeListFilterCriteria filterCriteria, PageRequest pageRequest) {
        return query(requester).getContextSchemeList(filterCriteria, pageRequest);
    }

    /**
     * Retrieves a list of context scheme values for a given context scheme.
     *
     * @param requester       The user making the request.
     * @param contextSchemeId The ID of the context scheme.
     * @return A list of {@link ContextSchemeValueDetailsRecord}.
     */
    public List<ContextSchemeValueDetailsRecord> getContextSchemeValueList(
            ScoreUser requester, ContextSchemeId contextSchemeId) {
        return query(requester).getContextSchemeValueList(contextSchemeId);
    }

    /**
     * Checks if a context scheme is unique based on its scheme ID, agency ID, and version ID.
     *
     * @param requester       The user making the request.
     * @param schemeId        The ID of the scheme to check.
     * @param schemeAgencyId  The agency ID of the scheme to check.
     * @param schemeVersionId The version ID of the scheme to check.
     * @return true if the context scheme is unique, false otherwise.
     */
    public boolean isContextSchemeUnique(
            ScoreUser requester,
            String schemeId, String schemeAgencyId, String schemeVersionId) {

        return query(requester).hasDuplicate(
                schemeId, schemeAgencyId, schemeVersionId);
    }

    /**
     * Checks if a context scheme is unique, excluding the current context scheme (by its ID).
     *
     * @param requester       The user making the request.
     * @param contextSchemeId The ID of the current context scheme to exclude from the check.
     * @param schemeId        The ID of the scheme to check.
     * @param schemeAgencyId  The agency ID of the scheme to check.
     * @param schemeVersionId The version ID of the scheme to check.
     * @return true if the context scheme is unique, excluding the current context scheme, false otherwise.
     */
    public boolean isContextSchemeUniqueExcludingCurrent(
            ScoreUser requester, ContextSchemeId contextSchemeId,
            String schemeId, String schemeAgencyId, String schemeVersionId) {

        return query(requester).hasDuplicateExcludingCurrent(
                contextSchemeId, schemeId, schemeAgencyId, schemeVersionId);
    }

    /**
     * Checks if the context scheme name is unique based on the scheme's ID, agency ID, and version ID.
     *
     * @param requester       The user making the request.
     * @param schemeName      The name of the scheme to check.
     * @param schemeId        The ID of the scheme to check.
     * @param schemeAgencyId  The agency ID of the scheme to check.
     * @param schemeVersionId The version ID of the scheme to check.
     * @return true if the context scheme name is unique, false otherwise.
     */
    public boolean isContextSchemeNameUnique(
            ScoreUser requester,
            String schemeName, String schemeId, String schemeAgencyId, String schemeVersionId) {

        return query(requester).hasDuplicateName(
                schemeName, schemeId, schemeAgencyId, schemeVersionId);
    }

    /**
     * Checks if the context scheme name is unique, excluding the current context scheme (by its ID).
     *
     * @param requester       The user making the request.
     * @param contextSchemeId The ID of the current context scheme to exclude from the check.
     * @param schemeName      The name of the scheme to check.
     * @param schemeId        The ID of the scheme to check.
     * @param schemeAgencyId  The agency ID of the scheme to check.
     * @param schemeVersionId The version ID of the scheme to check.
     * @return true if the context scheme name is unique, excluding the current context scheme, false otherwise.
     */
    public boolean isContextSchemeNameUniqueExcludingCurrent(
            ScoreUser requester, ContextSchemeId contextSchemeId,
            String schemeName, String schemeId, String schemeAgencyId, String schemeVersionId) {

        return query(requester).hasDuplicateNameExcludingCurrent(
                contextSchemeId, schemeName, schemeId, schemeAgencyId, schemeVersionId);
    }

    /**
     * Retrieves a list of summary records for context scheme values.
     *
     * @param requester The user making the request.
     * @return A list of context scheme value summary records.
     */
    public List<ContextSchemeValueSummaryRecord> getContextSchemeValueSummaryList(
            ScoreUser requester) {

        return query(requester).getContextSchemeValueSummaryList();
    }

}