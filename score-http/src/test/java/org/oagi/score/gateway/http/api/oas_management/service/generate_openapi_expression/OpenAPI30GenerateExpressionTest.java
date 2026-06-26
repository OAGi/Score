package org.oagi.score.gateway.http.api.oas_management.service.generate_openapi_expression;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.oas_management.model.OpenAPIGenerateExpressionOption;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Issue #1610: the 3.0 generator emits Swagger-style shapes and must read its OWN built-in type
 * column (openApi30Map, with the {@code nullable} flag) — never the 3.1 (2020-12) column. Mirrors
 * {@link OpenAPI31GenerateExpressionTest} so a generator wiring swap is caught on both sides.
 */
class OpenAPI30GenerateExpressionTest {

    private static OpenAPI30GenerateExpression mapperReadyGenerator() throws Exception {
        OpenAPI30GenerateExpression gen = new OpenAPI30GenerateExpression(null, new OpenAPIGenerateExpressionOption());
        gen.afterPropertiesSet();
        return gen;
    }

    private static XbtSummaryRecord xbt(String openApi30Map, String openApi31Map) {
        return new XbtSummaryRecord(null, null, null, null, "builtin", "xsd:string",
                null, null, openApi30Map, openApi31Map, null, null);
    }

    @Test
    void toProperties_readsOpenApi30Map_withNullableFlag_notTheTypeUnion() throws Exception {
        OpenAPI30GenerateExpression gen = mapperReadyGenerator();

        XbtSummaryRecord xbt = xbt("{\"type\":\"string\",\"nullable\":true}", "{\"type\":[\"string\",\"null\"]}");
        Map<String, Object> props = gen.toProperties(xbt);

        assertEquals("string", props.get("type"), "3.0 reads its own column: a plain String type, not a union");
        assertEquals(Boolean.TRUE, props.get("nullable"), "3.0 uses the Swagger `nullable` flag");
    }

    @Test
    void emptyExample_isPerXbtType() throws Exception {
        OpenAPI30GenerateExpression gen = mapperReadyGenerator();

        assertEquals("", gen.emptyExample(xbt("{\"type\":\"string\"}", null)));
        assertEquals(false, gen.emptyExample(xbt("{\"type\":\"boolean\"}", null)));
        assertNull(gen.emptyExample(xbt("{\"type\":\"integer\"}", null)));
    }
}
