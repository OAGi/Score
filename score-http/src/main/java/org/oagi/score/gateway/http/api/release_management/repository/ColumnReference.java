package org.oagi.score.gateway.http.api.release_management.repository;

/** A column of a table, as a foreign key refers to it or is made of it. */
public record ColumnReference(String table, String column) {
}
