package org.oagi.score.gateway.http.configuration.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * CORS policy for the main API. Credentialed cross-origin requests are accepted only from the configured origins
 * and from the origin the request itself was addressed to (so a web app served from the same host name, behind a
 * proxy that does not forward the scheme, keeps working). Any other origin gets no CORS headers.
 */
public class ScoreCorsConfigurationSource implements CorsConfigurationSource {

    private final List<String> allowedOrigins;

    public ScoreCorsConfigurationSource(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins == null ? List.of() : List.copyOf(allowedOrigins);
    }

    @Override
    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
        List<String> origins = new ArrayList<>(allowedOrigins);
        String host = request.getHeader("Host");
        if (host != null && !host.isBlank()) {
            origins.add("http://" + host);
            origins.add("https://" + host);
        }

        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.applyPermitDefaultValues();
        corsConfiguration.setAllowedMethods(
                Arrays.asList(
                        HttpMethod.GET.name(), HttpMethod.HEAD.name(),
                        HttpMethod.POST.name(), HttpMethod.PUT.name(),
                        HttpMethod.PATCH.name(), HttpMethod.DELETE.name())
        );
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowedOrigins(origins);
        return corsConfiguration;
    }
}
