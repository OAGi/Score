package org.oagi.score.gateway.http.configuration.security.webauthn;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.configuration.security.ScoreUserDetails;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.ImmutablePublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.authentication.WebAuthnAuthenticationRequestToken;
import org.springframework.security.web.webauthn.management.RelyingPartyAuthenticationRequest;
import org.springframework.security.web.webauthn.management.WebAuthnRelyingPartyOperations;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PasskeyAuthenticationProviderTest {

    private final WebAuthnRelyingPartyOperations operations = mock(WebAuthnRelyingPartyOperations.class);
    private final UserDetailsService userDetailsService = mock(UserDetailsService.class);
    private final PasskeyAuthenticationProvider provider = new PasskeyAuthenticationProvider(operations, userDetailsService);

    private final WebAuthnAuthenticationRequestToken request =
            new WebAuthnAuthenticationRequestToken(mock(RelyingPartyAuthenticationRequest.class));

    @Test
    void returnsTheSameKindOfPrincipalAsAPasswordLogin() {
        when(operations.authenticate(any())).thenReturn(ImmutablePublicKeyCredentialUserEntity.builder()
                .name("alice").id(Bytes.random()).displayName("Alice").build());
        ScoreUserDetails alice = new ScoreUserDetails("alice", "secret", List.of(new SimpleGrantedAuthority("ROLE_DEVELOPER")));
        when(userDetailsService.loadUserByUsername("alice")).thenReturn(alice);

        Authentication result = provider.authenticate(request);

        assertInstanceOf(PasskeyAuthenticationToken.class, result);
        assertTrue(result.isAuthenticated());
        // the application's endpoints take @AuthenticationPrincipal AuthenticatedPrincipal
        assertInstanceOf(AuthenticatedPrincipal.class, result.getPrincipal());
        assertEquals("alice", result.getName());
        assertEquals(Set.copyOf(alice.getAuthorities()), Set.copyOf(result.getAuthorities()));
    }

    @Test
    void aFailedAssertionIsABadCredentialsFailure() {
        when(operations.authenticate(any())).thenThrow(new IllegalArgumentException("bad signature"));
        BadCredentialsException ex = assertThrows(BadCredentialsException.class, () -> provider.authenticate(request));
        assertEquals("bad signature", ex.getMessage());
    }

    @Test
    void anUnknownAccountIsABadCredentialsFailure() {
        when(operations.authenticate(any())).thenReturn(ImmutablePublicKeyCredentialUserEntity.builder()
                .name("ghost").id(Bytes.random()).displayName("Ghost").build());
        when(userDetailsService.loadUserByUsername("ghost")).thenThrow(new UsernameNotFoundException("ghost"));
        assertThrows(BadCredentialsException.class, () -> provider.authenticate(request));
    }

    @Test
    void supportsOnlyWebAuthnRequests() {
        assertTrue(provider.supports(WebAuthnAuthenticationRequestToken.class));
        assertFalse(provider.supports(UsernamePasswordAuthenticationToken.class));
    }

}
