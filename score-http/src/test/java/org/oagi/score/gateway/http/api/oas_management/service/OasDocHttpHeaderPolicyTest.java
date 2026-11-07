package org.oagi.score.gateway.http.api.oas_management.service;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.oas_management.model.OasDocHttpHeader;
import org.oagi.score.gateway.http.api.oas_management.model.OasDocHttpHeader.Direction;
import org.oagi.score.gateway.http.api.oas_management.model.OasSchema;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Issue #1791: what an HTTP header of a document may be. */
class OasDocHttpHeaderPolicyTest {

    private static OasDocHttpHeader header(String name, String schema, Direction direction) {
        OasDocHttpHeader header = new OasDocHttpHeader();
        header.setName(name);
        header.setSchema(schema == null ? null : OasSchema.fromJson(schema));
        header.setDirection(direction);
        return header;
    }

    @Test
    void validHeadersPassAndNamesAreTrimmed() {
        OasDocHttpHeader header = header(" traceparent ", "{\"type\":\"string\"}", Direction.REQUEST);
        OasDocHttpHeaderPolicy.validate(List.of(header));
        assertEquals("traceparent", header.getName());
        OasDocHttpHeaderPolicy.validate(null);
    }

    @Test
    void rejectsABadName() {
        assertThrows(IllegalArgumentException.class,
                () -> OasDocHttpHeaderPolicy.validate(List.of(header("trace parent", "{}", Direction.REQUEST))));
        assertThrows(IllegalArgumentException.class,
                () -> OasDocHttpHeaderPolicy.validate(List.of(header(" ", "{}", Direction.REQUEST))));
    }

    @Test
    void rejectsAHeaderWithoutADirection() {
        assertThrows(IllegalArgumentException.class,
                () -> OasDocHttpHeaderPolicy.validate(List.of(header("traceparent", "{}", null))));
    }

    @Test
    void rejectsAHeaderWithoutASchema() {
        assertThrows(IllegalArgumentException.class,
                () -> OasDocHttpHeaderPolicy.validate(List.of(header("traceparent", null, Direction.REQUEST))));
    }

    @Test
    void rejectsTheSameHeaderTwiceForOneScopeAndDirection() {
        assertThrows(IllegalArgumentException.class, () -> OasDocHttpHeaderPolicy.validate(List.of(
                header("traceparent", "{}", Direction.REQUEST), header("TraceParent", "{}", Direction.REQUEST))));
        // another direction, or another operation, is a different use
        OasDocHttpHeader own = header("traceparent", "{}", Direction.REQUEST);
        own.setOasOperationId(BigInteger.ONE);
        OasDocHttpHeaderPolicy.validate(List.of(header("traceparent", "{}", Direction.REQUEST), own,
                header("traceparent", "{}", Direction.RESPONSE)));
    }

    private static OasDocHttpHeader request(String name, String in) {
        OasDocHttpHeader header = header(name, "{}", Direction.REQUEST);
        header.setIn(in);
        return header;
    }

    @Test
    void aRequestParameterIsInAHeaderUnlessItSaysOtherwise() {
        OasDocHttpHeader none = request("traceparent", null);
        OasDocHttpHeaderPolicy.validate(List.of(none));
        assertEquals("header", none.getIn());
        for (String in : List.of("query", "header", "path", "cookie")) {
            OasDocHttpHeaderPolicy.validate(List.of(request("x", in)));
        }
        assertThrows(IllegalArgumentException.class, () -> OasDocHttpHeaderPolicy.validate(List.of(request("x", "body"))));
        assertThrows(IllegalArgumentException.class, () -> OasDocHttpHeaderPolicy.validate(List.of(request("x", "Query"))));
    }

    @Test
    void aResponseHeaderHasNoLocation() {
        OasDocHttpHeader response = header("traceparent", "{}", Direction.RESPONSE);
        response.setIn("query");
        OasDocHttpHeaderPolicy.validate(List.of(response));
        assertNull(response.getIn());
    }

    @Test
    void onlyTheNameOfAHeaderIsATokenAndTheNameOfAQueryParameterOrACookieIsFree() {
        assertThrows(IllegalArgumentException.class, () -> OasDocHttpHeaderPolicy.validate(List.of(request("a b", "header"))));
        OasDocHttpHeaderPolicy.validate(List.of(request("filter[name]", "query"), request("session id", "cookie")));
        assertThrows(IllegalArgumentException.class, () -> OasDocHttpHeaderPolicy.validate(List.of(request(" ", "query"))));
    }

    @Test
    void aPathParameterIsRequiredAndNamedAfterAnExpressionOfThePath() {
        OasDocHttpHeader id = request("id", "path");
        id.setRequired(false);
        OasDocHttpHeaderPolicy.validate(List.of(id));
        assertTrue(id.isRequired());
        for (String bad : List.of("{id}", "a/b", "a b")) {
            assertThrows(IllegalArgumentException.class, () -> OasDocHttpHeaderPolicy.validate(List.of(request(bad, "path"))));
        }
    }

    @Test
    void refusesTheHeadersOpenApiIgnores() {
        for (String name : List.of("Accept", "content-type", "Authorization")) {
            assertThrows(IllegalArgumentException.class, () -> OasDocHttpHeaderPolicy.validate(List.of(request(name, "header"))));
            // a query parameter of that name is a parameter like any other
            OasDocHttpHeaderPolicy.validate(List.of(request(name, "query")));
        }
        assertThrows(IllegalArgumentException.class,
                () -> OasDocHttpHeaderPolicy.validate(List.of(header("Content-Type", "{}", Direction.RESPONSE))));
        // Accept is only ignored as a request parameter
        OasDocHttpHeaderPolicy.validate(List.of(header("Accept", "{}", Direction.RESPONSE)));
    }

    @Test
    void parametersAreToldApartByLocationAndNameAndOnlyAHeaderNameIgnoresCase() {
        OasDocHttpHeaderPolicy.validate(List.of(request("id", "query"), request("id", "cookie"), request("id", "header"),
                request("Id", "query")));
        assertThrows(IllegalArgumentException.class, () -> OasDocHttpHeaderPolicy.validate(List.of(
                request("id", "query"), request("id", "query"))));
        assertThrows(IllegalArgumentException.class, () -> OasDocHttpHeaderPolicy.validate(List.of(
                request("X-Id", "header"), request("x-id", null))));
    }

    @Test
    void aValueIsSerializedWithContentOrWithAStyleNotWithBoth() {
        OasDocHttpHeader cookie = request("session", "cookie");
        cookie.setContentMediaType(" text/plain ");
        OasDocHttpHeaderPolicy.validate(List.of(cookie));
        assertEquals("text/plain", cookie.getContentMediaType());

        for (java.util.function.Consumer<OasDocHttpHeader> serialization : List.<java.util.function.Consumer<OasDocHttpHeader>>of(
                h -> h.setStyle("form"), h -> h.setExplode(true), h -> h.setAllowReserved(true))) {
            OasDocHttpHeader both = request("q", "query");
            both.setContentMediaType("text/plain");
            serialization.accept(both);
            assertThrows(IllegalArgumentException.class, () -> OasDocHttpHeaderPolicy.validate(List.of(both)));
        }
    }

    @Test
    void aBlankMediaTypeIsNoContentAndAnInvalidOneIsRefused() {
        OasDocHttpHeader blank = request("q", "query");
        blank.setContentMediaType("  ");
        OasDocHttpHeaderPolicy.validate(List.of(blank));
        assertNull(blank.getContentMediaType());

        for (String bad : List.of("text", "text/", "/plain", "text plain", "text/plain; charset=utf-8")) {
            OasDocHttpHeader h = request("q", "query");
            h.setContentMediaType(bad);
            assertThrows(IllegalArgumentException.class, () -> OasDocHttpHeaderPolicy.validate(List.of(h)), bad);
        }
        OasDocHttpHeader json = request("filter", "query");
        json.setContentMediaType("application/vnd.api+json");
        OasDocHttpHeaderPolicy.validate(List.of(json));
    }

    @Test
    void theStylesAreThoseOfTheLocation() {
        String[][] allowed = {{"query", "form"}, {"query", "spaceDelimited"}, {"query", "pipeDelimited"}, {"query", "deepObject"},
                {"path", "matrix"}, {"path", "label"}, {"path", "simple"}, {"header", "simple"}, {"cookie", "form"}};
        for (String[] pair : allowed) {
            OasDocHttpHeader h = request("x", pair[0]);
            h.setStyle(pair[1]);
            h.setExplode("deepObject".equals(pair[1]) ? Boolean.TRUE : null);
            OasDocHttpHeaderPolicy.validate(List.of(h));
        }
        String[][] refused = {{"cookie", "simple"}, {"header", "form"}, {"path", "form"}, {"query", "simple"}, {"query", "matrix"}};
        for (String[] pair : refused) {
            OasDocHttpHeader h = request("x", pair[0]);
            h.setStyle(pair[1]);
            assertThrows(IllegalArgumentException.class, () -> OasDocHttpHeaderPolicy.validate(List.of(h)), pair[0] + " " + pair[1]);
        }
    }

    @Test
    void aResponseHeaderHasTheStyleSimpleOnly() {
        OasDocHttpHeader simple = header("x-total", "{}", Direction.RESPONSE);
        simple.setStyle("simple");
        simple.setExplode(false);
        OasDocHttpHeaderPolicy.validate(List.of(simple));
        OasDocHttpHeader form = header("x-total", "{}", Direction.RESPONSE);
        form.setStyle("form");
        assertThrows(IllegalArgumentException.class, () -> OasDocHttpHeaderPolicy.validate(List.of(form)));
    }

    @Test
    void deepObjectNeedsExplodeAndAllowReservedIsForTheQuery() {
        OasDocHttpHeader deep = request("filter", "query");
        deep.setStyle("deepObject");
        deep.setExplode(false);
        assertThrows(IllegalArgumentException.class, () -> OasDocHttpHeaderPolicy.validate(List.of(deep)));
        deep.setExplode(null); // the default of deepObject is no explode
        assertThrows(IllegalArgumentException.class, () -> OasDocHttpHeaderPolicy.validate(List.of(deep)));
        deep.setExplode(true);
        OasDocHttpHeaderPolicy.validate(List.of(deep));

        OasDocHttpHeader reserved = request("q", "query");
        reserved.setAllowReserved(true);
        OasDocHttpHeaderPolicy.validate(List.of(reserved));
        for (String in : List.of("path", "header", "cookie")) {
            OasDocHttpHeader h = request("x", in);
            h.setAllowReserved(true);
            assertThrows(IllegalArgumentException.class, () -> OasDocHttpHeaderPolicy.validate(List.of(h)), in);
        }
    }

    @Test
    void theMessageOfTheSpecificationNamesWhatIsWrongInTheHeaderNotInTheDocumentItWasCheckedIn() {
        OasDocHttpHeader h = header("x-trace", "{\"type\":[\"string\",\"null\"]}", Direction.REQUEST);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> OasDocHttpHeaderPolicy.validate(List.of(h), "3.0"));
        assertTrue(e.getMessage().startsWith("The request parameter 'x-trace' is not valid in OpenAPI 3.0.4: schema.type: "), e.getMessage());
        assertFalse(e.getMessage().contains("/paths") || e.getMessage().contains("one and only one"), e.getMessage());

        OasDocHttpHeader response = header("x-trace", "{\"type\":\"string\",\"minLength\":-1}", Direction.RESPONSE);
        e = assertThrows(IllegalArgumentException.class, () -> OasDocHttpHeaderPolicy.validate(List.of(response), "3.1"));
        assertTrue(e.getMessage().contains("response header 'x-trace'") && e.getMessage().contains("schema.minLength"), e.getMessage());

        OasDocHttpHeader sibling = header("x-trace", "{\"$ref\":\"#/components/schemas/X\",\"description\":\"d\"}", Direction.REQUEST);
        e = assertThrows(IllegalArgumentException.class, () -> OasDocHttpHeaderPolicy.validate(List.of(sibling), "3.0"));
        assertTrue(e.getMessage().contains("schema: a Reference Object ($ref) cannot have other fields") && e.getMessage().contains("'description'"), e.getMessage());
    }

    @Test
    void violationsSaysEveryHeaderThatIsWrong_andValidateSaysTheFirst() {
        OasDocHttpHeader noDirection = new OasDocHttpHeader();
        noDirection.setName("a");
        OasDocHttpHeader noSchema = new OasDocHttpHeader();
        noSchema.setName("b");
        noSchema.setDirection(OasDocHttpHeader.Direction.REQUEST);

        var violations = OasDocHttpHeaderPolicy.violations(java.util.List.of(noDirection, noSchema), null);

        assertEquals(2, violations.size());
        assertEquals(violations.get(0).message(), assertThrows(IllegalArgumentException.class,
                () -> OasDocHttpHeaderPolicy.validate(java.util.List.of(noDirection, noSchema))).getMessage());
    }
}

