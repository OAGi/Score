package org.oagi.score.gateway.http.api.bie_management.model.bbie;

import jakarta.annotation.Nullable;
import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.BIE;
import org.oagi.score.gateway.http.api.bie_management.model.Facet;
import org.oagi.score.gateway.http.api.bie_management.model.PrimitiveRestriction;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieId;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.BbiepId;
import org.oagi.score.gateway.http.api.cc_management.model.Cardinality;
import org.oagi.score.gateway.http.api.cc_management.model.ValueConstraint;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;

public record BbieDetailsRecord(
        BbieId bbieId,
        Guid guid,
        BccSummaryRecord basedBcc,
        @Nullable String path,
        @Nullable String hashPath,
        @Nullable AbieId fromAbieId,
        @Nullable BbiepId toBbiepId,
        @Nullable Cardinality cardinality,
        @Nullable PrimitiveRestriction primitiveRestriction,
        @Nullable ValueConstraint valueConstraint,
        @Nullable Facet facet,
        @Nullable String definition,
        @Nullable String remark,
        @Nullable String example,
        @Nullable Boolean nillable,
        @Nullable Boolean deprecated,
        @Nullable Boolean used,
        TopLevelAsbiepSummaryRecord ownerTopLevelAsbiep,

        UserSummaryRecord owner,
        WhoAndWhen created,
        WhoAndWhen lastUpdated) implements BIE {

    public Guid getGuid() {
        return guid();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private BbieId bbieId;
        private Guid guid;
        private BccSummaryRecord basedBcc;
        private String path;
        private String hashPath;
        private AbieId fromAbieId;
        private BbiepId toBbiepId;
        private Cardinality cardinality;
        private PrimitiveRestriction primitiveRestriction;
        private ValueConstraint valueConstraint;
        private Facet facet;
        private String definition;
        private String remark;
        private String example;
        private Boolean nillable;
        private Boolean deprecated;
        private Boolean used;
        private TopLevelAsbiepSummaryRecord ownerTopLevelAsbiep;
        private UserSummaryRecord owner;
        private WhoAndWhen created;
        private WhoAndWhen lastUpdated;

        private Builder() {
        }

        public Builder bbieId(BbieId bbieId) {
            this.bbieId = bbieId;
            return this;
        }

        public Builder guid(Guid guid) {
            this.guid = guid;
            return this;
        }

        public Builder basedBcc(BccSummaryRecord basedBcc) {
            this.basedBcc = basedBcc;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder hashPath(String hashPath) {
            this.hashPath = hashPath;
            return this;
        }

        public Builder fromAbieId(AbieId fromAbieId) {
            this.fromAbieId = fromAbieId;
            return this;
        }

        public Builder toBbiepId(BbiepId toBbiepId) {
            this.toBbiepId = toBbiepId;
            return this;
        }

        public Builder cardinality(Cardinality cardinality) {
            this.cardinality = cardinality;
            return this;
        }

        public Builder primitiveRestriction(PrimitiveRestriction primitiveRestriction) {
            this.primitiveRestriction = primitiveRestriction;
            return this;
        }

        public Builder valueConstraint(ValueConstraint valueConstraint) {
            this.valueConstraint = valueConstraint;
            return this;
        }

        public Builder facet(Facet facet) {
            this.facet = facet;
            return this;
        }

        public Builder definition(String definition) {
            this.definition = definition;
            return this;
        }

        public Builder remark(String remark) {
            this.remark = remark;
            return this;
        }

        public Builder example(String example) {
            this.example = example;
            return this;
        }

        public Builder nillable(Boolean nillable) {
            this.nillable = nillable;
            return this;
        }

        public Builder deprecated(Boolean deprecated) {
            this.deprecated = deprecated;
            return this;
        }

        public Builder used(Boolean used) {
            this.used = used;
            return this;
        }

        public Builder ownerTopLevelAsbiep(TopLevelAsbiepSummaryRecord ownerTopLevelAsbiep) {
            this.ownerTopLevelAsbiep = ownerTopLevelAsbiep;
            return this;
        }

        public Builder owner(UserSummaryRecord owner) {
            this.owner = owner;
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

        public BbieDetailsRecord build() {
            return new BbieDetailsRecord(
                    bbieId,
                    guid,
                    basedBcc,
                    path,
                    hashPath,
                    fromAbieId,
                    toBbiepId,
                    cardinality,
                    primitiveRestriction,
                    valueConstraint,
                    facet,
                    definition,
                    remark,
                    example,
                    nillable,
                    deprecated,
                    used,
                    ownerTopLevelAsbiep,
                    owner,
                    created,
                    lastUpdated
            );
        }
    }

}