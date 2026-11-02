package org.oagi.score.gateway.http.configuration.security;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationQueryService;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.core.annotation.Order;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The /ext/** filter chain that score-external-api's AuthGuard forwards the caller's Authorization header to
 * (GET /ext/auth). The real {@link SecurityConfiguration#externalFilterChain} is built; the JWT keys are served by a
 * local JWKS endpoint; the endpoint behind the chain is a stub, since the chain is what is under test.
 */
class ExternalFilterChainTest {

    private static HttpServer jwks;
    private static String jwkSetUri;
    private static RSAKey signingKey;

    /** Read by {@link Config}: the context is built per test, so the chain can be built for each setup. */
    private static SecurityConfiguration securityConfiguration;

    @BeforeAll
    static void startJwksEndpoint() throws Exception {
        signingKey = new RSAKeyGenerator(2048).keyID("test-key").generate();
        byte[] body = new JWKSet(signingKey.toPublicJWK()).toString().getBytes(StandardCharsets.UTF_8);

        jwks = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        jwks.createContext("/jwks", exchange -> {
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        jwks.start();
        jwkSetUri = "http://127.0.0.1:" + jwks.getAddress().getPort() + "/jwks";
    }

    @AfterAll
    static void stopJwksEndpoint() {
        jwks.stop(0);
    }

    @Configuration
    @EnableWebSecurity
    @EnableWebMvc
    static class Config {

        @Bean
        @Order(1)
        SecurityFilterChain externalFilterChain(HttpSecurity http) throws Exception {
            return securityConfiguration.externalFilterChain(http);
        }

        @Bean
        StubExternalController stubExternalController() {
            return new StubExternalController();
        }
    }

    @RestController
    static class StubExternalController {
        @GetMapping("/ext/auth")
        String auth() {
            return "Authenticated";
        }
    }

    private MockMvc mvc(boolean tenantEnabled, String allowedTypes) {
        ApplicationConfigurationQueryService configurationService = mock(ApplicationConfigurationQueryService.class);
        when(configurationService.isTenantEnabled(any())).thenReturn(tenantEnabled);
        SessionService sessionService = mock(SessionService.class);
        when(sessionService.getScoreSystemUser()).thenReturn(mock(ScoreUser.class));

        securityConfiguration = new SecurityConfiguration();
        ReflectionTestUtils.setField(securityConfiguration, "applicationConfigurationService", configurationService);
        ReflectionTestUtils.setField(securityConfiguration, "sessionService", sessionService);
        ReflectionTestUtils.setField(securityConfiguration, "jwkSetUri", jwkSetUri);
        ReflectionTestUtils.setField(securityConfiguration, "allowedTypes", allowedTypes);

        GenericWebApplicationContext context = new GenericWebApplicationContext(new MockServletContext());
        AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
        context.registerBean(Config.class);
        context.refresh();
        return webAppContextSetup(context).apply(springSecurity()).build();
    }

    private MockMvc mvc() {
        return mvc(false, "");
    }

    private static String token(RSAKey key, JOSEObjectType type, Date expiresAt) throws Exception {
        SignedJWT jwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID("test-key").type(type).build(),
                new JWTClaimsSet.Builder().subject("machine-client").expirationTime(expiresAt).build());
        jwt.sign(new RSASSASigner(key));
        return jwt.serialize();
    }

    private static String validToken() throws Exception {
        return token(signingKey, JOSEObjectType.JWT, new Date(System.currentTimeMillis() + 600_000));
    }

    private static MockHttpServletRequestBuilder authRequest(String bearer) {
        return get("/ext/auth").header("Authorization", "Bearer " + bearer);
    }

    @Test
    void aValidBearerTokenIsAuthenticated() throws Exception {
        mvc().perform(authRequest(validToken())).andExpect(status().isOk());
    }

    @Test
    void noTokenIsUnauthorized() throws Exception {
        mvc().perform(get("/ext/auth")).andExpect(status().isUnauthorized());
    }

    @Test
    void aMalformedTokenIsUnauthorized() throws Exception {
        mvc().perform(authRequest("not-a-jwt")).andExpect(status().isUnauthorized());
    }

    @Test
    void aTokenSignedWithAnotherKeyIsUnauthorized() throws Exception {
        RSAKey other = new RSAKeyGenerator(2048).keyID("test-key").generate();
        String forged = token(other, JOSEObjectType.JWT, new Date(System.currentTimeMillis() + 600_000));

        mvc().perform(authRequest(forged)).andExpect(status().isUnauthorized());
    }

    @Test
    void anExpiredTokenIsUnauthorized() throws Exception {
        String expired = token(signingKey, JOSEObjectType.JWT, new Date(System.currentTimeMillis() - 600_000));

        mvc().perform(authRequest(expired)).andExpect(status().isUnauthorized());
    }

    @Test
    void aTokenWhoseTypeIsNotJwtIsUnauthorizedByDefault() throws Exception {
        String accessToken = token(signingKey, new JOSEObjectType("at+jwt"), new Date(System.currentTimeMillis() + 600_000));

        mvc(false, "").perform(authRequest(accessToken)).andExpect(status().isUnauthorized());
    }

    /** application.yml configures `JWT,at+jwt` by default (RS_ALLOWED_TYPES). */
    @Test
    void aConfiguredTokenTypeIsAuthenticated() throws Exception {
        String accessToken = token(signingKey, new JOSEObjectType("at+jwt"), new Date(System.currentTimeMillis() + 600_000));

        mvc(false, "JWT, at+jwt").perform(authRequest(accessToken)).andExpect(status().isOk());
    }

    @Test
    void aTypeThatIsNotConfiguredIsUnauthorized() throws Exception {
        String other = token(signingKey, new JOSEObjectType("other"), new Date(System.currentTimeMillis() + 600_000));

        mvc(false, "JWT, at+jwt").perform(authRequest(other)).andExpect(status().isUnauthorized());
    }

    @Test
    void aConfiguredTokenTypeStillHonoursTheExpiry() throws Exception {
        String expired = token(signingKey, new JOSEObjectType("at+jwt"), new Date(System.currentTimeMillis() - 600_000));

        mvc(false, "JWT, at+jwt").perform(authRequest(expired)).andExpect(status().isUnauthorized());
    }

    @Test
    void aSessionCookieDoesNotAuthenticateAnExternalRequest() throws Exception {
        mvc().perform(get("/ext/auth").header("Cookie", "SESSION=abc")).andExpect(status().isUnauthorized());
    }

    @Test
    void aCrossOriginRequestIsAuthenticatedByTheTokenAndGetsNoCorsHeaders() throws Exception {
        mvc().perform(authRequest(validToken()).header("Origin", "http://evil.example"))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Credentials"));
    }

    @Test
    void aPreflightRequestGetsNoCorsHeaders() throws Exception {
        ResultActions result = mvc().perform(options("/ext/auth")
                .header("Origin", "http://evil.example")
                .header("Access-Control-Request-Method", "GET"));

        result.andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    void everyExternalRequestIsDeniedWhileTenantsAreEnabled() throws Exception {
        mvc(true, "").perform(authRequest(validToken())).andExpect(status().isForbidden());
    }
}
