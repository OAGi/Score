package org.oagi.score.gateway.http.api.context_management.context_category.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.oagi.score.gateway.http.api.context_management.context_category.model.ContextCategoryDetailsRecord;
import org.oagi.score.gateway.http.api.context_management.context_category.model.ContextCategoryId;
import org.oagi.score.gateway.http.api.context_management.context_category.model.ContextCategoryListEntryRecord;
import org.oagi.score.gateway.http.api.context_management.context_category.model.ContextCategorySummaryRecord;
import org.oagi.score.gateway.http.api.context_management.context_category.repository.criteria.ContextCategoryListFilterCriteria;
import org.oagi.score.gateway.http.api.context_management.context_category.service.ContextCategoryQueryService;
import org.oagi.score.gateway.http.api.context_management.context_scheme.model.ContextSchemeSummaryRecord;
import org.oagi.score.gateway.http.api.context_management.context_scheme.service.ContextSchemeQueryService;
import org.oagi.score.gateway.http.common.model.DateRangeCriteria;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.PageResponse;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static java.util.stream.Collectors.toSet;
import static org.oagi.score.gateway.http.common.util.ControllerUtils.pageRequest;
import static org.oagi.score.gateway.http.common.util.Utility.separate;
import static org.springframework.util.StringUtils.hasLength;

@RestController
@Tag(name = "Context Category - Queries", description = "API for retrieving context category-related data")
@RequestMapping("/context-categories")
public class ContextCategoryQueryController {

    @Autowired
    private ContextCategoryQueryService contextCategoryQueryService;

    @Autowired
    private ContextSchemeQueryService contextSchemeQueryService;

    @Autowired
    private SessionService sessionService;

    @Operation(summary = "Retrieve context category summaries", description = "Fetches summaries of context categories.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of context categories",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = ContextCategorySummaryRecord.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid parameters"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping(value = "/summaries")
    public List<ContextCategorySummaryRecord> getContextCategorySummaryList(
            @AuthenticationPrincipal AuthenticatedPrincipal user) {

        return contextCategoryQueryService.getContextCategorySummaryList(sessionService.asScoreUser(user));
    }

    @Operation(summary = "Retrieve context category details", description = "Fetches detailed information about a specific context category.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of context category details",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ContextCategoryDetailsRecord.class))),
            @ApiResponse(responseCode = "400", description = "Invalid parameters"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping(value = "/{ctxCategoryId:[\\d]+}")
    public ContextCategoryDetailsRecord getContextCategoryDetails(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @Parameter(description = "The ID of the context category.")
            @PathVariable("ctxCategoryId") ContextCategoryId contextCategoryId) {

        return contextCategoryQueryService.getContextCategoryDetails(sessionService.asScoreUser(user), contextCategoryId);
    }

    @Operation(summary = "Retrieve context category list", description = "Fetches a paginated list of context categories based on various filter criteria.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of context category list",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid parameters"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping()
    public PageResponse<ContextCategoryListEntryRecord> getContextCategoryList(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @RequestParam(name = "name", required = false)
            @Parameter(description = "The name of the context category to filter results.")
            String name,

            @RequestParam(name = "description", required = false)
            @Parameter(description = "Description filter for context categories.")
            String description,

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

        ContextCategoryListFilterCriteria filterCriteria = new ContextCategoryListFilterCriteria(
                name, description,
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

        var resultAndCount = contextCategoryQueryService.getContextCategoryList(sessionService.asScoreUser(user), filterCriteria, pageRequest);

        PageResponse<ContextCategoryListEntryRecord> response = new PageResponse<>();
        response.setList(resultAndCount.result());
        response.setPage(pageRequest.pageIndex());
        response.setSize(pageRequest.pageSize());
        response.setLength(resultAndCount.count());
        return response;
    }

    @Operation(summary = "Retrieve context scheme summaries", description = "Fetches summaries of context schemes for a given context category ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of context schemes",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = ContextCategorySummaryRecord.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid parameters"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping(value = "/{ctxCategoryId:[\\d]+}/context-schemes/summaries")
    public List<ContextSchemeSummaryRecord> getContextSchemeSummaryListByContextCategoryId(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @Parameter(description = "The ID of the context category.")
            @PathVariable("ctxCategoryId") ContextCategoryId ctxCategoryId) {

        return contextSchemeQueryService.getContextSchemeSummaryList(sessionService.asScoreUser(user), ctxCategoryId);
    }

}
