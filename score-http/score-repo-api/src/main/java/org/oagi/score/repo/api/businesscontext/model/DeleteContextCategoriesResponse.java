package org.oagi.score.repo.api.businesscontext.model;

import org.oagi.score.repo.api.base.Response;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;

public class DeleteContextCategoriesResponse extends Response {

    private final Collection<BigInteger> contextCategoryIds;

    public DeleteContextCategoriesResponse(BigInteger contextCategoryId) {
        this(Arrays.asList(contextCategoryId));
    }

    public DeleteContextCategoriesResponse(BigInteger... contextCategoryIds) {
        this(Arrays.asList(contextCategoryIds));
    }

    public DeleteContextCategoriesResponse(Collection<BigInteger> contextCategoryIds) {
        this.contextCategoryIds = contextCategoryIds;
    }

    public Collection<BigInteger> getContextCategoryIds() {
        return contextCategoryIds;
    }
}
