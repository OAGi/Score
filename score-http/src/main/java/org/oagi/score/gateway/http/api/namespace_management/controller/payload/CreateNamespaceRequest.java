package org.oagi.score.gateway.http.api.namespace_management.controller.payload;

import org.oagi.score.gateway.http.api.library_management.model.LibraryId;

import static org.oagi.score.gateway.http.common.util.StringUtils.hasLength;

public record CreateNamespaceRequest(
        LibraryId libraryId,
        String uri,
        String prefix,
        String description) {

    public CreateNamespaceRequest(LibraryId libraryId, String uri, String prefix, String description) {
        this.libraryId = libraryId;
        this.uri = hasLength(uri) ? uri.trim() : null;
        this.prefix = hasLength(prefix) ? prefix.trim() : "";
        this.description = description;
    }
}
