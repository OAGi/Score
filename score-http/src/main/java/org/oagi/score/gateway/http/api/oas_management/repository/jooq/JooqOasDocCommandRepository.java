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
import java.util.List;
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
     * hardcoded OAuth2 default and persists NO row. Replace strategy: delete all then re-insert one row
     * per scheme (apiKey | http | oauth2 | openIdConnect). Scheme names are unique per doc (DB UK).
     */
    private void saveSecuritySchemes(ULong oasDocId, List<OasSecurityScheme> schemes,
                                     ULong requesterId, LocalDateTime timestamp) {
        // No ON DELETE CASCADE: delete children first (oauth scope -> flow -> scheme) for this doc.
        dslContext().deleteFrom(OAS_OAUTH_SCOPE)
                .where(OAS_OAUTH_SCOPE.OAS_OAUTH_FLOW_ID.in(
                        dslContext().select(OAS_OAUTH_FLOW.OAS_OAUTH_FLOW_ID).from(OAS_OAUTH_FLOW)
                                .where(OAS_OAUTH_FLOW.OAS_SECURITY_SCHEME_ID.in(
                                        dslContext().select(OAS_SECURITY_SCHEME.OAS_SECURITY_SCHEME_ID)
                                                .from(OAS_SECURITY_SCHEME)
                                                .where(OAS_SECURITY_SCHEME.OAS_DOC_ID.eq(oasDocId))))))
                .execute();
        dslContext().deleteFrom(OAS_OAUTH_FLOW)
                .where(OAS_OAUTH_FLOW.OAS_SECURITY_SCHEME_ID.in(
                        dslContext().select(OAS_SECURITY_SCHEME.OAS_SECURITY_SCHEME_ID).from(OAS_SECURITY_SCHEME)
                                .where(OAS_SECURITY_SCHEME.OAS_DOC_ID.eq(oasDocId))))
                .execute();
        dslContext().deleteFrom(OAS_SECURITY_SCHEME)
                .where(OAS_SECURITY_SCHEME.OAS_DOC_ID.eq(oasDocId))
                .execute();

        if (schemes == null || schemes.isEmpty()) {
            return;
        }
        Set<String> usedNames = new HashSet<>();
        for (OasSecurityScheme scheme : schemes) {
            if (scheme == null || scheme.getType() == null || scheme.getType().isBlank()) {
                continue;
            }
            String type = scheme.getType();
            OasSecuritySchemeRecord record = new OasSecuritySchemeRecord();
            record.setGuid(randomGuid());
            record.setOasDocId(oasDocId);
            record.setType(type);
            String schemeName = resolveUniqueSchemeName(scheme, usedNames);
            usedNames.add(schemeName);
            record.setSchemeName(schemeName);
            record.setDescription(scheme.getDescription());
            if ("apiKey".equalsIgnoreCase(type)) {
                record.setApiKeyName(scheme.getApiKeyName());
                record.setApiKeyIn(scheme.getApiKeyIn());
            } else if ("http".equalsIgnoreCase(type)) {
                record.setHttpScheme(scheme.getHttpScheme());
                if ("bearer".equalsIgnoreCase(scheme.getHttpScheme())) {
                    record.setBearerFormat(scheme.getBearerFormat());
                }
            } else if ("openIdConnect".equalsIgnoreCase(type)) {
                record.setOpenIdConnectUrl(scheme.getOpenIdConnectUrl());
            }
            record.setDeprecated((byte) 0);
            record.setCreatedBy(requesterId);
            record.setLastUpdatedBy(requesterId);
            record.setCreationTimestamp(timestamp);
            record.setLastUpdateTimestamp(timestamp);
            ULong oasSecuritySchemeId = dslContext().insertInto(OAS_SECURITY_SCHEME)
                    .set(record)
                    .returning(OAS_SECURITY_SCHEME.OAS_SECURITY_SCHEME_ID)
                    .fetchOne().getOasSecuritySchemeId();

            // Issue #1729: persist the OAuth Flows Object (flows + scopes) for oauth2 schemes.
            if ("oauth2".equalsIgnoreCase(type) && scheme.getFlows() != null) {
                saveOAuthFlows(oasSecuritySchemeId, scheme.getFlows(), requesterId, timestamp);
            }
        }
    }

    // Issue #1729: persist a scheme's OAuth flows and their scopes. There is no ON DELETE CASCADE; the
    // replace strategy in saveSecuritySchemes deletes the existing scopes -> flows -> schemes first.
    private void saveOAuthFlows(ULong oasSecuritySchemeId, List<OasOAuthFlow> flows,
                                ULong requesterId, LocalDateTime timestamp) {
        Set<String> usedFlowTypes = new HashSet<>();
        for (OasOAuthFlow flow : flows) {
            if (flow == null || flow.getFlowType() == null || flow.getFlowType().isBlank()) {
                continue;
            }
            if (!usedFlowTypes.add(flow.getFlowType())) {
                continue; // at most one flow per type (DB unique key)
            }
            OasOauthFlowRecord flowRecord = new OasOauthFlowRecord();
            flowRecord.setGuid(randomGuid());
            flowRecord.setOasSecuritySchemeId(oasSecuritySchemeId);
            flowRecord.setFlowType(flow.getFlowType());
            flowRecord.setAuthorizationUrl(flow.getAuthorizationUrl());
            flowRecord.setTokenUrl(flow.getTokenUrl());
            flowRecord.setRefreshUrl(flow.getRefreshUrl());
            flowRecord.setDeviceAuthorizationUrl(flow.getDeviceAuthorizationUrl());
            flowRecord.setCreatedBy(requesterId);
            flowRecord.setLastUpdatedBy(requesterId);
            flowRecord.setCreationTimestamp(timestamp);
            flowRecord.setLastUpdateTimestamp(timestamp);
            ULong oasOAuthFlowId = dslContext().insertInto(OAS_OAUTH_FLOW)
                    .set(flowRecord)
                    .returning(OAS_OAUTH_FLOW.OAS_OAUTH_FLOW_ID)
                    .fetchOne().getOasOauthFlowId();

            if (flow.getScopes() == null) {
                continue;
            }
            Set<String> usedScopeNames = new HashSet<>();
            for (OasOAuthScope scope : flow.getScopes()) {
                if (scope == null || scope.getScopeName() == null || scope.getScopeName().isBlank()) {
                    continue;
                }
                if (!usedScopeNames.add(scope.getScopeName())) {
                    continue; // at most one scope per name (DB unique key)
                }
                OasOauthScopeRecord scopeRecord = new OasOauthScopeRecord();
                scopeRecord.setGuid(randomGuid());
                scopeRecord.setOasOauthFlowId(oasOAuthFlowId);
                scopeRecord.setScopeName(scope.getScopeName());
                scopeRecord.setDescription(scope.getDescription());
                scopeRecord.setCreatedBy(requesterId);
                scopeRecord.setLastUpdatedBy(requesterId);
                scopeRecord.setCreationTimestamp(timestamp);
                scopeRecord.setLastUpdateTimestamp(timestamp);
                dslContext().insertInto(OAS_OAUTH_SCOPE).set(scopeRecord).execute();
            }
        }
    }

    private void saveDocSecurityRequirements(ULong oasDocId, List<OasSecurityRequirement> requirements,
                                             ULong requesterId, LocalDateTime timestamp) {
        dslContext().deleteFrom(OAS_DOC_SECURITY_SCOPE)
                .where(OAS_DOC_SECURITY_SCOPE.OAS_DOC_SECURITY_ID.in(
                        dslContext().select(OAS_DOC_SECURITY.OAS_DOC_SECURITY_ID)
                                .from(OAS_DOC_SECURITY)
                                .where(OAS_DOC_SECURITY.OAS_DOC_ID.eq(oasDocId))))
                .execute();
        dslContext().deleteFrom(OAS_DOC_SECURITY)
                .where(OAS_DOC_SECURITY.OAS_DOC_ID.eq(oasDocId))
                .execute();

        if (requirements == null || requirements.isEmpty()) {
            return;
        }

        for (int i = 0; i < requirements.size(); i++) {
            OasSecurityRequirement requirement = requirements.get(i);
            if (requirement == null) {
                continue;
            }
            if (requirement.isAnonymous()) {
                insertDocSecurityRequirementEntry(oasDocId, i, null, Collections.emptyList(), requesterId, timestamp);
                continue;
            }
            if (requirement.getSchemes() == null) {
                continue;
            }
            Set<String> usedSchemeNames = new HashSet<>();
            for (OasSecurityRequirementScheme scheme : requirement.getSchemes()) {
                if (scheme == null || scheme.getSchemeName() == null || scheme.getSchemeName().isBlank()) {
                    continue;
                }
                String schemeName = scheme.getSchemeName().trim();
                if (!usedSchemeNames.add(schemeName)) {
                    continue;
                }
                insertDocSecurityRequirementEntry(oasDocId, i, schemeName, scheme.getScopes(), requesterId, timestamp);
            }
        }
    }

    private void insertDocSecurityRequirementEntry(ULong oasDocId, int requirementGroup, String schemeName,
                                                   List<String> scopes, ULong requesterId, LocalDateTime timestamp) {
        ULong oasDocSecurityId = dslContext().insertInto(OAS_DOC_SECURITY)
                .set(OAS_DOC_SECURITY.GUID, randomGuid())
                .set(OAS_DOC_SECURITY.OAS_DOC_ID, oasDocId)
                .set(OAS_DOC_SECURITY.REQUIREMENT_GROUP, requirementGroup)
                .set(OAS_DOC_SECURITY.SCHEME_NAME, schemeName)
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
