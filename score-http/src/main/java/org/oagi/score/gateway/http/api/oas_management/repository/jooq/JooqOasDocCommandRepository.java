package org.oagi.score.gateway.http.api.oas_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.*;
import org.oagi.score.gateway.http.api.oas_management.model.*;
import org.oagi.score.gateway.http.api.oas_management.repository.OasDocCommandRepository;
import org.oagi.score.gateway.http.api.oas_management.repository.criteria.*;
import org.oagi.score.gateway.http.common.model.AccessControl;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.base.ScoreDataAccessException;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.OasDocRecord;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.OasOauthFlowRecord;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.OasOauthScopeRecord;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.OasSecuritySchemeRecord;
import org.oagi.score.gateway.http.common.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.oagi.score.gateway.http.common.model.ScoreRole.DEVELOPER;
import static org.oagi.score.gateway.http.common.model.ScoreRole.END_USER;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;
import static org.oagi.score.gateway.http.common.util.ScoreGuidUtils.randomGuid;

public class JooqOasDocCommandRepository extends JooqBaseRepository
        implements OasDocCommandRepository {

    public JooqOasDocCommandRepository(DSLContext dslContext,
                                       ScoreUser requester,
                                       RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public CreateOasDocResponse createOasDoc(
            CreateOasDocRequest request) throws ScoreDataAccessException {

        ScoreUser requester = request.getRequester();
        ULong requesterId = ULong.valueOf(requester.userId().value());
        LocalDateTime timestamp = LocalDateTime.now();

        OasDocRecord record = new OasDocRecord();
        record.setGuid(randomGuid());
        record.setOpenApiVersion(request.getOpenAPIVersion());
        record.setTitle(request.getTitle());
        record.setDescription(request.getDescription());
        record.setTermsOfService(request.getTermsOfService());
        record.setVersion(request.getVersion());
        record.setContactName(request.getContactName());
        record.setContactUrl(request.getContactUrl());
        record.setContactEmail(request.getContactEmail());
        record.setLicenseName(request.getLicenseName());
        record.setLicenseUrl(request.getLicenseUrl());
        record.setOwnerUserId(requesterId);
        record.setCreatedBy(requesterId);
        record.setLastUpdatedBy(requesterId);
        record.setCreationTimestamp(timestamp);
        record.setLastUpdateTimestamp(timestamp);

        ULong oasDocId = dslContext().insertInto(OAS_DOC)
                .set(record)
                .returning(OAS_DOC.OAS_DOC_ID)
                .fetchOne().getOasDocId();
        // Issue #1729: persist the configured Security Schemes (if any).
        saveSecuritySchemes(oasDocId, request.getSecuritySchemes(), requesterId, timestamp);
        saveDocSecurityRequirements(oasDocId, request.getSecurityRequirements(), requesterId, timestamp);
        return new CreateOasDocResponse(oasDocId.toBigInteger());
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public UpdateOasDocResponse updateOasDoc(
            UpdateOasDocRequest request) throws ScoreDataAccessException {

        ScoreUser requester = request.getRequester();
        ULong requesterId = ULong.valueOf(requester.userId().value());
        LocalDateTime timestamp = LocalDateTime.now();

        OasDocRecord record = dslContext().selectFrom(OAS_DOC)
                .where(OAS_DOC.OAS_DOC_ID.eq(valueOf(request.getOasDocId())))
                .fetchOptional().orElse(null);
        if (record == null) {
            throw new ScoreDataAccessException(new IllegalArgumentException());
        }
        List<Field<?>> changedField = new ArrayList();
        if (!StringUtils.equals(request.getTitle(), record.getTitle())) {
            record.setTitle(request.getTitle());
            changedField.add(OAS_DOC.TITLE);
        }
        if (request.getDescription() != null && !StringUtils.equals(request.getDescription(), record.getDescription())) {
            record.setDescription(request.getDescription());
            changedField.add(OAS_DOC.DESCRIPTION);
        }
        if (!StringUtils.equals(request.getVersion(), record.getVersion())) {
            record.setVersion(request.getVersion());
            changedField.add(OAS_DOC.VERSION);
        }
        if (!StringUtils.equals(request.getOpenAPIVersion(), record.getOpenApiVersion())) {
            record.setOpenApiVersion(request.getOpenAPIVersion());
            changedField.add(OAS_DOC.OPEN_API_VERSION);
        }
        if (!StringUtils.equals(request.getTermsOfService(), record.getTermsOfService())) {
            record.setTermsOfService(request.getTermsOfService());
            changedField.add(OAS_DOC.TERMS_OF_SERVICE);
        }
        if (!StringUtils.equals(request.getLicenseName(), record.getLicenseName())) {
            record.setLicenseName(request.getLicenseName());
            changedField.add(OAS_DOC.LICENSE_NAME);
        }
        if (request.getLicenseUrl() != null && !StringUtils.equals(request.getLicenseUrl(), record.getLicenseUrl())) {
            record.setLicenseUrl(request.getLicenseUrl());
            changedField.add(OAS_DOC.LICENSE_URL);
        }
        if (request.getContactName() != null && !StringUtils.equals(request.getContactName(), record.getContactName())) {
            record.setContactName(request.getContactName());
            changedField.add(OAS_DOC.CONTACT_NAME);
        }
        if (request.getContactUrl() != null && !StringUtils.equals(request.getContactUrl(), record.getContactUrl())) {
            record.setContactUrl(request.getContactUrl());
            changedField.add(OAS_DOC.CONTACT_URL);
        }
        if (request.getContactEmail() != null && !StringUtils.equals(request.getContactEmail(), record.getContactEmail())) {
            record.setContactEmail(request.getContactEmail());
            changedField.add(OAS_DOC.CONTACT_EMAIL);
        }
        if (!changedField.isEmpty()) {
            record.setLastUpdatedBy(requesterId);
            changedField.add(OAS_DOC.LAST_UPDATED_BY);

            record.setLastUpdateTimestamp(timestamp);
            changedField.add(OAS_DOC.LAST_UPDATE_TIMESTAMP);

            int affectedRows = record.update(changedField);
            if (affectedRows != 1) {
                throw new ScoreDataAccessException(new IllegalStateException());
            }
        }
        // Issue #1729: replace the document's Security Schemes (independent of the doc-field changes above).
        saveSecuritySchemes(record.getOasDocId(), request.getSecuritySchemes(), requesterId, timestamp);
        saveDocSecurityRequirements(record.getOasDocId(), request.getSecurityRequirements(), requesterId, timestamp);
        return new UpdateOasDocResponse(
                record.getOasDocId().toBigInteger(),
                !changedField.isEmpty());
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public DeleteOasDocResponse deleteOasDoc(
            DeleteOasDocRequest request) throws ScoreDataAccessException {

        List<OasDocId> oasDocIdList = request.getOasDocIdList();
        if (oasDocIdList == null || oasDocIdList.isEmpty()) {
            return new DeleteOasDocResponse(Collections.emptyList());
        }

        List<ULong> oasResourceIds = dslContext().select(OAS_RESOURCE.OAS_RESOURCE_ID)
                .from(OAS_RESOURCE)
                .where(
                        oasDocIdList.size() == 1 ?
                                OAS_RESOURCE.OAS_DOC_ID.eq(valueOf(oasDocIdList.get(0))) :
                                OAS_RESOURCE.OAS_DOC_ID.in(valueOf(oasDocIdList))
                ).fetchInto(ULong.class);

        if (!oasResourceIds.isEmpty()) {
            List<ULong> oasOperationIds = dslContext().select(OAS_OPERATION.OAS_OPERATION_ID)
                    .from(OAS_OPERATION)
                    .where(
                            oasResourceIds.size() == 1 ?
                                    OAS_OPERATION.OAS_RESOURCE_ID.eq(oasResourceIds.get(0)) :
                                    OAS_OPERATION.OAS_RESOURCE_ID.in(oasResourceIds)
                    ).fetchInto(ULong.class);

            if (!oasOperationIds.isEmpty()) {
                deleteOasTagByOperationIdList(oasOperationIds);
                deleteOasRequestByOperationIdList(oasOperationIds);
                deleteOasResponseByOperationIdList(oasOperationIds);

                // Issue #1729: no ON DELETE CASCADE — remove operation-level security (scopes -> entries) first.
                dslContext().deleteFrom(OAS_OPERATION_SECURITY_SCOPE)
                        .where(OAS_OPERATION_SECURITY_SCOPE.OAS_OPERATION_SECURITY_ID.in(
                                dslContext().select(OAS_OPERATION_SECURITY.OAS_OPERATION_SECURITY_ID)
                                        .from(OAS_OPERATION_SECURITY)
                                        .where(OAS_OPERATION_SECURITY.OAS_OPERATION_ID.in(oasOperationIds))))
                        .execute();
                dslContext().deleteFrom(OAS_OPERATION_SECURITY)
                        .where(OAS_OPERATION_SECURITY.OAS_OPERATION_ID.in(oasOperationIds))
                        .execute();

                dslContext().delete(OAS_OPERATION)
                        .where(
                                oasOperationIds.size() == 1 ?
                                        OAS_OPERATION.OAS_OPERATION_ID.eq(oasOperationIds.get(0)) :
                                        OAS_OPERATION.OAS_OPERATION_ID.in(oasOperationIds)
                        ).execute();
            }

            dslContext().delete(OAS_RESOURCE)
                    .where(
                            oasResourceIds.size() == 1 ?
                                    OAS_RESOURCE.OAS_RESOURCE_ID.eq(oasResourceIds.get(0)) :
                                    OAS_RESOURCE.OAS_RESOURCE_ID.in(oasResourceIds)
                    ).execute();
        }

        // Issue #1729: no ON DELETE CASCADE — delete the document's security objects children-first.
        // Root-level security: scopes -> entries.
        dslContext().deleteFrom(OAS_DOC_SECURITY_SCOPE)
                .where(OAS_DOC_SECURITY_SCOPE.OAS_DOC_SECURITY_ID.in(
                        dslContext().select(OAS_DOC_SECURITY.OAS_DOC_SECURITY_ID).from(OAS_DOC_SECURITY)
                                .where(OAS_DOC_SECURITY.OAS_DOC_ID.in(valueOf(oasDocIdList)))))
                .execute();
        dslContext().deleteFrom(OAS_DOC_SECURITY)
                .where(OAS_DOC_SECURITY.OAS_DOC_ID.in(valueOf(oasDocIdList)))
                .execute();
        // Security schemes: oauth scopes -> flows -> schemes.
        dslContext().deleteFrom(OAS_OAUTH_SCOPE)
                .where(OAS_OAUTH_SCOPE.OAS_OAUTH_FLOW_ID.in(
                        dslContext().select(OAS_OAUTH_FLOW.OAS_OAUTH_FLOW_ID).from(OAS_OAUTH_FLOW)
                                .where(OAS_OAUTH_FLOW.OAS_SECURITY_SCHEME_ID.in(
                                        dslContext().select(OAS_SECURITY_SCHEME.OAS_SECURITY_SCHEME_ID)
                                                .from(OAS_SECURITY_SCHEME)
                                                .where(OAS_SECURITY_SCHEME.OAS_DOC_ID.in(valueOf(oasDocIdList)))))))
                .execute();
        dslContext().deleteFrom(OAS_OAUTH_FLOW)
                .where(OAS_OAUTH_FLOW.OAS_SECURITY_SCHEME_ID.in(
                        dslContext().select(OAS_SECURITY_SCHEME.OAS_SECURITY_SCHEME_ID).from(OAS_SECURITY_SCHEME)
                                .where(OAS_SECURITY_SCHEME.OAS_DOC_ID.in(valueOf(oasDocIdList)))))
                .execute();
        dslContext().deleteFrom(OAS_SECURITY_SCHEME)
                .where(OAS_SECURITY_SCHEME.OAS_DOC_ID.in(valueOf(oasDocIdList)))
                .execute();

        try {
            dslContext().delete(OAS_DOC)
                    .where(
                            oasDocIdList.size() == 1 ?
                                    OAS_DOC.OAS_DOC_ID.eq(valueOf(oasDocIdList.get(0))) :
                                    OAS_DOC.OAS_DOC_ID.in(valueOf(oasDocIdList))
                    )
                    .execute();
        } catch (Exception e) {
            throw new ScoreDataAccessException("It's not possible to delete the used oas doc.", e);
        }

        DeleteOasDocResponse response = new DeleteOasDocResponse(oasDocIdList);
        return response;
    }

    private void deleteOasTagByOperationIdList(List<ULong> oasOperationIdList) {
        List<ULong> oasTagIds = dslContext().select(OAS_RESOURCE_TAG.OAS_TAG_ID)
                .from(OAS_RESOURCE_TAG)
                .where(
                        oasOperationIdList.size() == 1 ?
                                OAS_RESOURCE_TAG.OAS_OPERATION_ID.eq(oasOperationIdList.get(0)) :
                                OAS_RESOURCE_TAG.OAS_OPERATION_ID.in(oasOperationIdList)
                ).fetchInto(ULong.class);

        dslContext().deleteFrom(OAS_RESOURCE_TAG)
                .where(
                        oasOperationIdList.size() == 1 ?
                                OAS_RESOURCE_TAG.OAS_OPERATION_ID.eq(oasOperationIdList.get(0)) :
                                OAS_RESOURCE_TAG.OAS_OPERATION_ID.in(oasOperationIdList)
                ).execute();

        if (!oasTagIds.isEmpty()) {
            dslContext().deleteFrom(OAS_TAG)
                    .where(
                            oasOperationIdList.size() == 1 ?
                                    OAS_TAG.OAS_TAG_ID.eq(oasTagIds.get(0)) :
                                    OAS_TAG.OAS_TAG_ID.in(oasTagIds)
                    ).execute();
        }
    }

    private void deleteOasRequestByOperationIdList(List<ULong> oasOperationIdList) {
        List<ULong> oasRequestIds = dslContext().select(OAS_REQUEST.OAS_REQUEST_ID)
                .from(OAS_REQUEST)
                .where(
                        oasOperationIdList.size() == 1 ?
                                OAS_REQUEST.OAS_OPERATION_ID.eq(oasOperationIdList.get(0)) :
                                OAS_REQUEST.OAS_OPERATION_ID.in(oasOperationIdList)
                )
                .fetchInto(ULong.class);

        if (!oasRequestIds.isEmpty()) {
            dslContext().deleteFrom(OAS_REQUEST_PARAMETER)
                    .where(
                            oasOperationIdList.size() == 1 ?
                                    OAS_REQUEST_PARAMETER.OAS_REQUEST_ID.eq(oasRequestIds.get(0)) :
                                    OAS_REQUEST_PARAMETER.OAS_REQUEST_ID.in(oasRequestIds)
                    )
                    .execute();

            dslContext().deleteFrom(OAS_REQUEST)
                    .where(
                            oasOperationIdList.size() == 1 ?
                                    OAS_REQUEST.OAS_REQUEST_ID.eq(oasRequestIds.get(0)) :
                                    OAS_REQUEST.OAS_REQUEST_ID.in(oasRequestIds)
                    )
                    .execute();
        }
    }

    private void deleteOasResponseByOperationIdList(List<ULong> oasOperationIdList) {
        List<ULong> oasResponseIds = dslContext().select(OAS_RESPONSE.OAS_RESPONSE_ID)
                .from(OAS_RESPONSE)
                .where(
                        oasOperationIdList.size() == 1 ?
                                OAS_RESPONSE.OAS_OPERATION_ID.eq(oasOperationIdList.get(0)) :
                                OAS_RESPONSE.OAS_OPERATION_ID.in(oasOperationIdList)
                )
                .fetchInto(ULong.class);

        if (!oasResponseIds.isEmpty()) {
            dslContext().deleteFrom(OAS_RESPONSE_HEADERS)
                    .where(
                            oasOperationIdList.size() == 1 ?
                                    OAS_RESPONSE_HEADERS.OAS_RESPONSE_ID.eq(oasResponseIds.get(0)) :
                                    OAS_RESPONSE_HEADERS.OAS_RESPONSE_ID.in(oasResponseIds)
                    )
                    .execute();

            dslContext().deleteFrom(OAS_RESPONSE)
                    .where(
                            oasOperationIdList.size() == 1 ?
                                    OAS_RESPONSE.OAS_RESPONSE_ID.eq(oasResponseIds.get(0)) :
                                    OAS_RESPONSE.OAS_RESPONSE_ID.in(oasResponseIds)
                    )
                    .execute();
        }
    }

    @Override
    public OasMessageBodyId insertOasMessageBody(InsertOasMessageBodyArguments arguments) {
        return new OasMessageBodyId(dslContext().insertInto(OAS_MESSAGE_BODY)
                .set(OAS_MESSAGE_BODY.CREATED_BY, valueOf(arguments.getUserId()))
                .set(OAS_MESSAGE_BODY.LAST_UPDATED_BY, valueOf(arguments.getUserId()))
                .set(OAS_MESSAGE_BODY.CREATION_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_MESSAGE_BODY.LAST_UPDATE_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_MESSAGE_BODY.TOP_LEVEL_ASBIEP_ID, valueOf(arguments.getTopLevelAsbiepId()))
                .returningResult(OAS_MESSAGE_BODY.OAS_MESSAGE_BODY_ID)
                .fetchOne().value1().toBigInteger());
    }

    @Override
    public OasResourceId insertOasResource(InsertOasResourceArguments arguments) {
        return new OasResourceId(dslContext().insertInto(OAS_RESOURCE)
                .set(OAS_RESOURCE.CREATED_BY, valueOf(arguments.getUserId()))
                .set(OAS_RESOURCE.LAST_UPDATED_BY, valueOf(arguments.getUserId()))
                .set(OAS_RESOURCE.CREATION_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_RESOURCE.LAST_UPDATE_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_RESOURCE.OAS_DOC_ID, valueOf(arguments.getOasDocId()))
                .set(OAS_RESOURCE.PATH, arguments.getPath())
                .set(OAS_RESOURCE.REF, arguments.getRef())
                .returningResult(OAS_RESOURCE.OAS_RESOURCE_ID)
                .fetchOne().value1().toBigInteger());
    }

    @Override
    public OasOperationId insertOasOperation(InsertOasOperationArguments arguments) {
        return new OasOperationId(dslContext().insertInto(OAS_OPERATION)
                .set(OAS_OPERATION.CREATED_BY, valueOf(arguments.getUserId()))
                .set(OAS_OPERATION.LAST_UPDATED_BY, valueOf(arguments.getUserId()))
                .set(OAS_OPERATION.CREATION_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_OPERATION.LAST_UPDATE_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_OPERATION.OAS_RESOURCE_ID, arguments.getOasResourceId())
                .set(OAS_OPERATION.VERB, arguments.getVerb())
                .set(OAS_OPERATION.OPERATION_ID, arguments.getOperationId())
                .set(OAS_OPERATION.SUMMARY, arguments.getSummary())
                .set(OAS_OPERATION.DESCRIPTION, arguments.getDescription())
                .set(OAS_OPERATION.DEPRECATED, (byte) (arguments.isDeprecated() ? 1 : 0))
                .returningResult(OAS_OPERATION.OAS_OPERATION_ID)
                .fetchOne().value1().toBigInteger());
    }

    @Override
    public OasTagId insertOasTag(InsertOasTagArguments arguments) {
        return new OasTagId(dslContext().insertInto(OAS_TAG)
                .set(OAS_TAG.CREATED_BY, valueOf(arguments.getUserId()))
                .set(OAS_TAG.LAST_UPDATED_BY, valueOf(arguments.getUserId()))
                .set(OAS_TAG.CREATION_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_TAG.LAST_UPDATE_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_TAG.NAME, arguments.getName())
                .set(OAS_TAG.DESCRIPTION, arguments.getDescription())
                .set(OAS_TAG.GUID, arguments.getGuid())
                .returningResult(OAS_TAG.OAS_TAG_ID)
                .fetchOne().value1().toBigInteger());
    }

    @Override
    public OasResourceTagId insertOasResourceTag(InsertOasResourceTagArguments arguments) {
        return new OasResourceTagId(dslContext().insertInto(OAS_RESOURCE_TAG)
                .set(OAS_RESOURCE_TAG.CREATED_BY, valueOf(arguments.getUserId()))
                .set(OAS_RESOURCE_TAG.LAST_UPDATED_BY, valueOf(arguments.getUserId()))
                .set(OAS_RESOURCE_TAG.CREATION_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_RESOURCE_TAG.LAST_UPDATE_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_RESOURCE_TAG.OAS_OPERATION_ID, valueOf(arguments.getOasOperationId()))
                .set(OAS_RESOURCE_TAG.OAS_TAG_ID, valueOf(arguments.getOasTagId()))
                .returningResult(OAS_RESOURCE_TAG.OAS_TAG_ID)
                .fetchOne().value1().toBigInteger());
    }

    @Override
    public OasRequestId insertOasRequest(InsertOasRequestArguments arguments) {
        return new OasRequestId(dslContext().insertInto(OAS_REQUEST)
                .set(OAS_REQUEST.CREATED_BY, valueOf(arguments.getUserId()))
                .set(OAS_REQUEST.LAST_UPDATED_BY, valueOf(arguments.getUserId()))
                .set(OAS_REQUEST.CREATION_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_REQUEST.LAST_UPDATE_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_REQUEST.OAS_MESSAGE_BODY_ID, valueOf(arguments.getOasMessageBodyId()))
                .set(OAS_REQUEST.OAS_OPERATION_ID, valueOf(arguments.getOasOperationId()))
                .set(OAS_REQUEST.DESCRIPTION, arguments.getDescription())
                .set(OAS_REQUEST.SUPPRESS_ROOT_INDICATOR, (byte) (arguments.isSuppressRootIndicator() ? 1 : 0))
                .set(OAS_REQUEST.MAKE_ARRAY_INDICATOR, (byte) (arguments.isMakeArrayIndicator() ? 1 : 0))
                .set(OAS_REQUEST.IS_CALLBACK, (byte) 0)
                .set(OAS_REQUEST.REQUIRED, (byte) (arguments.isRequired() ? 1 : 0))
                .returningResult(OAS_REQUEST.OAS_REQUEST_ID)
                .fetchOne().value1().toBigInteger());
    }

    @Override
    public OasResponseId insertOasResponse(InsertOasResponseArguments arguments) {
        return new OasResponseId(dslContext().insertInto(OAS_RESPONSE)
                .set(OAS_RESPONSE.CREATED_BY, valueOf(arguments.getUserId()))
                .set(OAS_RESPONSE.LAST_UPDATED_BY, valueOf(arguments.getUserId()))
                .set(OAS_RESPONSE.CREATION_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_RESPONSE.LAST_UPDATE_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_RESPONSE.OAS_MESSAGE_BODY_ID, valueOf(arguments.getOasMessageBodyId()))
                .set(OAS_RESPONSE.OAS_OPERATION_ID, valueOf(arguments.getOasOperationId()))
                .set(OAS_RESPONSE.DESCRIPTION, arguments.getDescription())
                // Issue #1730: Persist the HTTP status code (e.g. 202/204) so bodyless responses can be generated.
                .set(OAS_RESPONSE.HTTP_STATUS_CODE,
                        (arguments.getHttpStatusCode() != null && !arguments.getHttpStatusCode().isBlank())
                                ? Integer.valueOf(arguments.getHttpStatusCode().trim()) : null)
                .set(OAS_RESPONSE.SUPPRESS_ROOT_INDICATOR, (byte) (arguments.isSuppressRootIndicator() ? 1 : 0))
                .set(OAS_RESPONSE.MAKE_ARRAY_INDICATOR, (byte) (arguments.isMakeArrayIndicator() ? 1 : 0))
                .set(OAS_RESPONSE.INCLUDE_CONFIRM_INDICATOR, (byte) 0)
                .returningResult(OAS_RESPONSE.OAS_RESPONSE_ID)
                .fetchOne().value1().toBigInteger());
    }

    /**
     * Issue #1729: persist the document's Security Schemes. An empty/null list keeps the legacy
     * hardcoded OAuth2 default and persists NO row. Diff strategy (NOT delete-then-reinsert): rows the
     * client still carries (matched by {@code oas_security_scheme_id}) are UPDATEd in place so their id
     * stays stable for the {@code oas_doc_security}/{@code oas_operation_security} FK references; rows no
     * longer present are DELETEd (after their requirement references are cleared); rows without a known id
     * are INSERTed. Scheme names are unique per doc (DB UK).
     */
    private void saveSecuritySchemes(ULong oasDocId, List<OasSecurityScheme> schemes,
                                     ULong requesterId, LocalDateTime timestamp) {
        List<OasSecuritySchemeRecord> existing = dslContext().selectFrom(OAS_SECURITY_SCHEME)
                .where(OAS_SECURITY_SCHEME.OAS_DOC_ID.eq(oasDocId))
                .fetch();
        Map<ULong, OasSecuritySchemeRecord> existingById = new LinkedHashMap<>();
        for (OasSecuritySchemeRecord record : existing) {
            existingById.put(record.getOasSecuritySchemeId(), record);
        }

        List<OasSecurityScheme> incoming = (schemes == null) ? Collections.emptyList() : schemes;

        // Ids the client still carries -> these rows survive (are UPDATEd); the rest are removed.
        Set<ULong> keptIds = new HashSet<>();
        for (OasSecurityScheme scheme : incoming) {
            if (scheme == null || scheme.getType() == null || scheme.getType().isBlank()
                    || scheme.getOasSecuritySchemeId() == null) {
                continue;
            }
            ULong id = ULong.valueOf(scheme.getOasSecuritySchemeId());
            if (existingById.containsKey(id)) {
                keptIds.add(id);
            }
        }

        // DELETE removed schemes. No ON DELETE CASCADE: first clear the requirement references
        // (oas_doc_security / oas_operation_security) and the scheme's own oauth flows/scopes.
        for (ULong id : existingById.keySet()) {
            if (!keptIds.contains(id)) {
                deleteSecuritySchemeReferences(id);
                deleteOAuthFlows(id);
                dslContext().deleteFrom(OAS_SECURITY_SCHEME)
                        .where(OAS_SECURITY_SCHEME.OAS_SECURITY_SCHEME_ID.eq(id))
                        .execute();
            }
        }

        if (incoming.isEmpty()) {
            return;
        }

        // Park every surviving row on a unique temporary name first so reordering/swapping names across
        // the kept schemes cannot transiently violate the (oas_doc_id, scheme_name) unique key.
        for (ULong id : keptIds) {
            dslContext().update(OAS_SECURITY_SCHEME)
                    .set(OAS_SECURITY_SCHEME.SCHEME_NAME, existingById.get(id).getGuid())
                    .where(OAS_SECURITY_SCHEME.OAS_SECURITY_SCHEME_ID.eq(id))
                    .execute();
        }

        Set<String> usedNames = new HashSet<>();
        Set<ULong> processedIds = new HashSet<>();
        for (OasSecurityScheme scheme : incoming) {
            if (scheme == null || scheme.getType() == null || scheme.getType().isBlank()) {
                continue;
            }
            ULong id = (scheme.getOasSecuritySchemeId() != null)
                    ? ULong.valueOf(scheme.getOasSecuritySchemeId()) : null;
            // Guard against a malformed payload that repeats the same id: processing it twice would
            // overwrite the first variant's row (and orphan requirements referencing its name). Keep the
            // first occurrence only.
            if (id != null && !processedIds.add(id)) {
                continue;
            }
            String type = scheme.getType();
            String schemeName = resolveUniqueSchemeName(scheme, usedNames);
            usedNames.add(schemeName);

            OasSecuritySchemeRecord record = (id != null) ? existingById.get(id) : null;

            ULong oasSecuritySchemeId;
            if (record != null) {
                // UPDATE in place: the id is preserved so dependent FK references stay valid.
                record.setType(type);
                record.setSchemeName(schemeName);
                // The row was parked on a temporary name above; force the column to be written even when
                // the final name equals the originally-loaded one (otherwise the temp name would stick).
                record.changed(OAS_SECURITY_SCHEME.SCHEME_NAME, true);
                record.setDescription(scheme.getDescription());
                record.setApiKeyName("apiKey".equalsIgnoreCase(type) ? scheme.getApiKeyName() : null);
                record.setApiKeyIn("apiKey".equalsIgnoreCase(type) ? scheme.getApiKeyIn() : null);
                if ("http".equalsIgnoreCase(type)) {
                    record.setHttpScheme(scheme.getHttpScheme());
                    record.setBearerFormat("bearer".equalsIgnoreCase(scheme.getHttpScheme())
                            ? scheme.getBearerFormat() : null);
                } else {
                    record.setHttpScheme(null);
                    record.setBearerFormat(null);
                }
                record.setOpenIdConnectUrl("openIdConnect".equalsIgnoreCase(type)
                        ? scheme.getOpenIdConnectUrl() : null);
                record.setLastUpdatedBy(requesterId);
                record.setLastUpdateTimestamp(timestamp);
                record.update();
                oasSecuritySchemeId = record.getOasSecuritySchemeId();
            } else {
                // INSERT a brand-new scheme.
                OasSecuritySchemeRecord newRecord = new OasSecuritySchemeRecord();
                newRecord.setGuid(randomGuid());
                newRecord.setOasDocId(oasDocId);
                newRecord.setType(type);
                newRecord.setSchemeName(schemeName);
                newRecord.setDescription(scheme.getDescription());
                if ("apiKey".equalsIgnoreCase(type)) {
                    newRecord.setApiKeyName(scheme.getApiKeyName());
                    newRecord.setApiKeyIn(scheme.getApiKeyIn());
                } else if ("http".equalsIgnoreCase(type)) {
                    newRecord.setHttpScheme(scheme.getHttpScheme());
                    if ("bearer".equalsIgnoreCase(scheme.getHttpScheme())) {
                        newRecord.setBearerFormat(scheme.getBearerFormat());
                    }
                } else if ("openIdConnect".equalsIgnoreCase(type)) {
                    newRecord.setOpenIdConnectUrl(scheme.getOpenIdConnectUrl());
                }
                newRecord.setDeprecated((byte) 0);
                newRecord.setCreatedBy(requesterId);
                newRecord.setLastUpdatedBy(requesterId);
                newRecord.setCreationTimestamp(timestamp);
                newRecord.setLastUpdateTimestamp(timestamp);
                oasSecuritySchemeId = dslContext().insertInto(OAS_SECURITY_SCHEME)
                        .set(newRecord)
                        .returning(OAS_SECURITY_SCHEME.OAS_SECURITY_SCHEME_ID)
                        .fetchOne().getOasSecuritySchemeId();
            }

            // Issue #1729: persist the OAuth Flows Object for oauth2 schemes (diffed in place); clear any
            // leftover flows for non-oauth2 (e.g. when an existing scheme's type changed away from oauth2).
            if ("oauth2".equalsIgnoreCase(type)) {
                saveOAuthFlows(oasSecuritySchemeId, scheme.getFlows(), requesterId, timestamp);
            } else {
                deleteOAuthFlows(oasSecuritySchemeId);
            }
        }
    }

    // Issue #1729: delete a scheme's OAuth flows and their scopes (children-first; no ON DELETE CASCADE).
    private void deleteOAuthFlows(ULong oasSecuritySchemeId) {
        dslContext().deleteFrom(OAS_OAUTH_SCOPE)
                .where(OAS_OAUTH_SCOPE.OAS_OAUTH_FLOW_ID.in(
                        dslContext().select(OAS_OAUTH_FLOW.OAS_OAUTH_FLOW_ID).from(OAS_OAUTH_FLOW)
                                .where(OAS_OAUTH_FLOW.OAS_SECURITY_SCHEME_ID.eq(oasSecuritySchemeId))))
                .execute();
        dslContext().deleteFrom(OAS_OAUTH_FLOW)
                .where(OAS_OAUTH_FLOW.OAS_SECURITY_SCHEME_ID.eq(oasSecuritySchemeId))
                .execute();
    }

    // Issue #1729: before a scheme row is deleted, clear the Security Requirement entries that reference it
    // (document-level and operation-level, scopes-first) because the FK is RESTRICT, not ON DELETE CASCADE.
    private void deleteSecuritySchemeReferences(ULong oasSecuritySchemeId) {
        dslContext().deleteFrom(OAS_DOC_SECURITY_SCOPE)
                .where(OAS_DOC_SECURITY_SCOPE.OAS_DOC_SECURITY_ID.in(
                        dslContext().select(OAS_DOC_SECURITY.OAS_DOC_SECURITY_ID).from(OAS_DOC_SECURITY)
                                .where(OAS_DOC_SECURITY.OAS_SECURITY_SCHEME_ID.eq(oasSecuritySchemeId))))
                .execute();
        dslContext().deleteFrom(OAS_DOC_SECURITY)
                .where(OAS_DOC_SECURITY.OAS_SECURITY_SCHEME_ID.eq(oasSecuritySchemeId))
                .execute();
        dslContext().deleteFrom(OAS_OPERATION_SECURITY_SCOPE)
                .where(OAS_OPERATION_SECURITY_SCOPE.OAS_OPERATION_SECURITY_ID.in(
                        dslContext().select(OAS_OPERATION_SECURITY.OAS_OPERATION_SECURITY_ID).from(OAS_OPERATION_SECURITY)
                                .where(OAS_OPERATION_SECURITY.OAS_SECURITY_SCHEME_ID.eq(oasSecuritySchemeId))))
                .execute();
        dslContext().deleteFrom(OAS_OPERATION_SECURITY)
                .where(OAS_OPERATION_SECURITY.OAS_SECURITY_SCHEME_ID.eq(oasSecuritySchemeId))
                .execute();
    }

    // Issue #1729: map this doc's components.securitySchemes key -> oas_security_scheme_id, so a Security
    // Requirement entry (carried by name on the wire) can be persisted as the FK to oas_security_scheme.
    private Map<String, ULong> loadSchemeIdByName(ULong oasDocId) {
        Map<String, ULong> schemeIdByName = new LinkedHashMap<>();
        dslContext().select(OAS_SECURITY_SCHEME.SCHEME_NAME, OAS_SECURITY_SCHEME.OAS_SECURITY_SCHEME_ID)
                .from(OAS_SECURITY_SCHEME)
                .where(OAS_SECURITY_SCHEME.OAS_DOC_ID.eq(oasDocId))
                .fetch()
                .forEach(record -> schemeIdByName.put(record.value1(), record.value2()));
        return schemeIdByName;
    }

    // Issue #1729: persist a scheme's OAuth flows and their scopes as a DIFF (not delete-then-reinsert):
    // a flow still present (matched by flow_type, its unique key within the scheme) is UPDATEd in place so
    // its id stays stable; flows no longer present are deleted (scopes first; no ON DELETE CASCADE); new
    // flows are inserted. Each kept flow's scopes are reconciled the same way (matched by scope_name).
    private void saveOAuthFlows(ULong oasSecuritySchemeId, List<OasOAuthFlow> flows,
                                ULong requesterId, LocalDateTime timestamp) {
        List<OasOAuthFlow> incoming = (flows == null) ? Collections.emptyList() : flows;

        // Existing flows for this scheme, keyed by flow_type.
        Map<String, OasOauthFlowRecord> existingByType = new LinkedHashMap<>();
        dslContext().selectFrom(OAS_OAUTH_FLOW)
                .where(OAS_OAUTH_FLOW.OAS_SECURITY_SCHEME_ID.eq(oasSecuritySchemeId))
                .fetch()
                .forEach(record -> existingByType.put(record.getFlowType(), record));

        // Desired flow types (a flow_type is unique within the scheme).
        Set<String> desiredTypes = new HashSet<>();
        for (OasOAuthFlow flow : incoming) {
            if (flow != null && flow.getFlowType() != null && !flow.getFlowType().isBlank()) {
                desiredTypes.add(flow.getFlowType());
            }
        }

        // DELETE flows no longer desired (scopes first; no ON DELETE CASCADE).
        for (Map.Entry<String, OasOauthFlowRecord> existing : existingByType.entrySet()) {
            if (!desiredTypes.contains(existing.getKey())) {
                ULong oasOAuthFlowId = existing.getValue().getOasOauthFlowId();
                dslContext().deleteFrom(OAS_OAUTH_SCOPE)
                        .where(OAS_OAUTH_SCOPE.OAS_OAUTH_FLOW_ID.eq(oasOAuthFlowId)).execute();
                dslContext().deleteFrom(OAS_OAUTH_FLOW)
                        .where(OAS_OAUTH_FLOW.OAS_OAUTH_FLOW_ID.eq(oasOAuthFlowId)).execute();
            }
        }

        // INSERT new flows / UPDATE existing ones in place; reconcile each flow's scopes.
        Set<String> processedTypes = new HashSet<>();
        for (OasOAuthFlow flow : incoming) {
            if (flow == null || flow.getFlowType() == null || flow.getFlowType().isBlank()) {
                continue;
            }
            String flowType = flow.getFlowType();
            if (!processedTypes.add(flowType)) {
                continue; // at most one flow per type (DB unique key)
            }
            OasOauthFlowRecord record = existingByType.get(flowType);
            ULong oasOAuthFlowId;
            if (record != null) {
                // UPDATE in place only when a URL actually changed (the id is preserved either way).
                if (!Objects.equals(record.getAuthorizationUrl(), flow.getAuthorizationUrl())
                        || !Objects.equals(record.getTokenUrl(), flow.getTokenUrl())
                        || !Objects.equals(record.getRefreshUrl(), flow.getRefreshUrl())
                        || !Objects.equals(record.getDeviceAuthorizationUrl(), flow.getDeviceAuthorizationUrl())) {
                    record.setAuthorizationUrl(flow.getAuthorizationUrl());
                    record.setTokenUrl(flow.getTokenUrl());
                    record.setRefreshUrl(flow.getRefreshUrl());
                    record.setDeviceAuthorizationUrl(flow.getDeviceAuthorizationUrl());
                    record.setLastUpdatedBy(requesterId);
                    record.setLastUpdateTimestamp(timestamp);
                    record.update();
                }
                oasOAuthFlowId = record.getOasOauthFlowId();
            } else {
                OasOauthFlowRecord flowRecord = new OasOauthFlowRecord();
                flowRecord.setGuid(randomGuid());
                flowRecord.setOasSecuritySchemeId(oasSecuritySchemeId);
                flowRecord.setFlowType(flowType);
                flowRecord.setAuthorizationUrl(flow.getAuthorizationUrl());
                flowRecord.setTokenUrl(flow.getTokenUrl());
                flowRecord.setRefreshUrl(flow.getRefreshUrl());
                flowRecord.setDeviceAuthorizationUrl(flow.getDeviceAuthorizationUrl());
                flowRecord.setCreatedBy(requesterId);
                flowRecord.setLastUpdatedBy(requesterId);
                flowRecord.setCreationTimestamp(timestamp);
                flowRecord.setLastUpdateTimestamp(timestamp);
                oasOAuthFlowId = dslContext().insertInto(OAS_OAUTH_FLOW)
                        .set(flowRecord)
                        .returning(OAS_OAUTH_FLOW.OAS_OAUTH_FLOW_ID)
                        .fetchOne().getOasOauthFlowId();
            }
            reconcileOAuthScopes(oasOAuthFlowId, flow.getScopes(), requesterId, timestamp);
        }
    }

    // Issue #1729: reconcile an OAuth flow's scopes (name -> description) as a diff — insert new names,
    // delete removed names, update a kept name's description when it changed, leave unchanged rows (ids) alone.
    private void reconcileOAuthScopes(ULong oasOAuthFlowId, List<OasOAuthScope> scopes,
                                      ULong requesterId, LocalDateTime timestamp) {
        Map<String, OasOauthScopeRecord> existingByName = new LinkedHashMap<>();
        dslContext().selectFrom(OAS_OAUTH_SCOPE)
                .where(OAS_OAUTH_SCOPE.OAS_OAUTH_FLOW_ID.eq(oasOAuthFlowId))
                .fetch()
                .forEach(record -> existingByName.put(record.getScopeName(), record));

        // Desired scopes (name -> description); a scope_name is unique per flow, so keep the first description.
        LinkedHashMap<String, String> desired = new LinkedHashMap<>();
        if (scopes != null) {
            for (OasOAuthScope scope : scopes) {
                if (scope == null || scope.getScopeName() == null || scope.getScopeName().isBlank()) {
                    continue;
                }
                desired.putIfAbsent(scope.getScopeName(), scope.getDescription());
            }
        }

        for (Map.Entry<String, OasOauthScopeRecord> existing : existingByName.entrySet()) {
            if (!desired.containsKey(existing.getKey())) {
                dslContext().deleteFrom(OAS_OAUTH_SCOPE)
                        .where(OAS_OAUTH_SCOPE.OAS_OAUTH_SCOPE_ID.eq(existing.getValue().getOasOauthScopeId()))
                        .execute();
            }
        }
        for (Map.Entry<String, String> entry : desired.entrySet()) {
            OasOauthScopeRecord record = existingByName.get(entry.getKey());
            if (record == null) {
                OasOauthScopeRecord scopeRecord = new OasOauthScopeRecord();
                scopeRecord.setGuid(randomGuid());
                scopeRecord.setOasOauthFlowId(oasOAuthFlowId);
                scopeRecord.setScopeName(entry.getKey());
                scopeRecord.setDescription(entry.getValue());
                scopeRecord.setCreatedBy(requesterId);
                scopeRecord.setLastUpdatedBy(requesterId);
                scopeRecord.setCreationTimestamp(timestamp);
                scopeRecord.setLastUpdateTimestamp(timestamp);
                dslContext().insertInto(OAS_OAUTH_SCOPE).set(scopeRecord).execute();
            } else if (!Objects.equals(record.getDescription(), entry.getValue())) {
                record.setDescription(entry.getValue());
                record.setLastUpdatedBy(requesterId);
                record.setLastUpdateTimestamp(timestamp);
                record.update();
            }
        }
    }

    // Issue #1729: persist the document's root-level Security Requirement entries as a DIFF (not
    // delete-then-reinsert): a (requirement_group, oas_security_scheme_id) pair still present is kept with
    // its id and only its free-text scopes are reconciled; pairs no longer present are deleted; new pairs
    // are inserted. These rows carry no mutable payload beyond their key, so there is nothing else to update.
    private void saveDocSecurityRequirements(ULong oasDocId, List<OasSecurityRequirement> requirements,
                                             ULong requesterId, LocalDateTime timestamp) {
        // Schemes are saved before requirements; resolve each by-name reference to its FK id.
        Map<String, ULong> schemeIdByName = loadSchemeIdByName(oasDocId);

        // Desired state keyed by (requirement_group, scheme id) -> the entry (null scheme id = anonymous).
        LinkedHashMap<String, OasSecurityRequirementDiff.Entry> desired =
                OasSecurityRequirementDiff.build(requirements, schemeIdByName);

        // Existing rows for this doc, keyed the same way.
        Map<String, ULong> existingIdByKey = new LinkedHashMap<>();
        dslContext().select(OAS_DOC_SECURITY.OAS_DOC_SECURITY_ID, OAS_DOC_SECURITY.REQUIREMENT_GROUP,
                        OAS_DOC_SECURITY.OAS_SECURITY_SCHEME_ID)
                .from(OAS_DOC_SECURITY)
                .where(OAS_DOC_SECURITY.OAS_DOC_ID.eq(oasDocId))
                .fetch()
                .forEach(record -> existingIdByKey.put(
                        OasSecurityRequirementDiff.key(record.get(OAS_DOC_SECURITY.REQUIREMENT_GROUP), record.get(OAS_DOC_SECURITY.OAS_SECURITY_SCHEME_ID)),
                        record.get(OAS_DOC_SECURITY.OAS_DOC_SECURITY_ID)));

        // DELETE entries no longer desired (scopes first; no ON DELETE CASCADE).
        for (Map.Entry<String, ULong> existing : existingIdByKey.entrySet()) {
            if (!desired.containsKey(existing.getKey())) {
                dslContext().deleteFrom(OAS_DOC_SECURITY_SCOPE)
                        .where(OAS_DOC_SECURITY_SCOPE.OAS_DOC_SECURITY_ID.eq(existing.getValue()))
                        .execute();
                dslContext().deleteFrom(OAS_DOC_SECURITY)
                        .where(OAS_DOC_SECURITY.OAS_DOC_SECURITY_ID.eq(existing.getValue()))
                        .execute();
            }
        }

        // INSERT new entries; reconcile the scopes of kept entries in place.
        for (OasSecurityRequirementDiff.Entry entry : desired.values()) {
            ULong oasDocSecurityId = existingIdByKey.get(OasSecurityRequirementDiff.key(entry.group, entry.schemeId));
            if (oasDocSecurityId == null) {
                insertDocSecurityRequirementEntry(oasDocId, entry.group, entry.schemeId, entry.scopes, requesterId, timestamp);
            } else {
                reconcileDocSecurityScopes(oasDocSecurityId, entry.scopes, requesterId, timestamp);
            }
        }
    }

    // Issue #1729: reconcile a doc-level requirement entry's free-text scopes in place — insert names not
    // yet stored, delete names no longer requested, leave unchanged names (and their ids) untouched.
    private void reconcileDocSecurityScopes(ULong oasDocSecurityId, List<String> scopes,
                                            ULong requesterId, LocalDateTime timestamp) {
        Map<String, ULong> existing = new LinkedHashMap<>();
        dslContext().select(OAS_DOC_SECURITY_SCOPE.SCOPE_NAME, OAS_DOC_SECURITY_SCOPE.OAS_DOC_SECURITY_SCOPE_ID)
                .from(OAS_DOC_SECURITY_SCOPE)
                .where(OAS_DOC_SECURITY_SCOPE.OAS_DOC_SECURITY_ID.eq(oasDocSecurityId))
                .fetch()
                .forEach(record -> existing.put(record.value1(), record.value2()));
        Set<String> desired = new HashSet<>(scopes);
        for (Map.Entry<String, ULong> e : existing.entrySet()) {
            if (!desired.contains(e.getKey())) {
                dslContext().deleteFrom(OAS_DOC_SECURITY_SCOPE)
                        .where(OAS_DOC_SECURITY_SCOPE.OAS_DOC_SECURITY_SCOPE_ID.eq(e.getValue()))
                        .execute();
            }
        }
        for (String scopeName : scopes) {
            if (!existing.containsKey(scopeName)) {
                dslContext().insertInto(OAS_DOC_SECURITY_SCOPE)
                        .set(OAS_DOC_SECURITY_SCOPE.GUID, randomGuid())
                        .set(OAS_DOC_SECURITY_SCOPE.OAS_DOC_SECURITY_ID, oasDocSecurityId)
                        .set(OAS_DOC_SECURITY_SCOPE.SCOPE_NAME, scopeName)
                        .set(OAS_DOC_SECURITY_SCOPE.CREATED_BY, requesterId)
                        .set(OAS_DOC_SECURITY_SCOPE.LAST_UPDATED_BY, requesterId)
                        .set(OAS_DOC_SECURITY_SCOPE.CREATION_TIMESTAMP, timestamp)
                        .set(OAS_DOC_SECURITY_SCOPE.LAST_UPDATE_TIMESTAMP, timestamp)
                        .execute();
            }
        }
    }

    private void insertDocSecurityRequirementEntry(ULong oasDocId, int requirementGroup, ULong oasSecuritySchemeId,
                                                   List<String> scopes, ULong requesterId, LocalDateTime timestamp) {
        ULong oasDocSecurityId = dslContext().insertInto(OAS_DOC_SECURITY)
                .set(OAS_DOC_SECURITY.GUID, randomGuid())
                .set(OAS_DOC_SECURITY.OAS_DOC_ID, oasDocId)
                .set(OAS_DOC_SECURITY.REQUIREMENT_GROUP, requirementGroup)
                .set(OAS_DOC_SECURITY.OAS_SECURITY_SCHEME_ID, oasSecuritySchemeId)
                .set(OAS_DOC_SECURITY.CREATED_BY, requesterId)
                .set(OAS_DOC_SECURITY.LAST_UPDATED_BY, requesterId)
                .set(OAS_DOC_SECURITY.CREATION_TIMESTAMP, timestamp)
                .set(OAS_DOC_SECURITY.LAST_UPDATE_TIMESTAMP, timestamp)
                .returning(OAS_DOC_SECURITY.OAS_DOC_SECURITY_ID)
                .fetchOne().getOasDocSecurityId();
        insertDocSecurityScopes(oasDocSecurityId, scopes, requesterId, timestamp);
    }

    private void insertDocSecurityScopes(ULong oasDocSecurityId, List<String> scopes,
                                         ULong requesterId, LocalDateTime timestamp) {
        if (scopes == null || scopes.isEmpty()) {
            return;
        }
        Set<String> usedScopeNames = new HashSet<>();
        for (String scope : scopes) {
            if (scope == null || scope.isBlank()) {
                continue;
            }
            String scopeName = scope.trim();
            if (!usedScopeNames.add(scopeName)) {
                continue;
            }
            dslContext().insertInto(OAS_DOC_SECURITY_SCOPE)
                    .set(OAS_DOC_SECURITY_SCOPE.GUID, randomGuid())
                    .set(OAS_DOC_SECURITY_SCOPE.OAS_DOC_SECURITY_ID, oasDocSecurityId)
                    .set(OAS_DOC_SECURITY_SCOPE.SCOPE_NAME, scopeName)
                    .set(OAS_DOC_SECURITY_SCOPE.CREATED_BY, requesterId)
                    .set(OAS_DOC_SECURITY_SCOPE.LAST_UPDATED_BY, requesterId)
                    .set(OAS_DOC_SECURITY_SCOPE.CREATION_TIMESTAMP, timestamp)
                    .set(OAS_DOC_SECURITY_SCOPE.LAST_UPDATE_TIMESTAMP, timestamp)
                    .execute();
        }
    }

    // Issue #1729: resolve a unique components.securitySchemes key. A blank Scheme Name falls back to a
    // type-derived default; collisions (blank or explicit) get a numeric suffix so the per-doc unique key
    // never throws.
    private String resolveUniqueSchemeName(OasSecurityScheme scheme, Set<String> usedNames) {
        String base = (scheme.getSchemeName() != null && !scheme.getSchemeName().isBlank())
                ? scheme.getSchemeName().trim()
                : typeDefaultName(scheme);
        if (!usedNames.contains(base)) {
            return base;
        }
        int i = 2;
        while (usedNames.contains(base + i)) {
            i++;
        }
        return base + i;
    }

    private String typeDefaultName(OasSecurityScheme scheme) {
        String type = scheme.getType();
        if ("apiKey".equalsIgnoreCase(type)) {
            return "ApiKeyAuth";
        }
        if ("http".equalsIgnoreCase(type)) {
            return "basic".equalsIgnoreCase(scheme.getHttpScheme()) ? "BasicAuth" : "BearerAuth";
        }
        if ("openIdConnect".equalsIgnoreCase(type)) {
            return "OpenID";
        }
        return "OAuth2";
    }

}
