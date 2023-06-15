package org.oagi.score.repo.api.openapidoc;

import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.openapidoc.model.GetOasDocListRequest;
import org.oagi.score.repo.api.openapidoc.model.GetOasDocListResponse;
import org.oagi.score.repo.api.openapidoc.model.GetOasDocRequest;
import org.oagi.score.repo.api.openapidoc.model.GetOasDocResponse;

public interface OasDocReadRepository {
    GetOasDocResponse getOasDoc(GetOasDocRequest request) throws ScoreDataAccessException;

    GetOasDocListResponse getOasDocList(GetOasDocListRequest request) throws ScoreDataAccessException;
}
