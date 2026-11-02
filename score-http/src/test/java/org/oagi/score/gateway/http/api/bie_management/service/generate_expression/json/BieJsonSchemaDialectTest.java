package org.oagi.score.gateway.http.api.bie_management.service.generate_expression.json;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BieJsonSchemaDialectTest {

    private static final BieJsonSchemaDialect D2020 = Json202012Dialect.INSTANCE;
    private static final BieJsonSchemaDialect D04 = JsonDraft04Dialect.INSTANCE;

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
        BieJsonSchemaDialect next = new Json202012Dialect() {
            @Override
            public String schemaUri() {
                return "https://json-schema.org/draft/next/schema";
            }

            @Override
            public Map<String, Object> asbiep(Map<String, Object> schema,
                                              org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepSummaryRecord asbiep) {
                schema.put("x-next", true);
                return schema;
            }
        };

        assertEquals(schema("type", "object", "x-next", true), next.asbiep(schema("type", "object"), null));
        assertEquals(schema("type", "object"), D2020.asbiep(schema("type", "object"), null));
        assertEquals(Arrays.asList("string", "null"), next.nullable(schema("type", "string"), true).get("type"));
    }

    @Test
    void definitionsAndReferences_followTheVersionKeyword() {
        assertEquals("#/$defs/A", D2020.definitionRef("A"));
        assertEquals("#/definitions/A", D04.definitionRef("A"));
        assertEquals("$id", D2020.idKeyword());
        assertNull(D04.idKeyword());
        assertTrue(D2020.supportsSeparateSchemaFiles());
        assertFalse(D04.supportsSeparateSchemaFiles());
    }

    @Test
    void nullable_2020_usesTypeUnion_orAnyOfForTypelessSchemas() {
        assertEquals(Arrays.asList("string", "null"), D2020.nullable(schema("type", "string"), true).get("type"));

        Map<String, Object> ref = schema("$ref", "#/$defs/X");
        assertEquals(Arrays.asList(ref, schema("type", "null")), D2020.nullable(ref, true).get("anyOf"));

        Map<String, Object> inner = schema("type", "string");
        assertSame(inner, D2020.nullable(inner, false));
    }

    @Test
    void nullable_2020_isIdempotent() {
        Map<String, Object> alreadyUnion = schema("type", new ArrayList<>(Arrays.asList("string", "null")));
        assertEquals(Arrays.asList("string", "null"), D2020.nullable(alreadyUnion, true).get("type"));

        Map<String, Object> collection = schema("type", new ArrayList<>(List.of("string")));
        assertEquals(Arrays.asList("string", "null"), D2020.nullable(collection, true).get("type"));
    }

    @Test
    void nullable_draft04_wrapsInOneOfWithNullBranch_andSplitsTheReferenceIntoAllOf() {
        Map<String, Object> plain = schema("type", "string");
        assertEquals(Arrays.asList(schema("type", "null"), plain), D04.nullable(plain, true).get("oneOf"));
        assertSame(plain, D04.nullable(plain, false));

        Map<String, Object> refWithSiblings = schema("description", "d", "$ref", "#/definitions/X");
        assertEquals(schema("allOf", Arrays.asList(schema("$ref", "#/definitions/X"), schema("description", "d"))),
                D04.nullable(refWithSiblings, false));

        Map<String, Object> bareRef = schema("$ref", "#/definitions/X");
        assertSame(bareRef, D04.nullable(bareRef, false));
    }

    @Test
    void withReference_2020PutsTheReferenceFirst_draft04Last() {
        assertEquals(Arrays.asList("$ref", "description"),
                new ArrayList<>(D2020.withReference(schema("description", "d"), "#/$defs/X").keySet()));
        assertEquals(Arrays.asList("description", "$ref"),
                new ArrayList<>(D04.withReference(schema("description", "d"), "#/definitions/X").keySet()));
    }

    @Test
    void fixedValue_followsTheVersionKeyword() {
        Map<String, Object> v2020 = schema();
        D2020.fixedValue(v2020, "A");
        assertEquals(schema("const", "A"), v2020);

        Map<String, Object> v04 = schema();
        D04.fixedValue(v04, "A");
        assertEquals(schema("enum", List.of("A")), v04);
    }

    @Test
    void adaptToReference_2020NormalizesExamplesToTheDefinitionType_draft04LeavesThemAlone() {
        Map<String, Object> definitions = schema("integer", schema("type", "integer"),
                "boolean", schema("type", "boolean"), "string", schema("type", "string"));

        Map<String, Object> integer = schema("examples", Arrays.asList("12"));
        D2020.adaptToReference(integer, "#/$defs/integer", definitions);
        assertEquals(Arrays.asList(new java.math.BigInteger("12")), integer.get("examples"));

        Map<String, Object> bool = schema("examples", Arrays.asList("TRUE"));
        D2020.adaptToReference(bool, "#/$defs/boolean", definitions);
        assertEquals(Arrays.asList(true), bool.get("examples"));

        Map<String, Object> string = schema("examples", Arrays.asList("12"));
        D2020.adaptToReference(string, "#/$defs/string", definitions);
        assertEquals(Arrays.asList("12"), string.get("examples"));

        Map<String, Object> untouched = schema("examples", Arrays.asList("12"));
        D04.adaptToReference(untouched, "#/definitions/integer", definitions);
        assertEquals(Arrays.asList("12"), untouched.get("examples"));
    }

    @Test
    void scContentCopyKeys_followTheVersionKeyword() {
        assertEquals(List.of("const", "default", "examples"), D2020.scContentCopyKeys());
        assertEquals(List.of("enum", "default", "examples"), D04.scContentCopyKeys());
    }
}
