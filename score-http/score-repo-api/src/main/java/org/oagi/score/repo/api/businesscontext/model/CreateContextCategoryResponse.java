package org.oagi.score.repo.api.businesscontext.model;

import org.oagi.score.repo.api.base.Response;

import java.math.BigInteger;

public class CreateContextCategoryResponse extends Response {

    private final BigInteger contextCategoryId;

    public CreateContextCategoryResponse(BigInteger contextCategoryId) {
        this.contextCategoryId = contextCategoryId;
    }

    public BigInteger getContextCategoryId() {
        return contextCategoryId;
    }

}
