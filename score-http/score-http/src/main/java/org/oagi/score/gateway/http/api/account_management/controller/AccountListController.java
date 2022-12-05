package org.oagi.score.gateway.http.api.account_management.controller;

import org.oagi.score.gateway.http.api.account_management.data.AccountListRequest;
import org.oagi.score.gateway.http.api.account_management.data.AppUser;
import org.oagi.score.gateway.http.api.account_management.service.AccountListService;
import org.oagi.score.service.common.data.PageRequest;
import org.oagi.score.service.common.data.PageResponse;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class AccountListController {

    @Autowired
    private AccountListService service;

    @RequestMapping(value = "/accounts_list", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public PageResponse<AppUser> getAccounts(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestParam(name = "loginId", required = false) String loginId,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "organization", required = false) String organization,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "roles", required = false) String roles,
            @RequestParam(name = "excludeSSO", required = false) Boolean excludeSSO,
            @RequestParam(name = "excludeRequester", required = false) Boolean excludeRequester,
            @RequestParam(name = "tenantId", required = false) Long tenantId,
            @RequestParam(name = "notConnectedToTenant", required = false) Boolean notConnectedToTenant,
            @RequestParam(name = "sortActive") String sortActive,
            @RequestParam(name = "sortDirection") String sortDirection,
            @RequestParam(name = "pageIndex") int pageIndex,
            @RequestParam(name = "pageSize") int pageSize) {

        AccountListRequest request = new AccountListRequest();

        request.setLoginId(loginId);
        request.setName(name);
        request.setOrganization(organization);
        if (StringUtils.hasLength(status)) {
            List<String> statusList = Arrays.asList(status.split(",")).stream().map(e -> e.trim()).filter(e -> StringUtils.hasLength(e)).collect(Collectors.toList());
            if (statusList.size() == 1) {
                request.setEnabled("enable".equalsIgnoreCase(statusList.get(0)));
            }
        }
        if (StringUtils.hasLength(roles)) {
            List<String> roleList = Arrays.asList(roles.split(",")).stream().map(e -> e.trim()).filter(e -> StringUtils.hasLength(e)).collect(Collectors.toList());
            request.setRoles(roleList);
        }
        request.setExcludeSSO(excludeSSO != null ? excludeSSO : false);
        request.setExcludeRequester(excludeRequester);
        request.setTenantId(tenantId);
        request.setNotConnectedToTenant(notConnectedToTenant != null ? notConnectedToTenant : false);
        
        PageRequest pageRequest = new PageRequest();
        pageRequest.setSortActive(sortActive);
        pageRequest.setSortDirection(sortDirection);
        pageRequest.setPageIndex(pageIndex);
        pageRequest.setPageSize(pageSize);
        request.setPageRequest(pageRequest);

        return service.getAccounts(user, request);
    }

    @RequestMapping(value = "/account/{appUserId}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public AppUser getAccount(@PathVariable("appUserId") long appUserId) {
        return service.getAccountById(appUserId);
    }

    @RequestMapping(value = "/account", method = RequestMethod.PUT)
    public ResponseEntity create(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestBody AppUser account) {
        service.insert(user, account);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/accounts/names", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> getAccountLoginIds() {
        return service.getAccountLoginIds();
    }

    @RequestMapping(value = "/account/_check/loginId/hasTaken", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean hasTaken(@RequestBody Map<String, String> body) {
        return service.hasTaken(body.getOrDefault("loginId", ""));
    }

}
