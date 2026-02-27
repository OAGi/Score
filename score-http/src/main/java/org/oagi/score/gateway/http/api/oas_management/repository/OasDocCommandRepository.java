package org.oagi.score.gateway.http.api.oas_management.repository;

import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.*;
import org.oagi.score.gateway.http.api.oas_management.model.*;
import org.oagi.score.gateway.http.api.oas_management.repository.criteria.*;
import org.oagi.score.gateway.http.common.model.base.ScoreDataAccessException;

public interface OasDocCommandRepository {

    CreateOasDocResponse createOasDoc(CreateOasDocRequest request) throws ScoreDataAccessException;

    UpdateOasDocResponse updateOasDoc(UpdateOasDocRequest request) throws ScoreDataAccessException;

    DeleteOasDocResponse deleteOasDoc(DeleteOasDocRequest request) throws ScoreDataAccessException;

    OasMessageBodyId insertOasMessageBody(InsertOasMessageBodyArguments arguments);

    OasResourceId insertOasResource(InsertOasResourceArguments arguments);

    OasOperationId insertOasOperation(InsertOasOperationArguments arguments);

    OasTagId insertOasTag(InsertOasTagArguments arguments);

    OasResourceTagId insertOasResourceTag(InsertOasResourceTagArguments arguments);

    OasRequestId insertOasRequest(InsertOasRequestArguments arguments);

    OasResponseId insertOasResponse(InsertOasResponseArguments arguments);

}
