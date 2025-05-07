package org.oagi.score.gateway.http.api.release_management.controller.payload;

import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;

public record UpdateReleaseRequest(
        ReleaseId releaseId,
        NamespaceId namespaceId,
        String releaseNum,
        String releaseNote,
        String releaseLicense) {

    // Copy constructor to create a new instance with a releaseId
    public UpdateReleaseRequest withReleaseId(ReleaseId releaseId) {
        return new UpdateReleaseRequest(releaseId, namespaceId, releaseNum, releaseNote, releaseLicense);
    }

}
