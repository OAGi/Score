package org.oagi.score.repo.api.businesscontext.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class DeleteContextCategoriesRequest extends Request {

    private Collection<BigInteger> contextCategoryIds;

    public DeleteContextCategoriesRequest(ScoreUser requester) {
        super(requester);
    }

    public Collection<BigInteger> getContextCategoryIds() {
        return (contextCategoryIds == null) ? Collections.emptyList() : contextCategoryIds;
    }

    public void addContextCategoryId(BigInteger contextCategoryId) {
        if (contextCategoryId == null) {
            throw new IllegalArgumentException();
        }
        
        if (this.contextCategoryIds == null) {
            this.contextCategoryIds = new ArrayList();
        }

        this.contextCategoryIds.add(contextCategoryId);
    }

    public void setContextCategoryIds(Collection<BigInteger> contextCategoryIds) {
        this.contextCategoryIds = contextCategoryIds;
    }
}
