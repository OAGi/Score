package org.oagi.score.gateway.http.api.log_management.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.oagi.score.gateway.http.api.log_management.controller.payload.LogListRequest;
import org.oagi.score.gateway.http.api.log_management.model.Log;
import org.oagi.score.gateway.http.api.log_management.service.LogService;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.PageResponse;
import org.oagi.score.gateway.http.common.util.StringUtils;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;

import static org.oagi.score.gateway.http.common.util.ControllerUtils.pageRequest;
import static org.springframework.util.StringUtils.hasLength;

@RestController
@Tag(name = "Log - Queries", description = "API for retrieving log-related data")
@RequestMapping("/logs")
public class LogController {

    @Autowired
    private LogService service;

    @Autowired
    private SessionService sessionService;

    @Operation(
            summary = "Get logs by reference",
            description = "Retrieves a paginated list of logs associated with a specific reference identifier."
    )
    @GetMapping(value = "/{reference}")
    public PageResponse<Log> getLogs(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @Parameter(description = "Reference identifier used to filter logs.")
            @PathVariable("reference") String reference,

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

        if (!StringUtils.hasLength(reference)) {
            throw new IllegalArgumentException("Unknown reference");
        }

        LogListRequest request = new LogListRequest();
        request.setReference(reference);

        PageRequest pageRequest =
                (hasLength(orderBy)) ? pageRequest(pageIndex, pageSize, orderBy) :
                        pageRequest(pageIndex, pageSize, sortActive, sortDirection);

        request.setPageRequest(pageRequest);

        return service.getLogByReference(request);
    }

    @Operation(
            summary = "Get snapshot by log ID",
            description = "Retrieves the snapshot content associated with a specific log entry by its ID."
    )
    @GetMapping(value = "/{logId:[\\d]+}/snapshot")
    public String getSnapshot(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @Parameter(description = "ID of the log to retrieve the snapshot for.")
            @PathVariable("logId") BigInteger logId) {
        return service.getSnapshotById(sessionService.asScoreUser(user), logId);
    }
}
