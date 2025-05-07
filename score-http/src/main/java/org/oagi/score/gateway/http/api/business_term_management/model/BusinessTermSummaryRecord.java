package org.oagi.score.gateway.http.api.business_term_management.model;

import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;

public record BusinessTermSummaryRecord(
        BusinessTermId businessTermId,
        Guid guid,
        String businessTerm,
        String definition,
        String comment,
        String externalReferenceUri,
        String externalReferenceId,
        WhoAndWhen created,
        WhoAndWhen lastUpdated) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private BusinessTermId businessTermId;
        private Guid guid;
        private String businessTerm;
        private String definition;
        private String comment;
        private String externalReferenceUri;
        private String externalReferenceId;
        private WhoAndWhen created;
        private WhoAndWhen lastUpdated;

        public Builder businessTermId(BusinessTermId businessTermId) {
            this.businessTermId = businessTermId;
            return this;
        }

        public Builder guid(Guid guid) {
            this.guid = guid;
            return this;
        }

        public Builder businessTerm(String businessTerm) {
            this.businessTerm = businessTerm;
            return this;
        }

        public Builder definition(String definition) {
            this.definition = definition;
            return this;
        }

        public Builder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public Builder externalReferenceUri(String externalReferenceUri) {
            this.externalReferenceUri = externalReferenceUri;
            return this;
        }

        public Builder externalReferenceId(String externalReferenceId) {
            this.externalReferenceId = externalReferenceId;
            return this;
        }

        public Builder created(WhoAndWhen created) {
            this.created = created;
            return this;
        }

        public Builder lastUpdated(WhoAndWhen lastUpdated) {
            this.lastUpdated = lastUpdated;
            return this;
        }

        public BusinessTermSummaryRecord build() {
            return new BusinessTermSummaryRecord(
                    businessTermId, guid, businessTerm, definition, comment,
                    externalReferenceUri, externalReferenceId, created, lastUpdated);
        }
    }
}
