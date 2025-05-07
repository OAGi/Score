package org.oagi.score.gateway.http.api.context_management.business_context.service;

import org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationService;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.context_management.business_context.model.*;
import org.oagi.score.gateway.http.api.context_management.business_context.repository.BusinessContextQueryRepository;
import org.oagi.score.gateway.http.api.context_management.business_context.repository.criteria.BusinessContextListFilterCriteria;
import org.oagi.score.gateway.http.api.context_management.business_context.repository.criteria.BusinessContextSummaryListFilterCriteria;
import org.oagi.score.gateway.http.api.tenant_management.service.TenantQueryService;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.ResultAndCount;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Service class for querying business contexts.
 * This service provides methods to retrieve summaries, details, and values of business contexts.
 */
@Service
@Transactional(readOnly = true)
public class BusinessContextQueryService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    private BusinessContextQueryRepository query(ScoreUser requester) {
        return repositoryFactory.businessContextQueryRepository(requester);
    }

    @Autowired
    private TenantQueryService tenantService;

    @Autowired
    private ApplicationConfigurationService applicationConfigurationService;

    /**
     * Retrieves the list of business context summaries.
     *
     * @param requester The user requesting the data.
     * @return A list of business context summary records.
     */
    public List<BusinessContextSummaryRecord> getBusinessContextSummaryList(ScoreUser requester) {
        return query(requester).getBusinessContextSummaryList();
    }

    /**
     * Retrieves the list of business context summaries filtered by top-level ASBIEP ID.
     *
     * @param requester        The user requesting the data.
     * @param topLevelAsbiepId The top-level ASBIEP ID to filter the results.
     * @return A list of filtered business context summary records.
     */
    public List<BusinessContextSummaryRecord> getBusinessContextSummaryList(
            ScoreUser requester, TopLevelAsbiepId topLevelAsbiepId) {
        return query(requester).getBusinessContextSummaryList(
                new BusinessContextSummaryListFilterCriteria(
                        null, applicationConfigurationService.isTenantEnabled(requester),
                        topLevelAsbiepId, Collections.emptyList()));
    }

    /**
     * Retrieves the list of business context summaries filtered by top-level ASBIEP ID and business context meaning.
     *
     * @param requester           The user requesting the data.
     * @param topLevelAsbiepId    The top-level ASBIEP ID to filter the results.
     * @param businessContextName The business context meaning to filter the results.
     * @return A list of filtered business context summary records.
     */
    public List<BusinessContextSummaryRecord> getBusinessContextSummaryList(
            ScoreUser requester, TopLevelAsbiepId topLevelAsbiepId, String businessContextName) {
        return query(requester).getBusinessContextSummaryList(
                new BusinessContextSummaryListFilterCriteria(
                        businessContextName, applicationConfigurationService.isTenantEnabled(requester),
                        topLevelAsbiepId, Collections.emptyList()));
    }

    /**
     * Retrieves the list of business context summaries filtered by a list of business context IDs.
     *
     * @param requester             The user requesting the data.
     * @param businessContextIdList The list of business context IDs to filter the results.
     * @return A list of filtered business context summary records.
     */
    public List<BusinessContextSummaryRecord> getBusinessContextSummaryList(
            ScoreUser requester, Collection<BusinessContextId> businessContextIdList) {
        return query(requester).getBusinessContextSummaryList(
                new BusinessContextSummaryListFilterCriteria(
                        null, applicationConfigurationService.isTenantEnabled(requester),
                        null, businessContextIdList));
    }

    /**
     * Retrieves the details of a specific business context.
     *
     * @param requester         The user requesting the data.
     * @param businessContextId The ID of the business context to retrieve details for.
     * @return The details of the specified business context.
     */
    public BusinessContextDetailsRecord getBusinessContextDetails(
            ScoreUser requester, BusinessContextId businessContextId) {
        return query(requester).getBusinessContextDetails(businessContextId);
    }

    /**
     * Retrieves the list of business context values for a specific business context.
     *
     * @param requester         The user requesting the data.
     * @param businessContextId The ID of the business context.
     * @return A list of business context value records.
     */
    public List<BusinessContextValueRecord> getBusinessContextValueList(
            ScoreUser requester, BusinessContextId businessContextId) {
        return query(requester).getBusinessContextValueList(businessContextId);
    }

    /**
     * Retrieves a paginated list of business contexts based on the provided filter criteria.
     * It applies additional tenant-specific filtering if tenant-enabled.
     *
     * @param requester      The user requesting the data.
     * @param filterCriteria The filter criteria to apply to the list.
     * @param pageRequest    The pagination information.
     * @return A paginated list of business context entries along with the total count.
     */
    public ResultAndCount<BusinessContextListEntryRecord> getBusinessContextList(
            ScoreUser requester, BusinessContextListFilterCriteria filterCriteria, PageRequest pageRequest) {

        // If tenant is enabled and the filter requires BIE editing, apply tenant-specific filtering
        if (filterCriteria.tenantEnabled() && filterCriteria.bieEditing()) {
            filterCriteria = filterCriteria.withUserTenantIdList(
                    tenantService.getUserTenantsRoleByUserId(requester, requester.userId())
            );
        }

        // Retrieve the business context list
        var resultAndCount = query(requester).getBusinessContextList(filterCriteria, pageRequest);

        // If tenant is enabled, map the result to include tenant names
        if (filterCriteria.tenantEnabled()) {
            resultAndCount = new ResultAndCount<>(resultAndCount.result().stream().map(c -> {
                List<String> names = tenantService.getTenantNameByBusinessCtxId(requester, c.businessContextId());
                String tenant = names.stream().map(Object::toString).collect(Collectors.joining(","));
                return c.withConnectedTenantNames(tenant);
            }).collect(toList()), resultAndCount.count());
        }

        return resultAndCount;
    }
}
