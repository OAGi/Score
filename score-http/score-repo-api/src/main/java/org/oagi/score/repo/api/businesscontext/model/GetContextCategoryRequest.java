package org.oagi.score.repo.api.businesscontext.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;

public class GetContextCategoryRequest extends Request {

    private BigInteger contextCategoryId;

    public GetContextCategoryRequest(ScoreUser requester) {
        super(requester);
    }

    public BigInteger getContextCategoryId() {
        return contextCategoryId;
    }

    public void setContextCategoryId(BigInteger contextCategoryId) {
        this.contextCategoryId = contextCategoryId;
    }

    public GetContextCategoryRequest withContextCategoryId(BigInteger contextCategoryId) {
        this.setContextCategoryId(contextCategoryId);
        return this;
    }
}
