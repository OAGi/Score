package org.oagi.score.gateway.http.api.module_management.controller;

import org.oagi.score.service.common.data.PageRequest;
import org.oagi.score.service.common.data.PageResponse;
import org.oagi.score.gateway.http.api.module_management.data.*;
import org.oagi.score.gateway.http.api.module_management.service.ModuleSetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;

@RestController
public class ModuleSetController {

    @Autowired
    private ModuleSetService service;

    @RequestMapping(value = "/module_set", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public PageResponse<ModuleSet> getModuleSetList(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                    @RequestParam(name = "name", required = false) String name,
                                                    @RequestParam(name = "description", required = false) String description,
                                                    @RequestParam(name = "updaterLoginIds", required = false) String updaterLoginIds,
                                                    @RequestParam(name = "updateStart", required = false) String updateStart,
                                                    @RequestParam(name = "updateEnd", required = false) String updateEnd,
                                                    @RequestParam(name = "sortActive") String sortActive,
                                                    @RequestParam(name = "sortDirection") String sortDirection,
                                                    @RequestParam(name = "pageIndex") int pageIndex,
                                                    @RequestParam(name = "pageSize") int pageSize) {
        ModuleSetListRequest request = new ModuleSetListRequest();

        request.setName(name);
        request.setDescription(description);
        request.setUpdaterLoginIds(!StringUtils.hasLength(updaterLoginIds) ? Collections.emptyList() :
                Arrays.asList(updaterLoginIds.split(",")).stream().map(e -> e.trim()).filter(e -> StringUtils.hasLength(e)).collect(Collectors.toList()));

        if (StringUtils.hasLength(updateStart)) {
            request.setUpdateStartDate(new Date(Long.valueOf(updateStart)));
        }
        if (StringUtils.hasLength(updateEnd)) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Long.valueOf(updateEnd));
            calendar.add(Calendar.DATE, 1);
            request.setUpdateEndDate(calendar.getTime());
        }

        PageRequest pageRequest = new PageRequest();
        pageRequest.setSortActive(sortActive);
        pageRequest.setSortDirection(sortDirection);
        pageRequest.setPageIndex(pageIndex);
        pageRequest.setPageSize(pageSize);
        request.setPageRequest(pageRequest);
        return service.getModuleSetList(user, request);
    }

    @RequestMapping(value = "/module_set/{id}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ModuleSet getModuleSet(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                  @PathVariable("id") BigInteger moduleSetId) {
        return service.getModuleSet(user, moduleSetId);
    }

    @RequestMapping(value = "/module_set", method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ModuleSet createModuleSet(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                     @RequestBody CreateModuleSetRequest request) {
        return service.createModuleSet(user, request);
    }

    @RequestMapping(value = "/module_set/{id}", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void updateModuleSet(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                @PathVariable("id") BigInteger moduleSetId,
                                @RequestBody UpdateModuleSetRequest request) {
        request.setModuleSetId(moduleSetId);
        service.updateModuleSet(user, request);
    }

    @RequestMapping(value = "/module_set/{id}", method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void discardModuleSet(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                 @PathVariable("id") BigInteger moduleSetId) {
        service.discardModuleSet(user, moduleSetId);
    }

    @RequestMapping(value = "/module_set/{id}/module", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public PageResponse<ModuleSetModule> getModuleSetModuleList(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                                @PathVariable("id") BigInteger moduleSetId,
                                                                @RequestParam(name = "path", required = false) String path,
                                                                @RequestParam(name = "updaterLoginIds", required = false) String updaterLoginIds,
                                                                @RequestParam(name = "updateStart", required = false) String updateStart,
                                                                @RequestParam(name = "updateEnd", required = false) String updateEnd,
                                                                @RequestParam(name = "sortActive") String sortActive,
                                                                @RequestParam(name = "sortDirection") String sortDirection,
                                                                @RequestParam(name = "pageIndex") int pageIndex,
                                                                @RequestParam(name = "pageSize") int pageSize) {
        ModuleSetModuleListRequest request = new ModuleSetModuleListRequest();

        request.setModuleSetId(moduleSetId);
        request.setPath(path);
        request.setUpdaterLoginIds(!StringUtils.hasLength(updaterLoginIds) ? Collections.emptyList() :
                Arrays.asList(updaterLoginIds.split(",")).stream().map(e -> e.trim()).filter(e -> StringUtils.hasLength(e)).collect(Collectors.toList()));

        if (StringUtils.hasLength(updateStart)) {
            request.setUpdateStartDate(new Date(Long.valueOf(updateStart)));
        }
        if (StringUtils.hasLength(updateEnd)) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Long.valueOf(updateEnd));
            calendar.add(Calendar.DATE, 1);
            request.setUpdateEndDate(calendar.getTime());
        }

        PageRequest pageRequest = new PageRequest();
        pageRequest.setSortActive(sortActive);
        pageRequest.setSortDirection(sortDirection);
        pageRequest.setPageIndex(pageIndex);
        pageRequest.setPageSize(pageSize);
        request.setPageRequest(pageRequest);
        return service.getModuleSetModuleList(user, request);
    }

}
