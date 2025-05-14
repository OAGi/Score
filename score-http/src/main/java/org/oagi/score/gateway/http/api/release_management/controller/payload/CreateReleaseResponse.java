package org.oagi.score.gateway.http.api.release_management.controller.payload;

import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;

public record CreateReleaseResponse(ReleaseId releaseId, String status, String statusMessage) {
}
