package org.oagi.score.repo.api.openapidoc;

import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.openapidoc.model.*;

public interface BieForOasDocReadRepository {
    GetBieForOasDocResponse getBieForOasDoc(GetBieForOasDocRequest request) throws ScoreDataAccessException;

    GetBieForOasDocListResponse getBieForOasDocList(GetBieForOasDocListRequest request) throws ScoreDataAccessException;
}
