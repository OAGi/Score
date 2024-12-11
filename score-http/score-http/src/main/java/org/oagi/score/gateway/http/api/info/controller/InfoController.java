package org.oagi.score.gateway.http.api.info.controller;

import org.oagi.score.gateway.http.api.info.data.*;
import org.oagi.score.gateway.http.api.info.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@RestController
public class InfoController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ProductInfoService productInfoService;

    @Autowired
    private BieInfoService bieInfoService;

    @Autowired
    private CcInfoService ccInfoService;

    @Autowired
    private OAuth2AppInfoService oauth2AppInfoService;

    @Autowired
    private WebPageInfoService webPageInfoService;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @RequestMapping(value = "/info/products", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ProductInfo> getProductInfos() {
        List<ProductInfo> productInfos = new ArrayList();
        productInfos.add(productInfoService.gatewayMetadata());
        productInfos.add(productInfoService.databaseMetadata());
        productInfos.add(productInfoService.redisMetadata());
        return productInfos;
    }

    @RequestMapping(value = "/info/webpage", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public WebPageInfo getWebPageInfo() {
        return webPageInfoService.getWebPageInfo();
    }

    @RequestMapping(value = "/info/webpage", method = RequestMethod.POST)
    public void updateWebPageInfo(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                  @RequestBody WebPageInfo webPageInfo) {
        webPageInfoService.updateWebPageInfo(user, webPageInfo);
        simpMessagingTemplate.convertAndSend("/topic/webpage/info", webPageInfo);
    }

    @RequestMapping(value = "/info/cc_summary",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public SummaryCcInfo getSummaryCcInfo(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                          @RequestParam(name = "libraryId") BigInteger libraryId) {
        return ccInfoService.getSummaryCcInfo(user, libraryId);
    }

    @RequestMapping(value = "/info/bie_summary",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public SummaryBieInfo getSummaryBieInfo(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                            @RequestParam(name = "libraryId") BigInteger libraryId,
                                            @RequestParam(name = "releaseId", required = false, defaultValue = "-1") BigInteger releaseId) {
        return bieInfoService.getSummaryBieInfo(user, libraryId, releaseId);
    }

    @RequestMapping(value = "/info/cc_ext_summary",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public SummaryCcExtInfo getSummaryCcExtInfo(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                @RequestParam(name = "libraryId") BigInteger libraryId,
                                                @RequestParam(name = "releaseId", required = false, defaultValue = "-1") BigInteger releaseId) {
        return ccInfoService.getSummaryCcExtInfo(user, libraryId, releaseId);
    }

    @RequestMapping(value = "/info/oauth2_providers", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<OAuth2AppInfo> getOAuth2AppList(@AuthenticationPrincipal AuthenticatedPrincipal user) {
        return oauth2AppInfoService.getOAuth2AppList(user);
    }
}
