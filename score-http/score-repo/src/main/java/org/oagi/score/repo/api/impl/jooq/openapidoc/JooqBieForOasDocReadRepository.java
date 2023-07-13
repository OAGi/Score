package org.oagi.score.repo.api.impl.jooq.openapidoc;

import org.jooq.Record;
import org.jooq.*;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.bie.model.BieState;
import org.oagi.score.repo.api.impl.jooq.JooqScoreRepository;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.oagi.score.repo.api.openapidoc.BieForOasDocReadRepository;
import org.oagi.score.repo.api.openapidoc.model.*;
import org.oagi.score.repo.api.security.AccessControl;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.time.ZoneId;
import java.util.*;

import static org.jooq.impl.DSL.or;
import static org.oagi.score.repo.api.base.SortDirection.ASC;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.repo.api.impl.utils.StringUtils.trim;
import static org.oagi.score.repo.api.user.model.ScoreRole.DEVELOPER;
import static org.oagi.score.repo.api.user.model.ScoreRole.END_USER;

public class JooqBieForOasDocReadRepository extends JooqScoreRepository
        implements BieForOasDocReadRepository {
    public JooqBieForOasDocReadRepository(DSLContext dslContext) {
        super(dslContext);
    }

    private SelectOnConditionStep select() {
        return dslContext().select(
                        TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID,
                        TOP_LEVEL_ASBIEP.STATE,
                        TOP_LEVEL_ASBIEP.VERSION,
                        TOP_LEVEL_ASBIEP.RELEASE_ID,
                        ASBIEP.ASBIEP_ID,
                        ASCCP_MANIFEST.ASCCP_MANIFEST_ID,
                        ASCCP.ASCCP_ID,
                        ASCCP.GUID,
                        ASCCP.DEN,
                        APP_USER.as("owner").LOGIN_ID.as("owner"),
                        APP_USER.as("owner").APP_USER_ID.as("owner_user_id"),
                        APP_USER.as("owner").LOGIN_ID.as("owner_login_id"),
                        APP_USER.as("owner").IS_DEVELOPER.as("owner_is_developer"),
                        APP_USER.as("owner").IS_ADMIN.as("owner_is_admin"),
                        OAS_DOC.as("req_oas_doc").OAS_DOC_ID.as("req_oas_doc_id"),
                        OAS_DOC.as("res_oas_doc").OAS_DOC_ID.as("res_oas_doc_id"),
                        OAS_OPERATION.as("req_oas_operation").VERB.as("req_verb"),
                        OAS_OPERATION.as("res_oas_operation").VERB.as("res_verb"),
                        OAS_TAG.as("req_oas_tag").NAME.as("req_tag_name"),
                        OAS_TAG.as("res_oas_tag").NAME.as("res_tag_name"),
                        OAS_REQUEST.MAKE_ARRAY_INDICATOR.as("req_array_indicator"),
                        OAS_RESPONSE.MAKE_ARRAY_INDICATOR.as("res_array_indicator"),
                        OAS_REQUEST.SUPPRESS_ROOT_INDICATOR.as("req_suppress_root_indicator"),
                        OAS_RESPONSE.SUPPRESS_ROOT_INDICATOR.as("res_suppress_root_indicator"),
                        OAS_RESOURCE.as("req_oas_resource").PATH.as("req_resource_name"),
                        OAS_RESOURCE.as("res_oas_resource").PATH.as("res_resource_name"),
                        OAS_RESOURCE.as("req_oas_resource").OAS_RESOURCE_ID.as("req_oas_resource_id"),
                        OAS_RESOURCE.as("res_oas_resource").OAS_RESOURCE_ID.as("res_oas_resource_id"),
                        OAS_OPERATION.as("req_oas_operation").OPERATION_ID.as("req_operation_id"),
                        OAS_OPERATION.as("res_oas_operation").OPERATION_ID.as("res_operation_id"),
                        OAS_OPERATION.as("req_oas_operation").OAS_OPERATION_ID.as("req_oas_operation_id"),
                        OAS_OPERATION.as("res_oas_operation").OAS_OPERATION_ID.as("res_oas_operation_id"),
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
                .leftJoin(OAS_OPERATION.as("req_oas_operation")).on(OAS_REQUEST.OAS_OPERATION_ID.eq(OAS_OPERATION.as("req_oas_operation").OAS_OPERATION_ID))
                .leftJoin(OAS_RESOURCE.as("req_oas_resource")).on(OAS_OPERATION.as("req_oas_operation").OAS_OPERATION_ID.eq(OAS_RESOURCE.as("req_oas_resource").OAS_RESOURCE_ID))
                .leftJoin(OAS_DOC.as("req_oas_doc")).on(OAS_RESOURCE.as("req_oas_resource").OAS_DOC_ID.eq(OAS_DOC.as("req_oas_doc").OAS_DOC_ID))
                .leftJoin(OAS_DOC_TAG.as("req_oas_doc_tag")).on(OAS_DOC.as("req_oas_doc").OAS_DOC_ID.eq(OAS_DOC_TAG.as("req_oas_doc_tag").OAS_DOC_ID))
                .leftJoin(OAS_TAG.as("req_oas_tag")).on(OAS_DOC_TAG.as("req_oas_doc_tag").OAS_TAG_ID.eq(OAS_TAG.as("req_oas_tag").OAS_TAG_ID))
                .leftJoin(OAS_RESPONSE).on(OAS_RESPONSE.OAS_MESSAGE_BODY_ID.eq(OAS_MESSAGE_BODY.OAS_MESSAGE_BODY_ID))
                .leftJoin(OAS_OPERATION.as("res_oas_operation")).on(OAS_RESPONSE.OAS_OPERATION_ID.eq(OAS_OPERATION.as("res_oas_operation").OAS_OPERATION_ID))
                .leftJoin(OAS_RESOURCE.as("res_oas_resource")).on(OAS_OPERATION.as("res_oas_operation").OAS_RESOURCE_ID.eq(OAS_RESOURCE.as("res_oas_resource").OAS_RESOURCE_ID))
                .leftJoin(OAS_DOC.as("res_oas_doc")).on(OAS_RESOURCE.as("res_oas_resource").OAS_DOC_ID.eq(OAS_DOC.as("res_oas_doc").OAS_DOC_ID))
                .leftJoin(OAS_DOC_TAG.as("res_oas_doc_tag")).on(OAS_DOC.as("res_oas_doc").OAS_DOC_ID.eq(OAS_DOC_TAG.as("res_oas_doc_tag").OAS_DOC_ID))
                .leftJoin(OAS_TAG.as("res_oas_tag")).on(OAS_DOC_TAG.as("res_oas_doc_tag").OAS_TAG_ID.eq(OAS_TAG.as("res_oas_tag").OAS_TAG_ID))
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
            bieForOasDoc.setDen(record.get(ASCCP.DEN));
            bieForOasDoc.setGuid(record.get(ASCCP.GUID));
            if (record.get(OAS_DOC.as("req_oas_doc").OAS_DOC_ID.as("req_oas_doc_id")) != null) {
                bieForOasDoc.setOasDocId(record.get(OAS_DOC.as("req_oas_doc").OAS_DOC_ID.as("req_oas_doc_id")).toBigInteger());
                bieForOasDoc.setVerbs(Arrays.asList(record.get(OAS_OPERATION.as("req_oas_operation").VERB.as("req_verb"))));
                bieForOasDoc.setArrayIndicator(record.get(OAS_REQUEST.MAKE_ARRAY_INDICATOR.as("req_array_indicator")) == (byte) 1);
                bieForOasDoc.setSuppressRootIndicator(record.get(OAS_REQUEST.SUPPRESS_ROOT_INDICATOR.as("req_suppress_root_indicator")) == (byte) 1);
                bieForOasDoc.setResourceName(record.get(OAS_RESOURCE.as("req_oas_resource").PATH.as("req_resource_name")));
                bieForOasDoc.setOperationId(record.get(OAS_OPERATION.as("req_oas_operation").OPERATION_ID.as("req_operation_id")));
                bieForOasDoc.setTagName(record.get(OAS_TAG.as("req_oas_tag").NAME.as("req_tag_name")));
                bieForOasDoc.setMessageBody(Arrays.asList("requestBody"));
            } else if (record.get(OAS_DOC.as("res_oas_doc").OAS_DOC_ID.as("res_oas_doc_id")) != null) {
                bieForOasDoc.setOasDocId(record.get(OAS_DOC.as("res_oas_doc").OAS_DOC_ID.as("res_oas_doc_id")).toBigInteger());
                bieForOasDoc.setVerbs(Arrays.asList(record.get(OAS_OPERATION.as("res_oas_operation").VERB.as("res_verb"))));
                bieForOasDoc.setArrayIndicator(record.get(OAS_RESPONSE.MAKE_ARRAY_INDICATOR.as("res_array_indicator")) == (byte) 1);
                bieForOasDoc.setSuppressRootIndicator(record.get(OAS_RESPONSE.SUPPRESS_ROOT_INDICATOR.as("res_suppress_root_indicator")) == (byte) 1);
                bieForOasDoc.setResourceName(record.get(OAS_RESOURCE.as("res_oas_resource").PATH.as("res_resource_name")));
                bieForOasDoc.setOperationId(record.get(OAS_OPERATION.as("res_oas_operation").OPERATION_ID.as("res_operation_id")));
                bieForOasDoc.setTagName(record.get(OAS_TAG.as("res_oas_tag").NAME.as("res_tag_name")));
                bieForOasDoc.setMessageBody(Arrays.asList("responseBody"));
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

    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public GetBieForOasDocResponse getBieForOasDoc(GetBieForOasDocRequest request) throws ScoreDataAccessException {
        List<BieForOasDoc> bieForOasDoc = null;

        BigInteger oasDocId = request.getOasDocId();
        if (oasDocId != null) {
            bieForOasDoc = select()
                    .where(or(OAS_DOC.as("res_oas_doc").OAS_DOC_ID.eq(ULong.valueOf(oasDocId)), OAS_DOC.as("req_oas_doc").OAS_DOC_ID.eq(ULong.valueOf(oasDocId))))
                    .fetch(mapper());
        }
        return new GetBieForOasDocResponse(bieForOasDoc, 1, 1, 1);
    }

    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public AddBieForOasDocResponse addBieForOasDoc(AddBieForOasDocRequest request) throws ScoreDataAccessException {
        List<BieForOasDoc> bieForOasDoc = null;

        BigInteger oasDocId = request.getOasDocId();
        if (oasDocId != null && request.isOasRequest()) {
            bieForOasDoc = select()
                    .where(OAS_DOC.as("res_oas_doc").OAS_DOC_ID.eq(ULong.valueOf(oasDocId)))
                    .fetch(mapper());
        }

        if (oasDocId != null && !request.isOasRequest()) {
            bieForOasDoc = select()
                    .where(OAS_DOC.as("req_oas_doc").OAS_DOC_ID.eq(ULong.valueOf(oasDocId)))
                    .fetch(mapper());
        }
        return new AddBieForOasDocResponse(bieForOasDoc, 1, 1, 1);
    }

    private Collection<Condition> getConditions(GetBieForOasDocListRequest request) {
        List<Condition> conditions = new ArrayList();
        if (request.getOasDocId() != null) {
            conditions.add(OAS_DOC.as("req_oas_doc").OAS_DOC_ID.as("req_oas_doc_id").eq(
                    ULong.valueOf(request.getOasDocId())
            ));
        }
        return conditions;
    }

    private SortField getSortField(GetBieForOasDocListRequest request) {
        if (!StringUtils.hasLength(request.getSortActive())) {
            return null;
        }

        Field field;
        switch (trim(request.getSortActive()).toLowerCase()) {
            case "den":
                field = ASCCP.PROPERTY_TERM;
                break;
            case "lastupdatetimestamp":
                field = OAS_DOC.LAST_UPDATE_TIMESTAMP;
                break;

            default:
                return null;
        }

        return (request.getSortDirection() == ASC) ? field.asc() : field.desc();
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public GetBieForOasDocListResponse getBieForOasDocList(GetBieForOasDocListRequest request) throws ScoreDataAccessException {
        Collection<Condition> conditions = getConditions(request);
        SelectConditionStep conditionStep = select().where(conditions);

        SortField sortField = getSortField(request);
        int length = dslContext().fetchCount(conditionStep);
        SelectFinalStep finalStep;
        if (sortField == null) {
            if (request.isPagination()) {
                finalStep = conditionStep.limit(request.getPageOffset(), request.getPageSize());
            } else {
                finalStep = conditionStep;
            }
        } else {
            if (request.isPagination()) {
                finalStep = conditionStep.orderBy(sortField)
                        .limit(request.getPageOffset(), request.getPageSize());
            } else {
                finalStep = conditionStep.orderBy(sortField);
            }
        }

        return new GetBieForOasDocListResponse(
                finalStep.fetch(mapper()),
                request.getPageIndex(),
                request.getPageSize(),
                length
        );
    }

}
