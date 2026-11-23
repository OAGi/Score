package org.oagi.score.gateway.http.api.oas_management.service.generate_openapi_expression;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.oas_management.model.OasDocHttpHeader;
import org.oagi.score.gateway.http.api.oas_management.model.OasOperationId;
import org.oagi.score.gateway.http.api.oas_management.model.OasSchema;
import org.oagi.score.gateway.http.api.oas_management.model.OpenAPITemplateForVerbOption;
import org.oagi.score.gateway.http.api.oas_management.model.Operation;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Issue #1791: the HTTP headers of a document reach the request parameters and every response. */
class HttpHeaderInjectorTest {

    private static final String PATTERN = "^[0-9a-f]{2}-[0-9a-f]{32}-[0-9a-f]{16}-[0-9a-f]{2}$";
    private static final String TRACEPARENT_SCHEMA = "{\"type\":\"string\",\"pattern\":\"" + PATTERN.replace("\\", "\\\\") + "\"}";

    private static OasDocHttpHeader traceparent(BigInteger operationId, OasDocHttpHeader.Direction direction) {
        OasDocHttpHeader header = new OasDocHttpHeader();
        header.setOasOperationId(operationId);
        header.setName("traceparent");
        header.setDescription("Trace context.");
        header.setSchema(OasSchema.fromJson(TRACEPARENT_SCHEMA));
        header.setDirection(direction);
        header.setRequired(direction == OasDocHttpHeader.Direction.RESPONSE);
        return header;
    }

    /** The request parameter and/or the response header, the response header being required. */
    private static List<OasDocHttpHeader> traceparents(BigInteger operationId, boolean request, boolean response) {
        List<OasDocHttpHeader> headers = new ArrayList<>();
        if (request) {
            headers.add(traceparent(operationId, OasDocHttpHeader.Direction.REQUEST));
        }
        if (response) {
            headers.add(traceparent(operationId, OasDocHttpHeader.Direction.RESPONSE));
        }
        return headers;
    }

    private static List<OasDocHttpHeader> concat(List<OasDocHttpHeader>... lists) {
        List<OasDocHttpHeader> all = new ArrayList<>();
        for (List<OasDocHttpHeader> list : lists) {
            all.addAll(list);
        }
        return all;
    }

    private static OpenAPITemplateForVerbOption template(Operation verb, String resource, long operationId) {
        OpenAPITemplateForVerbOption template = new OpenAPITemplateForVerbOption(verb);
        template.setResourceName(resource);
        template.setOasOperationId(new OasOperationId(BigInteger.valueOf(operationId)));
        return template;
    }

    private static Map<String, Object> map(Object... keyValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            map.put((String) keyValues[i], keyValues[i + 1]);
        }
        return map;
    }

    /** A document of GET /orders (200 and a shared 404) and DELETE /orders/{id} (202 only). */
    private static Map<String, Object> document() {
        Map<String, Object> get = map("responses", map(
                "200", map("description", ""),
                "404", map("$ref", "#/components/responses/NotFound")));
        Map<String, Object> delete = map("parameters", List.of(map("name", "id", "in", "path")),
                "responses", map("202", map("description", "Accepted")));
        return map("paths", map("/orders", map("get", get), "/orders/{id}", map("delete", delete)),
                "components", map("responses", map("NotFound", map("description", "Not Found"))));
    }

    private static final List<OpenAPITemplateForVerbOption> TEMPLATES = List.of(
            template(Operation.GET, "/orders", 1), template(Operation.DELETE, "/orders/{id}", 2));

    @SuppressWarnings("unchecked")
    private static Map<String, Object> at(Object node, String... keys) {
        Object current = node;
        for (String key : keys) {
            current = ((Map<String, Object>) current).get(key);
        }
        return (Map<String, Object>) current;
    }

    @Test
    void documentWideHeaderReachesEveryOperationAndResponse() {
        Map<String, Object> root = document();
        HttpHeaderInjector.inject(root, TEMPLATES, TemplatePaths::of, traceparents(null, true, true));

        List<?> parameters = (List<?>) at(root, "paths", "/orders", "get").get("parameters");
        Map<String, Object> parameter = (Map<String, Object>) parameters.get(0);
        assertEquals("traceparent", parameter.get("name"));
        assertEquals("header", parameter.get("in"));
        assertEquals(false, parameter.get("required"));
        assertEquals(PATTERN, at(parameter, "schema").get("pattern"));

        // DELETE keeps its path parameter and gains the header, although it has no BIE response of its own.
        assertEquals(2, ((List<?>) at(root, "paths", "/orders/{id}", "delete").get("parameters")).size());

        Map<String, Object> ok = at(root, "paths", "/orders", "get", "responses", "200");
        assertEquals(true, at(ok, "headers", "traceparent").get("required"));
        assertEquals(PATTERN, at(ok, "headers", "traceparent", "schema").get("pattern"));
        assertNotNull(at(root, "paths", "/orders/{id}", "delete", "responses", "202", "headers", "traceparent"));
    }

    @Test
    void sharedResponseComponentCarriesTheHeaderOnce() {
        Map<String, Object> root = document();
        HttpHeaderInjector.inject(root, TEMPLATES, TemplatePaths::of, traceparents(null, false, true));

        // The operation still refers to the component; the component has the header.
        assertEquals("#/components/responses/NotFound",
                at(root, "paths", "/orders", "get", "responses", "404").get("$ref"));
        assertNotNull(at(root, "components", "responses", "NotFound", "headers", "traceparent"));
        assertNull(at(root, "paths", "/orders", "get").get("parameters"));
    }

    /** The macro of the web app gives every operation a header of its own: the shared responses must stay shared. */
    @Test
    void headersOfEveryOperationLeaveASharedResponseSharedAndCarryTheHeaderInTheComponent() {
        Map<String, Object> root = document();
        // DELETE gets a shared 404 as well, as the generated error responses of every operation do
        at(root, "paths", "/orders/{id}", "delete", "responses").put("404", map("$ref", "#/components/responses/NotFound"));
        HttpHeaderInjector.inject(root, TEMPLATES, TemplatePaths::of,
                concat(traceparents(BigInteger.ONE, true, true), traceparents(BigInteger.TWO, true, true)));

        assertEquals("#/components/responses/NotFound", at(root, "paths", "/orders", "get", "responses", "404").get("$ref"));
        assertEquals("#/components/responses/NotFound", at(root, "paths", "/orders/{id}", "delete", "responses", "404").get("$ref"));
        assertNull(at(root, "paths", "/orders", "get", "responses", "404").get("description"),
                "the shared response is not copied into the operation");
        assertNotNull(at(root, "components", "responses", "NotFound", "headers", "traceparent"));
        // the operations themselves have the parameter and the header on their own responses
        assertNotNull(at(root, "paths", "/orders", "get", "responses", "200", "headers", "traceparent"));
        assertNotNull(at(root, "paths", "/orders/{id}", "delete", "responses", "202", "headers", "traceparent"));
    }

    @Test
    void whenOnlyOneOfTheOperationsThatShareAResponseHasTheHeaderItGetsACopyAndTheSharedOneStaysAsItIs() {
        Map<String, Object> root = document();
        at(root, "paths", "/orders/{id}", "delete", "responses").put("404", map("$ref", "#/components/responses/NotFound"));
        HttpHeaderInjector.inject(root, TEMPLATES, TemplatePaths::of, traceparents(BigInteger.ONE, true, true));

        // GET (operation 1) has its own copy of the 404, with the header
        Map<String, Object> copy = at(root, "paths", "/orders", "get", "responses", "404");
        assertNull(copy.get("$ref"));
        assertEquals("Not Found", copy.get("description"));
        assertNotNull(at(copy, "headers", "traceparent"));
        // DELETE (operation 2) keeps the shared response, which has no header
        assertEquals("#/components/responses/NotFound", at(root, "paths", "/orders/{id}", "delete", "responses", "404").get("$ref"));
        assertNull(at(root, "components", "responses", "NotFound").get("headers"));
    }

    @Test
    void operationsWithDifferentHeadersEachGetTheirOwnCopyOfASharedResponse() {
        OasDocHttpHeader required = traceparent(BigInteger.ONE, OasDocHttpHeader.Direction.RESPONSE);
        OasDocHttpHeader optional = traceparent(BigInteger.TWO, OasDocHttpHeader.Direction.RESPONSE);
        optional.setRequired(false);
        Map<String, Object> root = document();
        at(root, "paths", "/orders/{id}", "delete", "responses").put("404", map("$ref", "#/components/responses/NotFound"));
        HttpHeaderInjector.inject(root, TEMPLATES, TemplatePaths::of, List.of(required, optional));

        assertEquals(true, at(root, "paths", "/orders", "get", "responses", "404", "headers", "traceparent").get("required"));
        assertEquals(false, at(root, "paths", "/orders/{id}", "delete", "responses", "404", "headers", "traceparent").get("required"));
        assertNull(at(root, "components", "responses", "NotFound").get("headers"));
    }

    @Test
    void headerOfOneOperationLeavesTheOthersAlone() {
        Map<String, Object> root = document();
        HttpHeaderInjector.inject(root, TEMPLATES, TemplatePaths::of, traceparents(BigInteger.TWO, true, true));

        assertNull(at(root, "paths", "/orders", "get").get("parameters"));
        assertNull(at(root, "paths", "/orders", "get", "responses", "200").get("headers"));
        assertNotNull(at(root, "paths", "/orders/{id}", "delete", "responses", "202", "headers", "traceparent"));
    }

    @Test
    void headerOfOneOperationOverridesTheDocumentWideOneOfTheSameName() {
        OasDocHttpHeader own = traceparent(BigInteger.TWO, OasDocHttpHeader.Direction.RESPONSE);
        own.setRequired(false);
        Map<String, Object> root = document();
        HttpHeaderInjector.inject(root, TEMPLATES, TemplatePaths::of, concat(traceparents(null, false, true), List.of(own)));

        assertEquals(true, at(root, "paths", "/orders", "get", "responses", "200", "headers", "traceparent").get("required"));
        assertEquals(false, at(root, "paths", "/orders/{id}", "delete", "responses", "202", "headers", "traceparent").get("required"));
    }

    @Test
    void whatWasDefinedTakesThePlaceOfWhatTheOperationAlreadyHasWithTheSameNameAndTheRestStays() {
        Map<String, Object> root = document();
        Map<String, Object> same = map("name", "Traceparent", "in", "header", "required", true);
        Map<String, Object> other = map("name", "Traceparent", "in", "query", "required", true);
        at(root, "paths", "/orders", "get").put("parameters", new ArrayList<>(List.of(same, other)));
        at(root, "paths", "/orders", "get", "responses", "200").put("headers", map("TraceParent", map("description", "x"),
                "X-Other", map("description", "y")));

        HttpHeaderInjector.inject(root, TEMPLATES, TemplatePaths::of, traceparents(null, true, true));

        List<?> parameters = (List<?>) at(root, "paths", "/orders", "get").get("parameters");
        assertEquals(2, parameters.size());
        assertEquals(other, parameters.get(0));
        assertEquals("traceparent", ((Map<?, ?>) parameters.get(1)).get("name"));
        Map<String, Object> headers = at(root, "paths", "/orders", "get", "responses", "200", "headers");
        assertEquals(List.of("X-Other", "traceparent"), List.copyOf(headers.keySet()));
    }

    @Test
    void theExampleOfTheHeaderIsEmittedOnTheParameterAndOnTheHeaderNotInTheSchema() {
        List<OasDocHttpHeader> headers = traceparents(null, true, true);
        headers.forEach(h -> h.setExample("00-4bf92f3577b34da6a3ce929d0e0e4736-a1b2c3d4e5f60718-01"));
        Map<String, Object> root = document();
        HttpHeaderInjector.inject(root, TEMPLATES, TemplatePaths::of, headers);

        Map<String, Object> parameter = (Map<String, Object>) ((List<?>) at(root, "paths", "/orders", "get").get("parameters")).get(0);
        assertEquals("00-4bf92f3577b34da6a3ce929d0e0e4736-a1b2c3d4e5f60718-01", parameter.get("example"));
        assertNull(at(parameter, "schema").get("example"));
        assertEquals("00-4bf92f3577b34da6a3ce929d0e0e4736-a1b2c3d4e5f60718-01",
                at(root, "paths", "/orders", "get", "responses", "200", "headers", "traceparent").get("example"));
    }

    @Test
    void theExampleKeepsItsJsonType() {
        OasDocHttpHeader header = traceparent(null, OasDocHttpHeader.Direction.REQUEST);
        header.setName("x-limit");
        header.setExample(25);
        Map<String, Object> root = document();
        HttpHeaderInjector.inject(root, TEMPLATES, TemplatePaths::of, List.of(header));

        Map<String, Object> parameter = (Map<String, Object>) ((List<?>) at(root, "paths", "/orders", "get").get("parameters")).get(0);
        assertEquals(25, parameter.get("example"));
    }

    @Test
    void withoutAnExampleNoExampleIsEmitted() {
        Map<String, Object> root = document();
        HttpHeaderInjector.inject(root, TEMPLATES, TemplatePaths::of, traceparents(null, true, true));

        Map<String, Object> parameter = (Map<String, Object>) ((List<?>) at(root, "paths", "/orders", "get").get("parameters")).get(0);
        assertFalse(parameter.containsKey("example"));
        assertFalse(at(root, "paths", "/orders", "get", "responses", "200", "headers", "traceparent").containsKey("example"));
    }

    @Test
    void noHeadersChangesNothing() {
        Map<String, Object> root = document();
        HttpHeaderInjector.inject(root, TEMPLATES, TemplatePaths::of, List.of());
        assertEquals(document(), root);
        HttpHeaderInjector.inject(root, TEMPLATES, TemplatePaths::of, null);
        assertEquals(document(), root);
    }

    /** The resolved path key of a template (the tests use the resource name as it is). */
    private static final class TemplatePaths {
        static String of(OpenAPITemplateForVerbOption template) {
            return template.getResourceName();
        }
    }

    private static OasDocHttpHeader parameter(BigInteger operationId, String name, String in) {
        OasDocHttpHeader parameter = new OasDocHttpHeader();
        parameter.setOasOperationId(operationId);
        parameter.setName(name);
        parameter.setDescription("d");
        parameter.setSchema(OasSchema.fromJson("{\"type\":\"string\"}"));
        parameter.setDirection(OasDocHttpHeader.Direction.REQUEST);
        parameter.setIn(in);
        parameter.setRequired("path".equals(in));
        return parameter;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> parametersOf(Map<String, Object> root, String path, String verb) {
        return (List<Map<String, Object>>) at(root, "paths", path, verb).get("parameters");
    }

    @Test
    void aRequestParameterIsGeneratedWhereItIs() {
        Map<String, Object> root = document();
        HttpHeaderInjector.inject(root, TEMPLATES, TemplatePaths::of, List.of(parameter(null, "limit", "query"),
                parameter(null, "session", "cookie"), parameter(null, "x-id", "header")));

        List<Map<String, Object>> parameters = parametersOf(root, "/orders", "get");
        assertEquals(List.of("limit:query", "session:cookie", "x-id:header"),
                parameters.stream().map(p -> p.get("name") + ":" + p.get("in")).toList());
        // and no response header comes out of it
        assertNull(at(root, "paths", "/orders", "get", "responses", "200").get("headers"));
    }

    @Test
    void aQueryAndAHeaderOfOneNameAreTwoParameters() {
        Map<String, Object> root = document();
        HttpHeaderInjector.inject(root, TEMPLATES, TemplatePaths::of,
                List.of(parameter(null, "id", "query"), parameter(null, "id", "header")));

        assertEquals(List.of("id:path", "id:query", "id:header"),
                parametersOf(root, "/orders/{id}", "delete").stream().map(p -> p.get("name") + ":" + p.get("in")).toList());
    }

    @Test
    void aPathParameterReplacesTheGeneratedOneOfTheSameName() {
        Map<String, Object> root = document();
        OasDocHttpHeader id = parameter(null, "id", "path");
        id.setDescription("The order.");
        HttpHeaderInjector.inject(root, TEMPLATES, TemplatePaths::of, List.of(id));

        List<Map<String, Object>> parameters = parametersOf(root, "/orders/{id}", "delete");
        assertEquals(1, parameters.size());
        assertEquals("The order.", parameters.get(0).get("description"));
        assertEquals(true, parameters.get(0).get("required"));
    }

    @Test
    void aPathParameterIsLeftOutWhereThePathHasNoSuchExpression() {
        Map<String, Object> root = document();
        HttpHeaderInjector.inject(root, TEMPLATES, TemplatePaths::of, List.of(parameter(null, "id", "path")));

        // /orders has no {id}: an OpenAPI document whose path parameter is not in the path is invalid
        assertNull(at(root, "paths", "/orders", "get").get("parameters"));
        assertEquals(1, parametersOf(root, "/orders/{id}", "delete").size());
    }

    @Test
    void aParameterOfTheOperationOverridesTheOneOfTheDocumentOfTheSameLocationAndName() {
        Map<String, Object> root = document();
        OasDocHttpHeader wide = parameter(null, "limit", "query");
        OasDocHttpHeader own = parameter(BigInteger.ONE, "limit", "query");
        own.setDescription("own");
        HttpHeaderInjector.inject(root, TEMPLATES, TemplatePaths::of, List.of(wide, own));

        assertEquals("own", parametersOf(root, "/orders", "get").get(0).get("description"));
        assertEquals("d", parametersOf(root, "/orders/{id}", "delete").get(1).get("description"));
    }

    @Test
    void aQueryParameterDoesNotCountAsTheHeaderOfTheSameName() {
        Map<String, Object> root = document();
        at(root, "paths", "/orders", "get").put("parameters",
                new ArrayList<>(List.of(map("name", "traceparent", "in", "query"))));
        HttpHeaderInjector.inject(root, TEMPLATES, TemplatePaths::of, traceparents(null, true, false));

        assertEquals(List.of("traceparent:query", "traceparent:header"),
                parametersOf(root, "/orders", "get").stream().map(p -> p.get("name") + ":" + p.get("in")).toList());
    }

    @Test
    void aCookieIsSerializedWithContentWhichHasTheSchemaAndTheExample() {
        Map<String, Object> root = document();
        OasDocHttpHeader cookie = parameter(null, "session", "cookie");
        cookie.setContentMediaType("text/plain");
        cookie.setExample("abc");
        HttpHeaderInjector.inject(root, TEMPLATES, TemplatePaths::of, List.of(cookie));

        Map<String, Object> generated = parametersOf(root, "/orders", "get").get(0);
        assertEquals("cookie", generated.get("in"));
        assertNull(generated.get("schema"));
        assertNull(generated.get("example"));
        assertNull(generated.get("style"));
        Map<String, Object> mediaType = at(generated, "content", "text/plain");
        assertEquals("string", at(mediaType, "schema").get("type"));
        assertEquals("abc", mediaType.get("example"));
    }

    @Test
    void aQueryParameterHasItsStyleExplodeAndAllowReservedBesideTheSchema() {
        Map<String, Object> root = document();
        OasDocHttpHeader filter = parameter(null, "filter", "query");
        filter.setStyle("deepObject");
        filter.setExplode(true);
        filter.setAllowReserved(true);
        filter.setExample("x");
        HttpHeaderInjector.inject(root, TEMPLATES, TemplatePaths::of, List.of(filter));

        Map<String, Object> generated = parametersOf(root, "/orders", "get").get(0);
        assertEquals("deepObject", generated.get("style"));
        assertEquals(true, generated.get("explode"));
        assertEquals(true, generated.get("allowReserved"));
        assertEquals("x", generated.get("example"));
        assertNotNull(generated.get("schema"));
        assertNull(generated.get("content"));
    }

    @Test
    void whatTheSpecificationDefaultsIsLeftOut() {
        Map<String, Object> root = document();
        HttpHeaderInjector.inject(root, TEMPLATES, TemplatePaths::of, List.of(parameter(null, "limit", "query")));

        Map<String, Object> generated = parametersOf(root, "/orders", "get").get(0);
        assertFalse(generated.containsKey("style"));
        assertFalse(generated.containsKey("explode"));
        assertFalse(generated.containsKey("allowReserved"));
    }

    @Test
    void aResponseHeaderIsSerializedWithContentToo() {
        Map<String, Object> root = document();
        OasDocHttpHeader total = traceparent(null, OasDocHttpHeader.Direction.RESPONSE);
        total.setContentMediaType("text/plain");
        HttpHeaderInjector.inject(root, TEMPLATES, TemplatePaths::of, List.of(total));

        Map<String, Object> header = at(root, "paths", "/orders", "get", "responses", "200", "headers", "traceparent");
        assertNull(header.get("schema"));
        assertEquals(PATTERN, at(header, "content", "text/plain", "schema").get("pattern"));
    }
}
