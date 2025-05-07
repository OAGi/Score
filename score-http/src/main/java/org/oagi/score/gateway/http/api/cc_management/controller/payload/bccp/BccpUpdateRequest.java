package org.oagi.score.gateway.http.api.cc_management.controller.payload.bccp;

import jakarta.annotation.Nullable;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;

public record BccpUpdateRequest(
        BccpManifestId bccpManifestId,
        @Nullable String propertyTerm,
        @Nullable Boolean nillable,
        @Nullable Boolean deprecated,
        @Nullable NamespaceId namespaceId,
        @Nullable String defaultValue,
        @Nullable String fixedValue,
        @Nullable String definition,
        @Nullable String definitionSource) {
}
