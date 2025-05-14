package org.oagi.score.gateway.http.api.release_management.controller.payload;

import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;

public record CreateReleaseRequest(
        LibraryId libraryId,
        NamespaceId namespaceId,
        String releaseNum,
        String releaseNote,
        String releaseLicense) {

    public CreateReleaseRequest(LibraryId libraryId, String releaseNum) {
        this(libraryId, null, releaseNum, null, null);
    }

}
