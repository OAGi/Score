package org.oagi.score.gateway.http.api.cc_management.controller.payload.bcc;

import jakarta.annotation.Nullable;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.EntityType;

public record BccUpdateRequest(
        BccManifestId bccManifestId,
        @Nullable EntityType entityType,
        @Nullable Integer cardinalityMin,
        @Nullable Integer cardinalityMax,
        @Nullable String definition,
        @Nullable String definitionSource,
        @Nullable Boolean deprecated,
        @Nullable Boolean nillable,
        @Nullable String defaultValue,
        @Nullable String fixedValue) {
}
