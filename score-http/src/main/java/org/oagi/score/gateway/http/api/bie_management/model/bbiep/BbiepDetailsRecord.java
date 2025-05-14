package org.oagi.score.gateway.http.api.bie_management.model.bbiep;

import jakarta.annotation.Nullable;
import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.BIE;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.ValueConstraint;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;

public record BbiepDetailsRecord(
        BbiepId bbiepId,
        Guid guid,
        BccpSummaryRecord basedBccp,
        @Nullable String path,
        @Nullable String hashPath,
        @Nullable String definition,
        @Nullable String remark,
        @Nullable String bizTerm,
        @Nullable ValueConstraint valueConstraint,
        @Nullable String displayName,
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
        private BbiepId bbiepId;
        private Guid guid;
        private BccpSummaryRecord basedBccp;
        private String path;
        private String hashPath;
        private String definition;
        private String remark;
        private String bizTerm;
        private ValueConstraint valueConstraint;
        private String displayName;
        private TopLevelAsbiepSummaryRecord ownerTopLevelAsbiep;
        private UserSummaryRecord owner;
        private WhoAndWhen created;
        private WhoAndWhen lastUpdated;

        private Builder() {
        }

        public Builder bbiepId(BbiepId bbiepId) {
            this.bbiepId = bbiepId;
            return this;
        }

        public Builder guid(Guid guid) {
            this.guid = guid;
            return this;
        }

        public Builder basedBccp(BccpSummaryRecord basedBccp) {
            this.basedBccp = basedBccp;
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

        public Builder valueConstraint(ValueConstraint valueConstraint) {
            this.valueConstraint = valueConstraint;
            return this;
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
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

        public BbiepDetailsRecord build() {
            return new BbiepDetailsRecord(
                    bbiepId,
                    guid,
                    basedBccp,
                    path,
                    hashPath,
                    definition,
                    remark,
                    bizTerm,
                    valueConstraint,
                    displayName,
                    ownerTopLevelAsbiep,
                    owner,
                    created,
                    lastUpdated
            );
        }
    }

}
