package org.oagi.score.gateway.http.api.application_management.service;

import org.oagi.score.gateway.http.api.application_management.model.PasskeySettingsInfo;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Validates and normalizes {@link PasskeySettingsInfo}.
 * <p>
 * Saving every field empty is the "disable" action and is valid. Otherwise an RP ID and at least one allowed
 * origin are required, and the RP ID must equal, or be a parent domain of, the host of every allowed origin
 * (the browser enforces this binding, so a mismatch could never work).
 */
public final class PasskeySettingsValidator {

    private static final Pattern HOST_PATTERN = Pattern.compile(
            "^(?=.{1,253}$)([a-z0-9]([a-z0-9-]{0,61}[a-z0-9])?)(\\.[a-z0-9]([a-z0-9-]{0,61}[a-z0-9])?)*$");

    private PasskeySettingsValidator() {
    }

    /**
     * @return a normalized copy (trimmed, RP ID lower-cased, origins without a trailing slash, duplicates removed)
     * @throws IllegalArgumentException when the settings are partially filled or inconsistent
     */
    public static PasskeySettingsInfo validateAndNormalize(PasskeySettingsInfo input) {
        PasskeySettingsInfo normalized = new PasskeySettingsInfo();
        String rpId = trim(input.getRpId()).toLowerCase(Locale.ROOT);
        String rpName = trim(input.getRpName());
        List<String> origins = new ArrayList<>();
        if (input.getAllowedOrigins() != null) {
            for (String origin : input.getAllowedOrigins()) {
                String trimmed = trim(origin);
                if (!trimmed.isEmpty()) {
                    String normalizedOrigin = stripTrailingSlash(trimmed);
                    if (!origins.contains(normalizedOrigin)) {
                        origins.add(normalizedOrigin);
                    }
                }
            }
        }

        if (rpId.isEmpty() && rpName.isEmpty() && origins.isEmpty()) {
            return normalized;
        }
        if (rpId.isEmpty()) {
            throw new IllegalArgumentException("The RP ID is required.");
        }
        if (origins.isEmpty()) {
            throw new IllegalArgumentException("At least one allowed origin is required.");
        }
        if (!HOST_PATTERN.matcher(rpId).matches()) {
            throw new IllegalArgumentException(
                    "The RP ID must be a bare domain name, such as example.org (no scheme, port, or path).");
        }

        for (String origin : origins) {
            String host = originHost(origin);
            if (!host.equals(rpId) && !host.endsWith("." + rpId)) {
                throw new IllegalArgumentException(
                        "The origin '" + origin + "' is not under the RP ID '" + rpId + "'.");
            }
        }

        normalized.setRpId(rpId);
        normalized.setRpName(rpName.isEmpty() ? null : rpName);
        normalized.setAllowedOrigins(origins);
        return normalized;
    }

    private static String originHost(String origin) {
        URI uri;
        try {
            uri = new URI(origin);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("The origin '" + origin + "' is not a valid URL.");
        }
        String scheme = uri.getScheme();
        String host = uri.getHost();
        if (scheme == null || host == null || uri.getUserInfo() != null
                || (uri.getRawPath() != null && !uri.getRawPath().isEmpty())
                || uri.getRawQuery() != null || uri.getRawFragment() != null) {
            throw new IllegalArgumentException(
                    "The origin '" + origin + "' must look like https://example.org (scheme, host, optional port).");
        }
        host = host.toLowerCase(Locale.ROOT);
        boolean secure = "https".equalsIgnoreCase(scheme);
        boolean localhost = "localhost".equals(host) || host.endsWith(".localhost");
        if (!secure && !("http".equalsIgnoreCase(scheme) && localhost)) {
            throw new IllegalArgumentException(
                    "The origin '" + origin + "' must use https (http is only allowed for localhost).");
        }
        return host;
    }

    private static String stripTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private static String trim(String value) {
        return (value == null) ? "" : value.trim();
    }

}
