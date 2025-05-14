package org.oagi.score.gateway.http.api.namespace_management.controller.payload;

import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;

import static org.oagi.score.gateway.http.common.util.StringUtils.hasLength;

public record UpdateNamespaceRequest(
        NamespaceId namespaceId,
        String uri,
        String prefix,
        String description) {

    public UpdateNamespaceRequest(NamespaceId namespaceId, String uri, String prefix, String description) {
        this.namespaceId = namespaceId;
        this.uri = hasLength(uri) ? uri.trim() : null;
        this.prefix = hasLength(prefix) ? prefix.trim() : "";
        this.description = description;
    }

    // Copy constructor to create a new instance with a namespaceId
    public UpdateNamespaceRequest withNamespaceId(NamespaceId namespaceId) {
        return new UpdateNamespaceRequest(namespaceId, uri, prefix, description);
    }

}
