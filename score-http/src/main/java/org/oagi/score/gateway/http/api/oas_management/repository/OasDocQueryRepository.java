package org.oagi.score.gateway.http.api.oas_management.repository;

import org.oagi.score.gateway.http.api.oas_management.controller.payload.*;
import org.oagi.score.gateway.http.api.oas_management.model.BieForOasDoc;
import org.oagi.score.gateway.http.api.oas_management.model.OasDoc;
import org.oagi.score.gateway.http.api.oas_management.repository.criteria.SelectBieForOasDocListArguments;
import org.oagi.score.gateway.http.common.model.PageResponse;
import org.oagi.score.gateway.http.common.model.base.ScoreDataAccessException;

public interface OasDocQueryRepository {

    GetOasDocResponse getOasDoc(GetOasDocRequest request) throws ScoreDataAccessException;

    GetOasDocListResponse getOasDocList(GetOasDocListRequest request) throws ScoreDataAccessException;

    GetOasOperationResponse getOasOperation(GetOasOperationRequest request) throws ScoreDataAccessException;

    GetOasRequestTableResponse getOasRequestTable(GetOasRequestTableRequest request) throws ScoreDataAccessException;

    GetOasResponseTableResponse getOasResponseTable(GetOasResponseTableRequest request) throws ScoreDataAccessException;

    boolean checkOasDocUniqueness(OasDoc oasDoc) throws ScoreDataAccessException;

    boolean checkOasDocTitleUniqueness(OasDoc oasDoc) throws ScoreDataAccessException;

    PageResponse<BieForOasDoc> selectBieForOasDocList(SelectBieForOasDocListArguments arguments) throws ScoreDataAccessException;

}
