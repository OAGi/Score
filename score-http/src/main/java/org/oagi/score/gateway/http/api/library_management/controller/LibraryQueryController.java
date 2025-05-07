package org.oagi.score.gateway.http.api.library_management.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.oagi.score.gateway.http.api.library_management.model.LibraryDetailsRecord;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.library_management.model.LibraryListEntry;
import org.oagi.score.gateway.http.api.library_management.model.LibrarySummaryRecord;
import org.oagi.score.gateway.http.api.library_management.repository.criteria.LibraryListFilterCriteria;
import org.oagi.score.gateway.http.api.library_management.service.LibraryQueryService;
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
@Tag(name = "Library - Queries", description = "API for retrieving library-related data")
@RequestMapping("/libraries")
public class LibraryQueryController {

    @Autowired
    private LibraryQueryService libraryQueryService;

    @Autowired
    private SessionService sessionService;

    @Operation(
            summary = "Get library summary list",
            description = "Retrieve a list of all libraries with minimal metadata for quick display or selection. " +
                    "This summary typically includes ID, name, state, and basic timestamps."
    )
    @GetMapping("/summaries")
    public List<LibrarySummaryRecord> getLibrarySummaryList(
            @AuthenticationPrincipal AuthenticatedPrincipal user) {

        return libraryQueryService.getLibrarySummaryList(sessionService.asScoreUser(user));
    }

    @Operation(
            summary = "Get library details",
            description = "Retrieve detailed information for a specific library by its unique ID. " +
                    "Includes full metadata, status, ownership, and versioning info."
    )
    @GetMapping(value = "/{libraryId:[\\d]+}")
    public LibraryDetailsRecord getLibraryDetails(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("libraryId")
            @Parameter(description = "Unique identifier of the library to retrieve details for.")
            LibraryId libraryId) {

        return libraryQueryService.getLibraryDetails(sessionService.asScoreUser(user), libraryId);
    }

    @Operation(
            summary = "Get library list with filtering and pagination",
            description = "Retrieve a paginated list of libraries with support for filtering by name, organization, domain, description, state, updater, and update timestamps. " +
                    "Allows sorting by multiple fields and flexible control over pagination. " +
                    "Useful for browsing or managing libraries in the system."
    )
    @GetMapping()
    public PageResponse<LibraryListEntry> getLibraryList(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @RequestParam(name = "type", required = false)
            @Parameter(description = "Filter results by library type.")
            String type,

            @RequestParam(name = "name", required = false)
            @Parameter(description = "Filter results by library name.")
            String name,

            @RequestParam(name = "organization", required = false)
            @Parameter(description = "Filter results by organization name.")
            String organization,

            @RequestParam(name = "description", required = false)
            @Parameter(description = "Filter results by library description.")
            String description,

            @RequestParam(name = "domain", required = false)
            @Parameter(description = "Filter results by domain name.")
            String domain,

            @RequestParam(name = "state", required = false)
            @Parameter(description = "Filter results by library state. Valid values include 'WIP', 'Published', etc.")
            String state,

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
                    "Example: `-name,+lastUpdateTimestamp,state` is equivalent to `name desc, lastUpdateTimestamp asc, state asc`.")
            String orderBy,

            @RequestParam(name = "pageIndex", required = false, defaultValue = "0")
            @Parameter(description = "Index of the page to retrieve (zero-based). " +
                    "If a negative value is provided, pagination is ignored and all results are returned.")
            Integer pageIndex,

            @RequestParam(name = "pageSize", required = false, defaultValue = "10")
            @Parameter(description = "Number of records per page. " +
                    "If a negative value is provided, pagination is ignored and all results are returned.")
            Integer pageSize) {

        LibraryListFilterCriteria filterCriteria = new LibraryListFilterCriteria(
                type, name, organization, description,
                domain, state,
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

        var resultAndCount = libraryQueryService.getLibraryList(sessionService.asScoreUser(user), filterCriteria, pageRequest);

        PageResponse<LibraryListEntry> response = new PageResponse<>();
        response.setList(resultAndCount.result());
        response.setPage(pageRequest.pageIndex());
        response.setSize(pageRequest.pageSize());
        response.setLength(resultAndCount.count());
        return response;
    }

}
