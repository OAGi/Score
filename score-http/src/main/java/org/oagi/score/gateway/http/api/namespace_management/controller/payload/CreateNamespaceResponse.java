package org.oagi.score.gateway.http.api.namespace_management.controller.payload;

import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;

public record CreateNamespaceResponse(NamespaceId namespaceId, String status, String statusMessage) {
}
