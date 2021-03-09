package org.oagi.score.gateway.http.api.module_management.controller;

import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.repo.api.module.model.*;
import org.oagi.score.gateway.http.api.module_management.service.ModuleSetService;
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

    @RequestMapping(value = "/module_set/{id}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ModuleSet getModuleSet(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                             @PathVariable("id") BigInteger moduleSetId) {
        GetModuleSetRequest request = new GetModuleSetRequest(sessionService.asScoreUser(user));
        request.setModuleSetId(moduleSetId);
        GetModuleSetResponse response = service.getModuleSet(request);
        return response.getModuleSet();
    }

    @RequestMapping(value = "/module_set", method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ModuleSet createModuleSet(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                   @RequestBody ModuleSet moduleSet) {
        CreateModuleSetRequest request = new CreateModuleSetRequest(sessionService.asScoreUser(user));
        request.setName(moduleSet.getName());
        request.setDescription(moduleSet.getDescription());
        CreateModuleSetResponse response = service.createModuleSet(request);
        return response.getModuleSet();
    }

    @RequestMapping(value = "/module_set/{id}", method = RequestMethod.POST,
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

    @RequestMapping(value = "/module_set/{id}", method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void discardModuleSet(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                 @PathVariable("id") BigInteger moduleSetId) {
        DeleteModuleSetRequest request = new DeleteModuleSetRequest(sessionService.asScoreUser(user));
        request.setModuleSetId(moduleSetId);
        service.discardModuleSet(request);
    }

    @RequestMapping(value = "/module_set/{id}/module", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public GetModuleListResponse getModuleSetModuleList(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                                @PathVariable("id") BigInteger moduleSetId,
                                                                @RequestParam(name = "path", required = false) String path,
                                                                @RequestParam(name = "updaterLoginIds", required = false) String updaterLoginIds,
                                                                @RequestParam(name = "updateStart", required = false) String updateStart,
                                                                @RequestParam(name = "updateEnd", required = false) String updateEnd,
                                                                @RequestParam(name = "sortActive") String sortActive,
                                                                @RequestParam(name = "sortDirection") String sortDirection,
                                                                @RequestParam(name = "pageIndex") int pageIndex,
                                                                @RequestParam(name = "pageSize") int pageSize) {

        GetModuleListRequest request = new GetModuleListRequest(sessionService.asScoreUser(user));

        request.setModuleSetId(moduleSetId);

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
        return service.getModuleSetModuleList(request);
    }

}
