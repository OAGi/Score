package org.oagi.score.gateway.http.api.info.controller;

import org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationService;
import org.oagi.score.gateway.http.api.info.data.*;
import org.oagi.score.gateway.http.api.info.service.BieInfoService;
import org.oagi.score.gateway.http.api.info.service.CcInfoService;
import org.oagi.score.gateway.http.api.info.service.OAuth2AppInfoService;
import org.oagi.score.gateway.http.api.info.service.ProductInfoService;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationService.SIGN_IN_PAGE_STATEMENT_CONFIG_PARAM_NAME;

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
    private ApplicationConfigurationService configService;

    @RequestMapping(value = "/info/products", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ProductInfo> getProductInfos() {
        List<ProductInfo> productInfos = new ArrayList();
        productInfos.add(productInfoService.gatewayMetadata());
        productInfos.add(productInfoService.databaseMetadata());
        productInfos.add(productInfoService.redisMetadata());
        return productInfos;
    }


    @RequestMapping(value = "/info/pages/signin", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> getSignInPageInfo() {
        Map<String, String> signInPageInfo = new HashMap<>();
        String statement = configService.getProperty(SIGN_IN_PAGE_STATEMENT_CONFIG_PARAM_NAME);
        signInPageInfo.put("paramKey", SIGN_IN_PAGE_STATEMENT_CONFIG_PARAM_NAME);
        if (StringUtils.hasLength(statement)) {
            signInPageInfo.put("statement", statement);
        }
        return signInPageInfo;
    }

    @RequestMapping(value = "/info/cc_summary",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public SummaryCcInfo getSummaryCcInfo(@AuthenticationPrincipal AuthenticatedPrincipal user) {
        return ccInfoService.getSummaryCcInfo(user);
    }

    @RequestMapping(value = "/info/bie_summary",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public SummaryBieInfo getSummaryBieInfo(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                            @RequestParam(name = "releaseId", required = false, defaultValue = "-1") BigInteger releaseId) {
        return bieInfoService.getSummaryBieInfo(user, releaseId);
    }

    @RequestMapping(value = "/info/cc_ext_summary",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public SummaryCcExtInfo getSummaryCcExtInfo(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                @RequestParam(name = "releaseId", required = false, defaultValue = "-1") BigInteger releaseId) {
        return ccInfoService.getSummaryCcExtInfo(user, releaseId);
    }

    @RequestMapping(value = "/info/oauth2_providers", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<OAuth2AppInfo> getOAuth2AppList(@AuthenticationPrincipal AuthenticatedPrincipal user) {
        return oauth2AppInfoService.getOAuth2AppList(user);
    }
}
