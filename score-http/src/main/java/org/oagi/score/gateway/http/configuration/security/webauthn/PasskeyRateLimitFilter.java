package org.oagi.score.gateway.http.configuration.security.webauthn;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.function.Function;

/**
 * Applies {@link PasskeyRateLimiter}'s per-address limit to the unauthenticated passkey endpoints that are not
 * served by an application controller. The client address comes from {@link ClientAddressResolver}.
 */
public class PasskeyRateLimitFilter extends OncePerRequestFilter {

    private static final PathPatternRequestMatcher.Builder MATCHERS = PathPatternRequestMatcher.withDefaults();

    private final RequestMatcher limited = new OrRequestMatcher(
            MATCHERS.matcher(HttpMethod.POST, "/webauthn/authenticate/options"),
            MATCHERS.matcher(HttpMethod.POST, "/info/passkey/lookup"));

    private final PasskeyRateLimiter limiter;
    private final Function<HttpServletRequest, String> clientAddress;

    public PasskeyRateLimitFilter(PasskeyRateLimiter limiter, Function<HttpServletRequest, String> clientAddress) {
        this.limiter = limiter;
        this.clientAddress = clientAddress;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (limited.matches(request) && !limiter.tryAcquireForIp(clientAddress.apply(request))) {
            response.setHeader("Retry-After", String.valueOf(PasskeyRateLimiter.WINDOW_MILLIS / 1000));
            response.sendError(429);
            return;
        }
        filterChain.doFilter(request, response);
    }

}
