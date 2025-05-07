package org.oagi.score.gateway.http.api.context_management.context_scheme.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.oagi.score.gateway.http.api.context_management.context_scheme.model.*;
import org.oagi.score.gateway.http.api.context_management.context_scheme.repository.criteria.ContextSchemeListFilterCriteria;
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
@Tag(name = "Context Scheme - Queries", description = "API for retrieving context scheme-related data")
@RequestMapping("/context-schemes")
public class ContextSchemeQueryController {

    @Autowired
    private ContextSchemeQueryService contextSchemeQueryService;

    @Autowired
    private SessionService sessionService;

    @Operation(summary = "Retrieve context scheme summaries", description = "Fetches summaries of context schemes.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of context schemes",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = ContextSchemeSummaryRecord.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid parameters"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping(value = "/summaries")
    public List<ContextSchemeSummaryRecord> getContextSchemeSummaryList(
            @AuthenticationPrincipal AuthenticatedPrincipal user) {

        return contextSchemeQueryService.getContextSchemeSummaryList(sessionService.asScoreUser(user));
    }

    @Operation(summary = "Retrieve context scheme details", description = "Fetches detailed information about a specific context scheme.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of context scheme details",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ContextSchemeDetailsRecord.class))),
            @ApiResponse(responseCode = "400", description = "Invalid parameters"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping(value = "/{ctxSchemeId:[\\d]+}")
    public ContextSchemeDetailsRecord getContextSchemeDetails(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @Parameter(description = "The ID of the context scheme.")
            @PathVariable("ctxSchemeId") ContextSchemeId contextSchemeId) {

        return contextSchemeQueryService.getContextSchemeDetails(sessionService.asScoreUser(user), contextSchemeId);
    }

    @Operation(summary = "Retrieve context scheme list", description = "Fetches a paginated list of context schemes based on various filter criteria.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of context scheme list",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid parameters"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping()
    public PageResponse<ContextSchemeListEntryRecord> getContextSchemeList(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @RequestParam(name = "name", required = false)
            @Parameter(description = "The meaning of the context scheme to filter results.")
            String name,

            @RequestParam(name = "description", required = false)
            @Parameter(description = "Description filter for context schemes.")
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

        ContextSchemeListFilterCriteria filterCriteria = new ContextSchemeListFilterCriteria(
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

        var resultAndCount = contextSchemeQueryService.getContextSchemeList(
                sessionService.asScoreUser(user), filterCriteria, pageRequest);

        PageResponse<ContextSchemeListEntryRecord> response = new PageResponse<>();
        response.setList(resultAndCount.result());
        response.setPage(pageRequest.pageIndex());
        response.setSize(pageRequest.pageSize());
        response.setLength(resultAndCount.count());
        return response;
    }

    @Operation(summary = "Retrieve context scheme value list", description = "Fetches a list of context scheme values for a given context scheme ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of context scheme values",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = ContextSchemeValueDetailsRecord.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid parameters"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping(value = "/{ctxSchemeId:[\\d]+}/values")
    public List<ContextSchemeValueDetailsRecord> getContextSchemeValueList(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @Parameter(description = "The ID of the context scheme.")
            @PathVariable("ctxSchemeId") ContextSchemeId contextSchemeId) {

        return contextSchemeQueryService.getContextSchemeValueList(
                sessionService.asScoreUser(user), contextSchemeId);
    }

    /**
     * Checks if a context scheme is unique.
     *
     * @param user            The authenticated user.
     * @param schemeId        The scheme ID.
     * @param schemeAgencyId  The scheme agency ID.
     * @param schemeVersionId The scheme version ID.
     * @return true if the context scheme is unique, false otherwise.
     */
    @Operation(summary = "Check uniqueness of a context scheme")
    @GetMapping(value = "/check-uniqueness")
    public boolean checkUniqueness(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @Parameter(description = "The scheme ID", required = true)
            @RequestParam("schemeId") String schemeId,

            @Parameter(description = "The scheme agency ID", required = true)
            @RequestParam("schemeAgencyId") String schemeAgencyId,

            @Parameter(description = "The scheme version ID", required = true)
            @RequestParam("schemeVersionId") String schemeVersionId) {

        return contextSchemeQueryService.isContextSchemeUnique(
                sessionService.asScoreUser(user),
                schemeId, schemeAgencyId, schemeVersionId);
    }

    /**
     * Checks if a context scheme is unique, excluding the current context scheme.
     *
     * @param user            The authenticated user.
     * @param contextSchemeId The context scheme ID to exclude.
     * @param schemeId        The scheme ID.
     * @param schemeAgencyId  The scheme agency ID.
     * @param schemeVersionId The scheme version ID.
     * @return true if the context scheme is unique, false otherwise.
     */
    @Operation(summary = "Check uniqueness of a context scheme excluding the current one")
    @GetMapping(value = "/{ctxSchemeId:[\\d]+}/check-uniqueness")
    public boolean checkUniquenessExcludingCurrent(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @Parameter(description = "The context scheme ID to exclude")
            @PathVariable("ctxSchemeId") ContextSchemeId contextSchemeId,

            @Parameter(description = "The scheme ID", required = true)
            @RequestParam("schemeId") String schemeId,

            @Parameter(description = "The scheme agency ID", required = true)
            @RequestParam("schemeAgencyId") String schemeAgencyId,

            @Parameter(description = "The scheme version ID", required = true)
            @RequestParam("schemeVersionId") String schemeVersionId) {

        return contextSchemeQueryService.isContextSchemeUniqueExcludingCurrent(
                sessionService.asScoreUser(user),
                contextSchemeId,
                schemeId, schemeAgencyId, schemeVersionId);
    }

    @GetMapping(value = "/check-name-uniqueness")
    public boolean checkNameUniqueness(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @Parameter(description = "The scheme name", required = true)
            @RequestParam("schemeName") String schemeName,

            @Parameter(description = "The scheme ID", required = true)
            @RequestParam("schemeId") String schemeId,

            @Parameter(description = "The scheme agency ID", required = true)
            @RequestParam("schemeAgencyId") String schemeAgencyId,

            @Parameter(description = "The scheme version ID", required = true)
            @RequestParam("schemeVersionId") String schemeVersionId) {

        return contextSchemeQueryService.isContextSchemeNameUnique(
                sessionService.asScoreUser(user),
                schemeName, schemeId, schemeAgencyId, schemeVersionId);
    }

    @GetMapping(value = "/{ctxSchemeId:[\\d]+}/check-name-uniqueness")
    public boolean checkNameUniquenessExcludingCurrent(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @Parameter(description = "The context scheme ID to exclude")
            @PathVariable("ctxSchemeId") ContextSchemeId contextSchemeId,

            @Parameter(description = "The scheme name", required = true)
            @RequestParam("schemeName") String schemeName,

            @Parameter(description = "The scheme ID", required = true)
            @RequestParam("schemeId") String schemeId,

            @Parameter(description = "The scheme agency ID", required = true)
            @RequestParam("schemeAgencyId") String schemeAgencyId,

            @Parameter(description = "The scheme version ID", required = true)
            @RequestParam("schemeVersionId") String schemeVersionId) {

        return contextSchemeQueryService.isContextSchemeNameUniqueExcludingCurrent(
                sessionService.asScoreUser(user),
                contextSchemeId,
                schemeName, schemeId, schemeAgencyId, schemeVersionId);
    }

    /**
     * Retrieves a summary list of context scheme values.
     *
     * @param user The authenticated user.
     * @return A list of {@link ContextSchemeValueSummaryRecord} objects.
     */
    @Operation(summary = "Retrieve a summary list of context scheme values")
    @GetMapping(value = "/values/summaries")
    public List<ContextSchemeValueSummaryRecord> getContextSchemeValueSummaryList(
            @AuthenticationPrincipal AuthenticatedPrincipal user) {

        return contextSchemeQueryService.getContextSchemeValueSummaryList(sessionService.asScoreUser(user));
    }

}
