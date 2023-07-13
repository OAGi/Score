package org.oagi.score.repo.api.impl.jooq.openapidoc;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.impl.jooq.JooqScoreRepository;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.OasResourceRecord;
import org.oagi.score.repo.api.openapidoc.BieForOasDocWriteRepository;
import org.oagi.score.repo.api.openapidoc.model.*;
import org.oagi.score.repo.api.user.model.ScoreUser;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.OAS_RESOURCE;

public class JooqBieForOasDocWriteRepository extends JooqScoreRepository
        implements BieForOasDocWriteRepository {
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

        OasResourceRecord record = dslContext().selectFrom(OAS_RESOURCE)
                .where(OAS_RESOURCE.OAS_DOC_ID.eq(ULong.valueOf(oasDocId)))
                .fetchOptional().orElse(null);
        if (record == null) {
            throw new ScoreDataAccessException(new IllegalArgumentException());
        }
        List<Field<?>> changedField = new ArrayList();

//        for(BieForOasDoc bieForOasDoc : request.getBieForOasDocList()){
//            BieForOasDoc bieForOasDocRecord = bieListRecord.stream().filter(p -> p.getTopLevelAsbiepId().equals(bieForOasDoc.getTopLevelAsbiepId())).findFirst().orElse(null);
//            if (!StringUtils.equals(bieForOasDoc.getResourceName(), bieForOasDocRecord.getResourceName())){
//                changedField.add(OAS_RESOURCE.PATH);
//            }
//            if (!StringUtils.equals(bieForOasDoc.getOperationId(), bieForOasDocRecord.getOperationId())){
//                changedField.add(OAS_OPERATION.OPERATION_ID);
//            }
//
//            if (!changedField.isEmpty()) {
//                bieForOasDoc.setLastUpdatedBy(ULong.valueOf(requesterUserId));
//                changedField.add(BUSINESS_TERM.LAST_UPDATED_BY);
//
//                bieForOasDoc.setLastUpdateTimestamp(timestamp);
//                changedField.add(BUSINESS_TERM.LAST_UPDATE_TIMESTAMP);
//
//                int affectedRows = bieForOasDocRecord.update(changedField);
//                if (affectedRows != 1) {
//                    throw new ScoreDataAccessException(new IllegalStateException());
//                }
//            }
//
//        }


        return null;
    }

    @Override
    public DeleteBieForOasDocResponse deleteBieForOasDoc(DeleteBieForOasDocRequest request) throws ScoreDataAccessException {
        return null;
    }
}
