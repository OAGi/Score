package org.oagi.score.gateway.http.common.model;

/** How the value of a server-side search predicate is compared with the text stored in the database. */
public enum TextSearchOperator {
    EQUALS,
    NOT_EQUALS,
    CONTAINS,
    STARTS_WITH,
    ENDS_WITH
}
