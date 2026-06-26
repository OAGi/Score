package org.oagi.score.gateway.http.api.oas_management.service.generate_openapi_expression;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.oas_management.model.OpenAPIErrorResponseBodyType;
import org.oagi.score.gateway.http.api.oas_management.model.OpenAPITemplateForVerbOption;
import org.oagi.score.gateway.http.api.oas_management.model.Operation;
import org.oagi.score.gateway.http.api.oas_management.service.generate_openapi_expression.ErrorResponseSchemas.OperationErrorSpec;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Issue #1347: unit tests for the defaulted OAS Doc error responses — the status matrix (§3.4),
 * the ProblemDetails literal (§5.3), and the idempotent merge-not-overwrite post-step (§5.6).
 */
class ErrorResponseSchemasTest {

    // ------------------------------------------------------------------ matrix (§3.4)

    @Test
    void matrix_getList_hasBadRequest_notNotFound_notConflict_no415_422() {
        assertEquals(List.of(400, 401, 403, 500, 502, 503, 504),
                ErrorResponseSchemas.errorStatusMatrix("get", true, false));
    }

    @Test
    void matrix_getItem_hasNotFound_notBadRequest() {
        assertEquals(List.of(401, 403, 404, 500, 502, 503, 504),
                ErrorResponseSchemas.errorStatusMatrix("get", false, false));
    }

    @Test
    void matrix_post_hasBodyErrors_notNotFound() {
        assertEquals(List.of(400, 401, 403, 409, 415, 422, 500, 502, 503, 504),
                ErrorResponseSchemas.errorStatusMatrix("post", false, false));
    }

    @Test
    void matrix_put_and_patch_full() {
        List<Integer> expected = List.of(400, 401, 403, 404, 409, 415, 422, 500, 502, 503, 504);
        assertEquals(expected, ErrorResponseSchemas.errorStatusMatrix("put", false, false));
        assertEquals(expected, ErrorResponseSchemas.errorStatusMatrix("patch", false, false));
        // array indicator must not affect non-GET verbs
        assertEquals(expected, ErrorResponseSchemas.errorStatusMatrix("put", true, false));
    }

    @Test
    void matrix_delete_30_hasNo415Nor422Nor400() {
        assertEquals(List.of(401, 403, 404, 409, 500, 502, 503, 504),
                ErrorResponseSchemas.errorStatusMatrix("delete", false, false));
    }

    @Test
    void matrix_delete_31_adds415And422() {
        assertEquals(List.of(401, 403, 404, 409, 415, 422, 500, 502, 503, 504),
                ErrorResponseSchemas.errorStatusMatrix("delete", false, true));
    }

    @Test
    void matrix_unknownVerb_isEmpty() {
        assertTrue(ErrorResponseSchemas.errorStatusMatrix("head", false, true).isEmpty());
        assertTrue(ErrorResponseSchemas.errorStatusMatrix(null, false, true).isEmpty());
    }

    // ------------------------------------------------------------------ phrases / names

    @Test
    void reasonPhrase_covers4xx5xx() {
        assertEquals("Bad Request", ErrorResponseSchemas.reasonPhrase(400));
        assertEquals("Not Found", ErrorResponseSchemas.reasonPhrase(404));
        assertEquals("Unprocessable Content", ErrorResponseSchemas.reasonPhrase(422));
        assertEquals("Internal Server Error", ErrorResponseSchemas.reasonPhrase(500));
        assertEquals("OK", ErrorResponseSchemas.reasonPhrase(200));
    }

    @Test
    void responseComponentName_isCodeUnderscorePascalPhrase() {
        assertEquals("400_BadRequest", ErrorResponseSchemas.responseComponentName(400));
        assertEquals("415_UnsupportedMediaType", ErrorResponseSchemas.responseComponentName(415));
        assertEquals("500_InternalServerError", ErrorResponseSchemas.responseComponentName(500));
    }

    // ------------------------------------------------------------------ ProblemDetails literal (§5.3)

    @Test
    @SuppressWarnings("unchecked")
    void problemDetailsSchema_isRfc9457Shape() {
        Map<String, Object> schema = ErrorResponseSchemas.problemDetailsSchema(false);
        assertEquals("object", schema.get("type"));
        assertEquals(List.of("status", "title"), schema.get("required"));
        Map<String, Object> props = (Map<String, Object>) schema.get("properties");
        assertEquals(List.of("type", "title", "status", "detail", "instance"),
                List.copyOf(props.keySet()));
        Map<String, Object> typeProp = (Map<String, Object>) props.get("type");
        assertEquals("uri-reference", typeProp.get("format"));
        assertEquals("about:blank", typeProp.get("default"));
        // identical for 3.0 and 3.1 (no nullable fields)
        assertEquals(schema, ErrorResponseSchemas.problemDetailsSchema(true));
    }

    // ------------------------------------------------------------------ injection (§5.6)

    @SuppressWarnings("unchecked")
    private static Map<String, Object> newRoot(Map<String, Object> schemas) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("paths", new LinkedHashMap<String, Object>());
        Map<String, Object> components = new LinkedHashMap<>();
        components.put("securitySchemes", new LinkedHashMap<>());
        components.put("schemas", schemas);
        root.put("components", components);
        return root;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> putOperation(Map<String, Object> root, String path, String verb,
                                                    Map<String, Object> successResponses) {
        Map<String, Object> paths = (Map<String, Object>) root.get("paths");
        Map<String, Object> pathItem = (Map<String, Object>) paths.computeIfAbsent(path, k -> new LinkedHashMap<>());
        Map<String, Object> op = new LinkedHashMap<>();
        op.put("operationId", verb + "X");
        op.put("responses", successResponses);
        pathItem.put(verb, op);
        return op;
    }

    @Test
    @SuppressWarnings("unchecked")
    void inject_none_emitsDescriptionOnly_noComponentsResponses() {
        Map<String, Object> schemas = new LinkedHashMap<>();
        Map<String, Object> root = newRoot(schemas);
        Map<String, Object> success = new LinkedHashMap<>();
        success.put("201", new LinkedHashMap<>(Map.of("description", "Created")));
        Map<String, Object> op = putOperation(root, "/orders", "post", success);

        ErrorResponseSchemas.injectErrorResponses(root, schemas,
                List.of(new OperationErrorSpec("/orders", "post", false, OpenAPIErrorResponseBodyType.NONE, null)), false);

        Map<String, Object> responses = (Map<String, Object>) op.get("responses");
        assertTrue(responses.containsKey("201"), "existing success preserved");
        Map<String, Object> badRequest = (Map<String, Object>) responses.get("400");
        assertEquals("Bad Request", badRequest.get("description"));
        assertFalse(badRequest.containsKey("content"), "NONE has no body");
        // no components.responses, no ProblemDetails schema
        Map<String, Object> components = (Map<String, Object>) root.get("components");
        assertFalse(components.containsKey("responses"));
        assertFalse(schemas.containsKey("ProblemDetails"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void inject_problemDetails_refsReusableComponentsAndInjectsSchema() {
        Map<String, Object> schemas = new LinkedHashMap<>();
        Map<String, Object> root = newRoot(schemas);
        Map<String, Object> success = new LinkedHashMap<>();
        success.put("200", new LinkedHashMap<>(Map.of("description", "OK")));
        Map<String, Object> op = putOperation(root, "/orders/{id}", "get", success);

        ErrorResponseSchemas.injectErrorResponses(root, schemas,
                List.of(new OperationErrorSpec("/orders/{id}", "get", false, OpenAPIErrorResponseBodyType.PROBLEM_DETAILS, null)), false);

        Map<String, Object> responses = (Map<String, Object>) op.get("responses");
        Map<String, Object> notFound = (Map<String, Object>) responses.get("404");
        assertEquals("#/components/responses/404_NotFound", notFound.get("$ref"));
        // ProblemDetails schema present
        assertTrue(schemas.containsKey("ProblemDetails"));
        // reusable components.responses present and application/problem+json
        Map<String, Object> components = (Map<String, Object>) root.get("components");
        Map<String, Object> compResponses = (Map<String, Object>) components.get("responses");
        Map<String, Object> notFoundComp = (Map<String, Object>) compResponses.get("404_NotFound");
        Map<String, Object> content = (Map<String, Object>) notFoundComp.get("content");
        assertTrue(content.containsKey("application/problem+json"));

        // each ProblemDetails response carries an illustrative example with an INTEGER status
        Map<String, Object> mediaType = (Map<String, Object>) content.get("application/problem+json");
        Map<String, Object> example = (Map<String, Object>) mediaType.get("example");
        assertEquals(List.of("title", "status", "detail", "instance"), List.copyOf(example.keySet()));
        assertEquals("Not Found", example.get("title"));
        assertEquals(Integer.valueOf(404), example.get("status"));
        assertTrue(example.get("status") instanceof Integer, "status must be an integer, not a string");
        assertEquals(ErrorResponseSchemas.detailPhrase(404), example.get("detail"));
        assertEquals("", example.get("instance"), "instance left empty (URI reference, not a full URL)");
    }

    @Test
    void problemExample_hasIntegerStatusAndRfc9457Fields_forEveryErrorStatus() {
        for (int status : List.of(400, 401, 403, 404, 409, 415, 422, 500, 502, 503, 504)) {
            Map<String, Object> example = ErrorResponseSchemas.problemExample(status);
            assertEquals(List.of("title", "status", "detail", "instance"), List.copyOf(example.keySet()));
            assertEquals(ErrorResponseSchemas.reasonPhrase(status), example.get("title"));
            assertEquals(Integer.valueOf(status), example.get("status"),
                    "status " + status + " must be an integer matching the schema");
            assertFalse(((String) example.get("detail")).isEmpty(), "detail filled for " + status);
            assertEquals("", example.get("instance"), "instance left empty for " + status);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void inject_confirmMessage_inlinesJsonSchemaRef() {
        Map<String, Object> schemas = new LinkedHashMap<>();
        Map<String, Object> root = newRoot(schemas);
        Map<String, Object> op = putOperation(root, "/orders", "post",
                new LinkedHashMap<>(Map.of("201", new LinkedHashMap<>(Map.of("description", "Created")))));

        ErrorResponseSchemas.injectErrorResponses(root, schemas,
                List.of(new OperationErrorSpec("/orders", "post", false, OpenAPIErrorResponseBodyType.CONFIRM_MESSAGE, "ConfirmMessage")), false);

        Map<String, Object> responses = (Map<String, Object>) op.get("responses");
        Map<String, Object> badRequest = (Map<String, Object>) responses.get("400");
        Map<String, Object> content = (Map<String, Object>) badRequest.get("content");
        Map<String, Object> json = (Map<String, Object>) content.get("application/json");
        Map<String, Object> schema = (Map<String, Object>) json.get("schema");
        assertEquals("#/components/schemas/ConfirmMessage", schema.get("$ref"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void inject_confirmMessage_withoutSchemaName_degradesToDescriptionOnly() {
        Map<String, Object> schemas = new LinkedHashMap<>();
        Map<String, Object> root = newRoot(schemas);
        Map<String, Object> op = putOperation(root, "/orders", "post",
                new LinkedHashMap<>(Map.of("201", new LinkedHashMap<>(Map.of("description", "Created")))));

        ErrorResponseSchemas.injectErrorResponses(root, schemas,
                List.of(new OperationErrorSpec("/orders", "post", false, OpenAPIErrorResponseBodyType.CONFIRM_MESSAGE, null)), false);

        Map<String, Object> responses = (Map<String, Object>) op.get("responses");
        Map<String, Object> badRequest = (Map<String, Object>) responses.get("400");
        assertEquals("Bad Request", badRequest.get("description"));
        assertFalse(badRequest.containsKey("content"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void inject_isIdempotent_andNeverOverwritesSuccess() {
        Map<String, Object> schemas = new LinkedHashMap<>();
        Map<String, Object> root = newRoot(schemas);
        Map<String, Object> success = new LinkedHashMap<>();
        success.put("200", new LinkedHashMap<>(Map.of("description", "OK custom")));
        Map<String, Object> op = putOperation(root, "/orders", "put", success);
        OperationErrorSpec spec = new OperationErrorSpec("/orders", "put", false, OpenAPIErrorResponseBodyType.PROBLEM_DETAILS, null);

        ErrorResponseSchemas.injectErrorResponses(root, schemas, List.of(spec), false);
        Map<String, Object> first = new LinkedHashMap<>((Map<String, Object>) op.get("responses"));

        // run again -> identical
        ErrorResponseSchemas.injectErrorResponses(root, schemas, List.of(spec), false);
        Map<String, Object> second = (Map<String, Object>) op.get("responses");

        assertEquals(first, second);
        // the original success body is never replaced by a $ref
        Map<String, Object> ok = (Map<String, Object>) second.get("200");
        assertEquals("OK custom", ok.get("description"));
    }

    // ------------------------------------------------------------------ body-type parse (#1347 B11)

    @Test
    void from_nullBlankAndUnknown_fallBackToNone_knownNamesRoundTrip() {
        // legacy / missing / unexpected values must never change generation behavior -> NONE
        assertEquals(OpenAPIErrorResponseBodyType.NONE, OpenAPIErrorResponseBodyType.from(null));
        assertEquals(OpenAPIErrorResponseBodyType.NONE, OpenAPIErrorResponseBodyType.from(""));
        assertEquals(OpenAPIErrorResponseBodyType.NONE, OpenAPIErrorResponseBodyType.from("   "));
        assertEquals(OpenAPIErrorResponseBodyType.NONE, OpenAPIErrorResponseBodyType.from("bogus"));
        // valueOf is case-sensitive, so a lowercase name is unrecognized and falls back to NONE
        assertEquals(OpenAPIErrorResponseBodyType.NONE, OpenAPIErrorResponseBodyType.from("problem_details"));
        // known names round-trip; surrounding whitespace is trimmed
        assertEquals(OpenAPIErrorResponseBodyType.PROBLEM_DETAILS, OpenAPIErrorResponseBodyType.from("PROBLEM_DETAILS"));
        assertEquals(OpenAPIErrorResponseBodyType.PROBLEM_DETAILS, OpenAPIErrorResponseBodyType.from("  PROBLEM_DETAILS  "));
        assertEquals(OpenAPIErrorResponseBodyType.CONFIRM_MESSAGE,
                OpenAPIErrorResponseBodyType.from(OpenAPIErrorResponseBodyType.CONFIRM_MESSAGE.name()));
        assertEquals(OpenAPIErrorResponseBodyType.NONE,
                OpenAPIErrorResponseBodyType.from(OpenAPIErrorResponseBodyType.NONE.name()));
    }

    // ------------------------------------------------------------------ twin collapse (#1347 B24)

    @Test
    void buildSpecs_collapsesRequestAndResponseTwins_intoOneSpec_nonNoneWins_arrayIsAnyTwin() {
        // One POST operation owns a Request template and a Response template that share an oas_operation;
        // only the Request twin carries PROBLEM_DETAILS, only the Response twin is an array.
        OpenAPITemplateForVerbOption request = new OpenAPITemplateForVerbOption(Operation.POST);
        request.setErrorResponseBodyType("PROBLEM_DETAILS");
        request.setArrayForJsonExpression(false);
        OpenAPITemplateForVerbOption response = new OpenAPITemplateForVerbOption(Operation.POST);
        response.setErrorResponseBodyType(null);
        response.setArrayForJsonExpression(true);

        // a constant path resolver groups both twins under one (path, verb)
        List<OperationErrorSpec> specs = ErrorResponseSchemas.buildSpecs(
                List.of(request, response), t -> "/orders", id -> "CM" + id);

        assertEquals(1, specs.size(), "Request + Response twins collapse into one spec");
        OperationErrorSpec spec = specs.get(0);
        assertEquals("/orders", spec.pathKey);
        assertEquals("post", spec.verbKey);
        assertEquals(OpenAPIErrorResponseBodyType.PROBLEM_DETAILS, spec.bodyType, "any non-NONE twin wins");
        assertTrue(spec.array, "array is true if any twin is an array");

        // guard: null templates -> empty
        assertTrue(ErrorResponseSchemas.buildSpecs(null, t -> "/x", id -> "n").isEmpty());
    }

    @Test
    void buildSpecs_confirmMessageTwin_resolvesConfirmSchemaNameFromTopLevelAsbiepId() {
        OpenAPITemplateForVerbOption request = new OpenAPITemplateForVerbOption(Operation.POST);
        request.setErrorResponseBodyType("CONFIRM_MESSAGE");
        request.setConfirmMessageTopLevelAsbiepId(BigInteger.valueOf(7));
        OpenAPITemplateForVerbOption response = new OpenAPITemplateForVerbOption(Operation.POST);
        response.setErrorResponseBodyType(null);

        List<OperationErrorSpec> specs = ErrorResponseSchemas.buildSpecs(
                List.of(request, response), t -> "/orders", id -> "CM" + id);

        assertEquals(1, specs.size());
        OperationErrorSpec spec = specs.get(0);
        assertEquals(OpenAPIErrorResponseBodyType.CONFIRM_MESSAGE, spec.bodyType);
        assertEquals("CM7", spec.confirmSchemaName, "confirm schema name resolved from the shared asbiep id");
    }

    // ------------------------------------------------------------------ referenced-status pruning (#1347 B19)

    @Test
    @SuppressWarnings("unchecked")
    void inject_problemDetails_onlyReferencedStatuses_appearInComponentsResponses() {
        Map<String, Object> schemas = new LinkedHashMap<>();
        Map<String, Object> root = newRoot(schemas);
        Map<String, Object> success = new LinkedHashMap<>();
        success.put("200", new LinkedHashMap<>(Map.of("description", "OK")));
        putOperation(root, "/orders/{id}", "get", success);

        // a single GET-item PROBLEM_DETAILS operation -> matrix = {401,403,404,500,502,503,504}
        ErrorResponseSchemas.injectErrorResponses(root, schemas,
                List.of(new OperationErrorSpec("/orders/{id}", "get", false, OpenAPIErrorResponseBodyType.PROBLEM_DETAILS, null)), false);

        Map<String, Object> components = (Map<String, Object>) root.get("components");
        Map<String, Object> compResponses = (Map<String, Object>) components.get("responses");
        // exactly the seven referenced statuses are present, nothing more (no component bloat)
        assertEquals(7, compResponses.size());
        assertTrue(compResponses.containsKey("401_Unauthorized"));
        assertTrue(compResponses.containsKey("404_NotFound"));
        assertTrue(compResponses.containsKey("500_InternalServerError"));
        // statuses NOT in the get-item matrix must not leak a component
        assertFalse(compResponses.containsKey("400_BadRequest"));
        assertFalse(compResponses.containsKey("409_Conflict"));
        assertFalse(compResponses.containsKey("415_UnsupportedMediaType"));
        assertFalse(compResponses.containsKey("422_UnprocessableContent"));
    }

    // ------------------------------------------------------------------ ProblemDetails status type (#1347 B17/B18)

    @Test
    @SuppressWarnings("unchecked")
    void problemDetailsSchema_statusPropertyIsInteger_andTypeNotRequired() {
        Map<String, Object> schema = ErrorResponseSchemas.problemDetailsSchema(false);
        Map<String, Object> props = (Map<String, Object>) schema.get("properties");
        Map<String, Object> statusProp = (Map<String, Object>) props.get("status");
        assertEquals("integer", statusProp.get("type"), "status is an integer per RFC 9457 §3.1.2");
        assertFalse(((List<String>) schema.get("required")).contains("type"),
                "'type' defaults to about:blank, so it is not required");
    }
}
