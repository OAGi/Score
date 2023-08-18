package org.oagi.score.repo.api.openapidoc;

import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.openapidoc.model.*;

public interface OasDocReadRepository {
    GetOasDocResponse getOasDoc(GetOasDocRequest request) throws ScoreDataAccessException;

    GetOasDocListResponse getOasDocList(GetOasDocListRequest request) throws ScoreDataAccessException;

    GetOasOperationResponse getOasOperation(GetOasOperationRequest request) throws ScoreDataAccessException;

    GetOasRequestTableResponse getOasRequestTable(GetOasRequestTableRequest request) throws ScoreDataAccessException;

    GetOasResponseTableResponse getOasResponseTable(GetOasResponseTableRequest request) throws ScoreDataAccessException;
}
