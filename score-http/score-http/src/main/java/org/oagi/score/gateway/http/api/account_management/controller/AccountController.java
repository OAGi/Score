package org.oagi.score.gateway.http.api.account_management.controller;

import com.google.common.collect.ImmutableMap;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.account_management.data.AppOauth2User;
import org.oagi.score.gateway.http.api.account_management.data.AppUser;
import org.oagi.score.gateway.http.api.account_management.service.AccountListService;
import org.oagi.score.gateway.http.api.account_management.service.AccountService;
import org.oagi.score.gateway.http.api.account_management.service.PendingListService;
import org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationService;
import org.oagi.score.gateway.http.api.tenant_management.service.TenantService;
import org.oagi.score.gateway.http.configuration.oauth2.ScoreClientRegistrationRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.oagi.score.service.configuration.AppUserAuthority.*;

@RestController
public class AccountController implements InitializingBean {

    @Autowired
    private CsrfTokenRepository csrfTokenRepository;

    @Autowired
    private AccountListService accountListService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private PendingListService pendingListService;

    @Autowired
    private ScoreClientRegistrationRepository clientRegistrationRepository;

    @Autowired
    private ApplicationConfigurationService configService;

    @Autowired
    private TenantService tenantService;

    private OidcClientInitiatedLogoutSuccessHandler oidcClientInitiatedLogoutSuccessHandler;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.oidcClientInitiatedLogoutSuccessHandler =
                new OidcClientInitiatedLogoutSuccessHandler(this.clientRegistrationRepository);
        this.oidcClientInitiatedLogoutSuccessHandler.setDefaultTargetUrl("/logout");
    }

    @RequestMapping(value = "/state", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> my(HttpServletRequest request, Authentication authentication) {
        Map<String, Object> resp = new HashMap();
        AppUser appUser = new AppUser();
        Object principal = authentication.getPrincipal();
        CsrfToken csrfToken = csrfTokenRepository.loadToken(request);
        if (csrfToken != null) {
            resp.put(csrfToken.getHeaderName(), csrfToken.getToken());
        }

        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            appUser = accountListService.getAccountByUsername(userDetails.getUsername());
            resp.put("username", userDetails.getUsername());
            resp.put("authentication", "basic");
            resp.put("roles", userDetails.getAuthorities().stream().map(e -> e.toString()).collect(Collectors.toList()));
            resp.put("enabled", appUser.isEnabled());
        } else if (principal instanceof OAuth2User) {
            OAuth2User oAuth2User = (OAuth2User) principal;
            String sub = oAuth2User.getAttribute("sub");
            AppOauth2User appOauth2User = pendingListService.getPendingBySub(sub);

            if (appOauth2User != null && appOauth2User.getAppUserId() != null) {
                appUser = accountListService.getAccountById(appOauth2User.getAppUserId());
                resp.put("username", appUser.getLoginId());
                resp.put("authentication", "oauth2");
                List<String> roles = new ArrayList();
                if (appUser.isAdmin()) {
                    roles.add(ADMIN_GRANTED_AUTHORITY);
                }
                roles.add(appUser.isDeveloper() ? DEVELOPER_GRANTED_AUTHORITY : END_USER_GRANTED_AUTHORITY);
                resp.put("roles", roles);
                resp.put("enabled", appUser.isEnabled());
            } else {
                resp.put("username", "unknown");
                resp.put("authentication", "unknown");
                if (appOauth2User == null) {
                    resp.put("roles", REJECT_GRANTED_AUTHORITY);
                } else {
                    resp.put("roles", PENDING_GRANTED_AUTHORITY);
                }
                resp.put("enabled", false);
            }
        }

        resp.put("tenant", ImmutableMap.builder()
                .put("enabled", configService.isTenantEnabled())
                .put("roles", getUserTenantsRoleByUserId(appUser.getAppUserId()))
                .build());

        resp.put("businessTerm", ImmutableMap.builder()
                .put("enabled", configService.isBusinessTermEnabled())
                .build());

        resp.put("bie", ImmutableMap.builder()
                .put("inverseMode", configService.isBieInverseModeEnabled())
                .build());

        resp.put("functionsRequiringEmailTransmission", ImmutableMap.builder()
                .put("enabled", configService.isFunctionsRequiringEmailTransmissionEnabled())
                .build());

        return resp;
    }

    private List<ULong> getUserTenantsRoleByUserId(BigInteger userId) {
        List<ULong> tenantRoles = new ArrayList<>();
        if (configService.isTenantEnabled()) {
            tenantRoles = tenantService.getUserTenantsRoleByUserId(userId);
        }
        return tenantRoles;
    }

    @RequestMapping(value = "/accounts/{id:[\\d]+}/enable", method = RequestMethod.POST)
    public ResponseEntity enable(
            @PathVariable("id") long id,
            @AuthenticationPrincipal AuthenticatedPrincipal user) {
        accountService.setEnable(user, id, true);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/accounts/{id:[\\d]+}/disable", method = RequestMethod.POST)
    public ResponseEntity disable(
            @PathVariable("id") long id,
            @AuthenticationPrincipal AuthenticatedPrincipal user) {
        accountService.setEnable(user, id, false);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/accounts/{id:[\\d]+}/transfer_ownership", method = RequestMethod.POST)
    public ResponseEntity transferOwnership(
            @PathVariable("id") BigInteger id,
            @AuthenticationPrincipal AuthenticatedPrincipal user) {
        accountService.transferOwnership(user, id);
        return ResponseEntity.accepted().build();
    }

    @RequestMapping(value = "/oauth2/logout", method = RequestMethod.GET)
    public void oauth2Logout(@AuthenticationPrincipal AuthenticatedPrincipal user,
                             HttpServletRequest request,
                             HttpServletResponse response,
                             Authentication authentication) throws IOException, ServletException {
        this.oidcClientInitiatedLogoutSuccessHandler
                .onLogoutSuccess(request, response, authentication);
    }
}