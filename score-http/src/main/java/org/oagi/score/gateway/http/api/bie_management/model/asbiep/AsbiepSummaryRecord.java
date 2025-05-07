package org.oagi.score.gateway.http.api.bie_management.model.asbiep;

import jakarta.annotation.Nullable;
import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.BIE;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;
import org.oagi.score.gateway.http.common.util.ScoreGuidUtils;

public record AsbiepSummaryRecord(
        AsbiepId asbiepId,
        Guid guid,
        AsccpManifestId basedAsccpManifestId,
        AbieId roleOfAbieId,
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
            AsbiepId asbiepId, AsccpManifestId basedAsccpManifestId,
            AbieId roleOfAbieId,
            BieState state, TopLevelAsbiepId topLevelAsbiepId,
            UserSummaryRecord owner, WhoAndWhen created, WhoAndWhen lastUpdated) {
        return new Builder(asbiepId, basedAsccpManifestId, roleOfAbieId,
                state, topLevelAsbiepId, owner, created, lastUpdated);
    }

    public static class Builder {
        private final AsbiepId asbiepId;
        private final AsccpManifestId basedAsccpManifestId;
        private final AbieId roleOfAbieId;
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

        public Builder(AsbiepId asbiepId, AsccpManifestId basedAsccpManifestId,
                       AbieId roleOfAbieId, BieState state,
                       TopLevelAsbiepId ownerTopLevelAsbiepId,
                       UserSummaryRecord owner, WhoAndWhen created,
                       WhoAndWhen lastUpdated) {
            this.asbiepId = asbiepId;
            this.basedAsccpManifestId = basedAsccpManifestId;
            this.roleOfAbieId = roleOfAbieId;
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

        public AsbiepSummaryRecord build() {
            return new AsbiepSummaryRecord(asbiepId,
                    (guid != null) ? guid : new Guid(ScoreGuidUtils.randomGuid()),
                    basedAsccpManifestId, roleOfAbieId,
                    path, hashPath, definition, remark, bizTerm, displayName,
                    state, ownerTopLevelAsbiepId, owner, created, lastUpdated);
        }
    }
}