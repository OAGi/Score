package org.oagi.score.gateway.http.api.oas_management.service.generate_openapi_expression;

import org.oagi.score.gateway.http.api.oas_management.model.OasDocHttpHeader;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Issue #1791: the Parameter Object and the Header Object of an HTTP header of an OpenAPI document, as they are
 * generated.
 */
public final class HttpHeaderObjects {

    private HttpHeaderObjects() {
    }

    /** The Parameter Object of a request parameter (the {@code in} is a header when the header has none). */
    public static Map<String, Object> parameter(OasDocHttpHeader header) {
        String in = header.getIn() == null ? "header" : header.getIn();
        Map<String, Object> parameter = new LinkedHashMap<>();
        parameter.put("name", header.getName());
        parameter.put("in", in);
        parameter.put("description", header.getDescription() == null ? "" : header.getDescription());
        // OpenAPI: the required of a path parameter MUST be true
        parameter.put("required", header.isRequired() || "path".equals(in));
        putSerialization(parameter, header, "query".equals(in));
        return parameter;
    }

    /** The Header Object of a response header. */
    public static Map<String, Object> header(OasDocHttpHeader header) {
        Map<String, Object> headerObject = new LinkedHashMap<>();
        headerObject.put("description", header.getDescription() == null ? "" : header.getDescription());
        headerObject.put("required", header.isRequired());
        putSerialization(headerObject, header, false);
        return headerObject;
    }

    /**
     * How the value is serialized: with {@code content}, a Media Type Object that has the schema and the example, or with
     * a schema, a style and the example of the Parameter Object or of the Header Object, which overrides the one of its
     * schema. The two exclude each other in OpenAPI.
     */
    private static void putSerialization(Map<String, Object> target, OasDocHttpHeader header, boolean query) {
        if (header.getContentMediaType() != null) {
            Map<String, Object> mediaType = new LinkedHashMap<>();
            mediaType.put("schema", header.getSchema().toMap());
            if (header.getExample() != null) {
                mediaType.put("example", header.getExample());
            }
            target.put("content", new LinkedHashMap<>(Map.of(header.getContentMediaType(), mediaType)));
            return;
        }
        if (header.getStyle() != null) {
            target.put("style", header.getStyle());
        }
        if (header.getExplode() != null) {
            target.put("explode", header.getExplode());
        }
        if (query && header.isAllowReserved()) {
            target.put("allowReserved", true);
        }
        target.put("schema", header.getSchema().toMap());
        if (header.getExample() != null) {
            target.put("example", header.getExample());
        }
    }

}
