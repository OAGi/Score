package org.oagi.score.gateway.http.api.module_management.controller;

import org.oagi.score.gateway.http.api.module_management.data.AssignCCToModule;
import org.oagi.score.gateway.http.api.module_management.data.ExportModuleSetReleaseResponse;
import org.oagi.score.gateway.http.api.module_management.data.ModuleAssignComponents;
import org.oagi.score.gateway.http.api.module_management.service.ModuleSetReleaseService;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.gateway.http.helper.DeleteOnCloseFileSystemResource;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.oagi.score.repo.api.module.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.oagi.score.repo.api.base.SortDirection.ASC;
import static org.oagi.score.repo.api.base.SortDirection.DESC;

@RestController
public class ModuleSetReleaseController {

    @Autowired
    private ModuleSetReleaseService service;

    @Autowired
    private SessionService sessionService;

    @RequestMapping(value = "/module_set_release_list", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public GetModuleSetReleaseListResponse getModuleSetReleaseList(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                                   @RequestParam(name = "name", required = false) String name,
                                                                   @RequestParam(name = "releaseId", required = false) BigInteger releaseId,
                                                                   @RequestParam(name = "default", required = false) Boolean isDefault,
                                                                   @RequestParam(name = "updaterLoginIds", required = false) String updaterLoginIds,
                                                                   @RequestParam(name = "updateStart", required = false) String updateStart,
                                                                   @RequestParam(name = "updateEnd", required = false) String updateEnd,
                                                                   @RequestParam(name = "sortActive") String sortActive,
                                                                   @RequestParam(name = "sortDirection") String sortDirection,
                                                                   @RequestParam(name = "pageIndex") int pageIndex,
                                                                   @RequestParam(name = "pageSize") int pageSize) {

        GetModuleSetReleaseListRequest request = new GetModuleSetReleaseListRequest(sessionService.asScoreUser(user));

        request.setName(name);
        request.setReleaseId(releaseId);
        request.setDefault(isDefault);
        request.setUpdaterUsernameList(!StringUtils.hasLength(updaterLoginIds) ? Collections.emptyList() :
                Arrays.asList(updaterLoginIds.split(","))
                        .stream().map(e -> e.trim()).filter(e -> StringUtils.hasLength(e))
                        .collect(Collectors.toList()));

        if (StringUtils.hasLength(updateStart)) {
            request.setUpdateStartDate(new Timestamp(Long.valueOf(updateStart)).toLocalDateTime());
        }
        if (StringUtils.hasLength(updateEnd)) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Long.valueOf(updateEnd));
            calendar.add(Calendar.DATE, 1);
            request.setUpdateEndDate(new Timestamp(calendar.getTimeInMillis()).toLocalDateTime());
        }

        request.setPageIndex(pageIndex);
        request.setPageSize(pageSize);
        request.setSortActive(sortActive);
        request.setSortDirection("asc".equalsIgnoreCase(sortDirection) ? ASC : DESC);

        return service.getModuleSetReleaseList(request);
    }

    @RequestMapping(value = "/module_set_release/{id:[\\d]+}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ModuleSetRelease getModuleSetRelease(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                @PathVariable("id") BigInteger moduleSetReleaseId) {
        GetModuleSetReleaseRequest request = new GetModuleSetReleaseRequest(sessionService.asScoreUser(user));
        request.setModuleSetReleaseId(moduleSetReleaseId);
        GetModuleSetReleaseResponse response = service.getModuleSetRelease(request);
        return response.getModuleSetRelease();
    }

    @RequestMapping(value = "/module_set_release", method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ModuleSetRelease createModuleSetRelease(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                   @RequestBody CreateModuleSetReleaseRequest request) {
        request.setRequester(sessionService.asScoreUser(user));
        CreateModuleSetReleaseResponse response = service.createModuleSetRelease(request);
        return response.getModuleSetRelease();
    }

    @RequestMapping(value = "/module_set_release/{id:[\\d]+}", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ModuleSetRelease updateModuleSetRelease(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                     @PathVariable("id") BigInteger moduleSetReleaseId,
                                     @RequestBody ModuleSetRelease moduleSetRelease) {
        UpdateModuleSetReleaseRequest request = new UpdateModuleSetReleaseRequest(sessionService.asScoreUser(user));
        request.setModuleSetReleaseId(moduleSetReleaseId);
        request.setModuleSetId(moduleSetRelease.getModuleSetId());
        request.setReleaseId(moduleSetRelease.getReleaseId());
        request.setModuleSetReleaseName(moduleSetRelease.getModuleSetReleaseName());
        request.setModuleSetReleaseDescription(moduleSetRelease.getModuleSetReleaseDescription());
        request.setDefault(moduleSetRelease.isDefault());
        UpdateModuleSetReleaseResponse response = service.updateModuleSetRelease(request);
        return response.getModuleSetRelease();
    }

    @RequestMapping(value = "/module_set_release/{id:[\\d]+}", method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void discardModuleSetRelease(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                 @PathVariable("id") BigInteger moduleSetReleaseId) {
        DeleteModuleSetReleaseRequest request = new DeleteModuleSetReleaseRequest(sessionService.asScoreUser(user));
        request.setModuleSetReleaseId(moduleSetReleaseId);
        service.discardModuleSetRelease(request);
    }

    @RequestMapping(value = "/module_set_release/{id:[\\d]+}/validate", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ValidateModuleSetReleaseResponse validateModuleSetRelease(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                                     @PathVariable("id") BigInteger moduleSetReleaseId) throws Exception {
        ValidateModuleSetReleaseRequest request = new ValidateModuleSetReleaseRequest(sessionService.asScoreUser(user));
        request.setModuleSetReleaseId(moduleSetReleaseId);
        return service.validateModuleSetRelease(request);
    }

    @RequestMapping(value = "/module_set_release/{id:[\\d]+}/export", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DeleteOnCloseFileSystemResource> exportModuleSetRelease(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("id") BigInteger moduleSetReleaseId) throws Exception {

        ExportModuleSetReleaseResponse response =
                service.exportModuleSetRelease(sessionService.asScoreUser(user), moduleSetReleaseId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + response.getFilename() + "\"")
                .contentType(MediaType.parseMediaType("application/zip"))
                .contentLength(response.getFile().length())
                .body(new DeleteOnCloseFileSystemResource(response.getFile()));
    }

    @RequestMapping(value = "/module_set_release/{id:[\\d]+}/assignable", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ModuleAssignComponents getAssignableCCs(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                   @PathVariable("id") BigInteger moduleSetReleaseId) throws Exception {
        GetAssignableCCListRequest request = new GetAssignableCCListRequest(sessionService.asScoreUser(user));
        request.setModuleSetReleaseId(moduleSetReleaseId);
        return service.getAssignableCCs(request);
    }

    @RequestMapping(value = "/module_set_release/{id:[\\d]+}/assigned", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ModuleAssignComponents getAssignedCCs(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                 @PathVariable("id") BigInteger moduleSetReleaseId,
                                                 @RequestParam(name = "moduleId", required = true) BigInteger moduleId) throws Exception {

        return service.getAssignedCCs(sessionService.asScoreUser(user), moduleSetReleaseId, moduleId);
    }

    @RequestMapping(value = "/module_set_release/{id:[\\d]+}/assign", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void assignCCs(@AuthenticationPrincipal AuthenticatedPrincipal user,
                             @PathVariable("id") BigInteger moduleSetReleaseId,
                             @RequestBody AssignCCToModule assignCCToModule) throws Exception {

        service.setAssignCc(sessionService.asScoreUser(user), assignCCToModule);
    }

    @RequestMapping(value = "/module_set_release/{id:[\\d]+}/unassign", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void unassignCCs(@AuthenticationPrincipal AuthenticatedPrincipal user,
                             @PathVariable("id") BigInteger moduleSetReleaseId,
                             @RequestBody AssignCCToModule assignCCToModule) throws Exception {

        service.unAssignCc(sessionService.asScoreUser(user), assignCCToModule);
    }
}
