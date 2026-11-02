package org.oagi.score.gateway.http.api.validation;

/** What is validated: the kind of object and its id. */
public record ValidationSubject(SubjectType type, String id) {
}
