package org.oagi.score.repo.api.impl.jooq.openapidoc;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.impl.jooq.JooqScoreRepository;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.OasOperationRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.OasRequestRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.OasResourceRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.OasResponseRecord;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.oagi.score.repo.api.openapidoc.BieForOasDocWriteRepository;
import org.oagi.score.repo.api.openapidoc.model.*;
import org.oagi.score.repo.api.user.model.ScoreUser;


import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.repo.api.impl.utils.BooleanUtils.BooleanToByte;

public class JooqBieForOasDocWriteRepository extends JooqScoreRepository implements BieForOasDocWriteRepository {
    public JooqBieForOasDocWriteRepository(DSLContext dslContext) {
        super(dslContext);
    }

    @Override
    public AddBieForOasDocResponse assignBieForOasDoc(AddBieForOasDocRequest request) throws ScoreDataAccessException {
        return null;
    }

    @Override
    public UpdateBieForOasDocResponse updateBieForOasDoc(UpdateBieForOasDocRequest request) throws ScoreDataAccessException {
        ScoreUser requester = request.getRequester();
        BigInteger requesterUserId = requester.getUserId();
        LocalDateTime timestamp = LocalDateTime.now();

        BigInteger oasDocId = request.getOasDocId();
        if (oasDocId == null) {
            throw new IllegalArgumentException("`oasDocId` parameter must not be null.");
        }

        List<Field<?>> oasResourceChangedField = new ArrayList();
        List<Field<?>> oasOperationChangedField = new ArrayList();
        List<Field<?>> oasRequestChangedField = new ArrayList();
        List<Field<?>> oasResponseChangedField = new ArrayList();
        for (BieForOasDoc bieForOasDoc : request.getBieForOasDocList()) {
            if (bieForOasDoc.getMessageBody().equals("Request")){
                //update oas_resource
                OasResourceRecord oasResourceRecord = dslContext().selectFrom(OAS_RESOURCE.as("req_oas_resource")).where(and(OAS_RESOURCE.as("req_oas_resource").OAS_RESOURCE_ID.as("req_oas_resource_id").eq(ULong.valueOf(bieForOasDoc.getOasResourceId())),
                        OAS_RESOURCE.as("req_oas_resource").OAS_DOC_ID.as("req_oas_doc_id").eq(ULong.valueOf(bieForOasDoc.getOasDocId())))).fetchOptional().orElse(null);
                if (oasResourceRecord == null) {
                    throw new ScoreDataAccessException(new IllegalArgumentException());
                }
                if (oasResourceRecord != null && !StringUtils.equals(bieForOasDoc.getResourceName(), oasResourceRecord.getPath())) {
                    oasResourceChangedField.add(OAS_RESOURCE.as("req_oas_resource").PATH);
                    oasResourceRecord.setPath(bieForOasDoc.getResourceName());
                    oasResourceChangedField.add(OAS_RESOURCE.as("req_oas_resource").LAST_UPDATED_BY);
                    oasResourceRecord.setLastUpdatedBy(ULong.valueOf(requesterUserId));
                    oasResourceChangedField.add(OAS_RESOURCE.as("req_oas_resource").LAST_UPDATE_TIMESTAMP);
                    oasResourceRecord.setLastUpdateTimestamp(timestamp);
                    int affectedRows = oasResourceRecord.update(oasResourceChangedField);
                    if (affectedRows != 1) {
                        throw new ScoreDataAccessException(new IllegalStateException());
                    }
                }
                //update oas_operation
                OasOperationRecord oasOperationRecord = dslContext().selectFrom(OAS_OPERATION.as("req_oas_operation")).where(and(OAS_OPERATION.as("req_oas_operation").OAS_RESOURCE_ID.as("req_oas_resource_id").eq(ULong.valueOf(bieForOasDoc.getOasResourceId())),
                        OAS_OPERATION.as("req_oas_operation").OAS_OPERATION_ID.as("req_oas_operation_id").eq(ULong.valueOf(bieForOasDoc.getOasOperationId())))).fetchOptional().orElse(null);
                if (oasOperationRecord == null) {
                    throw new ScoreDataAccessException(new IllegalArgumentException());
                }
                if (oasOperationRecord != null && !StringUtils.equals(bieForOasDoc.getOperationId(), oasOperationRecord.getOperationId())) {
                    oasOperationChangedField.add(OAS_OPERATION.as("req_oas_operation").OPERATION_ID);
                    oasOperationRecord.setOperationId(bieForOasDoc.getOperationId());
                }
                if (oasOperationRecord != null && !StringUtils.equals(bieForOasDoc.getVerb(), oasOperationRecord.getVerb())) {
                    oasOperationChangedField.add(OAS_OPERATION.as("req_oas_operation").VERB);
                    oasOperationRecord.setVerb(bieForOasDoc.getVerb());
                }
                oasOperationChangedField.add(OAS_OPERATION.as("req_oas_operation").LAST_UPDATED_BY);
                oasOperationRecord.setLastUpdatedBy(ULong.valueOf(requesterUserId));
                oasOperationChangedField.add(OAS_OPERATION.as("req_oas_operation").LAST_UPDATE_TIMESTAMP);
                oasResourceRecord.setLastUpdateTimestamp(timestamp);
                int affectedRows = oasOperationRecord.update(oasOperationChangedField);
                if (affectedRows != 1) {
                    throw new ScoreDataAccessException(new IllegalStateException());
                }

                //update arrayIndicator and SuppressRootIndicator
                OasRequestRecord oasRequestRecord = dslContext().selectFrom(OAS_REQUEST).where(OAS_REQUEST.OAS_OPERATION_ID.eq(ULong.valueOf(bieForOasDoc.getOasOperationId()))).fetchOptional().orElse(null);
                if (oasRequestRecord == null) {
                    throw new ScoreDataAccessException(new IllegalArgumentException());
                }
                if (oasRequestRecord != null && BooleanToByte(bieForOasDoc.isArrayIndicator()) != oasRequestRecord.getMakeArrayIndicator()) {
                    oasRequestChangedField.add(OAS_REQUEST.MAKE_ARRAY_INDICATOR);
                    oasRequestRecord.setMakeArrayIndicator(BooleanToByte(bieForOasDoc.isArrayIndicator()));
                }

                if (oasRequestRecord != null && BooleanToByte(bieForOasDoc.isSuppressRootIndicator()) != oasRequestRecord.getSuppressRootIndicator()) {
                    oasRequestChangedField.add(OAS_REQUEST.SUPPRESS_ROOT_INDICATOR);
                    oasRequestRecord.setSuppressRootIndicator(BooleanToByte(bieForOasDoc.isSuppressRootIndicator()));
                }

                oasRequestChangedField.add(OAS_REQUEST.LAST_UPDATED_BY);
                oasRequestRecord.setLastUpdatedBy(ULong.valueOf(requesterUserId));
                oasRequestChangedField.add(OAS_REQUEST.LAST_UPDATE_TIMESTAMP);
                oasRequestRecord.setLastUpdateTimestamp(timestamp);
                affectedRows = oasRequestRecord.update(oasRequestChangedField);
                if (affectedRows != 1) {
                    throw new ScoreDataAccessException(new IllegalStateException());
                }
            }

            if (bieForOasDoc.getMessageBody().equals("Response")){
                //update oas_resource
                OasResourceRecord oasResourceRecord = dslContext().selectFrom(OAS_RESOURCE.as("res_oas_resource")).where(and(OAS_RESOURCE.as("res_oas_resource").OAS_RESOURCE_ID.as("res_oas_resource_id").eq(ULong.valueOf(bieForOasDoc.getOasResourceId())),
                        OAS_RESOURCE.as("res_oas_resource").OAS_DOC_ID.as("res_oas_doc_id").eq(ULong.valueOf(bieForOasDoc.getOasDocId())))).fetchOptional().orElse(null);
                if (oasResourceRecord == null) {
                    throw new ScoreDataAccessException(new IllegalArgumentException());
                }
                if (oasResourceRecord != null && !StringUtils.equals(bieForOasDoc.getResourceName(), oasResourceRecord.getPath())) {
                    oasResourceChangedField.add(OAS_RESOURCE.as("res_oas_resource").PATH);
                    oasResourceRecord.setPath(bieForOasDoc.getResourceName());
                    oasResourceChangedField.add(OAS_RESOURCE.as("res_oas_resource").LAST_UPDATED_BY);
                    oasResourceRecord.setLastUpdatedBy(ULong.valueOf(requesterUserId));
                    oasResourceChangedField.add(OAS_RESOURCE.as("res_oas_resource").LAST_UPDATE_TIMESTAMP);
                    oasResourceRecord.setLastUpdateTimestamp(timestamp);
                    int affectedRows = oasResourceRecord.update(oasResourceChangedField);
                    if (affectedRows != 1) {
                        throw new ScoreDataAccessException(new IllegalStateException());
                    }
                }
                //update oas_operation
                OasOperationRecord oasOperationRecord = dslContext().selectFrom(OAS_OPERATION.as("res_oas_operation")).where(and(OAS_OPERATION.as("res_oas_operation").OAS_RESOURCE_ID.as("res_oas_resource_id").eq(ULong.valueOf(bieForOasDoc.getOasResourceId())),
                        OAS_OPERATION.as("res_oas_operation").OAS_OPERATION_ID.as("res_oas_operation_id").eq(ULong.valueOf(bieForOasDoc.getOasOperationId())))).fetchOptional().orElse(null);
                if (oasOperationRecord == null) {
                    throw new ScoreDataAccessException(new IllegalArgumentException());
                }
                if (oasOperationRecord != null && !StringUtils.equals(bieForOasDoc.getOperationId(), oasOperationRecord.getOperationId())) {
                    oasOperationChangedField.add(OAS_OPERATION.as("res_oas_operation").OPERATION_ID);
                    oasOperationRecord.setOperationId(bieForOasDoc.getOperationId());
                }
                if (oasOperationRecord != null && !StringUtils.equals(bieForOasDoc.getVerb(), oasOperationRecord.getVerb())) {
                    oasOperationChangedField.add(OAS_OPERATION.as("res_oas_operation").VERB);
                    oasOperationRecord.setVerb(bieForOasDoc.getVerb());
                }
                oasOperationChangedField.add(OAS_OPERATION.as("res_oas_operation").LAST_UPDATED_BY);
                oasOperationRecord.setLastUpdatedBy(ULong.valueOf(requesterUserId));
                oasOperationChangedField.add(OAS_OPERATION.as("res_oas_operation").LAST_UPDATE_TIMESTAMP);
                oasResourceRecord.setLastUpdateTimestamp(timestamp);
                int affectedRows = oasOperationRecord.update(oasOperationChangedField);
                if (affectedRows != 1) {
                    throw new ScoreDataAccessException(new IllegalStateException());
                }

                //update arrayIndicator and SuppressRootIndicator
                OasResponseRecord oasResponseRecord = dslContext().selectFrom(OAS_RESPONSE).where(OAS_RESPONSE.OAS_OPERATION_ID.eq(ULong.valueOf(bieForOasDoc.getOasOperationId()))).fetchOptional().orElse(null);
                if (oasResponseRecord == null) {
                    throw new ScoreDataAccessException(new IllegalArgumentException());
                }
                if (oasResponseRecord != null && BooleanToByte(bieForOasDoc.isArrayIndicator()) != oasResponseRecord.getMakeArrayIndicator()) {
                    oasResponseChangedField.add(OAS_RESPONSE.MAKE_ARRAY_INDICATOR);
                    oasResponseRecord.setMakeArrayIndicator(BooleanToByte(bieForOasDoc.isArrayIndicator()));
                }

                if (oasResponseRecord != null && BooleanToByte(bieForOasDoc.isSuppressRootIndicator()) != oasResponseRecord.getSuppressRootIndicator()) {
                    oasResponseChangedField.add(OAS_RESPONSE.SUPPRESS_ROOT_INDICATOR);
                    oasResponseRecord.setSuppressRootIndicator(BooleanToByte(bieForOasDoc.isSuppressRootIndicator()));
                }

                oasResponseChangedField.add(OAS_RESPONSE.LAST_UPDATED_BY);
                oasResponseRecord.setLastUpdatedBy(ULong.valueOf(requesterUserId));
                oasResponseChangedField.add(OAS_RESPONSE.LAST_UPDATE_TIMESTAMP);
                oasResponseRecord.setLastUpdateTimestamp(timestamp);
                affectedRows = oasResponseRecord.update(oasResponseChangedField);
                if (affectedRows != 1) {
                    throw new ScoreDataAccessException(new IllegalStateException());
                }
            }

        }
        return new UpdateBieForOasDocResponse(oasDocId, !oasResourceChangedField.isEmpty() || !oasOperationChangedField.isEmpty());
    }

    @Override
    public DeleteBieForOasDocResponse deleteBieForOasDoc(DeleteBieForOasDocRequest request) throws ScoreDataAccessException {
        return null;
    }
}
