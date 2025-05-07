package org.oagi.score.gateway.http.api.oas_management.controller.payload;

import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.base.Request;

import java.math.BigInteger;

public class GetOasResponseTableRequest extends Request {
    private BigInteger oasOperationId;

    public GetOasResponseTableRequest(ScoreUser requester) {
        super(requester);
    }

    public BigInteger getOasOperationId() {
        return oasOperationId;
    }

    public void setOasOperationId(BigInteger oasOperationId) {
        this.oasOperationId = oasOperationId;
    }

    public GetOasResponseTableRequest withOasOperationId(BigInteger oasOperationId) {
        this.setOasOperationId(oasOperationId);
        return this;
    }

}
