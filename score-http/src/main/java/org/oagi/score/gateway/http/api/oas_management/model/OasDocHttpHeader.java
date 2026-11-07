package org.oagi.score.gateway.http.api.oas_management.model;

import lombok.Data;

import java.math.BigInteger;

/**
 * Issue #1791: an HTTP header of an OpenAPI document, such as {@code traceparent}. It is generated either as a request
 * parameter ({@code in} the query, a header, the path or a cookie) or as a header of every response of the operations it applies to. The two are separate
 * entries, so that each has its own description, schema, example and {@code required}.
 * Which headers a document offers is up to the client; the server knows none by name.
 */
@Data
public class OasDocHttpHeader {

    /** The operation the header applies to; {@code null} when it applies to every operation of the document. */
    private BigInteger oasOperationId;

    /** The name of the header. Header names are case-insensitive, but the name given here is emitted as is. */
    private String name;

    private String description;

    /** A Schema Object (a JSON object). It is kept in the column {@code schema_type_reference}. */
    private OasSchema schema;

    /**
     * The example of the Parameter Object and of the Header Object: any JSON value (a string, a number, an object...).
     * It is not the example of the Schema Object, which is a part of {@link #schema}. {@code null} when there is none.
     */
    private Object example;

    /** Whether the header is a request parameter or a header of the responses of the operation. */
    private Direction direction;

    /**
     * Where a request parameter is: {@code query}, {@code header}, {@code path} or {@code cookie} (the {@code in} of the
     * Parameter Object). Only a request parameter has it; {@code null} for a response header, which is always a header,
     * and, for a request parameter, {@code header}.
     */
    private String in;

    /**
     * How the value is serialized, when it is not by the default of the location: {@code matrix}, {@code label},
     * {@code simple}, {@code form}, {@code spaceDelimited}, {@code pipeDelimited} or {@code deepObject}. {@code null} for
     * the default. Not used with a {@link #contentMediaType}.
     */
    private String style;

    /** The {@code explode} of the style; {@code null} for the default of the style. Not used with a content media type. */
    private Boolean explode;

    /** Whether the value of a query parameter allows reserved characters. Only for a parameter in the query. */
    private boolean allowReserved;

    /**
     * The media type (for example {@code text/plain}) of the {@code content} that serializes the value instead of a
     * schema with a style, which OpenAPI does not recommend for a cookie. The schema and the example then are those of
     * the Media Type Object. {@code null} when the value is serialized with a schema.
     */
    private String contentMediaType;

    /** Whether the parameter or the header is mandatory (the {@code required} of the Parameter Object or of the Header Object). */
    private boolean required;

    public enum Direction {
        /** A request parameter, in the query, a header, the path or a cookie. */
        REQUEST,
        /** A header of every response of the operation. */
        RESPONSE
    }
}
