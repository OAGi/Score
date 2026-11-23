package org.oagi.score.gateway.http.api.oas_management.model;

import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Issue #1791: the schema of an HTTP header is a Schema Object or a Reference Object, a JSON object either way. */
class OasSchemaTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Test
    void aSchemaObjectAndAReferenceObjectAreAccepted() {
        assertFalse(OasSchema.fromJson("{\"type\":\"string\",\"pattern\":\"^a$\"}").isReference());
        assertFalse(OasSchema.fromJson(" {} ").isReference());
        OasSchema reference = OasSchema.fromJson("{\"$ref\":\"#/components/schemas/Trace\"}");
        assertTrue(reference.isReference());
        assertEquals(Map.of("$ref", "#/components/schemas/Trace"), reference.toMap());
    }

    @Test
    void whatIsNotAJsonObjectIsRejected() {
        for (String json : new String[]{"[1]", "\"string\"", "25", "true", "null", "", " ", null, "string",
                "#/components/schemas/Trace", "{\"type\":", "{\"a\":1} garbage", "{\"a\":1}{\"b\":2}"}) {
            assertThrows(IllegalArgumentException.class, () -> OasSchema.fromJson(json), String.valueOf(json));
        }
        assertThrows(IllegalArgumentException.class, () -> OasSchema.of(null));
    }

    @Test
    void aReferenceObjectNeedsANonBlankStringRef() {
        for (String json : new String[]{"{\"$ref\":1}", "{\"$ref\":null}", "{\"$ref\":\"\"}", "{\"$ref\":\" \"}", "{\"$ref\":{}}"}) {
            assertThrows(IllegalArgumentException.class, () -> OasSchema.fromJson(json), json);
        }
    }

    @Test
    void itKeepsTheOrderOfTheKeysAndIsNotChangedFromOutside() {
        OasSchema schema = OasSchema.fromJson("{\"type\":\"string\",\"pattern\":\"^a$\",\"example\":\"a\"}");
        assertEquals(List.of("type", "pattern", "example"), List.copyOf(schema.toMap().keySet()));
        assertEquals("{\"type\":\"string\",\"pattern\":\"^a$\",\"example\":\"a\"}", schema.toJson());
        assertThrows(UnsupportedOperationException.class, () -> schema.toMap().put("x", 1));
        assertEquals(schema, OasSchema.fromJson(schema.toJson()));
    }

    @Test
    void itIsAJsonObjectInTheApi() throws Exception {
        OasDocHttpHeader header = JSON.readValue(
                "{\"name\":\"traceparent\",\"schema\":{\"type\":\"string\",\"pattern\":\"^a$\"},\"example\":\"abc\",\"request\":true}",
                OasDocHttpHeader.class);
        assertEquals(Map.of("type", "string", "pattern", "^a$"), header.getSchema().toMap());
        assertEquals("abc", header.getExample());

        Map<?, ?> written = JSON.readValue(JSON.writeValueAsString(header), Map.class);
        assertEquals(Map.of("type", "string", "pattern", "^a$"), written.get("schema"));
        assertEquals("abc", written.get("example"));
    }

    @Test
    void aSchemaThatIsNotAnObjectIsNotAcceptedFromTheApi() {
        assertThrows(JacksonException.class, () -> JSON.readValue("{\"name\":\"x\",\"schema\":[1]}", OasDocHttpHeader.class));
        assertThrows(JacksonException.class, () -> JSON.readValue("{\"name\":\"x\",\"schema\":\"string\"}", OasDocHttpHeader.class));
        assertThrows(JacksonException.class, () -> JSON.readValue("{\"name\":\"x\",\"schema\":{\"$ref\":1}}", OasDocHttpHeader.class));
    }

    @Test
    void anExampleIsAnyJsonValue() {
        assertNull(OasJson.fromJson(null));
        assertNull(OasJson.fromJson(" "));
        assertEquals("a", OasJson.fromJson(" \"a\" "));
        assertEquals(25, OasJson.fromJson("25"));
        assertEquals(Map.of("a", 1), OasJson.fromJson("{\"a\":1}"));
        assertThrows(IllegalArgumentException.class, () -> OasJson.fromJson("bearer {access_token}"));
        assertNull(OasJson.toJson(null));
        assertEquals("\"a\"", OasJson.toJson("a"));
    }
}
