package org.oagi.score.gateway.http.api.oas_management.model;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Issue #1791: the HTTP headers of a document in the JSON of the API (what the web app sends and receives). */
class OasDocHttpHeadersJsonTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private static final String HEADERS = "[{\"name\":\"traceparent\",\"description\":\"d\","
            + "\"schema\":{\"type\":\"string\",\"pattern\":\"^a$\"},\"direction\":\"RESPONSE\",\"required\":true}]";

    @Test
    void aDocumentCarriesItsHeadersAsAnArrayOfObjects() throws Exception {
        OasDoc doc = JSON.readValue("{\"title\":\"t\",\"httpHeaders\":" + HEADERS + "}", OasDoc.class);

        assertEquals(1, doc.getHttpHeaders().size());
        OasDocHttpHeader header = doc.getHttpHeaders().get(0);
        assertEquals("traceparent", header.getName());
        assertEquals(Map.of("type", "string", "pattern", "^a$"), header.getSchema().toMap());
        assertEquals(OasDocHttpHeader.Direction.RESPONSE, header.getDirection());
        assertTrue(header.isRequired());
        assertNull(header.getOasOperationId());
        assertNull(header.getExample());
    }

    @Test
    void aDocumentWithoutTheFieldHasNoHeadersAndAnEmptyArrayHasNone() throws Exception {
        assertNull(JSON.readValue("{\"title\":\"t\"}", OasDoc.class).getHttpHeaders());
        assertTrue(JSON.readValue("{\"title\":\"t\",\"httpHeaders\":[]}", OasDoc.class).getHttpHeaders().isEmpty());
    }

    @Test
    void theHeadersAreWrittenAsTheyAreRead() throws Exception {
        OasDoc doc = JSON.readValue("{\"title\":\"t\",\"httpHeaders\":" + HEADERS + "}", OasDoc.class);
        Map<?, ?> written = JSON.readValue(JSON.writeValueAsString(doc), Map.class);

        Map<?, ?> header = (Map<?, ?>) ((java.util.List<?>) written.get("httpHeaders")).get(0);
        assertEquals("traceparent", header.get("name"));
        assertEquals(Map.of("type", "string", "pattern", "^a$"), header.get("schema"));
    }

    @Test
    void anOperationAndAnExampleOfAnyJsonTypeAreCarried() throws Exception {
        OasDoc doc = JSON.readValue("{\"httpHeaders\":[{\"name\":\"x\",\"oasOperationId\":7,\"schema\":{},\"direction\":\"REQUEST\","
                + "\"example\":{\"a\":[1,\"b\"]}}]}", OasDoc.class);
        OasDocHttpHeader header = doc.getHttpHeaders().get(0);

        assertEquals(java.math.BigInteger.valueOf(7), header.getOasOperationId());
        assertEquals(Map.of("a", java.util.List.of(1, "b")), header.getExample());
    }

    @Test
    void theLocationOfARequestParameterIsTheInOfTheJson() throws Exception {
        OasDoc doc = JSON.readValue("{\"httpHeaders\":[{\"name\":\"limit\",\"schema\":{},\"direction\":\"REQUEST\","
                + "\"in\":\"query\"}]}", OasDoc.class);
        assertEquals("query", doc.getHttpHeaders().get(0).getIn());

        Map<?, ?> written = JSON.readValue(JSON.writeValueAsString(doc), Map.class);
        assertEquals("query", ((Map<?, ?>) ((java.util.List<?>) written.get("httpHeaders")).get(0)).get("in"));
    }
}
