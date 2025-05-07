package org.oagi.score.gateway.http.api.business_term_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.business_term_management.controller.payload.AssignBusinessTermRequest;
import org.oagi.score.gateway.http.api.business_term_management.model.AsbieBusinessTermId;
import org.oagi.score.gateway.http.api.business_term_management.model.BbieBusinessTermId;
import org.oagi.score.gateway.http.api.business_term_management.model.BieToAssign;
import org.oagi.score.gateway.http.api.business_term_management.model.BusinessTermId;
import org.oagi.score.gateway.http.api.business_term_management.repository.BusinessTermCommandRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.*;
import org.oagi.score.gateway.http.common.util.StringUtils;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.jooq.impl.DSL.count;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;
import static org.oagi.score.gateway.http.common.util.ScoreGuidUtils.randomGuid;

public class JooqBusinessTermCommandRepository extends JooqBaseRepository implements BusinessTermCommandRepository {

    public JooqBusinessTermCommandRepository(DSLContext dslContext, ScoreUser requester, RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public BusinessTermId create(
            String businessTerm, String externalReferenceId, String externalReferenceUri,
            String definition, String comment) {

        var query = repositoryFactory().businessTermQueryRepository(requester());

        boolean exists = dslContext().selectCount()
                .from(BUSINESS_TERM)
                .where(BUSINESS_TERM.EXTERNAL_REF_URI.eq(externalReferenceUri))
                .fetchOptionalInto(Integer.class).orElse(0) > 0;
        if (!exists) {
            BusinessTermRecord record = new BusinessTermRecord();

            record.setGuid(randomGuid());
            record.setBusinessTerm(businessTerm);
            record.setExternalRefId(externalReferenceId);
            record.setExternalRefUri(externalReferenceUri);
            record.setDefinition(definition);
            record.setComment(comment);
            record.setCreatedBy(valueOf(requester().userId()));
            record.setLastUpdatedBy(valueOf(requester().userId()));
            LocalDateTime timestamp = LocalDateTime.now();
            record.setCreationTimestamp(timestamp);
            record.setLastUpdateTimestamp(timestamp);

            return new BusinessTermId(
                    dslContext().insertInto(BUSINESS_TERM)
                            .set(record)
                            .returning(BUSINESS_TERM.BUSINESS_TERM_ID)
                            .fetchOne().getBusinessTermId().toBigInteger());
        } else {
            dslContext().update(BUSINESS_TERM)
                    .set(BUSINESS_TERM.BUSINESS_TERM_, businessTerm)
                    .set(BUSINESS_TERM.EXTERNAL_REF_ID, externalReferenceId)
                    .set(BUSINESS_TERM.DEFINITION, definition)
                    .set(BUSINESS_TERM.COMMENT, comment)
                    .set(BUSINESS_TERM.LAST_UPDATE_TIMESTAMP, LocalDateTime.now())
                    .set(BUSINESS_TERM.LAST_UPDATED_BY, valueOf(requester().userId()))
                    .where(BUSINESS_TERM.EXTERNAL_REF_URI.eq(externalReferenceUri))
                    .execute();

            return new BusinessTermId(
                    dslContext().select(BUSINESS_TERM.BUSINESS_TERM_ID)
                            .from(BUSINESS_TERM)
                            .where(BUSINESS_TERM.EXTERNAL_REF_URI.eq(externalReferenceUri))
                            .fetchOneInto(BigInteger.class));
        }
    }

    @Override
    public boolean update(BusinessTermId businessTermId,
                          String businessTerm,
                          String externalReferenceId,
                          String externalReferenceUri,
                          String definition,
                          String comment) {

        if (businessTermId == null) {
            throw new IllegalArgumentException("`businessTermId` must not be null");
        }

        BusinessTermRecord record = dslContext().selectFrom(BUSINESS_TERM)
                .where(BUSINESS_TERM.BUSINESS_TERM_ID.eq(valueOf(businessTermId)))
                .fetchOptional().orElse(null);
        if (record == null) {
            throw new IllegalArgumentException("Business Term record not found.");
        }

        List<Field<?>> changedField = new ArrayList();
        if (!StringUtils.equals(businessTerm, record.getBusinessTerm())) {
            record.setBusinessTerm(businessTerm);
            changedField.add(BUSINESS_TERM.BUSINESS_TERM_);
        }
        if (!StringUtils.equals(definition, record.getDefinition())) {
            record.setDefinition(definition);
            changedField.add(BUSINESS_TERM.DEFINITION);
        }
        if (!StringUtils.equals(externalReferenceId, record.getExternalRefId())) {
            record.setExternalRefId(externalReferenceId);
            changedField.add(BUSINESS_TERM.EXTERNAL_REF_ID);
        }
        if (!StringUtils.equals(externalReferenceUri, record.getExternalRefUri())) {
            record.setExternalRefUri(externalReferenceUri);
            changedField.add(BUSINESS_TERM.EXTERNAL_REF_URI);
        }
        if (!StringUtils.equals(comment, record.getComment())) {
            record.setComment(comment);
            changedField.add(BUSINESS_TERM.COMMENT);
        }
        if (!changedField.isEmpty()) {
            record.setLastUpdatedBy(valueOf(requester().userId()));
            changedField.add(BUSINESS_TERM.LAST_UPDATED_BY);

            record.setLastUpdateTimestamp(LocalDateTime.now());
            changedField.add(BUSINESS_TERM.LAST_UPDATE_TIMESTAMP);

            int numOfUpdatedRecords = record.update(changedField);
            return numOfUpdatedRecords == 1;
        }

        return false;
    }

    @Override
    public boolean delete(BusinessTermId businessTermId) {

        int numOfDeletedRecords = dslContext().delete(BUSINESS_TERM)
                .where(BUSINESS_TERM.BUSINESS_TERM_ID.eq(valueOf(businessTermId)))
                .execute();

        return numOfDeletedRecords == 1;
    }

    @Override
    public List<BigInteger> assignBusinessTerm(BusinessTermId businessTermId, AssignBusinessTermRequest request) {

        LocalDateTime timestamp = LocalDateTime.now();

        List<BigInteger> assignedBizTermIds = request.biesToAssign().stream().map(bieToAssign -> {
            if (bieToAssign.getBieType().equals("ASBIE")) {
                BigInteger asccId = findCcIdByBie(bieToAssign);
                AsccBiztermRecord asccBiztermRecord = new AsccBiztermRecord();
                asccBiztermRecord.setAsccId(ULong.valueOf(asccId));
                asccBiztermRecord.setBusinessTermId(valueOf(businessTermId));
                asccBiztermRecord.setCreatedBy(valueOf(requester().userId()));
                asccBiztermRecord.setLastUpdatedBy(valueOf(requester().userId()));
                asccBiztermRecord.setCreationTimestamp(timestamp);
                asccBiztermRecord.setLastUpdateTimestamp(timestamp);

                ULong asccBizTermRecordId;
                asccBizTermRecordId = getAsccBizTermRecordId(asccBiztermRecord.getBusinessTermId(), asccBiztermRecord.getAsccId());
                if (asccBizTermRecordId == null) {
                    asccBizTermRecordId = dslContext().insertInto(ASCC_BIZTERM)
                            .set(asccBiztermRecord)
                            .returning(ASCC_BIZTERM.ASCC_BIZTERM_ID)
                            .fetchOne().getAsccBiztermId();
                }

                if (request.primaryIndicator()) {
                    updateOtherBieBiztermToNotPrimary(new AsbieBusinessTermId(bieToAssign.getBieId()));
                }
                AsbieBiztermRecord asbieBiztermRecord = new AsbieBiztermRecord();
                asbieBiztermRecord.setAsbieId(ULong.valueOf(bieToAssign.getBieId()));
                asbieBiztermRecord.setAsccBiztermId(asccBizTermRecordId);
                asbieBiztermRecord.setPrimaryIndicator((byte) (request.primaryIndicator() ? 1 : 0));
                asbieBiztermRecord.setTypeCode(request.typeCode());
                asbieBiztermRecord.setCreatedBy(valueOf(requester().userId()));
                asbieBiztermRecord.setLastUpdatedBy(valueOf(requester().userId()));
                asbieBiztermRecord.setCreationTimestamp(timestamp);
                asbieBiztermRecord.setLastUpdateTimestamp(timestamp);

                ULong asbieBizTermRecordId = dslContext().insertInto(ASBIE_BIZTERM)
                        .set(asbieBiztermRecord)
                        .returning(ASBIE_BIZTERM.ASBIE_BIZTERM_ID)
                        .fetchOne().getAsbieBiztermId();
                return asbieBizTermRecordId.toBigInteger();
            } else if (bieToAssign.getBieType().equals("BBIE")) {
                BigInteger bccId = findCcIdByBie(bieToAssign);
                BccBiztermRecord bccBiztermRecord = new BccBiztermRecord();
                bccBiztermRecord.setBccId(ULong.valueOf(bccId));
                bccBiztermRecord.setBusinessTermId(valueOf(businessTermId));
                bccBiztermRecord.setCreatedBy(valueOf(requester().userId()));
                bccBiztermRecord.setLastUpdatedBy(valueOf(requester().userId()));
                bccBiztermRecord.setCreationTimestamp(timestamp);
                bccBiztermRecord.setLastUpdateTimestamp(timestamp);

                ULong bccBizTermRecordId;
                bccBizTermRecordId = getBccBizTermRecordId(bccBiztermRecord.getBusinessTermId(), bccBiztermRecord.getBccId());
                if (bccBizTermRecordId == null) {
                    bccBizTermRecordId = dslContext().insertInto(BCC_BIZTERM)
                            .set(bccBiztermRecord)
                            .returning(BCC_BIZTERM.BCC_BIZTERM_ID)
                            .fetchOne().getBccBiztermId();
                }

                if (request.primaryIndicator()) {
                    updateOtherBieBiztermToNotPrimary(new BbieBusinessTermId(bieToAssign.getBieId()));
                }

                BbieBiztermRecord bbieBizTermRecord = new BbieBiztermRecord();
                bbieBizTermRecord.setBbieId(ULong.valueOf(bieToAssign.getBieId()));
                bbieBizTermRecord.setBccBiztermId(bccBizTermRecordId);
                bbieBizTermRecord.setPrimaryIndicator((byte) (request.primaryIndicator() ? 1 : 0));
                bbieBizTermRecord.setTypeCode(request.typeCode());
                bbieBizTermRecord.setCreatedBy(valueOf(requester().userId()));
                bbieBizTermRecord.setLastUpdatedBy(valueOf(requester().userId()));
                bbieBizTermRecord.setCreationTimestamp(timestamp);
                bbieBizTermRecord.setLastUpdateTimestamp(timestamp);

                ULong bbieBiztermRecordId = dslContext().insertInto(BBIE_BIZTERM)
                        .set(bbieBizTermRecord)
                        .returning(BBIE_BIZTERM.BBIE_BIZTERM_ID)
                        .fetchOne().getBbieBiztermId();
                return bbieBiztermRecordId.toBigInteger();
            } else throw new IllegalArgumentException("Wrong BIE type");
        }).collect(Collectors.toList());

        return assignedBizTermIds;
    }

    private ULong getAsccBizTermRecordId(ULong businessTermId, ULong asccId) {
        AsccBiztermRecord asccBiztermRecord = dslContext()
                .selectFrom(ASCC_BIZTERM)
                .where(and(
                        ASCC_BIZTERM.BUSINESS_TERM_ID.eq(businessTermId),
                        ASCC_BIZTERM.ASCC_ID.eq(asccId)
                ))
                .fetchOne();
        return (asccBiztermRecord == null) ? null : asccBiztermRecord.getAsccBiztermId();
    }

    private ULong getBccBizTermRecordId(ULong businessTermId, ULong bccId) {
        BccBiztermRecord bccBiztermRecord = dslContext()
                .selectFrom(BCC_BIZTERM)
                .where(and(
                        BCC_BIZTERM.BUSINESS_TERM_ID.eq(businessTermId),
                        BCC_BIZTERM.BCC_ID.eq(bccId)
                ))
                .fetchOne();
        return (bccBiztermRecord == null) ? null : bccBiztermRecord.getBccBiztermId();
    }

    private BigInteger findCcIdByBie(BieToAssign bieToAssign) {
        if (bieToAssign.getBieType().equals("ASBIE")) {
            return dslContext()
                    .select(ASCC_MANIFEST.ASCC_ID)
                    .from(ASBIE)
                    .leftJoin(ASCC_MANIFEST)
                    .on(ASBIE.BASED_ASCC_MANIFEST_ID.eq(ASCC_MANIFEST.ASCC_MANIFEST_ID))
                    .where(ASBIE.ASBIE_ID.eq(ULong.valueOf(bieToAssign.getBieId())))
                    .fetchOneInto(BigInteger.class);
        } else if (bieToAssign.getBieType().equals("BBIE")) {
            return dslContext()
                    .select(BCC_MANIFEST.BCC_ID)
                    .from(BBIE)
                    .leftJoin(BCC_MANIFEST)
                    .on(BBIE.BASED_BCC_MANIFEST_ID.eq(BCC_MANIFEST.BCC_MANIFEST_ID))
                    .where(BBIE.BBIE_ID.eq(ULong.valueOf(bieToAssign.getBieId())))
                    .fetchOneInto(BigInteger.class);
        } else throw new IllegalArgumentException("Wrong BIE type");
    }

    private int updateOtherBieBiztermToNotPrimary(AsbieBusinessTermId asbieBusinessTermId) {
        LocalDateTime timestamp = LocalDateTime.now();
        return dslContext().update(ASBIE_BIZTERM)
                .set(ASBIE_BIZTERM.PRIMARY_INDICATOR, (byte) 0)
                .set(ASBIE_BIZTERM.LAST_UPDATED_BY, valueOf(requester().userId()))
                .set(ASBIE_BIZTERM.LAST_UPDATE_TIMESTAMP, timestamp)
                .where(ASBIE_BIZTERM.ASBIE_ID.eq(valueOf(asbieBusinessTermId)))
                .execute();
    }

    private int updateOtherBieBiztermToNotPrimary(BbieBusinessTermId bbieBusinessTermId) {
        LocalDateTime timestamp = LocalDateTime.now();
        return dslContext().update(BBIE_BIZTERM)
                .set(BBIE_BIZTERM.PRIMARY_INDICATOR, (byte) 0)
                .set(BBIE_BIZTERM.LAST_UPDATED_BY, valueOf(requester().userId()))
                .set(BBIE_BIZTERM.LAST_UPDATE_TIMESTAMP, timestamp)
                .where(BBIE_BIZTERM.BBIE_ID.eq(valueOf(bbieBusinessTermId)))
                .execute();
    }

    @Override
    public boolean updateAssignment(AsbieBusinessTermId asbieBusinessTermId,
                                    String typeCode, Boolean primaryIndicator) {
        AsbieBiztermRecord record = dslContext().selectFrom(ASBIE_BIZTERM)
                .where(ASBIE_BIZTERM.ASBIE_BIZTERM_ID.eq(valueOf(asbieBusinessTermId)))
                .fetchOptional().orElse(null);
        if (record == null) {
            throw new IllegalArgumentException("ASBIE business term assignment record not found.");
        }

        List<Field<?>> changedField = new ArrayList();
        if (!StringUtils.equals(typeCode, record.getTypeCode())) {
            record.setTypeCode(typeCode);
            changedField.add(ASBIE_BIZTERM.TYPE_CODE);
        }
        if (primaryIndicator != null && primaryIndicator != ((record.getPrimaryIndicator() == 1) ? true : false)) {
            record.setPrimaryIndicator((byte) (primaryIndicator ? 1 : 0));
            changedField.add(ASBIE_BIZTERM.PRIMARY_INDICATOR);
            if (primaryIndicator) {
                updateOtherBieBiztermToNotPrimary(asbieBusinessTermId);
            }
        }
        if (!changedField.isEmpty()) {
            record.setLastUpdatedBy(valueOf(requester().userId()));
            changedField.add(ASBIE_BIZTERM.LAST_UPDATED_BY);

            record.setLastUpdateTimestamp(LocalDateTime.now());
            changedField.add(ASBIE_BIZTERM.LAST_UPDATE_TIMESTAMP);

            int numOfUpdatedRecords = record.update(changedField);
            return numOfUpdatedRecords == 1;
        }

        return false;
    }

    @Override
    public boolean updateAssignment(BbieBusinessTermId bbieBusinessTermId,
                                    String typeCode, Boolean primaryIndicator) {
        BbieBiztermRecord record = dslContext().selectFrom(BBIE_BIZTERM)
                .where(BBIE_BIZTERM.BBIE_BIZTERM_ID.eq(valueOf(bbieBusinessTermId)))
                .fetchOptional().orElse(null);
        if (record == null) {
            throw new IllegalArgumentException("BBIE business term assignment record not found.");
        }
        List<Field<?>> changedField = new ArrayList();
        if (!StringUtils.equals(typeCode, record.getTypeCode())) {
            record.setTypeCode(typeCode);
            changedField.add(BBIE_BIZTERM.TYPE_CODE);
        }
        if (primaryIndicator != null && primaryIndicator != ((record.getPrimaryIndicator() == 1) ? true : false)) {
            record.setPrimaryIndicator((byte) (primaryIndicator ? 1 : 0));
            changedField.add(BBIE_BIZTERM.PRIMARY_INDICATOR);
            if (primaryIndicator) {
                updateOtherBieBiztermToNotPrimary(bbieBusinessTermId);
            }
        }
        if (!changedField.isEmpty()) {
            record.setLastUpdatedBy(valueOf(requester().userId()));
            changedField.add(BBIE_BIZTERM.LAST_UPDATED_BY);

            record.setLastUpdateTimestamp(LocalDateTime.now());
            changedField.add(BBIE_BIZTERM.LAST_UPDATE_TIMESTAMP);

            int numOfUpdatedRecords = record.update(changedField);
            return numOfUpdatedRecords == 1;
        }

        return false;
    }

    @Override
    public boolean delete(AsbieBusinessTermId asbieBusinessTermId) {
        List<ULong> asccBizTermIds = dslContext()
                .select(ASBIE_BIZTERM.ASCC_BIZTERM_ID)
                .from(ASBIE_BIZTERM)
                .where(ASBIE_BIZTERM.ASBIE_BIZTERM_ID.eq(valueOf(asbieBusinessTermId)))
                .fetchInto(ULong.class);
        int numOfDeletedRecords = dslContext()
                .delete(ASBIE_BIZTERM)
                .where(ASBIE_BIZTERM.ASBIE_BIZTERM_ID.eq(valueOf(asbieBusinessTermId)))
                .execute();

        deleteUnusedAsccBizTerm(asccBizTermIds);

        return numOfDeletedRecords == 1;
    }

    private void deleteUnusedAsccBizTerm(List<ULong> asccBizTermIds) {
        List<ULong> asccIdsToKeep = dslContext()
                .select(ASBIE_BIZTERM.ASCC_BIZTERM_ID)
                .from(ASBIE_BIZTERM)
                .where(ASBIE_BIZTERM.ASCC_BIZTERM_ID.in(asccBizTermIds))
                .groupBy(ASBIE_BIZTERM.ASCC_BIZTERM_ID)
                .having(count(ASBIE_BIZTERM.ASBIE_BIZTERM_ID).gt(0))
                .fetchInto(ULong.class);
        asccBizTermIds.removeIf(id -> asccIdsToKeep.contains(id));
        if (asccBizTermIds.size() != 0) {
            dslContext().delete(ASCC_BIZTERM)
                    .where(
                            asccBizTermIds.size() == 1 ?
                                    ASCC_BIZTERM.ASCC_BIZTERM_ID.eq(asccBizTermIds.get(0)) :
                                    ASCC_BIZTERM.ASCC_BIZTERM_ID.in(asccBizTermIds)
                    ).execute();
        }
    }

    @Override
    public boolean delete(BbieBusinessTermId bbieBusinessTermId) {
        List<ULong> bccBizTermIds = dslContext()
                .select(BBIE_BIZTERM.BCC_BIZTERM_ID)
                .from(BBIE_BIZTERM)
                .where(BBIE_BIZTERM.BBIE_BIZTERM_ID.eq(valueOf(bbieBusinessTermId)))
                .fetchInto(ULong.class);

        int numOfDeletedRecords = dslContext().delete(BBIE_BIZTERM)
                .where(BBIE_BIZTERM.BBIE_BIZTERM_ID.eq(valueOf(bbieBusinessTermId)))
                .execute();

        deleteUnusedBccBizTerms(bccBizTermIds);

        return numOfDeletedRecords == 1;
    }

    private void deleteUnusedBccBizTerms(List<ULong> bccBizTermIds) {
        List<ULong> bccIdsToKeep = dslContext()
                .select(BBIE_BIZTERM.BCC_BIZTERM_ID)
                .from(BBIE_BIZTERM)
                .where(BBIE_BIZTERM.BCC_BIZTERM_ID.in(bccBizTermIds))
                .groupBy(BBIE_BIZTERM.BCC_BIZTERM_ID)
                .having(count(BBIE_BIZTERM.BBIE_BIZTERM_ID).gt(0))
                .fetchInto(ULong.class);
        bccBizTermIds.removeIf(id -> bccIdsToKeep.contains(id));
        if (bccBizTermIds.size() != 0) {
            dslContext().delete(BCC_BIZTERM)
                    .where(
                            bccBizTermIds.size() == 1 ?
                                    BCC_BIZTERM.BCC_BIZTERM_ID.eq(bccBizTermIds.get(0)) :
                                    BCC_BIZTERM.BCC_BIZTERM_ID.in(bccBizTermIds)
                    ).execute();
        }
    }

}
