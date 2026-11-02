package org.oagi.score.gateway.http.api.validation;

/** The subject a violation is about, when it is not the one that was asked for: its kind, its id and a name for the user. */
public record SubjectRef(SubjectType type, String id, String label) {
}
