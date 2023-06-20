package org.oagi.score.repo.api.openapidoc;

import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.openapidoc.model.GetBieForOasDocListRequest;
import org.oagi.score.repo.api.openapidoc.model.GetBieForOasDocListResponse;
import org.oagi.score.repo.api.openapidoc.model.GetBieForOasDocRequest;
import org.oagi.score.repo.api.openapidoc.model.GetBieForOasDocResponse;

public interface BieForOasDocReadRepository {
    GetBieForOasDocResponse getBieForOasDoc(GetBieForOasDocRequest request) throws ScoreDataAccessException;

    GetBieForOasDocListResponse getBieForOasDocList(GetBieForOasDocListRequest request) throws ScoreDataAccessException;
}
