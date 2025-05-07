package org.oagi.score.gateway.http.api.namespace_management.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceDetailsRecord;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceListEntryRecord;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceSummaryRecord;
import org.oagi.score.gateway.http.api.namespace_management.repository.criteria.NamespaceListFilterCriteria;
import org.oagi.score.gateway.http.api.namespace_management.service.NamespaceQueryService;
import org.oagi.score.gateway.http.common.model.DateRangeCriteria;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.PageResponse;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.oagi.score.gateway.http.common.util.ControllerUtils.pageRequest;
import static org.oagi.score.gateway.http.common.util.Utility.separate;
import static org.springframework.util.StringUtils.hasLength;

@RestController
@Tag(name = "Namespace - Queries", description = "API for retrieving namespace-related data")
@RequestMapping("/namespaces")
public class NamespaceQueryController {

    @Autowired
    private NamespaceQueryService namespaceQueryService;

    @Autowired
    private SessionService sessionService;

    @Operation(summary = "Get namespace summaries", description = "Retrieve a list of namespace summaries based on the provided library ID.")
    @GetMapping(value = "/summaries")
    public List<NamespaceSummaryRecord> getNamespaceSummaryList(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestParam(name = "libraryId")
            @Parameter(description = "Unique identifier of the library.")
            LibraryId libraryId) {

        return namespaceQueryService.getNamespaceSummaryList(sessionService.asScoreUser(user), libraryId);
    }

    @Operation(summary = "Get list of namespaceIds", description = "Retrieve a paginated list of namespaceIds based on various filtering criteria.")
    @GetMapping()
    public PageResponse<NamespaceListEntryRecord> getNamespaceList(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @RequestParam(name = "libraryId")
            @Parameter(description = "Unique identifier of the library.")
            LibraryId libraryId,

            @RequestParam(name = "uri", required = false)
            @Parameter(description = "Filter results based on namespace URI.")
            String uri,

            @RequestParam(name = "prefix", required = false)
            @Parameter(description = "Filter results based on namespace prefix.")
            String prefix,

            @RequestParam(name = "description", required = false)
            @Parameter(description = "Filter results based on namespace description.")
            String description,

            @RequestParam(name = "standard", required = false)
            @Parameter(description = "Comma-separated list of flags to filter the standards.")
            String standard,

            @RequestParam(name = "ownerLoginIdList", required = false)
            @Parameter(description = "Comma-separated list of login IDs of owners to filter the results.")
            String ownerLoginIdList,

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

            @RequestParam(name = "pageIndex", required = false, defaultValue = "0")
            @Parameter(description = "Index of the page to retrieve (zero-based). " +
                    "If a negative value is provided, pagination is ignored and all results are returned.")
            Integer pageIndex,

            @RequestParam(name = "pageSize", required = false, defaultValue = "10")
            @Parameter(description = "Number of records per page. " +
                    "If a negative value is provided, pagination is ignored and all results are returned.")
            Integer pageSize) {

        Set<Boolean> standards = separate(standard).map("true"::equalsIgnoreCase).collect(toSet());
        NamespaceListFilterCriteria filterCriteria = new NamespaceListFilterCriteria(
                libraryId,
                uri, prefix, description,
                standards.size() == 1 ? standards.iterator().next() : null,
                separate(ownerLoginIdList).collect(toSet()),
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

        var resultAndCount = namespaceQueryService.getNamespaceList(sessionService.asScoreUser(user), filterCriteria, pageRequest);

        PageResponse<NamespaceListEntryRecord> response = new PageResponse<>();
        response.setList(resultAndCount.result());
        response.setPage(pageRequest.pageIndex());
        response.setSize(pageRequest.pageSize());
        response.setLength(resultAndCount.count());
        return response;
    }

    @Operation(summary = "Get namespace details", description = "Retrieve detailed information about a specific namespace by its ID.")
    @GetMapping(value = "/{namespaceId:[\\d]+}")
    public NamespaceDetailsRecord getNamespaceDetails(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @PathVariable("namespaceId")
            @Parameter(description = "Unique identifier of the namespace.")
            NamespaceId namespaceId) {

        return namespaceQueryService.getNamespaceDetails(sessionService.asScoreUser(user), namespaceId);
    }
}
