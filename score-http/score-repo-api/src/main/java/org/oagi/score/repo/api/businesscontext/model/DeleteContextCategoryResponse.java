package org.oagi.score.repo.api.businesscontext.model;

import org.oagi.score.repo.api.base.Response;

import java.math.BigInteger;
import java.util.List;

public class DeleteContextCategoryResponse extends Response {

    private final List<BigInteger> contextCategoryIdList;

    public DeleteContextCategoryResponse(List<BigInteger> contextCategoryIdList) {
        this.contextCategoryIdList = contextCategoryIdList;
    }

    public List<BigInteger> getContextCategoryIdList() {
        return contextCategoryIdList;
    }

    public boolean contains(BigInteger contextCategoryId) {
        return this.contextCategoryIdList.contains(contextCategoryId);
    }

    public boolean containsAll(List<BigInteger> contextCategoryIdList) {
        for (BigInteger contextCategoryId : contextCategoryIdList) {
            if (!this.contextCategoryIdList.contains(contextCategoryId)) {
                return false;
            }
        }
        return true;
    }
}
