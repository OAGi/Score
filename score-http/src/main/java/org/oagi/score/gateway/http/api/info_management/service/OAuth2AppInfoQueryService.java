package org.oagi.score.gateway.http.api.info_management.service;


import org.jooq.DSLContext;
import org.oagi.score.gateway.http.api.info_management.model.OAuth2AppInfoRecord;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.Oauth2AppRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.oagi.score.gateway.http.common.repository.jooq.entity.tables.Oauth2App.OAUTH2_APP;

@Service
@Transactional(readOnly = true)
public class OAuth2AppInfoQueryService {

    private static final String authorizationRequestBaseUri = "oauth2/authorization";

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    public List<OAuth2AppInfoRecord> getOAuth2AppList(ScoreUser requester) {
        return dslContext.selectFrom(OAUTH2_APP)
                .where(OAUTH2_APP.IS_DISABLED.eq((byte) 0))
                .stream()
                .sorted(Comparator.comparingInt(Oauth2AppRecord::getDisplayOrder))
                .map(oauth2AppRecord -> {
                    String registrationId = clientRegistrationRepository
                            .findByRegistrationId(oauth2AppRecord.getProviderName())
                            .getRegistrationId();

                    return new OAuth2AppInfoRecord(
                            authorizationRequestBaseUri + "/" + registrationId,
                            oauth2AppRecord.getProviderName(),
                            oauth2AppRecord.getDisplayProviderName(),
                            oauth2AppRecord.getFontColor(),
                            oauth2AppRecord.getBackgroundColor()
                    );
                }).collect(Collectors.toList());
    }

}