package org.oagi.score.gateway.http.api.context_management.business_context.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationService;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.context_management.business_context.model.*;
import org.oagi.score.gateway.http.api.context_management.business_context.repository.criteria.BusinessContextListFilterCriteria;
import org.oagi.score.gateway.http.api.context_management.business_context.service.BusinessContextQueryService;
import org.oagi.score.gateway.http.api.tenant_management.model.TenantId;
import org.oagi.score.gateway.http.common.model.DateRangeCriteria;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.PageResponse;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toSet;
import static org.oagi.score.gateway.http.common.util.ControllerUtils.pageRequest;
import static org.oagi.score.gateway.http.common.util.Utility.separate;
import static org.springframework.util.StringUtils.hasLength;

@RestController
@Tag(name = "Business Context - Queries", description = "API for retrieving business context-related data")
@RequestMapping("/business-contexts")
public class BusinessContextQueryController {

    @Autowired
    private BusinessContextQueryService businessContextQueryService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private ApplicationConfigurationService configService;

    @Operation(summary = "Retrieve business context summaries", description = "Fetches summaries of business contexts.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of business contexts",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = BusinessContextSummaryRecord.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid parameters"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping(value = "/summaries")
    public List<BusinessContextSummaryRecord> getBusinessContextSummaryList(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @RequestParam(name = "businessContextIdList", required = false)
            String businessContextIdList,

            @RequestParam(name = "topLevelAsbiepId", required = false)
            TopLevelAsbiepId topLevelAsbiepId) {

        if (hasLength(businessContextIdList)) {
            return businessContextQueryService.getBusinessContextSummaryList(
                    sessionService.asScoreUser(user),
                    separate(businessContextIdList).map(e -> new BusinessContextId(new BigInteger(e))).collect(toSet())
            );
        } else if (topLevelAsbiepId != null) {
            return businessContextQueryService.getBusinessContextSummaryList(
                    sessionService.asScoreUser(user), topLevelAsbiepId);
        } else {
            return businessContextQueryService.getBusinessContextSummaryList(sessionService.asScoreUser(user));
        }
    }

    @Operation(summary = "Retrieve business context details", description = "Fetches detailed information about a specific business context.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of business context details",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = BusinessContextDetailsRecord.class))),
            @ApiResponse(responseCode = "400", description = "Invalid parameters"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping(value = "/{bizCtxId:[\\d]+}")
    public BusinessContextDetailsRecord getBusinessContextDetails(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("bizCtxId") BusinessContextId businessContextId) {

        return businessContextQueryService.getBusinessContextDetails(sessionService.asScoreUser(user), businessContextId);
    }

    @Operation(summary = "Retrieve business context value list", description = "Fetches a list of business context values for a given business context ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of business context values",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = BusinessContextValueRecord.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid parameters"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping(value = "/{bizCtxId:[\\d]+}/values")
    public List<BusinessContextValueRecord> getBusinessContextValueListByBusinessContextId(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("bizCtxId") BusinessContextId businessContextId) {

        return businessContextQueryService.getBusinessContextValueList(sessionService.asScoreUser(user), businessContextId);
    }

    @Operation(summary = "Retrieve business context list", description = "Fetches a paginated list of business contexts based on various filter criteria.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of business context list",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid parameters"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping()
    public PageResponse<BusinessContextListEntryRecord> getBusinessContextList(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @RequestParam(name = "name", required = false)
            @Parameter(description = "The meaning of the business context to filter results.")
            String name,

            @RequestParam(name = "tenantId", required = false)
            TenantId tenantId,

            @RequestParam(name = "notConnectedToTenant", required = false)
            Boolean notConnectedToTenant,

            @RequestParam(name = "isBieEditing", required = false)
            Boolean isBieEditing,

            @RequestParam(name = "updaterLoginIdList", required = false)
            @Parameter(description = "Comma-separated list of login IDs of updaters to filter the results.")
            String updaterLoginIdList,

            @RequestParam(name = "updateStart", required = false)
            @Parameter(deprecated = true,
                    description = "Deprecated. Replaced by 'lastUpdatedOn'. " +
                            "Filter results to include only releases updated after this timestamp (milliseconds since epoch).")
            String updateStart,

            @RequestParam(name = "updateEnd", required = false)
            @Parameter(deprecated = true,
                    description = "Deprecated. Replaced by 'lastUpdatedOn'. " +
                            "Filter results to include only releases updated before this timestamp (milliseconds since epoch).")
            String updateEnd,

            @RequestParam(name = "lastUpdatedOn", required = false)
            @Parameter(description = "Filter results by last update timestamp range in epoch milliseconds. " +
                    "Format: `[after~before]`. Use `after` to specify the lower bound and `before` for the upper bound.")
            String lastUpdatedOn,

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
        BusinessContextListFilterCriteria filterCriteria = new BusinessContextListFilterCriteria(
                name,
                configService.isTenantEnabled(requester), tenantId,
                (notConnectedToTenant != null ? notConnectedToTenant : false),
                (isBieEditing != null ? isBieEditing : false),
                Collections.emptyList(),
                separate(updaterLoginIdList).collect(toSet()),
                (hasLength(lastUpdatedOn)) ?
                        DateRangeCriteria.create(lastUpdatedOn) :
                        (hasLength(updateStart) || hasLength(updateEnd)) ?
                                DateRangeCriteria.create(
                                        hasLength(updateStart) ? Long.valueOf(updateStart) : null,
                                        hasLength(updateEnd) ? Long.valueOf(updateEnd) : null) : null
        );

        PageRequest pageRequest =
                (hasLength(orderBy)) ? pageRequest(pageIndex, pageSize, orderBy) :
                        pageRequest(pageIndex, pageSize, sortActive, sortDirection);

        var resultAndCount = businessContextQueryService.getBusinessContextList(
                requester, filterCriteria, pageRequest);

        PageResponse<BusinessContextListEntryRecord> response = new PageResponse<>();
        response.setList(resultAndCount.result());
        response.setPage(pageRequest.pageIndex());
        response.setSize(pageRequest.pageSize());
        response.setLength(resultAndCount.count());
        return response;
    }

}
