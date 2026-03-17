package org.oagi.score.gateway.http.api.bie_management.model.bie_package;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude
public record ContextSchemeManifest(String schemeId,
                                    String schemeName,
                                    String schemeAgencyId,
                                    String schemeVersionId) {
}
