package org.oagi.score.repo.api.impl.jooq.openapidoc;

import org.jooq.*;
import org.jooq.Record;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.oas_management.data.BieForOasDocUpdateRequest;
import org.oagi.score.gateway.http.api.oas_management.data.BieForOasDocUpdateResponse;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.bie.model.BieState;
import org.oagi.score.repo.api.businessterm.model.UpdateBusinessTermResponse;
import org.oagi.score.repo.api.impl.jooq.JooqScoreRepository;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.BusinessTermRecord;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.oagi.score.repo.api.openapidoc.BieForOasDocWriteRepository;
import org.oagi.score.repo.api.openapidoc.model.*;
import org.oagi.score.repo.api.user.model.ScoreUser;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.jooq.impl.DSL.or;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.APP_USER;
import static org.oagi.score.repo.api.user.model.ScoreRole.DEVELOPER;
import static org.oagi.score.repo.api.user.model.ScoreRole.END_USER;

public class JooqBieForOasDocWriteRepository extends JooqScoreRepository
        implements BieForOasDocWriteRepository {
    public JooqBieForOasDocWriteRepository(DSLContext dslContext) {
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
                        OAS_OPERATION.as("req_oas_operation").OPERATION_ID.as("req_operation_id"),
                        OAS_OPERATION.as("res_oas_operation").OPERATION_ID.as("res_operation_id"),
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


    @Override
    public AddBieForOasDocResponse assignBieForOasDoc(AuthenticatedPrincipal user, AddBieForOasDocRequest request) throws ScoreDataAccessException {
        return null;
    }

    @Override
    public UpdateBieForOasDocResponse updateBieForOasDoc(AuthenticatedPrincipal user, UpdateBieForOasDocRequest request) throws ScoreDataAccessException {
        ScoreUser requester = request.getRequester();
        BigInteger requesterUserId = requester.getUserId();
        LocalDateTime timestamp = LocalDateTime.now();

        List<BieForOasDoc> bieListRecord = new ArrayList<>();
        BigInteger oasDocId = request.getOasDocId();
        if (oasDocId == null) {
            throw new IllegalArgumentException("`oasDocId` parameter must not be null.");
        }
        if (oasDocId != null) {
            bieListRecord = select()
                    .where(or(OAS_DOC.as("res_oas_doc").OAS_DOC_ID.eq(ULong.valueOf(oasDocId)), OAS_DOC.as("req_oas_doc").OAS_DOC_ID.eq(ULong.valueOf(oasDocId))))
                    .fetch(mapper());
        }

        if (bieListRecord.isEmpty()) {
            throw new ScoreDataAccessException(new IllegalArgumentException());
        }
        List<Field<?>> changedField = new ArrayList();

        for(BieForOasDoc bieForOasDoc : request.getBieForOasDocList()){
            BieForOasDoc bieForOasDocRecord = bieListRecord.stream().filter(p -> p.getTopLevelAsbiepId().equals(bieForOasDoc.getTopLevelAsbiepId())).findFirst().orElse(null);
            if (!StringUtils.equals(bieForOasDoc.getResourceName(), bieForOasDocRecord.getResourceName())){
                changedField.add(OAS_RESOURCE.PATH);
            }
            if (!StringUtils.equals(bieForOasDoc.getOperationId(), bieForOasDocRecord.getOperationId())){
                changedField.add(OAS_OPERATION.OPERATION_ID);
            }

            if (!changedField.isEmpty()) {
                bieForOasDoc.setLastUpdatedBy(ULong.valueOf(requesterUserId));
                changedField.add(BUSINESS_TERM.LAST_UPDATED_BY);

                bieForOasDoc.setLastUpdateTimestamp(timestamp);
                changedField.add(BUSINESS_TERM.LAST_UPDATE_TIMESTAMP);

                int affectedRows = bieForOasDocRecord.update(changedField);
                if (affectedRows != 1) {
                    throw new ScoreDataAccessException(new IllegalStateException());
                }
            }

        }



        return new UpdateBieForOasDocResponse(
                record.getBusinessTermId().toBigInteger(),
                !changedField.isEmpty());
    }

    @Override
    public DeleteBieForOasDocResponse deleteBieForOasDoc(DeleteBieForOasDocRequest request) throws ScoreDataAccessException {
        return null;
    }
}
