package org.oagi.score.gateway.http.api.account_management.controller;

import com.google.common.collect.ImmutableMap;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.oagi.score.gateway.http.api.account_management.model.AccountDetailsRecord;
import org.oagi.score.gateway.http.api.account_management.model.AppOauth2User;
import org.oagi.score.gateway.http.api.account_management.model.OAuth2UserRecord;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.account_management.service.AccountQueryService;
import org.oagi.score.gateway.http.api.account_management.service.PendingListService;
import org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationService;
import org.oagi.score.gateway.http.api.tenant_management.model.TenantId;
import org.oagi.score.gateway.http.api.tenant_management.service.TenantQueryService;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.configuration.oauth2.ScoreClientRegistrationRepository;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.oagi.score.gateway.http.api.account_management.model.AppUserAuthority.*;

@RestController
@Tag(name = "System Account - Queries", description = "API for retrieving system account-related data")
@RequestMapping("/")
public class SystemAccountQueryController implements InitializingBean {

    @Autowired
    private CsrfTokenRepository csrfTokenRepository;

    @Autowired
    private AccountQueryService accountQueryService;

    @Autowired
    private PendingListService pendingListService;

    @Autowired
    private ScoreClientRegistrationRepository clientRegistrationRepository;

    private OidcClientInitiatedLogoutSuccessHandler oidcClientInitiatedLogoutSuccessHandler;

    @Autowired
    private ApplicationConfigurationService configService;

    @Autowired
    private TenantQueryService tenantService;

    @Autowired
    private SessionService sessionService;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.oidcClientInitiatedLogoutSuccessHandler =
                new OidcClientInitiatedLogoutSuccessHandler(this.clientRegistrationRepository);
        this.oidcClientInitiatedLogoutSuccessHandler.setDefaultTargetUrl("/logout");
    }

    @GetMapping(value = "/state")
    public Map<String, Object> my(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            HttpServletRequest request, Authentication authentication) {

        ScoreUser requester = sessionService.asScoreUser(user);

        Map<String, Object> resp = new HashMap();
        AccountDetailsRecord accountDetails = null;
        Object principal = authentication.getPrincipal();
        CsrfToken csrfToken = csrfTokenRepository.loadToken(request);
        if (csrfToken != null) {
            resp.put(csrfToken.getHeaderName(), csrfToken.getToken());
        }

        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            accountDetails = accountQueryService.getAccountDetails(requester, userDetails.getUsername());
            resp.put("username", userDetails.getUsername());
            resp.put("authentication", "basic");
            resp.put("roles", userDetails.getAuthorities().stream().map(e -> e.toString()).collect(Collectors.toList()));
            resp.put("enabled", accountDetails.enabled());
        } else if (principal instanceof OAuth2User) {
            OAuth2User oAuth2User = (OAuth2User) principal;
            String sub = oAuth2User.getAttribute("sub");
            OAuth2UserRecord oAuth2UserRecord = accountQueryService.getOAuth2User(requester, sub);
            AppOauth2User appOauth2User = pendingListService.getPendingBySub(sub);

            if (oAuth2UserRecord != null && oAuth2UserRecord.userId() != null) {
                accountDetails = accountQueryService.getAccountDetails(requester, oAuth2UserRecord.userId());
                resp.put("username", accountDetails.loginId());
                resp.put("authentication", "oauth2");
                List<String> roles = new ArrayList();
                if (accountDetails.admin()) {
                    roles.add(ADMIN_GRANTED_AUTHORITY);
                }
                roles.add(accountDetails.developer() ? DEVELOPER_GRANTED_AUTHORITY : END_USER_GRANTED_AUTHORITY);
                resp.put("roles", roles);
                resp.put("enabled", accountDetails.enabled());
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
                .put("enabled", configService.isTenantEnabled(requester))
                .put("roles", (accountDetails != null) ? getUserTenantsRoleByUserId(requester, accountDetails.userId()) : Collections.emptyList())
                .build());

        resp.put("businessTerm", ImmutableMap.builder()
                .put("enabled", configService.isBusinessTermEnabled(requester))
                .build());

        resp.put("bie", ImmutableMap.builder()
                .put("inverseMode", configService.isBieInverseModeEnabled(requester))
                .build());

        resp.put("functionsRequiringEmailTransmission", ImmutableMap.builder()
                .put("enabled", configService.isFunctionsRequiringEmailTransmissionEnabled(requester))
                .build());

        return resp;
    }

    private List<TenantId> getUserTenantsRoleByUserId(ScoreUser requester, UserId userId) {
        List<TenantId> tenantRoles = new ArrayList<>();
        if (configService.isTenantEnabled(requester)) {
            tenantRoles = tenantService.getUserTenantsRoleByUserId(requester, userId);
        }
        return tenantRoles;
    }

    @GetMapping(value = "/oauth2/logout")
    public void oauth2Logout(@AuthenticationPrincipal AuthenticatedPrincipal user,
                             HttpServletRequest request,
                             HttpServletResponse response,
                             Authentication authentication) throws IOException, ServletException {
        this.oidcClientInitiatedLogoutSuccessHandler
                .onLogoutSuccess(request, response, authentication);
    }

}
