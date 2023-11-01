package org.oagi.score.repo.api.impl.jooq.openapidoc;

import org.jooq.Record;
import org.jooq.*;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.bie.model.BieState;
import org.oagi.score.repo.api.impl.jooq.JooqScoreRepository;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.OasResourceTagRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.OasTagRecord;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.oagi.score.repo.api.openapidoc.BieForOasDocReadRepository;
import org.oagi.score.repo.api.openapidoc.model.*;
import org.oagi.score.repo.api.security.AccessControl;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.inline;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.repo.api.impl.utils.StringUtils.trim;
import static org.oagi.score.repo.api.user.model.ScoreRole.DEVELOPER;
import static org.oagi.score.repo.api.user.model.ScoreRole.END_USER;

public class JooqBieForOasDocReadRepository extends JooqScoreRepository
        implements BieForOasDocReadRepository {
    public JooqBieForOasDocReadRepository(DSLContext dslContext) {
        super(dslContext);
    }

    private SelectOnConditionStep selectForRequest() {
        return dslContext().select(
                        TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID,
                        TOP_LEVEL_ASBIEP.STATE,
                        TOP_LEVEL_ASBIEP.VERSION,
                        TOP_LEVEL_ASBIEP.RELEASE_ID,
                        ASBIEP.ASBIEP_ID,
                        ASCCP_MANIFEST.ASCCP_MANIFEST_ID,
                        ASCCP.ASCCP_ID,
                        ASCCP.GUID,
                        ASCCP_MANIFEST.DEN,
                        ASBIEP.REMARK,
                        APP_USER.as("owner").LOGIN_ID.as("owner"),
                        APP_USER.as("owner").APP_USER_ID.as("owner_user_id"),
                        APP_USER.as("owner").LOGIN_ID.as("owner_login_id"),
                        APP_USER.as("owner").IS_DEVELOPER.as("owner_is_developer"),
                        APP_USER.as("owner").IS_ADMIN.as("owner_is_admin"),
                        inline("Request").as("oas_doc_message_body_type"),
                        OAS_DOC.as("oas_doc").OAS_DOC_ID.as("oas_doc_id"),
                        OAS_OPERATION.as("oas_operation").VERB.as("verb"),
                        OAS_TAG.as("oas_tag").NAME.as("tag_name"),
                        OAS_REQUEST.MAKE_ARRAY_INDICATOR.as("array_indicator"),
                        OAS_REQUEST.SUPPRESS_ROOT_INDICATOR.as("suppress_root_indicator"),
                        OAS_RESOURCE.as("oas_resource").PATH.as("resource_name"),
                        OAS_RESOURCE.as("oas_resource").OAS_RESOURCE_ID.as("oas_resource_id"),
                        OAS_OPERATION.as("oas_operation").OPERATION_ID.as("operation_id"),
                        OAS_OPERATION.as("oas_operation").OAS_OPERATION_ID.as("oas_operation_id"),
                        APP_USER.as("creator").APP_USER_ID.as("creator_user_id"),
                        APP_USER.as("creator").LOGIN_ID.as("creator_login_id"),
                        APP_USER.as("creator").IS_DEVELOPER.as("creator_is_developer"),
                        APP_USER.as("updater").APP_USER_ID.as("updater_user_id"),
                        APP_USER.as("updater").LOGIN_ID.as("updater_login_id"),
                        APP_USER.as("updater").IS_DEVELOPER.as("updater_is_developer"),
                        OAS_MESSAGE_BODY.CREATION_TIMESTAMP,
                        OAS_MESSAGE_BODY.LAST_UPDATE_TIMESTAMP)
                .from(OAS_MESSAGE_BODY)
                .leftJoin(OAS_REQUEST).on(OAS_REQUEST.OAS_MESSAGE_BODY_ID.eq(OAS_MESSAGE_BODY.OAS_MESSAGE_BODY_ID))
                .leftJoin(OAS_OPERATION.as("oas_operation")).on(OAS_REQUEST.OAS_OPERATION_ID.eq(OAS_OPERATION.as("oas_operation").OAS_OPERATION_ID))
                .leftJoin(OAS_RESOURCE.as("oas_resource")).on(OAS_OPERATION.as("oas_operation").OAS_OPERATION_ID.eq(OAS_RESOURCE.as("oas_resource").OAS_RESOURCE_ID))
                .leftJoin(OAS_DOC.as("oas_doc")).on(OAS_RESOURCE.as("oas_resource").OAS_DOC_ID.eq(OAS_DOC.as("oas_doc").OAS_DOC_ID))
                .leftJoin(OAS_RESOURCE_TAG.as("oas_resource_tag")).on(OAS_RESOURCE_TAG.as("oas_resource_tag").OAS_OPERATION_ID.eq(OAS_OPERATION.as("oas_operation").OAS_OPERATION_ID))
                .leftJoin(OAS_TAG.as("oas_tag")).on(OAS_RESOURCE_TAG.as("oas_resource_tag").OAS_TAG_ID.eq(OAS_TAG.as("oas_tag").OAS_TAG_ID))
                .rightJoin(TOP_LEVEL_ASBIEP).on(OAS_MESSAGE_BODY.TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID))
                .leftJoin(ASBIEP).on(TOP_LEVEL_ASBIEP.ASBIEP_ID.eq(ASBIEP.ASBIEP_ID))
                .leftJoin(ASCCP_MANIFEST).on(ASBIEP.BASED_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                .leftJoin(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                .join(APP_USER.as("owner")).on(TOP_LEVEL_ASBIEP.OWNER_USER_ID.eq(APP_USER.as("owner").APP_USER_ID))
                .join(APP_USER.as("creator")).on(OAS_MESSAGE_BODY.CREATED_BY.eq(APP_USER.as("creator").APP_USER_ID))
                .join(APP_USER.as("updater")).on(OAS_MESSAGE_BODY.LAST_UPDATED_BY.eq(APP_USER.as("updater").APP_USER_ID));
    }

    private SelectOnConditionStep selectForResponse() {
        return dslContext().select(
                        TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID,
                        TOP_LEVEL_ASBIEP.STATE,
                        TOP_LEVEL_ASBIEP.VERSION,
                        TOP_LEVEL_ASBIEP.RELEASE_ID,
                        ASBIEP.ASBIEP_ID,
                        ASCCP_MANIFEST.ASCCP_MANIFEST_ID,
                        ASCCP.ASCCP_ID,
                        ASCCP.GUID,
                        ASCCP_MANIFEST.DEN,
                        ASBIEP.REMARK,
                        APP_USER.as("owner").LOGIN_ID.as("owner"),
                        APP_USER.as("owner").APP_USER_ID.as("owner_user_id"),
                        APP_USER.as("owner").LOGIN_ID.as("owner_login_id"),
                        APP_USER.as("owner").IS_DEVELOPER.as("owner_is_developer"),
                        APP_USER.as("owner").IS_ADMIN.as("owner_is_admin"),
                        inline("Response").as("oas_doc_message_body_type"),
                        OAS_DOC.as("oas_doc").OAS_DOC_ID.as("oas_doc_id"),
                        OAS_OPERATION.as("oas_operation").VERB.as("verb"),
                        OAS_TAG.as("oas_tag").NAME.as("tag_name"),
                        OAS_RESPONSE.MAKE_ARRAY_INDICATOR.as("array_indicator"),
                        OAS_RESPONSE.SUPPRESS_ROOT_INDICATOR.as("suppress_root_indicator"),
                        OAS_RESOURCE.as("oas_resource").PATH.as("resource_name"),
                        OAS_RESOURCE.as("oas_resource").OAS_RESOURCE_ID.as("oas_resource_id"),
                        OAS_OPERATION.as("oas_operation").OPERATION_ID.as("operation_id"),
                        OAS_OPERATION.as("oas_operation").OAS_OPERATION_ID.as("oas_operation_id"),
                        APP_USER.as("creator").APP_USER_ID.as("creator_user_id"),
                        APP_USER.as("creator").LOGIN_ID.as("creator_login_id"),
                        APP_USER.as("creator").IS_DEVELOPER.as("creator_is_developer"),
                        APP_USER.as("updater").APP_USER_ID.as("updater_user_id"),
                        APP_USER.as("updater").LOGIN_ID.as("updater_login_id"),
                        APP_USER.as("updater").IS_DEVELOPER.as("updater_is_developer"),
                        OAS_MESSAGE_BODY.CREATION_TIMESTAMP,
                        OAS_MESSAGE_BODY.LAST_UPDATE_TIMESTAMP)
                .from(OAS_MESSAGE_BODY)
                .leftJoin(OAS_RESPONSE).on(OAS_RESPONSE.OAS_MESSAGE_BODY_ID.eq(OAS_MESSAGE_BODY.OAS_MESSAGE_BODY_ID))
                .leftJoin(OAS_OPERATION.as("oas_operation")).on(OAS_RESPONSE.OAS_OPERATION_ID.eq(OAS_OPERATION.as("oas_operation").OAS_OPERATION_ID))
                .leftJoin(OAS_RESOURCE.as("oas_resource")).on(OAS_OPERATION.as("oas_operation").OAS_RESOURCE_ID.eq(OAS_RESOURCE.as("oas_resource").OAS_RESOURCE_ID))
                .leftJoin(OAS_DOC.as("oas_doc")).on(OAS_RESOURCE.as("oas_resource").OAS_DOC_ID.eq(OAS_DOC.as("oas_doc").OAS_DOC_ID))
                .leftJoin(OAS_RESOURCE_TAG.as("oas_resource_tag")).on(OAS_RESOURCE_TAG.as("oas_resource_tag").OAS_OPERATION_ID.eq(OAS_OPERATION.as("oas_operation").OAS_OPERATION_ID))
                .leftJoin(OAS_TAG.as("oas_tag")).on(OAS_RESOURCE_TAG.as("oas_resource_tag").OAS_TAG_ID.eq(OAS_TAG.as("oas_tag").OAS_TAG_ID))
                .rightJoin(TOP_LEVEL_ASBIEP).on(OAS_MESSAGE_BODY.TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID))
                .leftJoin(ASBIEP).on(TOP_LEVEL_ASBIEP.ASBIEP_ID.eq(ASBIEP.ASBIEP_ID))
                .leftJoin(ASCCP_MANIFEST).on(ASBIEP.BASED_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                .leftJoin(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                .join(APP_USER.as("owner")).on(TOP_LEVEL_ASBIEP.OWNER_USER_ID.eq(APP_USER.as("owner").APP_USER_ID))
                .join(APP_USER.as("creator")).on(OAS_MESSAGE_BODY.CREATED_BY.eq(APP_USER.as("creator").APP_USER_ID))
                .join(APP_USER.as("updater")).on(OAS_MESSAGE_BODY.LAST_UPDATED_BY.eq(APP_USER.as("updater").APP_USER_ID));
    }

    private RecordMapper<Record, BieForOasDoc> mapper() {
        return record -> {
            BieForOasDoc bieForOasDoc = new BieForOasDoc();
            bieForOasDoc.setTopLevelAsbiepId(record.get(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID).toBigInteger());
            bieForOasDoc.setState(BieState.valueOf(record.get(TOP_LEVEL_ASBIEP.STATE)));
            bieForOasDoc.setVersion(record.get(TOP_LEVEL_ASBIEP.VERSION));
            bieForOasDoc.setDen(record.get(ASCCP_MANIFEST.DEN));
            bieForOasDoc.setRemark(record.get(ASBIEP.REMARK));
            bieForOasDoc.setGuid(record.get(ASCCP.GUID));
            bieForOasDoc.setMessageBody(record.get(field("oas_doc_message_body_type", String.class)));
            ULong oasDocId = record.get(OAS_DOC.as("oas_doc").OAS_DOC_ID.as("oas_doc_id"));
            if (oasDocId != null) {
                bieForOasDoc.setOasDocId(oasDocId.toBigInteger());
            }
            bieForOasDoc.setVerb(record.get(OAS_OPERATION.as("oas_operation").VERB.as("verb")));
            Byte arrayIndicator = record.get(OAS_REQUEST.MAKE_ARRAY_INDICATOR.as("array_indicator"));
            if (arrayIndicator != null) {
                bieForOasDoc.setArrayIndicator(arrayIndicator == (byte) 1);
            }
            Byte suppressRootIndicator = record.get(OAS_REQUEST.SUPPRESS_ROOT_INDICATOR.as("suppress_root_indicator"));
            if (suppressRootIndicator != null) {
                bieForOasDoc.setSuppressRootIndicator(suppressRootIndicator == (byte) 1);
            }
            bieForOasDoc.setResourceName(record.get(OAS_RESOURCE.as("oas_resource").PATH.as("resource_name")));
            bieForOasDoc.setOperationId(record.get(OAS_OPERATION.as("oas_operation").OPERATION_ID.as("operation_id")));
            bieForOasDoc.setTagName(record.get(OAS_TAG.as("oas_tag").NAME.as("tag_name")));
            ULong oasResourceId = record.get(OAS_RESOURCE.as("oas_resource").OAS_RESOURCE_ID.as("oas_resource_id"));
            if (oasResourceId != null) {
                bieForOasDoc.setOasResourceId(oasResourceId.toBigInteger());
            }
            ULong oasOperationId = record.get(OAS_OPERATION.as("oas_operation").OAS_OPERATION_ID.as("oas_operation_id"));
            if (oasOperationId != null) {
                bieForOasDoc.setOasOperationId(oasOperationId.toBigInteger());
            }
            bieForOasDoc.setReleaseId(record.get(TOP_LEVEL_ASBIEP.RELEASE_ID).toBigInteger());
            bieForOasDoc.setOwner(record.get(APP_USER.as("owner").LOGIN_ID.as("owner")).toString());
            bieForOasDoc.setCreatedBy(new ScoreUser(
                    record.get(APP_USER.as("creator").APP_USER_ID.as("creator_user_id")).toBigInteger(),
                    record.get(APP_USER.as("creator").LOGIN_ID.as("creator_login_id")),
                    (byte) 1 == record.get(APP_USER.as("creator").IS_DEVELOPER.as("creator_is_developer")) ? DEVELOPER : END_USER
            ));
            bieForOasDoc.setLastUpdatedBy(new ScoreUser(
                    record.get(APP_USER.as("updater").APP_USER_ID.as("updater_user_id")).toBigInteger(),
                    record.get(APP_USER.as("updater").LOGIN_ID.as("updater_login_id")),
                    (byte) 1 == record.get(APP_USER.as("updater").IS_DEVELOPER.as("updater_is_developer")) ? DEVELOPER : END_USER
            ));
            bieForOasDoc.setCreationTimestamp(
                    Date.from(record.get(OAS_MESSAGE_BODY.CREATION_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
            bieForOasDoc.setLastUpdateTimestamp(
                    Date.from(record.get(OAS_MESSAGE_BODY.LAST_UPDATE_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
            return bieForOasDoc;
        };
    }

    private Collection<Condition> getConditions(GetBieForOasDocRequest request) {
        List<Condition> conditions = new ArrayList();
        BigInteger oasDocId = request.getOasDocId();
        if (oasDocId != null) {
            conditions.add(OAS_DOC.as("oas_doc").OAS_DOC_ID.eq(ULong.valueOf(oasDocId)));
        }
        BigInteger topLevelAsbiepId = request.getTopLevelAsbiepId();
        if (topLevelAsbiepId != null) {
            conditions.add(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId)));
        }
        return conditions;
    }

    private List<SortField<?>> getSortField(GetBieForOasDocRequest request) {
        List<SortField<?>> sortFields = new ArrayList<>();

        if (!StringUtils.hasLength(request.getSortActive())) {
            return null;
        }
        String direction = request.getSortDirection().toString().toLowerCase();
        switch (trim(request.getSortActive()).toLowerCase()) {
            case "den":
                if ("asc".equals(direction)) {
                    sortFields.add(ASCCP_MANIFEST.DEN.asc());
                } else if ("desc".equals(direction)) {
                    sortFields.add(ASCCP_MANIFEST.DEN.desc());
                }
                break;
            case "verb":
                if ("asc".equals(direction)) {
                    sortFields.add(OAS_OPERATION.as("oas_operation").VERB.as("verb").asc());
                } else if ("desc".equals(direction)) {
                    sortFields.add(OAS_OPERATION.as("oas_operation").VERB.as("verb").desc());
                }
                break;
            case "lastupdatetimestamp":
                if ("asc".equals(direction)) {
                    sortFields.add(OAS_MESSAGE_BODY.LAST_UPDATE_TIMESTAMP.asc());
                } else if ("desc".equals(direction)) {
                    sortFields.add(OAS_MESSAGE_BODY.LAST_UPDATE_TIMESTAMP.desc());
                }
                break;
            case "operationid":
                if ("asc".equals(direction)) {
                    sortFields.add(OAS_OPERATION.as("oas_operation").OPERATION_ID.as("operation_id").asc());
                } else if ("desc".equals(direction)) {
                    sortFields.add(OAS_OPERATION.as("oas_operation").OPERATION_ID.as("operation_id").desc());
                }
                break;
            case "resourcename":
                if ("asc".equals(direction)) {
                    sortFields.add(OAS_RESOURCE.as("oas_resource").PATH.as("resource_name").asc());
                } else if ("desc".equals(direction)) {
                    sortFields.add(OAS_RESOURCE.as("oas_resource").PATH.as("resource_name").desc());
                }
                break;
            case "tagname":
                if ("asc".equals(direction)) {
                    sortFields.add(OAS_TAG.NAME.as("tag_name").asc());
                } else if ("desc".equals(direction)) {
                    sortFields.add(OAS_TAG.NAME.as("tag_name").desc());
                }
                break;
            default:
                sortFields.add(OAS_TAG.NAME.as("tag_name").asc());
                sortFields.add(OAS_OPERATION.as("oas_operation").OPERATION_ID.as("operation_id").asc());
        }

        return sortFields;
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public GetBieForOasDocResponse getBieForOasDoc(GetBieForOasDocRequest request) throws ScoreDataAccessException {
        SelectOrderByStep orderByStep = selectForRequest()
                .where(getConditions(request))
                .unionAll(selectForResponse()
                        .where(getConditions(request)));

        List<SortField<?>> sortFields = getSortField(request);
        int length = dslContext().fetchCount(orderByStep);
        SelectFinalStep finalStep;
        if (sortFields == null || sortFields.isEmpty()) {
            if (request.isPagination()) {
                finalStep = orderByStep.limit(request.getPageOffset(), request.getPageSize());
            } else {
                finalStep = orderByStep;
            }
        } else {
            if (request.isPagination()) {
                finalStep = orderByStep.orderBy(sortFields)
                        .limit(request.getPageOffset(), request.getPageSize());
            } else {
                finalStep = orderByStep.orderBy(sortFields);
            }
        }
        return new GetBieForOasDocResponse(
                finalStep.fetch(mapper()),
                request.getPageIndex(),
                request.getPageSize(),
                length
        );
    }

    @Override
    public GetAssignedOasTagResponse getAssignedOasTag(GetAssignedOasTagRequest request) throws ScoreDataAccessException {
        OasTagRecord oasTagRecord = null;
        if (request.getMessageBodyType().equals("Request")) {
            //Get oasTag
            OasResourceTagRecord req_oasResourceTagRecord = dslContext().selectFrom(OAS_RESOURCE_TAG.as("req_oas_resource_tag"))
                    .where(OAS_RESOURCE_TAG.as("req_oas_resource_tag").OAS_OPERATION_ID.eq(ULong.valueOf(request.getOasOperationId())))
                    .fetchOptional().orElse(null);
            if (req_oasResourceTagRecord == null) {
                return null;
            } else {
                ULong oasTagId = req_oasResourceTagRecord.getOasTagId();
                oasTagRecord = dslContext().selectFrom(OAS_TAG.as("req_oas_tag"))
                        .where(OAS_TAG.as("req_oas_tag").OAS_TAG_ID.eq(oasTagId)).fetchOptional().orElse(null);
            }
        } else if (request.getMessageBodyType().equals("Response")) {
            //Get oasTag
            OasResourceTagRecord res_oasResourceTagRecord = dslContext().selectFrom(OAS_RESOURCE_TAG.as("res_oas_resource_tag"))
                    .where(OAS_RESOURCE_TAG.as("res_oas_resource_tag").OAS_OPERATION_ID.eq(ULong.valueOf(request.getOasOperationId())))
                    .fetchOptional().orElse(null);
            if (res_oasResourceTagRecord == null) {
                return null;
            } else {
                ULong oasTagId = res_oasResourceTagRecord.getOasTagId();
                oasTagRecord = dslContext().selectFrom(OAS_TAG.as("res_oas_tag"))
                        .where(OAS_TAG.as("res_oas_tag").OAS_TAG_ID.eq(oasTagId)).fetchOptional().orElse(null);
            }

        } else throw new ScoreDataAccessException("Wrong MessageBody Type: " + request.getMessageBodyType());

        GetAssignedOasTagResponse response;
        if (oasTagRecord != null) {
            OasTag oasTag = new OasTag(
                    oasTagRecord.getOasTagId().toBigInteger(),
                    oasTagRecord.getGuid(),
                    oasTagRecord.getName(),
                    oasTagRecord.getDescription(),
                    null,
                    null);
            response = new GetAssignedOasTagResponse(oasTag);
        } else {
            return null;
        }
        return response;
    }
}
