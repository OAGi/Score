package org.oagi.score.gateway.http.api.cc_management.controller.payload.asccp;

import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;

import java.util.List;

public record CreateOagisBodResponse(List<AsccpManifestId> manifestIdList) {
}
