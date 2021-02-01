package org.oagi.score.gateway.http.configuration.oauth2;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@Component
public class ScoreOAuth2AuthorizedClientService implements OAuth2AuthorizedClientService {

    private static final String CLIENT_REGISTRATION_ID_KEY = "client_registration_id";
    private static final String PRINCIPAL_NAME_KEY = "principal_name";
    private static final String ACCESS_TOKEN_TYPE_KEY = "access_token_type";
    private static final String ACCESS_TOKEN_VALUE_KEY = "access_token_value";
    private static final String ACCESS_TOKEN_ISSUED_AT_KEY = "access_token_issued_at";
    private static final String ACCESS_TOKEN_EXPIRES_AT_KEY = "access_token_expires_at";
    private static final String ACCESS_TOKEN_SCOPES_KEY = "access_token_scopes";
    private static final String REFRESH_TOKEN_VALUE_KEY = "refresh_token_value";
    private static final String REFRESH_TOKEN_ISSUED_AT_KEY = "refresh_token_issued_at";
    private static final String CREATED_AT_KEY = "created_at";

    private static final List<String> hashKeys = Arrays.asList(
            CLIENT_REGISTRATION_ID_KEY, PRINCIPAL_NAME_KEY,
            ACCESS_TOKEN_TYPE_KEY, ACCESS_TOKEN_VALUE_KEY,
            ACCESS_TOKEN_ISSUED_AT_KEY, ACCESS_TOKEN_EXPIRES_AT_KEY, ACCESS_TOKEN_SCOPES_KEY,
            REFRESH_TOKEN_VALUE_KEY, REFRESH_TOKEN_ISSUED_AT_KEY, CREATED_AT_KEY
    );

    public static final String DEFAULT_NAMESPACE = "score:oauth2";

    private final RedisOperations<Object, Object> sessionRedisOperations;

    private final ClientRegistrationRepository clientRegistrationRepository;

    private final ObjectMapper mapper;

    public ScoreOAuth2AuthorizedClientService(@Autowired RedisOperations<Object, Object> sessionRedisOperations,
                                              @Autowired ClientRegistrationRepository clientRegistrationRepository) {
        this.sessionRedisOperations = sessionRedisOperations;
        this.clientRegistrationRepository = clientRegistrationRepository;

        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new ScoreOAuth2ClientJackson2Module());
    }

    private String getKey(String clientRegistrationId, String principalName) {
        return DEFAULT_NAMESPACE + ":authorized_client:" + clientRegistrationId + ":" + principalName;
    }

    @Override
    public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(String clientRegistrationId, String principalName) {
        Assert.hasText(clientRegistrationId, "clientRegistrationId cannot be empty");
        Assert.hasText(principalName, "principalName cannot be empty");

        Map<Object, Object> authorizedClientMap = sessionRedisOperations.opsForHash()
                .entries(getKey(clientRegistrationId, principalName));
        if (authorizedClientMap == null || authorizedClientMap.isEmpty()) {
            return null;
        }

        ClientRegistration clientRegistration = this.clientRegistrationRepository.findByRegistrationId(
                clientRegistrationId);
        if (clientRegistration == null) {
            throw new DataRetrievalFailureException("The ClientRegistration with id '" +
                    clientRegistrationId + "' exists in the data source, " +
                    "however, it was not found in the ClientRegistrationRepository.");
        }

        OAuth2AccessToken.TokenType tokenType = null;
        if (OAuth2AccessToken.TokenType.BEARER.getValue().equalsIgnoreCase(
                (String) authorizedClientMap.get(ACCESS_TOKEN_TYPE_KEY))) {
            tokenType = OAuth2AccessToken.TokenType.BEARER;
        }
        String tokenValue = new String((byte[]) authorizedClientMap.get(ACCESS_TOKEN_VALUE_KEY), StandardCharsets.UTF_8);
        Instant issuedAt = ((Timestamp) authorizedClientMap.get(ACCESS_TOKEN_ISSUED_AT_KEY)).toInstant();
        Instant expiresAt = ((Timestamp) authorizedClientMap.get(ACCESS_TOKEN_EXPIRES_AT_KEY)).toInstant();
        Set<String> scopes = Collections.emptySet();
        String accessTokenScopes = (String) authorizedClientMap.get(ACCESS_TOKEN_SCOPES_KEY);
        if (accessTokenScopes != null) {
            scopes = StringUtils.commaDelimitedListToSet(accessTokenScopes);
        }
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                tokenType, tokenValue, issuedAt, expiresAt, scopes);

        OAuth2RefreshToken refreshToken = null;
        byte[] refreshTokenValue = (byte[]) authorizedClientMap.get(REFRESH_TOKEN_VALUE_KEY);
        if (refreshTokenValue != null) {
            tokenValue = new String(refreshTokenValue, StandardCharsets.UTF_8);
            issuedAt = null;
            Timestamp refreshTokenIssuedAt = (Timestamp) authorizedClientMap.get(REFRESH_TOKEN_ISSUED_AT_KEY);
            if (refreshTokenIssuedAt != null) {
                issuedAt = refreshTokenIssuedAt.toInstant();
            }
            refreshToken = new OAuth2RefreshToken(tokenValue, issuedAt);
        }

        return (T) new OAuth2AuthorizedClient(
                clientRegistration, principalName, accessToken, refreshToken);
    }

    @Override
    public void saveAuthorizedClient(OAuth2AuthorizedClient authorizedClient, Authentication principal) {
        Assert.notNull(authorizedClient, "authorizedClient cannot be null");
        Assert.notNull(principal, "principal cannot be null");

        ClientRegistration clientRegistration = authorizedClient.getClientRegistration();
        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        OAuth2RefreshToken refreshToken = authorizedClient.getRefreshToken();

        Map<String, Object> authorizedClientMap = new HashMap<>();
        authorizedClientMap.put(CLIENT_REGISTRATION_ID_KEY, clientRegistration.getRegistrationId());
        authorizedClientMap.put(PRINCIPAL_NAME_KEY, principal.getName());
        authorizedClientMap.put(ACCESS_TOKEN_TYPE_KEY, accessToken.getTokenType().getValue());
        authorizedClientMap.put(ACCESS_TOKEN_VALUE_KEY, accessToken.getTokenValue().getBytes(StandardCharsets.UTF_8));
        authorizedClientMap.put(ACCESS_TOKEN_ISSUED_AT_KEY, Timestamp.from(accessToken.getIssuedAt()));
        authorizedClientMap.put(ACCESS_TOKEN_EXPIRES_AT_KEY, Timestamp.from(accessToken.getExpiresAt()));
        String accessTokenScopes = null;
        if (!CollectionUtils.isEmpty(accessToken.getScopes())) {
            accessTokenScopes = StringUtils.collectionToDelimitedString(accessToken.getScopes(), ",");
        }
        authorizedClientMap.put(ACCESS_TOKEN_SCOPES_KEY, accessTokenScopes);
        byte[] refreshTokenValue = null;
        Timestamp refreshTokenIssuedAt = null;
        if (refreshToken != null) {
            refreshTokenValue = refreshToken.getTokenValue().getBytes(StandardCharsets.UTF_8);
            if (refreshToken.getIssuedAt() != null) {
                refreshTokenIssuedAt = Timestamp.from(refreshToken.getIssuedAt());
            }
        }
        authorizedClientMap.put(REFRESH_TOKEN_VALUE_KEY, refreshTokenValue);
        authorizedClientMap.put(REFRESH_TOKEN_ISSUED_AT_KEY, refreshTokenIssuedAt);
        authorizedClientMap.put(CREATED_AT_KEY, new Timestamp(System.currentTimeMillis()));

        sessionRedisOperations.opsForHash()
                .putAll(getKey(clientRegistration.getRegistrationId(), principal.getName()),
                        authorizedClientMap);
    }

    @Override
    public void removeAuthorizedClient(String clientRegistrationId, String principalName) {
        Assert.hasText(clientRegistrationId, "clientRegistrationId cannot be empty");
        Assert.hasText(principalName, "principalName cannot be empty");

        sessionRedisOperations.opsForHash()
                .delete(getKey(clientRegistrationId, principalName), hashKeys);
    }
}