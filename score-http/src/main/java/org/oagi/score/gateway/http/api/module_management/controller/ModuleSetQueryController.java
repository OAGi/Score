package org.oagi.score.gateway.http.api.module_management.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.module_management.model.*;
import org.oagi.score.gateway.http.api.module_management.repository.criteria.ModuleSetListFilterCriteria;
import org.oagi.score.gateway.http.api.module_management.service.ModuleSetQueryService;
import org.oagi.score.gateway.http.common.model.DateRangeCriteria;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.PageResponse;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static java.util.stream.Collectors.toSet;
import static org.oagi.score.gateway.http.common.util.ControllerUtils.pageRequest;
import static org.oagi.score.gateway.http.common.util.Utility.separate;
import static org.springframework.util.StringUtils.hasLength;

@RestController
@Tag(name = "Module Set - Queries", description = "API for retrieving module set-related data")
@RequestMapping("/module-sets")
public class ModuleSetQueryController {

    @Autowired
    private ModuleSetQueryService moduleSetQueryService;

    @Autowired
    private SessionService sessionService;

    @Operation(summary = "Retrieve module set list", description = "Fetches a paginated list of module sets based on various filter criteria.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of module set list",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid parameters"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping()
    public PageResponse<ModuleSetListEntryRecord> getModuleSetList(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @RequestParam(name = "libraryId", required = true)
            @Parameter(description = "The ID of the library to filter module sets.")
            LibraryId libraryId,

            @RequestParam(name = "name", required = false)
            @Parameter(description = "The meaning of the module set to filter results.")
            String name,

            @RequestParam(name = "description", required = false)
            @Parameter(description = "Description filter for module sets.")
            String description,

            @RequestParam(name = "updaterLoginIdList", required = false)
            @Parameter(description = "Comma-separated list of login IDs of updaters to filter the results.")
            String updaterLoginIdList,

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
                    "Supports multiple comma-separated properties with an optional '+' (ascending) or '-' (descending) prefix.")
            String orderBy,

            @RequestParam(name = "pageIndex", required = false, defaultValue = "0")
            @Parameter(description = "Index of the page to retrieve (zero-based). If negative, all results are returned.")
            Integer pageIndex,

            @RequestParam(name = "pageSize", required = false, defaultValue = "10")
            @Parameter(description = "Number of records per page. If negative, all results are returned.")
            Integer pageSize) {

        ModuleSetListFilterCriteria filterCriteria = new ModuleSetListFilterCriteria(
                libraryId, name, description, separate(updaterLoginIdList).collect(toSet()),
                hasLength(lastUpdatedOn) ? DateRangeCriteria.create(lastUpdatedOn) : null
        );

        PageRequest pageRequest =
                (hasLength(orderBy)) ? pageRequest(pageIndex, pageSize, orderBy) :
                        pageRequest(pageIndex, pageSize, sortActive, sortDirection);

        var resultAndCount = moduleSetQueryService.getModuleSetList(
                sessionService.asScoreUser(user), filterCriteria, pageRequest);

        PageResponse<ModuleSetListEntryRecord> response = new PageResponse<>();
        response.setList(resultAndCount.result());
        response.setPage(pageRequest.pageIndex());
        response.setSize(pageRequest.pageSize());
        response.setLength(resultAndCount.count());
        return response;
    }

    @Operation(summary = "Retrieve module set summaries", description = "Fetches summaries of module sets.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of module set summaries",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ModuleSetSummaryRecord.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid parameters"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping(value = "/summaries")
    public List<ModuleSetSummaryRecord> getModuleSetSummaryList(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @Parameter(description = "The ID of the library.")
            @RequestParam(name = "libraryId") LibraryId libraryId) {

        return moduleSetQueryService.getModuleSetSummaryList(sessionService.asScoreUser(user), libraryId);
    }

    @Operation(summary = "Retrieve module set details", description = "Fetches detailed information about a specific module set.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of module set details",
                    content = @Content(schema = @Schema(implementation = ModuleSetDetailsRecord.class))),
            @ApiResponse(responseCode = "400", description = "Invalid parameters"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping(value = "/{moduleSetId:[\\d]+}")
    public ModuleSetDetailsRecord getModuleSetDetails(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @Parameter(description = "The ID of the module set.")
            @PathVariable("moduleSetId") ModuleSetId moduleSetId) {

        return moduleSetQueryService.getModuleSetDetails(sessionService.asScoreUser(user), moduleSetId);
    }

    @Operation(summary = "Retrieve module set metadata", description = "Fetches metadata of a module set.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of module set metadata",
                    content = @Content(schema = @Schema(implementation = ModuleSetMetadataRecord.class))),
            @ApiResponse(responseCode = "400", description = "Invalid parameters"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping(value = "/{moduleSetId:[\\d]+}/metadata")
    public ModuleSetMetadataRecord getModuleSetMetadata(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @Parameter(description = "The ID of the module set.")
            @PathVariable("moduleSetId") ModuleSetId moduleSetId) {

        return moduleSetQueryService.getModuleSetMetadata(sessionService.asScoreUser(user), moduleSetId);
    }

    @GetMapping(value = "/{moduleSetId:[\\d]+}/modules")
    public ModuleElementRecord getModuleElement(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @Parameter(description = "The ID of the module set.")
            @PathVariable("moduleSetId") ModuleSetId moduleSetId) {

        return moduleSetQueryService.getModuleElement(sessionService.asScoreUser(user), moduleSetId);
    }

    @GetMapping(value = "/{moduleSetId:[\\d]+}/modules/{moduleId:[\\d]+}")
    public ModuleSummaryRecord getModule(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @Parameter(description = "The ID of the module set.")
            @PathVariable("moduleSetId") ModuleSetId moduleSetId,

            @Parameter(description = "The ID of the module.")
            @PathVariable("moduleId") ModuleId moduleId) {

        return moduleSetQueryService.getModule(sessionService.asScoreUser(user), moduleSetId, moduleId);
    }
}
