package org.oagi.score.gateway.http.api.library_management.controller.payload;

import org.oagi.score.gateway.http.api.library_management.model.LibraryId;

public record CreateLibraryResponse(LibraryId namespaceId, String status, String statusMessage) {
}
