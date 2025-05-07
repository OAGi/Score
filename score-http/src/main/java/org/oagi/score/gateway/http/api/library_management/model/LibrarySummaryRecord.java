package org.oagi.score.gateway.http.api.library_management.model;

public record LibrarySummaryRecord(LibraryId libraryId, String name, String state, boolean readOnly) {
}
