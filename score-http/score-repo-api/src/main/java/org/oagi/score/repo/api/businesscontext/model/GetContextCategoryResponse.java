package org.oagi.score.repo.api.businesscontext.model;

import org.oagi.score.repo.api.base.Response;

public class GetContextCategoryResponse extends Response {

    private final ContextCategory contextCategory;

    public GetContextCategoryResponse(ContextCategory contextCategory) {
        this.contextCategory = contextCategory;
    }

    public final ContextCategory getContextCategory() {
        return contextCategory;
    }
}
