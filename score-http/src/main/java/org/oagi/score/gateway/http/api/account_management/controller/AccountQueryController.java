package org.oagi.score.gateway.http.api.account_management.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.oagi.score.gateway.http.api.account_management.model.AccountDetailsRecord;
import org.oagi.score.gateway.http.api.account_management.model.AccountListEntryRecord;
import org.oagi.score.gateway.http.api.account_management.repository.criteria.AccountListFilterCriteria;
import org.oagi.score.gateway.http.api.account_management.service.AccountQueryService;
import org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationService;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextId;
import org.oagi.score.gateway.http.api.tenant_management.model.TenantId;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.PageResponse;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toSet;
import static org.oagi.score.gateway.http.common.util.ControllerUtils.pageRequest;
import static org.oagi.score.gateway.http.common.util.Utility.separate;
import static org.springframework.util.StringUtils.hasLength;

@RestController
@Tag(name = "Account - Queries", description = "API for retrieving account-related data")
@RequestMapping("/accounts")
public class AccountQueryController {

    @Autowired
    private AccountQueryService accountQueryService;

    @Autowired
    private ApplicationConfigurationService configService;

    @Autowired
    private SessionService sessionService;

    @GetMapping()
    public PageResponse<AccountListEntryRecord> getAccountList(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestParam(name = "loginId", required = false) String loginId,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "organization", required = false) String organization,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "roles", required = false) String roles,
            @RequestParam(name = "excludeSSO", required = false) Boolean excludeSSO,
            @RequestParam(name = "excludeRequester", required = false) Boolean excludeRequester,
            @RequestParam(name = "tenantId", required = false) TenantId tenantId,
            @RequestParam(name = "notConnectedToTenant", required = false) Boolean notConnectedToTenant,
            @RequestParam(name = "businessCtxIds", required = false) String businessCtxIds,

            @RequestParam(name = "sortActive", required = false)
            @Parameter(deprecated = true,
                    description = "Deprecated. Replaced by 'orderBy'. Previously used to specify the active sorting property.")
            String sortActive,

            @RequestParam(name = "sortDirection", required = false)
            @Parameter(deprecated = true,
                    description = "Deprecated. Replaced by 'orderBy'. Previously used to specify sorting direction (ASC/DESC).")
            String sortDirection,

            @RequestParam(name = "orderBy", required = false)
            @Parameter(description = "Sorting criteria for the results. " +
                    "Supports multiple comma-separated properties with an optional '+' (ascending) or '-' (descending) prefix. " +
                    "If no prefix is specified, ascending order is applied by default. " +
                    "Example: `-releaseNum,+lastUpdateTimestamp,state` is equivalent to `releaseNum desc, lastUpdateTimestamp asc, state asc`.")
            String orderBy,

            @RequestParam(name = "pageIndex", required = false)
            @Parameter(description = "Index of the page to retrieve (zero-based). " +
                    "If a negative value is provided, pagination is ignored and all results are returned.")
            Integer pageIndex,

            @RequestParam(name = "pageSize", required = false)
            @Parameter(description = "Number of records per page. " +
                    "If a negative value is provided, pagination is ignored and all results are returned.")
            Integer pageSize) {

        ScoreUser requester = sessionService.asScoreUser(user);
        Collection<String> statusList = separate(status).collect(toSet());
        AccountListFilterCriteria filterCriteria = new AccountListFilterCriteria(
                loginId,
                name,
                organization,
                separate(roles).collect(toSet()),
                statusList.size() == 1 ? "enable".equalsIgnoreCase(statusList.iterator().next()) : null,
                excludeRequester,
                (excludeSSO != null) ? excludeSSO : false,

                configService.isTenantEnabled(requester),
                tenantId, (notConnectedToTenant != null) ? notConnectedToTenant : false,
                separate(businessCtxIds).map(e -> BusinessContextId.from(e)).collect(toSet()));

        PageRequest pageRequest =
                (hasLength(orderBy)) ? pageRequest(pageIndex, pageSize, orderBy) :
                        pageRequest(pageIndex, pageSize, sortActive, sortDirection);

        var resultAndCount = accountQueryService.getAccountList(
                requester, filterCriteria, pageRequest);

        PageResponse<AccountListEntryRecord> response = new PageResponse<>();
        response.setList(resultAndCount.result());
        response.setPage(pageRequest.pageIndex());
        response.setSize(pageRequest.pageSize());
        response.setLength(resultAndCount.count());
        return response;
    }

    @GetMapping(value = "/{appUserIdOrUsername}")
    public AccountDetailsRecord getAccount(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("appUserIdOrUsername") String appUserIdOrUsername) {
        ScoreUser requester = sessionService.asScoreUser(user);
        return accountQueryService.getAccountDetails(requester, appUserIdOrUsername);
    }

    @GetMapping(value = "/names")
    public List<String> getAccountLoginIds(
            @AuthenticationPrincipal AuthenticatedPrincipal user) {
        return accountQueryService.getAccountLoginIds(sessionService.asScoreUser(user));
    }

    @GetMapping(value = "/_check/{loginId}/hasTaken")
    public Boolean hasTaken(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("loginId") String loginId) {

        ScoreUser requester = sessionService.asScoreUser(user);
        return accountQueryService.getAccountDetails(requester, loginId) != null;
    }

}
