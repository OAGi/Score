package org.oagi.score.gateway.http.api.module_management.controller;

import org.oagi.score.gateway.http.api.module_management.data.ModuleSetRequest;
import org.oagi.score.gateway.http.api.module_management.service.ModuleSetService;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.repo.api.module.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.oagi.score.repo.api.base.SortDirection.ASC;
import static org.oagi.score.repo.api.base.SortDirection.DESC;

@RestController
public class ModuleSetController {

    @Autowired
    private ModuleSetService service;

    @Autowired
    private SessionService sessionService;

    @RequestMapping(value = "/module_set", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public GetModuleSetListResponse getModuleSetList(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                     @RequestParam(name = "name", required = false) String name,
                                                     @RequestParam(name = "description", required = false) String description,
                                                     @RequestParam(name = "updaterLoginIds", required = false) String updaterLoginIds,
                                                     @RequestParam(name = "updateStart", required = false) String updateStart,
                                                     @RequestParam(name = "updateEnd", required = false) String updateEnd,
                                                     @RequestParam(name = "sortActive") String sortActive,
                                                     @RequestParam(name = "sortDirection") String sortDirection,
                                                     @RequestParam(name = "pageIndex") int pageIndex,
                                                     @RequestParam(name = "pageSize") int pageSize) {

        GetModuleSetListRequest request = new GetModuleSetListRequest(sessionService.asScoreUser(user));

        request.setName(name);
        request.setDescription(description);
        request.setUpdaterUsernameList(!StringUtils.hasLength(updaterLoginIds) ? Collections.emptyList() :
                Arrays.asList(updaterLoginIds.split(",")).stream().map(e -> e.trim()).filter(e -> StringUtils.hasLength(e)).collect(Collectors.toList()));

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

        return service.getModuleSetList(request);
    }

    @RequestMapping(value = "/module_set/{id:[\\d]+}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ModuleSet getModuleSet(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                  @PathVariable("id") BigInteger moduleSetId) {
        GetModuleSetRequest request = new GetModuleSetRequest(sessionService.asScoreUser(user));
        request.setModuleSetId(moduleSetId);
        GetModuleSetResponse response = service.getModuleSet(request);
        return response.getModuleSet();
    }

    @RequestMapping(value = "/module_set/{id:[\\d]+}/metadata", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ModuleSetMetadata getModuleSetMetadata(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                  @PathVariable("id") BigInteger moduleSetId) {
        GetModuleSetMetadataRequest request = new GetModuleSetMetadataRequest(sessionService.asScoreUser(user));
        request.setModuleSetId(moduleSetId);
        GetModuleSetMetadataResponse response = service.getModuleSetMetadata(request);
        return response.getModuleSetMetadata();
    }

    @RequestMapping(value = "/module_set", method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ModuleSet createModuleSet(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                     @RequestBody ModuleSetRequest moduleSet) {
        CreateModuleSetRequest request = new CreateModuleSetRequest(sessionService.asScoreUser(user));
        request.setName(moduleSet.getName());
        request.setDescription(moduleSet.getDescription());
        request.setCreateModuleSetRelease(moduleSet.createModuleSetRelease);
        request.setTargetModuleSetReleaseId(moduleSet.targetModuleSetReleaseId);
        request.setTargetReleaseId(moduleSet.targetReleaseId);
        CreateModuleSetResponse response = service.createModuleSet(request);

        return response.getModuleSet();
    }

    @RequestMapping(value = "/module_set/{id:[\\d]+}", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ModuleSet updateModuleSet(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                     @PathVariable("id") BigInteger moduleSetId,
                                     @RequestBody ModuleSet moduleSet) {
        UpdateModuleSetRequest request = new UpdateModuleSetRequest(sessionService.asScoreUser(user));
        request.setName(moduleSet.getName());
        request.setDescription(moduleSet.getDescription());
        request.setModuleSetId(moduleSetId);
        UpdateModuleSetResponse response = service.updateModuleSet(request);
        return response.getModuleSet();
    }

    @RequestMapping(value = "/module_set/{id:[\\d]+}", method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void discardModuleSet(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                 @PathVariable("id") BigInteger moduleSetId) {
        DeleteModuleSetRequest request = new DeleteModuleSetRequest(sessionService.asScoreUser(user));
        request.setModuleSetId(moduleSetId);
        service.discardModuleSet(request);
    }

    @RequestMapping(value = "/module_set/{id:[\\d]+}/modules", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ModuleElement getModuleSetModules(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                             @PathVariable("id") BigInteger moduleSetId) {
        GetModuleElementRequest request = new GetModuleElementRequest(sessionService.asScoreUser(user));
        request.setModuleSetId(moduleSetId);
        return service.getModuleSetModules(request);
    }

    @RequestMapping(value = "/module_set/{id:[\\d]+}/module/create", method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CreateModuleResponse addModuleSetModule(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                   @PathVariable("id") BigInteger moduleSetId,
                                                   @RequestBody ModuleElement moduleElement) {
        CreateModuleRequest request = new CreateModuleRequest(sessionService.asScoreUser(user));
        request.setModuleSetId(moduleSetId);
        request.setName(moduleElement.getName());
        request.setModuleType(moduleElement.isDirectory() ? ModuleType.DIRECTORY : ModuleType.FILE);
        request.setNamespaceId(moduleElement.getNamespaceId());
        request.setVersionNum(moduleElement.getVersionNum());
        request.setParentModuleId(moduleElement.getParentModuleId());
        return service.createModule(request);
    }

    @RequestMapping(value = "/module_set/{id:[\\d]+}/module/{moduleId:[\\d]+}", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public UpdateModuleResponse updateModuleSetModule(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                      @PathVariable("id") BigInteger moduleSetId,
                                                      @PathVariable("moduleId") BigInteger moduleId,
                                                      @RequestBody ModuleElement moduleElement) {
        UpdateModuleRequest request = new UpdateModuleRequest(sessionService.asScoreUser(user));
        request.setModuleId(moduleElement.getModuleId());
        request.setName(moduleElement.getName());
        request.setNamespaceId(moduleElement.getNamespaceId());
        request.setVersionNum(moduleElement.getVersionNum());
        return service.updateModule(request);
    }

    @RequestMapping(value = "/module_set/{id:[\\d]+}/module/{moduleId:[\\d]+}", method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteModuleSetModule(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                      @PathVariable("id") BigInteger moduleSetId,
                                      @PathVariable("moduleId") BigInteger moduleId) {
        DeleteModuleRequest request = new DeleteModuleRequest(sessionService.asScoreUser(user));
        request.setModuleId(moduleId);
        service.deleteModule(request);
    }

    @RequestMapping(value = "/module_set/{id:[\\d]+}/module/{parentModuleId:[\\d]+}/copy", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void copyModule(@AuthenticationPrincipal AuthenticatedPrincipal user,
                           @PathVariable("id") BigInteger moduleSetId,
                           @PathVariable("parentModuleId") BigInteger parentModuleId,
                           @RequestBody CopyModuleRequest request) {
        request.setRequester(sessionService.asScoreUser(user));
        service.copyModule(request);
    }
}
