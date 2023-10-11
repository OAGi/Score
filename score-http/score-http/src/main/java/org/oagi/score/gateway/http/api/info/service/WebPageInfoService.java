package org.oagi.score.gateway.http.api.info.service;

import org.oagi.score.gateway.http.api.application_management.data.ApplicationConfigurationChangeRequest;
import org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationService;
import org.oagi.score.gateway.http.api.info.data.WebPageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationService.*;

@Service
@Transactional(readOnly = true)
public class WebPageInfoService {

    @Autowired
    private ApplicationConfigurationService configService;

    public WebPageInfo getWebPageInfo() {
        WebPageInfo webPageInfo = new WebPageInfo();
        webPageInfo.setBrand(configService.getProperty(NAVBAR_BRAND_CONFIG_PARAM_NAME));
        webPageInfo.setFavicon(configService.getProperty(FAVICON_LINK_CONFIG_PARAM_NAME));
        webPageInfo.setSignInStatement(configService.getProperty(SIGN_IN_PAGE_STATEMENT_CONFIG_PARAM_NAME));
        return webPageInfo;
    }

    @Transactional(readOnly = false)
    public void updateWebPageInfo(AuthenticatedPrincipal user, WebPageInfo webPageInfo) {
        configService.changeApplicationConfiguration(user,
                new ApplicationConfigurationChangeRequest()
                        .withKeyAndValue(NAVBAR_BRAND_CONFIG_PARAM_NAME, webPageInfo.getBrand()));

        configService.changeApplicationConfiguration(user,
                new ApplicationConfigurationChangeRequest()
                        .withKeyAndValue(FAVICON_LINK_CONFIG_PARAM_NAME, webPageInfo.getFavicon()));

        configService.changeApplicationConfiguration(user,
                new ApplicationConfigurationChangeRequest()
                        .withKeyAndValue(SIGN_IN_PAGE_STATEMENT_CONFIG_PARAM_NAME, webPageInfo.getSignInStatement()));
    }

}
