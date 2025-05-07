package org.oagi.score.gateway.http.api.bie_management.model.asbie;

import jakarta.annotation.Nullable;
import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.BIE;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieId;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepId;
import org.oagi.score.gateway.http.api.cc_management.model.Cardinality;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;

public record AsbieDetailsRecord(
        AsbieId asbieId,
        Guid guid,
        AsccSummaryRecord basedAscc,
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
        private AsbieId asbieId;
        private Guid guid;
        private AsccSummaryRecord basedAscc;
        private String path;
        private String hashPath;
        private AbieId fromAbieId;
        private AsbiepId toAsbiepId;
        private Cardinality cardinality;
        private String definition;
        private String remark;
        private Boolean nillable;
        private Boolean deprecated;
        private Boolean used;
        private TopLevelAsbiepSummaryRecord ownerTopLevelAsbiep;
        private UserSummaryRecord owner;
        private WhoAndWhen created;
        private WhoAndWhen lastUpdated;

        private Builder() {
        }

        public Builder asbieId(AsbieId asbieId) {
            this.asbieId = asbieId;
            return this;
        }

        public Builder guid(Guid guid) {
            this.guid = guid;
            return this;
        }

        public Builder basedAscc(AsccSummaryRecord basedAscc) {
            this.basedAscc = basedAscc;
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

        public Builder toAsbiepId(AsbiepId toAsbiepId) {
            this.toAsbiepId = toAsbiepId;
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

        public AsbieDetailsRecord build() {
            return new AsbieDetailsRecord(
                    asbieId,
                    guid,
                    basedAscc,
                    path,
                    hashPath,
                    fromAbieId,
                    toAsbiepId,
                    cardinality,
                    definition,
                    remark,
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
