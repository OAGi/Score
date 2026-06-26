package org.oagi.score.gateway.http.api.oas_management.service.generate_openapi_expression;

import org.oagi.score.gateway.http.api.oas_management.model.OpenAPIErrorResponseBodyType;
import org.oagi.score.gateway.http.api.oas_management.model.OpenAPITemplateForVerbOption;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Issue #1347: shared, version-aware helpers for the defaulted OAS Doc error responses.
 *
 * <p>All builders return plain mutable {@link LinkedHashMap}s so they merge cleanly into the
 * existing (immutable) generator maps and so the OpenAPI 3.0 and 3.1 generators cannot drift.
 * {@link #injectErrorResponses} is the single idempotent post-step both generators call after
 * their template loop: it merges (never overwrites) the matrix into each operation's
 * {@code responses}, and injects the shared ProblemDetails schema + reusable
 * {@code components.responses} once when any operation selects PROBLEM_DETAILS.
 *
 * <p>ProblemDetails has no nullable fields, so the 3.0 ({@code nullable:true}) vs 3.1
 * ({@code type:[T,"null"]}) divergence does not apply; the literal is identical in both versions.
 */
public final class ErrorResponseSchemas {

    private ErrorResponseSchemas() {
    }

    /**
     * One operation's resolved error-response inputs. The generator builds these from its templates
     * (it owns the path-name resolution and, for CONFIRM_MESSAGE, the confirm BIE's schema name).
     */
    public static final class OperationErrorSpec {
        public final String pathKey;     // the resolved key in root.paths (e.g. "/{version}/orders")
        public final String verbKey;     // lower-case HTTP method (get/post/put/patch/delete)
        public final boolean array;      // GET list (true) vs item (false); ignored for other verbs
        public final OpenAPIErrorResponseBodyType bodyType;
        public final String confirmSchemaName; // components.schemas name of the picked ConfirmMessage BIE, or null

        public OperationErrorSpec(String pathKey, String verbKey, boolean array,
                                  OpenAPIErrorResponseBodyType bodyType, String confirmSchemaName) {
            this.pathKey = pathKey;
            this.verbKey = verbKey;
            this.array = array;
            this.bodyType = bodyType;
            this.confirmSchemaName = confirmSchemaName;
        }
    }

    /**
     * Collapse an operation's templates (a POST owns a Request entry and a Response entry that share
     * one {@code oas_operation}) into one {@link OperationErrorSpec} per {@code (pathKey, verb)}. The
     * body type / ConfirmMessage BIE live on the shared operation, so any entry carrying them wins; the
     * array indicator is true if any entry is an array (GET list vs item). {@code confirmNameResolver}
     * may be null when the generator cannot yet resolve a ConfirmMessage schema name (then CONFIRM_MESSAGE
     * degrades to description-only).
     */
    public static List<OperationErrorSpec> buildSpecs(Collection<OpenAPITemplateForVerbOption> templates,
                                                      Function<OpenAPITemplateForVerbOption, String> pathResolver,
                                                      Function<BigInteger, String> confirmNameResolver) {
        List<OperationErrorSpec> specs = new ArrayList<>();
        if (templates == null) {
            return specs;
        }
        Map<String, List<OpenAPITemplateForVerbOption>> groups = new LinkedHashMap<>();
        for (OpenAPITemplateForVerbOption template : templates) {
            if (template.getVerbOption() == null) {
                continue;
            }
            String pathKey = pathResolver.apply(template);
            String verbKey = template.getVerbOption().name().toLowerCase();
            groups.computeIfAbsent(pathKey + " " + verbKey, k -> new ArrayList<>()).add(template);
        }
        for (List<OpenAPITemplateForVerbOption> group : groups.values()) {
            OpenAPITemplateForVerbOption rep = group.get(0);
            String pathKey = pathResolver.apply(rep);
            String verbKey = rep.getVerbOption().name().toLowerCase();
            boolean array = group.stream().anyMatch(OpenAPITemplateForVerbOption::isArrayForJsonExpression);
            OpenAPIErrorResponseBodyType bodyType = OpenAPIErrorResponseBodyType.NONE;
            BigInteger confirmId = null;
            for (OpenAPITemplateForVerbOption template : group) {
                OpenAPIErrorResponseBodyType bt = OpenAPIErrorResponseBodyType.from(template.getErrorResponseBodyType());
                if (bt != OpenAPIErrorResponseBodyType.NONE) {
                    bodyType = bt;
                }
                if (template.getConfirmMessageTopLevelAsbiepId() != null) {
                    confirmId = template.getConfirmMessageTopLevelAsbiepId();
                }
            }
            String confirmName = (bodyType == OpenAPIErrorResponseBodyType.CONFIRM_MESSAGE
                    && confirmId != null && confirmNameResolver != null)
                    ? confirmNameResolver.apply(confirmId) : null;
            specs.add(new OperationErrorSpec(pathKey, verbKey, array, bodyType, confirmName));
        }
        return specs;
    }

    // The canonical 4xx/5xx codes, in emission order.
    private static final int[] ORDERED_ERROR_STATUSES = {400, 401, 403, 404, 409, 415, 422, 500, 502, 503, 504};

    /**
     * The finalized default error-status matrix (issue #1347 §3.4), as the 4xx/5xx codes only
     * (success codes are emitted by the generator itself). Array indicator only affects GET
     * (collection vs item); all other verbs are array-independent. 415/422 are request-body errors,
     * so on DELETE they appear only for OpenAPI 3.1+ (DELETE carries a request body only in 3.1+ — #1610).
     */
    public static List<Integer> errorStatusMatrix(String verb, boolean isArray, boolean isOas31) {
        List<Integer> out = new ArrayList<>();
        if (verb == null) {
            return out;
        }
        String v = verb.trim().toLowerCase();
        for (int status : ORDERED_ERROR_STATUSES) {
            if (matrixContains(v, isArray, isOas31, status)) {
                out.add(status);
            }
        }
        return out;
    }

    private static boolean matrixContains(String verb, boolean isArray, boolean isOas31, int status) {
        switch (status) {
            case 400: // Bad Request: GET(list), POST, PUT, PATCH
                return ("get".equals(verb) && isArray) || isBodyVerb(verb);
            case 401: // Unauthorized: all
            case 403: // Forbidden: all
            case 500: // Internal Server Error: all
            case 502: // Bad Gateway: all
            case 503: // Service Unavailable: all
            case 504: // Gateway Timeout: all
                return isKnownVerb(verb);
            case 404: // Not Found: GET(item), PUT, PATCH, DELETE
                return ("get".equals(verb) && !isArray) || "put".equals(verb) || "patch".equals(verb) || "delete".equals(verb);
            case 409: // Conflict: POST, PUT, PATCH, DELETE
                return isBodyVerb(verb) || "delete".equals(verb);
            case 415: // Unsupported Media Type: POST, PUT, PATCH, DELETE(3.1+ only)
            case 422: // Unprocessable Content: POST, PUT, PATCH, DELETE(3.1+ only)
                return isBodyVerb(verb) || ("delete".equals(verb) && isOas31);
            default:
                return false;
        }
    }

    private static boolean isBodyVerb(String verb) {
        return "post".equals(verb) || "put".equals(verb) || "patch".equals(verb);
    }

    private static boolean isKnownVerb(String verb) {
        return "get".equals(verb) || isBodyVerb(verb) || "delete".equals(verb);
    }

    /** Human reason phrase for a status code (2xx success + the 4xx/5xx error set). */
    public static String reasonPhrase(int status) {
        switch (status) {
            case 200: return "OK";
            case 201: return "Created";
            case 202: return "Accepted";
            case 204: return "No Content";
            case 400: return "Bad Request";
            case 401: return "Unauthorized";
            case 403: return "Forbidden";
            case 404: return "Not Found";
            case 409: return "Conflict";
            case 415: return "Unsupported Media Type";
            case 422: return "Unprocessable Content";
            case 500: return "Internal Server Error";
            case 502: return "Bad Gateway";
            case 503: return "Service Unavailable";
            case 504: return "Gateway Timeout";
            default: return "";
        }
    }

    /** The reusable components.responses key for a status, e.g. {@code 400_BadRequest}. */
    public static String responseComponentName(int status) {
        return status + "_" + reasonPhrase(status).replace(" ", "");
    }

    /**
     * Human-readable {@code detail} text for an error status, mirroring the sample values in
     * issue #1347 (RFC 9457 §3.1.4 — "a human-readable explanation specific to this occurrence").
     * Used to populate the illustrative {@code example} on each ProblemDetails response.
     */
    public static String detailPhrase(int status) {
        switch (status) {
            case 400: return "The server cannot process the request due to malformed request syntax.";
            case 401: return "The request lacks valid authentication credentials.";
            case 403: return "The server understood the request but refuses to authorize it.";
            case 404: return "The origin server did not find a current representation for the target resource.";
            case 409: return "The request could not be completed due to a conflict with the current state of the resource.";
            case 415: return "The origin server refuses to service the request because the payload is in an unsupported media type.";
            case 422: return "The server was unable to process the contained instructions.";
            case 500: return "The server encountered an unexpected condition.";
            case 502: return "The server received an invalid response from an upstream server.";
            case 503: return "The server is currently unable to handle the request due to maintenance.";
            case 504: return "The server did not receive a timely response from an upstream server.";
            default: return "";
        }
    }

    /**
     * The illustrative ProblemDetails {@code example} for one status: {@code title}, {@code status}
     * (an {@code integer}, matching the schema — RFC 9457 §3.1.2), {@code detail}, and {@code instance}.
     * {@code type} is omitted because it defaults to {@code about:blank}. {@code instance} is left empty:
     * RFC 9457 §3.1.5 only requires a URI <em>reference</em> (not a full URL), and the actual occurrence
     * URI is runtime-specific, so the example carries an empty placeholder rather than a fictitious host.
     */
    public static Map<String, Object> problemExample(int status) {
        Map<String, Object> example = new LinkedHashMap<>();
        example.put("title", reasonPhrase(status));
        example.put("status", status);
        example.put("detail", detailPhrase(status));
        example.put("instance", "");
        return example;
    }

    /**
     * The hardcoded RFC 9457 ProblemDetails schema. Identical for 3.0 and 3.1 (no nullable fields);
     * the {@code is31} flag is accepted for API symmetry with the version-aware generators.
     */
    public static Map<String, Object> problemDetailsSchema(boolean is31) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("required", List.of("status", "title"));
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("type", orderedMap("type", "string", "format", "uri-reference", "default", "about:blank"));
        properties.put("title", orderedMap("type", "string"));
        properties.put("status", orderedMap("type", "integer"));
        properties.put("detail", orderedMap("type", "string"));
        properties.put("instance", orderedMap("type", "string", "format", "uri-reference"));
        schema.put("properties", properties);
        return schema;
    }

    /**
     * The single idempotent post-step (issue #1347 §5.6). For each operation spec, merges the matrix
     * error responses into the operation's {@code responses} map, never overwriting an existing
     * (success or already-present) code. PROBLEM_DETAILS responses {@code $ref} the reusable
     * {@code components.responses}; CONFIRM_MESSAGE inlines an {@code application/json} schema $ref;
     * NONE emits a description only. Running twice yields identical output.
     */
    public static void injectErrorResponses(Map<String, Object> root, Map<String, Object> schemas,
                                            List<OperationErrorSpec> specs, boolean is31) {
        if (root == null || specs == null || specs.isEmpty()) {
            return;
        }
        Object pathsObj = root.get("paths");
        if (!(pathsObj instanceof Map)) {
            return;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> paths = (Map<String, Object>) pathsObj;

        boolean problemDetailsUsed = false;
        Set<Integer> referencedProblemStatuses = new LinkedHashSet<>();

        for (OperationErrorSpec spec : specs) {
            Object pathItemObj = paths.get(spec.pathKey);
            if (!(pathItemObj instanceof Map)) {
                continue;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> pathItem = (Map<String, Object>) pathItemObj;
            Object opObj = pathItem.get(spec.verbKey);
            if (!(opObj instanceof Map)) {
                continue;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> operation = (Map<String, Object>) opObj;

            List<Integer> statuses = errorStatusMatrix(spec.verbKey, spec.array, is31);
            if (statuses.isEmpty()) {
                continue;
            }

            // Rebuild responses as a mutable map, preserving existing (success / already-present) entries.
            Map<String, Object> responses = new LinkedHashMap<>();
            Object existing = operation.get("responses");
            if (existing instanceof Map) {
                responses.putAll((Map<String, Object>) existing);
            }
            OpenAPIErrorResponseBodyType bodyType = (spec.bodyType != null) ? spec.bodyType : OpenAPIErrorResponseBodyType.NONE;
            for (int status : statuses) {
                String code = String.valueOf(status);
                if (responses.containsKey(code)) {
                    continue; // merge, never overwrite a success or already-configured response
                }
                if (bodyType == OpenAPIErrorResponseBodyType.PROBLEM_DETAILS) {
                    responses.put(code, orderedMap("$ref", "#/components/responses/" + responseComponentName(status)));
                    problemDetailsUsed = true;
                    referencedProblemStatuses.add(status);
                } else if (bodyType == OpenAPIErrorResponseBodyType.CONFIRM_MESSAGE && spec.confirmSchemaName != null) {
                    responses.put(code, confirmMessageResponse(status, spec.confirmSchemaName));
                } else {
                    // NONE, or CONFIRM_MESSAGE without a resolvable BIE schema: description only.
                    responses.put(code, descriptionOnlyResponse(status));
                }
            }
            operation.put("responses", responses);
        }

        if (problemDetailsUsed) {
            schemas.putIfAbsent("ProblemDetails", problemDetailsSchema(is31));
            injectProblemDetailsComponentResponses(root, referencedProblemStatuses);
        }
    }

    private static Map<String, Object> descriptionOnlyResponse(int status) {
        return orderedMap("description", reasonPhrase(status));
    }

    private static Map<String, Object> confirmMessageResponse(int status, String confirmSchemaName) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("description", reasonPhrase(status));
        response.put("content", orderedMap("application/json",
                orderedMap("schema", orderedMap("$ref", "#/components/schemas/" + confirmSchemaName))));
        return response;
    }

    private static Map<String, Object> problemResponseComponent(int status) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("description", reasonPhrase(status));
        // Media Type Object carries both the schema $ref and an illustrative example. The singular
        // `example` field is valid in OpenAPI 3.0 and 3.1 alike (the 3.1 `examples`-array rule applies
        // only inside schema objects), so this component stays identical across both versions.
        Map<String, Object> mediaType = new LinkedHashMap<>();
        mediaType.put("schema", orderedMap("$ref", "#/components/schemas/ProblemDetails"));
        mediaType.put("example", problemExample(status));
        response.put("content", orderedMap("application/problem+json", mediaType));
        return response;
    }

    /**
     * components is built as an immutable map at the generator's build site, so it is rebuilt fresh
     * here (preserving securitySchemes + schemas) with a mutable {@code responses} block appended,
     * then put back into the mutable {@code root}.
     */
    private static void injectProblemDetailsComponentResponses(Map<String, Object> root, Set<Integer> statuses) {
        Object componentsObj = root.get("components");
        Map<String, Object> newComponents = new LinkedHashMap<>();
        Map<String, Object> responsesComponent = new LinkedHashMap<>();
        if (componentsObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> components = (Map<String, Object>) componentsObj;
            newComponents.putAll(components);
            Object existingResponses = components.get("responses");
            if (existingResponses instanceof Map) {
                responsesComponent.putAll((Map<String, Object>) existingResponses);
            }
        }
        for (int status : statuses) {
            responsesComponent.putIfAbsent(responseComponentName(status), problemResponseComponent(status));
        }
        newComponents.put("responses", responsesComponent);
        root.put("components", newComponents);
    }

    // Small ordered-map builder for leaf objects (keeps emitted key order stable).
    private static Map<String, Object> orderedMap(Object... keyValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            map.put((String) keyValues[i], keyValues[i + 1]);
        }
        return map;
    }
}
