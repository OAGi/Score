package org.oagi.score.gateway.http.api.account_management.controller;

import org.oagi.score.gateway.http.api.account_management.data.AppOauth2User;
import org.oagi.score.gateway.http.api.account_management.data.PendingListRequest;
import org.oagi.score.gateway.http.api.account_management.service.PendingListService;
import org.oagi.score.service.common.data.PageRequest;
import org.oagi.score.service.common.data.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;


@RestController
public class PendingListController {

    @Autowired
    private PendingListService service;

    @RequestMapping(value = "/pending_list", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public PageResponse<AppOauth2User> getPendingList(
            @AuthenticationPrincipal AuthenticatedPrincipal requester,
            @RequestParam(name = "preferredUsername", required = false) String preferredUsername,
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "providerName", required = false) String providerName,
            @RequestParam(name = "createStart", required = false) String createStart,
            @RequestParam(name = "createEnd", required = false) String createEnd,
            @RequestParam(name = "sortActive") String sortActive,
            @RequestParam(name = "sortDirection") String sortDirection,
            @RequestParam(name = "pageIndex") int pageIndex,
            @RequestParam(name = "pageSize") int pageSize) {

        PendingListRequest request = new PendingListRequest();

        request.setPreferredUsername(preferredUsername);
        request.setEmail(email);
        request.setProviderName(providerName);

        if (StringUtils.hasLength(createStart)) {
            request.setCreateStartDate(new Date(Long.valueOf(createStart)));
        }
        if (StringUtils.hasLength(createEnd)) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Long.valueOf(createEnd));
            calendar.add(Calendar.DATE, 1);
            request.setCreateEndDate(calendar.getTime());
        }

        PageRequest pageRequest = new PageRequest();
        pageRequest.setSortActive(sortActive);
        pageRequest.setSortDirection(sortDirection);
        pageRequest.setPageIndex(pageIndex);
        pageRequest.setPageSize(pageSize);
        request.setPageRequest(pageRequest);

        return service.getPendingList(requester, request);
    }

    @RequestMapping(value = "/pending/{appOauth2UserId}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public AppOauth2User getPending(@PathVariable("appOauth2UserId") long appOauth2UserId) {
        return service.getPending(appOauth2UserId);
    }

    @RequestMapping(value = "/pending/{appOauth2UserId}", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity rejectPending(@PathVariable("appOauth2UserId") long appOauth2UserId,
                                        @RequestBody AppOauth2User request) {
        service.deletePending(request);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/pending/link/{appOauth2UserId}", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity linkToUser(@PathVariable("appOauth2UserId") long appOauth2UserId,
                                     @RequestBody Map<String, Object> body) {
        long appUserId = ((Integer) body.get("appUserId"));
        service.linkPendingToAppUser(appOauth2UserId, appUserId);
        return ResponseEntity.noContent().build();
    }
}