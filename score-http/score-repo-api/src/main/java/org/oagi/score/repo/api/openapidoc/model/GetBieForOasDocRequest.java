package org.oagi.score.repo.api.openapidoc.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;

public class GetBieForOasDocRequest extends Request {
    private BigInteger oasDocId;
    private String businessContext;
    public GetBieForOasDocRequest(ScoreUser requester) {
        super(requester);
        this.oasDocId = oasDocId;
    }

    public BigInteger getOasDocId() {
        return oasDocId;
    }

    public void setOasDocId(BigInteger oasDocId) {
        this.oasDocId = oasDocId;
    }

    public String getBusinessContext() {
        return businessContext;
    }

    public void setBusinessContext(String businessContext) {
        this.businessContext = businessContext;
    }
    public GetBieForOasDocRequest withOasDocId(BigInteger oasDocId) {
        this.setOasDocId(oasDocId);
        return this;
    }
}
