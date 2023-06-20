package org.oagi.score.repo.api.openapidoc.model;

import org.oagi.score.repo.api.base.Request;

import java.math.BigInteger;

public class GetBieForOasDocRequest extends Request {
    private BigInteger bieForOasDocId;

    public GetBieForOasDocRequest(BigInteger bieForOasDocId) {
        this.bieForOasDocId = bieForOasDocId;
    }

    public BigInteger getBieForOasDocId() {
        return bieForOasDocId;
    }

    public void setBieForOasDocId(BigInteger bieForOasDocId) {
        this.bieForOasDocId = bieForOasDocId;
    }

    public GetBieForOasDocRequest withBieForOasDocId(BigInteger bieForOasDocId) {
        this.setBieForOasDocId(bieForOasDocId);
        return this;
    }
}
