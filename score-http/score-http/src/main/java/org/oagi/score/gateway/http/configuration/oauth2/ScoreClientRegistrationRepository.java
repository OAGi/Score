package org.oagi.score.gateway.http.configuration.oauth2;


import org.jooq.DSLContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.oagi.score.repo.api.impl.jooq.entity.tables.Oauth2App.OAUTH2_APP;
import static org.oagi.score.repo.api.impl.jooq.entity.tables.Oauth2AppScope.OAUTH2_APP_SCOPE;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.*;
import static org.springframework.security.oauth2.core.ClientAuthenticationMethod.BASIC;
import static org.springframework.security.oauth2.core.ClientAuthenticationMethod.POST;

@Component
public class ScoreClientRegistrationRepository
        implements ClientRegistrationRepository, Iterable<ClientRegistration>, InitializingBean {

    private static final Map<String, ClientAuthenticationMethod> predefinedClientAuthenticationMethodMap =
            Arrays.asList(BASIC, POST)
                    .stream().collect(Collectors.toMap(ClientAuthenticationMethod::getValue, Function.identity()));

    private static final Map<String, AuthorizationGrantType> predefinedAuthorizationGrantTypeMap =
            Arrays.asList(AUTHORIZATION_CODE, IMPLICIT, REFRESH_TOKEN, CLIENT_CREDENTIALS, PASSWORD)
                    .stream().collect(Collectors.toMap(AuthorizationGrantType::getValue, Function.identity()));

    @Autowired
    private DSLContext dslContext;

    private Map<String, ClientRegistration> registrations;

    private final Map<String, Map<String, Object>> additionalParameters = new HashMap();

    @Override
    public void afterPropertiesSet() throws Exception {
        registrations = dslContext.selectFrom(OAUTH2_APP)
                .where(OAUTH2_APP.IS_DISABLED.eq((byte) 0))
                .stream().map(oauth2AppRecord -> {
                    AuthorizationGrantType authorizationGrantType =
                            predefinedAuthorizationGrantTypeMap.get(oauth2AppRecord.getAuthorizationGrantType());
                    if (authorizationGrantType == null) {
                        authorizationGrantType = new AuthorizationGrantType(oauth2AppRecord.getAuthorizationGrantType());
                    }

                    ClientAuthenticationMethod clientAuthenticationMethod =
                            predefinedClientAuthenticationMethodMap.get(oauth2AppRecord.getClientAuthenticationMethod());
                    if (clientAuthenticationMethod == null) {
                        clientAuthenticationMethod = new ClientAuthenticationMethod(oauth2AppRecord.getClientAuthenticationMethod());
                    }

                    List<String> scopes = dslContext.select(OAUTH2_APP_SCOPE.SCOPE)
                            .from(OAUTH2_APP_SCOPE)
                            .where(OAUTH2_APP_SCOPE.OAUTH2_APP_ID.eq(oauth2AppRecord.getOauth2AppId()))
                            .fetchInto(String.class);

                    String issuerUri = oauth2AppRecord.getIssuerUri();
                    ClientRegistration.Builder builder;
                    if (StringUtils.hasLength(issuerUri)) {
                        builder = ClientRegistrations.fromIssuerLocation(issuerUri)
                                .registrationId(oauth2AppRecord.getProviderName());
                    } else {
                        builder = ClientRegistration.withRegistrationId(oauth2AppRecord.getProviderName())
                                .authorizationUri(oauth2AppRecord.getAuthorizationUri())
                                .tokenUri(oauth2AppRecord.getTokenUri())
                                .userInfoUri(oauth2AppRecord.getUserInfoUri())
                                .jwkSetUri(oauth2AppRecord.getJwkSetUri());
                    }

                    String prompt = oauth2AppRecord.getPrompt();
                    if (StringUtils.hasLength(prompt)) {
                        String providerName = oauth2AppRecord.getProviderName();
                        if (!this.additionalParameters.containsKey(providerName)) {
                            this.additionalParameters.put(providerName, new HashMap());
                        }

                        this.additionalParameters.get(providerName)
                                .put("prompt", prompt);
                    }

                    return builder.clientId(oauth2AppRecord.getClientId())
                            .clientSecret(oauth2AppRecord.getClientSecret())
                            .redirectUriTemplate(oauth2AppRecord.getRedirectUri())
                            .clientAuthenticationMethod(clientAuthenticationMethod)
                            .authorizationGrantType(authorizationGrantType)
                            .scope(scopes)
                            .build();
                }).collect(Collectors.toMap(ClientRegistration::getRegistrationId, Function.identity()));
    }

    @Override
    public Iterator<ClientRegistration> iterator() {
        return this.registrations.values().iterator();
    }

    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {
        Assert.hasText(registrationId, "registrationId cannot be empty");
        return this.registrations.get(registrationId);
    }

    public Map<String, Object> additionalParameters(OAuth2AuthorizationRequest authorizationRequest) {
        Map<String, Object> additionalParameters =
                new LinkedHashMap<>(authorizationRequest.getAdditionalParameters());
        String registrationId = authorizationRequest.getAttribute("registration_id");
        if (StringUtils.hasLength(registrationId)) {
            additionalParameters.putAll(
                    this.additionalParameters.getOrDefault(registrationId, Collections.emptyMap())
            );
        }
        return additionalParameters;
    }
}