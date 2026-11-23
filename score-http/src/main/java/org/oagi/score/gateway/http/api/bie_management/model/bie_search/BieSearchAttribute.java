package org.oagi.score.gateway.http.api.bie_management.model.bie_search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Optional;

/**
 * The attributes of a BIE node that are searched on the server: the text that differs from place to place and is too
 * heavy to travel with the search graph. The client names them by {@link #attributeName()}; which table and column each
 * one reads is decided in the repository, never by the request.
 */
public enum BieSearchAttribute {
    DEFINITION("definition"),
    REMARK("remark"),
    EXAMPLE("example"),
    FIXED_VALUE("fixed_value"),
    DEFAULT_VALUE("default_value"),
    /** The terms assigned to an ASBIE or a BBIE together with the `biz_term` text of the other nodes. */
    BUSINESS_TERM("business_term"),
    /** The type of value domain a BBIE or BBIE_SC restricts its value to: Primitive, Code List or Agency ID List. */
    VALUE_DOMAIN_RESTRICTION("value_domain_restriction"),
    /** The name of the primitive, code list or agency ID list that a BBIE or BBIE_SC restricts its value to. */
    VALUE_DOMAIN("value_domain");

    private final String attributeName;

    BieSearchAttribute(String attributeName) {
        this.attributeName = attributeName;
    }

    @JsonValue
    public String attributeName() {
        return attributeName;
    }

    /** What a request names an attribute by; a name that is none of them is refused. */
    @JsonCreator
    public static BieSearchAttribute from(String attributeName) {
        return of(attributeName)
                .orElseThrow(() -> new IllegalArgumentException("Unknown search attribute: " + attributeName));
    }

    public static Optional<BieSearchAttribute> of(String attributeName) {
        return Arrays.stream(values())
                .filter(a -> a.attributeName.equalsIgnoreCase(attributeName))
                .findFirst();
    }
}
