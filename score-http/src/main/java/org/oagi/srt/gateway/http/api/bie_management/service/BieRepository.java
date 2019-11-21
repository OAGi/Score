package org.oagi.srt.gateway.http.api.bie_management.service;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.srt.data.BieState;
import org.oagi.srt.data.OagisComponentType;
import org.oagi.srt.entity.jooq.Tables;
import org.oagi.srt.entity.jooq.tables.records.TopLevelAbieRecord;
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

@Repository
public class BieRepository {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private SessionService sessionService;

    public long getCurrentAccIdByTopLevelAbieId(long topLevelAbieId) {
        return dslContext.select(Tables.ACC.CURRENT_ACC_ID)
                .from(Tables.ABIE)
                .join(Tables.TOP_LEVEL_ABIE).on(Tables.ABIE.ABIE_ID.eq(Tables.TOP_LEVEL_ABIE.ABIE_ID))
                .join(Tables.ACC).on(Tables.ABIE.BASED_ACC_ID.eq(Tables.ACC.ACC_ID))
                .where(Tables.TOP_LEVEL_ABIE.TOP_LEVEL_ABIE_ID.eq(ULong.valueOf(topLevelAbieId)))
                .fetchOneInto(Long.class);
    }

    public BieEditAcc getAccByCurrentAccId(long currentAccId, long releaseId) {
        // BIE only can see the ACCs whose state is in Published.
        List<BieEditAcc> accList = dslContext.select(
                Tables.ACC.ACC_ID,
                Tables.ACC.CURRENT_ACC_ID,
                Tables.ACC.BASED_ACC_ID,
                Tables.ACC.OAGIS_COMPONENT_TYPE,
                Tables.ACC.REVISION_NUM,
                Tables.ACC.REVISION_TRACKING_NUM,
                Tables.ACC.RELEASE_ID)
                .from(Tables.ACC)
                .where(and(
                        Tables.ACC.REVISION_NUM.greaterThan(0),
                        Tables.ACC.STATE.eq(CcState.Published.getValue()),
                        Tables.ACC.CURRENT_ACC_ID.eq(ULong.valueOf(currentAccId))))
                .fetchInto(BieEditAcc.class);
        return CcUtility.getLatestEntity(releaseId, accList);
    }

    public BieEditAcc getAcc(long accId) {
        return dslContext.select(
                Tables.ACC.ACC_ID,
                Tables.ACC.CURRENT_ACC_ID,
                Tables.ACC.BASED_ACC_ID,
                Tables.ACC.OAGIS_COMPONENT_TYPE,
                Tables.ACC.REVISION_NUM,
                Tables.ACC.REVISION_TRACKING_NUM,
                Tables.ACC.RELEASE_ID)
                .from(Tables.ACC)
                .where(Tables.ACC.ACC_ID.eq(ULong.valueOf(accId)))
                .fetchOptionalInto(BieEditAcc.class).orElse(null);
    }

    public BieEditBbiep getBbiep(long bbiepId, long topLevelAbieId) {
        return dslContext.select(
                Tables.BBIEP.BBIEP_ID,
                Tables.BBIEP.BASED_BCCP_ID)
                .from(Tables.BBIEP)
                .where(and(
                        Tables.BBIEP.OWNER_TOP_LEVEL_ABIE_ID.eq(ULong.valueOf(topLevelAbieId)),
                        Tables.BBIEP.BBIEP_ID.eq(ULong.valueOf(bbiepId))))
                .fetchOptionalInto(BieEditBbiep.class).orElse(null);
    }

    public BccForBie getBcc(long bccId) {
        return dslContext.select(
                Tables.BCC.BCC_ID,
                Tables.BCC.CURRENT_BCC_ID,
                Tables.BCC.GUID,
                Tables.BCC.CARDINALITY_MIN,
                Tables.BCC.CARDINALITY_MAX,
                Tables.BCC.DEN,
                Tables.BCC.DEFINITION,
                Tables.BCC.FROM_ACC_ID,
                Tables.BCC.TO_BCCP_ID,
                Tables.BCC.ENTITY_TYPE,
                Tables.BCC.REVISION_NUM,
                Tables.BCC.REVISION_TRACKING_NUM,
                Tables.BCC.RELEASE_ID)
                .from(Tables.BCC)
                .where(Tables.BCC.BCC_ID.eq(ULong.valueOf(bccId)))
                .fetchOptionalInto(BccForBie.class).orElse(null);
    }

    public BieEditBccp getBccp(long bccpId) {
        return dslContext.select(
                Tables.BCCP.BCCP_ID,
                Tables.BCCP.CURRENT_BCCP_ID,
                Tables.BCCP.GUID,
                Tables.BCCP.BDT_ID,
                Tables.BCCP.PROPERTY_TERM,
                Tables.BCCP.REVISION_NUM,
                Tables.BCCP.REVISION_TRACKING_NUM,
                Tables.BCCP.RELEASE_ID)
                .from(Tables.BCCP)
                .where(Tables.BCCP.BCCP_ID.eq(ULong.valueOf(bccpId)))
                .fetchOptionalInto(BieEditBccp.class).orElse(null);
    }

    public int getCountDtScByOwnerDtId(long ownerDtId) {
        return dslContext.selectCount()
                .from(Tables.DT_SC)
                .where(and(
                        Tables.DT_SC.OWNER_DT_ID.eq(ULong.valueOf(ownerDtId)),
                        Tables.DT_SC.CARDINALITY_MAX.ne(0)
                )).fetchOptionalInto(Integer.class).orElse(0);
    }

    public int getCountBbieScByBbieIdAndIsUsedAndOwnerTopLevelAbieId(Long bbieId,
                                                                     boolean used, long ownerTopLevelAbieId) {
        if (bbieId == null || bbieId == 0L) {
            return 0;
        }

        return dslContext.selectCount()
                .from(Tables.BBIE_SC)
                .where(and(Tables.BBIE_SC.BBIE_ID.eq(ULong.valueOf(bbieId)),
                        Tables.BBIE_SC.IS_USED.eq((byte) ((used) ? 1 : 0)),
                        Tables.BBIE_SC.OWNER_TOP_LEVEL_ABIE_ID.eq(ULong.valueOf(ownerTopLevelAbieId))))
                .fetchOptionalInto(Integer.class).orElse(0);
    }

    public List<BieEditBdtSc> getBdtScListByOwnerDtId(long ownerDtId) {
        return dslContext.select(
                Tables.DT_SC.DT_SC_ID,
                Tables.DT_SC.GUID,
                Tables.DT_SC.PROPERTY_TERM,
                Tables.DT_SC.REPRESENTATION_TERM,
                Tables.DT_SC.OWNER_DT_ID)
                .from(Tables.DT_SC)
                .where(and(
                        Tables.DT_SC.OWNER_DT_ID.eq(ULong.valueOf(ownerDtId)),
                        Tables.DT_SC.CARDINALITY_MAX.ne(0)
                )).fetchInto(BieEditBdtSc.class);
    }

    public BieEditBbieSc getBbieScIdByBbieIdAndDtScId(long bbieId, long dtScId, long topLevelAbieId) {
        return dslContext.select(
                Tables.BBIE_SC.BBIE_SC_ID,
                Tables.BBIE_SC.BBIE_ID,
                Tables.BBIE_SC.DT_SC_ID,
                Tables.BBIE_SC.IS_USED.as("used"))
                .from(Tables.BBIE_SC)
                .where(and(
                        Tables.BBIE_SC.BBIE_ID.eq(ULong.valueOf(bbieId)),
                        Tables.BBIE_SC.DT_SC_ID.eq(ULong.valueOf(dtScId)),
                        Tables.BBIE_SC.OWNER_TOP_LEVEL_ABIE_ID.eq(ULong.valueOf(topLevelAbieId))
                )).fetchOptionalInto(BieEditBbieSc.class).orElse(null);
    }


    public String getAsccpPropertyTermByAsbiepId(long asbiepId) {
        return dslContext.select(
                Tables.ASCCP.PROPERTY_TERM)
                .from(Tables.ASCCP)
                .join(Tables.ASBIEP).on(Tables.ASCCP.ASCCP_ID.eq(Tables.ASBIEP.BASED_ASCCP_ID))
                .where(Tables.ASBIEP.ASBIEP_ID.eq(ULong.valueOf(asbiepId)))
                .fetchOptionalInto(String.class).orElse(null);
    }

    public String getBccpPropertyTermByBbiepId(long bbiepId) {
        return dslContext.select(
                Tables.BCCP.PROPERTY_TERM)
                .from(Tables.BCCP)
                .join(Tables.BBIEP).on(Tables.BCCP.BCCP_ID.eq(Tables.BBIEP.BASED_BCCP_ID))
                .where(Tables.BBIEP.BBIEP_ID.eq(ULong.valueOf(bbiepId)))
                .fetchOptionalInto(String.class).orElse(null);
    }

    public BieEditAsccp getAsccpByCurrentAsccpId(long currentAsccpId, long releaseId) {
        List<BieEditAsccp> asccpList = dslContext.select(
                Tables.ASCCP.ASCCP_ID,
                Tables.ASCCP.CURRENT_ASCCP_ID,
                Tables.ASCCP.GUID,
                Tables.ASCCP.PROPERTY_TERM,
                Tables.ASCCP.ROLE_OF_ACC_ID,
                Tables.ASCCP.REVISION_NUM,
                Tables.ASCCP.REVISION_TRACKING_NUM,
                Tables.ASCCP.RELEASE_ID)
                .from(Tables.ASCCP)
                .where(and(
                        Tables.ASCCP.REVISION_NUM.greaterThan(0),
                        Tables.ASCCP.CURRENT_ASCCP_ID.eq(ULong.valueOf(currentAsccpId))))
                .fetchInto(BieEditAsccp.class);
        return CcUtility.getLatestEntity(releaseId, asccpList);
    }

    public BieEditBccp getBccpByCurrentBccpId(long currentBccpId, long releaseId) {
        List<BieEditBccp> bccpList = dslContext.select(
                Tables.BCCP.BCCP_ID,
                Tables.BCCP.CURRENT_BCCP_ID,
                Tables.BCCP.GUID,
                Tables.BCCP.PROPERTY_TERM,
                Tables.BCCP.BDT_ID,
                Tables.BCCP.REVISION_NUM,
                Tables.BCCP.REVISION_TRACKING_NUM,
                Tables.BCCP.RELEASE_ID)
                .from(Tables.BCCP)
                .where(and(
                        Tables.BCCP.REVISION_NUM.greaterThan(0),
                        Tables.BCCP.CURRENT_BCCP_ID.eq(ULong.valueOf(currentBccpId))))
                .fetchInto(BieEditBccp.class);
        return CcUtility.getLatestEntity(releaseId, bccpList);
    }

    public BieEditAbie getAbieByAsbiepId(long asbiepId) {
        return dslContext.select(
                Tables.ABIE.ABIE_ID,
                Tables.ABIE.BASED_ACC_ID)
                .from(Tables.ABIE)
                .join(Tables.ASBIEP).on(Tables.ABIE.ABIE_ID.eq(Tables.ASBIEP.ROLE_OF_ABIE_ID))
                .where(Tables.ASBIEP.ASBIEP_ID.eq(ULong.valueOf(asbiepId)))
                .fetchOptionalInto(BieEditAbie.class).orElse(null);
    }

    public List<BieEditAsbie> getAsbieListByFromAbieId(long fromAbieId, BieEditNode node) {
        return dslContext.select(
                Tables.ASBIE.ASBIE_ID,
                Tables.ASBIE.FROM_ABIE_ID,
                Tables.ASBIE.TO_ASBIEP_ID,
                Tables.ASBIE.BASED_ASCC_ID,
                Tables.ASBIE.IS_USED.as("used"))
                .from(Tables.ASBIE)
                .where(and(
                        Tables.ASBIE.OWNER_TOP_LEVEL_ABIE_ID.eq(ULong.valueOf(node.getTopLevelAbieId())),
                        Tables.ASBIE.FROM_ABIE_ID.eq(ULong.valueOf(fromAbieId))
                ))
                .fetchInto(BieEditAsbie.class);
    }

    public List<BieEditBbie> getBbieListByFromAbieId(long fromAbieId, BieEditNode node) {
        return dslContext.select(
                Tables.BBIE.BBIE_ID,
                Tables.BBIE.FROM_ABIE_ID,
                Tables.BBIE.TO_BBIEP_ID,
                Tables.BBIE.BASED_BCC_ID,
                Tables.BBIE.IS_USED.as("used"))
                .from(Tables.BBIE)
                .where(and(
                        Tables.BBIE.OWNER_TOP_LEVEL_ABIE_ID.eq(ULong.valueOf(node.getTopLevelAbieId())),
                        Tables.BBIE.FROM_ABIE_ID.eq(ULong.valueOf(fromAbieId))
                ))
                .fetchInto(BieEditBbie.class);
    }

    public long getRoleOfAccIdByAsbiepId(long asbiepId) {
        return dslContext.select(Tables.ASCCP.ROLE_OF_ACC_ID)
                .from(Tables.ASCCP)
                .join(Tables.ASBIEP).on(Tables.ASCCP.ASCCP_ID.eq(Tables.ASBIEP.BASED_ASCCP_ID))
                .where(Tables.ASBIEP.ASBIEP_ID.eq(ULong.valueOf(asbiepId)))
                .fetchOptionalInto(Long.class).orElse(0L);
    }

    public long getRoleOfAccIdByAsccpId(long asccpId) {
        return dslContext.select(Tables.ASCCP.ROLE_OF_ACC_ID)
                .from(Tables.ASCCP)
                .where(Tables.ASCCP.ASCCP_ID.eq(ULong.valueOf(asccpId)))
                .fetchOptionalInto(Long.class).orElse(0L);
    }

    public List<BieEditAscc> getAsccListByFromAccId(long fromAccId, long releaseId) {
        return getAsccListByFromAccId(fromAccId, releaseId, false);
    }

    public List<BieEditAscc> getAsccListByFromAccId(long fromAccId, long releaseId, boolean isPublished) {
        List<Condition> conditions = new ArrayList(Arrays.asList(
                Tables.ASCC.REVISION_NUM.greaterThan(0),
                Tables.ASCC.FROM_ACC_ID.eq(ULong.valueOf(fromAccId)),
                Tables.ASCC.RELEASE_ID.lessOrEqual(ULong.valueOf(releaseId))
        ));

        if (isPublished) {
            conditions.add(
                    Tables.ASCC.STATE.eq(CcState.Published.getValue())
            );
        }

        return dslContext.select(
                Tables.ASCC.ASCC_ID,
                Tables.ASCC.CURRENT_ASCC_ID,
                Tables.ASCC.GUID,
                Tables.ASCC.FROM_ACC_ID,
                Tables.ASCC.TO_ASCCP_ID,
                Tables.ASCC.SEQ_KEY,
                Tables.ASCC.REVISION_NUM,
                Tables.ASCC.REVISION_TRACKING_NUM,
                Tables.ASCC.RELEASE_ID)
                .from(Tables.ASCC)
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
                Tables.BCC.REVISION_NUM.greaterThan(0),
                Tables.BCC.FROM_ACC_ID.eq(ULong.valueOf(fromAccId)),
                Tables.BCC.RELEASE_ID.lessOrEqual(ULong.valueOf(releaseId))
        ));

        if (isPublished) {
            conditions.add(
                    Tables.BCC.STATE.eq(CcState.Published.getValue())
            );
        }

        List<BieEditBcc> bccList = dslContext.select(
                Tables.BCC.BCC_ID,
                Tables.BCC.CURRENT_BCC_ID,
                Tables.BCC.GUID,
                Tables.BCC.FROM_ACC_ID,
                Tables.BCC.TO_BCCP_ID,
                Tables.BCC.SEQ_KEY,
                Tables.BCC.ENTITY_TYPE,
                Tables.BCC.REVISION_NUM,
                Tables.BCC.REVISION_TRACKING_NUM,
                Tables.BCC.RELEASE_ID)
                .from(Tables.BCC)
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

        return dslContext.insertInto(Tables.TOP_LEVEL_ABIE)
                .set(record)
                .returning().fetchOne().getTopLevelAbieId().longValue();
    }

    public void updateAbieIdOnTopLevelAbie(long abieId, long topLevelAbieId) {
        dslContext.update(Tables.TOP_LEVEL_ABIE)
                .set(Tables.TOP_LEVEL_ABIE.ABIE_ID, ULong.valueOf(abieId))
                .where(Tables.TOP_LEVEL_ABIE.TOP_LEVEL_ABIE_ID.eq(ULong.valueOf(topLevelAbieId)))
                .execute();
    }

    public List<Long> getBizCtxIdByTopLevelAbieId(long topLevelAbieId) {
        return dslContext.select(Tables.BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID)
                .from(Tables.BIZ_CTX_ASSIGNMENT)
                .join(Tables.TOP_LEVEL_ABIE).on(Tables.BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ABIE_ID.eq(Tables.TOP_LEVEL_ABIE.TOP_LEVEL_ABIE_ID))
                .where(Tables.TOP_LEVEL_ABIE.TOP_LEVEL_ABIE_ID.eq(ULong.valueOf(topLevelAbieId)))
                .fetchInto(Long.class);
    }

    public void createBizCtxAssignments(long topLevelAbieId, List<Long> bizCtxIds) {
        bizCtxIds.stream().forEach(bizCtxId -> {
            dslContext.insertInto(Tables.BIZ_CTX_ASSIGNMENT)
                    .set(Tables.BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ABIE_ID, ULong.valueOf(topLevelAbieId))
                    .set(Tables.BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID, ULong.valueOf(bizCtxId))
                    .execute();
        });
    }

    public long createAbie(User user, long basedAccId, long topLevelAbieId) {
        long userId = sessionService.userId(user);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        return dslContext.insertInto(Tables.ABIE)
                .set(Tables.ABIE.GUID, SrtGuid.randomGuid())
                .set(Tables.ABIE.BASED_ACC_ID, ULong.valueOf(basedAccId))
                .set(Tables.ABIE.CREATED_BY, ULong.valueOf(userId))
                .set(Tables.ABIE.LAST_UPDATED_BY, ULong.valueOf(userId))
                .set(Tables.ABIE.CREATION_TIMESTAMP, timestamp)
                .set(Tables.ABIE.LAST_UPDATE_TIMESTAMP, timestamp)
                .set(Tables.ABIE.STATE, BieState.Editing.getValue())
                .set(Tables.ABIE.OWNER_TOP_LEVEL_ABIE_ID, ULong.valueOf(topLevelAbieId))
                .returning().fetchOne().getAbieId().longValue();
    }

    public long createAsbiep(User user, long asccpId, long abieId, long topLevelAbieId) {
        long userId = sessionService.userId(user);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        return dslContext.insertInto(Tables.ASBIEP)
                .set(Tables.ASBIEP.GUID, SrtGuid.randomGuid())
                .set(Tables.ASBIEP.BASED_ASCCP_ID, ULong.valueOf(asccpId))
                .set(Tables.ASBIEP.ROLE_OF_ABIE_ID, ULong.valueOf(abieId))
                .set(Tables.ASBIEP.CREATED_BY, ULong.valueOf(userId))
                .set(Tables.ASBIEP.LAST_UPDATED_BY, ULong.valueOf(userId))
                .set(Tables.ASBIEP.CREATION_TIMESTAMP, timestamp)
                .set(Tables.ASBIEP.LAST_UPDATE_TIMESTAMP, timestamp)
                .set(Tables.ASBIEP.OWNER_TOP_LEVEL_ABIE_ID, ULong.valueOf(topLevelAbieId))
                .returning().fetchOne().getAsbiepId().longValue();
    }

    public long createAsbie(User user, long fromAbieId, long toAsbiepId, long basedAsccId,
                            int seqKey, long topLevelAbieId) {

        long userId = sessionService.userId(user);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        Cardinality cardinality = dslContext.select(
                Tables.ASCC.CARDINALITY_MIN,
                Tables.ASCC.CARDINALITY_MAX).from(Tables.ASCC)
                .where(Tables.ASCC.ASCC_ID.eq(ULong.valueOf(basedAsccId)))
                .fetchOneInto(Cardinality.class);

        return dslContext.insertInto(Tables.ASBIE)
                .set(Tables.ASBIE.GUID, SrtGuid.randomGuid())
                .set(Tables.ASBIE.FROM_ABIE_ID, ULong.valueOf(fromAbieId))
                .set(Tables.ASBIE.TO_ASBIEP_ID, ULong.valueOf(toAsbiepId))
                .set(Tables.ASBIE.BASED_ASCC_ID, ULong.valueOf(basedAsccId))
                .set(Tables.ASBIE.CARDINALITY_MIN, cardinality.getCardinalityMin())
                .set(Tables.ASBIE.CARDINALITY_MAX, cardinality.getCardinalityMax())
                .set(Tables.ASBIE.IS_NILLABLE, (byte) ((0)))
                .set(Tables.ASBIE.CREATED_BY, ULong.valueOf(userId))
                .set(Tables.ASBIE.LAST_UPDATED_BY, ULong.valueOf(userId))
                .set(Tables.ASBIE.CREATION_TIMESTAMP, timestamp)
                .set(Tables.ASBIE.LAST_UPDATE_TIMESTAMP, timestamp)
                .set(Tables.ASBIE.SEQ_KEY, BigDecimal.valueOf(seqKey))
                .set(Tables.ASBIE.IS_USED, (byte) ((0)))
                .set(Tables.ASBIE.OWNER_TOP_LEVEL_ABIE_ID, ULong.valueOf(topLevelAbieId))
                .returning().fetchOne().getAsbieId().longValue();

    }

    public long createBbiep(User user, long basedBccpId, long topLevelAbieId) {
        long userId = sessionService.userId(user);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        return dslContext.insertInto(Tables.BBIEP)
                .set(Tables.BBIEP.GUID, SrtGuid.randomGuid())
                .set(Tables.BBIEP.BASED_BCCP_ID, ULong.valueOf(basedBccpId))
                .set(Tables.BBIEP.CREATED_BY, ULong.valueOf(userId))
                .set(Tables.BBIEP.LAST_UPDATED_BY, ULong.valueOf(userId))
                .set(Tables.BBIEP.CREATION_TIMESTAMP, timestamp)
                .set(Tables.BBIEP.LAST_UPDATE_TIMESTAMP, timestamp)
                .set(Tables.BBIEP.OWNER_TOP_LEVEL_ABIE_ID, ULong.valueOf(topLevelAbieId))
                .returning().fetchOne().getBbiepId().longValue();
    }

    public long createBbie(User user, long fromAbieId,
                           long toBbiepId, long basedBccId, long bdtId,
                           int seqKey, long topLevelAbieId) {

        long userId = sessionService.userId(user);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        Cardinality cardinality = dslContext.select(
                Tables.BCC.CARDINALITY_MIN,
                Tables.BCC.CARDINALITY_MAX).from(Tables.BCC)
                .where(Tables.BCC.BCC_ID.eq(ULong.valueOf(basedBccId)))
                .fetchOneInto(Cardinality.class);

        return dslContext.insertInto(Tables.BBIE)
                .set(Tables.BBIE.GUID, SrtGuid.randomGuid())
                .set(Tables.BBIE.FROM_ABIE_ID, ULong.valueOf(fromAbieId))
                .set(Tables.BBIE.TO_BBIEP_ID, ULong.valueOf(toBbiepId))
                .set(Tables.BBIE.BASED_BCC_ID, ULong.valueOf(basedBccId))
                .set(Tables.BBIE.BDT_PRI_RESTRI_ID, ULong.valueOf(getDefaultBdtPriRestriIdByBdtId(bdtId)))
                .set(Tables.BBIE.CARDINALITY_MIN, cardinality.getCardinalityMin())
                .set(Tables.BBIE.CARDINALITY_MAX, cardinality.getCardinalityMax())
                .set(Tables.BBIE.IS_NILLABLE, (byte) ((0)))
                .set(Tables.BBIE.IS_NULL, (byte) ((0)))
                .set(Tables.BBIE.CREATED_BY, ULong.valueOf(userId))
                .set(Tables.BBIE.LAST_UPDATED_BY, ULong.valueOf(userId))
                .set(Tables.BBIE.CREATION_TIMESTAMP, timestamp)
                .set(Tables.BBIE.LAST_UPDATE_TIMESTAMP, timestamp)
                .set(Tables.BBIE.SEQ_KEY, BigDecimal.valueOf(seqKey))
                .set(Tables.BBIE.IS_USED, (byte) ((0)))
                .set(Tables.BBIE.OWNER_TOP_LEVEL_ABIE_ID, ULong.valueOf(topLevelAbieId))
                .returning().fetchOne().getBbieId().longValue();
    }

    public long getDefaultBdtPriRestriIdByBdtId(long bdtId) {
        return dslContext.select(
                Tables.BDT_PRI_RESTRI.BDT_PRI_RESTRI_ID)
                .from(Tables.BDT_PRI_RESTRI)
                .where(and(
                        Tables.BDT_PRI_RESTRI.BDT_ID.eq(ULong.valueOf(bdtId))),
                        Tables.BDT_PRI_RESTRI.IS_DEFAULT.eq((byte) ((1))))
                .fetchOptionalInto(Long.class).orElse(0L);
    }

    public long createBbieSc(User user, long bbieId, long dtScId,
                             long topLevelAbieId) {

        Cardinality cardinality = dslContext.select(
                Tables.DT_SC.CARDINALITY_MIN,
                Tables.DT_SC.CARDINALITY_MAX).from(Tables.DT_SC)
                .where(Tables.DT_SC.DT_SC_ID.eq(ULong.valueOf(dtScId)))
                .fetchOneInto(Cardinality.class);

        return dslContext.insertInto(Tables.BBIE_SC)
                .set(Tables.BBIE_SC.GUID, SrtGuid.randomGuid())
                .set(Tables.BBIE_SC.BBIE_ID, ULong.valueOf(bbieId))
                .set(Tables.BBIE_SC.DT_SC_ID, ULong.valueOf(dtScId))
                .set(Tables.BBIE_SC.DT_SC_PRI_RESTRI_ID, ULong.valueOf(getDefaultDtScPriRestriIdByDtScId(dtScId)))
                .set(Tables.BBIE_SC.CARDINALITY_MIN, cardinality.getCardinalityMin())
                .set(Tables.BBIE_SC.CARDINALITY_MAX, cardinality.getCardinalityMax())
                .set(Tables.BBIE_SC.IS_USED, (byte) (0))
                .set(Tables.BBIE_SC.OWNER_TOP_LEVEL_ABIE_ID, ULong.valueOf(topLevelAbieId))
                .returning().fetchOne().getBbieScId().longValue();
    }

    public long getDefaultDtScPriRestriIdByDtScId(long dtScId) {
        return dslContext.select(
                Tables.BDT_SC_PRI_RESTRI.BDT_SC_PRI_RESTRI_ID)
                .from(Tables.BDT_SC_PRI_RESTRI)
                .where(and(
                        Tables.BDT_SC_PRI_RESTRI.BDT_SC_ID.eq(ULong.valueOf(dtScId)),
                        Tables.BDT_SC_PRI_RESTRI.IS_DEFAULT.eq((byte) 1)
                ))
                .fetchOptionalInto(Long.class).orElse(0L);
    }

    public void updateState(long topLevelAbieId, BieState state) {
        dslContext.update(Tables.TOP_LEVEL_ABIE)
                .set(Tables.TOP_LEVEL_ABIE.STATE, state.getValue())
                .where(Tables.TOP_LEVEL_ABIE.TOP_LEVEL_ABIE_ID.eq(ULong.valueOf(topLevelAbieId)))
                .execute();

        dslContext.update(Tables.ABIE)
                .set(Tables.ABIE.STATE, state.getValue())
                .where(Tables.ABIE.OWNER_TOP_LEVEL_ABIE_ID.eq(ULong.valueOf(topLevelAbieId)))
                .execute();
    }

    public OagisComponentType getOagisComponentTypeOfAccByAsccpId(long asccpId) {
        int oagisComponentType = dslContext.select(Tables.ACC.OAGIS_COMPONENT_TYPE)
                .from(Tables.ACC)
                .join(Tables.ASCCP).on(Tables.ASCCP.ROLE_OF_ACC_ID.eq(Tables.ACC.ACC_ID))
                .where(Tables.ASCCP.ASCCP_ID.eq(ULong.valueOf(asccpId)))
                .fetchOneInto(Integer.class);
        return OagisComponentType.valueOf(oagisComponentType);
    }

}
