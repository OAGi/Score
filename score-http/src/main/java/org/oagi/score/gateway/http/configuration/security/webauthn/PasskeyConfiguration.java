package org.oagi.score.gateway.http.configuration.security.webauthn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.webauthn.authentication.WebAuthnAuthenticationFilter;
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

    /**
     * Makes Spring's passkey sign-in filter behave like the password login: it keeps {@code ScoreUserDetails} as the
     * principal, answers through the same success handler, remembers the session, and starts a new session ID once
     * signed in. Spring leaves the last one off, so the session that {@code /webauthn/authenticate/options} created
     * before sign-in would otherwise become the signed-in session (session fixation).
     */
    public static void customizeAuthenticationFilter(WebAuthnAuthenticationFilter filter,
                                                     AuthenticationProvider provider,
                                                     AuthenticationSuccessHandler successHandler,
                                                     RememberMeServices rememberMeServices) {
        filter.setAuthenticationManager(new ProviderManager(provider));
        filter.setAuthenticationSuccessHandler(successHandler);
        filter.setRememberMeServices(rememberMeServices);
        filter.setSessionAuthenticationStrategy(new ChangeSessionIdAuthenticationStrategy());
    }

    @Bean
    public PasskeyAuthenticationProvider passkeyAuthenticationProvider(
            @Autowired WebAuthnRelyingPartyOperations relyingPartyOperations,
            @Autowired UserDetailsService userDetailsService) {
        return new PasskeyAuthenticationProvider(relyingPartyOperations, userDetailsService);
    }

}
