package org.oagi.score.gateway.http.api.cc_management.controller.payload.acc;

import org.oagi.score.gateway.http.api.cc_management.model.AsccpOrBccpManifestId;

public record AccSequenceUpdateRequest(AsccpOrBccpManifestId item, AsccpOrBccpManifestId after) {
}
