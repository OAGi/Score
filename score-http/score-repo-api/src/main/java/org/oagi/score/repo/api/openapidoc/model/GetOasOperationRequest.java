package org.oagi.score.repo.api.openapidoc.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;

public class GetOasOperationRequest extends Request {
    private BigInteger oasResourceId;
    public GetOasOperationRequest(ScoreUser requester){
        super(requester);
    }

    public BigInteger getOasResourceId() {
        return oasResourceId;
    }

    public void setOasResourceId(BigInteger oasResourceId) {
        this.oasResourceId = oasResourceId;
    }

    public GetOasOperationRequest withOasResourceId(BigInteger oasResourceId){
        this.setOasResourceId(oasResourceId);
        return this;
    }
}
