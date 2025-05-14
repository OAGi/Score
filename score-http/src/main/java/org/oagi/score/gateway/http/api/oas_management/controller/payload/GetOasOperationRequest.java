package org.oagi.score.gateway.http.api.oas_management.controller.payload;

import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.base.Request;

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
