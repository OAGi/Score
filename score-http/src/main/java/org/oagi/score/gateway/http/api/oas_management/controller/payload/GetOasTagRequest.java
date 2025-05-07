package org.oagi.score.gateway.http.api.oas_management.controller.payload;

import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.base.Request;

import java.math.BigInteger;

public class GetOasTagRequest extends Request {
    private BigInteger oasTagId;

    public GetOasTagRequest(ScoreUser requester) {
        super(requester);
    }

    public BigInteger getOasTagId() {
        return oasTagId;
    }

    public void setOasTagId(BigInteger oasTagId) {
        this.oasTagId = oasTagId;
    }

    public GetOasTagRequest withOasTagId(BigInteger oasTagId) {
        this.setOasTagId(oasTagId);
        return this;
    }
}
