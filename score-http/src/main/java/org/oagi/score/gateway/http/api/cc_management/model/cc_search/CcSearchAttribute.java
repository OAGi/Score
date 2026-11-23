package org.oagi.score.gateway.http.api.cc_management.model.cc_search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import static org.oagi.score.gateway.http.api.cc_management.model.cc_search.CcSearchType.*;

/**
 * The text of a core component that a tree search asks the server about. The client names an attribute by
 * {@link #attributeName()}; which tables and columns it reads is decided in the repository, never by the request.
 * An attribute is answered for every type of component that has the text, so the client picks the answer that fits the
 * node it judges (an association for a node that is tied to its parent by one, the component otherwise).
 */
public enum CcSearchAttribute {
    /** What a node is about: the definition of its association, else of its component. */
    DEFINITION("definition", EnumSet.of(ACC, ASCC, ASCCP, BCC, BCCP, DT, DT_SC)),
    DEFINITION_SOURCE("definition_source", EnumSet.of(ACC, ASCC, ASCCP, BCC, BCCP, DT, DT_SC)),
    /** The definition of the ASCC or BCC that ties a node to its parent. */
    ASSOCIATION_DEFINITION("association_definition", EnumSet.of(ASCC, BCC)),
    /** The definition of the ASCCP, BCCP or DT_SC a node shows. */
    COMPONENT_DEFINITION("component_definition", EnumSet.of(ASCCP, BCCP, DT_SC)),
    /** The definition of the type of a node: an ACC, or the data type of a BCCP. */
    TYPE_DEFINITION("type_definition", EnumSet.of(ACC, DT)),
    DEFAULT_VALUE("default_value", EnumSet.of(BCC, BCCP, DT_SC)),
    FIXED_VALUE("fixed_value", EnumSet.of(BCC, BCCP, DT_SC)),
    /** The definition of the content component of a data type. */
    CONTENT_COMPONENT_DEFINITION("content_component_definition", EnumSet.of(DT)),
    /**
     * The type (Primitive, Code List, Agency ID List) of the value domain a BCCP or a DT_SC starts with when a BIE has
     * stored nothing about it: the default of its data type. Not a column, so it is answered by its own query.
     */
    VALUE_DOMAIN_RESTRICTION("value_domain_restriction", EnumSet.of(BCCP, DT_SC)),
    /** The name of that default value domain. */
    VALUE_DOMAIN("value_domain", EnumSet.of(BCCP, DT_SC));

    private final String attributeName;
    private final Set<CcSearchType> types;

    CcSearchAttribute(String attributeName, Set<CcSearchType> types) {
        this.attributeName = attributeName;
        this.types = types;
    }

    @JsonValue
    public String attributeName() {
        return attributeName;
    }

    /** What a request names an attribute by; a name that is none of them is refused. */
    @JsonCreator
    public static CcSearchAttribute from(String attributeName) {
        return of(attributeName)
                .orElseThrow(() -> new IllegalArgumentException("Unknown search attribute: " + attributeName));
    }

    /** The types of component that have the text. */
    public Set<CcSearchType> types() {
        return types;
    }

    public static Optional<CcSearchAttribute> of(String attributeName) {
        return Arrays.stream(values())
                .filter(a -> a.attributeName.equalsIgnoreCase(attributeName))
                .findFirst();
    }
}
