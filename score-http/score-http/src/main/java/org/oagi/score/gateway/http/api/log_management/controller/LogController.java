package org.oagi.score.gateway.http.api.log_management.controller;

import org.oagi.score.gateway.http.api.log_management.service.LogService;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.oagi.score.service.common.data.PageRequest;
import org.oagi.score.service.common.data.PageResponse;
import org.oagi.score.service.log.model.Log;
import org.oagi.score.service.log.model.LogListRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;

@RestController
public class LogController {

    @Autowired
    private LogService service;

    @RequestMapping(value = "/logs/{reference}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public PageResponse<Log> getLogs(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                     @PathVariable("reference") String reference,
                                     @RequestParam(name = "sortActive") String sortActive,
                                     @RequestParam(name = "sortDirection") String sortDirection,
                                     @RequestParam(name = "pageIndex") int pageIndex,
                                     @RequestParam(name = "pageSize") int pageSize) {
        if (!StringUtils.hasLength(reference)) {
            throw new IllegalArgumentException("Unknown reference");
        }

        LogListRequest request = new LogListRequest();
        request.setReference(reference);

        PageRequest pageRequest = new PageRequest();
        pageRequest.setSortActive(sortActive);
        pageRequest.setSortDirection(sortDirection);
        pageRequest.setPageIndex(pageIndex);
        pageRequest.setPageSize(pageSize);
        request.setPageRequest(pageRequest);

        return service.getLogByReference(request);
    }

    @RequestMapping(value = "/logs/{logId}/snapshot", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public String getSnapshot(@AuthenticationPrincipal AuthenticatedPrincipal user,
                              @PathVariable("logId") BigInteger logId) {
        return service.getSnapshotById(user, logId);
    }
}
