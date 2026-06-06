package org.oagi.score.gateway.http.api.oas_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.*;
import org.oagi.score.gateway.http.api.oas_management.model.BieForOasDoc;
import org.oagi.score.gateway.http.api.oas_management.model.OasDocId;
import org.oagi.score.gateway.http.api.oas_management.model.OasSecurityRequirement;
import org.oagi.score.gateway.http.api.oas_management.repository.BieForOasDocCommandRepository;
import org.oagi.score.gateway.http.common.model.AccessControl;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.base.ScoreDataAccessException;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.*;
import org.oagi.score.gateway.http.common.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.common.model.ScoreRole.DEVELOPER;
import static org.oagi.score.gateway.http.common.model.ScoreRole.END_USER;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;
import static org.oagi.score.gateway.http.common.util.BooleanUtils.BooleanToByte;
import static org.oagi.score.gateway.http.common.util.ScoreGuidUtils.randomGuid;

public class JooqBieForOasDocCommandRepository extends JooqBaseRepository implements BieForOasDocCommandRepository {

    public JooqBieForOasDocCommandRepository(DSLContext dslContext,
                                             ScoreUser requester,
                                             RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public AddBieForOasDocResponse assignBieForOasDoc(AddBieForOasDocRequest request) throws ScoreDataAccessException {
        return null;
    }

    @Override
    public UpdateBieForOasDocResponse updateBieForOasDoc(UpdateBieForOasDocRequest request) throws ScoreDataAccessException {
        ScoreUser requester = request.getRequester();
        UserId requesterId = requester.userId();
        LocalDateTime timestamp = LocalDateTime.now();

        OasDocId oasDocId = request.getOasDocId();
        if (oasDocId == null) {
            throw new IllegalArgumentException("`oasDocId` parameter must not be null.");
        }

        List<Field<?>> oasResourceChangedField = new ArrayList();
        List<Field<?>> oasOperationChangedField = new ArrayList();
        List<Field<?>> oasRequestChangedField = new ArrayList();
        List<Field<?>> oasResponseChangedField = new ArrayList();
        List<Field<?>> oasTagChangeField = new ArrayList<>();
        boolean securityChanged = false;
        // Issue #1729: resolve each Security Requirement scheme (carried by name) to its
        // oas_security_scheme_id FK; the doc's schemes are persisted via the OasDoc create/update path.
        Map<String, ULong> schemeIdByName = loadSchemeIdByName(valueOf(oasDocId));
        // Issue #1729: an oas_operation appears as up to two rows (Request + Response). Persist its
        // security exactly once to avoid the second row's delete-then-reinsert clobbering the first.
        Set<ULong> securityProcessedOps = new HashSet<>();
        for (BieForOasDoc bieForOasDoc : request.getBieForOasDocList()) {
            if (bieForOasDoc.getMessageBody().equals("Request")) {
                //update oasTag
                OasResourceTagRecord req_oasResourceTagRecord = dslContext().selectFrom(OAS_RESOURCE_TAG.as("req_oas_resource_tag"))
                        .where(OAS_RESOURCE_TAG.as("req_oas_resource_tag").OAS_OPERATION_ID.eq(valueOf(bieForOasDoc.getOasOperationId())))
                        .fetchOptional().orElse(null);
                if (req_oasResourceTagRecord == null) {
                    if (bieForOasDoc.getTagName() != null) {
                        ULong oasTagId = dslContext().insertInto(OAS_TAG)
                                .set(OAS_TAG.GUID, randomGuid())
                                .set(OAS_TAG.NAME, bieForOasDoc.getTagName())
                                .set(OAS_TAG.CREATED_BY, valueOf(requesterId))
                                .set(OAS_TAG.LAST_UPDATED_BY, valueOf(requesterId))
                                .set(OAS_TAG.CREATION_TIMESTAMP, timestamp)
                                .set(OAS_TAG.LAST_UPDATE_TIMESTAMP, timestamp)
                                .returningResult(OAS_TAG.OAS_TAG_ID)
                                .fetchOne().value1();
                        dslContext().insertInto(OAS_RESOURCE_TAG)
                                .set(OAS_RESOURCE_TAG.CREATED_BY, valueOf(requesterId))
                                .set(OAS_RESOURCE_TAG.LAST_UPDATED_BY, valueOf(requesterId))
                                .set(OAS_RESOURCE_TAG.CREATION_TIMESTAMP, timestamp)
                                .set(OAS_RESOURCE_TAG.LAST_UPDATE_TIMESTAMP, timestamp)
                                .set(OAS_RESOURCE_TAG.OAS_TAG_ID, oasTagId)
                                .set(OAS_RESOURCE_TAG.OAS_OPERATION_ID, valueOf(bieForOasDoc.getOasOperationId()))
                                .returningResult(OAS_RESOURCE_TAG.OAS_TAG_ID).fetchOne().value1();
                    }
                } else {
                    ULong oasTagId = req_oasResourceTagRecord.getOasTagId();
                    OasTagRecord oasTagRecord = dslContext().selectFrom(OAS_TAG.as("req_oas_tag"))
                            .where(OAS_TAG.as("req_oas_tag").OAS_TAG_ID.eq(oasTagId)).fetchOptional().orElse(null);
                    if (oasTagRecord != null && !StringUtils.equals(oasTagRecord.getName(), bieForOasDoc.getTagName())) {
                        oasTagChangeField.add(OAS_TAG.NAME);
                        oasTagRecord.setName(bieForOasDoc.getTagName());
                        int affectedRows = oasTagRecord.update(oasTagChangeField);
                        if (affectedRows != 1) {
                            throw new ScoreDataAccessException(new IllegalStateException());
                        }
                    }
                }

                //update oas_resource
                OasResourceRecord oasResourceRecord = dslContext().selectFrom(OAS_RESOURCE.as("req_oas_resource")).where(and(OAS_RESOURCE.as("req_oas_resource").OAS_RESOURCE_ID.eq(valueOf(bieForOasDoc.getOasResourceId())),
                        OAS_RESOURCE.as("req_oas_resource").OAS_DOC_ID.eq(valueOf(oasDocId)))).fetchOptional().orElse(null);
                if (oasResourceRecord == null) {
                    throw new ScoreDataAccessException(new IllegalArgumentException());
                }
                if (oasResourceRecord != null && !StringUtils.equals(bieForOasDoc.getResourceName(), oasResourceRecord.getPath())) {
                    oasResourceChangedField.add(OAS_RESOURCE.as("req_oas_resource").PATH);
                    oasResourceRecord.setPath(bieForOasDoc.getResourceName());
                    oasResourceChangedField.add(OAS_RESOURCE.as("req_oas_resource").LAST_UPDATED_BY);
                    oasResourceRecord.setLastUpdatedBy(valueOf(requesterId));
                    oasResourceChangedField.add(OAS_RESOURCE.as("req_oas_resource").LAST_UPDATE_TIMESTAMP);
                    oasResourceRecord.setLastUpdateTimestamp(timestamp);
                    int affectedRows = oasResourceRecord.update(oasResourceChangedField);
                    if (affectedRows != 1) {
                        throw new ScoreDataAccessException(new IllegalStateException());
                    }
                }
                //update oas_operation
                OasOperationRecord oasOperationRecord = dslContext().selectFrom(OAS_OPERATION.as("req_oas_operation")).where(and(OAS_OPERATION.as("req_oas_operation").OAS_RESOURCE_ID.eq(valueOf(bieForOasDoc.getOasResourceId())),
                        OAS_OPERATION.as("req_oas_operation").OAS_OPERATION_ID.eq(valueOf(bieForOasDoc.getOasOperationId())))).fetchOptional().orElse(null);
                if (oasOperationRecord != null) {
                    if (!StringUtils.equals(bieForOasDoc.getOperationId(), oasOperationRecord.getOperationId())) {
                        oasOperationChangedField.add(OAS_OPERATION.as("req_oas_operation").OPERATION_ID);
                        oasOperationRecord.setOperationId(bieForOasDoc.getOperationId());
                    }

                    if (!StringUtils.equals(bieForOasDoc.getVerb(), oasOperationRecord.getVerb())) {
                        oasOperationChangedField.add(OAS_OPERATION.as("req_oas_operation").VERB);
                        oasOperationRecord.setVerb(bieForOasDoc.getVerb());
                    }
                    oasOperationRecord.setLastUpdatedBy(valueOf(requesterId));
                    oasOperationChangedField.add(OAS_OPERATION.as("req_oas_operation").LAST_UPDATED_BY);
                    oasOperationChangedField.add(OAS_OPERATION.as("req_oas_operation").LAST_UPDATE_TIMESTAMP);
                    oasResourceRecord.setLastUpdateTimestamp(timestamp);
                    int affectedRows = oasOperationRecord.update(oasOperationChangedField);
                    if (affectedRows != 1) {
                        throw new ScoreDataAccessException(new IllegalStateException());
                    }
                }
                //update arrayIndicator and SuppressRootIndicator
                ULong oasRequestId = null;
                ULong oasResponseId = null;
                OasRequestRecord oasRequestRecord = dslContext().selectFrom(OAS_REQUEST).where(OAS_REQUEST.OAS_OPERATION_ID.eq(valueOf(bieForOasDoc.getOasOperationId()))).fetchOptional().orElse(null);
                OasResponseRecord oasResponseRecord = dslContext().selectFrom(OAS_RESPONSE).where(OAS_RESPONSE.OAS_OPERATION_ID.eq(valueOf(bieForOasDoc.getOasOperationId()))).fetchOptional().orElse(null);
                if (oasResponseRecord != null) {
                    dslContext().delete(OAS_RESPONSE).where(OAS_RESPONSE.OAS_OPERATION_ID.eq(valueOf(bieForOasDoc.getOasOperationId()))).execute();
                    dslContext().delete(OAS_MESSAGE_BODY).where(OAS_MESSAGE_BODY.OAS_MESSAGE_BODY_ID.eq(oasResponseRecord.getOasMessageBodyId())).execute();
                }
                if (oasRequestRecord == null) {
                    ULong oasMessageBodyId = dslContext().insertInto(OAS_MESSAGE_BODY)
                            .set(OAS_MESSAGE_BODY.CREATED_BY, valueOf(requesterId))
                            .set(OAS_MESSAGE_BODY.LAST_UPDATED_BY, valueOf(requesterId))
                            .set(OAS_MESSAGE_BODY.TOP_LEVEL_ASBIEP_ID, valueOf(bieForOasDoc.getTopLevelAsbiepId()))
                            .set(OAS_MESSAGE_BODY.CREATION_TIMESTAMP, timestamp)
                            .set(OAS_MESSAGE_BODY.LAST_UPDATE_TIMESTAMP, timestamp)
                            .returningResult(OAS_MESSAGE_BODY.OAS_MESSAGE_BODY_ID)
                            .fetchOne().value1();

                    oasRequestId = dslContext().insertInto(OAS_REQUEST)
                            .set(OAS_REQUEST.CREATED_BY, valueOf(requesterId))
                            .set(OAS_REQUEST.LAST_UPDATED_BY, valueOf(requesterId))
                            .set(OAS_REQUEST.CREATION_TIMESTAMP, timestamp)
                            .set(OAS_REQUEST.LAST_UPDATE_TIMESTAMP, timestamp)
                            .set(OAS_REQUEST.OAS_MESSAGE_BODY_ID, oasMessageBodyId)
                            .set(OAS_REQUEST.OAS_OPERATION_ID, valueOf(bieForOasDoc.getOasOperationId()))
                            .set(OAS_REQUEST.SUPPRESS_ROOT_INDICATOR, (byte) (bieForOasDoc.isSuppressRootIndicator() ? 1 : 0))
                            .set(OAS_REQUEST.MAKE_ARRAY_INDICATOR, (byte) (bieForOasDoc.isArrayIndicator() ? 1 : 0))
                            .set(OAS_REQUEST.IS_CALLBACK, (byte) 0)
                            .set(OAS_REQUEST.REQUIRED, (byte) (1))
                            .returningResult(OAS_REQUEST.OAS_REQUEST_ID)
                            .fetchOne().value1();
                } else {

                    if (BooleanToByte(bieForOasDoc.isArrayIndicator()) != oasRequestRecord.getMakeArrayIndicator()) {
                        oasRequestChangedField.add(OAS_REQUEST.MAKE_ARRAY_INDICATOR);
                        oasRequestRecord.setMakeArrayIndicator(BooleanToByte(bieForOasDoc.isArrayIndicator()));
                    }

                    if (BooleanToByte(bieForOasDoc.isSuppressRootIndicator()) != oasRequestRecord.getSuppressRootIndicator()) {
                        oasRequestChangedField.add(OAS_REQUEST.SUPPRESS_ROOT_INDICATOR);
                        oasRequestRecord.setSuppressRootIndicator(BooleanToByte(bieForOasDoc.isSuppressRootIndicator()));
                    }

                    oasRequestChangedField.add(OAS_REQUEST.LAST_UPDATED_BY);
                    oasRequestRecord.setLastUpdatedBy(valueOf(requesterId));
                    oasRequestChangedField.add(OAS_REQUEST.LAST_UPDATE_TIMESTAMP);
                    oasRequestRecord.setLastUpdateTimestamp(timestamp);
                    int affectedRows = oasRequestRecord.update(oasRequestChangedField);
                    if (affectedRows != 1) {
                        throw new ScoreDataAccessException(new IllegalStateException());
                    }
                }

            }

            if (bieForOasDoc.getMessageBody().equals("Response")) {
                //update oas_resource
                OasResourceRecord oasResourceRecord = dslContext().selectFrom(OAS_RESOURCE.as("res_oas_resource")).where(and(OAS_RESOURCE.as("res_oas_resource").OAS_RESOURCE_ID.eq(valueOf(bieForOasDoc.getOasResourceId())),
                        OAS_RESOURCE.as("res_oas_resource").OAS_DOC_ID.eq(valueOf(oasDocId)))).fetchOptional().orElse(null);
                if (oasResourceRecord == null) {
                    throw new ScoreDataAccessException(new IllegalArgumentException());
                }
                if (oasResourceRecord != null && !StringUtils.equals(bieForOasDoc.getResourceName(), oasResourceRecord.getPath())) {
                    oasResourceChangedField.add(OAS_RESOURCE.as("res_oas_resource").PATH);
                    oasResourceRecord.setPath(bieForOasDoc.getResourceName());
                    oasResourceChangedField.add(OAS_RESOURCE.as("res_oas_resource").LAST_UPDATED_BY);
                    oasResourceRecord.setLastUpdatedBy(valueOf(requesterId));
                    oasResourceChangedField.add(OAS_RESOURCE.as("res_oas_resource").LAST_UPDATE_TIMESTAMP);
                    oasResourceRecord.setLastUpdateTimestamp(timestamp);
                    int affectedRows = oasResourceRecord.update(oasResourceChangedField);
                    if (affectedRows != 1) {
                        throw new ScoreDataAccessException(new IllegalStateException());
                    }
                }
                //update oas_operation
                OasOperationRecord oasOperationRecord = dslContext().selectFrom(OAS_OPERATION.as("res_oas_operation")).where(and(OAS_OPERATION.as("res_oas_operation").OAS_RESOURCE_ID.eq(valueOf(bieForOasDoc.getOasResourceId())),
                        OAS_OPERATION.as("res_oas_operation").OAS_OPERATION_ID.eq(valueOf(bieForOasDoc.getOasOperationId())))).fetchOptional().orElse(null);
                if (oasOperationRecord != null) {
                    if (!StringUtils.equals(bieForOasDoc.getVerb(), oasOperationRecord.getVerb())) {
                        oasOperationChangedField.add(OAS_OPERATION.as("res_oas_operation").VERB);
                        oasOperationRecord.setVerb(bieForOasDoc.getVerb());
                    }

                    if (!StringUtils.equals(bieForOasDoc.getOperationId(), oasOperationRecord.getOperationId())) {
                        oasOperationChangedField.add(OAS_OPERATION.as("res_oas_operation").OPERATION_ID);
                        oasOperationRecord.setOperationId(bieForOasDoc.getOperationId());
                    }
                    // Issue #1729: security_overridden + operation security rows are persisted once per
                    // oas_operation (deduped) in saveOperationSecurityRequirements below, so the flag stays
                    // consistent with the rows regardless of the Request/Response branch.
                    oasOperationChangedField.add(OAS_OPERATION.as("res_oas_operation").LAST_UPDATED_BY);
                    oasOperationRecord.setLastUpdatedBy(valueOf(requesterId));
                    oasOperationChangedField.add(OAS_OPERATION.as("res_oas_operation").LAST_UPDATE_TIMESTAMP);
                    oasResourceRecord.setLastUpdateTimestamp(timestamp);
                    int affectedRows = oasOperationRecord.update(oasOperationChangedField);
                    if (affectedRows != 1) {
                        throw new ScoreDataAccessException(new IllegalStateException());
                    }
                }
                //update oasTag
                OasResourceTagRecord res_oasResourceTagRecord = dslContext().selectFrom(OAS_RESOURCE_TAG.as("res_oas_resource_tag"))
                        .where(OAS_RESOURCE_TAG.as("res_oas_resource_tag").OAS_OPERATION_ID.eq(valueOf(bieForOasDoc.getOasOperationId())))
                        .fetchOptional().orElse(null);
                if (res_oasResourceTagRecord == null) {
                    if (bieForOasDoc.getTagName() != null) {
                        ULong oasTagId = dslContext().insertInto(OAS_TAG)
                                .set(OAS_TAG.GUID, randomGuid())
                                .set(OAS_TAG.NAME, bieForOasDoc.getTagName())
                                .set(OAS_TAG.CREATED_BY, valueOf(requesterId))
                                .set(OAS_TAG.LAST_UPDATED_BY, valueOf(requesterId))
                                .set(OAS_TAG.CREATION_TIMESTAMP, timestamp)
                                .set(OAS_TAG.LAST_UPDATE_TIMESTAMP, timestamp)
                                .returningResult(OAS_TAG.OAS_TAG_ID)
                                .fetchOne().value1();
                        dslContext().insertInto(OAS_RESOURCE_TAG)
                                .set(OAS_RESOURCE_TAG.CREATED_BY, valueOf(requesterId))
                                .set(OAS_RESOURCE_TAG.LAST_UPDATED_BY, valueOf(requesterId))
                                .set(OAS_RESOURCE_TAG.CREATION_TIMESTAMP, timestamp)
                                .set(OAS_RESOURCE_TAG.LAST_UPDATE_TIMESTAMP, timestamp)
                                .set(OAS_RESOURCE_TAG.OAS_TAG_ID, oasTagId)
                                .set(OAS_RESOURCE_TAG.OAS_OPERATION_ID, valueOf(bieForOasDoc.getOasOperationId()))
                                .returningResult(OAS_RESOURCE_TAG.OAS_TAG_ID).fetchOne().value1();
                    }
                } else {
                    ULong oasTagId = res_oasResourceTagRecord.getOasTagId();
                    OasTagRecord oasTagRecord = dslContext().selectFrom(OAS_TAG.as("res_oas_tag"))
                            .where(OAS_TAG.as("res_oas_tag").OAS_TAG_ID.eq(oasTagId)).fetchOptional().orElse(null);
                    if (oasTagRecord != null && !StringUtils.equals(oasTagRecord.getName(), bieForOasDoc.getTagName())) {
                        oasTagChangeField.add(OAS_TAG.NAME);
                        oasTagRecord.setName(bieForOasDoc.getTagName());
                        int affectedRows = oasTagRecord.update(oasTagChangeField);
                        if (affectedRows != 1) {
                            throw new ScoreDataAccessException(new IllegalStateException());
                        }
                    }
                }

                //update arrayIndicator and SuppressRootIndicator
                //update arrayIndicator and SuppressRootIndicator
                ULong oasRequestId = null;
                ULong oasResponseId = null;
                OasRequestRecord oasRequestRecord = dslContext().selectFrom(OAS_REQUEST).where(OAS_REQUEST.OAS_OPERATION_ID.eq(valueOf(bieForOasDoc.getOasOperationId()))).fetchOptional().orElse(null);
                OasResponseRecord oasResponseRecord = dslContext().selectFrom(OAS_RESPONSE).where(OAS_RESPONSE.OAS_OPERATION_ID.eq(valueOf(bieForOasDoc.getOasOperationId()))).fetchOptional().orElse(null);
                if (oasRequestRecord != null) {
                    dslContext().delete(OAS_REQUEST).where(OAS_REQUEST.OAS_OPERATION_ID.eq(valueOf(bieForOasDoc.getOasOperationId()))).execute();
                    dslContext().delete(OAS_MESSAGE_BODY).where(OAS_MESSAGE_BODY.OAS_MESSAGE_BODY_ID.eq(oasRequestRecord.getOasMessageBodyId())).execute();
                }
                if (oasResponseRecord == null) {
                    ULong oasMessageBodyId = dslContext().insertInto(OAS_MESSAGE_BODY)
                            .set(OAS_MESSAGE_BODY.CREATED_BY, valueOf(requesterId))
                            .set(OAS_MESSAGE_BODY.LAST_UPDATED_BY, valueOf(requesterId))
                            .set(OAS_MESSAGE_BODY.TOP_LEVEL_ASBIEP_ID, valueOf(bieForOasDoc.getTopLevelAsbiepId()))
                            .set(OAS_MESSAGE_BODY.CREATION_TIMESTAMP, timestamp)
                            .set(OAS_MESSAGE_BODY.LAST_UPDATE_TIMESTAMP, timestamp)
                            .returningResult(OAS_MESSAGE_BODY.OAS_MESSAGE_BODY_ID)
                            .fetchOne().value1();

                    oasResponseId = dslContext().insertInto(OAS_RESPONSE)
                            .set(OAS_RESPONSE.CREATED_BY, valueOf(requesterId))
                            .set(OAS_RESPONSE.LAST_UPDATED_BY, valueOf(requesterId))
                            .set(OAS_RESPONSE.CREATION_TIMESTAMP, timestamp)
                            .set(OAS_RESPONSE.LAST_UPDATE_TIMESTAMP, timestamp)
                            .set(OAS_RESPONSE.OAS_MESSAGE_BODY_ID, oasMessageBodyId)
                            .set(OAS_RESPONSE.OAS_OPERATION_ID, valueOf(bieForOasDoc.getOasOperationId()))
                            .set(OAS_RESPONSE.SUPPRESS_ROOT_INDICATOR, (byte) (bieForOasDoc.isSuppressRootIndicator() ? 1 : 0))
                            .set(OAS_RESPONSE.MAKE_ARRAY_INDICATOR, (byte) (bieForOasDoc.isArrayIndicator() ? 1 : 0))
                            .set(OAS_RESPONSE.INCLUDE_CONFIRM_INDICATOR, (byte) 0)
                            .returningResult(OAS_RESPONSE.OAS_RESPONSE_ID)
                            .fetchOne().value1();
                } else {
                    if (BooleanToByte(bieForOasDoc.isArrayIndicator()) != oasResponseRecord.getMakeArrayIndicator()) {
                        oasResponseChangedField.add(OAS_RESPONSE.MAKE_ARRAY_INDICATOR);
                        oasResponseRecord.setMakeArrayIndicator(BooleanToByte(bieForOasDoc.isArrayIndicator()));
                    }

                    if (BooleanToByte(bieForOasDoc.isSuppressRootIndicator()) != oasResponseRecord.getSuppressRootIndicator()) {
                        oasResponseChangedField.add(OAS_RESPONSE.SUPPRESS_ROOT_INDICATOR);
                        oasResponseRecord.setSuppressRootIndicator(BooleanToByte(bieForOasDoc.isSuppressRootIndicator()));
                    }

                    oasResponseChangedField.add(OAS_RESPONSE.LAST_UPDATED_BY);
                    oasResponseRecord.setLastUpdatedBy(valueOf(requesterId));
                    oasResponseChangedField.add(OAS_RESPONSE.LAST_UPDATE_TIMESTAMP);
                    oasResponseRecord.setLastUpdateTimestamp(timestamp);
                    int affectedRows = oasResponseRecord.update(oasResponseChangedField);
                    if (affectedRows != 1) {
                        throw new ScoreDataAccessException(new IllegalStateException());
                    }

                }
            }

            if (bieForOasDoc.getOasOperationId() != null
                    && securityProcessedOps.add(valueOf(bieForOasDoc.getOasOperationId()))) {
                securityChanged |= saveOperationSecurityRequirements(
                        valueOf(bieForOasDoc.getOasOperationId()),
                        bieForOasDoc.isSecurityOverridden(),
                        bieForOasDoc.getSecurityRequirements(),
                        schemeIdByName,
                        valueOf(requesterId),
                        timestamp);
            }
        }
        return new UpdateBieForOasDocResponse(oasDocId, !oasResourceChangedField.isEmpty() || !oasOperationChangedField.isEmpty()
                || !oasRequestChangedField.isEmpty() || !oasResponseChangedField.isEmpty() || !oasTagChangeField.isEmpty()
                || securityChanged);
    }

    // Issue #1729: persist an operation's Security Requirement override as a DIFF (not delete-then-reinsert):
    // a (requirement_group, oas_security_scheme_id) pair still present is kept and only its free-text scopes
    // are reconciled; pairs no longer present are deleted; new pairs are inserted. The desired set is empty
    // when the operation inherits (securityOverridden=false) or is public (overridden + empty list).
    private boolean saveOperationSecurityRequirements(ULong oasOperationId, boolean securityOverridden,
                                                      List<OasSecurityRequirement> requirements,
                                                      Map<String, ULong> schemeIdByName,
                                                      ULong requesterId, LocalDateTime timestamp) {
        LinkedHashMap<String, OasSecurityRequirementDiff.Entry> desired = securityOverridden
                ? OasSecurityRequirementDiff.build(requirements, schemeIdByName)
                : new LinkedHashMap<>();

        // Existing rows for this operation, keyed by (requirement_group, scheme id).
        Map<String, ULong> existingIdByKey = new LinkedHashMap<>();
        dslContext().select(OAS_OPERATION_SECURITY.OAS_OPERATION_SECURITY_ID, OAS_OPERATION_SECURITY.REQUIREMENT_GROUP,
                        OAS_OPERATION_SECURITY.OAS_SECURITY_SCHEME_ID)
                .from(OAS_OPERATION_SECURITY)
                .where(OAS_OPERATION_SECURITY.OAS_OPERATION_ID.eq(oasOperationId))
                .fetch()
                .forEach(record -> existingIdByKey.put(
                        OasSecurityRequirementDiff.key(record.get(OAS_OPERATION_SECURITY.REQUIREMENT_GROUP), record.get(OAS_OPERATION_SECURITY.OAS_SECURITY_SCHEME_ID)),
                        record.get(OAS_OPERATION_SECURITY.OAS_OPERATION_SECURITY_ID)));

        boolean changed = false;

        // DELETE entries no longer desired (scopes first; no ON DELETE CASCADE).
        for (Map.Entry<String, ULong> existing : existingIdByKey.entrySet()) {
            if (!desired.containsKey(existing.getKey())) {
                dslContext().deleteFrom(OAS_OPERATION_SECURITY_SCOPE)
                        .where(OAS_OPERATION_SECURITY_SCOPE.OAS_OPERATION_SECURITY_ID.eq(existing.getValue()))
                        .execute();
                dslContext().deleteFrom(OAS_OPERATION_SECURITY)
                        .where(OAS_OPERATION_SECURITY.OAS_OPERATION_SECURITY_ID.eq(existing.getValue()))
                        .execute();
                changed = true;
            }
        }

        // INSERT new entries; reconcile the scopes of kept entries in place.
        for (OasSecurityRequirementDiff.Entry entry : desired.values()) {
            ULong oasOperationSecurityId = existingIdByKey.get(OasSecurityRequirementDiff.key(entry.group, entry.schemeId));
            if (oasOperationSecurityId == null) {
                insertOperationSecurityRequirementEntry(oasOperationId, entry.group, entry.schemeId, entry.scopes, requesterId, timestamp);
                changed = true;
            } else {
                changed |= reconcileOperationSecurityScopes(oasOperationSecurityId, entry.scopes, requesterId, timestamp);
            }
        }

        // Keep oas_operation.security_overridden consistent; touch the operation row only when the flag
        // changes or the requirement rows changed (avoids needless last_update_timestamp churn).
        Byte currentFlag = dslContext().select(OAS_OPERATION.SECURITY_OVERRIDDEN)
                .from(OAS_OPERATION)
                .where(OAS_OPERATION.OAS_OPERATION_ID.eq(oasOperationId))
                .fetchOne(OAS_OPERATION.SECURITY_OVERRIDDEN);
        boolean flagChanged = currentFlag == null || currentFlag != BooleanToByte(securityOverridden);
        if (flagChanged || changed) {
            dslContext().update(OAS_OPERATION)
                    .set(OAS_OPERATION.SECURITY_OVERRIDDEN, BooleanToByte(securityOverridden))
                    .set(OAS_OPERATION.LAST_UPDATED_BY, requesterId)
                    .set(OAS_OPERATION.LAST_UPDATE_TIMESTAMP, timestamp)
                    .where(OAS_OPERATION.OAS_OPERATION_ID.eq(oasOperationId))
                    .execute();
        }
        return changed || flagChanged;
    }

    // Issue #1729: reconcile an operation requirement entry's free-text scopes in place — insert names not
    // yet stored, delete names no longer requested, leave unchanged names (and their ids) untouched.
    private boolean reconcileOperationSecurityScopes(ULong oasOperationSecurityId, List<String> scopes,
                                                     ULong requesterId, LocalDateTime timestamp) {
        Map<String, ULong> existing = new LinkedHashMap<>();
        dslContext().select(OAS_OPERATION_SECURITY_SCOPE.SCOPE_NAME, OAS_OPERATION_SECURITY_SCOPE.OAS_OPERATION_SECURITY_SCOPE_ID)
                .from(OAS_OPERATION_SECURITY_SCOPE)
                .where(OAS_OPERATION_SECURITY_SCOPE.OAS_OPERATION_SECURITY_ID.eq(oasOperationSecurityId))
                .fetch()
                .forEach(record -> existing.put(record.value1(), record.value2()));
        boolean changed = false;
        Set<String> desired = new HashSet<>(scopes);
        for (Map.Entry<String, ULong> e : existing.entrySet()) {
            if (!desired.contains(e.getKey())) {
                dslContext().deleteFrom(OAS_OPERATION_SECURITY_SCOPE)
                        .where(OAS_OPERATION_SECURITY_SCOPE.OAS_OPERATION_SECURITY_SCOPE_ID.eq(e.getValue()))
                        .execute();
                changed = true;
            }
        }
        for (String scopeName : scopes) {
            if (!existing.containsKey(scopeName)) {
                dslContext().insertInto(OAS_OPERATION_SECURITY_SCOPE)
                        .set(OAS_OPERATION_SECURITY_SCOPE.GUID, randomGuid())
                        .set(OAS_OPERATION_SECURITY_SCOPE.OAS_OPERATION_SECURITY_ID, oasOperationSecurityId)
                        .set(OAS_OPERATION_SECURITY_SCOPE.SCOPE_NAME, scopeName)
                        .set(OAS_OPERATION_SECURITY_SCOPE.CREATED_BY, requesterId)
                        .set(OAS_OPERATION_SECURITY_SCOPE.LAST_UPDATED_BY, requesterId)
                        .set(OAS_OPERATION_SECURITY_SCOPE.CREATION_TIMESTAMP, timestamp)
                        .set(OAS_OPERATION_SECURITY_SCOPE.LAST_UPDATE_TIMESTAMP, timestamp)
                        .execute();
                changed = true;
            }
        }
        return changed;
    }

    // Issue #1729: map this doc's components.securitySchemes key -> oas_security_scheme_id, so an
    // operation Security Requirement (carried by name) can be persisted as the FK to oas_security_scheme.
    private Map<String, ULong> loadSchemeIdByName(ULong oasDocId) {
        Map<String, ULong> schemeIdByName = new LinkedHashMap<>();
        dslContext().select(OAS_SECURITY_SCHEME.SCHEME_NAME, OAS_SECURITY_SCHEME.OAS_SECURITY_SCHEME_ID)
                .from(OAS_SECURITY_SCHEME)
                .where(OAS_SECURITY_SCHEME.OAS_DOC_ID.eq(oasDocId))
                .fetch()
                .forEach(record -> schemeIdByName.put(record.value1(), record.value2()));
        return schemeIdByName;
    }

    private void insertOperationSecurityRequirementEntry(ULong oasOperationId, int requirementGroup, ULong oasSecuritySchemeId,
                                                         List<String> scopes, ULong requesterId, LocalDateTime timestamp) {
        ULong oasOperationSecurityId = dslContext().insertInto(OAS_OPERATION_SECURITY)
                .set(OAS_OPERATION_SECURITY.GUID, randomGuid())
                .set(OAS_OPERATION_SECURITY.OAS_OPERATION_ID, oasOperationId)
                .set(OAS_OPERATION_SECURITY.REQUIREMENT_GROUP, requirementGroup)
                .set(OAS_OPERATION_SECURITY.OAS_SECURITY_SCHEME_ID, oasSecuritySchemeId)
                .set(OAS_OPERATION_SECURITY.CREATED_BY, requesterId)
                .set(OAS_OPERATION_SECURITY.LAST_UPDATED_BY, requesterId)
                .set(OAS_OPERATION_SECURITY.CREATION_TIMESTAMP, timestamp)
                .set(OAS_OPERATION_SECURITY.LAST_UPDATE_TIMESTAMP, timestamp)
                .returning(OAS_OPERATION_SECURITY.OAS_OPERATION_SECURITY_ID)
                .fetchOne().getOasOperationSecurityId();
        insertOperationSecurityScopes(oasOperationSecurityId, scopes, requesterId, timestamp);
    }

    private void insertOperationSecurityScopes(ULong oasOperationSecurityId, List<String> scopes,
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
            dslContext().insertInto(OAS_OPERATION_SECURITY_SCOPE)
                    .set(OAS_OPERATION_SECURITY_SCOPE.GUID, randomGuid())
                    .set(OAS_OPERATION_SECURITY_SCOPE.OAS_OPERATION_SECURITY_ID, oasOperationSecurityId)
                    .set(OAS_OPERATION_SECURITY_SCOPE.SCOPE_NAME, scopeName)
                    .set(OAS_OPERATION_SECURITY_SCOPE.CREATED_BY, requesterId)
                    .set(OAS_OPERATION_SECURITY_SCOPE.LAST_UPDATED_BY, requesterId)
                    .set(OAS_OPERATION_SECURITY_SCOPE.CREATION_TIMESTAMP, timestamp)
                    .set(OAS_OPERATION_SECURITY_SCOPE.LAST_UPDATE_TIMESTAMP, timestamp)
                    .execute();
        }
    }

    // Issue #1729: no ON DELETE CASCADE — clear an operation's security (scopes -> entries) before the
    // operation row itself is deleted, to avoid an FK violation.
    private void deleteOperationSecurity(ULong oasOperationId) {
        dslContext().deleteFrom(OAS_OPERATION_SECURITY_SCOPE)
                .where(OAS_OPERATION_SECURITY_SCOPE.OAS_OPERATION_SECURITY_ID.in(
                        dslContext().select(OAS_OPERATION_SECURITY.OAS_OPERATION_SECURITY_ID)
                                .from(OAS_OPERATION_SECURITY)
                                .where(OAS_OPERATION_SECURITY.OAS_OPERATION_ID.eq(oasOperationId))))
                .execute();
        dslContext().deleteFrom(OAS_OPERATION_SECURITY)
                .where(OAS_OPERATION_SECURITY.OAS_OPERATION_ID.eq(oasOperationId))
                .execute();
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public DeleteBieForOasDocResponse deleteBieForOasDoc(DeleteBieForOasDocRequest request) throws ScoreDataAccessException {
        List<BieForOasDoc> bieForOasDocList = request.getBieForOasDocList();
        OasDocId oasDocId = request.getOasDocId();
        if (oasDocId == null) {
            throw new IllegalArgumentException("`oasDocId` parameter must not be null.");
        }

        if (bieForOasDocList == null || bieForOasDocList.isEmpty()) {
            return new DeleteBieForOasDocResponse(Collections.emptyList());
        }
        // based on the message type , delete from oas_request or oas_response
        for (BieForOasDoc bieForOasDoc : request.getBieForOasDocList()) {
            if (bieForOasDoc.getMessageBody().equals("Request")) {
                //delete oas_request
                OasRequestRecord oasRequestRecord = dslContext().selectFrom(OAS_REQUEST).where(OAS_REQUEST.OAS_OPERATION_ID.eq(valueOf(bieForOasDoc.getOasOperationId()))).fetchOptional().orElse(null);
                OasOperationRecord oasOperationRecord = dslContext().selectFrom(OAS_OPERATION).where(OAS_OPERATION.OAS_OPERATION_ID.eq(valueOf(bieForOasDoc.getOasOperationId()))).fetchOptional().orElse(null);
                OasResourceTagRecord req_oasResourceTagRecord = dslContext().selectFrom(OAS_RESOURCE_TAG.as("req_oas_resource_tag"))
                        .where(OAS_RESOURCE_TAG.as("req_oas_resource_tag").OAS_OPERATION_ID.eq(valueOf(bieForOasDoc.getOasOperationId())))
                        .fetchOptional().orElse(null);
                if (oasRequestRecord != null) {
                    dslContext().delete(OAS_REQUEST).where(OAS_REQUEST.OAS_OPERATION_ID.eq(valueOf(bieForOasDoc.getOasOperationId()))).execute();
                    dslContext().delete(OAS_MESSAGE_BODY).where(OAS_MESSAGE_BODY.OAS_MESSAGE_BODY_ID.eq(oasRequestRecord.getOasMessageBodyId())).execute();
                    if (oasOperationRecord != null) {
                        if (req_oasResourceTagRecord != null) {
                            ULong oasTagId = req_oasResourceTagRecord.getOasTagId();
                            dslContext().delete(OAS_RESOURCE_TAG).where(OAS_RESOURCE_TAG.OAS_OPERATION_ID.eq(valueOf(bieForOasDoc.getOasOperationId()))).execute();
                            dslContext().delete(OAS_TAG).where(OAS_TAG.OAS_TAG_ID.eq(oasTagId)).execute();
                        }
                        deleteOperationSecurity(valueOf(bieForOasDoc.getOasOperationId()));
                        dslContext().delete(OAS_OPERATION).where(OAS_OPERATION.OAS_OPERATION_ID.eq(valueOf(bieForOasDoc.getOasOperationId()))).execute();
                        dslContext().delete(OAS_RESOURCE).where(OAS_RESOURCE.OAS_RESOURCE_ID.eq(oasOperationRecord.getOasResourceId())).execute();
                    }
                }
            }

            if (bieForOasDoc.getMessageBody().equals("Response")) {
                //delete oas_response
                OasResponseRecord oasResponseRecord = dslContext().selectFrom(OAS_RESPONSE).where(OAS_RESPONSE.OAS_OPERATION_ID.eq(valueOf(bieForOasDoc.getOasOperationId()))).fetchOptional().orElse(null);
                OasOperationRecord oasOperationRecord = dslContext().selectFrom(OAS_OPERATION).where(OAS_OPERATION.OAS_OPERATION_ID.eq(valueOf(bieForOasDoc.getOasOperationId()))).fetchOptional().orElse(null);
                OasResourceTagRecord res_oasResourceTagRecord = dslContext().selectFrom(OAS_RESOURCE_TAG.as("res_oas_resource_tag"))
                        .where(OAS_RESOURCE_TAG.as("res_oas_resource_tag").OAS_OPERATION_ID.eq(valueOf(bieForOasDoc.getOasOperationId())))
                        .fetchOptional().orElse(null);
                if (oasResponseRecord != null) {
                    dslContext().delete(OAS_RESPONSE).where(OAS_RESPONSE.OAS_OPERATION_ID.eq(valueOf(bieForOasDoc.getOasOperationId()))).execute();
                    dslContext().delete(OAS_MESSAGE_BODY).where(OAS_MESSAGE_BODY.OAS_MESSAGE_BODY_ID.eq(oasResponseRecord.getOasMessageBodyId())).execute();
                    if (oasOperationRecord != null) {
                        if (res_oasResourceTagRecord != null) {
                            ULong oasTagId = res_oasResourceTagRecord.getOasTagId();
                            dslContext().delete(OAS_RESOURCE_TAG).where(OAS_RESOURCE_TAG.OAS_OPERATION_ID.eq(valueOf(bieForOasDoc.getOasOperationId()))).execute();
                            dslContext().delete(OAS_TAG).where(OAS_TAG.OAS_TAG_ID.eq(oasTagId)).execute();
                        }
                        deleteOperationSecurity(valueOf(bieForOasDoc.getOasOperationId()));
                        dslContext().delete(OAS_OPERATION).where(OAS_OPERATION.OAS_OPERATION_ID.eq(valueOf(bieForOasDoc.getOasOperationId()))).execute();
                        dslContext().delete(OAS_RESOURCE).where(OAS_RESOURCE.OAS_RESOURCE_ID.eq(oasOperationRecord.getOasResourceId())).execute();
                    }
                }
            }
        }

        DeleteBieForOasDocResponse response = new DeleteBieForOasDocResponse(bieForOasDocList);
        return response;
    }
}
