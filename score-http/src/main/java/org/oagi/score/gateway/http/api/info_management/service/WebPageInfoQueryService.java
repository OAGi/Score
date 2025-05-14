package org.oagi.score.gateway.http.api.info_management.service;

import com.google.common.collect.ObjectArrays;
import org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationService;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.info_management.model.BoxColorSet;
import org.oagi.score.gateway.http.api.info_management.model.WebPageInfoRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseState;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationService.*;

@Service
@Transactional(readOnly = true)
public class WebPageInfoQueryService {

    @Autowired
    private ApplicationConfigurationService configService;

    public WebPageInfoRecord getWebPageInfo(ScoreUser requester) {
        String brand = configService.getProperty(requester, NAVBAR_BRAND_CONFIG_PARAM_NAME);
        String favicon = configService.getProperty(requester, FAVICON_LINK_CONFIG_PARAM_NAME);
        String signInStatement = configService.getProperty(requester, SIGN_IN_PAGE_STATEMENT_CONFIG_PARAM_NAME);

        Map<String, BoxColorSet> componentStateColorSetMap = new HashMap<>();
        for (String state : ObjectArrays.concat(Arrays.stream(CcState.values()).map(e -> e.name())
                .collect(Collectors.toList()).toArray(new String[CcState.values().length]), "Deprecated")) {
            BoxColorSet boxColorSet = new BoxColorSet();
            boxColorSet.setBackground(configService.getProperty(requester, COMPONENT_STATE_BACKGROUND_COLOR_CONFIG_PARAM_NAME(state)));
            boxColorSet.setFont(configService.getProperty(requester, COMPONENT_STATE_FONT_COLOR_CONFIG_PARAM_NAME(state)));
            componentStateColorSetMap.put(state, boxColorSet);
        }

        Map<String, BoxColorSet> releaseStateColorSetList = new HashMap<>();
        for (String state : Arrays.stream(ReleaseState.values()).map(e -> e.name()).collect(Collectors.toList())) {
            BoxColorSet boxColorSet = new BoxColorSet();
            boxColorSet.setBackground(configService.getProperty(requester, RELEASE_STATE_BACKGROUND_COLOR_CONFIG_PARAM_NAME(state)));
            boxColorSet.setFont(configService.getProperty(requester, RELEASE_STATE_FONT_COLOR_CONFIG_PARAM_NAME(state)));
            releaseStateColorSetList.put(state, boxColorSet);
        }

        Map<String, BoxColorSet> userRoleColorSetList = new HashMap<>();
        for (String state : Arrays.asList("Admin", "Developer", "End-User")) {
            BoxColorSet boxColorSet = new BoxColorSet();
            boxColorSet.setBackground(configService.getProperty(requester, USER_ROLE_BACKGROUND_COLOR_CONFIG_PARAM_NAME(state)));
            boxColorSet.setFont(configService.getProperty(requester, USER_ROLE_FONT_COLOR_CONFIG_PARAM_NAME(state)));
            userRoleColorSetList.put(state, boxColorSet);
        }

        return new WebPageInfoRecord(brand, favicon, signInStatement,
                componentStateColorSetMap, releaseStateColorSetList, userRoleColorSetList);
    }

}
