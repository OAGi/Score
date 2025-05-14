package org.oagi.score.gateway.http.api.bie_management.model.bbiep;

import jakarta.annotation.Nullable;
import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.BIE;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;
import org.oagi.score.gateway.http.common.util.ScoreGuidUtils;

public record BbiepSummaryRecord(
        BbiepId bbiepId,
        Guid guid,
        BccpManifestId basedBccpManifestId,
        @Nullable String path,
        @Nullable String hashPath,
        @Nullable String definition,
        @Nullable String remark,
        @Nullable String bizTerm,
        @Nullable String displayName,
        BieState state,
        TopLevelAsbiepId ownerTopLevelAsbiepId,

        UserSummaryRecord owner,
        WhoAndWhen created,
        WhoAndWhen lastUpdated) implements BIE {

    public Guid getGuid() {
        return guid();
    }

    public static Builder builder(
            BbiepId bbiepId, BccpManifestId basedBccpManifestId,
            BieState state, TopLevelAsbiepId topLevelAsbiepId,
            UserSummaryRecord owner, WhoAndWhen created, WhoAndWhen lastUpdated) {
        return new Builder(bbiepId, basedBccpManifestId,
                state, topLevelAsbiepId, owner, created, lastUpdated);
    }

    public static class Builder {
        private final BbiepId bbiepId;
        private final BccpManifestId basedBccpManifestId;
        private final BieState state;
        private final TopLevelAsbiepId ownerTopLevelAsbiepId;
        private final UserSummaryRecord owner;
        private final WhoAndWhen created;
        private final WhoAndWhen lastUpdated;

        private Guid guid;
        private String path;
        private String hashPath;
        private String definition;
        private String remark;
        private String bizTerm;
        private String displayName;

        public Builder(BbiepId bbiepId, BccpManifestId basedBccpManifestId,
                       BieState state,
                       TopLevelAsbiepId ownerTopLevelAsbiepId,
                       UserSummaryRecord owner, WhoAndWhen created,
                       WhoAndWhen lastUpdated) {
            this.bbiepId = bbiepId;
            this.basedBccpManifestId = basedBccpManifestId;
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

        public BbiepSummaryRecord build() {
            return new BbiepSummaryRecord(bbiepId,
                    (guid != null) ? guid : new Guid(ScoreGuidUtils.randomGuid()),
                    basedBccpManifestId,
                    path, hashPath, definition, remark, bizTerm, displayName,
                    state, ownerTopLevelAsbiepId, owner, created, lastUpdated);
        }
    }

}
