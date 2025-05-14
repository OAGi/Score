package org.oagi.score.gateway.http.api.release_management.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.plantuml.service.PlantUmlService;
import org.oagi.score.gateway.http.api.release_management.controller.payload.GenerateMigrationScriptResponse;
import org.oagi.score.gateway.http.api.release_management.model.*;
import org.oagi.score.gateway.http.api.release_management.repository.criteria.ReleaseListFilterCriteria;
import org.oagi.score.gateway.http.api.release_management.service.ReleaseQueryService;
import org.oagi.score.gateway.http.common.model.DateRangeCriteria;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.PageResponse;
import org.oagi.score.gateway.http.common.util.DeleteOnCloseFileSystemResource;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toSet;
import static org.oagi.score.gateway.http.common.util.ControllerUtils.pageRequest;
import static org.oagi.score.gateway.http.common.util.Utility.separate;
import static org.springframework.util.StringUtils.hasLength;

@RestController
@Tag(name = "Release - Queries", description = "API for retrieving release-related data")
@RequestMapping("/releases")
public class ReleaseQueryController {

    @Autowired
    private ReleaseQueryService releaseQueryService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private PlantUmlService plantUmlService;

    @Operation(summary = "Get Release Summaries", description = "Fetches a list of release summaries for a given library.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ReleaseSummaryRecord.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid parameters"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping(value = "/summaries")
    public List<ReleaseSummaryRecord> getReleaseSummaryList(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @RequestParam(name = "libraryId")
            @Parameter(description = "ID of the library")
            LibraryId libraryId,

            @RequestParam(name = "releaseStates", required = false)
            @Parameter(description = "Comma-separated list of release states")
            String releaseStates) {

        return releaseQueryService.getReleaseSummaryList(sessionService.asScoreUser(user),
                libraryId, separate(releaseStates).map(e -> ReleaseState.valueOf(e)).collect(toSet()));
    }

    @Operation(summary = "Get Release List", description = "Retrieves a paginated list of releases.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid parameters"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping()
    public PageResponse<ReleaseListEntryRecord> getReleaseList(
            @AuthenticationPrincipal
            AuthenticatedPrincipal user,

            @RequestParam(name = "libraryId")
            @Parameter(description = "Unique identifier of the library.")
            LibraryId libraryId,

            @RequestParam(name = "releaseNum", required = false)
            @Parameter(description = "Specific release number to filter by. If omitted, all releases are included.")
            String releaseNum,

            @RequestParam(name = "excludeReleaseNums", required = false)
            @Parameter(description = "Comma-separated list of release numbers to exclude from the results.")
            String excludeReleaseNums,

            @RequestParam(name = "releaseStates", required = false)
            @Parameter(description = "Comma-separated list of release states (e.g., DRAFT, PUBLISHED) to filter the results.")
            String releaseStates,

            @RequestParam(name = "namespaceIds", required = false)
            @Parameter(description = "Comma-separated list of namespace IDs to filter the results.")
            String namespaceIds,

            @RequestParam(name = "creatorLoginIdList", required = false)
            @Parameter(description = "Comma-separated list of login IDs of creators to filter the results.")
            String creatorLoginIdList,

            @RequestParam(name = "createStart", required = false)
            @Parameter(deprecated = true,
                    description = "Deprecated. Replaced by 'createdOn'. " +
                    "Filter results to include only releases created after this timestamp (milliseconds since epoch).")
            String createStart,

            @RequestParam(name = "createEnd", required = false)
            @Parameter(deprecated = true,
                    description = "Deprecated. Replaced by 'createdOn'. " +
                    "Filter results to include only releases created before this timestamp (milliseconds since epoch).")
            String createEnd,

            @RequestParam(name = "createdOn", required = false)
            @Parameter(description = "Filter results by creation timestamp range in epoch milliseconds. " +
                    "Format: `[after~before]`. Use `after` to specify the lower bound and `before` for the upper bound.")
            String createdOn,

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

        ReleaseListFilterCriteria filterCriteria = new ReleaseListFilterCriteria(
                libraryId,
                releaseNum,
                separate(excludeReleaseNums).collect(toSet()),
                separate(releaseStates).map(e -> ReleaseState.valueOf(e)).collect(toSet()),
                separate(namespaceIds).map(e -> new NamespaceId(new BigInteger(e))).collect(toSet()),
                separate(creatorLoginIdList).collect(toSet()),
                (hasLength(createdOn)) ?
                        DateRangeCriteria.create(createdOn) :
                        (hasLength(createStart) || hasLength(createEnd)) ?
                                DateRangeCriteria.create(
                                        hasLength(createStart) ? Long.valueOf(createStart) : null,
                                        hasLength(createEnd) ? Long.valueOf(createEnd) : null) : null
                ,
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

        var resultAndCount = releaseQueryService.getReleaseList(sessionService.asScoreUser(user), filterCriteria, pageRequest);

        PageResponse<ReleaseListEntryRecord> response = new PageResponse<>();
        response.setList(resultAndCount.result());
        response.setPage(pageRequest.pageIndex());
        response.setSize(pageRequest.pageSize());
        response.setLength(resultAndCount.count());
        return response;
    }

    @Operation(summary = "Get Release Details", description = "Retrieves details of a specific release.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval",
                    content = @Content(schema = @Schema(implementation = ReleaseDetailsRecord.class))),
            @ApiResponse(responseCode = "400", description = "Invalid parameters"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping(value = "/{releaseId:[\\d]+}")
    public ReleaseDetailsRecord getReleaseDetails(
            @AuthenticationPrincipal
            AuthenticatedPrincipal user,

            @PathVariable("releaseId")
            @Parameter(description = "Unique identifier of the release.")
            ReleaseId releaseId) {

        return releaseQueryService.getReleaseDetails(sessionService.asScoreUser(user), releaseId);
    }

    @GetMapping(value = "/{releaseId:[\\d]+}/assignable")
    public AssignComponents assignComponents(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                             @PathVariable("releaseId") ReleaseId releaseId) {

        return releaseQueryService.getAssignComponents(sessionService.asScoreUser(user), releaseId);
    }

    @GetMapping(value = "/{releaseId:[\\d]+}/generate_migration_script")
    public ResponseEntity<DeleteOnCloseFileSystemResource> generateMigrationScript(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("releaseId") ReleaseId releaseId) throws Exception {

        GenerateMigrationScriptResponse response =
                releaseQueryService.generateMigrationScript(sessionService.asScoreUser(user), releaseId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + response.getFilename() + "\"")
                .contentType(MediaType.parseMediaType("application/zip"))
                .contentLength(response.getFile().length())
                .body(new DeleteOnCloseFileSystemResource(response.getFile()));
    }

    @GetMapping(value = "/{releaseId:[\\d]+}/plantuml")
    public Map<String, String> generatePlantUml(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("releaseId") ReleaseId releaseId,
            @RequestParam(value = "releaseLinkTemplate", defaultValue = "/release/{releaseId}") String releaseLinkTemplate,
            @RequestParam(value = "libraryLinkTemplate", defaultValue = "/library/{libraryId}") String libraryLinkTemplate) throws IOException {

        String text = releaseQueryService.generatePlantUmlText(sessionService.asScoreUser(user),
                releaseId, releaseLinkTemplate, libraryLinkTemplate);

        return Map.of("text", text,
                "encodedText", plantUmlService.getEncodedText(text));
    }

}
