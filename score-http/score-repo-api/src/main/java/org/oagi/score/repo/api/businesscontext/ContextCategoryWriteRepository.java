package org.oagi.score.repo.api.businesscontext;

import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.businesscontext.model.*;

public interface ContextCategoryWriteRepository {

    CreateContextCategoryResponse createContextCategory(
            CreateContextCategoryRequest request) throws ScoreDataAccessException;

    UpdateContextCategoryResponse updateContextCategory(
            UpdateContextCategoryRequest request) throws ScoreDataAccessException;

    DeleteContextCategoryResponse deleteContextCategory(
            DeleteContextCategoryRequest request) throws ScoreDataAccessException;

}
