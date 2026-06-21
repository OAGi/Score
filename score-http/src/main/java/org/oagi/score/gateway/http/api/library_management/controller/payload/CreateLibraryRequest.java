package org.oagi.score.gateway.http.api.library_management.controller.payload;

import static org.oagi.score.gateway.http.common.util.StringUtils.hasLength;

public record CreateLibraryRequest(
        String type,
        String name,
        String organization,
        String link,
        String domain,
        String description,
        String namespaceUri,
        String namespacePrefix) {

    public CreateLibraryRequest(
            String type,
            String name,
            String organization,
            String link,
            String domain,
            String description,
            String namespaceUri,
            String namespacePrefix) {
        this.type = type;
        this.name = name;
        this.organization = organization;
        this.link = link;
        this.domain = domain;
        this.description = description;
        this.namespaceUri = hasLength(namespaceUri) ? namespaceUri.trim() : null;
        this.namespacePrefix = hasLength(namespacePrefix) ? namespacePrefix.trim() : "";
    }
}
