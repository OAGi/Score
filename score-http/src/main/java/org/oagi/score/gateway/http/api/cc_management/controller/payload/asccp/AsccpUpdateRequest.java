package org.oagi.score.gateway.http.api.cc_management.controller.payload.asccp;

import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;

public record AsccpUpdateRequest(
        AsccpManifestId asccpManifestId,
        String propertyTerm,
        String definition,
        String definitionSource,
        Boolean reusable,
        Boolean deprecated,
        Boolean nillable,
        NamespaceId namespaceId) {
}
