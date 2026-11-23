package org.oagi.score.gateway.http.api.oas_management.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Issue #1791: the {@code schema} of a Parameter Object or of a Header Object: a Schema Object, which is a JSON object.
 * OpenAPI 3.0 also allows a Reference Object ({@code {"$ref": "#/components/schemas/X"}}) in its place; from 3.1 on, a
 * {@code $ref} is a keyword of the Schema Object, which may have other keywords beside it. A JSON object is all this class
 * holds on to: the keywords of a Schema Object are not fixed, and which of them an OpenAPI version knows is up to the
 * client. It is a JSON object in the API, and its JSON text in the database ({@code schema_type_reference}).
 */
public final class OasSchema {

    private static final String REF = "$ref";

    private final Map<String, Object> value;

    private OasSchema(Map<String, Object> value) {
        this.value = value;
    }

    /**
     * @throws IllegalArgumentException when the value is not a JSON object, or has a {@code $ref} that is not
     *                                  a non-blank string
     */
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static OasSchema of(Map<String, Object> value) {
        if (value == null) {
            throw new IllegalArgumentException("The schema must be a JSON object.");
        }
        if (value.containsKey(REF) && !(value.get(REF) instanceof String ref && !ref.isBlank())) {
            throw new IllegalArgumentException("The $ref of a schema must be a non-blank string.");
        }
        return new OasSchema(Collections.unmodifiableMap(new LinkedHashMap<>(value)));
    }

    /** @throws IllegalArgumentException when the text is not the JSON text of a JSON object that {@link #of} accepts */
    @SuppressWarnings("unchecked")
    public static OasSchema fromJson(String json) {
        Object parsed = OasJson.fromJson(json);
        if (!(parsed instanceof Map)) {
            throw new IllegalArgumentException("The schema must be a JSON object.");
        }
        return of((Map<String, Object>) parsed);
    }

    public String toJson() {
        return OasJson.toJson(value);
    }

    /** The JSON object. */
    @JsonValue
    public Map<String, Object> toMap() {
        return value;
    }

    public boolean isReference() {
        return value.containsKey(REF);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof OasSchema schema && value.equals(schema.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return toJson();
    }
}
