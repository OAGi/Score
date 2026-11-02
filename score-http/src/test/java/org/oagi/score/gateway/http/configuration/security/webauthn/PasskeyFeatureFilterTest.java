package org.oagi.score.gateway.http.configuration.security.webauthn;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.configuration.security.ScoreUserDetails;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PasskeyFeatureFilterTest {

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    /**
     * @return 200 when the filter let the request through to the rest of the chain, otherwise the status it sent
     */
    private static int run(boolean enabled, String method, String uri) throws Exception {
        return run(enabled, method, uri, null);
    }

    private static int run(boolean enabled, String method, String uri, String origin) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        request.setServletPath(uri);
        if (origin != null) {
            request.addHeader("Origin", origin);
        }
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        new PasskeyFeatureFilter(() -> enabled, () -> List.of("https://score.example.org", "http://localhost:4200")).doFilter(request, response, chain);
        return (chain.getRequest() != null) ? HttpServletResponse.SC_OK : response.getStatus();
    }

    private static void signInLocal() {
        ScoreUserDetails user = new ScoreUserDetails("alice", "x", List.of(new SimpleGrantedAuthority("ROLE_END_USER")));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    }

    @Test
    void passkeyEndpointsAre404WhileNotConfigured() throws Exception {
        for (String uri : List.of("/webauthn/authenticate/options", "/login/webauthn", "/webauthn/register/options",
                "/passkeys", "/passkeys/abc")) {
            assertEquals(404, run(false, "POST", uri), uri);
        }
    }

    @Test
    void otherEndpointsAreUntouchedWhileNotConfigured() throws Exception {
        for (String uri : List.of("/login", "/state", "/passkey-admin/impact", "/info/passkey", "/application/settings")) {
            assertEquals(200, run(false, "GET", uri), uri);
        }
    }

    @Test
    void signInEndpointsAreOpenWhenConfigured() throws Exception {
        assertEquals(200, run(true, "POST", "/webauthn/authenticate/options"));
        assertEquals(200, run(true, "POST", "/login/webauthn"));
    }

    @Test
    void builtInCredentialDeleteIsAlways404() throws Exception {
        signInLocal();
        assertEquals(404, run(true, "DELETE", "/webauthn/register/abc"));
    }

    @Test
    void accountEndpointsNeedASignedInLocalAccount() throws Exception {
        assertEquals(401, run(true, "POST", "/webauthn/register/options"));
        assertEquals(401, run(true, "GET", "/passkeys"));

        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
                "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
        assertEquals(401, run(true, "GET", "/passkeys"));
    }

    @Test
    void oidcSessionsCannotUseAccountEndpoints() throws Exception {
        DefaultOAuth2User oauth2User = new DefaultOAuth2User(
                AuthorityUtils.createAuthorityList("ROLE_USER"), Map.of("sub", "123"), "sub");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(oauth2User, null, oauth2User.getAuthorities()));
        assertEquals(403, run(true, "GET", "/passkeys"));
        assertEquals(403, run(true, "POST", "/webauthn/register/options"));
    }

    @Test
    void localAccountReachesAccountEndpointsWhenConfigured() throws Exception {
        signInLocal();
        assertEquals(200, run(true, "GET", "/passkeys"));
        assertEquals(200, run(true, "POST", "/webauthn/register/options"));
        assertEquals(200, run(true, "POST", "/webauthn/register"));
    }

    @Test
    void stateChangingAccountRequestsFromAnotherOriginAreRefused() throws Exception {
        signInLocal();
        for (String origin : List.of("https://evil.score.example.org", "null")) {
            assertEquals(403, run(true, "POST", "/webauthn/register/options", origin));
            assertEquals(403, run(true, "POST", "/webauthn/register", origin));
            assertEquals(403, run(true, "PATCH", "/passkeys/abc", origin));
            // Spring's own DELETE /webauthn/register/{id} is switched off altogether
            assertEquals(403, run(true, "DELETE", "/passkeys/abc", origin));
        }
    }

    @Test
    void stateChangingAccountRequestsFromAnAllowedOriginPass() throws Exception {
        signInLocal();
        assertEquals(200, run(true, "POST", "/webauthn/register/options", "https://score.example.org"));
        assertEquals(200, run(true, "POST", "/webauthn/register", "HTTPS://Score.Example.org/"));
        assertEquals(200, run(true, "PATCH", "/passkeys/abc", "http://localhost:4200"));
    }

    @Test
    void readsAndRequestsWithoutAnOriginAreNotRefusedForTheirOrigin() throws Exception {
        signInLocal();
        assertEquals(200, run(true, "GET", "/passkeys", "https://evil.example.com"));
        assertEquals(200, run(true, "POST", "/webauthn/register/options"));
    }

    @Test
    void signInEndpointsDoNotCheckTheOrigin() throws Exception {
        // the assertion itself is bound to the allowed origins by the WebAuthn verification
        assertEquals(200, run(true, "POST", "/login/webauthn", "https://evil.example.com"));
    }

}
