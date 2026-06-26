package org.oagi.score.gateway.http.api.oas_management.service.generate_openapi_expression;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.oas_management.model.OpenAPIGenerateExpressionOption;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Issue #1610: unit tests for the OpenAPI 3.1 (JSON Schema 2020-12) keyword helpers — type-union
 * nullability ({@code [T,"null"]}), the {@code anyOf {"type":"null"}} fallback for ref/allOf schemas,
 * the plural {@code examples} array, and the built-in type body sourced from {@code openApi31Map()}.
 */
class OpenAPI31GenerateExpressionTest {

    /** The ctor stores-but-does-not-deref its args; applyNillableTypeUnion never touches the mapper. */
    private static OpenAPI31GenerateExpression bareGenerator() {
        return new OpenAPI31GenerateExpression(null, null);
    }

    /** toProperties/emptyExample need the JSON mapper that afterPropertiesSet() builds; the option defaults to YAML. */
    private static OpenAPI31GenerateExpression mapperReadyGenerator() throws Exception {
        OpenAPI31GenerateExpression gen = new OpenAPI31GenerateExpression(null, new OpenAPIGenerateExpressionOption());
        gen.afterPropertiesSet();
        return gen;
    }

    private static XbtSummaryRecord xbt(String openApi30Map, String openApi31Map) {
        return new XbtSummaryRecord(null, null, null, null, "builtin", "xsd:string",
                null, null, openApi30Map, openApi31Map, null, null);
    }

    // --------------------------------------------------------------- B4: positive nullability (type union)

    @Test
    void applyNillableTypeUnion_stringType_becomesTypeUnionWithNull_andIsIdempotent() {
        OpenAPI31GenerateExpression gen = bareGenerator();

        Map<String, Object> stringType = new LinkedHashMap<>();
        stringType.put("type", "string");
        assertEquals(Arrays.asList("string", "null"), gen.applyNillableTypeUnion(stringType, true).get("type"));
        // 3.1 nullability must never emit the 3.0 `nullable` flag
        assertFalse(gen.applyNillableTypeUnion(stringType, true).containsKey("nullable"));

        // not nillable -> left as the plain String type
        Map<String, Object> notNillable = new LinkedHashMap<>();
        notNillable.put("type", "string");
        assertEquals("string", gen.applyNillableTypeUnion(notNillable, false).get("type"));

        // already a union containing null -> no duplicate null appended (idempotent)
        Map<String, Object> alreadyUnion = new LinkedHashMap<>();
        alreadyUnion.put("type", new ArrayList<>(Arrays.asList("string", "null")));
        assertEquals(Arrays.asList("string", "null"), gen.applyNillableTypeUnion(alreadyUnion, true).get("type"));
    }

    // --------------------------------------------------------------- B5: anyOf null branch for ref/allOf

    @Test
    void applyNillableTypeUnion_refSchemaWithoutType_wrapsInAnyOfWithNullBranch() {
        OpenAPI31GenerateExpression gen = bareGenerator();

        Map<String, Object> refSchema = new LinkedHashMap<>();
        refSchema.put("allOf", Arrays.asList(Collections.singletonMap("$ref", "#/components/schemas/Foo")));

        Map<String, Object> out = gen.applyNillableTypeUnion(refSchema, true);
        assertTrue(out.containsKey("anyOf"), "a typeless (ref/allOf) schema wraps in anyOf");
        List<?> anyOf = (List<?>) out.get("anyOf");
        assertEquals(2, anyOf.size());
        assertSame(refSchema, anyOf.get(0), "the original schema is the first anyOf branch");
        assertEquals("null", ((Map<?, ?>) anyOf.get(1)).get("type"), "the second branch is {type: null}");

        // a Collection type without null -> appends null (not wrapped in anyOf)
        Map<String, Object> collType = new LinkedHashMap<>();
        collType.put("type", new ArrayList<>(Arrays.asList("string")));
        assertEquals(Arrays.asList("string", "null"), gen.applyNillableTypeUnion(collType, true).get("type"));
    }

    // --------------------------------------------------------------- B9: examples array + emptyExample per type

    @Test
    void buildExamples_wrapsExampleIntoArray_andEmptyExampleIsPerXbtType() throws Exception {
        OpenAPI31GenerateExpression gen = mapperReadyGenerator();

        // a provided example is wrapped into the (plural) examples array
        assertEquals(List.of("hello"), gen.buildExamples(xbt(null, "{\"type\":\"string\"}"), "hello"));

        // empty example: string -> "", boolean -> false, integer -> null (single-element list)
        assertEquals(List.of(""), gen.buildExamples(xbt(null, "{\"type\":\"string\"}"), ""));
        assertEquals(List.of(false), gen.buildExamples(xbt(null, "{\"type\":\"boolean\"}"), ""));
        List<Object> integerExamples = gen.buildExamples(xbt(null, "{\"type\":\"integer\"}"), "");
        assertEquals(1, integerExamples.size());
        assertNull(integerExamples.get(0), "the integer empty example is null");
    }

    // --------------------------------------------------------------- B11: built-in body from openApi31Map

    @Test
    void toProperties_readsOpenApi31Map_notOpenApi30Map() throws Exception {
        OpenAPI31GenerateExpression gen = mapperReadyGenerator();

        XbtSummaryRecord xbt = xbt("{\"type\":\"string\",\"nullable\":true}", "{\"type\":[\"string\",\"null\"]}");
        Map<String, Object> props = gen.toProperties(xbt);

        assertEquals(Arrays.asList("string", "null"), props.get("type"), "3.1 reads its own 2020-12 column");
        assertFalse(props.containsKey("nullable"), "the 3.0 `nullable` flag must not leak into 3.1");
    }
}
