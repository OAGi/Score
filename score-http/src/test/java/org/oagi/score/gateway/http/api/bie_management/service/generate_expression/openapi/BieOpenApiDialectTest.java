package org.oagi.score.gateway.http.api.bie_management.service.generate_expression.openapi;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BieOpenApiDialectTest {

    private static final BieOpenApiDialect V30 = BieOpenApi30Dialect.INSTANCE;
    private static final BieOpenApiDialect V31 = BieOpenApi31Dialect.INSTANCE;

    private static Map<String, Object> schema(Object... kv) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            map.put((String) kv[i], kv[i + 1]);
        }
        return map;
    }

    /** A future version overrides only the hooks it expresses differently. */
    @Test
    void aVersionOverridesOnlyTheNodeHooksItChanges() {
        BieOpenApiDialect v32 = new BieOpenApi31Dialect() {
            @Override
            public String openApiVersion() {
                return "3.2.0";
            }

            @Override
            public Map<String, Object> asbiep(Map<String, Object> schema,
                                              org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepSummaryRecord asbiep) {
                schema.put("x-v32", true);
                return schema;
            }
        };

        Map<String, Object> schema = schema("type", "object");
        assertEquals(schema("type", "object", "x-v32", true), v32.asbiep(schema, null));
        assertEquals(schema("type", "object"), V31.asbiep(schema("type", "object"), null));
        assertEquals(Arrays.asList("string", "null"), v32.nullable(schema("type", "string"), true).get("type"));
    }

    @Test
    void pinsCanonicalPatchVersion() {
        assertEquals("3.0.4", V30.openApiVersion());
        assertEquals("3.1.2", V31.openApiVersion());
    }

    @Test
    void deleteRequestBody_isAllowedFrom31Only() {
        assertFalse(V30.allowsDeleteRequestBody());
        assertTrue(V31.allowsDeleteRequestBody());
    }

    @Test
    void nullable_v30_wrapsInOneOfWithNullableBranch() {
        Map<String, Object> inner = schema("type", "string");
        Map<String, Object> result = V30.nullable(inner, true);

        assertEquals(Arrays.asList(schema("nullable", true), inner), result.get("oneOf"));
        assertSame(inner, V30.nullable(inner, false));
    }

    @Test
    void nullable_v31_usesTypeUnion_orAnyOfForTypelessSchemas() {
        Map<String, Object> typed = V31.nullable(schema("type", "string"), true);
        assertEquals(Arrays.asList("string", "null"), typed.get("type"));

        Map<String, Object> ref = schema("$ref", "#/components/schemas/X");
        Map<String, Object> typeless = V31.nullable(ref, true);
        assertEquals(Arrays.asList(ref, schema("type", "null")), typeless.get("anyOf"));
    }

    @Test
    void nullable_v31_isIdempotent_andNeverEmitsTheNullableFlag() {
        Map<String, Object> string = schema("type", "string");
        assertEquals(Arrays.asList("string", "null"), V31.nullable(string, true).get("type"));
        assertFalse(V31.nullable(string, true).containsKey("nullable"));

        assertEquals("string", V31.nullable(schema("type", "string"), false).get("type"));

        Map<String, Object> alreadyUnion = schema("type", new ArrayList<>(Arrays.asList("string", "null")));
        assertEquals(Arrays.asList("string", "null"), V31.nullable(alreadyUnion, true).get("type"));

        Map<String, Object> collection = schema("type", new ArrayList<>(List.of("string")));
        assertEquals(Arrays.asList("string", "null"), V31.nullable(collection, true).get("type"));
    }

    @Test
    void fixedValueAndExample_followTheVersionKeyword() {
        Map<String, Object> v30 = schema();
        V30.fixedValue(v30, "A");
        V30.example(v30, "x");
        assertEquals(schema("enum", List.of("A"), "example", "x"), v30);

        Map<String, Object> v31 = schema();
        V31.fixedValue(v31, "A");
        V31.example(v31, "x");
        assertEquals(schema("const", "A", "examples", List.of("x")), v31);
    }

    @Test
    void scContentCopyKeys_followTheVersionKeyword() {
        assertEquals(List.of("description", "enum", "default", "example"), V30.scContentCopyKeys());
        assertEquals(List.of("description", "const", "default", "examples"), V31.scContentCopyKeys());
    }
}
