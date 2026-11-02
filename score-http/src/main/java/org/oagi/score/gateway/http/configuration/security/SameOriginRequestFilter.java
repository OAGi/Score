package org.oagi.score.gateway.http.configuration.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;
import java.util.Set;

/**
 * CSRF defence for the main chain, which does not use CSRF tokens. A state-changing request that carries an
 * {@code Origin} header (or, without one, a {@code Referer}) is rejected with 403 unless that origin is one of the
 * origins {@link ScoreCorsConfigurationSource} allows. A browser always sends {@code Origin} on a cross-site POST and
 * a page cannot forge it, so this stops form posts to {@code /login} and to the endpoints that take no JSON body,
 * including from sibling subdomains that {@code SameSite} does not separate. A request with neither header does not
 * come from a page and is let through.
 */
public class SameOriginRequestFilter extends OncePerRequestFilter {

    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");

    private final ScoreCorsConfigurationSource allowedOrigins;

    public SameOriginRequestFilter(ScoreCorsConfigurationSource allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!SAFE_METHODS.contains(request.getMethod()) && !fromAllowedOrigin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean fromAllowedOrigin(HttpServletRequest request) {
        String origin = request.getHeader(HttpHeaders.ORIGIN);
        if (origin == null) {
            origin = originOf(request.getHeader(HttpHeaders.REFERER));
            if (origin == null) {
                return !request.getHeaders(HttpHeaders.REFERER).hasMoreElements();
            }
        }
        String normalized = stripTrailingSlash(origin.trim());
        return allowedOrigins.allowedOrigins(request).stream()
                .anyMatch(e -> stripTrailingSlash(e.trim()).equalsIgnoreCase(normalized));
    }

    /** The scheme://authority of a Referer, or null when it is absent or not an absolute URL. */
    private static String originOf(String referer) {
        if (referer == null || referer.isBlank()) {
            return null;
        }
        try {
            URI uri = URI.create(referer.trim());
            if (uri.getScheme() == null || uri.getRawAuthority() == null) {
                return null;
            }
            return uri.getScheme() + "://" + uri.getRawAuthority();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static String stripTrailingSlash(String origin) {
        return origin.endsWith("/") ? origin.substring(0, origin.length() - 1) : origin;
    }
}
