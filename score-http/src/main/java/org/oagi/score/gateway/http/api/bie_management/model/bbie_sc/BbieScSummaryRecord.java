package org.oagi.score.gateway.http.api.bie_management.model.bbie_sc;

import jakarta.annotation.Nullable;
import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.*;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieId;
import org.oagi.score.gateway.http.api.cc_management.model.Cardinality;
import org.oagi.score.gateway.http.api.cc_management.model.ValueConstraint;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;
import org.oagi.score.gateway.http.common.util.ScoreGuidUtils;

public record BbieScSummaryRecord(
        BbieScId bbieScId,
        Guid guid,
        DtScManifestId basedDtScManifestId,
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
        BieState state,
        TopLevelAsbiepId ownerTopLevelAsbiepId,
        UserSummaryRecord owner,
        WhoAndWhen created,
        WhoAndWhen lastUpdated) implements BIE {

    public Guid getGuid() {
        return guid();
    }

    public static Builder builder(BbieScId bbieScId, DtScManifestId basedDtScManifestId,
                                  BbieId bbieId, BieState state, TopLevelAsbiepId ownerTopLevelAsbiepId,
                                  UserSummaryRecord owner, WhoAndWhen created, WhoAndWhen lastUpdated) {
        return new Builder(bbieScId, basedDtScManifestId, bbieId, state, ownerTopLevelAsbiepId,
                owner, created, lastUpdated);
    }

    public static class Builder {
        private final BbieScId bbieScId;
        private final DtScManifestId basedDtScManifestId;
        private final BbieId bbieId;
        private final BieState state;
        private final TopLevelAsbiepId ownerTopLevelAsbiepId;
        private final UserSummaryRecord owner;
        private final WhoAndWhen created;
        private final WhoAndWhen lastUpdated;

        private Guid guid;
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

        Builder(BbieScId bbieScId, DtScManifestId basedDtScManifestId, BbieId bbieId,
                BieState state, TopLevelAsbiepId ownerTopLevelAsbiepId,
                UserSummaryRecord owner, WhoAndWhen created, WhoAndWhen lastUpdated) {
            this.bbieScId = bbieScId;
            this.basedDtScManifestId = basedDtScManifestId;
            this.bbieId = bbieId;
            this.state = state;
            this.ownerTopLevelAsbiepId = ownerTopLevelAsbiepId;
            this.owner = owner;
            this.created = created;
            this.lastUpdated = lastUpdated;
        }

        public Builder guid(Guid guid) {
            this.guid = guid;
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

        public BbieScSummaryRecord build() {
            return new BbieScSummaryRecord(bbieScId,
                    (guid != null) ? guid : new Guid(ScoreGuidUtils.randomGuid()),
                    basedDtScManifestId, bbieId,
                    path, hashPath, cardinality, primitiveRestriction, valueConstraint, facet,
                    definition, remark, bizTerm, displayName, example, deprecated, used,
                    state, ownerTopLevelAsbiepId, owner, created, lastUpdated);
        }
    }
}
