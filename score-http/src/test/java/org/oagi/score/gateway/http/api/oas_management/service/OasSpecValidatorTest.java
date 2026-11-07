package org.oagi.score.gateway.http.api.oas_management.service;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.oas_management.service.OasSpecValidator.Version;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Issue #1791: the validator itself is right about documents it is known to accept and to refuse. */
class OasSpecValidatorTest {

    static Map<String, Object> map(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            m.put((String) kv[i], kv[i + 1]);
        }
        return m;
    }

    static Map<String, Object> document(Version version, Map<String, Object> operation) {
        return map("openapi", version.number(), "info", map("title", "t", "version", "1"),
                "paths", map("/orders/{id}", map("get", operation)));
    }

    static Map<String, Object> okOperation(Object... parameters) {
        return map("parameters", List.of(parameters),
                "responses", map("200", map("description", "ok")));
    }

    @Test
    void aMinimalDocumentIsValid() {
        for (Version version : Version.values()) {
            assertEquals(List.of(), OasSpecValidator.validate(version, document(version, okOperation(
                    map("name", "id", "in", "path", "required", true, "schema", map("type", "string"))))), version.number());
        }
    }

    @Test
    void thePublishedSchemaRefusesWhatTheSpecificationRefuses() {
        for (Version version : Version.values()) {
            // a path parameter that is not required, a location that does not exist, a parameter with both schema and content
            assertFalse(OasSpecValidator.validate(version, document(version, okOperation(
                    map("name", "id", "in", "path", "required", false, "schema", map("type", "string"))))).isEmpty(), version.number());
            assertFalse(OasSpecValidator.validate(version, document(version, okOperation(
                    map("name", "x", "in", "body", "schema", map("type", "string"))))).isEmpty(), version.number());
            assertFalse(OasSpecValidator.validate(version, document(version, okOperation(
                    map("name", "x", "in", "query", "schema", map("type", "string"),
                            "content", map("text/plain", map("schema", map("type", "string"))))))).isEmpty(), version.number());
        }
    }

    @Test
    void theSchemaObjectIsCheckedPerVersion() {
        Map<String, Object> nullType = map("name", "x", "in", "query", "schema", map("type", List.of("string", "null")));
        assertFalse(OasSpecValidator.validate(Version.V3_0_4, document(Version.V3_0_4, okOperation(nullType))).isEmpty(),
                "3.0 has no type array");
        assertEquals(List.of(), OasSpecValidator.validate(Version.V3_1_2, document(Version.V3_1_2, okOperation(nullType))));

        Map<String, Object> nullable = map("name", "x", "in", "query", "schema", map("type", "string", "nullable", true));
        assertEquals(List.of(), OasSpecValidator.validate(Version.V3_0_4, document(Version.V3_0_4, okOperation(nullable))));
    }

    @Test
    void theVersionIsTakenFromTheMinorVersionOfTheDocument() {
        assertEquals(Version.V3_0_4, Version.of("3.0"));
        assertEquals(Version.V3_0_4, Version.of("3.0.4"));
        assertEquals(Version.V3_1_2, Version.of("3.1"));
        assertEquals(Version.V3_1_2, Version.of("3.1.2"));
        assertEquals(Version.V3_1_2, Version.of(null));
    }
}
