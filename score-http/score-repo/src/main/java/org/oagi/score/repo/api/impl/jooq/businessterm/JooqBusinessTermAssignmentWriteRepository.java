package org.oagi.score.repo.api.impl.jooq.businessterm;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.businessterm.BusinessTermAssignmentWriteRepository;
import org.oagi.score.repo.api.businessterm.model.*;
import org.oagi.score.repo.api.impl.jooq.JooqScoreRepository;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AsbieBiztermRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AsccBiztermRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.BbieBiztermRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.BccBiztermRecord;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.oagi.score.repo.api.security.AccessControl;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.jooq.impl.DSL.count;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.repo.api.user.model.ScoreRole.DEVELOPER;
import static org.oagi.score.repo.api.user.model.ScoreRole.END_USER;

public class JooqBusinessTermAssignmentWriteRepository
        extends JooqScoreRepository
        implements BusinessTermAssignmentWriteRepository {

    public JooqBusinessTermAssignmentWriteRepository(DSLContext dslContext) {
        super(dslContext);
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public AssignBusinessTermResponse assignBusinessTerm(
            AssignBusinessTermRequest request) throws ScoreDataAccessException {

        ScoreUser requester = request.getRequester();
        BigInteger requesterUserId = requester.getUserId();
        LocalDateTime timestamp = LocalDateTime.now();

        List<BigInteger> assignedBizTermIds = request.getBiesToAssign().stream().map(bieToAssign -> {
            if (bieToAssign.getBieType().equals("ASBIE")) {
                BigInteger asccId = findCcIdByBie(bieToAssign);
                AsccBiztermRecord asccBiztermRecord = new AsccBiztermRecord();
                asccBiztermRecord.setAsccId(ULong.valueOf(asccId));
                asccBiztermRecord.setBusinessTermId(ULong.valueOf(request.getBusinessTermId()));
                asccBiztermRecord.setCreatedBy(ULong.valueOf(requesterUserId));
                asccBiztermRecord.setLastUpdatedBy(ULong.valueOf(requesterUserId));
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

                if (request.isPrimary()) {
                    updateOtherBieBiztermToNotPrimary(bieToAssign.getBieId(), bieToAssign.getBieType(), requesterUserId);
                }
                AsbieBiztermRecord asbieBiztermRecord = new AsbieBiztermRecord();
                asbieBiztermRecord.setAsbieId(ULong.valueOf(bieToAssign.getBieId()));
                asbieBiztermRecord.setAsccBiztermId(asccBizTermRecordId);
                asbieBiztermRecord.setPrimaryIndicator((byte) (request.isPrimary() ? 1 : 0));
                asbieBiztermRecord.setTypeCode(request.getTypeCode());
                asbieBiztermRecord.setCreatedBy(ULong.valueOf(requesterUserId));
                asbieBiztermRecord.setLastUpdatedBy(ULong.valueOf(requesterUserId));
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
                bccBiztermRecord.setBusinessTermId(ULong.valueOf(request.getBusinessTermId()));
                bccBiztermRecord.setCreatedBy(ULong.valueOf(requesterUserId));
                bccBiztermRecord.setLastUpdatedBy(ULong.valueOf(requesterUserId));
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

                if (request.isPrimary()) {
                    updateOtherBieBiztermToNotPrimary(bieToAssign.getBieId(), bieToAssign.getBieType(), requesterUserId);
                }

                BbieBiztermRecord bbieBizTermRecord = new BbieBiztermRecord();
                bbieBizTermRecord.setBbieId(ULong.valueOf(bieToAssign.getBieId()));
                bbieBizTermRecord.setBccBiztermId(bccBizTermRecordId);
                bbieBizTermRecord.setPrimaryIndicator((byte) (request.isPrimary() ? 1 : 0));
                bbieBizTermRecord.setTypeCode(request.getTypeCode());
                bbieBizTermRecord.setCreatedBy(ULong.valueOf(requesterUserId));
                bbieBizTermRecord.setLastUpdatedBy(ULong.valueOf(requesterUserId));
                bbieBizTermRecord.setCreationTimestamp(timestamp);
                bbieBizTermRecord.setLastUpdateTimestamp(timestamp);

                ULong bbieBiztermRecordId = dslContext().insertInto(BBIE_BIZTERM)
                        .set(bbieBizTermRecord)
                        .returning(BBIE_BIZTERM.BBIE_BIZTERM_ID)
                        .fetchOne().getBbieBiztermId();
                return bbieBiztermRecordId.toBigInteger();
            } else throw new ScoreDataAccessException("Wrong BIE type");
        }).collect(Collectors.toList());

        return new AssignBusinessTermResponse(assignedBizTermIds);
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

    private BigInteger findCcIdByBie(BieToAssign bieToAssign) throws ScoreDataAccessException {
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
        } else throw new ScoreDataAccessException("Wrong BIE type");
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public UpdateBusinessTermAssignmentResponse updateBusinessTermAssignment(
            UpdateBusinessTermAssignmentRequest request) throws ScoreDataAccessException {

        ScoreUser requester = request.getRequester();
        BigInteger requesterUserId = requester.getUserId();
        LocalDateTime timestamp = LocalDateTime.now();

        if (request.getBieType().equals("ASBIE")) {
            AsbieBiztermRecord record = dslContext().selectFrom(ASBIE_BIZTERM)
                    .where(ASBIE_BIZTERM.ASBIE_BIZTERM_ID.eq(ULong.valueOf(request.getAssignedBizTermId())))
                    .fetchOptional().orElse(null);
            if (record == null) {
                throw new ScoreDataAccessException(new IllegalArgumentException());
            }
            List<Field<?>> changedField = new ArrayList();
            if (!StringUtils.equals(request.getTypeCode(), record.getTypeCode())) {
                record.setTypeCode(request.getTypeCode());
                changedField.add(ASBIE_BIZTERM.TYPE_CODE);
            }
            if (request.isPrimary() != ((record.getPrimaryIndicator() == 1) ? true : false)) {
                record.setPrimaryIndicator((byte) (request.isPrimary() ? 1 : 0));
                changedField.add(ASBIE_BIZTERM.PRIMARY_INDICATOR);
                if (request.isPrimary()) {
                    updateOtherBieBiztermToNotPrimary(request.getBieId(), request.getBieType(), requesterUserId);
                }
            }
            if (!changedField.isEmpty()) {
                record.setLastUpdatedBy(ULong.valueOf(requesterUserId));
                changedField.add(ASBIE_BIZTERM.LAST_UPDATED_BY);

                record.setLastUpdateTimestamp(timestamp);
                changedField.add(ASBIE_BIZTERM.LAST_UPDATE_TIMESTAMP);

                int affectedRows = record.update(changedField);
                if (affectedRows != 1) {
                    throw new ScoreDataAccessException(new IllegalStateException());
                }
            }
            return new UpdateBusinessTermAssignmentResponse(
                    record.getAsbieBiztermId().toBigInteger(), "ASBIE", !changedField.isEmpty());
        } else if (request.getBieType().equals("BBIE")) {
            BbieBiztermRecord record = dslContext().selectFrom(BBIE_BIZTERM)
                    .where(BBIE_BIZTERM.BBIE_BIZTERM_ID.eq(ULong.valueOf(request.getAssignedBizTermId())))
                    .fetchOptional().orElse(null);
            if (record == null) {
                throw new ScoreDataAccessException(new IllegalArgumentException());
            }
            List<Field<?>> changedField = new ArrayList();
            if (!StringUtils.equals(request.getTypeCode(), record.getTypeCode())) {
                record.setTypeCode(request.getTypeCode());
                changedField.add(BBIE_BIZTERM.TYPE_CODE);
            }
            if (request.isPrimary() != ((record.getPrimaryIndicator() == 1) ? true : false)) {
                record.setPrimaryIndicator((byte) (request.isPrimary() ? 1 : 0));
                changedField.add(BBIE_BIZTERM.PRIMARY_INDICATOR);
                if (request.isPrimary()) {
                    updateOtherBieBiztermToNotPrimary(request.getBieId(), request.getBieType(), requesterUserId);
                }
            }
            if (!changedField.isEmpty()) {
                record.setLastUpdatedBy(ULong.valueOf(requesterUserId));
                changedField.add(BBIE_BIZTERM.LAST_UPDATED_BY);

                record.setLastUpdateTimestamp(timestamp);
                changedField.add(BBIE_BIZTERM.LAST_UPDATE_TIMESTAMP);

                int affectedRows = record.update(changedField);
                if (affectedRows != 1) {
                    throw new ScoreDataAccessException(new IllegalStateException());
                }
            }
            return new UpdateBusinessTermAssignmentResponse(
                    record.getBbieBiztermId().toBigInteger(), "BBIE", !changedField.isEmpty());
        } else throw new ScoreDataAccessException("Wrong BIE type");
    }

    private int updateOtherBieBiztermToNotPrimary(BigInteger bieId, String bieType, BigInteger requesterUserId)
            throws ScoreDataAccessException {
        LocalDateTime timestamp = LocalDateTime.now();

        if (bieType.equals("ASBIE")) {
            return dslContext().update(ASBIE_BIZTERM)
                    .set(ASBIE_BIZTERM.PRIMARY_INDICATOR, (byte) 0)
                    .set(ASBIE_BIZTERM.LAST_UPDATED_BY, ULong.valueOf(requesterUserId))
                    .set(ASBIE_BIZTERM.LAST_UPDATE_TIMESTAMP, timestamp)
                    .where(ASBIE_BIZTERM.ASBIE_ID.eq(ULong.valueOf(bieId)))
                    .execute();
        } else if (bieType.equals("BBIE")) {
            return dslContext().update(BBIE_BIZTERM)
                    .set(BBIE_BIZTERM.PRIMARY_INDICATOR, (byte) 0)
                    .set(BBIE_BIZTERM.LAST_UPDATED_BY, ULong.valueOf(requesterUserId))
                    .set(BBIE_BIZTERM.LAST_UPDATE_TIMESTAMP, timestamp)
                    .where(BBIE_BIZTERM.BBIE_ID.eq(ULong.valueOf(bieId)))
                    .execute();
        } else {
            throw new ScoreDataAccessException("Wrong BIE type");
        }
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public DeleteAssignedBusinessTermResponse deleteBusinessTermAssignment(
            DeleteAssignedBusinessTermRequest request) throws ScoreDataAccessException {

        List<BieToAssign> assignedBizTermIdList = request.getAssignedBtList();
        if (assignedBizTermIdList == null || assignedBizTermIdList.isEmpty()) {
            return new DeleteAssignedBusinessTermResponse(Collections.emptyList());
        }

        List<ULong> asbieIds = assignedBizTermIdList.stream().filter(a -> a.getBieType().equals("ASBIE"))
                .map(e -> ULong.valueOf(e.getBieId())).collect(Collectors.toList());
        if (!asbieIds.isEmpty()) {
            List<ULong> asccBizTermIds = dslContext()
                    .select(ASBIE_BIZTERM.ASCC_BIZTERM_ID)
                    .from(ASBIE_BIZTERM)
                    .where(
                            asbieIds.size() == 1 ?
                                    ASBIE_BIZTERM.ASBIE_BIZTERM_ID.eq(asbieIds.get(0)) :
                                    ASBIE_BIZTERM.ASBIE_BIZTERM_ID.in(asbieIds)
                    ).fetchInto(ULong.class);
            dslContext()
                    .delete(ASBIE_BIZTERM)
                    .where(
                            asbieIds.size() == 1 ?
                                    ASBIE_BIZTERM.ASBIE_BIZTERM_ID.eq(asbieIds.get(0)) :
                                    ASBIE_BIZTERM.ASBIE_BIZTERM_ID.in(asbieIds)
                    ).execute();

            deleteUnusedAsccBizTerm(asccBizTermIds);
        }

        List<ULong> bbieIds = assignedBizTermIdList.stream().filter(a -> a.getBieType().equals("BBIE"))
                .map(e -> ULong.valueOf(e.getBieId())).collect(Collectors.toList());
        if (!bbieIds.isEmpty()) {
            List<ULong> bccBizTermIds = dslContext()
                    .select(BBIE_BIZTERM.BCC_BIZTERM_ID)
                    .from(BBIE_BIZTERM)
                    .where(
                            asbieIds.size() == 1 ?
                                    BBIE_BIZTERM.BBIE_BIZTERM_ID.eq(bbieIds.get(0)) :
                                    BBIE_BIZTERM.BBIE_BIZTERM_ID.in(bbieIds)
                    ).fetchInto(ULong.class);

            dslContext().delete(BBIE_BIZTERM)
                    .where(
                            asbieIds.size() == 1 ?
                                    BBIE_BIZTERM.BBIE_BIZTERM_ID.eq(bbieIds.get(0)) :
                                    BBIE_BIZTERM.BBIE_BIZTERM_ID.in(bbieIds)
                    )
                    .execute();

            deleteUnusedBccBizTerms(bccBizTermIds);
        }

        DeleteAssignedBusinessTermResponse response = new DeleteAssignedBusinessTermResponse(assignedBizTermIdList);
        return response;
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
}
