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

    /**
     * Issue #1492: find-or-create the resource for a {@code (oas_doc_id, path)}.
     * Returns the id of the existing resource when one already exists for that document+path
     * (the lowest id if legacy duplicate rows exist), otherwise inserts a new one. Enforced purely
     * in application code -- there is intentionally no DB unique constraint.
     */
    OasResourceId findOrCreateOasResource(InsertOasResourceArguments arguments);

    /**
     * Issue #1492: find-or-create the operation for a {@code (oas_resource_id, verb)}.
     * When an operation already exists for that resource+verb, the EXISTING operation wins (its
     * operationId/summary/description/deprecated/tag are NOT overwritten) and its id is returned
     * (the lowest id if legacy duplicate rows exist); otherwise a new operation is inserted.
     * Enforced purely in application code -- there is no DB unique constraint.
     */
    OasOperationId findOrCreateOasOperation(InsertOasOperationArguments arguments);

    /**
     * Issue #1492: true when the operation already owns a body of the requested type
     * ({@code isRequest} -&gt; an {@code oas_request}; otherwise an {@code oas_response}).
     */
    boolean operationHasBody(OasOperationId oasOperationId, boolean isRequest);

    /** Issue #1492: the existing resource id for {@code (oasDocId, path)}, or null. */
    OasResourceId findOasResourceId(OasDocId oasDocId, String path);

    /** Issue #1492: the existing operation id for {@code (resourceId, verb)}, or null. */
    OasOperationId findOasOperationId(OasResourceId oasResourceId, String verb);

    OasTagId insertOasTag(InsertOasTagArguments arguments);

    OasResourceTagId insertOasResourceTag(InsertOasResourceTagArguments arguments);

    OasRequestId insertOasRequest(InsertOasRequestArguments arguments);

    OasResponseId insertOasResponse(InsertOasResponseArguments arguments);

}
