package org.oagi.score.repo.api.openapidoc.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

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
