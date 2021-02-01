package org.oagi.score.gateway.http.api.account_management.controller;

import org.oagi.score.gateway.http.api.account_management.data.AppOauth2User;
import org.oagi.score.gateway.http.api.account_management.data.AppUser;
import org.oagi.score.gateway.http.api.account_management.service.AccountListService;
import org.oagi.score.gateway.http.api.account_management.service.AccountService;
import org.oagi.score.gateway.http.api.account_management.service.PendingListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

import static org.oagi.score.service.configuration.AppUserAuthority.*;

@RestController
public class AccountController {

    @Autowired
    private CsrfTokenRepository csrfTokenRepository;

    @Autowired
    private AccountListService accountListService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private PendingListService pendingListService;

    @RequestMapping(value = "/state", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> my(HttpServletRequest request, Authentication authentication) {
        Map<String, Object> resp = new HashMap();
        Object principal = authentication.getPrincipal();
        CsrfToken csrfToken = csrfTokenRepository.loadToken(request);
        if (csrfToken != null) {
            resp.put(csrfToken.getHeaderName(), csrfToken.getToken());
        }

        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            AppUser appUser = accountListService.getAccountByUsername(userDetails.getUsername());
            resp.put("username", userDetails.getUsername());
            resp.put("authentication", "basic");
            resp.put("role", userDetails.getAuthorities().stream().findFirst().get().toString());
            resp.put("enabled", appUser.isEnabled());
        } else if (principal instanceof OAuth2User) {
            OAuth2User oAuth2User = (OAuth2User) principal;
            String sub = oAuth2User.getAttribute("sub");
            AppOauth2User appOauth2User = pendingListService.getPendingBySub(sub);

            if (appOauth2User != null && appOauth2User.getAppUserId() != null) {
                AppUser appUser = accountListService.getAccountById(appOauth2User.getAppUserId().longValue());
                resp.put("username", appUser.getLoginId());
                resp.put("authentication", "oauth2");
                resp.put("role", appUser.isDeveloper() ? DEVELOPER_GRANTED_AUTHORITY : END_USER_GRANTED_AUTHORITY);
                resp.put("enabled", appUser.isEnabled());
            } else {
                resp.put("username", "unknown");
                resp.put("authentication", "unknown");
                if (appOauth2User == null) {
                    resp.put("role", REJECT_GRANTED_AUTHORITY);
                } else {
                    resp.put("role", PENDING_GRANTED_AUTHORITY);
                }
                resp.put("enabled", false);
            }
        }

        return resp;
    }

    @RequestMapping(value = "/accounts/{id}/enable", method = RequestMethod.POST)
    public ResponseEntity enable(
            @PathVariable("id") long id,
            @AuthenticationPrincipal AuthenticatedPrincipal user) {
        accountService.setEnable(user, id, true);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/accounts/{id}/disable", method = RequestMethod.POST)
    public ResponseEntity disable(
            @PathVariable("id") long id,
            @AuthenticationPrincipal AuthenticatedPrincipal user) {
        accountService.setEnable(user, id, false);
        return ResponseEntity.noContent().build();
    }


}