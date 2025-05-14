package org.oagi.score.gateway.http.api.namespace_management.model;

public record NamespaceSummaryRecord(NamespaceId namespaceId, String uri, String prefix, boolean standard) {
}
