package org.oagi.score.e2e.page.oas;

import org.apache.commons.io.FileUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A small, read-only view over a generated OpenAPI document (YAML) that was downloaded by the
 * 'Generate' action of the Edit OpenAPI Document page. It parses the file with SnakeYAML and
 * exposes assertion-friendly accessors (schema names, operation identifiers, security schemes,
 * request bodies, response codes, and {@code $ref} references) so that test cases can verify the
 * generated content without coupling to the backend generator code.
 */
public class OpenAPIDocumentExport {

    private final Map<String, Object> root;

    @SuppressWarnings("unchecked")
    public OpenAPIDocumentExport(File file) {
        try {
            String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            Object loaded = new Yaml().load(content);
            this.root = (loaded instanceof Map) ? (Map<String, Object>) loaded : new LinkedHashMap<>();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read generated OpenAPI document: " + file, e);
        }
    }

    public static OpenAPIDocumentExport from(File file) {
        return new OpenAPIDocumentExport(file);
    }

    public Map<String, Object> raw() {
        return root;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object o) {
        return (o instanceof Map) ? (Map<String, Object>) o : null;
    }

    @SuppressWarnings("unchecked")
    private static List<Object> asList(Object o) {
        return (o instanceof List) ? (List<Object>) o : null;
    }

    private Map<String, Object> components() {
        return asMap(root.get("components"));
    }

    /* ----------------------------------------------------------------- schemas */

    public Map<String, Object> schemas() {
        Map<String, Object> components = components();
        Map<String, Object> schemas = components == null ? null : asMap(components.get("schemas"));
        return schemas == null ? Collections.emptyMap() : schemas;
    }

    public Set<String> schemaNames() {
        return new LinkedHashSet<>(schemas().keySet());
    }

    public boolean hasSchema(String name) {
        return schemas().containsKey(name);
    }

    public Map<String, Object> schema(String name) {
        return asMap(schemas().get(name));
    }

    /**
     * Count how many declared schemas have a name that contains the given fragment.
     */
    public long countSchemaNamesContaining(String fragment) {
        return schemaNames().stream().filter(name -> name.contains(fragment)).count();
    }

    /**
     * The {@code items.$ref} of an array schema (e.g. the inner item that a {@code <BIEName>List}
     * array points to), or {@code null} when the schema is not an array or has no item reference.
     */
    public String schemaItemsRef(String schemaName) {
        Map<String, Object> schema = schema(schemaName);
        if (schema == null) {
            return null;
        }
        Map<String, Object> items = asMap(schema.get("items"));
        Object ref = items == null ? null : items.get("$ref");
        return (ref instanceof String) ? (String) ref : null;
    }

    /**
     * The schema object of one property of a named schema — {@code schemas.<schemaName>.properties.<propertyName>}
     * — or {@code null} when the schema or property is absent. (Issue #1610: lets a test read into the inner
     * shape of a generated BIE schema, e.g. a fixed-value or example-bearing BBIE property.)
     */
    public Map<String, Object> schemaProperty(String schemaName, String propertyName) {
        Map<String, Object> schema = schema(schemaName);
        Map<String, Object> properties = schema == null ? null : asMap(schema.get("properties"));
        return properties == null ? null : asMap(properties.get(propertyName));
    }

    /**
     * The {@code const} (fixed value) of a property schema — OpenAPI 3.1 / JSON Schema 2020-12 — or
     * {@code null} when the property is not a fixed value. The 3.0 generator emits a single-element
     * {@code enum} instead (see {@link #schemaProperty}).
     */
    public Object schemaConst(String schemaName, String propertyName) {
        Map<String, Object> property = schemaProperty(schemaName, propertyName);
        return property == null ? null : property.get("const");
    }

    /**
     * The {@code examples} array of a property schema (OpenAPI 3.1), or an empty list when absent.
     */
    public List<Object> schemaExamples(String schemaName, String propertyName) {
        Map<String, Object> property = schemaProperty(schemaName, propertyName);
        List<Object> examples = property == null ? null : asList(property.get("examples"));
        return examples == null ? Collections.emptyList() : examples;
    }

    /* -------------------------------------------------------------- security */

    public Map<String, Object> securitySchemes() {
        Map<String, Object> components = components();
        Map<String, Object> schemes = components == null ? null : asMap(components.get("securitySchemes"));
        return schemes == null ? Collections.emptyMap() : schemes;
    }

    public Set<String> securitySchemeNames() {
        return new LinkedHashSet<>(securitySchemes().keySet());
    }

    public Map<String, Object> securityScheme(String name) {
        return asMap(securitySchemes().get(name));
    }

    /**
     * The document (root) level {@code security} requirement, or an empty list when absent.
     */
    public List<Object> rootSecurity() {
        List<Object> security = asList(root.get("security"));
        return security == null ? Collections.emptyList() : security;
    }

    public boolean hasRootSecurity() {
        return root.containsKey("security");
    }

    /**
     * The {@code type} of a named security scheme (e.g. {@code apiKey}, {@code http}, {@code oauth2},
     * {@code openIdConnect}), or {@code null} when the scheme is absent.
     */
    public String securitySchemeType(String name) {
        Map<String, Object> scheme = securityScheme(name);
        Object type = scheme == null ? null : scheme.get("type");
        return type == null ? null : String.valueOf(type);
    }

    /**
     * A single field of a named security scheme (e.g. {@code in}/{@code name} for apiKey,
     * {@code scheme}/{@code bearerFormat} for http, {@code openIdConnectUrl} for openIdConnect).
     */
    public Object securitySchemeField(String name, String key) {
        Map<String, Object> scheme = securityScheme(name);
        return scheme == null ? null : scheme.get(key);
    }

    /**
     * The {@code flows} map of an oauth2 security scheme (keyed by flow type), or an empty map.
     */
    public Map<String, Object> oauth2Flows(String name) {
        Map<String, Object> scheme = securityScheme(name);
        Map<String, Object> flows = scheme == null ? null : asMap(scheme.get("flows"));
        return flows == null ? Collections.emptyMap() : flows;
    }

    /**
     * The {@code scopes} map (name -&gt; description) of one flow of an oauth2 scheme, or an empty map.
     */
    public Map<String, Object> oauth2FlowScopes(String name, String flowType) {
        Map<String, Object> flow = asMap(oauth2Flows(name).get(flowType));
        Map<String, Object> scopes = flow == null ? null : asMap(flow.get("scopes"));
        return scopes == null ? Collections.emptyMap() : scopes;
    }

    /**
     * The root-level keys of the document in their serialized (insertion) order. Useful to assert that
     * {@code security} is emitted right after {@code info}.
     */
    public List<String> rootKeys() {
        return new ArrayList<>(root.keySet());
    }

    /**
     * The position of a root-level key in serialized order, or {@code -1} when absent.
     */
    public int rootKeyIndex(String key) {
        return rootKeys().indexOf(key);
    }

    /**
     * The scheme names referenced by a {@code security} list (a list of Security Requirement Objects).
     * Each requirement is a map keyed by scheme name (ANDed); the list is the set of OR alternatives.
     */
    public List<String> securityListSchemeNames(List<Object> security) {
        List<String> names = new ArrayList<>();
        if (security != null) {
            for (Object requirement : security) {
                Map<String, Object> map = asMap(requirement);
                if (map != null) {
                    names.addAll(map.keySet());
                }
            }
        }
        return names;
    }

    /**
     * Whether a {@code security} list contains a requirement that references the given scheme name.
     */
    public boolean securityListReferencesScheme(List<Object> security, String schemeName) {
        return securityListSchemeNames(security).contains(schemeName);
    }

    /**
     * Whether a {@code security} list contains an anonymous requirement (an empty mapping {@code {}}).
     */
    public boolean securityListHasAnonymous(List<Object> security) {
        if (security == null) {
            return false;
        }
        for (Object requirement : security) {
            Map<String, Object> map = asMap(requirement);
            if (map != null && map.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * The per-operation {@code security} as a list of Security Requirement Objects, or an empty list.
     */
    public List<Object> operationSecurityList(String path, String method) {
        List<Object> list = asList(operationSecurity(path, method));
        return list == null ? Collections.emptyList() : list;
    }

    /**
     * Whether the operation's {@code security} is present and is an empty list ({@code security: []}),
     * which is the explicit "public" (no-auth) override.
     */
    public boolean operationSecurityIsPublic(String path, String method) {
        Object security = operationSecurity(path, method);
        return security instanceof List && ((List<?>) security).isEmpty();
    }

    /**
     * The scope strings selected for a scheme within an operation's {@code security}, or an empty list.
     */
    public List<String> operationSecurityScopes(String path, String method, String schemeName) {
        for (Object requirement : operationSecurityList(path, method)) {
            Map<String, Object> map = asMap(requirement);
            if (map != null && map.containsKey(schemeName)) {
                List<Object> scopes = asList(map.get(schemeName));
                List<String> result = new ArrayList<>();
                if (scopes != null) {
                    scopes.forEach(scope -> result.add(String.valueOf(scope)));
                }
                return result;
            }
        }
        return Collections.emptyList();
    }

    /**
     * Every per-operation {@code security} value declared anywhere under {@code paths}.
     */
    public List<Object> operationSecurityValuesUnderPaths() {
        List<Object> values = new ArrayList<>();
        collectByKey(paths(), "security", values::add);
        return values;
    }

    /**
     * All scheme names referenced by any per-operation {@code security} under {@code paths}.
     */
    public List<String> operationSecuritySchemeNamesUnderPaths() {
        List<String> names = new ArrayList<>();
        for (Object security : operationSecurityValuesUnderPaths()) {
            names.addAll(securityListSchemeNames(asList(security)));
        }
        return names;
    }

    /**
     * All scope strings referenced for a given scheme by any per-operation {@code security} under
     * {@code paths} (used to verify the legacy default OAuth2 per-operation scopes).
     */
    public List<String> operationSecurityScopesUnderPaths(String schemeName) {
        List<String> scopes = new ArrayList<>();
        for (Object security : operationSecurityValuesUnderPaths()) {
            List<Object> list = asList(security);
            if (list == null) {
                continue;
            }
            for (Object requirement : list) {
                Map<String, Object> map = asMap(requirement);
                if (map != null && map.containsKey(schemeName)) {
                    List<Object> schemeScopes = asList(map.get(schemeName));
                    if (schemeScopes != null) {
                        schemeScopes.forEach(scope -> scopes.add(String.valueOf(scope)));
                    }
                }
            }
        }
        return scopes;
    }

    /* ------------------------------------------------------------------ paths */

    public Map<String, Object> paths() {
        Map<String, Object> paths = asMap(root.get("paths"));
        return paths == null ? Collections.emptyMap() : paths;
    }

    public Set<String> pathNames() {
        return new LinkedHashSet<>(paths().keySet());
    }

    public boolean hasPath(String path) {
        return paths().containsKey(path);
    }

    /**
     * Return the operation object at the given path and HTTP method (method is case-insensitive),
     * or {@code null} when it is not present.
     */
    public Map<String, Object> operation(String path, String method) {
        Map<String, Object> pathItem = asMap(paths().get(path));
        if (pathItem == null) {
            return null;
        }
        return asMap(pathItem.get(method.toLowerCase()));
    }

    public boolean operationHasRequestBody(String path, String method) {
        Map<String, Object> operation = operation(path, method);
        return operation != null && operation.containsKey("requestBody");
    }

    public Object operationSecurity(String path, String method) {
        Map<String, Object> operation = operation(path, method);
        return operation == null ? null : operation.get("security");
    }

    public boolean operationHasSecurity(String path, String method) {
        Map<String, Object> operation = operation(path, method);
        return operation != null && operation.containsKey("security");
    }

    public Set<String> operationResponseCodes(String path, String method) {
        Map<String, Object> operation = operation(path, method);
        if (operation == null) {
            return Collections.emptySet();
        }
        Map<String, Object> responses = asMap(operation.get("responses"));
        if (responses == null) {
            return Collections.emptySet();
        }
        Set<String> codes = new LinkedHashSet<>();
        for (Object key : responses.keySet()) {
            codes.add(String.valueOf(key));
        }
        return codes;
    }

    public String responseDescription(String path, String method, String code) {
        Map<String, Object> operation = operation(path, method);
        if (operation == null) {
            return null;
        }
        Map<String, Object> responses = asMap(operation.get("responses"));
        if (responses == null) {
            return null;
        }
        for (Map.Entry<String, Object> entry : responses.entrySet()) {
            if (String.valueOf(entry.getKey()).equals(code)) {
                Map<String, Object> response = asMap(entry.getValue());
                Object description = response == null ? null : response.get("description");
                return (description instanceof String) ? (String) description : null;
            }
        }
        return null;
    }

    /**
     * The raw response object (a map) at the given path / method / status code, or {@code null} when
     * absent. The code is matched against the serialized key (which SnakeYAML may parse as an integer).
     */
    public Map<String, Object> responseNode(String path, String method, String code) {
        Map<String, Object> operation = operation(path, method);
        if (operation == null) {
            return null;
        }
        Map<String, Object> responses = asMap(operation.get("responses"));
        if (responses == null) {
            return null;
        }
        for (Map.Entry<String, Object> entry : responses.entrySet()) {
            if (String.valueOf(entry.getKey()).equals(code)) {
                return asMap(entry.getValue());
            }
        }
        return null;
    }

    /**
     * Whether the operation declares a response for the given status code.
     */
    public boolean operationHasResponse(String path, String method, String code) {
        return operationResponseCodes(path, method).contains(code);
    }

    /**
     * The {@code $ref} of a response object (e.g. a PROBLEM_DETAILS response referencing a reusable
     * {@code components.responses} entry), or {@code null} when the response is inline / absent.
     */
    public String responseRef(String path, String method, String code) {
        Map<String, Object> response = responseNode(path, method, code);
        Object ref = response == null ? null : response.get("$ref");
        return (ref instanceof String) ? (String) ref : null;
    }

    /**
     * Whether a response declares any {@code content} (a body). NONE responses are description-only.
     */
    public boolean responseHasContent(String path, String method, String code) {
        Map<String, Object> response = responseNode(path, method, code);
        return response != null && asMap(response.get("content")) != null;
    }

    /**
     * The media-type keys of a response's {@code content} (e.g. {@code application/json}), or empty.
     */
    public Set<String> responseMediaTypes(String path, String method, String code) {
        Map<String, Object> response = responseNode(path, method, code);
        Map<String, Object> content = response == null ? null : asMap(response.get("content"));
        return content == null ? Collections.emptySet() : new LinkedHashSet<>(content.keySet());
    }

    /**
     * The {@code content.<mediaType>.schema.$ref} of a response (e.g. a CONFIRM_MESSAGE response's
     * {@code application/json} schema reference), or {@code null} when absent.
     */
    public String responseContentSchemaRef(String path, String method, String code, String mediaType) {
        Map<String, Object> response = responseNode(path, method, code);
        Map<String, Object> content = response == null ? null : asMap(response.get("content"));
        Map<String, Object> media = content == null ? null : asMap(content.get(mediaType));
        Map<String, Object> schema = media == null ? null : asMap(media.get("schema"));
        Object ref = schema == null ? null : schema.get("$ref");
        return (ref instanceof String) ? (String) ref : null;
    }

    /**
     * The reusable {@code components.responses} map (used by PROBLEM_DETAILS error responses), or empty.
     */
    public Map<String, Object> componentResponses() {
        Map<String, Object> components = components();
        Map<String, Object> responses = components == null ? null : asMap(components.get("responses"));
        return responses == null ? Collections.emptyMap() : responses;
    }

    public boolean hasComponentResponse(String name) {
        return componentResponses().containsKey(name);
    }

    /**
     * The {@code content.<mediaType>.schema.$ref} of a reusable {@code components.responses} entry,
     * or {@code null} when absent.
     */
    public String componentResponseContentSchemaRef(String name, String mediaType) {
        Map<String, Object> response = asMap(componentResponses().get(name));
        Map<String, Object> content = response == null ? null : asMap(response.get("content"));
        Map<String, Object> media = content == null ? null : asMap(content.get(mediaType));
        Map<String, Object> schema = media == null ? null : asMap(media.get("schema"));
        Object ref = schema == null ? null : schema.get("$ref");
        return (ref instanceof String) ? (String) ref : null;
    }

    /* ------------------------------------------------------- deep collectors */

    /**
     * Every {@code operationId} value declared anywhere in the document.
     */
    public List<String> operationIds() {
        List<String> ids = new ArrayList<>();
        collectByKey(root, "operationId", value -> {
            if (value instanceof String) {
                ids.add((String) value);
            }
        });
        return ids;
    }

    /**
     * Every {@code $ref} reference string declared anywhere in the document.
     */
    public List<String> allRefs() {
        List<String> refs = new ArrayList<>();
        collectByKey(root, "$ref", value -> {
            if (value instanceof String) {
                refs.add((String) value);
            }
        });
        return refs;
    }

    public boolean anyRefEndsWith(String suffix) {
        return allRefs().stream().anyMatch(ref -> ref.endsWith(suffix));
    }

    /**
     * How many times the exact {@code $ref} string appears anywhere in the document. A value of two
     * or more indicates that a schema is shared (e.g. by an array's items and a non-array operation).
     */
    public long refCount(String ref) {
        return allRefs().stream().filter(r -> r.equals(ref)).count();
    }

    @SuppressWarnings("unchecked")
    private void collectByKey(Object node, String key, Consumer<Object> sink) {
        if (node instanceof Map) {
            for (Map.Entry<String, Object> entry : ((Map<String, Object>) node).entrySet()) {
                if (key.equals(entry.getKey())) {
                    sink.accept(entry.getValue());
                }
                collectByKey(entry.getValue(), key, sink);
            }
        } else if (node instanceof List) {
            for (Object item : (List<Object>) node) {
                collectByKey(item, key, sink);
            }
        }
    }
}
