package org.oagi.score.gateway.http.api.info_management.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.oagi.score.gateway.http.api.info_management.model.*;
import org.oagi.score.gateway.http.api.info_management.service.*;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.gateway.http.api.passkey_management.service.PasskeyQueryService;
import org.oagi.score.gateway.http.configuration.security.webauthn.PasskeyProperties;
import org.oagi.score.gateway.http.configuration.security.webauthn.PasskeyRateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "Information - Queries", description = "API for retrieving information-related data")
@RequestMapping("/info")
public class InfoQueryController {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private ProductInfoQueryService productInfoQueryService;

    @Autowired
    private BieInfoQueryService bieInfoService;

    @Autowired
    private CcInfoQueryService ccInfoService;

    @Autowired
    private OAuth2AppInfoQueryService oauth2AppInfoService;

    @Autowired
    private WebPageInfoQueryService webPageInfoService;

    @Autowired
    private PasskeyProperties passkeyProperties;

    @Autowired
    private PasskeyQueryService passkeyQueryService;

    @Autowired
    private PasskeyRateLimiter passkeyRateLimiter;

    @GetMapping(value = "/products")
    public List<ProductInfoRecord> getProductInfos() {
        List<ProductInfoRecord> productInfos = new ArrayList();
        productInfos.add(productInfoQueryService.gatewayMetadata());
        productInfos.add(productInfoQueryService.databaseMetadata());
        productInfos.add(productInfoQueryService.redisMetadata());
        return productInfos;
    }

    @GetMapping(value = "/webpages")
    public WebPageInfoRecord getWebPageInfo() {
        return webPageInfoService.getWebPageInfo(sessionService.getScoreSystemUser());
    }

    /**
     * Whether passkey sign-in is enabled, for the (unauthenticated) login page. Reveals nothing else: not the RP ID
     * or the allowed origins.
     */
    @GetMapping(value = "/passkey")
    public Map<String, Boolean> getPasskeyInfo() {
        return Map.of("enabled", passkeyProperties.isEnabled());
    }

    /**
     * For the login page's username step: whether this account has a passkey it can use here. Sent in the body, not
     * the URL, so that the login ID stays out of access logs. Limited per address (PasskeyRateLimitFilter) and per
     * login ID, and a login ID that does not exist gets a stable made-up answer, so the answer does not tell
     * whether an account exists.
     */
    @PostMapping(value = "/passkey/lookup")
    public Map<String, Boolean> lookupPasskey(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        if (username != null && !username.isBlank() && !passkeyRateLimiter.tryAcquireForUsername(username)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS);
        }
        return Map.of("passkey", passkeyQueryService.hasUsablePasskey(sessionService.getScoreSystemUser(), username));
    }

    @GetMapping(value = "/oauth2-providers")
    public List<OAuth2AppInfoRecord> getOAuth2AppList() {
        return oauth2AppInfoService.getOAuth2AppList(sessionService.getScoreSystemUser());
    }

    @GetMapping(value = "/cc-summaries")
    public SummaryCcInfoRecord getSummaryCcInfo(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestParam(name = "libraryId") LibraryId libraryId) {
        return ccInfoService.getSummaryCcInfo(sessionService.asScoreUser(user), libraryId);
    }

    @GetMapping(value = "/bie-summaries")
    public SummaryBieInfoRecord getSummaryBieInfo(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestParam(name = "libraryId") LibraryId libraryId,
            @RequestParam(name = "releaseId", required = false) ReleaseId releaseId) {
        return bieInfoService.getSummaryBieInfo(sessionService.asScoreUser(user), libraryId, releaseId);
    }

    @GetMapping(value = "/cc-ext-summaries")
    public SummaryCcExtInfoRecord getSummaryCcExtInfo(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @RequestParam(name = "libraryId") LibraryId libraryId,
            @RequestParam(name = "releaseId", required = false) ReleaseId releaseId) {
        return ccInfoService.getSummaryCcExtInfo(sessionService.asScoreUser(user), libraryId, releaseId);
    }

}
