package org.oagi.score.repo.api.openapidoc.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;

public class GetOasDocRequest extends Request {
    private BigInteger oasDocId;

    public GetOasDocRequest(ScoreUser requester) {
        super(requester);
    }

    public BigInteger getOasDocId() {
        return oasDocId;
    }

    public void setOasDocId(BigInteger oasDocId) {
        this.oasDocId = oasDocId;
    }

    public GetOasDocRequest withOasDocId(BigInteger oasDocId) {
        this.setOasDocId(oasDocId);
        return this;
    }
}
