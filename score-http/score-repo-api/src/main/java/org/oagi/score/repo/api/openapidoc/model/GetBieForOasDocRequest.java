package org.oagi.score.repo.api.openapidoc.model;

import org.oagi.score.repo.api.base.Request;

import java.math.BigInteger;

public class GetBieForOasDocRequest extends Request {
    private BigInteger topLevelAsbiepId;

    public GetBieForOasDocRequest(BigInteger topLevelAsbiepId) {
        this.topLevelAsbiepId = topLevelAsbiepId;
    }

    public BigInteger getTopLevelAsbiepId() {
        return topLevelAsbiepId;
    }

    public void setTopLevelAsbiepId(BigInteger topLevelAsbiepId) {
        this.topLevelAsbiepId = topLevelAsbiepId;
    }

    public GetBieForOasDocRequest withTopLevelAsbiepId(BigInteger topLevelAsbiepId) {
        this.setTopLevelAsbiepId(topLevelAsbiepId);
        return this;
    }
}
