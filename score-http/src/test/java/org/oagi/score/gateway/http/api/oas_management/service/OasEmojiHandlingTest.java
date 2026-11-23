package org.oagi.score.gateway.http.api.oas_management.service;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.oas_management.model.OasDocHttpHeader;
import org.oagi.score.gateway.http.api.oas_management.model.OasDocHttpHeader.Direction;
import org.oagi.score.gateway.http.api.oas_management.model.OasResourcePaths;
import org.oagi.score.gateway.http.api.oas_management.model.OasSchema;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Issue #1892: the text of an OpenAPI Document may hold emoji (two UTF-16 units each). The code that reads, builds
 * and validates names must neither reject them where the specification allows them nor cut one in two.
 */
class OasEmojiHandlingTest {

    private static final String SMILE = "😀";

    private static OasDocHttpHeader parameter(String in, String name) {
        OasDocHttpHeader header = new OasDocHttpHeader();
        header.setName(name);
        header.setIn(in);
        header.setSchema(OasSchema.fromJson("{\"type\":\"string\"}"));
        header.setDirection(Direction.REQUEST);
        return header;
    }

    @Test
    void aParameterNameOfAQueryPathOrCookieMayHoldEmoji() {
        for (String in : List.of("query", "path", "cookie")) {
            OasDocHttpHeader parameter = parameter(in, "id" + SMILE);
            OasDocHttpHeaderPolicy.validate(List.of(parameter));
            assertEquals("id" + SMILE, parameter.getName(), in);
        }
    }

    @Test
    void anHttpHeaderNameMayNotHoldEmoji() {
        // the name of an HTTP header is a token of ASCII characters (RFC 7230)
        assertThrows(IllegalArgumentException.class,
                () -> OasDocHttpHeaderPolicy.validate(List.of(parameter("header", "X-Id" + SMILE))));
    }

    @Test
    void thePathParametersOfAPathAreReadWithEmoji() {
        assertEquals(List.of("id" + SMILE, SMILE), OasResourcePaths.parameterNames("/pets" + SMILE + "/{id" + SMILE + "}/x/{" + SMILE + "}"));
    }

    @Test
    void anOperationIdIsBuiltFromANameWithEmojiAndReadBackWhole() {
        String operationId = OasOperationNaming.buildOperationId("GET", "Pet " + SMILE, false);
        assertTrue(operationId.endsWith("Pet" + SMILE), operationId);
        assertEquals("Pet" + SMILE, OasOperationNaming.extractBieName(operationId, false));
        // an emoji at the start is not cut in two by the capitalization
        assertEquals(SMILE + "pet", OasOperationNaming.operationName(SMILE + "pet"));
    }
}
