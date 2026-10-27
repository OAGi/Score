package org.oagi.score.gateway.http.api.validation;

/** One step of the address of a node: its kind (ACC, ASCC, ASCCP, BCC, BCCP, ...) and manifest id. */
public record PathSegment(String type, String manifestId) {
}
