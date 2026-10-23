package org.oagi.score.gateway.http.configuration.security.webauthn;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasskeyRateLimiterTest {

    private final AtomicLong now = new AtomicLong(1_000_000L);
    private final PasskeyRateLimiter limiter = new PasskeyRateLimiter(3, 4, 2, now::get);

    @Test
    void anAddressIsLimitedWithinTheWindowAndFreedAfterIt() {
        for (int i = 0; i < 3; i++) {
            assertTrue(limiter.tryAcquireLookupForIp("10.0.0.1"));
        }
        assertFalse(limiter.tryAcquireLookupForIp("10.0.0.1"));
        assertTrue(limiter.tryAcquireLookupForIp("10.0.0.2"));

        now.addAndGet(PasskeyRateLimiter.WINDOW_MILLIS);
        assertTrue(limiter.tryAcquireLookupForIp("10.0.0.1"));
    }

    @Test
    void lookupsAndSignInOptionsHaveTheirOwnAllowanceAndLimit() {
        // 3 lookups per address, 4 sign-in options per address, and neither spends the other
        for (int i = 0; i < 3; i++) {
            assertTrue(limiter.tryAcquireLookupForIp("10.0.0.1"));
        }
        assertFalse(limiter.tryAcquireLookupForIp("10.0.0.1"));
        for (int i = 0; i < 4; i++) {
            assertTrue(limiter.tryAcquireOptionsForIp("10.0.0.1"), "options " + i);
        }
        assertFalse(limiter.tryAcquireOptionsForIp("10.0.0.1"));
    }

    @Test
    void aLoginIdIsLimitedWhateverCaseOrSpacingIsUsed() {
        assertTrue(limiter.tryAcquireForUsername("alice"));
        assertTrue(limiter.tryAcquireForUsername(" Alice "));
        assertFalse(limiter.tryAcquireForUsername("ALICE"));
        assertTrue(limiter.tryAcquireForUsername("bob"));
    }

    @Test
    void addressAndLoginIdLimitsAreIndependent() {
        assertTrue(limiter.tryAcquireForUsername("alice"));
        assertTrue(limiter.tryAcquireForUsername("alice"));
        assertFalse(limiter.tryAcquireForUsername("alice"));
        assertTrue(limiter.tryAcquireLookupForIp("alice"));
    }

    private MockHttpServletResponse run(String method, String uri, String ip) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        request.setServletPath(uri);
        request.setRemoteAddr(ip);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        new PasskeyRateLimitFilter(limiter, HttpServletRequest::getRemoteAddr).doFilter(request, response, chain);
        if (chain.getRequest() != null) {
            response.setStatus(HttpServletResponse.SC_OK);
        }
        return response;
    }

    @Test
    void theFilterAnswers429ToTheUnauthenticatedEndpointsOnceAnAddressIsOverTheLimit() throws Exception {
        for (int i = 0; i < 3; i++) {
            assertEquals(200, run("POST", "/info/passkey/lookup", "10.0.0.1").getStatus());
        }
        MockHttpServletResponse limited = run("POST", "/info/passkey/lookup", "10.0.0.1");
        assertEquals(429, limited.getStatus());
        assertNotNull(limited.getHeader("Retry-After"));
        assertEquals(200, run("POST", "/info/passkey/lookup", "10.0.0.2").getStatus());

        // the sign-in options are limited on their own, and more generously
        for (int i = 0; i < 4; i++) {
            assertEquals(200, run("POST", "/webauthn/authenticate/options", "10.0.0.1").getStatus(), "options " + i);
        }
        assertEquals(429, run("POST", "/webauthn/authenticate/options", "10.0.0.1").getStatus());
    }

    @Test
    void otherRequestsAreNotCounted() throws Exception {
        for (int i = 0; i < 10; i++) {
            assertEquals(200, run("GET", "/info/passkey", "10.0.0.1").getStatus());
            assertEquals(200, run("POST", "/login/webauthn", "10.0.0.1").getStatus());
        }
        assertNull(run("POST", "/webauthn/authenticate/options", "10.0.0.9").getHeader("Retry-After"));
    }

    @Test
    void theFilterCountsTheAddressItIsGiven() throws Exception {
        PasskeyRateLimitFilter filter = new PasskeyRateLimitFilter(limiter, r -> r.getHeader("X-Test-Client"));
        for (int i = 0; i < 4; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/info/passkey/lookup");
            request.setServletPath("/info/passkey/lookup");
            request.setRemoteAddr("10.0.0.1");
            request.addHeader("X-Test-Client", (i < 3) ? "203.0.113.7" : "203.0.113.8");
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(request, response, new MockFilterChain());
            // three requests from one client use up its allowance; a fourth from another client is still served
            assertEquals(200, response.getStatus(), "request " + i);
        }
        MockHttpServletRequest again = new MockHttpServletRequest("POST", "/info/passkey/lookup");
        again.setServletPath("/info/passkey/lookup");
        again.setRemoteAddr("10.0.0.1");
        again.addHeader("X-Test-Client", "203.0.113.7");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(again, response, new MockFilterChain());
        assertEquals(429, response.getStatus());
    }

}
