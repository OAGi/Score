package org.oagi.score.gateway.http.api.oas_management.service.generate_openapi_expression;

import org.oagi.score.gateway.http.api.oas_management.model.OasDocHttpHeader;
import org.oagi.score.gateway.http.api.oas_management.model.OasSchema;
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
 * Issue #1791: merges the HTTP headers of an OpenAPI document into the operations that were generated: a request
 * parameter or a header of every response. What was defined takes the place of what the generator made with the same
 * location and name; the rest of what the operation has stays. A header of the whole document is overridden by a header
 * of the same name given for one operation.
 */
final class HttpHeaderInjector {

    private static final String REF = "$ref";
    private static final String COMPONENT_RESPONSE_PREFIX = "#/components/responses/";

    private HttpHeaderInjector() {
    }

    @SuppressWarnings("unchecked")
    static void inject(Map<String, Object> root, Collection<OpenAPITemplateForVerbOption> templates,
                       Function<OpenAPITemplateForVerbOption, String> pathResolver,
                       List<OasDocHttpHeader> headers) {
        if (root == null || templates == null || headers == null || headers.isEmpty()
                || !(root.get("paths") instanceof Map)) {
            return;
        }
        Map<String, Object> paths = (Map<String, Object>) root.get("paths");

        // One operation of the document may be generated from several templates (a Request and a Response entry).
        Map<String, Set<BigInteger>> operationIds = new LinkedHashMap<>();
        for (OpenAPITemplateForVerbOption template : templates) {
            if (template.getVerbOption() == null) {
                continue;
            }
            Set<BigInteger> ids = operationIds.computeIfAbsent(
                    pathResolver.apply(template) + "\n" + template.getVerbOption().name().toLowerCase(),
                    k -> new LinkedHashSet<>());
            if (template.getOasOperationId() != null) {
                ids.add(template.getOasOperationId().value());
            }
        }

        checkReferences(root, headers);
        checkPathParameters(headers, operationIds);

        // What each operation gets, before anything is changed.
        List<Plan> plans = new ArrayList<>();
        for (Map.Entry<String, Set<BigInteger>> entry : operationIds.entrySet()) {
            String[] pathAndVerb = entry.getKey().split("\n");
            if (paths.get(pathAndVerb[0]) instanceof Map<?, ?> pathItem && pathItem.get(pathAndVerb[1]) instanceof Map) {
                plans.add(new Plan(pathAndVerb[0], pathAndVerb[1], (Map<String, Object>) pathItem.get(pathAndVerb[1]),
                        applicable(headers, entry.getValue(), true), applicable(headers, entry.getValue(), false)));
            }
        }

        // A response that operations share ({@code $ref}) carries the headers once, in the components, when every
        // operation that shares it has the same headers; when they differ, the operation that has headers gets its own
        // copy of the response, so that the other operations keep the shared one as it is.
        Map<String, Set<List<ResponseHeader>>> sharedBy = new LinkedHashMap<>();
        Map<String, List<OasDocHttpHeader>> sharedHeaders = new LinkedHashMap<>();
        for (Plan plan : plans) {
            for (Object response : responsesOf(plan.operation).values()) {
                String componentName = response instanceof Map<?, ?> map ? componentName((Map<String, Object>) map) : null;
                if (componentName != null) {
                    sharedBy.computeIfAbsent(componentName, k -> new LinkedHashSet<>()).add(signature(plan.response));
                    sharedHeaders.putIfAbsent(componentName, plan.response);
                }
            }
        }
        Set<String> carriedByTheComponent = new LinkedHashSet<>();
        for (Map.Entry<String, Set<List<ResponseHeader>>> shared : sharedBy.entrySet()) {
            if (shared.getValue().size() == 1 && !sharedHeaders.get(shared.getKey()).isEmpty()) {
                carriedByTheComponent.add(shared.getKey());
            }
        }

        for (Plan plan : plans) {
            if (!plan.request.isEmpty()) {
                List<Object> parameters = withParameters(plan.operation.get("parameters"), plan.request, plan.path, plan.verb);
                // nothing is added where every parameter was left out: no empty list of parameters
                if (!parameters.isEmpty() || plan.operation.containsKey("parameters")) {
                    putParameters(plan.operation, parameters);
                }
            }
            if (plan.response.isEmpty() || !(plan.operation.get("responses") instanceof Map)) {
                continue;
            }
            Map<String, Object> responses = new LinkedHashMap<>(responsesOf(plan.operation));
            for (Map.Entry<String, Object> each : responses.entrySet()) {
                if (!(each.getValue() instanceof Map)) {
                    continue;
                }
                Map<String, Object> value = (Map<String, Object>) each.getValue();
                String componentName = componentName(value);
                if (componentName == null) {
                    each.setValue(withHeaders(value, plan.response));
                } else if (!carriedByTheComponent.contains(componentName)) {
                    Map<String, Object> component = componentResponse(root, componentName);
                    if (component != null) {
                        each.setValue(withHeaders(component, plan.response));
                    }
                }
            }
            plan.operation.put("responses", responses);
        }

        for (String componentName : carriedByTheComponent) {
            Map<String, Object> component = componentResponse(root, componentName);
            if (component != null) {
                setComponentResponse(root, componentName, withHeaders(component, sharedHeaders.get(componentName)));
            }
        }
    }

    /**
     * The parameters of an operation come after its {@code operationId} and before its request body and responses, where
     * the generator has always put them; an operation that has them already keeps them where they are.
     */
    private static void putParameters(Map<String, Object> operation, List<Object> parameters) {
        if (operation.containsKey("parameters")) {
            operation.put("parameters", parameters);
            return;
        }
        Map<String, Object> entries = new LinkedHashMap<>(operation);
        boolean placed = false;
        operation.clear();
        for (Map.Entry<String, Object> entry : entries.entrySet()) {
            if (!placed && ("requestBody".equals(entry.getKey()) || "responses".equals(entry.getKey()))) {
                operation.put("parameters", parameters);
                placed = true;
            }
            operation.put(entry.getKey(), entry.getValue());
            if (!placed && "operationId".equals(entry.getKey())) {
                operation.put("parameters", parameters);
                placed = true;
            }
        }
        if (!placed) {
            operation.put("parameters", parameters);
        }
    }

    /**
     * A header whose schema refers to something that is not in the document would make the document refer to nothing: the
     * references to the document itself ({@code #/components/schemas/X}) have to be there. The other references are not
     * the document's to check.
     */
    @SuppressWarnings("unchecked")
    private static void checkReferences(Map<String, Object> root, List<OasDocHttpHeader> headers) {
        for (OasDocHttpHeader header : headers) {
            for (String ref : localReferences(header.getSchema().toMap(), new ArrayList<>())) {
                Object node = root;
                for (String token : ref.substring(1).split("/")) {
                    if (token.isEmpty()) {
                        continue;
                    }
                    String key = token.replace("~1", "/").replace("~0", "~");
                    node = node instanceof Map<?, ?> map ? map.get(key)
                            : (node instanceof List<?> list && key.matches("\\d+") && Integer.parseInt(key) < list.size()
                            ? list.get(Integer.parseInt(key)) : null);
                    if (node == null) {
                        throw new IllegalArgumentException("The schema of the HTTP header '" + header.getName()
                                + "' refers to '" + ref + "', which is not in the OpenAPI document.");
                    }
                }
            }
        }
    }

    private static List<String> localReferences(Object node, List<String> found) {
        if (node instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if ("$ref".equals(entry.getKey()) && entry.getValue() instanceof String ref && ref.startsWith("#")) {
                    found.add(ref);
                } else {
                    localReferences(entry.getValue(), found);
                }
            }
        } else if (node instanceof List<?> list) {
            list.forEach(e -> localReferences(e, found));
        }
        return found;
    }

    /**
     * A path parameter has to be a template expression of the path. One that is given for an operation has to be in the
     * path of that operation. One that is given for the whole document is only generated for the operations that have it
     * in their path, but has to be in the path of one of them.
     */
    private static void checkPathParameters(List<OasDocHttpHeader> headers, Map<String, Set<BigInteger>> operationIds) {
        for (OasDocHttpHeader header : headers) {
            if (header.getDirection() != OasDocHttpHeader.Direction.REQUEST || !"path".equals(header.getIn())) {
                continue;
            }
            String template = "{" + header.getName() + "}";
            if (header.getOasOperationId() == null) {
                if (!operationIds.isEmpty() && operationIds.keySet().stream().noneMatch(k -> k.split("\n")[0].contains(template))) {
                    throw new IllegalArgumentException("The path parameter '" + header.getName()
                            + "' of the document is in the path of none of its operations.");
                }
                continue;
            }
            for (Map.Entry<String, Set<BigInteger>> entry : operationIds.entrySet()) {
                String[] pathAndVerb = entry.getKey().split("\n");
                if (entry.getValue().contains(header.getOasOperationId()) && !pathAndVerb[0].contains(template)) {
                    throw new IllegalArgumentException("The path parameter '" + header.getName() + "' is not in the path '"
                            + pathAndVerb[0] + "' of the operation " + pathAndVerb[1].toUpperCase() + " it is given for.");
                }
            }
        }
    }

    /** One operation of the document, and the headers it is to get. */
    private record Plan(String path, String verb, Map<String, Object> operation, List<OasDocHttpHeader> request, List<OasDocHttpHeader> response) {
    }

    /** What makes the response header of one operation the same as the one of another, the operation aside. */
    private record ResponseHeader(String name, String description, OasSchema schema, Object example, boolean required,
                                    String style, Boolean explode, String contentMediaType) {
    }

    private static List<ResponseHeader> signature(List<OasDocHttpHeader> response) {
        return response.stream()
                .map(h -> new ResponseHeader(h.getName().toLowerCase(), h.getDescription(), h.getSchema(), h.getExample(),
                        h.isRequired(), h.getStyle(), h.getExplode(), h.getContentMediaType()))
                .sorted(java.util.Comparator.comparing(ResponseHeader::name))
                .toList();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> responsesOf(Map<String, Object> operation) {
        return operation.get("responses") instanceof Map ? (Map<String, Object>) operation.get("responses") : Map.of();
    }

    /** A header name is case-insensitive, the name of a query parameter, a path parameter or a cookie is not. */
    private static String key(String in, String name) {
        return in + " " + ("header".equals(in) ? name.toLowerCase() : name);
    }

    /** The headers of a direction for the operation, those of the operation first replacing those of the same name. */
    private static List<OasDocHttpHeader> applicable(List<OasDocHttpHeader> headers, Set<BigInteger> operationIds,
                                                     boolean request) {
        OasDocHttpHeader.Direction direction = request
                ? OasDocHttpHeader.Direction.REQUEST : OasDocHttpHeader.Direction.RESPONSE;
        Map<String, OasDocHttpHeader> byKey = new LinkedHashMap<>();
        for (OasDocHttpHeader header : headers) {
            if (header.getDirection() == direction && header.getOasOperationId() == null) {
                byKey.put(key(header), header);
            }
        }
        for (OasDocHttpHeader header : headers) {
            if (header.getDirection() == direction && header.getOasOperationId() != null
                    && operationIds.contains(header.getOasOperationId())) {
                byKey.put(key(header), header);
            }
        }
        return new ArrayList<>(byKey.values());
    }

    /** What a header is told apart by: its location and name (a response header is always in a header). */
    private static String key(OasDocHttpHeader header) {
        return key(header.getDirection() == OasDocHttpHeader.Direction.REQUEST && header.getIn() != null
                ? header.getIn() : "header", header.getName());
    }

    /**
     * A parameter that was defined for the document takes the place of the one the generator made with the same location
     * and name (such as {@code sinceLastDateTime}, or the path parameter of a path), so that the generated one can be
     * dropped from the generator some day. What the generator made and no definition covers stays as it is.
     */
    @SuppressWarnings("unchecked")
    private static List<Object> withParameters(Object existing, List<OasDocHttpHeader> headers, String path, String verb) {
        List<Object> parameters = new ArrayList<>();
        if (existing instanceof List<?> list) {
            parameters.addAll(list);
        }
        for (OasDocHttpHeader header : headers) {
            String in = header.getIn() == null ? "header" : header.getIn();
            // a path parameter has to be a template expression of the path
            if ("path".equals(in) && !path.contains("{" + header.getName() + "}")) {
                continue;
            }
            String key = key(in, header.getName());
            parameters.removeIf(p -> p instanceof Map<?, ?> map && map.get("in") instanceof String generatedIn
                    && key(generatedIn, String.valueOf(map.get("name"))).equals(key));
            parameters.add(HttpHeaderObjects.parameter(header));
        }
        return parameters;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> withHeaders(Map<String, Object> response, List<OasDocHttpHeader> headers) {
        Map<String, Object> result = new LinkedHashMap<>(response);
        Map<String, Object> responseHeaders = new LinkedHashMap<>();
        if (response.get("headers") instanceof Map) {
            responseHeaders.putAll((Map<String, Object>) response.get("headers"));
        }
        for (OasDocHttpHeader header : headers) {
            // a header that was defined takes the place of the one the response has with the same name
            responseHeaders.keySet().removeIf(k -> k.equalsIgnoreCase(header.getName()));
            responseHeaders.put(header.getName(), HttpHeaderObjects.header(header));
        }
        result.put("headers", responseHeaders);
        return result;
    }

    private static String componentName(Map<String, Object> response) {
        Object ref = response.get(REF);
        return (ref instanceof String s && s.startsWith(COMPONENT_RESPONSE_PREFIX))
                ? s.substring(COMPONENT_RESPONSE_PREFIX.length()) : null;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> componentResponse(Map<String, Object> root, String name) {
        if (root.get("components") instanceof Map<?, ?> components
                && components.get("responses") instanceof Map<?, ?> responses
                && responses.get(name) instanceof Map) {
            return (Map<String, Object>) responses.get(name);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static void setComponentResponse(Map<String, Object> root, String name, Map<String, Object> response) {
        Map<String, Object> components = new LinkedHashMap<>((Map<String, Object>) root.get("components"));
        Map<String, Object> responses = new LinkedHashMap<>((Map<String, Object>) components.get("responses"));
        responses.put(name, response);
        components.put("responses", responses);
        root.put("components", components);
    }
}
