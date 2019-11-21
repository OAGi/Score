package org.oagi.srt.gateway.http.api.account_management.controller;

import org.oagi.srt.gateway.http.api.account_management.data.AccountListRequest;
import org.oagi.srt.gateway.http.api.account_management.data.AppUser;
import org.oagi.srt.gateway.http.api.account_management.service.AccountListService;
import org.oagi.srt.gateway.http.api.common.data.PageRequest;
import org.oagi.srt.gateway.http.api.common.data.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class AccountListController {

    @Autowired
    private AccountListService service;

    @RequestMapping(value = "/accounts_list", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public PageResponse<AppUser> getAccounts(
            @AuthenticationPrincipal User requester,
            @RequestParam(name = "loginId", required = false) String loginId,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "organization", required = false) String organization,
            @RequestParam(name = "role", required = false) String role,
            @RequestParam(name = "excludeRequester", required = false) Boolean excludeRequester,
            @RequestParam(name = "sortActive") String sortActive,
            @RequestParam(name = "sortDirection") String sortDirection,
            @RequestParam(name = "pageIndex") int pageIndex,
            @RequestParam(name = "pageSize") int pageSize) {

        AccountListRequest request = new AccountListRequest();

        request.setLoginId(loginId);
        request.setName(name);
        request.setOrganization(organization);
        request.setRole(role);
        request.setExcludeRequester(excludeRequester);

        PageRequest pageRequest = new PageRequest();
        pageRequest.setSortActive(sortActive);
        pageRequest.setSortDirection(sortDirection);
        pageRequest.setPageIndex(pageIndex);
        pageRequest.setPageSize(pageSize);
        request.setPageRequest(pageRequest);

        return service.getAccounts(requester, request);
    }

    @RequestMapping(value = "/account/{loginId}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public AppUser getAccount(@PathVariable("loginId") String loginId) {
        return service.getAccount(loginId);
    }

    @RequestMapping(value = "/account", method = RequestMethod.PUT)
    public ResponseEntity create(@RequestBody AppUser account) {
        service.insert(account);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/accounts/names", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<String> getAccountLoginIds() {
        return service.getAccountLoginIds();
    }

    @RequestMapping(value = "/account/_check/loginId/hasTaken", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Boolean hasTaken(@RequestBody Map<String, String> body) {
        return service.hasTaken(body.getOrDefault("loginId", ""));
    }

}
