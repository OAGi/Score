package org.oagi.score.gateway.http.api.bie_management.service.generate_expression.openapi;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Issue #1610: a builder reads the built-in type body of the column of its own dialect, never the other one's.
 */
class BieOpenApiSchemaBuilderTest {

    private static BieOpenApiSchemaBuilder builder(BieOpenApiDialect dialect) {
        return new BieOpenApiSchemaBuilder(null, dialect, BieOpenApiSchemaPolicy.STANDARD, true, null, null,
                new LinkedHashMap<>(), new LinkedHashMap<>());
    }

    private static XbtSummaryRecord xbt(String openApi30Map, String openApi31Map) {
        return new XbtSummaryRecord(null, null, null, null, "builtin", "xsd:string",
                null, null, openApi30Map, openApi31Map, null, null);
    }

    @Test
    void toProperties_v30_readsOpenApi30Map_withNullableFlag_notTheTypeUnion() {
        XbtSummaryRecord xbt = xbt("{\"type\":\"string\",\"nullable\":true}", "{\"type\":[\"string\",\"null\"]}");
        Map<String, Object> props = builder(BieOpenApi30Dialect.INSTANCE).toProperties(xbt);

        assertEquals("string", props.get("type"), "3.0 reads its own column: a plain String type, not a union");
        assertEquals(Boolean.TRUE, props.get("nullable"), "3.0 uses the Swagger `nullable` flag");
    }

    @Test
    void toProperties_v31_readsOpenApi31Map_notOpenApi30Map() {
        XbtSummaryRecord xbt = xbt("{\"type\":\"string\",\"nullable\":true}", "{\"type\":[\"string\",\"null\"]}");
        Map<String, Object> props = builder(BieOpenApi31Dialect.INSTANCE).toProperties(xbt);

        assertEquals(Arrays.asList("string", "null"), props.get("type"), "3.1 reads its own 2020-12 column");
        assertFalse(props.containsKey("nullable"), "the 3.0 `nullable` flag must not leak into 3.1");
    }

    @Test
    void emptyExample_isPerXbtType() {
        for (BieOpenApiDialect dialect : new BieOpenApiDialect[]{BieOpenApi30Dialect.INSTANCE, BieOpenApi31Dialect.INSTANCE}) {
            BieOpenApiSchemaBuilder builder = builder(dialect);
            XbtSummaryRecord string = xbt("{\"type\":\"string\"}", "{\"type\":\"string\"}");
            XbtSummaryRecord bool = xbt("{\"type\":\"boolean\"}", "{\"type\":\"boolean\"}");
            XbtSummaryRecord integer = xbt("{\"type\":\"integer\"}", "{\"type\":\"integer\"}");

            assertEquals("", builder.emptyExample(string));
            assertEquals(false, builder.emptyExample(bool));
            assertNull(builder.emptyExample(integer), "the integer empty example is null");
        }
    }
}
