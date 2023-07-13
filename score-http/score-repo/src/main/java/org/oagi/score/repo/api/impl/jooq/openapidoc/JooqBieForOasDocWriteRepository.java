package org.oagi.score.repo.api.impl.jooq.openapidoc;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.impl.jooq.JooqScoreRepository;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.OasOperationRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.OasResourceRecord;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.oagi.score.repo.api.openapidoc.BieForOasDocWriteRepository;
import org.oagi.score.repo.api.openapidoc.model.*;
import org.oagi.score.repo.api.user.model.ScoreUser;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.OAS_OPERATION;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.OAS_RESOURCE;

public class JooqBieForOasDocWriteRepository extends JooqScoreRepository implements BieForOasDocWriteRepository {
    public JooqBieForOasDocWriteRepository(DSLContext dslContext) {
        super(dslContext);
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

        BigInteger oasDocId = request.getOasDocId();
        if (oasDocId == null) {
            throw new IllegalArgumentException("`oasDocId` parameter must not be null.");
        }

        List<OasResourceRecord> oasResourceRecordList = dslContext().selectFrom(OAS_RESOURCE).where(OAS_RESOURCE.OAS_DOC_ID.eq(ULong.valueOf(oasDocId))).fetch();
        if (oasResourceRecordList == null) {
            throw new ScoreDataAccessException(new IllegalArgumentException());
        }
        List<Field<?>> changedField = new ArrayList();
        boolean isChanged = false;


        for (BieForOasDoc bieForOasDoc : request.getBieForOasDocList()) {
            OasResourceRecord oasResourceRecord = oasResourceRecordList.stream().filter(p -> p.getOasResourceId().equals(bieForOasDoc.getOasResourceId())).findFirst().orElse(null);

            if (oasResourceRecord != null && !StringUtils.equals(bieForOasDoc.getResourceName(), oasResourceRecord.getPath())) {
                changedField.add(OAS_RESOURCE.PATH);
                oasResourceRecord.setPath(bieForOasDoc.getResourceName());
                changedField.add(OAS_RESOURCE.LAST_UPDATED_BY);
                oasResourceRecord.setLastUpdatedBy(ULong.valueOf(requesterUserId));
                changedField.add(OAS_RESOURCE.LAST_UPDATE_TIMESTAMP);
                oasResourceRecord.setLastUpdateTimestamp(timestamp);
                int affectedRows = oasResourceRecord.update(changedField);
                if (affectedRows != 1) {
                    throw new ScoreDataAccessException(new IllegalStateException());
                }
                isChanged = true;
                changedField.clear();

                OasOperationRecord oasOperationRecord = dslContext().selectFrom(OAS_OPERATION).where(OAS_OPERATION.OAS_RESOURCE_ID.eq(ULong.valueOf((oasResourceRecord.getOasResourceId()).toBigInteger()))).fetchOptional().orElse(null);

                if (oasOperationRecord != null && !StringUtils.equals(bieForOasDoc.getOperationId(), oasOperationRecord.getOperationId())) {
                    changedField.add(OAS_OPERATION.OPERATION_ID);
                    oasOperationRecord.setOperationId(bieForOasDoc.getOperationId());
                    changedField.add(OAS_OPERATION.LAST_UPDATED_BY);
                    oasOperationRecord.setLastUpdatedBy(ULong.valueOf(requesterUserId));
                    changedField.add(OAS_OPERATION.LAST_UPDATE_TIMESTAMP);
                    oasResourceRecord.setLastUpdateTimestamp(timestamp);
                    affectedRows = oasOperationRecord.update(changedField);
                    if (affectedRows != 1) {
                        throw new ScoreDataAccessException(new IllegalStateException());
                    }
                    changedField.clear();
                }

            }
        }
        return new UpdateBieForOasDocResponse(oasDocId, isChanged);
    }

    @Override
    public DeleteBieForOasDocResponse deleteBieForOasDoc(DeleteBieForOasDocRequest request) throws ScoreDataAccessException {
        return null;
    }
}
