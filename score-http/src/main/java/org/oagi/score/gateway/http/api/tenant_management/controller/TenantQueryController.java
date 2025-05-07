package org.oagi.score.gateway.http.api.tenant_management.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationService;
import org.oagi.score.gateway.http.api.tenant_management.controller.payload.TenantListRequest;
import org.oagi.score.gateway.http.api.tenant_management.model.Tenant;
import org.oagi.score.gateway.http.api.tenant_management.model.TenantId;
import org.oagi.score.gateway.http.api.tenant_management.model.TenantInfo;
import org.oagi.score.gateway.http.api.tenant_management.service.TenantQueryService;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.PageResponse;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static org.oagi.score.gateway.http.common.util.ControllerUtils.pageRequest;
import static org.springframework.util.StringUtils.hasLength;

@RestController
@Tag(name = "Tenant - Queries", description = "API for retrieving tenant-related data")
@RequestMapping("/tenants")
public class TenantQueryController {

    @Autowired
    private TenantQueryService tenantService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private ApplicationConfigurationService configService;

    @GetMapping()
    public PageResponse<Tenant> getAllTenantRoles(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @RequestParam(name = "name", required = false) String name,

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
        if (!configService.isTenantEnabled(requester)) {
            throw new AccessDeniedException("Unauthorized Access!");
        }

        TenantListRequest tenantRequest = new TenantListRequest();
        tenantRequest.setName(name);

        PageRequest pageRequest =
                (hasLength(orderBy)) ? pageRequest(pageIndex, pageSize, orderBy) :
                        pageRequest(pageIndex, pageSize, sortActive, sortDirection);
        tenantRequest.setPageRequest(pageRequest);

        return tenantService.getAllTenantRoles(requester, tenantRequest);
    }

    @GetMapping(value = "/{tenantId:[\\d]+}")
    public TenantInfo getTenantInfo(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("tenantId") TenantId tenantId) {

        ScoreUser requester = sessionService.asScoreUser(user);
        if (!configService.isTenantEnabled(requester)) {
            throw new AccessDeniedException("Unauthorized Access!");
        }
        return tenantService.getTenantById(requester, tenantId);
    }

}
