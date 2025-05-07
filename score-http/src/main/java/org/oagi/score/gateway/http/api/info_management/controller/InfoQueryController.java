package org.oagi.score.gateway.http.api.info_management.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.oagi.score.gateway.http.api.info_management.model.*;
import org.oagi.score.gateway.http.api.info_management.service.*;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

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
