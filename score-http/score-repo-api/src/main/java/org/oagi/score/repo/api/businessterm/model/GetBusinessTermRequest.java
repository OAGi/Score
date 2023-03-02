package org.oagi.score.repo.api.businessterm.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;

public class GetBusinessTermRequest extends Request {

    private BigInteger businessTermId;

    public GetBusinessTermRequest(ScoreUser requester) {
        super(requester);
    }

    public BigInteger getBusinessTermId() {
        return businessTermId;
    }

    public void setBusinessTermId(BigInteger businessTermId) {
        this.businessTermId = businessTermId;
    }

    public GetBusinessTermRequest withBusinessTermId(BigInteger businessTermId) {
        this.setBusinessTermId(businessTermId);
        return this;
    }
}
