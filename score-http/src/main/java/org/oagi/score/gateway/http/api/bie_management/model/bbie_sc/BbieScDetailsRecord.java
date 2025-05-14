package org.oagi.score.gateway.http.api.bie_management.model.bbie_sc;

import jakarta.annotation.Nullable;
import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.BIE;
import org.oagi.score.gateway.http.api.bie_management.model.Facet;
import org.oagi.score.gateway.http.api.bie_management.model.PrimitiveRestriction;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieId;
import org.oagi.score.gateway.http.api.cc_management.model.Cardinality;
import org.oagi.score.gateway.http.api.cc_management.model.ValueConstraint;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;

public record BbieScDetailsRecord(
        BbieScId bbieScId,
        Guid guid,
        DtScSummaryRecord basedDtSc,
        BbieId bbieId,
        @Nullable String path,
        @Nullable String hashPath,
        @Nullable Cardinality cardinality,
        @Nullable PrimitiveRestriction primitiveRestriction,
        @Nullable ValueConstraint valueConstraint,
        @Nullable Facet facet,
        @Nullable String definition,
        @Nullable String remark,
        @Nullable String bizTerm,
        @Nullable String displayName,
        @Nullable String example,
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
        private BbieScId bbieScId;
        private Guid guid;
        private DtScSummaryRecord basedDtSc;
        private BbieId bbieId;
        private String path;
        private String hashPath;
        private Cardinality cardinality;
        private PrimitiveRestriction primitiveRestriction;
        private ValueConstraint valueConstraint;
        private Facet facet;
        private String definition;
        private String remark;
        private String bizTerm;
        private String displayName;
        private String example;
        private Boolean deprecated;
        private Boolean used;
        private TopLevelAsbiepSummaryRecord ownerTopLevelAsbiep;
        private UserSummaryRecord owner;
        private WhoAndWhen created;
        private WhoAndWhen lastUpdated;

        private Builder() {
        }

        public Builder bbieScId(BbieScId bbieScId) {
            this.bbieScId = bbieScId;
            return this;
        }

        public Builder guid(Guid guid) {
            this.guid = guid;
            return this;
        }

        public Builder basedDtSc(DtScSummaryRecord basedDtSc) {
            this.basedDtSc = basedDtSc;
            return this;
        }

        public Builder bbieId(BbieId bbieId) {
            this.bbieId = bbieId;
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

        public Builder bizTerm(String bizTerm) {
            this.bizTerm = bizTerm;
            return this;
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder example(String example) {
            this.example = example;
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

        public BbieScDetailsRecord build() {
            return new BbieScDetailsRecord(
                    bbieScId,
                    guid,
                    basedDtSc,
                    bbieId,
                    path,
                    hashPath,
                    cardinality,
                    primitiveRestriction,
                    valueConstraint,
                    facet,
                    definition,
                    remark,
                    bizTerm,
                    displayName,
                    example,
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
