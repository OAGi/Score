package org.oagi.score.gateway.http.api.cc_management.controller.payload.ascc;

import org.oagi.score.gateway.http.api.cc_management.model.Cardinality;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;

public record AsccCreateRequest(
        AccManifestId accManifestId,
        AsccpManifestId asccpManifestId,
        int pos,
        Cardinality cardinality,
        boolean skipReusableCheck) {

    public static Builder builder(AccManifestId accManifestId,
                                  AsccpManifestId asccpManifestId) {
        return new Builder(accManifestId, asccpManifestId);
    }

    public static class Builder {
        private AccManifestId accManifestId;
        private AsccpManifestId asccpManifestId;
        private int pos = -1;
        private int cardinalityMin = 0;
        private int cardinalityMax = -1;
        private boolean skipReusableCheck = false;

        public Builder(AccManifestId accManifestId, AsccpManifestId asccpManifestId) {
            this.accManifestId = accManifestId;
            this.asccpManifestId = asccpManifestId;
        }

        public Builder pos(int pos) {
            this.pos = pos;
            return this;
        }

        public Builder cardinalityMin(int cardinalityMin) {
            this.cardinalityMin = cardinalityMin;
            return this;
        }

        public Builder cardinalityMax(int cardinalityMax) {
            this.cardinalityMax = cardinalityMax;
            return this;
        }

        public Builder skipReusableCheck(boolean skipReusableCheck) {
            this.skipReusableCheck = skipReusableCheck;
            return this;
        }

        public AsccCreateRequest build() {
            return new AsccCreateRequest(accManifestId, asccpManifestId,
                    pos, new Cardinality(cardinalityMin, cardinalityMax),
                    skipReusableCheck);
        }
    }
}
