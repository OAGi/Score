package org.oagi.score.gateway.http.api.oas_management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.CreateOasDocRequest;
import org.oagi.score.gateway.http.api.oas_management.model.OasOAuthFlow;
import org.oagi.score.gateway.http.api.oas_management.model.OasOAuthScope;
import org.oagi.score.gateway.http.api.oas_management.model.OasSecurityScheme;
import org.oagi.score.gateway.http.api.oas_management.repository.OasDocCommandRepository;
import org.oagi.score.gateway.http.api.oas_management.repository.OasDocQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Issue #1891: the key of components.securitySchemes has only letters, digits, '.', '-' and '_' (the Components Object of the
 * OpenAPI Specification), and a scope is an OAuth 2.0 scope token (printable ASCII without a space, a double quote and a
 * backslash, RFC 6749). The columns of the names hold ASCII only, and the service refuses any other name before anything is
 * saved.
 */
class OpenAPIDocServiceSecuritySchemeNameTest {

    private OpenAPIDocService service;
    private OasDocCommandRepository command;
    private ScoreUser requester;

    @BeforeEach
    void setUp() {
        service = new OpenAPIDocService();
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        OasDocQueryRepository query = mock(OasDocQueryRepository.class);
        command = mock(OasDocCommandRepository.class);
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);
        requester = new ScoreUser(new UserId(BigInteger.valueOf(42)), "tester", "Tester",
                "tester@example.com", true, List.of(ScoreRole.DEVELOPER));
        when(repositoryFactory.oasDocQueryRepository(any())).thenReturn(query);
        when(repositoryFactory.oasDocCommandRepository(any())).thenReturn(command);
        when(query.checkOasDocUniqueness(any())).thenReturn(true);
    }

    private CreateOasDocRequest create(String schemeName) {
        OasSecurityScheme scheme = new OasSecurityScheme();
        scheme.setType("openIdConnect");
        scheme.setOpenIdConnectUrl("https://example.com/.well-known/openid-configuration");
        scheme.setSchemeName(schemeName);
        CreateOasDocRequest request = new CreateOasDocRequest(requester);
        request.setTitle("t");
        request.setOpenAPIVersion("3.1");
        request.setVersion("1.0");
        request.setSecuritySchemes(List.of(scheme));
        return request;
    }

    @Test
    void aNameOfLettersDigitsDotDashAndUnderscoreIsAccepted() {
        for (String name : List.of("Auth", "auth", "Api-Key_2.v1", "A")) {
            service.createOasDoc(requester, create(name));
        }
        verify(command, times(4)).createOasDoc(any());
    }

    @Test
    void aNameWithAnythingElseIsRefusedAndNothingIsSaved() {
        for (String name : List.of("Auth\uD83D\uDE00", "인증", "Auth Key", "Auth/Key", "Auth:Key", "Caf\u00e9")) {
            assertThrows(IllegalArgumentException.class, () -> service.createOasDoc(requester, create(name)), name);
        }
        verify(command, never()).createOasDoc(any());
    }

    private CreateOasDocRequest createOauth2(String scopeName) {
        OasOAuthScope scope = new OasOAuthScope();
        scope.setScopeName(scopeName);
        OasOAuthFlow flow = new OasOAuthFlow();
        flow.setFlowType("clientCredentials");
        flow.setTokenUrl("https://example.com/token");
        flow.setScopes(List.of(scope));
        OasSecurityScheme scheme = new OasSecurityScheme();
        scheme.setType("oauth2");
        scheme.setSchemeName("OAuth2");
        scheme.setFlows(List.of(flow));
        CreateOasDocRequest request = new CreateOasDocRequest(requester);
        request.setTitle("t");
        request.setOpenAPIVersion("3.1");
        request.setVersion("1.0");
        request.setSecuritySchemes(List.of(scheme));
        return request;
    }

    @Test
    void aScopeIsAnOAuthScopeToken() {
        for (String scope : List.of("read", "Read", "read:pets", "https://example.com/auth/read")) {
            service.createOasDoc(requester, createOauth2(scope));
        }
        verify(command, times(4)).createOasDoc(any());
        clearInvocations(command);
        for (String scope : List.of("read\uD83D\uDE00", "\uC77D\uAE30", "read pets", "read\"pets", "read\\pets")) {
            assertThrows(IllegalArgumentException.class, () -> service.createOasDoc(requester, createOauth2(scope)), scope);
        }
        verify(command, never()).createOasDoc(any());
    }
}
