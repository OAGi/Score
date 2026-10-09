package org.oagi.score.gateway.http.configuration.security.webauthn;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.authentication.WebAuthnAuthenticationRequestToken;
import org.springframework.security.web.webauthn.management.WebAuthnRelyingPartyOperations;
import org.springframework.util.Assert;

/**
 * Verifies a WebAuthn assertion and loads the account through the same {@link UserDetailsService} the password
 * login uses, so roles and account state are identical across sign-in methods.
 */
public class PasskeyAuthenticationProvider implements AuthenticationProvider {

    private final WebAuthnRelyingPartyOperations relyingPartyOperations;
    private final UserDetailsService userDetailsService;

    public PasskeyAuthenticationProvider(WebAuthnRelyingPartyOperations relyingPartyOperations,
                                         UserDetailsService userDetailsService) {
        Assert.notNull(relyingPartyOperations, "relyingPartyOperations cannot be null");
        Assert.notNull(userDetailsService, "userDetailsService cannot be null");
        this.relyingPartyOperations = relyingPartyOperations;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        WebAuthnAuthenticationRequestToken request = (WebAuthnAuthenticationRequestToken) authentication;
        try {
            PublicKeyCredentialUserEntity userEntity =
                    relyingPartyOperations.authenticate(request.getWebAuthnRequest());
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEntity.getName());
            return new PasskeyAuthenticationToken(userDetails, userDetails.getAuthorities());
        } catch (RuntimeException ex) {
            throw new BadCredentialsException(ex.getMessage(), ex);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return WebAuthnAuthenticationRequestToken.class.isAssignableFrom(authentication);
    }

}
