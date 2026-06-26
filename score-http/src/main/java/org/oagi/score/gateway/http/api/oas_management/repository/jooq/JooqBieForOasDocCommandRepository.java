package org.oagi.score.gateway.http.api.oas_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.*;
import org.oagi.score.gateway.http.api.oas_management.model.BieForOasDoc;
import org.oagi.score.gateway.http.api.oas_management.model.OasDocId;
import org.oagi.score.gateway.http.api.oas_management.model.OasSecurityRequirement;
import org.oagi.score.gateway.http.api.oas_management.model.OpenAPIErrorResponseBodyType;
import org.oagi.score.gateway.http.api.oas_management.repository.BieForOasDocCommandRepository;
import org.oagi.score.gateway.http.common.model.AccessControl;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.base.ScoreDataAccessException;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.*;
import org.oagi.score.gateway.http.common.util.StringUtils;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

        // Issue #1492: reject an inline edit producing two same-type bodies on one
        // (path, verb), or two distinct operations on one (path, verb).
        assertNoDuplicateBodySlot(request.getBieForOasDocList());

        // Message Body flip (Request<->Response): the body types this update submits per operation. An
        // UPDATE only ever INSERTs a new body when a row's Message Body was flipped in place (genuine adds
        // persist through the assign/add-operation endpoints and reload, so they never arrive here as a new
        // body). On such an INSERT the opposite body is removed UNLESS the same payload still carries a row
        // of that opposite type for the operation (a legitimately dual-body endpoint the user kept). This
        // implements the (operation, message-body)-keyed reconcile while never touching an unchanged or
        // off-page sibling, because it is gated on the DB INSERT signal, not on payload inference.
        Map<ULong, Set<String>> submittedBodyTypesByOp = new HashMap<>();
        for (BieForOasDoc submitted : request.getBieForOasDocList()) {
            if (submitted.getOasOperationId() == null) {
                continue;
            }
            submittedBodyTypesByOp
                    .computeIfAbsent(valueOf(submitted.getOasOperationId()), k -> new HashSet<>())
                    .add(submitted.getMessageBody());
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
        // Issue #1347: the same twin-row guard also covers the per-operation error-response body type.
        Set<ULong> securityProcessedOps = new HashSet<>();
        for (BieForOasDoc bieForOasDoc : request.getBieForOasDocList()) {
            if (bieForOasDoc.getMessageBody().equals("Request")) {
                //update oasTag
                OasResourceTagRecord req_oasResourceTagRecord = dslContext().selectFrom(OAS_RESOURCE_TAG.as("req_oas_resource_tag"))
                        .where(OAS_RESOURCE_TAG.as("req_oas_resource_tag").OAS_OPERATION_ID.eq(valueOf(bieForOasDoc.getOasOperationId())))
                        .fetchOptional().orElse(null);
                if (req_oasResourceTagRecord == null) {
                    if (StringUtils.hasLength(bieForOasDoc.getTagName())) {
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
                    if (!StringUtils.hasLength(bieForOasDoc.getTagName())) {
                        // The tag was cleared: drop the association and the now-orphan tag so generation
                        // does not emit an empty `tags: [""]`.
                        dslContext().deleteFrom(OAS_RESOURCE_TAG)
                                .where(OAS_RESOURCE_TAG.OAS_OPERATION_ID.eq(valueOf(bieForOasDoc.getOasOperationId())))
                                .execute();
                        dslContext().deleteFrom(OAS_TAG).where(OAS_TAG.OAS_TAG_ID.eq(oasTagId)).execute();
                    } else {
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
                // Issue #1492: the Request branch no longer deletes the operation's Response
                // body. One operation owns at most one Request AND one Response; editing the Request must
                // leave the sibling Response intact (the old delete-opposite-body behavior is removed).
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

                    // Message Body flipped Response->Request: a new Request was just INSERTed in an UPDATE,
                    // which only happens for an in-place body-type flip. Convert in place by removing the
                    // now-replaced Response body, unless the payload still carries a Response row for this
                    // operation (a kept dual-body endpoint).
                    if (!submittedBodyTypesByOp
                            .getOrDefault(valueOf(bieForOasDoc.getOasOperationId()), Collections.emptySet())
                            .contains("Response")) {
                        deleteResponseBodyOnly(valueOf(bieForOasDoc.getOasOperationId()));
                    }
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
                    if (StringUtils.hasLength(bieForOasDoc.getTagName())) {
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
                    if (!StringUtils.hasLength(bieForOasDoc.getTagName())) {
                        // The tag was cleared: drop the association and the now-orphan tag so generation
                        // does not emit an empty `tags: [""]`.
                        dslContext().deleteFrom(OAS_RESOURCE_TAG)
                                .where(OAS_RESOURCE_TAG.OAS_OPERATION_ID.eq(valueOf(bieForOasDoc.getOasOperationId())))
                                .execute();
                        dslContext().deleteFrom(OAS_TAG).where(OAS_TAG.OAS_TAG_ID.eq(oasTagId)).execute();
                    } else {
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
                }

                //update arrayIndicator and SuppressRootIndicator
                ULong oasRequestId = null;
                ULong oasResponseId = null;
                OasResponseRecord oasResponseRecord = dslContext().selectFrom(OAS_RESPONSE).where(OAS_RESPONSE.OAS_OPERATION_ID.eq(valueOf(bieForOasDoc.getOasOperationId()))).fetchOptional().orElse(null);
                // Issue #1492: the Response branch no longer deletes the operation's Request
                // body. One operation owns at most one Request AND one Response; editing the Response must
                // leave the sibling Request intact (the old delete-opposite-body behavior is removed).
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

                    // Message Body flipped Request->Response (the reported bug): a new Response was just
                    // INSERTed in an UPDATE, which only happens for an in-place body-type flip. Convert in
                    // place by removing the now-replaced Request body, unless the payload still carries a
                    // Request row for this operation (a kept dual-body endpoint).
                    if (!submittedBodyTypesByOp
                            .getOrDefault(valueOf(bieForOasDoc.getOasOperationId()), Collections.emptySet())
                            .contains("Request")) {
                        deleteRequestBodyOnly(valueOf(bieForOasDoc.getOasOperationId()));
                    }
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
                // Issue #1347: persist the operation's error-response body type + ConfirmMessage BIE once
                // per operation (same dedup), so the Request/Response twin rows don't clobber each other.
                securityChanged |= saveOperationErrorResponseBody(
                        valueOf(bieForOasDoc.getOasOperationId()),
                        bieForOasDoc.getErrorResponseBodyType(),
                        bieForOasDoc.getConfirmMessageTopLevelAsbiepId(),
                        valueOf(requesterId),
                        timestamp);
            }
        }
        return new UpdateBieForOasDocResponse(oasDocId, !oasResourceChangedField.isEmpty() || !oasOperationChangedField.isEmpty()
                || !oasRequestChangedField.isEmpty() || !oasResponseChangedField.isEmpty() || !oasTagChangeField.isEmpty()
                || securityChanged);
    }

    // Issue #1492: an inline edit must not produce, within one document, two bodies of the same type on a
    // (resourceName, verb) — i.e. a duplicate (Resource Name, Verb, Message Body). A (resourceName, verb) MAY
    // be backed by more than one oas_operation row (e.g. a legacy/imported doc that stored the Request and
    // Response as separate operations, possibly with different auto-derived operationIds); the generator
    // merges those into one path item, so saving such a doc must not be blocked. The rejected condition
    // surfaces as the contracted 400 message (mapped from IllegalArgumentException). This mirrors the frontend
    // (path, verb, bodyType) mat-error guard.
    static void assertNoDuplicateBodySlot(List<BieForOasDoc> bieForOasDocList) {
        if (bieForOasDocList == null || bieForOasDocList.isEmpty()) {
            return;
        }
        Set<String> seenBodySlots = new HashSet<>();
        for (BieForOasDoc bieForOasDoc : bieForOasDocList) {
            if (bieForOasDoc == null) {
                continue;
            }
            String resourceName = bieForOasDoc.getResourceName();
            String verb = bieForOasDoc.getVerb();
            String messageBody = bieForOasDoc.getMessageBody();
            if (resourceName == null || verb == null || messageBody == null) {
                continue;
            }
            String pathVerb = resourceName + "|" + verb;
            String bodySlot = pathVerb + "|" + messageBody;
            if (!seenBodySlots.add(bodySlot)) {
                throw new IllegalArgumentException(
                        "This operation (" + verb.toUpperCase() + " " + resourceName + ") already has a "
                                + messageBody + " body. An operation can have at most one Request and one Response body.");
            }
        }
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

    // Issue #1347: persist an operation's error-response body type (and, for CONFIRM_MESSAGE, the picked
    // ConfirmMessage BIE) once per operation. Touches the row only when a value actually changes, to avoid
    // needless last_update_timestamp churn and the Request/Response twin-row clobber. The ConfirmMessage id
    // is cleared for any non-CONFIRM_MESSAGE type so a stale FK reference can never linger.
    private boolean saveOperationErrorResponseBody(ULong oasOperationId, String errorResponseBodyType,
                                                   BigInteger confirmTopLevelAsbiepId,
                                                   ULong requesterId, LocalDateTime timestamp) {
        OpenAPIErrorResponseBodyType type = OpenAPIErrorResponseBodyType.from(errorResponseBodyType);
        String desiredType = type.name();
        ULong desiredConfirmId = (type == OpenAPIErrorResponseBodyType.CONFIRM_MESSAGE && confirmTopLevelAsbiepId != null)
                ? ULong.valueOf(confirmTopLevelAsbiepId) : null;

        var current = dslContext()
                .select(OAS_OPERATION.ERROR_RESPONSE_BODY_TYPE, OAS_OPERATION.ERROR_CONFIRM_TOP_LEVEL_ASBIEP_ID)
                .from(OAS_OPERATION)
                .where(OAS_OPERATION.OAS_OPERATION_ID.eq(oasOperationId))
                .fetchOne();
        if (current == null) {
            return false;
        }
        boolean changed = !desiredType.equals(current.get(OAS_OPERATION.ERROR_RESPONSE_BODY_TYPE))
                || !Objects.equals(desiredConfirmId, current.get(OAS_OPERATION.ERROR_CONFIRM_TOP_LEVEL_ASBIEP_ID));
        if (changed) {
            dslContext().update(OAS_OPERATION)
                    .set(OAS_OPERATION.ERROR_RESPONSE_BODY_TYPE, desiredType)
                    .set(OAS_OPERATION.ERROR_CONFIRM_TOP_LEVEL_ASBIEP_ID, desiredConfirmId)
                    .set(OAS_OPERATION.LAST_UPDATED_BY, requesterId)
                    .set(OAS_OPERATION.LAST_UPDATE_TIMESTAMP, timestamp)
                    .where(OAS_OPERATION.OAS_OPERATION_ID.eq(oasOperationId))
                    .execute();
        }
        return changed;
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
        // Issue #1492: one operation owns at most one Request AND one Response. Removing a
        // body deletes ONLY that body (and its children); the operation/resource are removed ONLY when
        // they have no remaining child, so a surviving sibling body is never orphaned/FK-violated.
        for (BieForOasDoc bieForOasDoc : request.getBieForOasDocList()) {
            ULong oasOperationId = valueOf(bieForOasDoc.getOasOperationId());
            if (oasOperationId == null) {
                continue;
            }

            if ("Request".equals(bieForOasDoc.getMessageBody())) {
                if (deleteRequestBodyOnly(oasOperationId)) {
                    deleteOperationAndResourceIfEmpty(oasOperationId);
                }
            }

            if ("Response".equals(bieForOasDoc.getMessageBody())) {
                if (deleteResponseBodyOnly(oasOperationId)) {
                    deleteOperationAndResourceIfEmpty(oasOperationId);
                }
            }
        }

        DeleteBieForOasDocResponse response = new DeleteBieForOasDocResponse(bieForOasDocList);
        return response;
    }

    /**
     * Remove ONLY the operation's Request body and its children (oas_request_parameter) and message body,
     * leaving the operation/resource intact. Returns {@code true} when a Request body existed and was
     * removed. Shared by {@link #deleteBieForOasDoc} (which then calls
     * {@link #deleteOperationAndResourceIfEmpty}) and the in-place Message Body flip in
     * {@link #updateBieForOasDoc} (which does NOT, because the operation still owns the just-inserted
     * opposite body). Keeping one cascade prevents the delete paths from drifting apart.
     */
    private boolean deleteRequestBodyOnly(ULong oasOperationId) {
        if (oasOperationId == null) {
            return false;
        }
        OasRequestRecord oasRequestRecord = dslContext().selectFrom(OAS_REQUEST)
                .where(OAS_REQUEST.OAS_OPERATION_ID.eq(oasOperationId)).fetchOptional().orElse(null);
        if (oasRequestRecord == null) {
            return false;
        }
        // Request children: oas_request_parameter (by oas_request_id), then the request row, then its
        // message body.
        dslContext().delete(OAS_REQUEST_PARAMETER)
                .where(OAS_REQUEST_PARAMETER.OAS_REQUEST_ID.eq(oasRequestRecord.getOasRequestId())).execute();
        dslContext().delete(OAS_REQUEST).where(OAS_REQUEST.OAS_REQUEST_ID.eq(oasRequestRecord.getOasRequestId())).execute();
        dslContext().delete(OAS_MESSAGE_BODY)
                .where(OAS_MESSAGE_BODY.OAS_MESSAGE_BODY_ID.eq(oasRequestRecord.getOasMessageBodyId())).execute();
        return true;
    }

    /**
     * Remove ONLY the operation's Response body and its children (oas_response_headers, oas_parameter_link)
     * and message body, leaving the operation/resource intact. Returns {@code true} when a Response body
     * existed and was removed. See {@link #deleteRequestBodyOnly} for the shared-cascade rationale.
     */
    private boolean deleteResponseBodyOnly(ULong oasOperationId) {
        if (oasOperationId == null) {
            return false;
        }
        OasResponseRecord oasResponseRecord = dslContext().selectFrom(OAS_RESPONSE)
                .where(OAS_RESPONSE.OAS_OPERATION_ID.eq(oasOperationId)).fetchOptional().orElse(null);
        if (oasResponseRecord == null) {
            return false;
        }
        // Response children: oas_response_headers AND oas_parameter_link (by oas_response_id, a NOT NULL
        // RESTRICT FK), then the response row, then its message body.
        dslContext().delete(OAS_RESPONSE_HEADERS)
                .where(OAS_RESPONSE_HEADERS.OAS_RESPONSE_ID.eq(oasResponseRecord.getOasResponseId())).execute();
        dslContext().delete(OAS_PARAMETER_LINK)
                .where(OAS_PARAMETER_LINK.OAS_RESPONSE_ID.eq(oasResponseRecord.getOasResponseId())).execute();
        dslContext().delete(OAS_RESPONSE).where(OAS_RESPONSE.OAS_RESPONSE_ID.eq(oasResponseRecord.getOasResponseId())).execute();
        dslContext().delete(OAS_MESSAGE_BODY)
                .where(OAS_MESSAGE_BODY.OAS_MESSAGE_BODY_ID.eq(oasResponseRecord.getOasMessageBodyId())).execute();
        return true;
    }

    /**
     * Issue #1492: delete the operation (and its resource) ONLY when no body remains.
     * If a sibling Request/Response still references the operation, nothing is removed (the operation
     * and its tag/security stay intact). When the operation IS removed, its tag/security and the
     * nullable {@code oas_parameter_link} operation edge are cleared first; the resource is removed only
     * after its last operation goes away.
     */
    private void deleteOperationAndResourceIfEmpty(ULong oasOperationId) {
        boolean hasRequest = dslContext().fetchExists(
                dslContext().selectOne().from(OAS_REQUEST).where(OAS_REQUEST.OAS_OPERATION_ID.eq(oasOperationId)));
        boolean hasResponse = dslContext().fetchExists(
                dslContext().selectOne().from(OAS_RESPONSE).where(OAS_RESPONSE.OAS_OPERATION_ID.eq(oasOperationId)));
        if (hasRequest || hasResponse) {
            return; // a sibling body still uses this operation -> keep operation + resource.
        }

        OasOperationRecord oasOperationRecord = dslContext().selectFrom(OAS_OPERATION)
                .where(OAS_OPERATION.OAS_OPERATION_ID.eq(oasOperationId)).fetchOptional().orElse(null);
        if (oasOperationRecord == null) {
            return;
        }

        // Operation tag (link + the now-orphan tag).
        OasResourceTagRecord oasResourceTagRecord = dslContext().selectFrom(OAS_RESOURCE_TAG)
                .where(OAS_RESOURCE_TAG.OAS_OPERATION_ID.eq(oasOperationId)).fetchOptional().orElse(null);
        if (oasResourceTagRecord != null) {
            ULong oasTagId = oasResourceTagRecord.getOasTagId();
            dslContext().delete(OAS_RESOURCE_TAG).where(OAS_RESOURCE_TAG.OAS_OPERATION_ID.eq(oasOperationId)).execute();
            dslContext().delete(OAS_TAG).where(OAS_TAG.OAS_TAG_ID.eq(oasTagId)).execute();
        }
        // Operation-level security (scopes -> entries).
        deleteOperationSecurity(oasOperationId);
        // Nullable oas_parameter_link operation edge (its response edge was already cleared per body).
        dslContext().delete(OAS_PARAMETER_LINK).where(OAS_PARAMETER_LINK.OAS_OPERATION_ID.eq(oasOperationId)).execute();

        dslContext().delete(OAS_OPERATION).where(OAS_OPERATION.OAS_OPERATION_ID.eq(oasOperationId)).execute();

        // Remove the resource only when it has no remaining operation.
        ULong oasResourceId = oasOperationRecord.getOasResourceId();
        boolean hasOtherOperation = dslContext().fetchExists(
                dslContext().selectOne().from(OAS_OPERATION).where(OAS_OPERATION.OAS_RESOURCE_ID.eq(oasResourceId)));
        if (!hasOtherOperation) {
            dslContext().delete(OAS_RESOURCE).where(OAS_RESOURCE.OAS_RESOURCE_ID.eq(oasResourceId)).execute();
        }
    }
}
