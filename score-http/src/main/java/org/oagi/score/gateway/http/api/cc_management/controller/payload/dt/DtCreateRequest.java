package org.oagi.score.gateway.http.api.cc_management.controller.payload.dt;

import jakarta.annotation.Nullable;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;

public record DtCreateRequest(
        ReleaseId releaseId,
        DtManifestId basedDtManifestId,
        @Nullable String tag) {

    public static Builder builder(ReleaseId releaseId, DtManifestId basedDtManifestId) {
        return new Builder(releaseId, basedDtManifestId);
    }

    public static class Builder {
        private final ReleaseId releaseId;
        private final DtManifestId basedDtManifestId;
        private String tag;

        public Builder(ReleaseId releaseId, DtManifestId basedDtManifestId) {
            this.releaseId = releaseId;
            this.basedDtManifestId = basedDtManifestId;
        }

        public DtCreateRequest.Builder tag(String tag) {
            this.tag = tag;
            return this;
        }

        public DtCreateRequest build() {
            return new DtCreateRequest(releaseId, basedDtManifestId, tag);
        }
    }

}
