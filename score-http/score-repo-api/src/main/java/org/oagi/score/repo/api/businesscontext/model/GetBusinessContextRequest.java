package org.oagi.score.repo.api.businesscontext.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;

public class GetBusinessContextRequest extends Request {

    private BigInteger businessContextId;

    public GetBusinessContextRequest(ScoreUser requester) {
        super(requester);
    }

    public BigInteger getBusinessContextId() {
        return businessContextId;
    }

    public void setBusinessContextId(BigInteger businessContextId) {
        this.businessContextId = businessContextId;
    }

    public GetBusinessContextRequest withBusinessContextId(BigInteger businessContextId) {
        this.setBusinessContextId(businessContextId);
        return this;
    }
}
