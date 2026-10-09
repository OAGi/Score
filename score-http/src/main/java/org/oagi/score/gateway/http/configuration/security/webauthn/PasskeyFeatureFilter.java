package org.oagi.score.gateway.http.configuration.security.webauthn;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.oagi.score.gateway.http.configuration.security.ScoreUserDetails;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.function.BooleanSupplier;

/**
 * Guards every passkey endpoint on the server, so the feature is genuinely off when it is not configured.
 * <p>
 * Spring Security's filter chain is built once at startup, but passkey sign-in is switched on and off at runtime by
 * saving or clearing the settings. This filter evaluates that on each request:
 * <ul>
 *   <li>when passkey sign-in is not configured, the passkey endpoints answer {@code 404};</li>
 *   <li>Spring's built-in {@code DELETE /webauthn/register/{id}} is always {@code 404}; removal goes through the
 *   application's own endpoint, which keeps the "last sign-in method" rule;</li>
 *   <li>registration and management endpoints require a signed-in local account (a {@code ScoreUserDetails}
 *   principal), so an unauthenticated caller gets {@code 401} and an OIDC session gets {@code 403}.</li>
 * </ul>
 * Sign-in endpoints ({@code /webauthn/authenticate/options}, {@code /login/webauthn}) stay open to everyone while
 * the feature is enabled.
 */
public class PasskeyFeatureFilter extends OncePerRequestFilter {

    private static final PathPatternRequestMatcher.Builder MATCHERS = PathPatternRequestMatcher.withDefaults();

    private final RequestMatcher featureEndpoints = new org.springframework.security.web.util.matcher.OrRequestMatcher(
            MATCHERS.matcher("/webauthn/**"),
            MATCHERS.matcher("/login/webauthn"),
            MATCHERS.matcher("/passkeys/**"));

    private final RequestMatcher accountEndpoints = new org.springframework.security.web.util.matcher.OrRequestMatcher(
            MATCHERS.matcher("/webauthn/register/**"),
            MATCHERS.matcher("/passkeys/**"));

    private final RequestMatcher builtInDelete = MATCHERS.matcher(HttpMethod.DELETE, "/webauthn/register/**");

    private final BooleanSupplier enabled;

    public PasskeyFeatureFilter(BooleanSupplier enabled) {
        this.enabled = enabled;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (featureEndpoints.matches(request)) {
            if (builtInDelete.matches(request) || !enabled.getAsBoolean()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            if (accountEndpoints.matches(request)) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication == null || authentication instanceof AnonymousAuthenticationToken
                        || !authentication.isAuthenticated()) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
                if (!(authentication.getPrincipal() instanceof ScoreUserDetails)) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

}
