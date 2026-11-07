package org.oagi.score.gateway.http.api.oas_management.service;

import org.oagi.score.gateway.http.api.oas_management.model.OasDocHttpHeader;

import org.oagi.score.gateway.http.api.oas_management.service.generate_openapi_expression.HttpHeaderObjects;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Issue #1791: what an HTTP header of an OpenAPI document may be. The schema and the example are not checked here: a
 * {@code OasSchema} cannot be anything but a JSON object, and an example is any JSON value.
 */
public final class OasDocHttpHeaderPolicy {

    // The token of RFC 9110, the characters a header name is made of.
    private static final Pattern HEADER_NAME = Pattern.compile("[!#$%&'*+.^_`|~0-9A-Za-z-]+");

    // The name of a path parameter is a template expression of the path: {name}.
    private static final Pattern PATH_PARAMETER_NAME = Pattern.compile("[^{}/\\s]+");

    private static final Set<String> LOCATIONS = Set.of("query", "header", "path", "cookie");

    // OpenAPI: such a parameter, or a response header named Content-Type, SHALL be ignored, so it is refused here.
    private static final Set<String> IGNORED_REQUEST_HEADERS = Set.of("accept", "content-type", "authorization");
    private static final String IGNORED_RESPONSE_HEADER = "content-type";

    private OasDocHttpHeaderPolicy() {
    }

    public static void validate(List<OasDocHttpHeader> headers) {
        validate(headers, null);
    }

    /**
     * Check the headers. When the OpenAPI version of the document is given ({@code 3.0}, {@code 3.1}...), each header is
     * also checked, as it is generated, against the JSON Schema that the OpenAPI Initiative publishes for the version.
     */
    public static void validate(List<OasDocHttpHeader> headers, String openApiVersion) {
        if (headers == null) {
            return;
        }
        Set<String> uses = new HashSet<>();
        for (OasDocHttpHeader header : headers) {
            if (header == null) {
                continue;
            }
            String name = header.getName() == null ? "" : header.getName().trim();
            header.setName(name);
            if (header.getDirection() == null) {
                throw new IllegalArgumentException("The HTTP header '" + name
                        + "' must be a request parameter or a response header.");
            }
            String in = validateLocation(header, name);
            validateSerialization(header, name, in);
            if (header.getSchema() == null) {
                throw new IllegalArgumentException("The schema of the HTTP header '" + name + "' is required.");
            }

            // a header name is case-insensitive, the name of a query parameter, a path parameter or a cookie is not
            String key = "header".equals(in) ? name.toLowerCase() : name;
            if (openApiVersion != null) {
                validateAgainstSpecification(header, name, in, OasSpecValidator.Version.of(openApiVersion));
            }
            if (!uses.add(header.getOasOperationId() + " " + header.getDirection() + " " + in + " " + key)) {
                throw new IllegalArgumentException("Duplicate HTTP header: '" + name + "'.");
            }
        }
    }

    // The types of value a style is defined for (OpenAPI, Style Values); the styles not named here are for every type.
    private static final Map<String, Set<String>> STYLE_TYPES = Map.of(
            "spaceDelimited", Set.of("array", "object"),
            "pipeDelimited", Set.of("array", "object"),
            "deepObject", Set.of("object"));

    /** What the specification says of the whole header, as the JSON Schema of the version says it. */
    private static void validateAgainstSpecification(OasDocHttpHeader header, String name, String in,
                                                      OasSpecValidator.Version version) {
        boolean request = header.getDirection() == OasDocHttpHeader.Direction.REQUEST;
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("description", "ok");
        Map<String, Object> operation = new LinkedHashMap<>();
        String path = "/x";
        if (request) {
            if ("path".equals(in)) {
                path = "/x/{" + name + "}";
            }
            operation.put("parameters", List.of(HttpHeaderObjects.parameter(header)));
        } else {
            response.put("headers", Map.of(name, HttpHeaderObjects.header(header)));
        }
        operation.put("responses", Map.of("200", response));
        Map<String, Object> document = new LinkedHashMap<>();
        document.put("openapi", version.number());
        document.put("info", Map.of("title", "t", "version", "1"));
        document.put("paths", Map.of(path, Map.of("get", operation)));

        List<String> problems = OasSpecValidator.validate(version, document);
        if (version == OasSpecValidator.Version.V3_0_4 && header.getSchema() != null) {
            // The JSON Schema of OpenAPI 3.0 lets a Reference Object have other fields, which the specification says SHALL be
            // ignored: whatever the user wrote next to the $ref would be lost without a word.
            String sibling = fieldNextToReference(header.getSchema().toMap());
            if (sibling != null) {
                problems = new ArrayList<>(problems);
                problems.add("/schema: a Reference Object ($ref) cannot have other fields in OpenAPI 3.0, they are ignored ('" + sibling + "')");
            }
        }
        if (!problems.isEmpty()) {
            throw new IllegalArgumentException("The " + (request ? "request parameter '" : "response header '") + name
                    + "' is not valid in OpenAPI " + version.number() + ": " + describe(problems, name));
        }
    }

    /**
     * The problems of the JSON Schema as the user can read them: where in the parameter or the header (not in the document
     * it was put in), and without the ones that only say that something deeper is wrong.
     */
    private static String describe(List<String> problems, String name) {
        List<String> told = new ArrayList<>();
        for (String problem : problems) {
            if (problem.contains("must be valid to one and only one schema") || problem.contains("is not evaluated and the schema does not allow")
                    || problem.contains("must be valid to at least one schema")) {
                continue;
            }
            String where = problem.substring(0, problem.indexOf(": "));
            String message = problem.substring(problem.indexOf(": ") + 2);
            where = where.replaceFirst("^/paths/[^/]+/get/parameters/0", "").replaceFirst("^/paths/[^/]+/get/responses/200/headers/[^/]+", "");
            where = where.replace("~1", "/").replace("~0", "~");
            told.add((where.isEmpty() ? "" : where.substring(1).replace('/', '.') + ": ") + message);
        }
        if (told.isEmpty()) {
            told = problems;
        }
        return String.join("; ", told.subList(0, Math.min(3, told.size())));
    }

    /** The first field that is next to a {@code $ref} anywhere in the schema, or {@code null}. */
    private static String fieldNextToReference(Object node) {
        if (node instanceof Map<?, ?> map) {
            if (map.get("$ref") instanceof String) {
                for (Object key : map.keySet()) {
                    if (!"$ref".equals(key) && !String.valueOf(key).startsWith("x-")) {
                        return String.valueOf(key);
                    }
                }
            }
            for (Object value : map.values()) {
                String found = fieldNextToReference(value);
                if (found != null) {
                    return found;
                }
            }
        } else if (node instanceof List<?> list) {
            for (Object value : list) {
                String found = fieldNextToReference(value);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    /** The type of the schema as the keyword says it ({@code type} is a string, or a list of them since OpenAPI 3.1). */
    private static Set<String> typesOf(OasDocHttpHeader header) {
        Object type = header.getSchema() == null ? null : header.getSchema().toMap().get("type");
        if (type instanceof String single) {
            return Set.of(single);
        }
        if (type instanceof List<?> list) {
            Set<String> types = new java.util.HashSet<>();
            list.forEach(t -> types.add(String.valueOf(t)));
            return types;
        }
        return Set.of();
    }

    // The styles OpenAPI offers for each location of a parameter. A header has the one style, simple.
    private static final Map<String, Set<String>> STYLES = Map.of(
            "query", Set.of("form", "spaceDelimited", "pipeDelimited", "deepObject"),
            "path", Set.of("matrix", "label", "simple"),
            "header", Set.of("simple"),
            "cookie", Set.of("form"));

    // type "/" subtype, as of RFC 6838; parameters of a media type are not kept here
    private static final Pattern MEDIA_TYPE = Pattern.compile("[A-Za-z0-9][A-Za-z0-9!#$&^_.+-]*/[A-Za-z0-9*][A-Za-z0-9!#$&^_.+*-]*");

    /** A value is serialized with a style or with content, not with both. */
    private static void validateSerialization(OasDocHttpHeader header, String name, String in) {
        String mediaType = header.getContentMediaType() == null ? null : header.getContentMediaType().trim();
        header.setContentMediaType(mediaType == null || mediaType.isEmpty() ? null : mediaType);
        boolean query = "query".equals(in) && header.getDirection() == OasDocHttpHeader.Direction.REQUEST;
        if (header.getContentMediaType() != null) {
            if (!MEDIA_TYPE.matcher(header.getContentMediaType()).matches()) {
                throw new IllegalArgumentException("Invalid media type of '" + name + "': '"
                        + header.getContentMediaType() + "'.");
            }
            if (header.getStyle() != null || header.getExplode() != null || header.isAllowReserved()) {
                throw new IllegalArgumentException("'" + name
                        + "' is serialized with content: it has no style, explode or allowReserved.");
            }
            return;
        }
        if (header.getStyle() != null) {
            if (!STYLES.get(in).contains(header.getStyle())) {
                throw new IllegalArgumentException("The style '" + header.getStyle() + "' of '" + name
                        + "' is not one of a parameter in the " + in + ": " + String.join(", ", new java.util.TreeSet<>(STYLES.get(in))) + ".");
            }
            Set<String> types = typesOf(header);
            Set<String> wanted = STYLE_TYPES.get(header.getStyle());
            if (wanted != null && !types.isEmpty() && types.stream().noneMatch(wanted::contains)) {
                throw new IllegalArgumentException("The style '" + header.getStyle() + "' of '" + name
                        + "' is for a value of type " + String.join(" or ", new java.util.TreeSet<>(wanted))
                        + ", but its schema is of type " + String.join(" or ", new java.util.TreeSet<>(types)) + ".");
            }
            // the default of explode is false for every style but form, and OpenAPI does not define deepObject without it
            if ("deepObject".equals(header.getStyle()) && !Boolean.TRUE.equals(header.getExplode())) {
                throw new IllegalArgumentException("A deepObject style of '" + name
                        + "' needs explode: OpenAPI does not define it without.");
            }
        }
        if (header.isAllowReserved() && !query) {
            throw new IllegalArgumentException("allowReserved of '" + name + "' only applies to a parameter in the query.");
        }
    }

    /**
     * Check the location and the name against it, and give the header what follows from it. Returns the location: the
     * one of a request parameter (a header when none is given), {@code header} for a response header.
     */
    private static String validateLocation(OasDocHttpHeader header, String name) {
        boolean request = header.getDirection() == OasDocHttpHeader.Direction.REQUEST;
        String in = request ? (header.getIn() == null ? "header" : header.getIn()) : "header";
        if (!LOCATIONS.contains(in)) {
            throw new IllegalArgumentException("The location of the parameter '" + name
                    + "' must be query, header, path or cookie, not '" + in + "'.");
        }
        header.setIn(request ? in : null);

        if (name.isEmpty()) {
            throw new IllegalArgumentException("The name of a parameter or a header is required.");
        }
        switch (in) {
            case "header" -> {
                if (!HEADER_NAME.matcher(name).matches()) {
                    throw new IllegalArgumentException("Invalid HTTP header name: '" + header.getName() + "'.");
                }
                String lower = name.toLowerCase();
                if (request ? IGNORED_REQUEST_HEADERS.contains(lower) : IGNORED_RESPONSE_HEADER.equals(lower)) {
                    throw new IllegalArgumentException("The " + (request ? "request" : "response") + " header '" + name
                            + "' is ignored by OpenAPI: it is defined by the specification itself.");
                }
            }
            case "path" -> {
                if (!PATH_PARAMETER_NAME.matcher(name).matches()) {
                    throw new IllegalArgumentException("Invalid path parameter name: '" + name + "'.");
                }
                // OpenAPI: a path parameter MUST be required
                header.setRequired(true);
            }
            default -> {
            }
        }
        return in;
    }
}
