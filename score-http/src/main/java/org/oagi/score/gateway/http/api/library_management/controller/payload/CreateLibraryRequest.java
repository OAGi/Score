package org.oagi.score.gateway.http.api.library_management.controller.payload;

public record CreateLibraryRequest(
        String type,
        String name,
        String organization,
        String link,
        String domain,
        String description) {
}
