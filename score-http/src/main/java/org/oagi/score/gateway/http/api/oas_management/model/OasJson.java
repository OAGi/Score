package org.oagi.score.gateway.http.api.oas_management.model;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Issue #1791: the JSON text that is kept in a column ({@code text}) for a value that the API carries as JSON itself.
 */
public final class OasJson {

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

    private OasJson() {
    }

    /** The JSON text of the value; {@code null} for {@code null}. */
    public static String toJson(Object value) {
        return value == null ? null : JSON_MAPPER.writeValueAsString(value);
    }

    /**
     * The value the JSON text holds; {@code null} for a missing or blank text.
     *
     * @throws IllegalArgumentException when the text is not valid JSON
     */
    public static Object fromJson(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return JSON_MAPPER.readValue(json.trim(), Object.class);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Not valid JSON: " + e.getOriginalMessage(), e);
        }
    }
}
