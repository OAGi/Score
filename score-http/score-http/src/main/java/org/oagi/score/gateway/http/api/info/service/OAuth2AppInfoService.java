package org.oagi.score.gateway.http.api.info.service;


import org.jooq.DSLContext;
import org.oagi.score.gateway.http.api.info.data.OAuth2AppInfo;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.Oauth2AppRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.oagi.score.repo.api.impl.jooq.entity.tables.Oauth2App.OAUTH2_APP;

@Service
@Transactional(readOnly = true)
public class OAuth2AppInfoService {

    private static final String authorizationRequestBaseUri = "oauth2/authorization";

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    public List<OAuth2AppInfo> getOAuth2AppList(AuthenticatedPrincipal user) {
        return dslContext.selectFrom(OAUTH2_APP)
                .where(OAUTH2_APP.IS_DISABLED.eq((byte) 0))
                .stream()
                .sorted(Comparator.comparingInt(Oauth2AppRecord::getDisplayOrder))
                .map(oauth2AppRecord -> {
                    String registrationId = clientRegistrationRepository
                            .findByRegistrationId(oauth2AppRecord.getProviderName())
                            .getRegistrationId();

                    OAuth2AppInfo appInfo = new OAuth2AppInfo();
                    appInfo.setLoginUrl(authorizationRequestBaseUri + "/" + registrationId);
                    appInfo.setProviderName(oauth2AppRecord.getProviderName());
                    appInfo.setDisplayProviderName(oauth2AppRecord.getDisplayProviderName());
                    appInfo.setFontColor(oauth2AppRecord.getFontColor());
                    appInfo.setBackgroundColor(oauth2AppRecord.getBackgroundColor());
                    return appInfo;
                }).collect(Collectors.toList());
    }
}