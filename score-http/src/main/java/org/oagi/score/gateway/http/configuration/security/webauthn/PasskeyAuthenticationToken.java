package org.oagi.score.gateway.http.configuration.security.webauthn;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * The authentication produced by a successful passkey sign-in.
 * <p>
 * Spring's own {@code WebAuthnAuthentication} carries a {@code PublicKeyCredentialUserEntity} as the principal,
 * which the application's {@code @AuthenticationPrincipal AuthenticatedPrincipal} endpoints cannot use. This token
 * carries the same {@code ScoreUserDetails} principal a password login produces, so every endpoint behaves
 * identically, and the type tells the success handler and {@code /state} to report {@code "passkey"}.
 */
public class PasskeyAuthenticationToken extends UsernamePasswordAuthenticationToken {

    public PasskeyAuthenticationToken(UserDetails principal, Collection<? extends GrantedAuthority> authorities) {
        super(principal, null, authorities);
    }

}
