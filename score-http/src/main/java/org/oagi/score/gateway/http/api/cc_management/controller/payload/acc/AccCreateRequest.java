package org.oagi.score.gateway.http.api.cc_management.controller.payload.acc;

import jakarta.annotation.Nullable;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccType;
import org.oagi.score.gateway.http.api.cc_management.model.acc.OagisComponentType;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;

import static org.oagi.score.gateway.http.api.cc_management.model.acc.OagisComponentType.Semantics;

public record AccCreateRequest(
        ReleaseId releaseId,
        @Nullable AccManifestId basedAccManifestId,
        @Nullable String initialObjectClassTerm,
        @Nullable OagisComponentType initialComponentType,
        @Nullable AccType initialType,
        @Nullable String initialDefinition,
        @Nullable NamespaceId namespaceId,
        @Nullable String tag) {

    public static Builder builder(ReleaseId releaseId) {
        return new Builder(releaseId);
    }

    public static class Builder {
        private final ReleaseId releaseId;
        private AccManifestId basedAccManifestId;
        private String initialObjectClassTerm = "Object Class Term";
        private OagisComponentType initialComponentType = Semantics;
        private AccType initialType = AccType.Default;
        private String initialDefinition;
        private NamespaceId namespaceId;
        private String tag;

        public Builder(ReleaseId releaseId) {
            this.releaseId = releaseId;
        }

        public Builder basedAccManifestId(AccManifestId basedAccManifestId) {
            this.basedAccManifestId = basedAccManifestId;
            return this;
        }

        public Builder initialObjectClassTerm(String initialObjectClassTerm) {
            this.initialObjectClassTerm = initialObjectClassTerm;
            return this;
        }

        public Builder initialComponentType(OagisComponentType initialComponentType) {
            this.initialComponentType = initialComponentType;
            return this;
        }

        public Builder initialType(AccType initialType) {
            this.initialType = initialType;
            return this;
        }

        public Builder initialDefinition(String initialDefinition) {
            this.initialDefinition = initialDefinition;
            return this;
        }

        public Builder namespaceId(NamespaceId namespaceId) {
            this.namespaceId = namespaceId;
            return this;
        }

        public Builder tag(String tag) {
            this.tag = tag;
            return this;
        }

        public AccCreateRequest build() {
            return new AccCreateRequest(releaseId, basedAccManifestId, initialObjectClassTerm,
                    initialComponentType, initialType, initialDefinition, namespaceId, tag);
        }
    }

}
