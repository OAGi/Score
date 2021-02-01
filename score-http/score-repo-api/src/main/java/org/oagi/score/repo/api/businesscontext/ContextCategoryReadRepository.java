package org.oagi.score.repo.api.businesscontext;

import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.businesscontext.model.GetContextCategoryListRequest;
import org.oagi.score.repo.api.businesscontext.model.GetContextCategoryListResponse;
import org.oagi.score.repo.api.businesscontext.model.GetContextCategoryRequest;
import org.oagi.score.repo.api.businesscontext.model.GetContextCategoryResponse;

public interface ContextCategoryReadRepository {

    GetContextCategoryResponse getContextCategory(
            GetContextCategoryRequest request) throws ScoreDataAccessException;

    GetContextCategoryListResponse getContextCategoryList(
            GetContextCategoryListRequest request) throws ScoreDataAccessException;

}
