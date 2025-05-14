package org.oagi.score.gateway.http.api.bie_management.model.asbie;

import jakarta.annotation.Nullable;
import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.BIE;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieId;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepId;
import org.oagi.score.gateway.http.api.cc_management.model.Cardinality;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;
import org.oagi.score.gateway.http.common.util.ScoreGuidUtils;

public record AsbieSummaryRecord(
        AsbieId asbieId,
        Guid guid,
        AsccManifestId basedAsccManifestId,
        @Nullable String path,
        @Nullable String hashPath,
        @Nullable AbieId fromAbieId,
        @Nullable AsbiepId toAsbiepId,
        @Nullable Cardinality cardinality,
        @Nullable String definition,
        @Nullable String remark,
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

    public static Builder builder(AsbieId asbieId, AsccManifestId basedAsccManifestId,
                                  AbieId fromAbieId, AsbiepId toAsbiepId,
                                  BieState state, TopLevelAsbiepId ownerTopLevelAsbiepId,
                                  UserSummaryRecord owner, WhoAndWhen created, WhoAndWhen lastUpdated) {
        return new Builder(asbieId, basedAsccManifestId, fromAbieId, toAsbiepId,
                state, ownerTopLevelAsbiepId, owner, created, lastUpdated);
    }

    public static class Builder {
        private final AsbieId asbieId;
        private final AsccManifestId basedAsccManifestId;
        private final AbieId fromAbieId;
        private final AsbiepId toAsbiepId;
        private final BieState state;
        private final TopLevelAsbiepId ownerTopLevelAsbiepId;
        private final UserSummaryRecord owner;
        private final WhoAndWhen created;
        private final WhoAndWhen lastUpdated;

        private Guid guid;
        private String path;
        private String hashPath;
        private Cardinality cardinality;
        private String definition;
        private String remark;
        private Boolean nillable;
        private Boolean deprecated;
        private Boolean used;

        Builder(AsbieId asbieId, AsccManifestId basedAsccManifestId,
                AbieId fromAbieId, AsbiepId toAsbiepId,
                BieState state, TopLevelAsbiepId ownerTopLevelAsbiepId,
                UserSummaryRecord owner, WhoAndWhen created, WhoAndWhen lastUpdated) {
            this.asbieId = asbieId;
            this.basedAsccManifestId = basedAsccManifestId;
            this.fromAbieId = fromAbieId;
            this.toAsbiepId = toAsbiepId;
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

        public Builder definition(String definition) {
            this.definition = definition;
            return this;
        }

        public Builder remark(String remark) {
            this.remark = remark;
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

        public AsbieSummaryRecord build() {
            return new AsbieSummaryRecord(asbieId,
                    (guid != null) ? guid : new Guid(ScoreGuidUtils.randomGuid()),
                    basedAsccManifestId,
                    path, hashPath, fromAbieId, toAsbiepId, cardinality,
                    definition, remark, nillable, deprecated, used,
                    state, ownerTopLevelAsbiepId, owner, created, lastUpdated);
        }
    }
}
