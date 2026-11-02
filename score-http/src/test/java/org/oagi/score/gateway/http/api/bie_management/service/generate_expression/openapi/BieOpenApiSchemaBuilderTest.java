package org.oagi.score.gateway.http.api.bie_management.service.generate_expression.openapi;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.AsbieSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.service.generate_expression.GenerationContext;
import org.oagi.score.gateway.http.api.cc_management.model.Cardinality;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    // ---- a nillable ASBIE that reuses another BIE -----------------------------------------------------------------

    private static final String REUSED_REF = "#/components/schemas/Reused";

    /** Builds the property of an ASBIE that reuses the BIE of another top-level ASBIEP, and returns it. */
    private static Map<String, Object> reusedAsbieProperty(BieOpenApiDialect dialect, Boolean nillable, int max) {
        return reusedAsbieProperty(dialect, nillable, max, new LinkedHashMap<>());
    }

    private static Map<String, Object> reusedAsbieProperty(BieOpenApiDialect dialect, Boolean nillable, int max,
                                                           Map<String, Object> schemas) {
        TopLevelAsbiepId owner = new TopLevelAsbiepId(BigInteger.ONE);
        TopLevelAsbiepId reusedOwner = new TopLevelAsbiepId(BigInteger.TWO);

        AsbieSummaryRecord asbie = new AsbieSummaryRecord(null, null, null, null, null, null, null,
                new Cardinality(0, max), null, null, nillable, null, null, null, owner, null, null, null);
        AsbiepSummaryRecord asbiep = mock(AsbiepSummaryRecord.class);
        when(asbiep.ownerTopLevelAsbiepId()).thenReturn(reusedOwner);
        AsccpSummaryRecord asccp = mock(AsccpSummaryRecord.class);
        when(asccp.propertyTerm()).thenReturn("Shipped Item Instance");
        GenerationContext context = mock(GenerationContext.class);
        when(context.queryAssocToASBIEP(asbie)).thenReturn(asbiep);
        when(context.queryBasedASCCP(asbiep)).thenReturn(asccp);

        // The reused BIE has a schema already, so only the association is under test.
        schemas.put("Reused", new LinkedHashMap<>());
        Map<TopLevelAsbiepId, String> reusedNames = new LinkedHashMap<>();
        reusedNames.put(reusedOwner, "Reused");
        BieOpenApiSchemaBuilder builder = new BieOpenApiSchemaBuilder(context, dialect,
                BieOpenApiSchemaPolicy.STANDARD, true, null, null, schemas, reusedNames);

        Map<String, Object> parent = new LinkedHashMap<>();
        parent.put("properties", new LinkedHashMap<String, Object>());
        builder.fillProperties(parent, asbie);
        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) parent.get("properties");
        @SuppressWarnings("unchecked")
        Map<String, Object> property = (Map<String, Object>) properties.get("shippedItemInstance");
        return property;
    }

    private static Map<String, Object> ref() {
        Map<String, Object> ref = new LinkedHashMap<>();
        ref.put("$ref", REUSED_REF);
        return ref;
    }

    @Test
    void reusedAsbie_nillable_v30_admitsNullNextToTheReference() {
        Map<String, Object> nullOnly = new LinkedHashMap<>();
        nullOnly.put("type", "object");
        nullOnly.put("nullable", true);
        nullOnly.put("enum", Collections.singletonList(null));

        Map<String, Object> property = reusedAsbieProperty(BieOpenApi30Dialect.INSTANCE, true, 1);

        assertEquals(Arrays.asList(nullOnly, ref()), property.get("oneOf"));
        assertFalse(property.containsKey("$ref"));
    }

    @Test
    void reusedAsbie_nillable_v31_admitsNullNextToTheReference() {
        Map<String, Object> nullType = new LinkedHashMap<>();
        nullType.put("type", "null");

        Map<String, Object> property = reusedAsbieProperty(BieOpenApi31Dialect.INSTANCE, true, 1);

        assertEquals(Arrays.asList(ref(), nullType), property.get("anyOf"));
        assertFalse(property.containsKey("$ref"));
    }

    @Test
    void reusedAsbie_notNillable_isJustTheReference() {
        for (BieOpenApiDialect dialect : new BieOpenApiDialect[]{BieOpenApi30Dialect.INSTANCE, BieOpenApi31Dialect.INSTANCE}) {
            assertEquals(ref(), reusedAsbieProperty(dialect, false, 1));
            assertEquals(ref(), reusedAsbieProperty(dialect, null, 1));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void reusedAsbie_nillable_array_keepsTheNullableAlternativeOnTheItems() {
        Map<String, Object> schemas = new LinkedHashMap<>();
        Map<String, Object> property = reusedAsbieProperty(BieOpenApi30Dialect.INSTANCE, true, -1, schemas);

        // An unbounded array of a reused BIE is a shared '<name>List' schema; its items admit null.
        assertEquals(Collections.singletonMap("$ref", "#/components/schemas/ReusedList"), property);
        Map<String, Object> list = (Map<String, Object>) schemas.get("ReusedList");
        assertEquals("array", list.get("type"));
        List<Object> alternatives = (List<Object>) ((Map<String, Object>) list.get("items")).get("oneOf");
        assertEquals(ref(), alternatives.get(1));
    }
}
