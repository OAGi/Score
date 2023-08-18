package org.oagi.score.repo.api.impl.jooq.businessterm;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.businessterm.BusinessTermWriteRepository;
import org.oagi.score.repo.api.businessterm.model.*;
import org.oagi.score.repo.api.impl.jooq.JooqScoreRepository;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.BusinessTermRecord;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.oagi.score.repo.api.security.AccessControl;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.BUSINESS_TERM;
import static org.oagi.score.repo.api.impl.jooq.utils.ScoreGuidUtils.randomGuid;
import static org.oagi.score.repo.api.user.model.ScoreRole.DEVELOPER;
import static org.oagi.score.repo.api.user.model.ScoreRole.END_USER;

public class JooqBusinessTermWriteRepository
        extends JooqScoreRepository
        implements BusinessTermWriteRepository {

    public JooqBusinessTermWriteRepository(DSLContext dslContext) {
        super(dslContext);
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public CreateBusinessTermResponse createBusinessTerm(
            CreateBusinessTermRequest request) throws ScoreDataAccessException {

        ScoreUser requester = request.getRequester();
        BigInteger requesterUserId = requester.getUserId();
        LocalDateTime timestamp = LocalDateTime.now();

        BusinessTermRecord record = new BusinessTermRecord();

        record.setGuid(randomGuid());
        record.setBusinessTerm(request.getBusinessTerm());
        record.setComment(request.getComment());
        record.setDefinition(request.getDefinition());
        record.setExternalRefId(request.getExternalReferenceId());
        record.setExternalRefUri(request.getExternalReferenceUri());
        record.setCreatedBy(ULong.valueOf(requesterUserId));
        record.setLastUpdatedBy(ULong.valueOf(requesterUserId));
        record.setCreationTimestamp(timestamp);
        record.setLastUpdateTimestamp(timestamp);

        ULong businessTermId = dslContext().insertInto(BUSINESS_TERM)
                .set(record)
                .returning(BUSINESS_TERM.BUSINESS_TERM_ID)
                .fetchOne().getBusinessTermId();
        return new CreateBusinessTermResponse(businessTermId.toBigInteger());
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public CreateBulkBusinessTermResponse createBusinessTermsFromFile(CreateBulkBusinessTermRequest request)
            throws ScoreDataAccessException {

        ScoreUser requester = request.getRequester();
        BigInteger requesterUserId = requester.getUserId();
        LocalDateTime timestamp = LocalDateTime.now();

        List<BigInteger> createdRecordIds = new ArrayList<>();
        for (BusinessTerm businessTerm : request.getBusinessTermList()) {
            BusinessTermRecord record = new BusinessTermRecord();
            record.setGuid(randomGuid());
            record.setBusinessTerm(businessTerm.getBusinessTerm());
            record.setDefinition(businessTerm.getDefinition());
            record.setComment(businessTerm.getComment());
            record.setExternalRefId(businessTerm.getExternalReferenceId());
            record.setExternalRefUri(businessTerm.getExternalReferenceUri());
            record.setCreatedBy(ULong.valueOf(requesterUserId));
            record.setLastUpdatedBy(ULong.valueOf(requesterUserId));
            record.setCreationTimestamp(timestamp);
            record.setLastUpdateTimestamp(timestamp);

            BusinessTermRecord existentRecord = dslContext().selectFrom(BUSINESS_TERM)
                    .where(BUSINESS_TERM.EXTERNAL_REF_URI.eq(businessTerm.getExternalReferenceUri()))
                    .fetchOne();
            if (existentRecord != null) {
                dslContext().update(BUSINESS_TERM)
                        .set(BUSINESS_TERM.BUSINESS_TERM_, record.getBusinessTerm())
                        .set(BUSINESS_TERM.EXTERNAL_REF_ID, record.getExternalRefId())
                        .set(BUSINESS_TERM.DEFINITION, record.getDefinition())
                        .set(BUSINESS_TERM.COMMENT, record.getComment())
                        .set(BUSINESS_TERM.LAST_UPDATE_TIMESTAMP, record.getLastUpdateTimestamp())
                        .set(BUSINESS_TERM.LAST_UPDATED_BY, record.getLastUpdatedBy())
                        .where(BUSINESS_TERM.BUSINESS_TERM_ID.eq(record.getBusinessTermId()))
                        .execute();
            } else {
                ULong createdBusinessTermId = dslContext().insertInto(BUSINESS_TERM)
                        .set(record)
                        .returning(BUSINESS_TERM.BUSINESS_TERM_ID)
                        .fetchOne().getBusinessTermId();
                createdRecordIds.add(createdBusinessTermId.toBigInteger());
            }
        }

        return new CreateBulkBusinessTermResponse(createdRecordIds);
    }


    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public UpdateBusinessTermResponse updateBusinessTerm(
            UpdateBusinessTermRequest request) throws ScoreDataAccessException {

        ScoreUser requester = request.getRequester();
        BigInteger requesterUserId = requester.getUserId();
        LocalDateTime timestamp = LocalDateTime.now();

        BusinessTermRecord record = dslContext().selectFrom(BUSINESS_TERM)
                .where(BUSINESS_TERM.BUSINESS_TERM_ID.eq(ULong.valueOf(request.getBusinessTermId())))
                .fetchOptional().orElse(null);
        if (record == null) {
            throw new ScoreDataAccessException(new IllegalArgumentException());
        }
        List<Field<?>> changedField = new ArrayList();
        if (!StringUtils.equals(request.getBusinessTerm(), record.getBusinessTerm())) {
            record.setBusinessTerm(request.getBusinessTerm());
            changedField.add(BUSINESS_TERM.BUSINESS_TERM_);
        }
        if (request.getDefinition() != null && !StringUtils.equals(request.getDefinition(), record.getDefinition())) {
            record.setDefinition(request.getDefinition());
            changedField.add(BUSINESS_TERM.DEFINITION);
        }
        if (!StringUtils.equals(request.getExternalReferenceId(), record.getExternalRefId())) {
            record.setExternalRefId(request.getExternalReferenceId());
            changedField.add(BUSINESS_TERM.EXTERNAL_REF_ID);
        }
        if (!StringUtils.equals(request.getExternalReferenceUri(), record.getExternalRefUri())) {
            record.setExternalRefUri(request.getExternalReferenceUri());
            changedField.add(BUSINESS_TERM.EXTERNAL_REF_URI);
        }
        if (request.getComment() != null && !StringUtils.equals(request.getComment(), record.getComment())) {
            record.setComment(request.getComment());
            changedField.add(BUSINESS_TERM.COMMENT);
        }
        if (!changedField.isEmpty()) {
            record.setLastUpdatedBy(ULong.valueOf(requesterUserId));
            changedField.add(BUSINESS_TERM.LAST_UPDATED_BY);

            record.setLastUpdateTimestamp(timestamp);
            changedField.add(BUSINESS_TERM.LAST_UPDATE_TIMESTAMP);

            int affectedRows = record.update(changedField);
            if (affectedRows != 1) {
                throw new ScoreDataAccessException(new IllegalStateException());
            }
        }

        return new UpdateBusinessTermResponse(
                record.getBusinessTermId().toBigInteger(),
                !changedField.isEmpty());
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public DeleteBusinessTermResponse deleteBusinessTerm(
            DeleteBusinessTermRequest request) throws ScoreDataAccessException {

        List<BigInteger> businessTermIdList = request.getBusinessTermIdList();
        if (businessTermIdList == null || businessTermIdList.isEmpty()) {
            return new DeleteBusinessTermResponse(Collections.emptyList());
        }
        try {
            dslContext().delete(BUSINESS_TERM)
                    .where(
                            businessTermIdList.size() == 1 ?
                                    BUSINESS_TERM.BUSINESS_TERM_ID.eq(ULong.valueOf(businessTermIdList.get(0))) :
                                    BUSINESS_TERM.BUSINESS_TERM_ID.in(businessTermIdList.stream().map(e -> ULong.valueOf(e)).collect(Collectors.toList()))
                    )
                    .execute();
        } catch (Exception e) {
            throw new ScoreDataAccessException("It's not possible to delete the used business term.");
        }

        DeleteBusinessTermResponse response = new DeleteBusinessTermResponse(businessTermIdList);
        return response;
    }
}
