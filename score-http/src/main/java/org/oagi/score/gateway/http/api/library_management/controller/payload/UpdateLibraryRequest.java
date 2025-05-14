package org.oagi.score.gateway.http.api.library_management.controller.payload;

import org.oagi.score.gateway.http.api.library_management.model.LibraryId;

public record UpdateLibraryRequest(
        LibraryId libraryId,
        String type,
        String name,
        String organization,
        String link,
        String domain,
        String description) {

    // Copy constructor to create a new instance with a libraryId
    public UpdateLibraryRequest withLibraryId(LibraryId libraryId) {
        return new UpdateLibraryRequest(libraryId, type, name, organization, link, domain, description);
    }

}
