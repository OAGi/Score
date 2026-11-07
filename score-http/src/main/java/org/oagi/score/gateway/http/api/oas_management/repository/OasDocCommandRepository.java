package org.oagi.score.gateway.http.api.oas_management.repository;

import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.*;
import org.oagi.score.gateway.http.api.oas_management.model.*;
import org.oagi.score.gateway.http.api.oas_management.repository.criteria.*;
import org.oagi.score.gateway.http.common.model.base.ScoreDataAccessException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    /**
     * Issue #1791: what of the operation decides the request parameters registered for it (see
     * {@link #reconcileOperationParameters}). {@code null}, an operation not created yet, has none.
     */
    OasOperationParameterState captureOperationParameters(OasOperationId oasOperationId);

    /**
     * Issue #1791: make the request parameters of the operation follow what changed in it since {@code before}:
     * {@code sinceLastDateTime} for a GET that returns an array, a path parameter for each name of its path.
     */
    void reconcileOperationParameters(OasOperationId oasOperationId, OasOperationParameterState before,
                                      org.oagi.score.gateway.http.api.account_management.model.UserId userId);

    /**
     * Apply what the user added, changed and deleted of the request parameters and response headers of the document; the
     * entries that the system registered for the operations are those that are there when this is called. Returns the
     * request parameters and response headers that the document has then.
     */
    List<OasDocHttpHeader> applyHeaderChanges(OasDocId oasDocId, OasDocChangeSet.HeaderChanges changes);

    /** The path of each operation of the document, with the {@code {version}} of the path as the version of the document. */
    Map<OasOperationId, String> findResolvedOperationPaths(OasDocId oasDocId);

    /** Issue #1492: the existing resource id for {@code (oasDocId, path)}, or null. */
    OasResourceId findOasResourceId(OasDocId oasDocId, String path);

    /** Issue #1492: the existing operation id for {@code (resourceId, verb)}, or null. */
    OasOperationId findOasOperationId(OasResourceId oasResourceId, String verb);

    /**
     * The given operationIds that an operation of the document, other than the excluded ones, already
     * uses. The comparison is exact (case-sensitive): the OpenAPI specification says the operationId
     * value is case-sensitive, while the column's collation ignores case, accents and trailing spaces.
     */
    Set<String> findOperationIdsInOasDoc(OasDocId oasDocId, Collection<String> operationIds,
                                         Collection<OasOperationId> excludedOasOperationIds);

    /**
     * Every operation of the document on the given endpoint, i.e. whose resource has the given path and
     * which has the given verb. A Request and a Response body of one endpoint can live in separate
     * operation rows; they are one OpenAPI operation and so share an operationId.
     */
    Set<OasOperationId> findOasOperationIdsAtEndpoint(OasDocId oasDocId, String path, String verb);

    /**
     * The operations of the document that own a Request body ({@code isRequest}) or a Response body on the endpoint, i.e. whose
     * resource has the given path and which have the given verb.
     */
    Set<OasOperationId> findOperationsWithBodyAtEndpoint(OasDocId oasDocId, String path, String verb, boolean isRequest);

    /** Every operation of the document as it is stored, with the bodies it owns. */
    List<OasOperationEndpoint> findOperationEndpoints(OasDocId oasDocId);

    OasTagId insertOasTag(InsertOasTagArguments arguments);

    OasResourceTagId insertOasResourceTag(InsertOasResourceTagArguments arguments);

    OasRequestId insertOasRequest(InsertOasRequestArguments arguments);

    OasResponseId insertOasResponse(InsertOasResponseArguments arguments);

}
