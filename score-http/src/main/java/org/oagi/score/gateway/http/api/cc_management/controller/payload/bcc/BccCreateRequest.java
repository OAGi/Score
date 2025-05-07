package org.oagi.score.gateway.http.api.cc_management.controller.payload.bcc;

import org.oagi.score.gateway.http.api.cc_management.model.Cardinality;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;

public record BccCreateRequest(
        AccManifestId accManifestId,
        BccpManifestId bccpManifestId,
        int pos,
        Cardinality cardinality) {

    public static Builder builder(AccManifestId accManifestId,
                                  BccpManifestId bccpManifestId) {
        return new Builder(accManifestId, bccpManifestId);
    }

    public static class Builder {
        private AccManifestId accManifestId;
        private BccpManifestId bccpManifestId;
        private int pos = -1;
        private int cardinalityMin = 0;
        private int cardinalityMax = -1;

        public Builder(AccManifestId accManifestId, BccpManifestId bccpManifestId) {
            this.accManifestId = accManifestId;
            this.bccpManifestId = bccpManifestId;
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

        public BccCreateRequest build() {
            return new BccCreateRequest(accManifestId, bccpManifestId, pos, new Cardinality(cardinalityMin, cardinalityMax));
        }
    }
}
