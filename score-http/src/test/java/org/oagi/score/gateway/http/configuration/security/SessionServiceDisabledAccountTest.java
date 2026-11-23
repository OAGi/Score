package org.oagi.score.gateway.http.configuration.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.repository.ScoreUserQueryRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.session.MapSession;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Disabling an account ends its password and SSO sessions, and a disabled account is read from the database. */
class SessionServiceDisabledAccountTest {

    private static final List<SimpleGrantedAuthority> AUTHORITIES = List.of(new SimpleGrantedAuthority("ROLE_END_USER"));

    private final ScoreUserQueryRepository query = mock(ScoreUserQueryRepository.class);
    private final RedisIndexedSessionRepository sessions = mock(RedisIndexedSessionRepository.class);
    private final SessionService service = new SessionService();

    @BeforeEach
    void setUp() {
        RepositoryFactory repositories = mock(RepositoryFactory.class);
        when(repositories.scoreUserQueryRepository()).thenReturn(query);
        ReflectionTestUtils.setField(service, "repositoryFactory", repositories);
        ReflectionTestUtils.setField(service, "sessionRepository", sessions);
    }

    private static OAuth2AuthenticationToken oidc(String sub) {
        OidcIdToken idToken = new OidcIdToken("t", Instant.now(), Instant.now().plusSeconds(60), Map.of("sub", sub));
        return new OAuth2AuthenticationToken(new DefaultOidcUser(AUTHORITIES, idToken), AUTHORITIES, "idp");
    }

    @Test
    void theSessionsOfTheLoginIdAndOfEveryLinkedSubAreEnded() {
        when(query.getOidcSubs("alice")).thenReturn(List.of("sub-1"));
        doReturn(Map.of("s-password", new MapSession("s-password"))).when(sessions).findByPrincipalName("alice");
        doReturn(Map.of("s-sso", new MapSession("s-sso"))).when(sessions).findByPrincipalName("sub-1");

        service.invalidateByUsername("alice");

        verify(sessions).deleteById("s-password");
        verify(sessions).deleteById("s-sso");
    }

    @Test
    void aPasswordAccountIsLookedUpByLoginId() {
        when(query.isAccountDisabled("alice")).thenReturn(true);

        assertTrue(service.isDisabled(UsernamePasswordAuthenticationToken.authenticated("alice", "x", AUTHORITIES)));
        assertFalse(service.isDisabled(UsernamePasswordAuthenticationToken.authenticated("bob", "x", AUTHORITIES)));
    }

    @Test
    void anSsoAccountIsLookedUpByProviderAndSub() {
        when(query.isOidcAccountDisabled("idp", "sub-1")).thenReturn(true);

        assertTrue(service.isDisabled(oidc("sub-1")));
        assertFalse(service.isDisabled(oidc("sub-2")));
    }

    @Test
    void noAuthenticationOrAnAnonymousOneIsNotLookedUp() {
        assertFalse(service.isDisabled(null));
        assertFalse(service.isDisabled(new AnonymousAuthenticationToken("k", "anonymousUser", AUTHORITIES)));
        verify(query, never()).isAccountDisabled("anonymousUser");
    }
}
