package org.oagi.score.gateway.http.api.cc_management.controller.payload.dt;

import jakarta.annotation.Nullable;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;

import java.util.List;

public record DtUpdateRequest(
        DtManifestId dtManifestId,
        @Nullable String qualifier,
        @Nullable String sixDigitId,
        @Nullable Boolean deprecated,
        @Nullable NamespaceId namespaceId,
        @Nullable String contentComponentDefinition,
        @Nullable String definition,
        @Nullable String definitionSource,
        @Nullable List<DtAwdPriSummaryRecord> dtAwdPriList) {

    public static Builder builder(DtManifestId dtManifestId) {
        return new Builder(dtManifestId);
    }

    public static class Builder {
        private final DtManifestId dtManifestId;
        private String qualifier;
        private String sixDigitId;
        private Boolean deprecated;
        private NamespaceId namespaceId;
        private String contentComponentDefinition;
        private String definition;
        private String definitionSource;
        private List<DtAwdPriSummaryRecord> dtAwdPriList;

        public Builder(DtManifestId dtManifestId) {
            this.dtManifestId = dtManifestId;
        }

        public Builder qualifier(String qualifier) {
            this.qualifier = qualifier;
            return this;
        }

        public Builder sixDigitId(String sixDigitId) {
            this.sixDigitId = sixDigitId;
            return this;
        }

        public Builder deprecated(Boolean deprecated) {
            this.deprecated = deprecated;
            return this;
        }

        public Builder namespaceId(NamespaceId namespaceId) {
            this.namespaceId = namespaceId;
            return this;
        }

        public Builder contentComponentDefinition(String contentComponentDefinition) {
            this.contentComponentDefinition = contentComponentDefinition;
            return this;
        }

        public Builder definition(String definition) {
            this.definition = definition;
            return this;
        }

        public Builder definitionSource(String definitionSource) {
            this.definitionSource = definitionSource;
            return this;
        }

        public Builder dtAwdPriList(List<DtAwdPriSummaryRecord> dtAwdPriList) {
            this.dtAwdPriList = dtAwdPriList;
            return this;
        }

        public DtUpdateRequest build() {
            return new DtUpdateRequest(dtManifestId, qualifier, sixDigitId, deprecated, namespaceId,
                    contentComponentDefinition, definition, definitionSource, dtAwdPriList);
        }
    }
}