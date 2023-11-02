package org.oagi.score.repo.api.openapidoc.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

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
