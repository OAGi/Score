package org.oagi.score.gateway.http.api.oas_management.service.generate_openapi_expression;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.oas_management.model.OasDocHttpHeader;
import org.oagi.score.gateway.http.api.oas_management.model.OasDocHttpHeader.Direction;
import org.oagi.score.gateway.http.api.oas_management.model.OasJson;
import org.oagi.score.gateway.http.api.oas_management.model.OasOperationId;
import org.oagi.score.gateway.http.api.oas_management.model.OasSchema;
import org.oagi.score.gateway.http.api.oas_management.model.OpenAPITemplateForVerbOption;
import org.oagi.score.gateway.http.api.oas_management.model.Operation;
import org.oagi.score.gateway.http.api.oas_management.service.OasSpecValidator;
import org.oagi.score.gateway.http.api.oas_management.service.OasSpecValidator.Version;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Issue #1791: what a request parameter and a response header look like on every kind of endpoint that Add BIE and Add
 * Operation make, in OpenAPI 3.0.4 and 3.1.2. The endpoints are the shapes that the generator really produced for the
 * documents of a production copy (GET of a list and of one resource, POST, PUT, PATCH and DELETE with and without a body,
 * responses of their own and shared ones), kept in {@code oas-endpoint-shapes}, and the bodyless operations of Add
 * Operation. Each document is checked against the JSON Schema that the OpenAPI Initiative publishes, and against what the
 * schema cannot say; and it must be the document it was, plus the headers.
 */
class HttpHeaderEndpointMatrixTest {

    private static final BigInteger THE_OPERATION = BigInteger.ONE;
    private static final BigInteger ANOTHER_OPERATION = BigInteger.TWO;

    private record Shape(String path, String verb, Map<String, Object> operation) {
        @Override
        public String toString() {
            return verb.toUpperCase() + " " + path;
        }
    }

    private static final List<Shape> SHAPES = loadShapes();
    private static final Map<String, Object> SHARED_RESPONSES = loadSharedResponses();

    // ---- the fixture -----------------------------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> files() {
        List<Map<String, Object>> files = new ArrayList<>();
        for (String version : List.of("3.0", "3.1")) {
            try (InputStream in = HttpHeaderEndpointMatrixTest.class.getResourceAsStream("/oas-endpoint-shapes/shapes-" + version + ".json")) {
                files.add((Map<String, Object>) OasJson.fromJson(new String(in.readAllBytes())));
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        return files;
    }

    @SuppressWarnings("unchecked")
    private static List<Shape> loadShapes() {
        List<Shape> shapes = new ArrayList<>();
        for (Map<String, Object> file : files()) {
            for (Map<String, Object> shape : (List<Map<String, Object>>) file.get("operations")) {
                String path = (String) shape.get("path");
                shapes.add(new Shape(path.startsWith("/") ? path : "/" + path, (String) shape.get("verb"),
                        (Map<String, Object>) shape.get("operation")));
            }
        }
        // The operations of Add Operation have no BIE: a status only, in every verb.
        for (String verb : List.of("get", "post", "put", "patch", "delete")) {
            for (String status : List.of("200", "202", "204")) {
                Map<String, Object> responses = new LinkedHashMap<>();
                responses.put(status, new LinkedHashMap<>(Map.of("description", "status " + status)));
                responses.put("404", new LinkedHashMap<>(Map.of("$ref", "#/components/responses/NotFound")));
                Map<String, Object> operation = new LinkedHashMap<>();
                operation.put("operationId", verb + "Bodyless" + status);
                operation.put("parameters", List.of(new LinkedHashMap<>(Map.of("name", "id", "in", "path", "required", true,
                        "schema", Map.of("type", "string")))));
                operation.put("responses", responses);
                shapes.add(new Shape("/things/{id}", verb, operation));
            }
        }
        // ... and without a path parameter
        Map<String, Object> responses = new LinkedHashMap<>();
        responses.put("204", new LinkedHashMap<>(Map.of("description", "No Content")));
        shapes.add(new Shape("/things", "delete", new LinkedHashMap<>(Map.of("operationId", "deleteThings", "responses", responses))));
        return shapes;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> loadSharedResponses() {
        Map<String, Object> responses = new LinkedHashMap<>();
        for (Map<String, Object> file : files()) {
            responses.putAll((Map<String, Object>) file.get("responses"));
        }
        responses.put("NotFound", new LinkedHashMap<>(Map.of("description", "Not Found")));
        return responses;
    }

    @SuppressWarnings("unchecked")
    private static <T> T copy(T value) {
        return (T) OasJson.fromJson(OasJson.toJson(value));
    }

    private static Map<String, Object> document(Version version, Shape shape) {
        Map<String, Object> verbs = new LinkedHashMap<>();
        verbs.put(shape.verb(), copy(shape.operation()));
        Map<String, Object> components = new LinkedHashMap<>();
        components.put("schemas", new LinkedHashMap<>(Map.of("Stub", Map.of("type", "object"), "Thing", Map.of("type", "string"))));
        components.put("responses", copy(SHARED_RESPONSES));
        Map<String, Object> document = new LinkedHashMap<>();
        document.put("openapi", version.number());
        document.put("info", Map.of("title", "t", "version", "1"));
        document.put("paths", new LinkedHashMap<>(Map.of(shape.path(), verbs)));
        document.put("components", components);
        return document;
    }

    // ---- headers ---------------------------------------------------------------------------------------------------

    private static OasDocHttpHeader h(Direction direction, BigInteger operation, String name, String in, String schema) {
        OasDocHttpHeader header = new OasDocHttpHeader();
        header.setDirection(direction);
        header.setOasOperationId(operation);
        header.setName(name);
        header.setIn(direction == Direction.REQUEST ? in : null);
        header.setDescription("The " + name + ".");
        header.setSchema(OasSchema.fromJson(schema));
        return header;
    }

    private static final String STRING = "{\"type\":\"string\"}";
    private static final String ARRAY = "{\"type\":\"array\",\"items\":{\"type\":\"string\"}}";
    private static final String OBJECT = "{\"type\":\"object\",\"properties\":{\"a\":{\"type\":\"string\"}}}";
    private static final String TRACE = "{\"type\":\"string\",\"pattern\":\"^[0-9a-f]{2}-[0-9a-f]{32}-[0-9a-f]{16}-[0-9a-f]{2}$\"}";

    private static OasDocHttpHeader with(OasDocHttpHeader header, java.util.function.Consumer<OasDocHttpHeader> change) {
        change.accept(header);
        return header;
    }

    /** A scenario: the headers a user can have given, and whether they apply to the operation under test. */
    private record Scenario(String name, Supplier<List<OasDocHttpHeader>> headers, boolean touches) {
    }

    private static Scenario s(String name, boolean touches, Supplier<List<OasDocHttpHeader>> headers) {
        return new Scenario(name, headers, touches);
    }

    private static List<Scenario> scenarios() {
        List<Scenario> scenarios = new ArrayList<>();
        scenarios.add(s("traceparent for the document", true, () -> List.of(
                with(h(Direction.REQUEST, null, "traceparent", "header", TRACE), x -> x.setExample("00-4bf92f3577b34da6a3ce929d0e0e4736-a1b2c3d4e5f60718-01")),
                with(h(Direction.RESPONSE, null, "traceparent", null, TRACE), x -> x.setRequired(true)))));
        scenarios.add(s("request parameter only", true, () -> List.of(h(Direction.REQUEST, null, "x-request-id", "header", STRING))));
        scenarios.add(s("response header only", true, () -> List.of(with(h(Direction.RESPONSE, null, "x-rate-limit", null, "{\"type\":\"integer\"}"), x -> x.setRequired(true)))));
        scenarios.add(s("traceparent for the operation", true, () -> List.of(
                h(Direction.REQUEST, THE_OPERATION, "traceparent", "header", TRACE),
                with(h(Direction.RESPONSE, THE_OPERATION, "traceparent", null, TRACE), x -> x.setRequired(true)))));
        scenarios.add(s("traceparent for another operation", false, () -> List.of(
                h(Direction.REQUEST, ANOTHER_OPERATION, "traceparent", "header", TRACE),
                h(Direction.RESPONSE, ANOTHER_OPERATION, "traceparent", null, TRACE))));
        scenarios.add(s("the operation overrides the document", true, () -> List.of(
                h(Direction.REQUEST, null, "traceparent", "header", TRACE),
                with(h(Direction.REQUEST, THE_OPERATION, "Traceparent", "header", STRING), x -> x.setRequired(true)),
                h(Direction.RESPONSE, null, "traceparent", null, TRACE),
                with(h(Direction.RESPONSE, THE_OPERATION, "TraceParent", null, STRING), x -> x.setRequired(true)))));
        scenarios.add(s("query parameters", true, () -> List.of(
                with(h(Direction.REQUEST, null, "tags", "query", ARRAY), x -> { x.setStyle("form"); x.setExplode(true); }),
                with(h(Direction.REQUEST, null, "ids", "query", ARRAY), x -> { x.setStyle("form"); x.setExplode(false); }),
                with(h(Direction.REQUEST, null, "q", "query", STRING), x -> x.setAllowReserved(true)),
                with(h(Direction.REQUEST, null, "words", "query", ARRAY), x -> x.setStyle("spaceDelimited")),
                with(h(Direction.REQUEST, null, "pipes", "query", ARRAY), x -> x.setStyle("pipeDelimited")),
                with(h(Direction.REQUEST, null, "filter", "query", OBJECT), x -> { x.setStyle("deepObject"); x.setExplode(true); })))); 
        scenarios.add(s("cookie parameters", true, () -> List.of(
                with(h(Direction.REQUEST, null, "session", "cookie", STRING), x -> x.setStyle("form")),
                with(h(Direction.REQUEST, null, "prefs", "cookie", STRING), x -> { x.setContentMediaType("text/plain"); x.setExample("a=1"); }))));
        scenarios.add(s("header parameters of every type", true, () -> List.of(
                with(h(Direction.REQUEST, null, "x-list", "header", ARRAY), x -> { x.setStyle("simple"); x.setExplode(true); }),
                with(h(Direction.REQUEST, null, "x-object", "header", OBJECT), x -> x.setExplode(true)),
                with(h(Direction.REQUEST, null, "x-json", "header", OBJECT), x -> { x.setContentMediaType("application/json"); x.setExample(Map.of("a", "b")); }),
                h(Direction.REQUEST, null, "x-int", "header", "{\"type\":\"integer\"}"))));
        scenarios.add(s("response headers of every kind", true, () -> List.of(
                with(h(Direction.RESPONSE, null, "x-list", null, ARRAY), x -> { x.setStyle("simple"); x.setExplode(true); }),
                with(h(Direction.RESPONSE, null, "x-json", null, OBJECT), x -> { x.setContentMediaType("application/json"); x.setExample(Map.of("a", "b")); }),
                with(h(Direction.RESPONSE, null, "x-text", null, STRING), x -> { x.setContentMediaType("text/plain"); x.setRequired(true); }),
                with(h(Direction.RESPONSE, null, "x-int", null, "{\"type\":\"integer\"}"), x -> x.setExample(7)))));
        scenarios.add(s("examples of every JSON type", true, () -> List.of(
                with(h(Direction.REQUEST, null, "x-string", "header", STRING), x -> x.setExample("abc")),
                with(h(Direction.REQUEST, null, "x-number", "header", "{\"type\":\"number\"}"), x -> x.setExample(1.5)),
                with(h(Direction.REQUEST, null, "x-bool", "header", "{\"type\":\"boolean\"}"), x -> x.setExample(true)),
                with(h(Direction.REQUEST, null, "x-object", "header", OBJECT), x -> x.setExample(Map.of("a", "b"))),
                with(h(Direction.REQUEST, null, "x-array", "header", ARRAY), x -> x.setExample(List.of("a", "b"))),
                with(h(Direction.RESPONSE, null, "x-string", null, STRING), x -> x.setExample("abc")),
                with(h(Direction.RESPONSE, null, "x-array", null, ARRAY), x -> x.setExample(List.of("a", "b"))))));
        scenarios.add(s("a schema that refers to the document", true, () -> List.of(
                h(Direction.REQUEST, null, "x-ref", "header", "{\"$ref\":\"#/components/schemas/Thing\"}"),
                h(Direction.RESPONSE, null, "x-ref", null, "{\"$ref\":\"#/components/schemas/Thing\"}"))));
        scenarios.add(s("the same name in every place", true, () -> List.of(
                h(Direction.REQUEST, null, "id", "query", STRING), h(Direction.REQUEST, null, "id", "cookie", STRING),
                h(Direction.REQUEST, null, "id", "header", STRING), h(Direction.RESPONSE, null, "id", null, STRING))));
        return scenarios;
    }

    // ---- looking at a document -------------------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private static Map<String, Object> operationOf(Map<String, Object> document, Shape shape) {
        return (Map<String, Object>) ((Map<String, Object>) ((Map<String, Object>) document.get("paths")).get(shape.path())).get(shape.verb());
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> responsesOf(Map<String, Object> document) {
        return (Map<String, Object>) ((Map<String, Object>) document.get("components")).get("responses");
    }

    private static OpenAPITemplateForVerbOption template(Shape shape, BigInteger id) {
        OpenAPITemplateForVerbOption template = new OpenAPITemplateForVerbOption(Operation.valueOf(shape.verb().toUpperCase()));
        template.setResourceName(shape.path());
        template.setOasOperationId(new OasOperationId(id));
        return template;
    }

    private static void inject(Map<String, Object> document, Shape shape, List<OasDocHttpHeader> headers) {
        HttpHeaderInjector.inject(document, List.of(template(shape, THE_OPERATION)), OpenAPITemplateForVerbOption::getResourceName, headers);
    }

    /** The headers of a response: its own, or those of the shared response it refers to. */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> headersOf(Map<String, Object> document, Map<String, Object> response) {
        Map<String, Object> target = response;
        if (response.get("$ref") instanceof String ref) {
            target = (Map<String, Object>) responsesOf(document).get(ref.substring(ref.lastIndexOf('/') + 1));
        }
        Object headers = target.get("headers");
        return headers == null ? Map.of() : (Map<String, Object>) headers;
    }

    /** The parameters of the operation, as they are told apart in OpenAPI: by location and name. */
    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> parametersOf(Map<String, Object> operation) {
        Object parameters = operation.get("parameters");
        return parameters == null ? List.of() : (List<Map<String, Object>>) parameters;
    }

    /** What the JSON Schema cannot say: the rules of the specification that are about the whole of an operation. */
    @SuppressWarnings("unchecked")
    private static List<String> lint(Shape shape, Map<String, Object> document) {
        List<String> problems = new ArrayList<>();
        Map<String, Object> operation = operationOf(document, shape);
        List<String> seen = new ArrayList<>();
        for (Map<String, Object> parameter : parametersOf(operation)) {
            String in = (String) parameter.get("in");
            String name = (String) parameter.get("name");
            String key = in + " " + ("header".equals(in) ? name.toLowerCase() : name);
            if (seen.contains(key)) {
                problems.add("the parameter " + key + " is defined twice");
            }
            seen.add(key);
            if ("path".equals(in) && !shape.path().contains("{" + name + "}")) {
                problems.add("the path parameter " + name + " is not in the path");
            }
            if (parameter.containsKey("schema") == parameter.containsKey("content")) {
                problems.add("the parameter " + key + " has both or neither of schema and content");
            }
            if (parameter.containsKey("content") && ((Map<?, ?>) parameter.get("content")).size() != 1) {
                problems.add("the content of " + key + " has not one entry");
            }
            if (parameter.containsKey("example") && parameter.containsKey("examples")) {
                problems.add("the parameter " + key + " has example and examples");
            }
            if ("header".equals(in) && List.of("accept", "content-type", "authorization").contains(name.toLowerCase())) {
                problems.add("the header parameter " + name + " is ignored");
            }
        }
        // every template expression of the path has its path parameter
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\{([^}]+)}").matcher(shape.path());
        while (matcher.find()) {
            String name = matcher.group(1);
            if (parametersOf(operation).stream().noneMatch(p -> "path".equals(p.get("in")) && name.equals(p.get("name")))) {
                problems.add("the template expression {" + name + "} has no path parameter");
            }
        }
        for (Map.Entry<String, Object> response : ((Map<String, Object>) operation.get("responses")).entrySet()) {
            for (Map.Entry<String, Object> header : headersOf(document, (Map<String, Object>) response.getValue()).entrySet()) {
                Map<String, Object> object = (Map<String, Object>) header.getValue();
                if (header.getKey().equalsIgnoreCase("content-type")) {
                    problems.add("the response header Content-Type is ignored");
                }
                if (object.containsKey("name") || object.containsKey("in")) {
                    problems.add("the header " + header.getKey() + " has name or in");
                }
            }
        }
        return problems;
    }

    private static String where(Version version, Shape shape, Scenario scenario) {
        return version.number() + " " + shape + " / " + scenario.name();
    }

    // ---- the tests -------------------------------------------------------------------------------------------------

    @Test
    void theFixtureIsASoundBaseline() {
        assertTrue(SHAPES.size() >= 30, "kinds of endpoint: " + SHAPES.size());
        List<String> problems = new ArrayList<>();
        for (Version version : Version.values()) {
            for (Shape shape : SHAPES) {
                Map<String, Object> document = document(version, shape);
                OasSpecValidator.validate(version, document).forEach(p -> problems.add(version.number() + " " + shape + ": " + p));
                lint(shape, document).forEach(p -> problems.add(version.number() + " " + shape + ": " + p));
            }
        }
        assertEquals(List.of(), problems, "the documents before any header are valid");
    }

    @Test
    void everyEndpointWithEveryKindOfHeaderIsAValidDocumentAndIsTheDocumentItWasPlusTheHeaders() {
        List<String> problems = new ArrayList<>();
        int documents = 0;
        for (Version version : Version.values()) {
            for (Shape shape : SHAPES) {
                for (Scenario scenario : scenarios()) {
                    Map<String, Object> before = document(version, shape);
                    Map<String, Object> after = document(version, shape);
                    try {
                        inject(after, shape, scenario.headers().get());
                    } catch (RuntimeException e) {
                        problems.add(where(version, shape, scenario) + ": " + e.getMessage());
                        continue;
                    }
                    documents++;
                    OasSpecValidator.validate(version, after).forEach(p -> problems.add(where(version, shape, scenario) + ": " + p));
                    lint(shape, after).forEach(p -> problems.add(where(version, shape, scenario) + ": " + p));
                    if (!scenario.touches()) {
                        if (!before.equals(after)) {
                            problems.add(where(version, shape, scenario) + ": the document changed, but nothing applies to it");
                        }
                        continue;
                    }
                    problems.addAll(onlyAdditions(version, shape, scenario, before, after));
                }
            }
        }
        System.out.println("documents checked: " + documents);
        assertEquals(List.of(), problems.subList(0, Math.min(40, problems.size())));
    }

    /** Taking the new parameters and headers away gives the document it was: nothing was changed or lost. */
    @SuppressWarnings("unchecked")
    private static List<String> onlyAdditions(Version version, Shape shape, Scenario scenario,
                                              Map<String, Object> before, Map<String, Object> after) {
        List<String> problems = new ArrayList<>();
        Map<String, Object> restored = copy(after);
        Map<String, Object> operation = operationOf(restored, shape);
        Map<String, Object> was = operationOf(before, shape);
        List<Map<String, Object>> parametersBefore = parametersOf(was);
        List<Map<String, Object>> kept = new ArrayList<>();
        for (Map<String, Object> parameter : parametersOf(operation)) {
            if (parametersBefore.contains(parameter)) {
                kept.add(parameter);
            }
        }
        if (kept.isEmpty()) {
            operation.remove("parameters");
        } else {
            operation.put("parameters", kept);
        }
        if (kept.size() != parametersBefore.size()) {
            problems.add(where(version, shape, scenario) + ": a parameter of the operation was changed or lost");
        }
        Map<String, Object> responses = (Map<String, Object>) operation.get("responses");
        Map<String, Object> responsesBefore = (Map<String, Object>) was.get("responses");
        responses.forEach((code, response) -> {
            Map<String, Object> inline = (Map<String, Object>) response;
            Map<String, Object> original = (Map<String, Object>) responsesBefore.get(code);
            if (original.containsKey("$ref") && !inline.containsKey("$ref")) {
                // a response that is shared by operations with different headers is copied, with its headers
                inline.remove("headers");
                if (!inline.equals(copy(responsesOf(before).get(((String) original.get("$ref")).substring(((String) original.get("$ref")).lastIndexOf('/') + 1))))) {
                    problems.add(where(version, shape, scenario) + ": the copy of the shared response " + code + " differs from it");
                }
                inline.clear();
                inline.putAll(original);
            } else {
                inline.remove("headers");
                if (original.containsKey("headers")) {
                    inline.put("headers", original.get("headers"));
                }
            }
        });
        Map<String, Object> components = (Map<String, Object>) restored.get("components");
        Map<String, Object> shared = (Map<String, Object>) components.get("responses");
        shared.values().forEach(r -> ((Map<String, Object>) r).remove("headers"));
        Map<String, Object> originalShared = copy(responsesOf(before));
        originalShared.values().forEach(r -> ((Map<String, Object>) r).remove("headers"));
        if (!shared.equals(originalShared)) {
            problems.add(where(version, shape, scenario) + ": a shared response was changed");
        }
        components.put("responses", responsesOf(before));
        if (!restored.equals(before)) {
            problems.add(where(version, shape, scenario) + ": more than the headers changed");
        }
        return problems;
    }

    @Test
    void everyOperationAndEveryResponseCarriesTheHeadersOfTheDocument() {
        List<String> problems = new ArrayList<>();
        for (Version version : Version.values()) {
            for (Shape shape : SHAPES) {
                Map<String, Object> document = document(version, shape);
                List<OasDocHttpHeader> headers = List.of(
                        h(Direction.REQUEST, null, "traceparent", "header", TRACE),
                        with(h(Direction.RESPONSE, null, "traceparent", null, TRACE), x -> x.setRequired(true)));
                inject(document, shape, headers);
                Map<String, Object> operation = operationOf(document, shape);
                long requestParameters = parametersOf(operation).stream()
                        .filter(p -> "traceparent".equals(p.get("name")) && "header".equals(p.get("in"))).count();
                if (requestParameters != 1) {
                    problems.add(version.number() + " " + shape + ": " + requestParameters + " traceparent parameters");
                }
                @SuppressWarnings("unchecked")
                Map<String, Object> responses = (Map<String, Object>) operation.get("responses");
                responses.forEach((code, response) -> {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> header = (Map<String, Object>) headersOf(document, (Map<String, Object>) response).get("traceparent");
                    if (header == null) {
                        problems.add(version.number() + " " + shape + " " + code + ": no traceparent header");
                    } else if (!Boolean.TRUE.equals(header.get("required"))) {
                        problems.add(version.number() + " " + shape + " " + code + ": the header is not required");
                    }
                });
            }
        }
        assertEquals(List.of(), problems);
    }

    @Test
    void aPathParameterThatIsDefinedReplacesTheOneTheGeneratorMade() {
        List<String> problems = new ArrayList<>();
        for (Version version : Version.values()) {
            for (Shape shape : SHAPES) {
                java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\{([^}]+)}").matcher(shape.path());
                if (!matcher.find()) {
                    continue;
                }
                String name = matcher.group(1);
                for (String style : new String[]{null, "matrix", "label", "simple"}) {
                    Map<String, Object> document = document(version, shape);
                    OasDocHttpHeader id = h(Direction.REQUEST, THE_OPERATION, name, "path", "{\"type\":\"integer\"}");
                    id.setStyle(style);
                    id.setRequired(true);
                    inject(document, shape, List.of(id));
                    List<Map<String, Object>> found = parametersOf(operationOf(document, shape)).stream()
                            .filter(p -> "path".equals(p.get("in")) && name.equals(p.get("name"))).toList();
                    if (found.size() != 1) {
                        problems.add(version.number() + " " + shape + " " + name + ": " + found.size() + " path parameters");
                    } else {
                        assertEquals("integer", ((Map<?, ?>) found.get(0).get("schema")).get("type"));
                        assertEquals(true, found.get(0).get("required"));
                    }
                    OasSpecValidator.validate(version, document).forEach(p -> problems.add(version.number() + " " + shape + ": " + p));
                    lint(shape, document).forEach(p -> problems.add(version.number() + " " + shape + ": " + p));
                }
            }
        }
        assertEquals(List.of(), problems);
    }

    @Test
    void aPathParameterForAnOperationNeedsToBeInItsPathAndOneForTheDocumentInThePathOfOne() {
        Shape noPath = SHAPES.stream().filter(sh -> sh.path().equals("/things") && sh.verb().equals("delete")).findFirst().orElseThrow();
        Shape withPath = SHAPES.stream().filter(sh -> sh.path().equals("/things/{id}") && sh.verb().equals("get")).findFirst().orElseThrow();
        for (Version version : Version.values()) {
            IllegalArgumentException forTheOperation = assertThrows(IllegalArgumentException.class, () ->
                    inject(document(version, noPath), noPath, List.of(h(Direction.REQUEST, THE_OPERATION, "id", "path", STRING))));
            assertTrue(forTheOperation.getMessage().contains("'id' is not in the path '/things'"), forTheOperation.getMessage());
            IllegalArgumentException forTheDocument = assertThrows(IllegalArgumentException.class, () ->
                    inject(document(version, noPath), noPath, List.of(h(Direction.REQUEST, null, "id", "path", STRING))));
            assertTrue(forTheDocument.getMessage().contains("none of its operations"), forTheDocument.getMessage());
            assertThrows(IllegalArgumentException.class, () ->
                    inject(document(version, withPath), withPath, List.of(h(Direction.REQUEST, THE_OPERATION, "other", "path", STRING))));
            // for the document, an operation without the expression is left alone
            Map<String, Object> document = document(version, withPath);
            inject(document, withPath, List.of(h(Direction.REQUEST, null, "id", "path", "{\"type\":\"integer\"}")));
            assertEquals(List.of(), OasSpecValidator.validate(version, document));
        }
    }

    @Test
    void aSchemaThatReferstoWhatTheDocumentDoesNotHaveIsRefused() {
        Shape shape = SHAPES.get(0);
        for (Version version : Version.values()) {
            for (String ref : List.of("#/components/schemas/Missing", "#/components/responses/Missing/headers",
                    "#/paths/~1nowhere")) {
                IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> inject(document(version, shape), shape,
                        List.of(h(Direction.RESPONSE, null, "x-ref", null, "{\"type\":\"array\",\"items\":{\"$ref\":\"" + ref + "\"}}"))));
                assertTrue(e.getMessage().contains(ref), e.getMessage());
            }
            // the one of another document is not the document's to check
            inject(document(version, shape), shape, List.of(h(Direction.RESPONSE, null, "x-ref", null,
                    "{\"$ref\":\"https://example.com/schemas/thing.json\"}")));
        }
    }

    @Test
    void aDefinedParameterTakesThePlaceOfTheOneTheGeneratorMade() {
        Shape list = SHAPES.stream().filter(sh -> sh.verb().equals("get") && parametersOf(sh.operation()).stream()
                .anyMatch(p -> "sinceLastDateTime".equals(p.get("name")))).findFirst().orElseThrow();
        for (Version version : Version.values()) {
            for (BigInteger scope : new BigInteger[]{null, THE_OPERATION}) {
                Map<String, Object> document = document(version, list);
                int before = parametersOf(operationOf(document, list)).size();
                inject(document, list, List.of(h(Direction.REQUEST, scope, "sinceLastDateTime", "query", "{\"type\":\"integer\"}")));
                List<Map<String, Object>> after = parametersOf(operationOf(document, list));
                // one of them, the defined one
                assertEquals(before, after.size());
                List<Map<String, Object>> named = after.stream().filter(p -> "sinceLastDateTime".equals(p.get("name"))).toList();
                assertEquals(1, named.size());
                assertEquals(Map.of("type", "integer"), named.get(0).get("schema"));
            }
        }
    }

    @Test
    void aDefinedPathParameterTakesThePlaceOfTheGeneratedOneAndNothingIsLeftWithoutOne() {
        Shape single = SHAPES.stream().filter(sh -> sh.path().contains("{")).findFirst().orElseThrow();
        String name = single.path().substring(single.path().indexOf('{') + 1, single.path().indexOf('}'));
        for (Version version : Version.values()) {
            Map<String, Object> document = document(version, single);
            inject(document, single, List.of(h(Direction.REQUEST, null, name, "path", "{\"type\":\"integer\"}")));
            List<Map<String, Object>> named = parametersOf(operationOf(document, single)).stream()
                    .filter(p -> name.equals(p.get("name")) && "path".equals(p.get("in"))).toList();
            assertEquals(1, named.size());
            assertEquals(Map.of("type", "integer"), named.get(0).get("schema"));
            // without a definition the generated one stays
            Map<String, Object> plain = document(version, single);
            assertEquals(1, parametersOf(operationOf(plain, single)).stream()
                    .filter(p -> name.equals(p.get("name")) && "path".equals(p.get("in"))).count());
        }
    }
}
