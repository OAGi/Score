package org.oagi.score.gateway.http.api.module_management.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.module_management.controller.payload.ExportModuleSetReleaseResponse;
import org.oagi.score.gateway.http.api.module_management.controller.payload.ModuleAssignableComponentsRecord;
import org.oagi.score.gateway.http.api.module_management.controller.payload.ModuleAssignedComponentsRecord;
import org.oagi.score.gateway.http.api.module_management.controller.payload.ValidateModuleSetReleaseResponse;
import org.oagi.score.gateway.http.api.module_management.model.*;
import org.oagi.score.gateway.http.api.module_management.repository.criteria.ModuleSetReleaseListFilterCriteria;
import org.oagi.score.gateway.http.api.module_management.service.ModuleSetReleaseQueryService;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
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

import java.util.List;

import static java.util.stream.Collectors.toSet;
import static org.oagi.score.gateway.http.common.util.ControllerUtils.pageRequest;
import static org.oagi.score.gateway.http.common.util.Utility.separate;
import static org.springframework.util.StringUtils.hasLength;

@RestController
@Tag(name = "Module Set Release - Queries", description = "API for retrieving module set release-related data")
@RequestMapping("/module-set-releases")
public class ModuleSetReleaseQueryController {

    @Autowired
    private ModuleSetReleaseQueryService moduleSetReleaseQueryService;

    @Autowired
    private SessionService sessionService;

    @GetMapping(value = "/summaries")
    public List<ModuleSetReleaseSummaryRecord> getModuleSetReleaseSummaryList(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @RequestParam("libraryId") LibraryId libraryId) {

        return moduleSetReleaseQueryService.getModuleSetReleaseSummaryList(
                sessionService.asScoreUser(user), libraryId);
    }

    @GetMapping(value = "/{moduleSetReleaseId:[\\d]+}")
    public ModuleSetReleaseDetailsRecord getModuleSetReleaseDetails(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("moduleSetReleaseId") ModuleSetReleaseId moduleSetReleaseId) {

        return moduleSetReleaseQueryService.getModuleSetReleaseDetails(
                sessionService.asScoreUser(user), moduleSetReleaseId);
    }

    @GetMapping()
    public PageResponse<ModuleSetReleaseListEntryRecord> getModuleSetReleaseList(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @RequestParam(name = "libraryId", required = true)
            @Parameter(description = "The ID of the library to filter module set releases.")
            LibraryId libraryId,

            @RequestParam(name = "releaseId", required = false)
            @Parameter(description = "The ID of the release to filter module set releases.")
            ReleaseId releaseId,

            @RequestParam(name = "name", required = false)
            @Parameter(description = "The name of the module set to filter results.")
            String name,

            @RequestParam(name = "default", required = false)
            Boolean isDefault,

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

        ModuleSetReleaseListFilterCriteria filterCriteria = new ModuleSetReleaseListFilterCriteria(
                libraryId, releaseId, name, isDefault, separate(updaterLoginIdList).collect(toSet()),
                hasLength(lastUpdatedOn) ? DateRangeCriteria.create(lastUpdatedOn) : null
        );

        PageRequest pageRequest =
                (hasLength(orderBy)) ? pageRequest(pageIndex, pageSize, orderBy) :
                        pageRequest(pageIndex, pageSize, sortActive, sortDirection);

        var resultAndCount = moduleSetReleaseQueryService.getModuleSetReleaseList(
                sessionService.asScoreUser(user), filterCriteria, pageRequest);

        PageResponse<ModuleSetReleaseListEntryRecord> response = new PageResponse<>();
        response.setList(resultAndCount.result());
        response.setPage(pageRequest.pageIndex());
        response.setSize(pageRequest.pageSize());
        response.setLength(resultAndCount.count());
        return response;
    }

    @GetMapping(value = "/{moduleSetReleaseId:[\\d]+}/validate")
    public ValidateModuleSetReleaseResponse validateModuleSetRelease(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @PathVariable("moduleSetReleaseId")
            ModuleSetReleaseId moduleSetReleaseId) throws Exception {

        return moduleSetReleaseQueryService.validateModuleSetRelease(
                sessionService.asScoreUser(user), moduleSetReleaseId);
    }

    @GetMapping(value = "/{moduleSetReleaseId:[\\d]+}/validate/{requestId}")
    public ValidateModuleSetReleaseResponse progressValidationModuleSetRelease(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("moduleSetReleaseId") ModuleSetReleaseId moduleSetReleaseId,
            @PathVariable("requestId") String requestId) {

        return moduleSetReleaseQueryService.progressValidationModuleSetRelease(
                sessionService.asScoreUser(user), moduleSetReleaseId, requestId);
    }

    @GetMapping(value = "/{moduleSetReleaseId:[\\d]+}/export")
    public ResponseEntity<DeleteOnCloseFileSystemResource> exportModuleSetRelease(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("moduleSetReleaseId") ModuleSetReleaseId moduleSetReleaseId) throws Exception {

        ExportModuleSetReleaseResponse response =
                moduleSetReleaseQueryService.exportModuleSetRelease(sessionService.asScoreUser(user), moduleSetReleaseId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + response.filename() + "\"")
                .contentType(MediaType.parseMediaType("application/zip"))
                .contentLength(response.file().length())
                .body(new DeleteOnCloseFileSystemResource(response.file()));
    }

    @GetMapping(value = "/{moduleSetReleaseId:[\\d]+}/assignable")
    public ModuleAssignableComponentsRecord getAssignableCCs(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("moduleSetReleaseId") ModuleSetReleaseId moduleSetReleaseId) throws Exception {

        return moduleSetReleaseQueryService.getAssignableCCs(
                sessionService.asScoreUser(user), moduleSetReleaseId);
    }

    @GetMapping(value = "/{moduleSetReleaseId:[\\d]+}/assigned")
    public ModuleAssignedComponentsRecord getAssignedCCs(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("moduleSetReleaseId") ModuleSetReleaseId moduleSetReleaseId,
            @RequestParam(name = "moduleId") ModuleId moduleId) throws Exception {

        return moduleSetReleaseQueryService.getAssignedCCs(
                sessionService.asScoreUser(user), moduleSetReleaseId, moduleId);
    }

}
