package org.oagi.score.gateway.http.api.bie_management.model.abie;

import jakarta.annotation.Nullable;
import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.BIE;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;
import org.oagi.score.gateway.http.common.util.ScoreGuidUtils;

public record AbieSummaryRecord(
        AbieId abieId,
        Guid guid,
        AccManifestId basedAccManifestId,
        String path,
        String hashPath,
        @Nullable String definition,
        @Nullable String remark,
        @Nullable String bizTerm,
        BieState state,
        TopLevelAsbiepId ownerTopLevelAsbiepId,

        UserSummaryRecord owner,
        WhoAndWhen created,
        WhoAndWhen lastUpdated) implements BIE {

    public Guid getGuid() {
        return guid();
    }

    public static Builder builder(AbieId abieId, AccManifestId basedAccManifestId,
                                  BieState state, TopLevelAsbiepId ownerTopLevelAsbiepId,
                                  UserSummaryRecord owner,
                                  WhoAndWhen created, WhoAndWhen lastUpdated) {
        return new Builder(abieId, basedAccManifestId, state, ownerTopLevelAsbiepId,
                owner, created, lastUpdated);
    }

    public static class Builder {
        private final AbieId abieId;
        private final AccManifestId basedAccManifestId;
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

        Builder(AbieId abieId, AccManifestId basedAccManifestId,
                BieState state, TopLevelAsbiepId ownerTopLevelAsbiepId,
                UserSummaryRecord owner,
                WhoAndWhen created, WhoAndWhen lastUpdated) {
            this.abieId = abieId;
            this.basedAccManifestId = basedAccManifestId;
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

        public AbieSummaryRecord build() {
            return new AbieSummaryRecord(abieId,
                    (guid != null) ? guid : new Guid(ScoreGuidUtils.randomGuid()),
                    basedAccManifestId,
                    path, hashPath, definition, remark, bizTerm,
                    state, ownerTopLevelAsbiepId, owner, created, lastUpdated);
        }
    }

}
