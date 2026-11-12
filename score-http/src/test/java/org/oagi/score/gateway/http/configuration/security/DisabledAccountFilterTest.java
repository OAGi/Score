package org.oagi.score.gateway.http.configuration.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** A request of a disabled account is refused, except the paths the account needs to see why and to leave. */
class DisabledAccountFilterTest {

    private final SessionService sessionService = mock(SessionService.class);
    private final DisabledAccountFilter filter = new DisabledAccountFilter(sessionService,
            List.of("/state", "/logout", "/login", "/info/**", "/oauth2/**"));
    private final FilterChain chain = mock(FilterChain.class);

    @BeforeEach
    void signIn() {
        SecurityContextHolder.setContext(new SecurityContextImpl(UsernamePasswordAuthenticationToken.authenticated(
                "alice", "x", List.of(new SimpleGrantedAuthority("ROLE_END_USER")))));
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private MockHttpServletResponse run(String uri) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        request.setRequestURI(uri);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, chain);
        return response;
    }

    @Test
    void anEnabledAccountPasses() throws Exception {
        when(sessionService.isDisabled(any())).thenReturn(false);

        assertEquals(200, run("/code-lists").getStatus());
        verify(chain).doFilter(any(), any());
    }

    @Test
    void aDisabledAccountIsRefusedBeforeTheEndpoint() throws Exception {
        when(sessionService.isDisabled(any())).thenReturn(true);

        assertEquals(403, run("/code-lists").getStatus());
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void aDisabledAccountCanStillReachTheStateTheSignOutAndSignInPathsAndThePublicPaths() throws Exception {
        when(sessionService.isDisabled(any())).thenReturn(true);

        for (String path : List.of("/state", "/logout", "/oauth2/logout", "/login", "/info/webpages",
                "/oauth2/authorization/idp")) {
            assertEquals(200, run(path).getStatus(), path);
        }
    }
}
