package org.oagi.score.gateway.http.api.cc_management.controller.payload.asccp;

import jakarta.annotation.Nullable;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.Definition;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpType;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;

import static org.oagi.score.gateway.http.api.cc_management.model.CcState.WIP;
import static org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpType.Default;

public record AsccpCreateRequest(
        ReleaseId releaseId,
        AccManifestId roleOfAccManifestId,
        @Nullable String initialPropertyTerm,
        @Nullable AsccpType asccpType,
        @Nullable Boolean reusable,
        @Nullable CcState initialState,
        @Nullable NamespaceId namespaceId,
        @Nullable Definition definition,
        @Nullable String tag) {

    public static Builder builder(ReleaseId releaseId, AccManifestId roleOfAccManifestId) {
        return new Builder(releaseId, roleOfAccManifestId);
    }

    public static class Builder {
        private final ReleaseId releaseId;
        private final AccManifestId roleOfAccManifestId;
        private String initialPropertyTerm;
        private AsccpType asccpType = Default;
        private Boolean reusable = true;
        private CcState initialState = WIP;
        private NamespaceId namespaceId;
        private Definition definition;
        private String tag;

        public Builder(ReleaseId releaseId, AccManifestId roleOfAccManifestId) {
            this.releaseId = releaseId;
            this.roleOfAccManifestId = roleOfAccManifestId;
        }

        public Builder initialPropertyTerm(String initialPropertyTerm) {
            this.initialPropertyTerm = initialPropertyTerm;
            return this;
        }

        public Builder asccpType(AsccpType asccpType) {
            this.asccpType = asccpType;
            return this;
        }

        public Builder reusable(Boolean reusable) {
            this.reusable = reusable;
            return this;
        }

        public Builder initialState(CcState initialState) {
            this.initialState = initialState;
            return this;
        }

        public Builder namespaceId(NamespaceId namespaceId) {
            this.namespaceId = namespaceId;
            return this;
        }

        public Builder definition(Definition definition) {
            this.definition = definition;
            return this;
        }

        public Builder tag(String tag) {
            this.tag = tag;
            return this;
        }

        public AsccpCreateRequest build() {
            return new AsccpCreateRequest(releaseId, roleOfAccManifestId, initialPropertyTerm,
                    asccpType, reusable, initialState, namespaceId, definition, tag);
        }
    }

}
