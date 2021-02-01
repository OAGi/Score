package org.oagi.score.gateway.http.configuration.oauth2;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Repository
public class ScoreOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public static final String DEFAULT_NAMESPACE = "score:oauth2";

    private final RedisOperations<Object, Object> sessionRedisOperations;

    private final ObjectMapper mapper;

    public ScoreOAuth2AuthorizationRequestRepository(@Autowired RedisOperations<Object, Object> sessionRedisOperations) {
        this.sessionRedisOperations = sessionRedisOperations;

        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new ScoreOAuth2ClientJackson2Module());
    }

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        Assert.notNull(request, "request cannot be null");
        return this.getAuthorizationRequests(request);
    }

    private String getKey(String state) {
        return DEFAULT_NAMESPACE + ":authorization_req:" + state;
    }

    @Override
    @SneakyThrows
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request,
                                         HttpServletResponse response) {
        Assert.notNull(request, "request cannot be null");
        Assert.notNull(response, "response cannot be null");
        if (authorizationRequest == null) {
            this.removeAuthorizationRequest(request, response);
            return;
        }
        String state = authorizationRequest.getState();
        Assert.hasText(state, "authorizationRequest.state cannot be empty");

        sessionRedisOperations.opsForValue()
                .set(getKey(state), mapper.writeValueAsString(authorizationRequest));
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request) {
        Assert.notNull(request, "request cannot be null");
        String stateParameter = this.getStateParameter(request);
        if (stateParameter == null) {
            return null;
        }
        OAuth2AuthorizationRequest originalRequest = this.getAuthorizationRequests(request);
        if (originalRequest != null) {
            sessionRedisOperations.delete(getKey(stateParameter));
        }
        return originalRequest;
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        Assert.notNull(response, "response cannot be null");
        return this.removeAuthorizationRequest(request);
    }

    /**
     * Gets the state parameter from the {@link HttpServletRequest}
     *
     * @param request the request to use
     * @return the state parameter or null if not found
     */
    private String getStateParameter(HttpServletRequest request) {
        return request.getParameter(OAuth2ParameterNames.STATE);
    }

    /**
     * Gets a non-null and mutable map of {@link OAuth2AuthorizationRequest#getState()} to an {@link OAuth2AuthorizationRequest}
     *
     * @param request
     * @return a non-null and mutable map of {@link OAuth2AuthorizationRequest#getState()} to an {@link OAuth2AuthorizationRequest}.
     */
    @SneakyThrows
    private OAuth2AuthorizationRequest getAuthorizationRequests(HttpServletRequest request) {
        String stateParameter = this.getStateParameter(request);
        if (stateParameter == null) {
            return null;
        }

        String requestObjString = (String) sessionRedisOperations.opsForValue().get(getKey(stateParameter));
        return (!StringUtils.hasLength(requestObjString)) ? null :
                mapper.readValue(requestObjString, OAuth2AuthorizationRequest.class);
    }
}