package org.oagi.score.gateway.http.api.oas_management.repository;

import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.*;
import org.oagi.score.gateway.http.api.oas_management.repository.criteria.*;
import org.oagi.score.gateway.http.common.model.base.ScoreDataAccessException;

public interface OasDocCommandRepository {

    CreateOasDocResponse createOasDoc(CreateOasDocRequest request) throws ScoreDataAccessException;

    UpdateOasDocResponse updateOasDoc(UpdateOasDocRequest request) throws ScoreDataAccessException;

    DeleteOasDocResponse deleteOasDoc(DeleteOasDocRequest request) throws ScoreDataAccessException;

    ULong insertOasMessageBody(InsertOasMessageBodyArguments arguments);

    ULong insertOasResource(InsertOasResourceArguments arguments);

    ULong insertOasOperation(InsertOasOperationArguments arguments);

    ULong insertOasTag(InsertOasTagArguments arguments);

    ULong insertOasResourceTag(InsertOasResourceTagArguments arguments);

    ULong insertOasRequest(InsertOasRequestArguments arguments);

    ULong insertOasResponse(InsertOasResponseArguments arguments);

}
