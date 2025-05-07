package org.oagi.score.gateway.http.api.cc_management.model;

import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;

public record AsccpOrBccpManifestId(AsccpManifestId asccpManifestId,
                                    BccpManifestId bccpManifestId) {
}
