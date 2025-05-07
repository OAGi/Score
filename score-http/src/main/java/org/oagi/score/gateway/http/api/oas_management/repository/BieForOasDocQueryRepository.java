package org.oagi.score.gateway.http.api.oas_management.repository;

import org.oagi.score.gateway.http.api.oas_management.controller.payload.GetAssignedOasTagRequest;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.GetAssignedOasTagResponse;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.GetBieForOasDocRequest;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.GetBieForOasDocResponse;
import org.oagi.score.gateway.http.common.model.base.ScoreDataAccessException;

public interface BieForOasDocQueryRepository {

    GetBieForOasDocResponse getBieForOasDoc(GetBieForOasDocRequest request) throws ScoreDataAccessException;

    GetAssignedOasTagResponse getAssignedOasTag(GetAssignedOasTagRequest request) throws ScoreDataAccessException;

}
