package org.oagi.score.gateway.http.api.info_management.service;

import org.oagi.score.gateway.http.api.application_management.controller.payload.ApplicationConfigurationChangeRequest;
import org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationService;
import org.oagi.score.gateway.http.api.info_management.model.BoxColorSet;
import org.oagi.score.gateway.http.api.info_management.model.WebPageInfoRecord;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationService.*;

@Service
@Transactional
public class WebPageInfoCommandService {

    @Autowired
    private ApplicationConfigurationService configService;

    public void updateWebPageInfo(ScoreUser requester, WebPageInfoRecord webPageInfo) {
        ApplicationConfigurationChangeRequest request = new ApplicationConfigurationChangeRequest()
                .withKeyAndValue(NAVBAR_BRAND_CONFIG_PARAM_NAME, webPageInfo.brand())
                .withKeyAndValue(FAVICON_LINK_CONFIG_PARAM_NAME, webPageInfo.favicon())
                .withKeyAndValue(SIGN_IN_PAGE_STATEMENT_CONFIG_PARAM_NAME, webPageInfo.signInStatement());

        Map<String, BoxColorSet> componentStateColorSetMap = webPageInfo.componentStateColorSetMap();
        if (componentStateColorSetMap != null && componentStateColorSetMap.size() > 0) {
            for (String state : componentStateColorSetMap.keySet()) {
                BoxColorSet stateColorSet = componentStateColorSetMap.get(state);
                String background = null;
                String font = null;
                if (stateColorSet != null) {
                    background = stateColorSet.getBackground();
                    font = stateColorSet.getFont();
                }

                request = request.withKeyAndValue(COMPONENT_STATE_BACKGROUND_COLOR_CONFIG_PARAM_NAME(state), background)
                        .withKeyAndValue(COMPONENT_STATE_FONT_COLOR_CONFIG_PARAM_NAME(state), font);
            }
        }

        Map<String, BoxColorSet> releaseStateColorSetMap = webPageInfo.releaseStateColorSetMap();
        if (releaseStateColorSetMap != null && releaseStateColorSetMap.size() > 0) {
            for (String state : releaseStateColorSetMap.keySet()) {
                BoxColorSet stateColorSet = releaseStateColorSetMap.get(state);
                String background = null;
                String font = null;
                if (stateColorSet != null) {
                    background = stateColorSet.getBackground();
                    font = stateColorSet.getFont();
                }

                request = request.withKeyAndValue(RELEASE_STATE_BACKGROUND_COLOR_CONFIG_PARAM_NAME(state), background)
                        .withKeyAndValue(RELEASE_STATE_FONT_COLOR_CONFIG_PARAM_NAME(state), font);
            }
        }

        Map<String, BoxColorSet> userRoleColorSetMap = webPageInfo.userRoleColorSetMap();
        if (userRoleColorSetMap != null && userRoleColorSetMap.size() > 0) {
            for (String state : userRoleColorSetMap.keySet()) {
                BoxColorSet roleColorSet = userRoleColorSetMap.get(state);
                String background = null;
                String font = null;
                if (roleColorSet != null) {
                    background = roleColorSet.getBackground();
                    font = roleColorSet.getFont();
                }

                request = request.withKeyAndValue(USER_ROLE_BACKGROUND_COLOR_CONFIG_PARAM_NAME(state), background)
                        .withKeyAndValue(USER_ROLE_FONT_COLOR_CONFIG_PARAM_NAME(state), font);
            }
        }

        configService.changeApplicationConfiguration(requester, request);
    }

}
