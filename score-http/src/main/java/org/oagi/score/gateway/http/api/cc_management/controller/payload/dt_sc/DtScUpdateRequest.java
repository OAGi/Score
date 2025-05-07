package org.oagi.score.gateway.http.api.cc_management.controller.payload.dt_sc;

import jakarta.annotation.Nullable;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;

import java.util.List;

public record DtScUpdateRequest(
        DtScManifestId dtScManifestId,
        @Nullable String objectClassTerm,
        @Nullable String propertyTerm,
        @Nullable String representationTerm,
        @Nullable Integer cardinalityMin,
        @Nullable Integer cardinalityMax,
        @Nullable String definition,
        @Nullable String definitionSource,
        @Nullable Boolean deprecated,
        @Nullable String defaultValue,
        @Nullable String fixedValue,
        @Nullable List<DtScAwdPriSummaryRecord> dtScAwdPriList) {

    public static Builder builder(DtScManifestId dtScManifestId) {
        return new Builder(dtScManifestId);
    }

    public static class Builder {
        private final DtScManifestId dtScManifestId;
        private String objectClassTerm;
        private String propertyTerm;
        private String representationTerm;
        private Integer cardinalityMin;
        private Integer cardinalityMax;
        private String definition;
        private String definitionSource;
        private Boolean deprecated;
        private String defaultValue;
        private String fixedValue;
        private List<DtScAwdPriSummaryRecord> dtScAwdPriList;

        public Builder(DtScManifestId dtScManifestId) {
            this.dtScManifestId = dtScManifestId;
        }

        public Builder objectClassTerm(String objectClassTerm) {
            this.objectClassTerm = objectClassTerm;
            return this;
        }

        public Builder propertyTerm(String propertyTerm) {
            this.propertyTerm = propertyTerm;
            return this;
        }

        public Builder representationTerm(String representationTerm) {
            this.representationTerm = representationTerm;
            return this;
        }

        public Builder cardinalityMin(Integer cardinalityMin) {
            this.cardinalityMin = cardinalityMin;
            return this;
        }

        public Builder cardinalityMax(Integer cardinalityMax) {
            this.cardinalityMax = cardinalityMax;
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

        public Builder deprecated(Boolean deprecated) {
            this.deprecated = deprecated;
            return this;
        }

        public Builder defaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder fixedValue(String fixedValue) {
            this.fixedValue = fixedValue;
            return this;
        }

        public Builder dtScAwdPriList(List<DtScAwdPriSummaryRecord> dtScAwdPriList) {
            this.dtScAwdPriList = dtScAwdPriList;
            return this;
        }

        public DtScUpdateRequest build() {
            return new DtScUpdateRequest(dtScManifestId, objectClassTerm, propertyTerm, representationTerm,
                    cardinalityMin, cardinalityMax, definition, definitionSource, deprecated,
                    defaultValue, fixedValue, dtScAwdPriList);
        }
    }
}
