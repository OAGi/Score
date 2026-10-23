package org.oagi.score.gateway.http.configuration.security.webauthn;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.webauthn.authentication.WebAuthnAuthenticationFilter;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class PasskeyConfigurationTest {

    @Test
    void passkeySignInStartsANewSessionIdLikeThePasswordLogin() {
        WebAuthnAuthenticationFilter filter = new WebAuthnAuthenticationFilter();
        AuthenticationSuccessHandler successHandler = mock(AuthenticationSuccessHandler.class);
        RememberMeServices rememberMe = mock(RememberMeServices.class);

        PasskeyConfiguration.customizeAuthenticationFilter(
                filter, mock(AuthenticationProvider.class), successHandler, rememberMe);

        // Spring's default is a strategy that does nothing, which keeps the pre-login session ID.
        assertInstanceOf(ChangeSessionIdAuthenticationStrategy.class,
                ReflectionTestUtils.getField(filter, "sessionStrategy"));
        assertSame(successHandler, ReflectionTestUtils.getField(filter, "successHandler"));
        assertSame(rememberMe, filter.getRememberMeServices());
    }

}
