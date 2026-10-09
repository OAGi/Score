package org.oagi.score.gateway.http.configuration.security.webauthn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;
import org.springframework.security.web.webauthn.management.WebAuthnRelyingPartyOperations;

/**
 * Beans picked up by Spring Security's {@code webAuthn(...)} configurer. It uses a
 * {@link WebAuthnRelyingPartyOperations} bean when one exists, and builds a fixed one (which insists on an RP ID)
 * otherwise; the bean here lets the application start, with passkeys simply off, when {@code score.passkey.*} is
 * not set.
 */
@Configuration
public class PasskeyConfiguration {

    @Bean
    public WebAuthnRelyingPartyOperations webAuthnRelyingPartyOperations(
            PublicKeyCredentialUserEntityRepository userEntities,
            UserCredentialRepository userCredentials,
            PasskeyProperties passkeyProperties) {
        return new SettingsDrivenRelyingPartyOperations(userEntities, userCredentials, passkeyProperties::current);
    }

    @Bean
    public PasskeyAuthenticationProvider passkeyAuthenticationProvider(
            @Autowired WebAuthnRelyingPartyOperations relyingPartyOperations,
            @Autowired UserDetailsService userDetailsService) {
        return new PasskeyAuthenticationProvider(relyingPartyOperations, userDetailsService);
    }

}
