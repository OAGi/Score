package org.oagi.score.gateway.http.api.bie_management.model.bbie;

import jakarta.annotation.Nullable;
import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.*;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieId;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.BbiepId;
import org.oagi.score.gateway.http.api.cc_management.model.Cardinality;
import org.oagi.score.gateway.http.api.cc_management.model.ValueConstraint;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;
import org.oagi.score.gateway.http.common.util.ScoreGuidUtils;

public record BbieSummaryRecord(
        BbieId bbieId,
        Guid guid,
        BccManifestId basedBccManifestId,
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
        BieState state,
        TopLevelAsbiepId ownerTopLevelAsbiepId,
        UserSummaryRecord owner,
        WhoAndWhen created,
        WhoAndWhen lastUpdated) implements BIE {

    public Guid getGuid() {
        return guid();
    }

    public static Builder builder(BbieId bbieId, BccManifestId basedBccManifestId,
                                  AbieId fromAbieId, BbiepId toBbiepId,
                                  BieState state, TopLevelAsbiepId ownerTopLevelAsbiepId,
                                  UserSummaryRecord owner, WhoAndWhen created, WhoAndWhen lastUpdated) {
        return new Builder(bbieId, basedBccManifestId, fromAbieId, toBbiepId,
                state, ownerTopLevelAsbiepId, owner, created, lastUpdated);
    }

    public static class Builder {
        private final BbieId bbieId;
        private final BccManifestId basedBccManifestId;
        private final AbieId fromAbieId;
        private final BbiepId toBbiepId;
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
        private String example;
        private Boolean nillable;
        private Boolean deprecated;
        private Boolean used;

        Builder(BbieId bbieId, BccManifestId basedBccManifestId,
                AbieId fromAbieId, BbiepId toBbiepId,
                BieState state, TopLevelAsbiepId ownerTopLevelAsbiepId,
                UserSummaryRecord owner, WhoAndWhen created, WhoAndWhen lastUpdated) {
            this.bbieId = bbieId;
            this.basedBccManifestId = basedBccManifestId;
            this.fromAbieId = fromAbieId;
            this.toBbiepId = toBbiepId;
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

        public BbieSummaryRecord build() {
            return new BbieSummaryRecord(bbieId,
                    (guid != null) ? guid : new Guid(ScoreGuidUtils.randomGuid()),
                    basedBccManifestId,
                    path, hashPath, fromAbieId, toBbiepId, cardinality,
                    primitiveRestriction, valueConstraint, facet,
                    definition, remark, example, nillable, deprecated, used,
                    state, ownerTopLevelAsbiepId, owner, created, lastUpdated);
        }
    }
}