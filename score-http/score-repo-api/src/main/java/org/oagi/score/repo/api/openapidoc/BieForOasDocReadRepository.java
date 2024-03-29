package org.oagi.score.repo.api.openapidoc;

import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.openapidoc.model.GetAssignedOasTagRequest;
import org.oagi.score.repo.api.openapidoc.model.GetAssignedOasTagResponse;
import org.oagi.score.repo.api.openapidoc.model.GetBieForOasDocRequest;
import org.oagi.score.repo.api.openapidoc.model.GetBieForOasDocResponse;

public interface BieForOasDocReadRepository {

    GetBieForOasDocResponse getBieForOasDoc(GetBieForOasDocRequest request) throws ScoreDataAccessException;

    GetAssignedOasTagResponse getAssignedOasTag(GetAssignedOasTagRequest request) throws ScoreDataAccessException;

}
