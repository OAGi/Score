package org.oagi.score.gateway.http.api.cc_management.controller.payload.asccp;

import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;

public record CreateOagisVerbRequest(AccManifestId basedVerbAccManifestId) {
}
