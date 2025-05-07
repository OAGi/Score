package org.oagi.score.gateway.http.api.cc_management.controller.payload.acc;

import jakarta.annotation.Nullable;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccType;
import org.oagi.score.gateway.http.api.cc_management.model.acc.OagisComponentType;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;

public record AccUpdateRequest(
        AccManifestId accManifestId,
        @Nullable AccType type,
        @Nullable String objectClassTerm,
        @Nullable OagisComponentType componentType,
        @Nullable String definition,
        @Nullable String definitionSource,
        @Nullable Boolean isAbstract,
        @Nullable Boolean deprecated,
        @Nullable NamespaceId namespaceId) {
}
