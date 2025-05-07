package org.oagi.score.gateway.http.api.bie_management.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.oagi.score.gateway.http.api.bie_management.model.*;
import org.oagi.score.gateway.http.api.bie_management.model.expression.BieGenerateExpressionResult;
import org.oagi.score.gateway.http.api.bie_management.repository.criteria.BieListInBiePackageFilterCriteria;
import org.oagi.score.gateway.http.api.bie_management.repository.criteria.BiePackageListFilterCriteria;
import org.oagi.score.gateway.http.api.bie_management.service.BiePackageQueryService;
import org.oagi.score.gateway.http.api.bie_management.service.BieQueryService;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.DateRangeCriteria;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.PageResponse;
import org.oagi.score.gateway.http.common.model.base.ScoreDataAccessException;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.FileInputStream;
import java.io.IOException;

import static java.util.stream.Collectors.toSet;
import static org.oagi.score.gateway.http.common.util.ControllerUtils.pageRequest;
import static org.oagi.score.gateway.http.common.util.Utility.separate;

@RestController
@Tag(name = "BIE Package - Queries", description = "API for retrieving BIE package-related data")
@RequestMapping("/bie-packages")
public class BiePackageQueryController {

    @Autowired
    private BiePackageQueryService biePackageQueryService;

    @Autowired
    private SessionService sessionService;
    @Autowired
    private BieQueryService bieQueryService;

    @GetMapping()
    public PageResponse<BiePackageListEntryRecord> getBiePackageList(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestParam(name = "libraryId") LibraryId libraryId,
            @RequestParam(name = "versionId", required = false) String versionId,
            @RequestParam(name = "versionName", required = false) String versionName,
            @RequestParam(name = "description", required = false) String description,
            @RequestParam(name = "den", required = false) String den,
            @RequestParam(name = "businessTerm", required = false) String businessTerm,
            @RequestParam(name = "version", required = false) String version,
            @RequestParam(name = "remark", required = false) String remark,
            @RequestParam(name = "states", required = false) String states,
            @RequestParam(name = "biePackageIds", required = false) String biePackageIds,
            @RequestParam(name = "releaseIds", required = false) String releaseIds,

            @RequestParam(name = "ownerLoginIdList", required = false)
            @Parameter(description = "Comma-separated list of login IDs of owners to filter the results.")
            String ownerLoginIdList,

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

        BiePackageListFilterCriteria filterCriteria = new BiePackageListFilterCriteria(
                libraryId, versionId, versionName, description, den, businessTerm, version, remark,
                separate(states).map(e -> BieState.valueOf(e)).collect(toSet()),
                separate(releaseIds).map(e -> ReleaseId.from(e)).collect(toSet()),
                separate(biePackageIds).map(e -> BiePackageId.from(e)).collect(toSet()),
                separate(ownerLoginIdList).collect(toSet()),
                separate(updaterLoginIdList).collect(toSet()),
                StringUtils.hasLength(lastUpdatedOn) ? DateRangeCriteria.create(lastUpdatedOn) : null
        );

        PageRequest pageRequest =
                (StringUtils.hasLength(orderBy)) ? pageRequest(pageIndex, pageSize, orderBy) :
                        pageRequest(pageIndex, pageSize, sortActive, sortDirection);

        var resultAndCount = biePackageQueryService.getBiePackageList(
                sessionService.asScoreUser(user), filterCriteria, pageRequest);

        PageResponse<BiePackageListEntryRecord> response = new PageResponse<>();
        response.setList(resultAndCount.result());
        response.setPage(pageRequest.pageIndex());
        response.setSize(pageRequest.pageSize());
        response.setLength(resultAndCount.count());
        return response;
    }

    @GetMapping(value = "/{biePackageId:[\\d]+}")
    public BiePackageDetailsRecord getBiePackage(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("biePackageId") BiePackageId biePackageId) throws ScoreDataAccessException {

        return biePackageQueryService.getBiePackageDetails(sessionService.asScoreUser(user), biePackageId);
    }

    @GetMapping(value = "/{biePackageId:[\\d]+}/generate")
    public ResponseEntity<InputStreamResource> generate(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("biePackageId") BiePackageId biePackageId,
            @RequestParam(name = "topLevelAsbiepIdList", required = false) String topLevelAsbiepIdList,
            @RequestParam(name = "schemaExpression", required = false) String schemaExpression,
            HttpServletRequest httpServletRequest) throws IOException {

        BieGenerateExpressionResult response = biePackageQueryService.generate(sessionService.asScoreUser(user), biePackageId,
                separate(topLevelAsbiepIdList).map(e -> TopLevelAsbiepId.from(e)).collect(toSet()),
                schemaExpression);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + response.filename() + "\"")
                .contentType(MediaType.parseMediaType(response.contentType()))
                .contentLength(response.file().length())
                .body(new InputStreamResource(new FileInputStream(response.file())));
    }

    @GetMapping(value = "/{biePackageId:\\d+}/bies")
    public PageResponse<BieListEntryRecord> getBieListInBiePackage(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("biePackageId") BiePackageId biePackageId,
            @RequestParam(name = "den", required = false) String den,
            @RequestParam(name = "businessContext", required = false) String businessContext,
            @RequestParam(name = "version", required = false) String version,
            @RequestParam(name = "remark", required = false) String remark,

            @RequestParam(name = "ownerLoginIdList", required = false)
            @Parameter(description = "Comma-separated list of login IDs of owners to filter the results.")
            String ownerLoginIdList,

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

        BieListInBiePackageFilterCriteria filterCriteria = new BieListInBiePackageFilterCriteria(
                biePackageId,
                den, businessContext, version, remark,
                separate(ownerLoginIdList).collect(toSet()),
                separate(updaterLoginIdList).collect(toSet()),
                StringUtils.hasLength(lastUpdatedOn) ? DateRangeCriteria.create(lastUpdatedOn) : null
        );

        PageRequest pageRequest =
                (StringUtils.hasLength(orderBy)) ? pageRequest(pageIndex, pageSize, orderBy) :
                        pageRequest(pageIndex, pageSize, sortActive, sortDirection);

        var resultAndCount = bieQueryService.getBieList(
                sessionService.asScoreUser(user), filterCriteria, pageRequest);

        PageResponse<BieListEntryRecord> response = new PageResponse<>();
        response.setList(resultAndCount.result());
        response.setPage(pageRequest.pageIndex());
        response.setSize(pageRequest.pageSize());
        response.setLength(resultAndCount.count());
        return response;
    }

}
