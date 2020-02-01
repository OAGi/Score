package org.oagi.srt.gateway.http.api.bie_management.service;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.tools.StringUtils;
import org.jooq.types.ULong;
import org.oagi.srt.data.BieState;
import org.oagi.srt.data.OagisComponentType;
import org.oagi.srt.entity.jooq.tables.records.*;
import org.oagi.srt.gateway.http.api.bie_management.data.bie_edit.*;
import org.oagi.srt.gateway.http.api.cc_management.data.CcState;
import org.oagi.srt.gateway.http.api.cc_management.helper.CcUtility;
import org.oagi.srt.gateway.http.configuration.security.SessionService;
import org.oagi.srt.gateway.http.helper.SrtGuid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.jooq.impl.DSL.and;
import static org.oagi.srt.entity.jooq.Tables.*;

@Repository
public class BieRepository {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private SessionService sessionService;

    public long getCurrentAccIdByTopLevelAbieId(long topLevelAbieId) {
        return dslContext.select(ACC.CURRENT_ACC_ID)
                .from(ABIE)
                .join(TOP_LEVEL_ABIE).on(ABIE.ABIE_ID.eq(TOP_LEVEL_ABIE.ABIE_ID))
                .join(ACC).on(ABIE.BASED_ACC_ID.eq(ACC.ACC_ID))
                .where(TOP_LEVEL_ABIE.TOP_LEVEL_ABIE_ID.eq(ULong.valueOf(topLevelAbieId)))
                .fetchOneInto(Long.class);
    }

    public BieEditAcc getAccByCurrentAccId(long currentAccId, long releaseId) {
        // BIE only can see the ACCs whose state is in Published.
        List<BieEditAcc> accList = dslContext.select(
                ACC.ACC_ID,
                ACC.CURRENT_ACC_ID,
                ACC.BASED_ACC_ID,
                ACC.OAGIS_COMPONENT_TYPE,
                ACC.REVISION_NUM,
                ACC.REVISION_TRACKING_NUM,
                ACC.RELEASE_ID)
                .from(ACC)
                .where(and(
                        ACC.REVISION_NUM.greaterThan(0),
                        ACC.STATE.eq(CcState.Published.getValue()),
                        ACC.CURRENT_ACC_ID.eq(ULong.valueOf(currentAccId))))
                .fetchInto(BieEditAcc.class);
        return CcUtility.getLatestEntity(releaseId, accList);
    }

    public BieEditAcc getAcc(long accId) {
        return dslContext.select(
                ACC.ACC_ID,
                ACC.CURRENT_ACC_ID,
                ACC.BASED_ACC_ID,
                ACC.OAGIS_COMPONENT_TYPE,
                ACC.REVISION_NUM,
                ACC.REVISION_TRACKING_NUM,
                ACC.RELEASE_ID)
                .from(ACC)
                .where(ACC.ACC_ID.eq(ULong.valueOf(accId)))
                .fetchOptionalInto(BieEditAcc.class).orElse(null);
    }

    public BieEditBbiep getBbiep(long bbiepId, long topLevelAbieId) {
        return dslContext.select(
                BBIEP.BBIEP_ID,
                BBIEP.BASED_BCCP_ID)
                .from(BBIEP)
                .where(and(
                        BBIEP.OWNER_TOP_LEVEL_ABIE_ID.eq(ULong.valueOf(topLevelAbieId)),
                        BBIEP.BBIEP_ID.eq(ULong.valueOf(bbiepId))))
                .fetchOptionalInto(BieEditBbiep.class).orElse(null);
    }

    public BccForBie getBcc(long bccId) {
        return dslContext.select(
                BCC.BCC_ID,
                BCC.CURRENT_BCC_ID,
                BCC.GUID,
                BCC.CARDINALITY_MIN,
                BCC.CARDINALITY_MAX,
                BCC.DEN,
                BCC.DEFINITION,
                BCC.FROM_ACC_ID,
                BCC.TO_BCCP_ID,
                BCC.ENTITY_TYPE,
                BCC.REVISION_NUM,
                BCC.REVISION_TRACKING_NUM,
                BCC.RELEASE_ID)
                .from(BCC)
                .where(BCC.BCC_ID.eq(ULong.valueOf(bccId)))
                .fetchOptionalInto(BccForBie.class).orElse(null);
    }

    public BieEditBccp getBccp(long bccpId) {
        return dslContext.select(
                BCCP.BCCP_ID,
                BCCP.CURRENT_BCCP_ID,
                BCCP.GUID,
                BCCP.BDT_ID,
                BCCP.PROPERTY_TERM,
                BCCP.REVISION_NUM,
                BCCP.REVISION_TRACKING_NUM,
                BCCP.RELEASE_ID)
                .from(BCCP)
                .where(BCCP.BCCP_ID.eq(ULong.valueOf(bccpId)))
                .fetchOptionalInto(BieEditBccp.class).orElse(null);
    }

    public int getCountDtScByOwnerDtId(long ownerDtId) {
        return dslContext.selectCount()
                .from(DT_SC)
                .where(and(
                        DT_SC.OWNER_DT_ID.eq(ULong.valueOf(ownerDtId)),
                        DT_SC.CARDINALITY_MAX.ne(0)
                )).fetchOptionalInto(Integer.class).orElse(0);
    }

    public int getCountBbieScByBbieIdAndIsUsedAndOwnerTopLevelAbieId(Long bbieId,
                                                                     boolean used, long ownerTopLevelAbieId) {
        if (bbieId == null || bbieId == 0L) {
            return 0;
        }

        return dslContext.selectCount()
                .from(BBIE_SC)
                .where(and(BBIE_SC.BBIE_ID.eq(ULong.valueOf(bbieId)),
                        BBIE_SC.IS_USED.eq((byte) ((used) ? 1 : 0)),
                        BBIE_SC.OWNER_TOP_LEVEL_ABIE_ID.eq(ULong.valueOf(ownerTopLevelAbieId))))
                .fetchOptionalInto(Integer.class).orElse(0);
    }

    public List<BieEditBdtSc> getBdtScListByOwnerDtId(long ownerDtId) {
        return dslContext.select(
                DT_SC.DT_SC_ID,
                DT_SC.GUID,
                DT_SC.PROPERTY_TERM,
                DT_SC.REPRESENTATION_TERM,
                DT_SC.OWNER_DT_ID)
                .from(DT_SC)
                .where(and(
                        DT_SC.OWNER_DT_ID.eq(ULong.valueOf(ownerDtId)),
                        DT_SC.CARDINALITY_MAX.ne(0)
                )).fetchInto(BieEditBdtSc.class);
    }

    public BieEditBbieSc getBbieScIdByBbieIdAndDtScId(long bbieId, long dtScId, long topLevelAbieId) {
        return dslContext.select(
                BBIE_SC.BBIE_SC_ID,
                BBIE_SC.BBIE_ID,
                BBIE_SC.DT_SC_ID,
                BBIE_SC.IS_USED.as("used"))
                .from(BBIE_SC)
                .where(and(
                        BBIE_SC.BBIE_ID.eq(ULong.valueOf(bbieId)),
                        BBIE_SC.DT_SC_ID.eq(ULong.valueOf(dtScId)),
                        BBIE_SC.OWNER_TOP_LEVEL_ABIE_ID.eq(ULong.valueOf(topLevelAbieId))
                )).fetchOptionalInto(BieEditBbieSc.class).orElse(null);
    }


    public String getAsccpPropertyTermByAsbiepId(long asbiepId) {
        return dslContext.select(
                ASCCP.PROPERTY_TERM)
                .from(ASCCP)
                .join(ASBIEP).on(ASCCP.ASCCP_ID.eq(ASBIEP.BASED_ASCCP_ID))
                .where(ASBIEP.ASBIEP_ID.eq(ULong.valueOf(asbiepId)))
                .fetchOptionalInto(String.class).orElse(null);
    }

    public String getBccpPropertyTermByBbiepId(long bbiepId) {
        return dslContext.select(
                BCCP.PROPERTY_TERM)
                .from(BCCP)
                .join(BBIEP).on(BCCP.BCCP_ID.eq(BBIEP.BASED_BCCP_ID))
                .where(BBIEP.BBIEP_ID.eq(ULong.valueOf(bbiepId)))
                .fetchOptionalInto(String.class).orElse(null);
    }

    public BieEditAsccp getAsccpByCurrentAsccpId(long currentAsccpId, long releaseId) {
        List<BieEditAsccp> asccpList = dslContext.select(
                ASCCP.ASCCP_ID,
                ASCCP.CURRENT_ASCCP_ID,
                ASCCP.GUID,
                ASCCP.PROPERTY_TERM,
                ASCCP.ROLE_OF_ACC_ID,
                ASCCP.REVISION_NUM,
                ASCCP.REVISION_TRACKING_NUM,
                ASCCP.RELEASE_ID)
                .from(ASCCP)
                .where(and(
                        ASCCP.REVISION_NUM.greaterThan(0),
                        ASCCP.CURRENT_ASCCP_ID.eq(ULong.valueOf(currentAsccpId))))
                .fetchInto(BieEditAsccp.class);
        return CcUtility.getLatestEntity(releaseId, asccpList);
    }

    public BieEditBccp getBccpByCurrentBccpId(long currentBccpId, long releaseId) {
        List<BieEditBccp> bccpList = dslContext.select(
                BCCP.BCCP_ID,
                BCCP.CURRENT_BCCP_ID,
                BCCP.GUID,
                BCCP.PROPERTY_TERM,
                BCCP.BDT_ID,
                BCCP.REVISION_NUM,
                BCCP.REVISION_TRACKING_NUM,
                BCCP.RELEASE_ID)
                .from(BCCP)
                .where(and(
                        BCCP.REVISION_NUM.greaterThan(0),
                        BCCP.CURRENT_BCCP_ID.eq(ULong.valueOf(currentBccpId))))
                .fetchInto(BieEditBccp.class);
        return CcUtility.getLatestEntity(releaseId, bccpList);
    }

    public BieEditAbie getAbieByAsbiepId(long asbiepId) {
        return dslContext.select(
                ABIE.ABIE_ID,
                ABIE.BASED_ACC_ID)
                .from(ABIE)
                .join(ASBIEP).on(ABIE.ABIE_ID.eq(ASBIEP.ROLE_OF_ABIE_ID))
                .where(ASBIEP.ASBIEP_ID.eq(ULong.valueOf(asbiepId)))
                .fetchOptionalInto(BieEditAbie.class).orElse(null);
    }

    public List<BieEditAsbie> getAsbieListByFromAbieId(long fromAbieId, BieEditNode node) {
        return dslContext.select(
                ASBIE.ASBIE_ID,
                ASBIE.FROM_ABIE_ID,
                ASBIE.TO_ASBIEP_ID,
                ASBIE.BASED_ASCC_ID,
                ASBIE.IS_USED.as("used"),
                ASBIE.CARDINALITY_MIN,
                ASBIE.CARDINALITY_MAX)
                .from(ASBIE)
                .where(and(
                        ASBIE.OWNER_TOP_LEVEL_ABIE_ID.eq(ULong.valueOf(node.getTopLevelAbieId())),
                        ASBIE.FROM_ABIE_ID.eq(ULong.valueOf(fromAbieId))
                ))
                .fetchInto(BieEditAsbie.class);
    }

    public List<BieEditBbie> getBbieListByFromAbieId(long fromAbieId, BieEditNode node) {
        return dslContext.select(
                BBIE.BBIE_ID,
                BBIE.FROM_ABIE_ID,
                BBIE.TO_BBIEP_ID,
                BBIE.BASED_BCC_ID,
                BBIE.IS_USED.as("used"),
                BBIE.CARDINALITY_MIN,
                BBIE.CARDINALITY_MAX)
                .from(BBIE)
                .where(and(
                        BBIE.OWNER_TOP_LEVEL_ABIE_ID.eq(ULong.valueOf(node.getTopLevelAbieId())),
                        BBIE.FROM_ABIE_ID.eq(ULong.valueOf(fromAbieId))
                ))
                .fetchInto(BieEditBbie.class);
    }

    public long getRoleOfAccIdByAsbiepId(long asbiepId) {
        return dslContext.select(ASCCP.ROLE_OF_ACC_ID)
                .from(ASCCP)
                .join(ASBIEP).on(ASCCP.ASCCP_ID.eq(ASBIEP.BASED_ASCCP_ID))
                .where(ASBIEP.ASBIEP_ID.eq(ULong.valueOf(asbiepId)))
                .fetchOptionalInto(Long.class).orElse(0L);
    }

    public long getRoleOfAccIdByAsccpId(long asccpId) {
        return dslContext.select(ASCCP.ROLE_OF_ACC_ID)
                .from(ASCCP)
                .where(ASCCP.ASCCP_ID.eq(ULong.valueOf(asccpId)))
                .fetchOptionalInto(Long.class).orElse(0L);
    }

    public List<BieEditAscc> getAsccListByFromAccId(long fromAccId, long releaseId) {
        return getAsccListByFromAccId(fromAccId, releaseId, false);
    }

    public List<BieEditAscc> getAsccListByFromAccId(long fromAccId, long releaseId, boolean isPublished) {
        List<Condition> conditions = new ArrayList(Arrays.asList(
                ASCC.REVISION_NUM.greaterThan(0),
                ASCC.FROM_ACC_ID.eq(ULong.valueOf(fromAccId)),
                ASCC.RELEASE_ID.lessOrEqual(ULong.valueOf(releaseId))
        ));

        if (isPublished) {
            conditions.add(
                    ASCC.STATE.eq(CcState.Published.getValue())
            );
        }

        return dslContext.select(
                ASCC.ASCC_ID,
                ASCC.CURRENT_ASCC_ID,
                ASCC.GUID,
                ASCC.FROM_ACC_ID,
                ASCC.TO_ASCCP_ID,
                ASCC.SEQ_KEY,
                ASCC.CARDINALITY_MIN,
                ASCC.CARDINALITY_MAX,
                ASCC.REVISION_NUM,
                ASCC.REVISION_TRACKING_NUM,
                ASCC.RELEASE_ID)
                .from(ASCC)
                .where(and(conditions))
                .fetchInto(BieEditAscc.class)
                .stream()
                .collect(groupingBy(BieEditAscc::getGuid)).values().stream()
                .map(e -> CcUtility.getLatestEntity(releaseId, e))
                .filter(e -> e != null).collect(Collectors.toList());
    }

    public List<BieEditBcc> getBccListByFromAccId(long fromAccId, long releaseId) {
        return getBccListByFromAccId(fromAccId, releaseId, false);
    }

    public List<BieEditBcc> getBccListByFromAccId(long fromAccId, long releaseId, boolean isPublished) {
        List<Condition> conditions = new ArrayList(Arrays.asList(
                BCC.REVISION_NUM.greaterThan(0),
                BCC.FROM_ACC_ID.eq(ULong.valueOf(fromAccId)),
                BCC.RELEASE_ID.lessOrEqual(ULong.valueOf(releaseId))
        ));

        if (isPublished) {
            conditions.add(
                    BCC.STATE.eq(CcState.Published.getValue())
            );
        }

        List<BieEditBcc> bccList = dslContext.select(
                BCC.BCC_ID,
                BCC.CURRENT_BCC_ID,
                BCC.GUID,
                BCC.FROM_ACC_ID,
                BCC.TO_BCCP_ID,
                BCC.SEQ_KEY,
                BCC.ENTITY_TYPE,
                BCC.CARDINALITY_MIN,
                BCC.CARDINALITY_MAX,
                BCC.REVISION_NUM,
                BCC.REVISION_TRACKING_NUM,
                BCC.RELEASE_ID)
                .from(BCC)
                .where(and(conditions))
                .fetchInto(BieEditBcc.class);

        return bccList.stream().collect(groupingBy(BieEditBcc::getGuid)).values().stream()
                .map(e -> CcUtility.getLatestEntity(releaseId, e))
                .filter(e -> e != null).collect(Collectors.toList());
    }

    public long createTopLevelAbie(long userId, long releaseId, BieState state) {
        TopLevelAbieRecord record = new TopLevelAbieRecord();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        record.setOwnerUserId(ULong.valueOf(userId));
        record.setReleaseId(ULong.valueOf(releaseId));
        record.setState(state.getValue());
        record.setLastUpdatedBy(ULong.valueOf(userId));
        record.setLastUpdateTimestamp(timestamp);

        return dslContext.insertInto(TOP_LEVEL_ABIE)
                .set(record)
                .returning().fetchOne().getTopLevelAbieId().longValue();
    }

    public void updateAbieIdOnTopLevelAbie(long abieId, long topLevelAbieId) {
        dslContext.update(TOP_LEVEL_ABIE)
                .set(TOP_LEVEL_ABIE.ABIE_ID, ULong.valueOf(abieId))
                .where(TOP_LEVEL_ABIE.TOP_LEVEL_ABIE_ID.eq(ULong.valueOf(topLevelAbieId)))
                .execute();
    }

    public List<Long> getBizCtxIdByTopLevelAbieId(long topLevelAbieId) {
        return dslContext.select(BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID)
                .from(BIZ_CTX_ASSIGNMENT)
                .join(TOP_LEVEL_ABIE).on(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ABIE_ID.eq(TOP_LEVEL_ABIE.TOP_LEVEL_ABIE_ID))
                .where(TOP_LEVEL_ABIE.TOP_LEVEL_ABIE_ID.eq(ULong.valueOf(topLevelAbieId)))
                .fetchInto(Long.class);
    }

    public void createBizCtxAssignments(long topLevelAbieId, List<Long> bizCtxIds) {
        bizCtxIds.stream().forEach(bizCtxId -> {
            dslContext.insertInto(BIZ_CTX_ASSIGNMENT)
                    .set(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ABIE_ID, ULong.valueOf(topLevelAbieId))
                    .set(BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID, ULong.valueOf(bizCtxId))
                    .execute();
        });
    }

    public AbieRecord createAbie(User user, long basedAccId, long topLevelAbieId) {
        long userId = sessionService.userId(user);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        return dslContext.insertInto(ABIE)
                .set(ABIE.GUID, SrtGuid.randomGuid())
                .set(ABIE.BASED_ACC_ID, ULong.valueOf(basedAccId))
                .set(ABIE.CREATED_BY, ULong.valueOf(userId))
                .set(ABIE.LAST_UPDATED_BY, ULong.valueOf(userId))
                .set(ABIE.CREATION_TIMESTAMP, timestamp)
                .set(ABIE.LAST_UPDATE_TIMESTAMP, timestamp)
                .set(ABIE.STATE, BieState.Editing.getValue())
                .set(ABIE.OWNER_TOP_LEVEL_ABIE_ID, ULong.valueOf(topLevelAbieId))
                .returning().fetchOne();
    }

    public AsbiepRecord createAsbiep(User user, long asccpId, long abieId, long topLevelAbieId) {
        long userId = sessionService.userId(user);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        return dslContext.insertInto(ASBIEP)
                .set(ASBIEP.GUID, SrtGuid.randomGuid())
                .set(ASBIEP.BASED_ASCCP_ID, ULong.valueOf(asccpId))
                .set(ASBIEP.ROLE_OF_ABIE_ID, ULong.valueOf(abieId))
                .set(ASBIEP.CREATED_BY, ULong.valueOf(userId))
                .set(ASBIEP.LAST_UPDATED_BY, ULong.valueOf(userId))
                .set(ASBIEP.CREATION_TIMESTAMP, timestamp)
                .set(ASBIEP.LAST_UPDATE_TIMESTAMP, timestamp)
                .set(ASBIEP.OWNER_TOP_LEVEL_ABIE_ID, ULong.valueOf(topLevelAbieId))
                .returning().fetchOne();
    }

    public AsbieRecord createAsbie(User user, long fromAbieId, long toAsbiepId, long basedAsccId,
                                   int seqKey, long topLevelAbieId) {

        long userId = sessionService.userId(user);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        Record2<Integer, Integer> cardinality = dslContext.select(
                ASCC.CARDINALITY_MIN,
                ASCC.CARDINALITY_MAX).from(ASCC)
                .where(ASCC.ASCC_ID.eq(ULong.valueOf(basedAsccId)))
                .fetchOne();

        Byte AsccpNillable = dslContext.select(ASCCP.IS_NILLABLE).from(ASCCP)
                .join(ASCC).on(ASCCP.ASCCP_ID.eq(ASCC.TO_ASCCP_ID))
                .where(ASCC.ASCC_ID.eq(ULong.valueOf(basedAsccId))).fetchOne().getValue(ASCCP.IS_NILLABLE);

        return dslContext.insertInto(ASBIE)
                .set(ASBIE.GUID, SrtGuid.randomGuid())
                .set(ASBIE.FROM_ABIE_ID, ULong.valueOf(fromAbieId))
                .set(ASBIE.TO_ASBIEP_ID, ULong.valueOf(toAsbiepId))
                .set(ASBIE.BASED_ASCC_ID, ULong.valueOf(basedAsccId))
                .set(ASBIE.CARDINALITY_MIN, cardinality.get(ASCC.CARDINALITY_MIN))
                .set(ASBIE.CARDINALITY_MAX, cardinality.get(ASCC.CARDINALITY_MAX))
                .set(ASBIE.IS_NILLABLE, AsccpNillable)
                .set(ASBIE.CREATED_BY, ULong.valueOf(userId))
                .set(ASBIE.LAST_UPDATED_BY, ULong.valueOf(userId))
                .set(ASBIE.CREATION_TIMESTAMP, timestamp)
                .set(ASBIE.LAST_UPDATE_TIMESTAMP, timestamp)
                .set(ASBIE.SEQ_KEY, BigDecimal.valueOf(seqKey))
                .set(ASBIE.IS_USED, (byte) (cardinality.get(ASCC.CARDINALITY_MIN) > 0 ? 1 : 0))
                .set(ASBIE.OWNER_TOP_LEVEL_ABIE_ID, ULong.valueOf(topLevelAbieId))
                .returning().fetchOne();

    }

    public BbiepRecord createBbiep(User user, long basedBccpId, long topLevelAbieId) {
        long userId = sessionService.userId(user);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        return dslContext.insertInto(BBIEP)
                .set(BBIEP.GUID, SrtGuid.randomGuid())
                .set(BBIEP.BASED_BCCP_ID, ULong.valueOf(basedBccpId))
                .set(BBIEP.CREATED_BY, ULong.valueOf(userId))
                .set(BBIEP.LAST_UPDATED_BY, ULong.valueOf(userId))
                .set(BBIEP.CREATION_TIMESTAMP, timestamp)
                .set(BBIEP.LAST_UPDATE_TIMESTAMP, timestamp)
                .set(BBIEP.OWNER_TOP_LEVEL_ABIE_ID, ULong.valueOf(topLevelAbieId))
                .returning().fetchOne();
    }

    public BbieRecord createBbie(User user, long fromAbieId,
                                 long toBbiepId, long basedBccId, long bdtId,
                                 int seqKey, long topLevelAbieId) {

        long userId = sessionService.userId(user);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        BccRecord bccRecord = dslContext.select(
                BCC.TO_BCCP_ID,
                BCC.CARDINALITY_MIN,
                BCC.CARDINALITY_MAX,
                BCC.DEFAULT_VALUE,
                BCC.FIXED_VALUE,
                BCC.IS_NILLABLE)
                .from(BCC)
                .where(BCC.BCC_ID.eq(ULong.valueOf(basedBccId)))
                .fetchOneInto(BccRecord.class);

        BccpRecord bccpRecord = dslContext.select(
                BCCP.DEFAULT_VALUE,
                BCCP.FIXED_VALUE)
                .from(BCCP)
                .where(BCCP.BCCP_ID.eq(bccRecord.getToBccpId()))
                .fetchOneInto(BccpRecord.class);

        return dslContext.insertInto(BBIE)
                .set(BBIE.GUID, SrtGuid.randomGuid())
                .set(BBIE.FROM_ABIE_ID, ULong.valueOf(fromAbieId))
                .set(BBIE.TO_BBIEP_ID, ULong.valueOf(toBbiepId))
                .set(BBIE.BASED_BCC_ID, ULong.valueOf(basedBccId))
                .set(BBIE.BDT_PRI_RESTRI_ID, ULong.valueOf(getDefaultBdtPriRestriIdByBdtId(bdtId)))
                .set(BBIE.CARDINALITY_MIN, bccRecord.getCardinalityMin())
                .set(BBIE.CARDINALITY_MAX, bccRecord.getCardinalityMax())
                .set(BBIE.IS_NILLABLE, bccRecord.getIsNillable())
                .set(BBIE.IS_NULL, (byte) ((0)))
                .set(BBIE.CREATED_BY, ULong.valueOf(userId))
                .set(BBIE.LAST_UPDATED_BY, ULong.valueOf(userId))
                .set(BBIE.CREATION_TIMESTAMP, timestamp)
                .set(BBIE.LAST_UPDATE_TIMESTAMP, timestamp)
                .set(BBIE.SEQ_KEY, BigDecimal.valueOf(seqKey))
                .set(BBIE.IS_USED, (byte) (bccRecord.getCardinalityMin() > 0 ? 1 : 0))
                .set(BBIE.OWNER_TOP_LEVEL_ABIE_ID, ULong.valueOf(topLevelAbieId))
                .set(BBIE.DEFAULT_VALUE, StringUtils.defaultIfEmpty(bccRecord.getDefaultValue(), bccpRecord.getDefaultValue()))
                .set(BBIE.FIXED_VALUE, StringUtils.defaultIfEmpty(bccRecord.getFixedValue(), bccpRecord.getFixedValue()))
                .returning().fetchOne();
    }

    public long getDefaultBdtPriRestriIdByBdtId(long bdtId) {
        return dslContext.select(
                BDT_PRI_RESTRI.BDT_PRI_RESTRI_ID)
                .from(BDT_PRI_RESTRI)
                .where(and(
                        BDT_PRI_RESTRI.BDT_ID.eq(ULong.valueOf(bdtId))),
                        BDT_PRI_RESTRI.IS_DEFAULT.eq((byte) ((1))))
                .fetchOptionalInto(Long.class).orElse(0L);
    }

    public long createBbieSc(User user, long bbieId, long dtScId,
                             long topLevelAbieId) {

        DtScRecord dtScRecord = dslContext.selectFrom(DT_SC)
                .where(DT_SC.DT_SC_ID.eq(ULong.valueOf(dtScId)))
                .fetchOne();

        return dslContext.insertInto(BBIE_SC)
                .set(BBIE_SC.GUID, SrtGuid.randomGuid())
                .set(BBIE_SC.BBIE_ID, ULong.valueOf(bbieId))
                .set(BBIE_SC.DT_SC_ID, ULong.valueOf(dtScId))
                .set(BBIE_SC.DT_SC_PRI_RESTRI_ID, ULong.valueOf(getDefaultDtScPriRestriIdByDtScId(dtScId)))
                .set(BBIE_SC.CARDINALITY_MIN, dtScRecord.getCardinalityMin())
                .set(BBIE_SC.CARDINALITY_MAX, dtScRecord.getCardinalityMax())
                .set(BBIE_SC.IS_USED, (byte)(dtScRecord.getCardinalityMin() > 0 ? 1 : 0))
                .set(BBIE_SC.OWNER_TOP_LEVEL_ABIE_ID, ULong.valueOf(topLevelAbieId))
                .set(BBIE_SC.DEFAULT_VALUE, dtScRecord.getDefaultValue())
                .set(BBIE_SC.FIXED_VALUE, dtScRecord.getFixedValue())
                .returning().fetchOne().getBbieScId().longValue();
    }

    public long getDefaultDtScPriRestriIdByDtScId(long dtScId) {
        return dslContext.select(
                BDT_SC_PRI_RESTRI.BDT_SC_PRI_RESTRI_ID)
                .from(BDT_SC_PRI_RESTRI)
                .where(and(
                        BDT_SC_PRI_RESTRI.BDT_SC_ID.eq(ULong.valueOf(dtScId)),
                        BDT_SC_PRI_RESTRI.IS_DEFAULT.eq((byte) 1)
                ))
                .fetchOptionalInto(Long.class).orElse(0L);
    }

    public void updateState(long topLevelAbieId, BieState state) {
        dslContext.update(TOP_LEVEL_ABIE)
                .set(TOP_LEVEL_ABIE.STATE, state.getValue())
                .where(TOP_LEVEL_ABIE.TOP_LEVEL_ABIE_ID.eq(ULong.valueOf(topLevelAbieId)))
                .execute();

        dslContext.update(ABIE)
                .set(ABIE.STATE, state.getValue())
                .where(ABIE.OWNER_TOP_LEVEL_ABIE_ID.eq(ULong.valueOf(topLevelAbieId)))
                .execute();
    }

    public OagisComponentType getOagisComponentTypeOfAccByAsccpId(long asccpId) {
        int oagisComponentType = dslContext.select(ACC.OAGIS_COMPONENT_TYPE)
                .from(ACC)
                .join(ASCCP).on(ASCCP.ROLE_OF_ACC_ID.eq(ACC.ACC_ID))
                .where(ASCCP.ASCCP_ID.eq(ULong.valueOf(asccpId)))
                .fetchOneInto(Integer.class);
        return OagisComponentType.valueOf(oagisComponentType);
    }

}
