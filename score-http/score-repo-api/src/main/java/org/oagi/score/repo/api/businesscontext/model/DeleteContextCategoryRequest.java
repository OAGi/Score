package org.oagi.score.repo.api.businesscontext.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DeleteContextCategoryRequest extends Request {

    private List<BigInteger> contextCategoryIdList = Collections.emptyList();

    public DeleteContextCategoryRequest(ScoreUser requester) {
        super(requester);
    }

    public List<BigInteger> getContextCategoryIdList() {
        return contextCategoryIdList;
    }

    public void setContextCategoryId(BigInteger contextCategoryId) {
        if (contextCategoryId != null) {
            this.contextCategoryIdList = Arrays.asList(contextCategoryId);
        }
    }

    public DeleteContextCategoryRequest withContextCategoryId(BigInteger contextCategoryId) {
        this.setContextCategoryId(contextCategoryId);
        return this;
    }

    public void setContextCategoryIdList(List<BigInteger> contextCategoryIdList) {
        if (contextCategoryIdList != null) {
            this.contextCategoryIdList = contextCategoryIdList;
        }
    }

    public DeleteContextCategoryRequest withContextCategoryIdList(List<BigInteger> contextCategoryIdList) {
        this.setContextCategoryIdList(contextCategoryIdList);
        return this;
    }

}
