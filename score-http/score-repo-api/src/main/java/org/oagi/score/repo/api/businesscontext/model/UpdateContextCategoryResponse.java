package org.oagi.score.repo.api.businesscontext.model;

import org.oagi.score.repo.api.base.Response;

import java.math.BigInteger;

public class UpdateContextCategoryResponse extends Response {

    private final BigInteger contextCategoryId;
    private final boolean changed;

    public UpdateContextCategoryResponse(BigInteger contextCategoryId, boolean changed) {
        this.contextCategoryId = contextCategoryId;
        this.changed = changed;
    }

    public BigInteger getContextCategoryId() {
        return contextCategoryId;
    }

    public boolean isChanged() {
        return changed;
    }
}
