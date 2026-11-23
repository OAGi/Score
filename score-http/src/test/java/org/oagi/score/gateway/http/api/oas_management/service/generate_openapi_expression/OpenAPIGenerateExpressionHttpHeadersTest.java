package org.oagi.score.gateway.http.api.oas_management.service.generate_openapi_expression;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.bie_management.service.generate_expression.openapi.BieOpenApi30Dialect;
import org.oagi.score.gateway.http.api.bie_management.service.generate_expression.openapi.BieOpenApi31Dialect;
import org.oagi.score.gateway.http.api.bie_management.service.generate_expression.openapi.BieOpenApiDialect;
import org.oagi.score.gateway.http.api.oas_management.model.*;
import org.oagi.score.gateway.http.api.oas_management.model.OasDocHttpHeader.Direction;
import tools.jackson.databind.json.JsonMapper;

import org.oagi.score.gateway.http.api.oas_management.service.OasSpecValidator;

import java.io.File;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Issue #1791: the generator merges the HTTP headers of the document after the error responses, so that every
 * operation, and every response of it, carries them. Documents of operations without a BIE are generated here, which
 * needs no database.
 */
class OpenAPIGenerateExpressionHttpHeadersTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private static OasDocHttpHeader traceparent(Direction direction) {
        OasDocHttpHeader header = new OasDocHttpHeader();
        header.setName("traceparent");
        header.setDescription("Trace context.");
        header.setSchema(OasSchema.fromJson("{\"type\":\"string\",\"pattern\":\"^a$\"}"));
        header.setDirection(direction);
        header.setRequired(direction == Direction.RESPONSE);
        return header;
    }

    private static List<OasDocHttpHeader> traceparents() {
        return List.of(traceparent(Direction.REQUEST), traceparent(Direction.RESPONSE));
    }

    private static OpenAPITemplateForVerbOption template(Operation verb, String path, String errorBodyType, long id) {
        OpenAPITemplateForVerbOption template = new OpenAPITemplateForVerbOption(verb);
        template.setResourceName(path);
        template.setOperationId(verb.name().toLowerCase() + "Order");
        template.setOasOperationId(new OasOperationId(BigInteger.valueOf(id)));
        template.setErrorResponseBodyType(errorBodyType);
        return template;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> generate(BieOpenApiDialect dialect, List<OasDocHttpHeader> headers,
                                                String errorBodyType) throws Exception {
        OasDoc oasDoc = new OasDoc();
        oasDoc.setTitle("t");
        oasDoc.setVersion("1.0");
        oasDoc.setOpenAPIVersion(dialect.openApiVersion());
        oasDoc.setHttpHeaders(headers);

        OpenAPIGenerateExpressionOption option = new OpenAPIGenerateExpressionOption();
        option.setOasDoc(oasDoc);
        option.setOpenAPIExpressionFormat(OpenAPIExpressionFormat.JSON);
        option.addTemplate(template(Operation.DELETE, "/orders/{id}", errorBodyType, 1));
        option.addTemplate(template(Operation.PATCH, "/orders/{id}", errorBodyType, 2));

        OpenAPIGenerateExpression expression = new OpenAPIGenerateExpression(null, option, dialect);
        expression.afterPropertiesSet();
        for (OpenAPITemplateForVerbOption template : option.getTemplates()) {
            expression.generate(template);
        }
        // the same steps, in the same order, as OpenAPIGenerateService
        expression.generateErrorResponses();
        expression.generateHttpHeaders();

        File file = expression.asFile("headers-test");
        try {
            return JSON.readValue(file, Map.class);
        } finally {
            file.delete();
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> at(Object node, String... keys) {
        Object current = node;
        for (String key : keys) {
            current = ((Map<String, Object>) current).get(key);
        }
        return (Map<String, Object>) current;
    }

    @Test
    void everyOperationAndEveryResponseCarriesTheHeaderInBothVersions() throws Exception {
        for (BieOpenApiDialect dialect : List.of(BieOpenApi30Dialect.INSTANCE, BieOpenApi31Dialect.INSTANCE)) {
            Map<String, Object> doc = generate(dialect, traceparents(), "NONE");

            for (String verb : List.of("delete", "patch")) {
                Map<String, Object> operation = at(doc, "paths", "/orders/{id}", verb);
                List<?> parameters = (List<?>) operation.get("parameters");
                assertEquals(1, parameters.stream().filter(p -> "traceparent".equals(((Map<?, ?>) p).get("name"))).count(),
                        dialect.openApiVersion() + " " + verb);
                // the generator makes no path parameter: it is an entry of the document, like any other parameter
                assertEquals(0, parameters.stream().filter(p -> "path".equals(((Map<?, ?>) p).get("in"))).count());

                Map<String, Object> responses = at(operation, "responses");
                assertTrue(responses.size() > 1, "a success response and the error responses");
                for (Map.Entry<String, Object> response : responses.entrySet()) {
                    assertEquals(true, at(response.getValue(), "headers", "traceparent").get("required"),
                            dialect.openApiVersion() + " " + verb + " " + response.getKey());
                    assertEquals("^a$", at(response.getValue(), "headers", "traceparent", "schema").get("pattern"));
                }
            }
        }
    }

    @Test
    void theHeaderReachesTheErrorResponsesBecauseItIsMergedAfterThem() throws Exception {
        Map<String, Object> doc = generate(BieOpenApi31Dialect.INSTANCE, traceparents(), "NONE");
        Map<String, Object> responses = at(doc, "paths", "/orders/{id}", "patch", "responses");

        assertTrue(responses.containsKey("404"), "the error responses are generated");
        assertNotNull(at(responses, "404", "headers", "traceparent"));
    }

    @Test
    void sharedProblemDetailsResponsesCarryTheHeaderInTheComponents() throws Exception {
        Map<String, Object> doc = generate(BieOpenApi31Dialect.INSTANCE, traceparents(), "PROBLEM_DETAILS");

        Map<String, Object> responses = at(doc, "paths", "/orders/{id}", "patch", "responses");
        assertEquals("#/components/responses/404_NotFound", at(responses, "404").get("$ref"));
        assertNotNull(at(doc, "components", "responses", "404_NotFound", "headers", "traceparent"));
        // the success response is its own and has the header inline
        assertNotNull(at(responses, "204", "headers", "traceparent"));
    }

    @Test
    void withoutHeadersTheDocumentIsTheSameAsBefore() throws Exception {
        Map<String, Object> none = generate(BieOpenApi30Dialect.INSTANCE, null, "NONE");
        Map<String, Object> empty = generate(BieOpenApi30Dialect.INSTANCE, List.of(), "NONE");

        assertEquals(none, empty);
        assertFalse(JSON.writeValueAsString(none).contains("traceparent"));
    }

    // ---- every verb and status of Add Operation, in the real generator ---------------------------------------------

    private static final List<Operation> VERBS = List.of(Operation.GET, Operation.POST, Operation.PUT, Operation.PATCH, Operation.DELETE);
    private static final List<Integer> STATUSES = List.of(200, 201, 202, 204);

    private static OasDocHttpHeader header(Direction direction, String name, String in, String schema, java.util.function.Consumer<OasDocHttpHeader> change) {
        OasDocHttpHeader header = new OasDocHttpHeader();
        header.setDirection(direction);
        header.setName(name);
        header.setIn(in);
        header.setDescription("The " + name + ".");
        header.setSchema(OasSchema.fromJson(schema));
        change.accept(header);
        return header;
    }

    /** One of every kind of header a user can give. */
    private static List<OasDocHttpHeader> everyKind() {
        String array = "{\"type\":\"array\",\"items\":{\"type\":\"string\"}}";
        String object = "{\"type\":\"object\",\"properties\":{\"a\":{\"type\":\"string\"}}}";
        return List.of(
                header(Direction.REQUEST, "traceparent", "header", "{\"type\":\"string\",\"pattern\":\"^a$\"}", h -> h.setExample("a")),
                header(Direction.REQUEST, "tags", "query", array, h -> { h.setStyle("form"); h.setExplode(true); }),
                header(Direction.REQUEST, "filter", "query", object, h -> { h.setStyle("deepObject"); h.setExplode(true); h.setRequired(true); }),
                header(Direction.REQUEST, "q", "query", "{\"type\":\"string\"}", h -> h.setAllowReserved(true)),
                header(Direction.REQUEST, "session", "cookie", "{\"type\":\"string\"}", h -> h.setContentMediaType("text/plain")),
                header(Direction.REQUEST, "x-json", "header", object, h -> { h.setContentMediaType("application/json"); h.setExample(Map.of("a", "b")); }),
                header(Direction.RESPONSE, "traceparent", null, "{\"type\":\"string\",\"pattern\":\"^a$\"}", h -> { h.setRequired(true); h.setExample("a"); }),
                header(Direction.RESPONSE, "x-list", null, array, h -> { h.setStyle("simple"); h.setExplode(true); }),
                header(Direction.RESPONSE, "x-text", null, "{\"type\":\"string\"}", h -> h.setContentMediaType("text/plain")));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> generateEveryVerb(BieOpenApiDialect dialect, List<OasDocHttpHeader> headers,
                                                         String errorBodyType) throws Exception {
        return JSON.readValue(generateEveryVerbAs(dialect, headers, errorBodyType, OpenAPIExpressionFormat.JSON), Map.class);
    }

    private static String generateEveryVerbAs(BieOpenApiDialect dialect, List<OasDocHttpHeader> headers,
                                              String errorBodyType, OpenAPIExpressionFormat format) throws Exception {
        OasDoc oasDoc = new OasDoc();
        oasDoc.setTitle("t");
        oasDoc.setVersion("1.0");
        oasDoc.setOpenAPIVersion(dialect.openApiVersion());
        oasDoc.setHttpHeaders(headers);

        OpenAPIGenerateExpressionOption option = new OpenAPIGenerateExpressionOption();
        option.setOasDoc(oasDoc);
        option.setOpenAPIExpressionFormat(format);
        long id = 0;
        for (Operation verb : VERBS) {
            for (int status : STATUSES) {
                OpenAPITemplateForVerbOption template = template(verb, "/" + verb.name().toLowerCase() + "-" + status + "/{id}", errorBodyType, ++id);
                template.setHttpStatusCode(status);
                option.addTemplate(template);
            }
        }
        OpenAPIGenerateExpression expression = new OpenAPIGenerateExpression(null, option, dialect);
        expression.afterPropertiesSet();
        for (OpenAPITemplateForVerbOption template : option.getTemplates()) {
            expression.generate(template);
        }
        expression.generateErrorResponses();
        expression.generateHttpHeaders();
        File file = expression.asFile("headers-test");
        try {
            return java.nio.file.Files.readString(file.toPath());
        } finally {
            file.delete();
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void everyVerbAndStatusOfAnOperationWithoutABieGeneratesAValidDocumentInBothVersions() throws Exception {
        for (BieOpenApiDialect dialect : List.of(BieOpenApi30Dialect.INSTANCE, BieOpenApi31Dialect.INSTANCE)) {
            OasSpecValidator.Version version = OasSpecValidator.Version.of(dialect.openApiVersion());
            for (String errorBodyType : List.of("NONE", "PROBLEM_DETAILS")) {
                Map<String, Object> baseline = generateEveryVerb(dialect, null, errorBodyType);
                Map<String, Object> document = generateEveryVerb(dialect, everyKind(), errorBodyType);
                String where = dialect.openApiVersion() + " " + errorBodyType;

                assertEquals(List.of(), OasSpecValidator.validate(version, baseline), where + " baseline");
                assertEquals(List.of(), OasSpecValidator.validate(version, document), where);

                Map<String, Object> paths = (Map<String, Object>) document.get("paths");
                assertEquals(VERBS.size() * STATUSES.size(), paths.size());
                for (Map.Entry<String, Object> path : paths.entrySet()) {
                    Map<String, Object> operation = (Map<String, Object>) ((Map<String, Object>) path.getValue()).values().stream().filter(v -> v instanceof Map).findFirst().orElseThrow();
                    List<Map<String, Object>> parameters = (List<Map<String, Object>>) operation.get("parameters");
                    // the six of the user: the generator makes no parameter, the path parameters are entries of the document
                    assertEquals(6, parameters.size(), where + " " + path.getKey());
                    assertTrue(parameters.stream().noneMatch(p -> "path".equals(p.get("in"))));
                    for (Map.Entry<String, Object> response : ((Map<String, Object>) operation.get("responses")).entrySet()) {
                        Map<String, Object> target = (Map<String, Object>) response.getValue();
                        if (target.get("$ref") instanceof String ref) {
                            target = (Map<String, Object>) ((Map<String, Object>) ((Map<String, Object>) document.get("components"))
                                    .get("responses")).get(ref.substring(ref.lastIndexOf('/') + 1));
                        }
                        assertEquals(java.util.Set.of("traceparent", "x-list", "x-text"),
                                ((Map<String, Object>) target.get("headers")).keySet(), where + " " + path.getKey() + " " + response.getKey());
                    }
                }
                // nothing but the headers was added
                Map<String, Object> restored = OasJson.fromJson(OasJson.toJson(document)) instanceof Map<?, ?> m ? (Map<String, Object>) m : null;
                strip(restored);
                assertEquals(baseline, restored, where + " the document is the one without headers plus the headers");
            }
        }
    }

    /** Takes away what the headers added: the parameters but the path one, and the headers of every response. */
    @SuppressWarnings("unchecked")
    private static void strip(Map<String, Object> document) {
        for (Object item : ((Map<String, Object>) document.get("paths")).values()) {
            for (Object operation : ((Map<String, Object>) item).values().stream().filter(v -> v instanceof Map).toList()) {
                Map<String, Object> o = (Map<String, Object>) operation;
                List<Map<String, Object>> parameters = (List<Map<String, Object>>) o.get("parameters");
                parameters.clear();
                // the generator makes no parameters of its own, so an operation without any has no list of them
                o.remove("parameters");
                ((Map<String, Object>) o.get("responses")).values().forEach(r -> ((Map<String, Object>) r).remove("headers"));
            }
        }
        Object components = document.get("components");
        if (components instanceof Map<?, ?> c && c.get("responses") instanceof Map<?, ?> responses) {
            responses.values().forEach(r -> ((Map<String, Object>) r).remove("headers"));
        }
    }

    // ---- the YAML of a document says what the JSON of it says ------------------------------------------------------

    /** Values a YAML reader of version 1.1 (PyYAML, SnakeYAML) reads as something other than the string they are. */
    private static final List<String> TRICKY_STRINGS = List.of("yes", "No", "on", "OFF", "true", "null", "~", "1", "1.0", "0123",
            "0x1F", "1e3", ".inf", "2020-01-01", "2020-01-01T10:00:00Z", "12:30", "a: b", "- a", "# a", "a #b", "[a]", "{a}",
            "'a'", "\"a\"", "&a", "*a", "!a", "%a", "@a", "`a", "|", ">", "a\nb", "a\n", " a", "a ", "", "é한", "00-4bf92f3577b34da6a3ce929d0e0e4736-a1b2c3d4e5f60718-01",
            "^[0-9a-f]{2}-[0-9a-f]{32}$", "=", "<<", "?", "? a");

    @Test
    @SuppressWarnings("unchecked")
    void theYamlOfADocumentSaysWhatItsJsonSays() throws Exception {
        for (BieOpenApiDialect dialect : List.of(BieOpenApi30Dialect.INSTANCE, BieOpenApi31Dialect.INSTANCE)) {
            List<OasDocHttpHeader> headers = new java.util.ArrayList<>();
            int n = 0;
            for (String text : TRICKY_STRINGS) {
                String name = "x-" + (n++);
                headers.add(header(Direction.REQUEST, name, "header", "{\"type\":\"string\",\"pattern\":" + OasJson.toJson(text) + ",\"default\":" + OasJson.toJson(text) + "}",
                        h -> { h.setExample(text); h.setDescription(text); }));
                headers.add(header(Direction.RESPONSE, name, null, "{\"type\":\"string\",\"enum\":[" + OasJson.toJson(text) + "]}",
                        h -> { h.setExample(text); h.setDescription(text); }));
            }
            Map<String, Object> json = generateEveryVerb(dialect, headers, "NONE");
            Map<String, Object> yaml = new org.yaml.snakeyaml.Yaml().load(generateEveryVerbAs(dialect, headers, "NONE", OpenAPIExpressionFormat.YAML));
            assertEquals(json, yaml, dialect.openApiVersion() + ": the YAML reads as another document");
        }
    }
}

