package org.oagi.score.repo.api.businesscontext;

import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.businesscontext.model.*;

public interface ContextSchemeReadRepository {

    GetContextSchemeResponse getContextScheme(
            GetContextSchemeRequest request) throws ScoreDataAccessException;

    GetContextSchemeListResponse getContextSchemeList(
            GetContextSchemeListRequest request) throws ScoreDataAccessException;

    GetContextSchemeValueListResponse getContextSchemeValueList(
            GetContextSchemeValueListRequest request) throws ScoreDataAccessException;

}
