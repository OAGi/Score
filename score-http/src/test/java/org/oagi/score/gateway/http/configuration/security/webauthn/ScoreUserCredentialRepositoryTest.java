package org.oagi.score.gateway.http.configuration.security.webauthn;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.webauthn.api.AuthenticatorTransport;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScoreUserCredentialRepositoryTest {

    @Test
    void transportsRoundTrip() {
        Set<AuthenticatorTransport> transports = new LinkedHashSet<>();
        transports.add(AuthenticatorTransport.INTERNAL);
        transports.add(AuthenticatorTransport.HYBRID);

        String joined = ScoreUserCredentialRepository.joinTransports(transports);

        assertEquals("internal,hybrid", joined);
        assertEquals(transports, ScoreUserCredentialRepository.splitTransports(joined));
    }

    @Test
    void noTransportsIsStoredAsNull() {
        assertNull(ScoreUserCredentialRepository.joinTransports(null));
        assertNull(ScoreUserCredentialRepository.joinTransports(Set.of()));
        assertTrue(ScoreUserCredentialRepository.splitTransports(null).isEmpty());
        assertTrue(ScoreUserCredentialRepository.splitTransports(" ").isEmpty());
    }

    @Test
    void anEmptyNameIsStoredAsNull() {
        assertNull(ScoreUserCredentialRepository.labelOrNull(""));
        assertNull(ScoreUserCredentialRepository.labelOrNull("  "));
        assertNull(ScoreUserCredentialRepository.labelOrNull(null));
        assertEquals("Work laptop", ScoreUserCredentialRepository.labelOrNull(" Work laptop "));
    }

    @AfterEach
    void clearRequest() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void userAgentOfTheCurrentRequestIsRecordedAndTrimmed() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("User-Agent", "x".repeat(600));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        assertEquals(512, ScoreUserCredentialRepository.currentUserAgent().length());
    }

    @Test
    void noUserAgentWithoutARequestOrHeader() {
        assertNull(ScoreUserCredentialRepository.currentUserAgent());

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
        assertNull(ScoreUserCredentialRepository.currentUserAgent());
    }

}
