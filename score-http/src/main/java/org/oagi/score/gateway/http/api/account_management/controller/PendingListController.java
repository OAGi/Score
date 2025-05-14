package org.oagi.score.gateway.http.api.account_management.controller;

import io.swagger.v3.oas.annotations.Parameter;
import org.oagi.score.gateway.http.api.account_management.controller.payload.PendingListRequest;
import org.oagi.score.gateway.http.api.account_management.model.AppOauth2User;
import org.oagi.score.gateway.http.api.account_management.service.PendingListService;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.PageResponse;
import org.oagi.score.gateway.http.configuration.security.SessionService;
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

import static org.oagi.score.gateway.http.common.util.ControllerUtils.pageRequest;
import static org.springframework.util.StringUtils.hasLength;


@RestController
public class PendingListController {

    @Autowired
    private PendingListService service;

    @Autowired
    private SessionService sessionService;

    @GetMapping(value = "/pending_list")
    public PageResponse<AppOauth2User> getPendingList(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestParam(name = "preferredUsername", required = false) String preferredUsername,
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "providerName", required = false) String providerName,
            @RequestParam(name = "createStart", required = false) String createStart,
            @RequestParam(name = "createEnd", required = false) String createEnd,

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

        PageRequest pageRequest =
                (hasLength(orderBy)) ? pageRequest(pageIndex, pageSize, orderBy) :
                        pageRequest(pageIndex, pageSize, sortActive, sortDirection);
        request.setPageRequest(pageRequest);

        return service.getPendingList(sessionService.asScoreUser(user), request);
    }

    @RequestMapping(value = "/pending/{appOauth2UserId:[\\d]+}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public AppOauth2User getPending(@PathVariable("appOauth2UserId") long appOauth2UserId) {
        return service.getPending(appOauth2UserId);
    }

    @RequestMapping(value = "/pending/{appOauth2UserId:[\\d]+}", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity rejectPending(@PathVariable("appOauth2UserId") long appOauth2UserId,
                                        @RequestBody AppOauth2User request) {
        service.deletePending(request);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/pending/link/{appOauth2UserId:[\\d]+}", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity linkToUser(@PathVariable("appOauth2UserId") long appOauth2UserId,
                                     @RequestBody Map<String, Object> body) {
        long appUserId = ((Integer) body.get("appUserId"));
        service.linkPendingToAppUser(appOauth2UserId, appUserId);
        return ResponseEntity.noContent().build();
    }
}