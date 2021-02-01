package org.oagi.score.repo.api.businesscontext.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;

public class GetContextSchemeRequest extends Request {

    private BigInteger contextSchemeId;

    public GetContextSchemeRequest(ScoreUser requester) {
        super(requester);
    }

    public BigInteger getContextSchemeId() {
        return contextSchemeId;
    }

    public void setContextSchemeId(BigInteger contextSchemeId) {
        this.contextSchemeId = contextSchemeId;
    }

    public GetContextSchemeRequest withContextSchemeId(BigInteger contextSchemeId) {
        this.setContextSchemeId(contextSchemeId);
        return this;
    }
}
