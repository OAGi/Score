package org.oagi.score.gateway.http.api.info.service;

import com.google.common.collect.ObjectArrays;
import org.oagi.score.gateway.http.api.application_management.data.ApplicationConfigurationChangeRequest;
import org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationService;
import org.oagi.score.gateway.http.api.info.data.BoxColorSet;
import org.oagi.score.gateway.http.api.info.data.WebPageInfo;
import org.oagi.score.repo.api.corecomponent.model.CcState;
import org.oagi.score.repo.api.release.model.ReleaseState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationService.*;
import static org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationService.COMPONENT_STATE_BACKGROUND_COLOR_CONFIG_PARAM_NAME;

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

        Map<String, BoxColorSet> componentStateColorSetMap = new HashMap<>();
        for (String state : ObjectArrays.concat(Arrays.stream(CcState.values()).map(e -> e.name())
                .collect(Collectors.toList()).toArray(new String[CcState.values().length]), "Deprecated")) {
            BoxColorSet boxColorSet = new BoxColorSet();
            boxColorSet.setBackground(configService.getProperty(COMPONENT_STATE_BACKGROUND_COLOR_CONFIG_PARAM_NAME(state)));
            boxColorSet.setFont(configService.getProperty(COMPONENT_STATE_FONT_COLOR_CONFIG_PARAM_NAME(state)));
            componentStateColorSetMap.put(state, boxColorSet);
        }
        webPageInfo.setComponentStateColorSetMap(componentStateColorSetMap);

        Map<String, BoxColorSet> releaseStateColorSetList = new HashMap<>();
        for (String state : Arrays.stream(ReleaseState.values()).map(e -> e.name()).collect(Collectors.toList())) {
            BoxColorSet boxColorSet = new BoxColorSet();
            boxColorSet.setBackground(configService.getProperty(RELEASE_STATE_BACKGROUND_COLOR_CONFIG_PARAM_NAME(state)));
            boxColorSet.setFont(configService.getProperty(RELEASE_STATE_FONT_COLOR_CONFIG_PARAM_NAME(state)));
            releaseStateColorSetList.put(state, boxColorSet);
        }
        webPageInfo.setReleaseStateColorSetMap(releaseStateColorSetList);

        Map<String, BoxColorSet> userRoleColorSetList = new HashMap<>();
        for (String state : Arrays.asList("Admin", "Developer", "End-User")) {
            BoxColorSet boxColorSet = new BoxColorSet();
            boxColorSet.setBackground(configService.getProperty(USER_ROLE_BACKGROUND_COLOR_CONFIG_PARAM_NAME(state)));
            boxColorSet.setFont(configService.getProperty(USER_ROLE_FONT_COLOR_CONFIG_PARAM_NAME(state)));
            userRoleColorSetList.put(state, boxColorSet);
        }
        webPageInfo.setUserRoleColorSetMap(userRoleColorSetList);

        return webPageInfo;
    }

    @Transactional(readOnly = false)
    public void updateWebPageInfo(AuthenticatedPrincipal user, WebPageInfo webPageInfo) {
        ApplicationConfigurationChangeRequest request = new ApplicationConfigurationChangeRequest()
                .withKeyAndValue(NAVBAR_BRAND_CONFIG_PARAM_NAME, webPageInfo.getBrand())
                .withKeyAndValue(FAVICON_LINK_CONFIG_PARAM_NAME, webPageInfo.getFavicon())
                .withKeyAndValue(SIGN_IN_PAGE_STATEMENT_CONFIG_PARAM_NAME, webPageInfo.getSignInStatement());

        Map<String, BoxColorSet> componentStateColorSetMap = webPageInfo.getComponentStateColorSetMap();
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

        Map<String, BoxColorSet> releaseStateColorSetMap = webPageInfo.getReleaseStateColorSetMap();
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

        Map<String, BoxColorSet> userRoleColorSetMap = webPageInfo.getUserRoleColorSetMap();
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

        configService.changeApplicationConfiguration(user, request);
    }

}
