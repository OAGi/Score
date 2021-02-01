package org.oagi.score.repo.api.businesscontext;

import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.businesscontext.model.*;

public interface ContextSchemeWriteRepository {

    CreateContextSchemeResponse createContextScheme(
            CreateContextSchemeRequest request) throws ScoreDataAccessException;

    UpdateContextSchemeResponse updateContextScheme(
            UpdateContextSchemeRequest request) throws ScoreDataAccessException;

    DeleteContextSchemeResponse deleteContextScheme(
            DeleteContextSchemeRequest request) throws ScoreDataAccessException;
}
