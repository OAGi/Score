package org.oagi.score.repo.api.impl.jooq.openapidoc;

import org.jooq.Record;
import org.jooq.*;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.impl.jooq.JooqScoreRepository;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.oagi.score.repo.api.openapidoc.BieForOasDocReadRepository;
import org.oagi.score.repo.api.openapidoc.model.*;
import org.oagi.score.repo.api.security.AccessControl;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

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
                        TOP_LEVEL_ASBIEP.RELEASE_ID,
                        ASBIEP.ASBIEP_ID,
                        ASCCP_MANIFEST.ASCCP_MANIFEST_ID,
                        ASCCP.ASCCP_ID,
                        ASCCP.DEN,
                        OAS_DOC.as("req_oas_doc").OAS_DOC_ID.as("req_oas_doc_id"),
                        OAS_DOC.as("res_oas_doc").OAS_DOC_ID.as("res_oas_doc_id"),
                        OAS_OPERATION.as("req_oas_operation").VERB.as("req_verb"),
                        OAS_OPERATION.as("res_oas_operation").VERB.as("res_verb"),
                        OAS_REQUEST.MAKE_ARRAY_INDICATOR.as("req_array_indicator"),
                        OAS_RESPONSE.MAKE_ARRAY_INDICATOR.as("res_array_indicator"),
                        OAS_REQUEST.SUPPRESS_ROOT_INDICATOR.as("req_suppress_root_indicator"),
                        OAS_RESPONSE.SUPPRESS_ROOT_INDICATOR.as("res_suppress_root_indicator"),
                        OAS_RESOURCE.as("req_oas_resource").PATH.as("req_resource_name"),
                        OAS_RESOURCE.as("res_oas_resource").PATH.as("res_resource_name"),
                        OAS_OPERATION.as("req_oas_operation").OPERATION_ID.as("req_operation_id"),
                        OAS_OPERATION.as("res_oas_operation").OPERATION_ID.as("res_operation_id"),
                        APP_USER.as("creator").APP_USER_ID.as("creator_user_id"),
                        APP_USER.as("creator").LOGIN_ID.as("creator_login_id"),
                        APP_USER.as("creator").IS_DEVELOPER.as("creator_is_developer"),
                        APP_USER.as("updater").APP_USER_ID.as("updater_user_id"),
                        APP_USER.as("updater").LOGIN_ID.as("updater_login_id"),
                        APP_USER.as("updater").IS_DEVELOPER.as("updater_is_developer"),
                        OAS_DOC.CREATION_TIMESTAMP,
                        OAS_DOC.LAST_UPDATE_TIMESTAMP)
                .from(OAS_MESSAGE_BODY)
                .leftJoin(OAS_REQUEST).on(OAS_REQUEST.OAS_MESSAGE_BODY_ID.eq(OAS_MESSAGE_BODY.OAS_MESSAGE_BODY_ID))
                .leftJoin(OAS_OPERATION.as("req_oas_operation")).on(OAS_REQUEST.OAS_OPERATION_ID.eq(OAS_OPERATION.as("req_oas_operation").OAS_OPERATION_ID))
                .leftJoin(OAS_RESOURCE.as("req_oas_resource")).on(OAS_OPERATION.as("req_oas_operation").OAS_OPERATION_ID.eq(OAS_RESOURCE.as("req_oas_resource").OAS_RESOURCE_ID))
                .leftJoin(OAS_DOC.as("req_oas_doc")).on(OAS_RESOURCE.as("req_oas_resource").OAS_DOC_ID.eq(OAS_DOC.as("req_oas_doc").OAS_DOC_ID))
                .leftJoin(OAS_RESPONSE).on(OAS_RESPONSE.OAS_MESSAGE_BODY_ID.eq(OAS_MESSAGE_BODY.OAS_MESSAGE_BODY_ID))
                .leftJoin(OAS_OPERATION.as("res_oas_operation")).on(OAS_RESPONSE.OAS_OPERATION_ID.eq(OAS_OPERATION.as("res_oas_operation").OAS_OPERATION_ID))
                .leftJoin(OAS_RESOURCE.as("res_oas_resource")).on(OAS_OPERATION.as("res_oas_operation").OAS_RESOURCE_ID.eq(OAS_RESOURCE.as("res_oas_resource").OAS_RESOURCE_ID))
                .leftJoin(OAS_DOC.as("res_oas_doc")).on(OAS_RESOURCE.as("res_oas_resource").OAS_DOC_ID.eq(OAS_DOC.as("res_oas_doc").OAS_DOC_ID))
                .rightJoin(TOP_LEVEL_ASBIEP).on(OAS_MESSAGE_BODY.TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID))
                .leftJoin(ASBIEP).on(TOP_LEVEL_ASBIEP.ASBIEP_ID.eq(ASBIEP.ASBIEP_ID))
                .leftJoin(ASCCP_MANIFEST).on(ASBIEP.BASED_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                .leftJoin(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                .join(APP_USER.as("creator")).on(OAS_DOC.CREATED_BY.eq(APP_USER.as("creator").APP_USER_ID))
                .join(APP_USER.as("updater")).on(OAS_DOC.LAST_UPDATED_BY.eq(APP_USER.as("updater").APP_USER_ID));
    }

    private RecordMapper<Record, BieForOasDoc> mapper() {
        return record -> {
            BieForOasDoc bieForOasDoc = new BieForOasDoc();
            bieForOasDoc.setTopLevelAsbiepId(record.get(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID).toBigInteger());
            bieForOasDoc.setOasDocId(record.get(OAS_DOC.OAS_DOC_ID).toBigInteger());
            bieForOasDoc.setGuid(record.get(ASCCP.GUID));
            bieForOasDoc.setState(record.get(TOP_LEVEL_ASBIEP.STATE));
            bieForOasDoc.setStatus(record.get(TOP_LEVEL_ASBIEP.STATUS));
            bieForOasDoc.setPropertyTerm(record.get(ASCCP.PROPERTY_TERM));
            bieForOasDoc.setVersion(record.get(TOP_LEVEL_ASBIEP.VERSION));
            if (OAS_REQUEST != null) {
                bieForOasDoc.setArrayIndicator(record.get(OAS_REQUEST.MAKE_ARRAY_INDICATOR) == (byte) 1);
                bieForOasDoc.setSuppressRoot(record.get(OAS_REQUEST.SUPPRESS_ROOT_INDICATOR) == (byte) 1);
                bieForOasDoc.setMessageBody("request Body");
            } else if (OAS_RESPONSE != null) {
                bieForOasDoc.setArrayIndicator(record.get(OAS_RESPONSE.MAKE_ARRAY_INDICATOR) == (byte) 1);
                bieForOasDoc.setSuppressRoot(record.get(OAS_RESPONSE.SUPPRESS_ROOT_INDICATOR) == (byte) 1);
                bieForOasDoc.setMessageBody("response Body");
            }
            bieForOasDoc.setResourceName(record.get(OAS_RESOURCE.PATH));
            bieForOasDoc.setVerb(record.get(OAS_OPERATION.VERB));
            bieForOasDoc.setReleaseId(record.get(TOP_LEVEL_ASBIEP.RELEASE_ID).toBigInteger());
            bieForOasDoc.setOperationId(record.get(OAS_OPERATION.OPERATION_ID));
            bieForOasDoc.setTagName(record.get(OAS_TAG.NAME));
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
        BieForOasDoc bieForOasDoc = null;

        BigInteger topLevelAsbiepId = request.getTopLevelAsbiepId();
        if (topLevelAsbiepId != null) {
            bieForOasDoc = (BieForOasDoc) select()
                    .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId)))
                    .fetchOne(mapper());
        }

        return new GetBieForOasDocResponse(bieForOasDoc);
    }

    private Collection<Condition> getConditions(GetBieForOasDocListRequest request) {
        List<Condition> conditions = new ArrayList();

        if (request.getTopLevelAsbiepIdList() != null && !request.getTopLevelAsbiepIdList().isEmpty()) {
            if (request.getTopLevelAsbiepIdList().size() == 1) {
                conditions.add(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(
                        ULong.valueOf(request.getTopLevelAsbiepIdList().iterator().next())
                ));
            } else {
                conditions.add(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.in(
                        request.getTopLevelAsbiepIdList().stream()
                                .map(e -> ULong.valueOf(e)).collect(Collectors.toList())
                ));
            }
        }

        if (request.getOasDocId() != null) {
            conditions.add(OAS_DOC.OAS_DOC_ID.eq(
                    ULong.valueOf(request.getOasDocId())
            ));
        }


        if (!request.getUpdaterUsernameList().isEmpty()) {
            conditions.add(APP_USER.as("updater").LOGIN_ID.in(
                    new HashSet<>(request.getUpdaterUsernameList()).stream()
                            .filter(e -> StringUtils.hasLength(e)).map(e -> trim(e)).collect(Collectors.toList())
            ));
        }
        if (request.getUpdateStartDate() != null) {
            conditions.add(OAS_DOC.LAST_UPDATE_TIMESTAMP.greaterOrEqual(request.getUpdateStartDate()));
        }
        if (request.getUpdateEndDate() != null) {
            conditions.add(OAS_DOC.LAST_UPDATE_TIMESTAMP.lessThan(request.getUpdateEndDate()));
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
