package org.oagi.score.gateway.http.configuration.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Refuses (403) every request of a signed-in account that has been disabled, however its session was created: by
 * password, passkey or single sign-on, and also when the session was opened before the account was disabled. The flag
 * is read from the database on each request, as a session keeps the value of the sign-in.
 * <p>
 * The paths a disabled account still needs are let through, so that the web app can show the "Account is disabled"
 * page and the user can leave or sign in again as another account. The paths are given by the configuration, which
 * leaves out the paths that are open on the chain but work for the signed-in user.
 */
public class DisabledAccountFilter extends OncePerRequestFilter {

    private final SessionService sessionService;
    private final List<RequestMatcher> exempt;

    /** @param exemptPaths the path patterns that a disabled account may still reach */
    public DisabledAccountFilter(SessionService sessionService, Collection<String> exemptPaths) {
        this.sessionService = sessionService;
        this.exempt = exemptPaths.stream()
                .map(path -> (RequestMatcher) PathPatternRequestMatcher.withDefaults().matcher(path))
                .toList();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return exempt.stream().anyMatch(matcher -> matcher.matches(request));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (sessionService.isDisabled(SecurityContextHolder.getContext().getAuthentication())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "The account is disabled.");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
