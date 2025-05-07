package org.oagi.score.gateway.http.api.bie_management.model.abie;

import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.BIE;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;

public record AbieDetailsRecord(
        AbieId abieId,
        Guid guid,
        AccSummaryRecord basedAcc,
        String path,
        String hashPath,
        String definition,
        String remark,
        String bizTerm,
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

        private AbieId abieId;
        private Guid guid;
        private AccSummaryRecord basedAcc;
        private String path;
        private String hashPath;
        private String definition;
        private String remark;
        private String bizTerm;
        private TopLevelAsbiepSummaryRecord ownerTopLevelAsbiep;
        private UserSummaryRecord owner;
        private WhoAndWhen created;
        private WhoAndWhen lastUpdated;

        private Builder() {
        }

        public Builder abieId(AbieId abieId) {
            this.abieId = abieId;
            return this;
        }

        public Builder guid(Guid guid) {
            this.guid = guid;
            return this;
        }

        public Builder basedAcc(AccSummaryRecord basedAcc) {
            this.basedAcc = basedAcc;
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

        public AbieDetailsRecord build() {
            return new AbieDetailsRecord(
                    abieId,
                    guid,
                    basedAcc,
                    path,
                    hashPath,
                    definition,
                    remark,
                    bizTerm,
                    ownerTopLevelAsbiep,
                    owner,
                    created,
                    lastUpdated
            );
        }
    }
}
