package org.oagi.score.gateway.http.api.cc_management.controller.payload.ascc;

import jakarta.annotation.Nullable;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;

public record AsccUpdateRequest(AsccManifestId asccManifestId,
                                @Nullable Integer cardinalityMin,
                                @Nullable Integer cardinalityMax,
                                @Nullable String definition,
                                @Nullable String definitionSource,
                                @Nullable Boolean deprecated) {
}
