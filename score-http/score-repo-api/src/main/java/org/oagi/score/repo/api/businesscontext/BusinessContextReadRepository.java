package org.oagi.score.repo.api.businesscontext;

import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.businesscontext.model.GetBusinessContextListRequest;
import org.oagi.score.repo.api.businesscontext.model.GetBusinessContextListResponse;
import org.oagi.score.repo.api.businesscontext.model.GetBusinessContextRequest;
import org.oagi.score.repo.api.businesscontext.model.GetBusinessContextResponse;

public interface BusinessContextReadRepository {

    GetBusinessContextResponse getBusinessContext(
            GetBusinessContextRequest request) throws ScoreDataAccessException;

    GetBusinessContextListResponse getBusinessContextList(
            GetBusinessContextListRequest request) throws ScoreDataAccessException;
    
}
