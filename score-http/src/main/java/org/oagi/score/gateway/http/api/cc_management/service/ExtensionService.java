package org.oagi.score.gateway.http.api.cc_management.service;

import org.jooq.DSLContext;
import org.jooq.Record5;
import org.jooq.Result;
import org.jooq.types.ULong;
import org.oagi.score.cache.impl.ACCCachingRepository;
import org.oagi.score.data.ACC;
import org.oagi.score.data.*;
import org.oagi.score.entity.jooq.Tables;
import org.oagi.score.entity.jooq.tables.records.AccRecord;
import org.oagi.score.entity.jooq.tables.records.AsccRecord;
import org.oagi.score.entity.jooq.tables.records.AsccpRecord;
import org.oagi.score.entity.jooq.tables.records.BccRecord;
import org.oagi.score.gateway.http.api.bie_management.data.bie_edit.BieEditAcc;
import org.oagi.score.gateway.http.api.cc_management.data.CcState;
import org.oagi.score.gateway.http.api.cc_management.data.ExtensionUpdateRequest;
import org.oagi.score.gateway.http.api.cc_management.data.ExtensionUpdateResponse;
import org.oagi.score.gateway.http.api.cc_management.data.node.*;
import org.oagi.score.gateway.http.api.cc_management.helper.CcUtility;
import org.oagi.score.gateway.http.api.cc_management.repository.CcNodeRepository;
import org.oagi.score.gateway.http.api.common.data.AccessPrivilege;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.gateway.http.helper.SrtGuid;
import org.oagi.score.gateway.http.helper.Utility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.jooq.impl.DSL.and;
import static org.jooq.impl.DSL.max;
import static org.oagi.score.entity.jooq.Tables.*;
import static org.oagi.score.gateway.http.api.common.data.AccessPrivilege.*;

@Service
@Transactional(readOnly = true)
public class ExtensionService {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private CcNodeRepository repository;

    @Autowired
    private CcListService ccListService;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ACCCachingRepository accRepository;


    public CcAccNode getExtensionNode(AuthenticatedPrincipal user, long extensionId, long releaseId) {
        CcAccNode ueAcc = repository.getAccNodeByAccId(extensionId, null);
        CcAsccpNode asccpNode = repository.getAsccpNodeByRoleOfAccId(ueAcc.getAccId(), null);
        CcAccNode eAcc = repository.getAccNodeFromAsccByAsccpId(asccpNode.getAsccpId(), releaseId);
        eAcc.setState(CcState.valueOf(ueAcc.getRawState()));

        long userId = sessionService.userId(user);
        long ownerUserId = dslContext.select(ACC.OWNER_USER_ID).from(ACC)
                .where(ACC.ACC_ID.eq(ULong.valueOf(ueAcc.getAccId()))).fetchOneInto(Long.class);

        AccessPrivilege accessPrivilege = Prohibited;
        switch (eAcc.getState()) {
            case Editing:
                if (userId == ownerUserId) {
                    accessPrivilege = CanEdit;
                } else {
                    accessPrivilege = Prohibited;
                }
                break;

            case Candidate:
                if (userId == ownerUserId) {
                    accessPrivilege = CanEdit;
                } else {
                    accessPrivilege = CanView;
                }

                break;

            case Published:
                accessPrivilege = CanView;
                break;
        }

        eAcc.setAccess(accessPrivilege.name());
        return eAcc;
    }

    public ACC getExistsUserExtension(long accId, long releaseId) {
        ACC ueAcc =
                dslContext.select(
                        Tables.ACC.as("ueAcc").ACC_ID,
                        Tables.ACC.as("ueAcc").OAGIS_COMPONENT_TYPE
                ).from(Tables.ACC.as("eAcc"))
                        .join(Tables.ASCC).on(Tables.ACC.as("eAcc").ACC_ID.eq(ASCC.FROM_ACC_ID))
                        .join(Tables.ASCCP).on(ASCC.TO_ASCCP_ID.eq(ASCCP.ASCCP_ID))
                        .join(Tables.ACC.as("ueAcc")).on(ASCCP.ROLE_OF_ACC_ID.eq(Tables.ACC.as("ueAcc").ACC_ID))
                        .where(and(ACC.as("eAcc").ACC_ID.eq(ULong.valueOf(accId)),
                                ASCC.REVISION_NUM.eq(0))
                        ).fetchOneInto(ACC.class);

        if (ueAcc == null) {
            return null;
        }

        if (ueAcc.getOagisComponentType() == OagisComponentType.UserExtensionGroup.getValue()) {
            return dslContext.selectFrom(Tables.ACC)
                    .where(Tables.ACC.ACC_ID.eq(ULong.valueOf(ueAcc.getAccId())))
                    .fetchOneInto(ACC.class);
        }
        return null;
    }

    public ACC getLatestUserExtension(long accId, long releaseId) {
        return dslContext.selectFrom(Tables.ACC)
                .where(Tables.ACC.CURRENT_ACC_ID.eq(ULong.valueOf(accId)))
                .orderBy(Tables.ACC.ACC_ID.desc())
                .limit(1)
                .fetchOneInto(ACC.class);
    }


    @Transactional
    public long appendUserExtension(BieEditAcc eAcc, ACC ueAcc,
                                    long releaseId, AuthenticatedPrincipal user) {
        if (ueAcc != null) {
            if (CcState.Published.getValue() == ueAcc.getState()) {
                return increaseRevisionNum(ueAcc, releaseId, user);
            } else {
                return ueAcc.getAccId();
            }
        } else {
            return createNewUserExtensionGroupACC(ccListService.getAcc(eAcc.getAccId()), releaseId, user);
        }
    }

    private long increaseRevisionNum(ACC ueAcc, long releaseId, AuthenticatedPrincipal user) {
        ULong userId = ULong.valueOf(sessionService.userId(user));
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        int revisionNum = increaseAccRevisionNum(ueAcc, releaseId, userId, timestamp);
        increaseAsccRevisionNum(ueAcc, revisionNum, releaseId, userId, timestamp);
        increaseBccRevisionNum(ueAcc, revisionNum, releaseId, userId, timestamp);

        return ueAcc.getAccId();
    }

    private int increaseAccRevisionNum(ACC ueAcc, long releaseId,
                                       ULong userId, Timestamp timestamp) {
        AccRecord history = dslContext.selectFrom(Tables.ACC)
                .where(Tables.ACC.CURRENT_ACC_ID.eq(ULong.valueOf(ueAcc.getAccId())))
                .orderBy(Tables.ACC.ACC_ID.desc()).limit(1)
                .fetchOne();

        int newRevisionNum = history.getRevisionNum() + 1;
        history.setAccId(null);
        history.setState(CcState.Editing.getValue());
        history.setRevisionNum(newRevisionNum);
        history.setRevisionTrackingNum(1);
        history.setRevisionAction(Integer.valueOf(RevisionAction.Update.getValue()).byteValue());
        history.setCreatedBy(userId);
        history.setLastUpdatedBy(userId);
        history.setOwnerUserId(userId);
        history.setCreationTimestamp(timestamp);
        history.setLastUpdateTimestamp(timestamp);

        dslContext.insertInto(Tables.ACC).set(history).execute();

        dslContext.update(Tables.ACC)
                .set(Tables.ACC.STATE, history.getState())
                .set(Tables.ACC.OWNER_USER_ID, userId)
                .set(Tables.ACC.LAST_UPDATED_BY, userId)
                .set(Tables.ACC.LAST_UPDATE_TIMESTAMP, timestamp)
                .where(Tables.ACC.ACC_ID.eq(ULong.valueOf(ueAcc.getAccId())))
                .execute();

        return newRevisionNum;
    }

    private void increaseAsccRevisionNum(ACC ueAcc, int revisionNum, long releaseId,
                                         ULong userId, Timestamp timestamp) {
        List<CcAsccNode> asccNodes = dslContext.select(
                ASCC.ASCC_ID,
                ASCC.CURRENT_ASCC_ID,
                ASCC.GUID,
                ASCC.REVISION_NUM,
                ASCC.REVISION_TRACKING_NUM,
                ASCC.RELEASE_ID)
                .from(ASCC)
                .where(and(
                        ASCC.FROM_ACC_ID.eq(ULong.valueOf(ueAcc.getAccId())),
                        ASCC.REVISION_NUM.greaterThan(0)
                ))
                .fetchInto(CcAsccNode.class);

        if (asccNodes.isEmpty()) {
            return;
        }

        // Update a state of the 'current record'.
        dslContext.update(ASCC)
                .set(ASCC.STATE, CcState.Editing.getValue())
                .set(ASCC.LAST_UPDATED_BY, userId)
                .set(ASCC.LAST_UPDATE_TIMESTAMP, timestamp)
                .where(and(
                        ASCC.FROM_ACC_ID.eq(ULong.valueOf(ueAcc.getAccId())),
                        ASCC.REVISION_NUM.eq(0)))
                .execute();

        Result<AsccRecord> asccRecordResult = asccRecordResult(asccNodes, releaseId);

        for (AsccRecord history : asccRecordResult) {
            history.setAsccId(null);
            history.setRevisionNum(revisionNum);
            history.setRevisionTrackingNum(1);
            history.setRevisionAction((byte) RevisionAction.Update.getValue());
            history.setCreatedBy(userId);
            history.setLastUpdatedBy(userId);
            history.setOwnerUserId(userId);
            history.setCreationTimestamp(timestamp);
            history.setLastUpdateTimestamp(timestamp);
            history.setState(CcState.Editing.getValue());

            dslContext.insertInto(ASCC).set(history).execute();
        }
    }

    private Result<AsccRecord> asccRecordResult(List<CcAsccNode> asccNodes, long releaseId) {
        asccNodes = asccNodes.stream()
                .collect(groupingBy(CcAsccNode::getGuid)).values().stream()
                .map(entities -> CcUtility.getLatestEntity(releaseId, entities))
                .collect(Collectors.toList());

        List<ULong> asccIds = asccNodes.stream()
                .map(asccNode -> ULong.valueOf(asccNode.getAsccId()))
                .collect(Collectors.toList());

        return dslContext.selectFrom(ASCC)
                .where(ASCC.ASCC_ID.in(asccIds))
                .fetch();
    }

    private void increaseBccRevisionNum(ACC ueAcc, int revisionNum, long releaseId,
                                        ULong userId, Timestamp timestamp) {
        List<CcBccNode> bccNodes = dslContext.select(
                BCC.BCC_ID,
                BCC.CURRENT_BCC_ID,
                BCC.GUID,
                BCC.REVISION_NUM,
                BCC.REVISION_TRACKING_NUM,
                BCC.RELEASE_ID
        ).from(BCC).where(and(
                BCC.FROM_ACC_ID.eq(ULong.valueOf(ueAcc.getAccId())),
                BCC.REVISION_NUM.greaterThan(0)))
                .fetchInto(CcBccNode.class);

        if (bccNodes.isEmpty()) {
            return;
        }

        // Update a state of the 'current record'.
        dslContext.update(BCC)
                .set(BCC.STATE, CcState.Editing.getValue())
                .set(BCC.LAST_UPDATED_BY, userId)
                .set(BCC.LAST_UPDATE_TIMESTAMP, timestamp)
                .where(and(
                        BCC.FROM_ACC_ID.eq(ULong.valueOf(ueAcc.getAccId())),
                        BCC.REVISION_NUM.eq(0)))
                .execute();

        Result<BccRecord> bccRecordResult = bccRecordResult(bccNodes, releaseId);

        for (BccRecord history : bccRecordResult) {
            history.setBccId(null);
            history.setRevisionNum(revisionNum);
            history.setRevisionTrackingNum(1);
            history.setRevisionAction((byte) RevisionAction.Update.getValue());
            history.setCreatedBy(userId);
            history.setLastUpdatedBy(userId);
            history.setOwnerUserId(userId);
            history.setCreationTimestamp(timestamp);
            history.setLastUpdateTimestamp(timestamp);
            history.setState(CcState.Editing.getValue());

            dslContext.insertInto(BCC).set(history).execute();
        }
    }

    private Result<BccRecord> bccRecordResult(List<CcBccNode> bccNodes, long releaseId) {
        bccNodes = bccNodes.stream()
                .collect(groupingBy(CcBccNode::getGuid)).values().stream()
                .map(entities -> CcUtility.getLatestEntity(releaseId, entities))
                .collect(Collectors.toList());

        List<ULong> bccIds = bccNodes.stream()
                .map(bccNode -> ULong.valueOf(bccNode.getBccId()))
                .collect(Collectors.toList());

        return dslContext.selectFrom(BCC)
                .where(BCC.BCC_ID.in(bccIds))
                .fetch();
    }

    private long createNewUserExtensionGroupACC(ACC eAcc, long releaseId, AuthenticatedPrincipal user) {
        AccRecord ueAcc = createACCForExtension(eAcc, user);
        createACCHistoryForExtension(ueAcc, 1, releaseId);

        AsccpRecord ueAsccp = createASCCPForExtension(eAcc, user, ueAcc);
        createASCCPHistoryForExtension(ueAsccp, 1, releaseId);

        AsccRecord ueAscc = createASCCForExtension(eAcc, user, ueAcc, ueAsccp);
        createASCCHistoryForExtension(ueAscc, 1, releaseId);

        return ueAcc.getAccId().longValue();
    }

    private AccRecord createACCForExtension(ACC eAcc, AuthenticatedPrincipal user) {
        String objectClassTerm = Utility.getUserExtensionGroupObjectClassTerm(eAcc.getObjectClassTerm());
        ULong userId = ULong.valueOf(sessionService.userId(user));
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        return dslContext.insertInto(Tables.ACC,
                Tables.ACC.GUID,
                Tables.ACC.OBJECT_CLASS_TERM,
                Tables.ACC.DEN,
                Tables.ACC.DEFINITION,
                Tables.ACC.OAGIS_COMPONENT_TYPE,
                Tables.ACC.CREATED_BY,
                Tables.ACC.LAST_UPDATED_BY,
                Tables.ACC.OWNER_USER_ID,
                Tables.ACC.CREATION_TIMESTAMP,
                Tables.ACC.LAST_UPDATE_TIMESTAMP,
                Tables.ACC.STATE,
                Tables.ACC.REVISION_NUM,
                Tables.ACC.REVISION_TRACKING_NUM,
                Tables.ACC.REVISION_ACTION).values(
                SrtGuid.randomGuid(),
                objectClassTerm,
                objectClassTerm + ". Details",
                "A system created component containing user extension to the " + eAcc.getObjectClassTerm() + ".",
                OagisComponentType.UserExtensionGroup.getValue(),
                userId,
                userId,
                userId,
                timestamp,
                timestamp,
                CcState.Editing.getValue(),
                0,
                0,
                null
        ).returning().fetchOne();
    }

    private void createACCHistoryForExtension(AccRecord ueAcc, int revisionNum, long releaseId) {
        dslContext.insertInto(Tables.ACC,
                Tables.ACC.GUID,
                Tables.ACC.OBJECT_CLASS_TERM,
                Tables.ACC.DEN,
                Tables.ACC.DEFINITION,
                Tables.ACC.OAGIS_COMPONENT_TYPE,
                Tables.ACC.CREATED_BY,
                Tables.ACC.LAST_UPDATED_BY,
                Tables.ACC.OWNER_USER_ID,
                Tables.ACC.CREATION_TIMESTAMP,
                Tables.ACC.LAST_UPDATE_TIMESTAMP,
                Tables.ACC.STATE,
                Tables.ACC.REVISION_NUM,
                Tables.ACC.REVISION_TRACKING_NUM,
                Tables.ACC.REVISION_ACTION,
                Tables.ACC.RELEASE_ID,
                Tables.ACC.CURRENT_ACC_ID).values(
                ueAcc.getGuid(),
                ueAcc.getObjectClassTerm(),
                ueAcc.getDen(),
                ueAcc.getDefinition(),
                ueAcc.getOagisComponentType(),
                ueAcc.getCreatedBy(),
                ueAcc.getLastUpdatedBy(),
                ueAcc.getOwnerUserId(),
                ueAcc.getCreationTimestamp(),
                ueAcc.getLastUpdateTimestamp(),
                ueAcc.getState(),
                revisionNum,
                1,
                Integer.valueOf(RevisionAction.Insert.getValue()).byteValue(),
                ULong.valueOf(releaseId),
                ueAcc.getAccId()
        ).execute();
    }

    private AsccpRecord createASCCPForExtension(ACC eAcc, AuthenticatedPrincipal user, AccRecord ueAcc) {
        ULong userId = ULong.valueOf(sessionService.userId(user));
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        return dslContext.insertInto(Tables.ASCCP,
                Tables.ASCCP.GUID,
                Tables.ASCCP.PROPERTY_TERM,
                Tables.ASCCP.ROLE_OF_ACC_ID,
                Tables.ASCCP.DEN,
                Tables.ASCCP.DEFINITION,
                Tables.ASCCP.REUSABLE_INDICATOR,
                Tables.ASCCP.IS_DEPRECATED,
                Tables.ASCCP.IS_NILLABLE,
                Tables.ASCCP.CREATED_BY,
                Tables.ASCCP.LAST_UPDATED_BY,
                Tables.ASCCP.OWNER_USER_ID,
                Tables.ASCCP.CREATION_TIMESTAMP,
                Tables.ASCCP.LAST_UPDATE_TIMESTAMP,
                Tables.ASCCP.STATE,
                Tables.ASCCP.REVISION_NUM,
                Tables.ASCCP.REVISION_TRACKING_NUM,
                Tables.ASCCP.REVISION_ACTION).values(
                SrtGuid.randomGuid(),
                ueAcc.getObjectClassTerm(),
                ueAcc.getAccId(),
                ueAcc.getObjectClassTerm() + ". " + ueAcc.getObjectClassTerm(),
                "A system created component containing user extension to the " + eAcc.getObjectClassTerm() + ".",
                Byte.valueOf((byte) 0),
                Byte.valueOf((byte) 0),
                Byte.valueOf((byte) 0),
                userId,
                userId,
                userId,
                timestamp,
                timestamp,
                CcState.Published.getValue(),
                0,
                0,
                null
        ).returning().fetchOne();
    }

    private void createASCCPHistoryForExtension(AsccpRecord ueAsccp, int revisionNum, long releaseId) {
        dslContext.insertInto(Tables.ASCCP,
                Tables.ASCCP.GUID,
                Tables.ASCCP.PROPERTY_TERM,
                Tables.ASCCP.ROLE_OF_ACC_ID,
                Tables.ASCCP.DEN,
                Tables.ASCCP.DEFINITION,
                Tables.ASCCP.REUSABLE_INDICATOR,
                Tables.ASCCP.IS_DEPRECATED,
                Tables.ASCCP.IS_NILLABLE,
                Tables.ASCCP.CREATED_BY,
                Tables.ASCCP.LAST_UPDATED_BY,
                Tables.ASCCP.OWNER_USER_ID,
                Tables.ASCCP.CREATION_TIMESTAMP,
                Tables.ASCCP.LAST_UPDATE_TIMESTAMP,
                Tables.ASCCP.STATE,
                Tables.ASCCP.REVISION_NUM,
                Tables.ASCCP.REVISION_TRACKING_NUM,
                Tables.ASCCP.REVISION_ACTION,
                Tables.ASCCP.RELEASE_ID,
                Tables.ASCCP.CURRENT_ASCCP_ID).values(
                ueAsccp.getGuid(),
                ueAsccp.getPropertyTerm(),
                ueAsccp.getRoleOfAccId(),
                ueAsccp.getDen(),
                ueAsccp.getDefinition(),
                ueAsccp.getReusableIndicator(),
                ueAsccp.getIsDeprecated(),
                ueAsccp.getIsNillable(),
                ueAsccp.getCreatedBy(),
                ueAsccp.getLastUpdatedBy(),
                ueAsccp.getOwnerUserId(),
                ueAsccp.getCreationTimestamp(),
                ueAsccp.getLastUpdateTimestamp(),
                ueAsccp.getState(),
                revisionNum,
                1,
                Integer.valueOf(RevisionAction.Insert.getValue()).byteValue(),
                ULong.valueOf(releaseId),
                ueAsccp.getAsccpId()
        ).execute();
    }

    private AsccRecord createASCCForExtension(ACC eAcc, AuthenticatedPrincipal user, AccRecord ueAcc, AsccpRecord ueAsccp) {
        ULong userId = ULong.valueOf(sessionService.userId(user));
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        return dslContext.insertInto(Tables.ASCC,
                Tables.ASCC.GUID,
                Tables.ASCC.CARDINALITY_MIN,
                Tables.ASCC.CARDINALITY_MAX,
                Tables.ASCC.SEQ_KEY,
                Tables.ASCC.FROM_ACC_ID,
                Tables.ASCC.TO_ASCCP_ID,
                Tables.ASCC.DEN,
                Tables.ASCC.IS_DEPRECATED,
                Tables.ASCC.CREATED_BY,
                Tables.ASCC.LAST_UPDATED_BY,
                Tables.ASCC.OWNER_USER_ID,
                Tables.ASCC.CREATION_TIMESTAMP,
                Tables.ASCC.LAST_UPDATE_TIMESTAMP,
                Tables.ASCC.STATE,
                Tables.ASCC.REVISION_NUM,
                Tables.ASCC.REVISION_TRACKING_NUM,
                Tables.ASCC.REVISION_ACTION).values(
                SrtGuid.randomGuid(),
                0,
                1,
                1,
                ULong.valueOf(eAcc.getCurrentAccId()),
                ueAsccp.getAsccpId(),
                eAcc.getObjectClassTerm() + ". " + ueAsccp.getDen(),
                Byte.valueOf((byte) 0),
                userId,
                userId,
                userId,
                timestamp,
                timestamp,
                CcState.Published.getValue(),
                0,
                0,
                null
        ).returning().fetchOne();
    }

    private void createASCCHistoryForExtension(AsccRecord ueAscc, int revisionNum, long releaseId) {
        dslContext.insertInto(Tables.ASCC,
                Tables.ASCC.GUID,
                Tables.ASCC.CARDINALITY_MIN,
                Tables.ASCC.CARDINALITY_MAX,
                Tables.ASCC.SEQ_KEY,
                Tables.ASCC.FROM_ACC_ID,
                Tables.ASCC.TO_ASCCP_ID,
                Tables.ASCC.DEN,
                Tables.ASCC.IS_DEPRECATED,
                Tables.ASCC.CREATED_BY,
                Tables.ASCC.LAST_UPDATED_BY,
                Tables.ASCC.OWNER_USER_ID,
                Tables.ASCC.CREATION_TIMESTAMP,
                Tables.ASCC.LAST_UPDATE_TIMESTAMP,
                Tables.ASCC.STATE,
                Tables.ASCC.REVISION_NUM,
                Tables.ASCC.REVISION_TRACKING_NUM,
                Tables.ASCC.REVISION_ACTION,
                Tables.ASCC.RELEASE_ID,
                Tables.ASCC.CURRENT_ASCC_ID).values(
                ueAscc.getGuid(),
                ueAscc.getCardinalityMin(),
                ueAscc.getCardinalityMax(),
                ueAscc.getSeqKey(),
                ueAscc.getFromAccId(),
                ueAscc.getToAsccpId(),
                ueAscc.getDen(),
                ueAscc.getIsDeprecated(),
                ueAscc.getCreatedBy(),
                ueAscc.getLastUpdatedBy(),
                ueAscc.getOwnerUserId(),
                ueAscc.getCreationTimestamp(),
                ueAscc.getLastUpdateTimestamp(),
                ueAscc.getState(),
                revisionNum,
                1,
                Integer.valueOf(RevisionAction.Insert.getValue()).byteValue(),
                ULong.valueOf(releaseId),
                ueAscc.getAsccId()
        ).execute();
    }

    public boolean isExistChildren(long extensionId, Long releaseId, Long asccpId, Long bccpId) {
        if (asccpId != null) {
            boolean exists = dslContext.selectCount()
                    .from(ASCC).where(and(
                            ASCC.FROM_ACC_ID.eq(ULong.valueOf(extensionId)),
                            ASCC.TO_ASCCP_ID.eq(ULong.valueOf(asccpId))
                    ))
                    .fetchOptionalInto(Integer.class).orElse(0) > 0;
            if (exists) {
                return true;
            }
        } else if (bccpId != null) {
            boolean exists = dslContext.selectCount()
                    .from(BCC).where(and(
                            BCC.FROM_ACC_ID.eq(ULong.valueOf(extensionId)),
                            BCC.TO_BCCP_ID.eq(ULong.valueOf(bccpId))
                    ))
                    .fetchOptionalInto(Integer.class).orElse(0) > 0;
            if (exists) {
                return true;
            }
        }

        Long basedAccId = dslContext.select(ACC.as("extension_acc").BASED_ACC_ID)
                .from(ASCCP)
                .join(ASCC).on(ASCC.TO_ASCCP_ID.eq(ASCCP.ASCCP_ID))
                .join(ACC.as("base_acc")).on(ACC.as("base_acc").ACC_ID.eq(ASCC.FROM_ACC_ID))
                .join(ACC.as("extension_acc")).on(ACC.as("extension_acc").ACC_ID.eq(ACC.as("base_acc").BASED_ACC_ID))
                .where(and(
                        ASCCP.ROLE_OF_ACC_ID.eq(ULong.valueOf(extensionId)),
                        ASCC.RELEASE_ID.eq(ULong.valueOf(releaseId))
                ))
                .fetchOptionalInto(Long.class).orElse(null);

        if (basedAccId != null) {
            boolean baseExist = false;
            if (asccpId != null) {
                baseExist = dslContext.selectCount()
                        .from(ASCC)
                        .where(and(
                                ASCC.FROM_ACC_ID.eq(ULong.valueOf(basedAccId)),
                                ASCC.TO_ASCCP_ID.eq(ULong.valueOf(asccpId))
                        ))
                        .fetchOptionalInto(Integer.class).orElse(0) > 0;
            } else if (bccpId != null) {
                baseExist = dslContext.selectCount()
                        .from(BCC)
                        .where(and(
                                BCC.FROM_ACC_ID.eq(ULong.valueOf(basedAccId)),
                                BCC.TO_BCCP_ID.eq(ULong.valueOf(bccpId))
                        ))
                        .fetchOptionalInto(Integer.class).orElse(0) > 0;
            }
            if (baseExist) {
                return true;
            }
        }

        return false;
    }


    @Transactional
    public void appendAsccp(AuthenticatedPrincipal user, long extensionId, Long releaseId, long asccpId) {
        int nextSeqKey = getNextSeqKey(extensionId);

        asccpId = dslContext.select(ASCCP.CURRENT_ASCCP_ID)
                .from(ASCCP).where(ASCCP.ASCCP_ID.eq(ULong.valueOf(asccpId)))
                .fetchOneInto(Long.class);

        /*
         * Issue #710
         * Duplicated associations cannot be existed.
         */
        boolean exists = isExistChildren(extensionId, releaseId, asccpId, null);
        if (exists) {
            throw new IllegalArgumentException("You cannot associate the same component.");
        }

        AsccRecord ascc = createASCC(user, extensionId, asccpId, nextSeqKey);

        int revisionNum = dslContext.select(ACC.REVISION_NUM)
                .from(ACC).where(ACC.CURRENT_ACC_ID.eq(ULong.valueOf(extensionId)))
                .orderBy(ACC.ACC_ID.desc()).limit(1)
                .fetchOneInto(Integer.class);

        createASCCHistory(ascc, revisionNum, releaseId);
    }

    private AsccRecord createASCC(AuthenticatedPrincipal user, long accId, long asccpId, int seqKey) {
        String accObjectClassTerm = dslContext.select(ACC.OBJECT_CLASS_TERM)
                .from(ACC).where(ACC.ACC_ID.eq(ULong.valueOf(accId)))
                .fetchOneInto(String.class);
        String asccpDen = dslContext.select(ASCCP.DEN)
                .from(ASCCP).where(ASCCP.ASCCP_ID.eq(ULong.valueOf(asccpId)))
                .fetchOneInto(String.class);

        ULong userId = ULong.valueOf(sessionService.userId(user));
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        return dslContext.insertInto(Tables.ASCC,
                Tables.ASCC.GUID,
                Tables.ASCC.CARDINALITY_MIN,
                Tables.ASCC.CARDINALITY_MAX,
                Tables.ASCC.SEQ_KEY,
                Tables.ASCC.FROM_ACC_ID,
                Tables.ASCC.TO_ASCCP_ID,
                Tables.ASCC.DEN,
                Tables.ASCC.IS_DEPRECATED,
                Tables.ASCC.CREATED_BY,
                Tables.ASCC.LAST_UPDATED_BY,
                Tables.ASCC.OWNER_USER_ID,
                Tables.ASCC.CREATION_TIMESTAMP,
                Tables.ASCC.LAST_UPDATE_TIMESTAMP,
                Tables.ASCC.STATE,
                Tables.ASCC.REVISION_NUM,
                Tables.ASCC.REVISION_TRACKING_NUM,
                Tables.ASCC.REVISION_ACTION).values(
                SrtGuid.randomGuid(),
                0,
                -1,
                seqKey,
                ULong.valueOf(accId),
                ULong.valueOf(asccpId),
                accObjectClassTerm + ". " + asccpDen,
                Byte.valueOf((byte) 0),
                userId,
                userId,
                userId,
                timestamp,
                timestamp,
                CcState.Editing.getValue(),
                0,
                0,
                null
        ).returning().fetchOne();
    }

    private void createASCCHistory(AsccRecord ascc, int revisionNum, long releaseId) {
        dslContext.insertInto(Tables.ASCC,
                Tables.ASCC.GUID,
                Tables.ASCC.CARDINALITY_MIN,
                Tables.ASCC.CARDINALITY_MAX,
                Tables.ASCC.SEQ_KEY,
                Tables.ASCC.FROM_ACC_ID,
                Tables.ASCC.TO_ASCCP_ID,
                Tables.ASCC.DEN,
                Tables.ASCC.IS_DEPRECATED,
                Tables.ASCC.CREATED_BY,
                Tables.ASCC.LAST_UPDATED_BY,
                Tables.ASCC.OWNER_USER_ID,
                Tables.ASCC.CREATION_TIMESTAMP,
                Tables.ASCC.LAST_UPDATE_TIMESTAMP,
                Tables.ASCC.RELEASE_ID,
                Tables.ASCC.STATE,
                Tables.ASCC.REVISION_NUM,
                Tables.ASCC.REVISION_TRACKING_NUM,
                Tables.ASCC.REVISION_ACTION,
                Tables.ASCC.CURRENT_ASCC_ID).values(
                ascc.getGuid(),
                ascc.getCardinalityMin(),
                ascc.getCardinalityMax(),
                ascc.getSeqKey(),
                ascc.getFromAccId(),
                ascc.getToAsccpId(),
                ascc.getDen(),
                ascc.getIsDeprecated(),
                ascc.getCreatedBy(),
                ascc.getLastUpdatedBy(),
                ascc.getOwnerUserId(),
                ascc.getCreationTimestamp(),
                ascc.getLastUpdateTimestamp(),
                ULong.valueOf(releaseId),
                ascc.getState(),
                1,
                1,
                Integer.valueOf(RevisionAction.Insert.getValue()).byteValue(),
                ascc.getAsccId()
        ).execute();
    }

    @Transactional
    public void appendBccp(AuthenticatedPrincipal user, long extensionId, Long releaseId, long bccpId) {
        int nextSeqKey = getNextSeqKey(extensionId);

        bccpId = dslContext.select(BCCP.CURRENT_BCCP_ID)
                .from(BCCP).where(BCCP.BCCP_ID.eq(ULong.valueOf(bccpId)))
                .fetchOneInto(Long.class);

        /*
         * Issue #710
         * Duplicated associations cannot be existed.
         */
        boolean exists = isExistChildren(extensionId, releaseId, null, bccpId);
        if (exists) {
            throw new IllegalArgumentException("You cannot associate the same component.");
        }

        BccRecord bcc = createBCC(user, extensionId, bccpId, nextSeqKey);

        int revisionNum = dslContext.select(ACC.REVISION_NUM)
                .from(ACC).where(ACC.CURRENT_ACC_ID.eq(ULong.valueOf(extensionId)))
                .orderBy(ACC.ACC_ID.desc()).limit(1)
                .fetchOneInto(Integer.class);

        createBCCHistory(bcc, revisionNum, releaseId);
    }

    private BccRecord createBCC(AuthenticatedPrincipal user, long accId, long bccpId, int seqKey) {
        String accObjectClassTerm = dslContext.select(ACC.OBJECT_CLASS_TERM)
                .from(ACC).where(ACC.ACC_ID.eq(ULong.valueOf(accId)))
                .fetchOneInto(String.class);
        String bccpDen = dslContext.select(BCCP.DEN)
                .from(BCCP).where(BCCP.BCCP_ID.eq(ULong.valueOf(bccpId)))
                .fetchOneInto(String.class);

        ULong userId = ULong.valueOf(sessionService.userId(user));
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        return dslContext.insertInto(Tables.BCC,
                Tables.BCC.GUID,
                Tables.BCC.CARDINALITY_MIN,
                Tables.BCC.CARDINALITY_MAX,
                Tables.BCC.SEQ_KEY,
                Tables.BCC.ENTITY_TYPE,
                Tables.BCC.FROM_ACC_ID,
                Tables.BCC.TO_BCCP_ID,
                Tables.BCC.DEN,
                Tables.BCC.IS_DEPRECATED,
                Tables.BCC.CREATED_BY,
                Tables.BCC.LAST_UPDATED_BY,
                Tables.BCC.OWNER_USER_ID,
                Tables.BCC.CREATION_TIMESTAMP,
                Tables.BCC.LAST_UPDATE_TIMESTAMP,
                Tables.BCC.STATE,
                Tables.BCC.REVISION_NUM,
                Tables.BCC.REVISION_TRACKING_NUM,
                Tables.BCC.REVISION_ACTION).values(
                SrtGuid.randomGuid(),
                0,
                -1,
                seqKey,
                BCCEntityType.Element.getValue(),
                ULong.valueOf(accId),
                ULong.valueOf(bccpId),
                accObjectClassTerm + ". " + bccpDen,
                Byte.valueOf((byte) 0),
                userId,
                userId,
                userId,
                timestamp,
                timestamp,
                CcState.Editing.getValue(),
                0,
                0,
                null
        ).returning().fetchOne();
    }

    private void createBCCHistory(BccRecord bcc, int revisionNum, long releaseId) {
        dslContext.insertInto(Tables.BCC,
                Tables.BCC.GUID,
                Tables.BCC.CARDINALITY_MIN,
                Tables.BCC.CARDINALITY_MAX,
                Tables.BCC.SEQ_KEY,
                Tables.BCC.ENTITY_TYPE,
                Tables.BCC.FROM_ACC_ID,
                Tables.BCC.TO_BCCP_ID,
                Tables.BCC.DEN,
                Tables.BCC.IS_DEPRECATED,
                Tables.BCC.CREATED_BY,
                Tables.BCC.LAST_UPDATED_BY,
                Tables.BCC.OWNER_USER_ID,
                Tables.BCC.CREATION_TIMESTAMP,
                Tables.BCC.LAST_UPDATE_TIMESTAMP,
                Tables.BCC.RELEASE_ID,
                Tables.BCC.STATE,
                Tables.BCC.REVISION_NUM,
                Tables.BCC.REVISION_TRACKING_NUM,
                Tables.BCC.REVISION_ACTION,
                Tables.BCC.CURRENT_BCC_ID).values(
                bcc.getGuid(),
                bcc.getCardinalityMin(),
                bcc.getCardinalityMax(),
                bcc.getSeqKey(),
                bcc.getEntityType(),
                bcc.getFromAccId(),
                bcc.getToBccpId(),
                bcc.getDen(),
                bcc.getIsDeprecated(),
                bcc.getCreatedBy(),
                bcc.getLastUpdatedBy(),
                bcc.getOwnerUserId(),
                bcc.getCreationTimestamp(),
                bcc.getLastUpdateTimestamp(),
                ULong.valueOf(releaseId),
                bcc.getState(),
                1,
                1,
                Integer.valueOf(RevisionAction.Insert.getValue()).byteValue(),
                bcc.getBccId()
        ).execute();
    }

    private int getNextSeqKey(long accId) {
        Integer asccMaxSeqKey = dslContext.select(max(ASCC.SEQ_KEY))
                .from(ASCC)
                .where(ASCC.FROM_ACC_ID.eq(ULong.valueOf(accId)))
                .fetchOneInto(Integer.class);
        if (asccMaxSeqKey == null) {
            asccMaxSeqKey = 0;
        }

        Integer bccMaxSeqKey = dslContext.select(max(BCC.SEQ_KEY))
                .from(BCC)
                .where(BCC.FROM_ACC_ID.eq(ULong.valueOf(accId)))
                .fetchOneInto(Integer.class);
        if (bccMaxSeqKey == null) {
            bccMaxSeqKey = 0;
        }

        return Math.max(asccMaxSeqKey, bccMaxSeqKey) + 1;
    }

    @Transactional
    public void discardAscc(AuthenticatedPrincipal user, long extensionId, Long releaseId, long asccId) {
        AsccRecord asccRecord = dslContext.selectFrom(Tables.ASCC)
                .where(ASCC.ASCC_ID.eq(ULong.valueOf(asccId))).fetchOne();
        dslContext.deleteFrom(Tables.ASCC)
                .where(ASCC.CURRENT_ASCC_ID.eq(asccRecord.getCurrentAsccId()))
                .execute();
        dslContext.deleteFrom(Tables.ASCC)
                .where(ASCC.ASCC_ID.eq(asccRecord.getCurrentAsccId()))
                .execute();
        decreaseSeqKeyGreaterThan(extensionId, asccRecord.getSeqKey());
    }

    @Transactional
    public void discardBcc(AuthenticatedPrincipal user, long extensionId, Long releaseId, long bccId) {
        BccRecord bccRecord = dslContext.selectFrom(Tables.BCC)
                .where(BCC.BCC_ID.eq(ULong.valueOf(bccId))).fetchOne();
        dslContext.deleteFrom(Tables.BCC)
                .where(BCC.CURRENT_BCC_ID.eq(bccRecord.getCurrentBccId()))
                .execute();
        dslContext.deleteFrom(Tables.BCC)
                .where(BCC.BCC_ID.eq(bccRecord.getCurrentBccId()))
                .execute();
        decreaseSeqKeyGreaterThan(extensionId, bccRecord.getSeqKey());
    }

    private void decreaseSeqKeyGreaterThan(long extensionId, int seqKey) {
        dslContext.update(Tables.ASCC)
                .set(ASCC.SEQ_KEY, ASCC.SEQ_KEY.subtract(1))
                .where(and(
                        ASCC.FROM_ACC_ID.eq(ULong.valueOf(extensionId)),
                        ASCC.SEQ_KEY.greaterThan(seqKey)
                ))
                .execute();

        dslContext.update(Tables.BCC)
                .set(BCC.SEQ_KEY, BCC.SEQ_KEY.subtract(1))
                .where(and(
                        BCC.FROM_ACC_ID.eq(ULong.valueOf(extensionId)),
                        BCC.SEQ_KEY.greaterThan(seqKey)
                ))
                .execute();
    }

    @Transactional
    public void updateState(AuthenticatedPrincipal user, long extensionId, Long releaseId, CcState state) {
        ULong userId = ULong.valueOf(sessionService.userId(user));
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        updateAccState(extensionId, releaseId, state, userId, timestamp);
        updateAsccState(extensionId, releaseId, state, userId, timestamp);
        updateBccState(extensionId, releaseId, state, userId, timestamp);

        /*
         * Issue #823
         *
         * After publishing the extension, ASBIEs and BBIEs whose based_ascc_id and based_bcc_id
         * have linked to previous revisions must update to link the latest records.
         */
        if (state == CcState.Published) {
            updateASBIELinks(extensionId, releaseId);
            updateBBIELinks(extensionId, releaseId);
        }
    }

    private void updateAccState(long extensionId, Long releaseId, CcState state,
                                ULong userId, Timestamp timestamp) {
        dslContext.update(Tables.ACC)
                .set(ACC.STATE, state.getValue())
                .set(ACC.LAST_UPDATED_BY, userId)
                .set(ACC.LAST_UPDATE_TIMESTAMP, timestamp)
                .where(ACC.ACC_ID.eq(ULong.valueOf(extensionId)))
                .execute();

        AccRecord history = dslContext.selectFrom(Tables.ACC)
                .where(ACC.CURRENT_ACC_ID.eq(ULong.valueOf(extensionId)))
                .orderBy(ACC.ACC_ID.desc()).limit(1)
                .fetchOne();

        history.setAccId(null);
        history.setRevisionTrackingNum(history.getRevisionTrackingNum() + 1);
        history.setRevisionAction((byte) RevisionAction.Update.getValue());
        history.setCreatedBy(userId);
        history.setLastUpdatedBy(userId);
        history.setCreationTimestamp(timestamp);
        history.setLastUpdateTimestamp(timestamp);
        history.setState(state.getValue());
        dslContext.insertInto(Tables.ACC).set(history).execute();
    }

    private void updateAsccState(long extensionId, Long releaseId, CcState state,
                                 ULong userId, Timestamp timestamp) {
        List<CcAsccNode> asccNodes = dslContext.select(
                ASCC.ASCC_ID,
                ASCC.CURRENT_ASCC_ID,
                ASCC.GUID,
                ASCC.REVISION_NUM,
                ASCC.REVISION_TRACKING_NUM,
                ASCC.RELEASE_ID)
                .from(ASCC)
                .where(and(
                        ASCC.FROM_ACC_ID.eq(ULong.valueOf(extensionId)),
                        ASCC.REVISION_NUM.greaterThan(0)
                ))
                .fetchInto(CcAsccNode.class);

        if (asccNodes.isEmpty()) {
            return;
        }

        // Update a state of the 'current record'.
        dslContext.update(ASCC)
                .set(ASCC.STATE, state.getValue())
                .set(ASCC.LAST_UPDATED_BY, userId)
                .set(ASCC.LAST_UPDATE_TIMESTAMP, timestamp)
                .where(and(
                        ASCC.FROM_ACC_ID.eq(ULong.valueOf(extensionId)),
                        ASCC.REVISION_NUM.eq(0)))
                .execute();

        Result<AsccRecord> asccRecordResult = asccRecordResult(asccNodes, releaseId);

        for (AsccRecord history : asccRecordResult) {
            history.setAsccId(null);
            history.setRevisionTrackingNum(history.getRevisionTrackingNum() + 1);
            history.setRevisionAction((byte) RevisionAction.Update.getValue());
            history.setCreatedBy(userId);
            history.setLastUpdatedBy(userId);
            history.setCreationTimestamp(timestamp);
            history.setLastUpdateTimestamp(timestamp);
            history.setState(state.getValue());

            dslContext.insertInto(ASCC).set(history).execute();
        }
    }

    private void updateBccState(long extensionId, Long releaseId, CcState state,
                                ULong userId, Timestamp timestamp) {
        List<CcBccNode> bccNodes = dslContext.select(
                BCC.BCC_ID,
                BCC.CURRENT_BCC_ID,
                BCC.GUID,
                BCC.REVISION_NUM,
                BCC.REVISION_TRACKING_NUM,
                BCC.RELEASE_ID)
                .from(BCC)
                .where(and(
                        BCC.FROM_ACC_ID.eq(ULong.valueOf(extensionId)),
                        BCC.REVISION_NUM.greaterThan(0)
                )).fetchInto(CcBccNode.class);

        if (bccNodes.isEmpty()) {
            return;
        }

        // Update a state of the 'current record'.
        dslContext.update(BCC)
                .set(BCC.STATE, state.getValue())
                .set(BCC.LAST_UPDATED_BY, userId)
                .set(BCC.LAST_UPDATE_TIMESTAMP, timestamp)
                .where(and(
                        BCC.FROM_ACC_ID.eq(ULong.valueOf(extensionId)),
                        BCC.REVISION_NUM.eq(0)))
                .execute();

        Result<BccRecord> bccRecordResult = bccRecordResult(bccNodes, releaseId);

        for (BccRecord history : bccRecordResult) {
            history.setBccId(null);
            history.setRevisionTrackingNum(history.getRevisionTrackingNum() + 1);
            history.setRevisionAction((byte) RevisionAction.Update.getValue());
            history.setCreatedBy(userId);
            history.setLastUpdatedBy(userId);
            history.setCreationTimestamp(timestamp);
            history.setLastUpdateTimestamp(timestamp);
            history.setState(state.getValue());

            dslContext.insertInto(BCC).set(history).execute();
        }
    }

    private void updateASBIELinks(long extensionId, Long releaseId) {
        Map<String, List<Record5<String, ULong, Integer, Integer, Integer>>> asccRecordMap =
                dslContext.select(ASCC.GUID, ASCC.ASCC_ID, ASCC.STATE, ASCC.REVISION_NUM, ASCC.REVISION_TRACKING_NUM)
                        .from(ASCC)
                        .join(ACC).on(ASCC.FROM_ACC_ID.eq(ACC.ACC_ID))
                        .where(and(
                                ASCC.FROM_ACC_ID.eq(ULong.valueOf(extensionId)),
                                ASCC.REVISION_NUM.ne(0),
                                ASCC.STATE.eq(CcState.Published.getValue())
                        ))
                        .fetchStream().collect(groupingBy(e -> e.get(ASCC.GUID)));

        for (List<Record5<String, ULong, Integer, Integer, Integer>> asccRecords : asccRecordMap.values()) {
            if (asccRecords.isEmpty()) {
                continue;
            }

            Collections.sort(asccRecords, (o1, o2) -> Integer.compare(o2.get(ASCC.REVISION_NUM), o1.get(ASCC.REVISION_NUM)));

            Record5<String, ULong, Integer, Integer, Integer> latestRecord = asccRecords.get(0);
            int revisionNum = latestRecord.get(ASCC.REVISION_NUM);
            if (revisionNum == 1) {
                continue;
            }

            dslContext.update(ASBIE)
                    .set(ASBIE.BASED_ASCC_ID, latestRecord.get(ASCC.ASCC_ID))
                    .where(ASBIE.BASED_ASCC_ID.in(
                            asccRecords.stream()
                                    .map(e -> e.get(ASCC.ASCC_ID))
                                    .collect(Collectors.toList())
                    ))
                    .execute();
        }
    }

    private void updateBBIELinks(long extensionId, Long releaseId) {
        Map<String, List<Record5<String, ULong, Integer, Integer, Integer>>> bccRecordMap =
                dslContext.select(BCC.GUID, BCC.BCC_ID, BCC.STATE, BCC.REVISION_NUM, BCC.REVISION_TRACKING_NUM)
                .from(BCC)
                .join(ACC).on(BCC.FROM_ACC_ID.eq(ACC.ACC_ID))
                .where(and(
                        BCC.FROM_ACC_ID.eq(ULong.valueOf(extensionId)),
                        BCC.REVISION_NUM.ne(0),
                        BCC.STATE.eq(CcState.Published.getValue())
                ))
                .fetchStream().collect(groupingBy(e -> e.get(BCC.GUID)));

        for (List<Record5<String, ULong, Integer, Integer, Integer>> bccRecords : bccRecordMap.values()) {
            if (bccRecords.isEmpty()) {
                continue;
            }

            Collections.sort(bccRecords, (o1, o2) -> Integer.compare(o2.get(BCC.REVISION_NUM), o1.get(BCC.REVISION_NUM)));

            Record5<String, ULong, Integer, Integer, Integer> latestRecord = bccRecords.get(0);
            int revisionNum = latestRecord.get(BCC.REVISION_NUM);
            if (revisionNum == 1) {
                continue;
            }

            dslContext.update(BBIE)
                    .set(BBIE.BASED_BCC_ID, latestRecord.get(BCC.BCC_ID))
                    .where(BBIE.BASED_BCC_ID.in(
                            bccRecords.stream()
                                    .map(e -> e.get(BCC.BCC_ID))
                                    .collect(Collectors.toList())
                    ))
                    .execute();
        }
    }

    private void storeBieUserExtRevisions(long extensionId, Long releaseId) {
        List<TopLevelAsbiep> topLevelAsbieps = dslContext.selectFrom(TOP_LEVEL_ASBIEP)
                .where(TOP_LEVEL_ASBIEP.STATE.ne(BieState.Published.getValue()))
                .fetchInto(TopLevelAsbiep.class);

        ExtensionPathHandler extensionPathHandler =
                applicationContext.getBean(ExtensionPathHandler.class, releaseId);

        for (TopLevelAsbiep topLevelAsbiep : topLevelAsbieps) {
            Long asbiepId = topLevelAsbiep.getAsbiepId();
            if (asbiepId == null) {
                continue;
            }

            long basedAccId = dslContext.select(Tables.ACC.CURRENT_ACC_ID)
                    .from(ABIE)
                    .join(ASBIEP).on(and(
                            ABIE.ABIE_ID.eq(ASBIEP.ROLE_OF_ABIE_ID),
                            ABIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID)
                    ))
                    .join(Tables.ACC).on(Tables.ABIE.BASED_ACC_ID.eq(Tables.ACC.ACC_ID))
                    .where(ASBIEP.ASBIEP_ID.eq(ULong.valueOf(asbiepId)))
                    .fetchOneInto(Long.class);

            ULong eAccId =
                    dslContext.select(Tables.ACC.as("eAcc").ACC_ID)
                            .from(Tables.ACC.as("eAcc"))
                            .join(Tables.ASCC).on(Tables.ACC.as("eAcc").ACC_ID.eq(ASCC.FROM_ACC_ID))
                            .join(Tables.ASCCP).on(ASCC.TO_ASCCP_ID.eq(ASCCP.ASCCP_ID))
                            .join(Tables.ACC.as("ueAcc")).on(ASCCP.ROLE_OF_ACC_ID.eq(Tables.ACC.as("ueAcc").ACC_ID))
                            .where(and(
                                    ACC.as("ueAcc").ACC_ID.eq(ULong.valueOf(extensionId)),
                                    ASCC.REVISION_NUM.eq(0)
                            )).fetchOneInto(ULong.class);
        }
    }

    @Transactional
    public ExtensionUpdateResponse updateDetails(AuthenticatedPrincipal user, ExtensionUpdateRequest request) {
        ExtensionUpdateResponse response = new ExtensionUpdateResponse();

        ULong extensionId = ULong.valueOf(request.getExtensionId());
        ULong releaseId = ULong.valueOf(request.getReleaseId());
        ULong userId = ULong.valueOf(sessionService.userId(user));
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        List<CcAsccpNodeDetail.Ascc> asccList = request.getAsccpDetails().stream().map(asccpDetail -> asccpDetail.getAscc())
                .collect(Collectors.toList());

        for (CcAsccpNodeDetail.Ascc ascc : asccList) {
            response.getAsccResults().put(ascc.getAsccId(),
                    updateAscc(extensionId, releaseId, ascc, userId, timestamp)
            );
        }

        List<CcBccpNodeDetail.Bcc> bccList = request.getBccpDetails().stream().map(bccpDetail -> bccpDetail.getBcc())
                .collect(Collectors.toList());
        for (CcBccpNodeDetail.Bcc bcc : bccList) {
            response.getBccResults().put(bcc.getBccId(),
                    updateBcc(extensionId, releaseId, bcc, userId, timestamp)
            );
        }

        return response;
    }

    private boolean updateAscc(ULong extensionId, ULong releaseId,
                               CcAsccpNodeDetail.Ascc ascc,
                               ULong userId, Timestamp timestamp) {

        ULong currentAsccId = dslContext.select(ASCC.CURRENT_ASCC_ID)
                .from(ASCC)
                .where(ASCC.ASCC_ID.eq(ULong.valueOf(ascc.getAsccId())))
                .fetchOneInto(ULong.class);

        AsccRecord currentRecord;
        if (currentAsccId == null) { // current record
            currentRecord = dslContext.selectFrom(ASCC)
                    .where(ASCC.ASCC_ID.eq(ULong.valueOf(ascc.getAsccId())))
                    .fetchOne();
        } else { // history record
            currentRecord = dslContext.selectFrom(ASCC)
                    .where(ASCC.ASCC_ID.eq(currentAsccId))
                    .fetchOne();
        }

        currentRecord.setCardinalityMin(ascc.getCardinalityMin());
        currentRecord.setCardinalityMax(ascc.getCardinalityMax());
        currentRecord.setIsDeprecated((byte) ((ascc.isDeprecated()) ? 1 : 0));
        currentRecord.setDefinition(ascc.getDefinition());
        currentRecord.setLastUpdatedBy(userId);
        currentRecord.setLastUpdateTimestamp(timestamp);
        int result = currentRecord.update(
                ASCC.CARDINALITY_MIN, ASCC.CARDINALITY_MAX,
                ASCC.IS_DEPRECATED, ASCC.DEFINITION,
                ASCC.LAST_UPDATED_BY, ASCC.LAST_UPDATE_TIMESTAMP
        );

        AsccRecord history = dslContext.selectFrom(Tables.ASCC)
                .where(ASCC.CURRENT_ASCC_ID.eq(currentRecord.getAsccId()))
                .orderBy(ASCC.ASCC_ID.desc()).limit(1).fetchOne();

        history.setAsccId(null);
        history.setCardinalityMin(ascc.getCardinalityMin());
        history.setCardinalityMax(ascc.getCardinalityMax());
        history.setIsDeprecated((byte) ((ascc.isDeprecated()) ? 1 : 0));
        history.setDefinition(ascc.getDefinition());
        history.setRevisionTrackingNum(history.getRevisionTrackingNum() + 1);
        history.setRevisionAction((byte) RevisionAction.Update.getValue());
        history.setCreatedBy(userId);
        history.setLastUpdatedBy(userId);
        history.setCreationTimestamp(timestamp);
        history.setLastUpdateTimestamp(timestamp);

        dslContext.insertInto(ASCC).set(history).execute();

        return (result == 1);
    }

    private boolean updateBcc(ULong extensionId, ULong releaseId,
                              CcBccpNodeDetail.Bcc bcc,
                              ULong userId, Timestamp timestamp) {

        ULong currentBccId = dslContext.select(BCC.CURRENT_BCC_ID)
                .from(BCC)
                .where(BCC.BCC_ID.eq(ULong.valueOf(bcc.getBccId())))
                .fetchOneInto(ULong.class);

        BccRecord currentRecord;
        if (currentBccId == null) { // current record
            currentRecord = dslContext.selectFrom(BCC)
                    .where(BCC.BCC_ID.eq(ULong.valueOf(bcc.getBccId())))
                    .fetchOne();
        } else { // history record
            currentRecord = dslContext.selectFrom(BCC)
                    .where(BCC.BCC_ID.eq(currentBccId))
                    .fetchOne();
        }

        currentRecord.setEntityType(bcc.getEntityType());
        currentRecord.setCardinalityMin(bcc.getCardinalityMin());
        currentRecord.setCardinalityMax(bcc.getCardinalityMax());
        currentRecord.setIsNillable((byte) ((bcc.isNillable()) ? 1 : 0));
        currentRecord.setIsDeprecated((byte) ((bcc.isDeprecated()) ? 1 : 0));
        currentRecord.setDefinition(bcc.getDefinition());
        currentRecord.setDefaultValue(bcc.getDefaultValue());
        currentRecord.setFixedValue(bcc.getFixedValue());
        currentRecord.setLastUpdatedBy(userId);
        currentRecord.setLastUpdateTimestamp(timestamp);
        int result = currentRecord.update(
                BCC.ENTITY_TYPE,
                BCC.CARDINALITY_MIN, BCC.CARDINALITY_MAX,
                BCC.IS_NILLABLE, BCC.IS_DEPRECATED, BCC.DEFINITION,
                BCC.DEFAULT_VALUE, BCC.FIXED_VALUE,
                BCC.LAST_UPDATED_BY, BCC.LAST_UPDATE_TIMESTAMP
        );

        BccRecord history = dslContext.selectFrom(Tables.BCC)
                .where(BCC.CURRENT_BCC_ID.eq(currentRecord.getBccId()))
                .orderBy(BCC.BCC_ID.desc()).limit(1).fetchOne();

        history.setBccId(null);
        history.setEntityType(bcc.getEntityType());
        history.setCardinalityMin(bcc.getCardinalityMin());
        history.setCardinalityMax(bcc.getCardinalityMax());
        history.setIsNillable((byte) ((bcc.isNillable()) ? 1 : 0));
        history.setIsDeprecated((byte) ((bcc.isDeprecated()) ? 1 : 0));
        history.setDefaultValue(bcc.getDefaultValue());
        history.setDefinition(bcc.getDefinition());
        history.setRevisionTrackingNum(history.getRevisionTrackingNum() + 1);
        history.setRevisionAction((byte) RevisionAction.Update.getValue());
        history.setCreatedBy(userId);
        history.setLastUpdatedBy(userId);
        history.setCreationTimestamp(timestamp);
        history.setLastUpdateTimestamp(timestamp);

        dslContext.insertInto(BCC).set(history).execute();

        return (result == 1);
    }

    @Transactional
    public void transferOwnership(AuthenticatedPrincipal user, long releaseId, long extensionId, String targetLoginId) {
        long targetAppUserId = dslContext.select(APP_USER.APP_USER_ID)
                .from(APP_USER)
                .where(APP_USER.LOGIN_ID.equalIgnoreCase(targetLoginId))
                .fetchOptionalInto(Long.class).orElse(0L);
        if (targetAppUserId == 0L) {
            throw new IllegalArgumentException("Not found a target user.");
        }

        ULong target = ULong.valueOf(targetAppUserId);
        ULong userId = ULong.valueOf(sessionService.userId(user));
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        updateAccOwnerUserId(extensionId, releaseId, target, userId, timestamp);
        updateAsccOwnerUserId(extensionId, releaseId, target, userId, timestamp);
        updateBccOwnerUserId(extensionId, releaseId, target, userId, timestamp);
    }

    private void updateAccOwnerUserId(long extensionId, Long releaseId, ULong targetAppUserId,
                                      ULong userId, Timestamp timestamp) {
        ULong currentAccId = dslContext.select(Tables.ACC.CURRENT_ACC_ID)
                .from(Tables.ACC)
                .where(ACC.ACC_ID.eq(ULong.valueOf(extensionId)))
                .fetchOneInto(ULong.class);

        dslContext.update(Tables.ACC)
                .set(ACC.OWNER_USER_ID, targetAppUserId)
                .set(ACC.LAST_UPDATED_BY, userId)
                .set(ACC.LAST_UPDATE_TIMESTAMP, timestamp)
                .where(ACC.ACC_ID.eq(currentAccId))
                .execute();

        AccRecord history = dslContext.selectFrom(Tables.ACC)
                .where(ACC.CURRENT_ACC_ID.eq(currentAccId))
                .orderBy(ACC.ACC_ID.desc()).limit(1)
                .fetchOne();

        history.setAccId(null);
        history.setRevisionTrackingNum(history.getRevisionTrackingNum() + 1);
        history.setRevisionAction((byte) RevisionAction.Update.getValue());
        history.setCreatedBy(userId);
        history.setLastUpdatedBy(userId);
        history.setCreationTimestamp(timestamp);
        history.setLastUpdateTimestamp(timestamp);
        history.setOwnerUserId(targetAppUserId);
        long accId = dslContext.insertInto(Tables.ACC).set(history).returning().fetchOne().getAccId().longValue();
        // update Cache after transferOwnership of Extension.
        accRepository.findById(accId);
    }

    private void updateAsccOwnerUserId(long extensionId, Long releaseId, ULong targetAppUserId,
                                       ULong userId, Timestamp timestamp) {
        ULong currentAccId = dslContext.select(Tables.ACC.CURRENT_ACC_ID)
                .from(Tables.ACC)
                .where(ACC.ACC_ID.eq(ULong.valueOf(extensionId)))
                .fetchOneInto(ULong.class);

        List<CcAsccNode> asccNodes = dslContext.select(
                ASCC.ASCC_ID,
                ASCC.CURRENT_ASCC_ID,
                ASCC.GUID,
                ASCC.REVISION_NUM,
                ASCC.REVISION_TRACKING_NUM,
                ASCC.RELEASE_ID)
                .from(ASCC)
                .where(and(
                        ASCC.FROM_ACC_ID.eq(currentAccId),
                        ASCC.REVISION_NUM.greaterThan(0)))
                .fetchInto(CcAsccNode.class);

        if (asccNodes.isEmpty()) {
            return;
        }

        // Update a state of the 'current record'.
        dslContext.update(ASCC)
                .set(ASCC.OWNER_USER_ID, targetAppUserId)
                .set(ASCC.LAST_UPDATED_BY, userId)
                .set(ASCC.LAST_UPDATE_TIMESTAMP, timestamp)
                .where(and(
                        ASCC.FROM_ACC_ID.eq(currentAccId),
                        ASCC.REVISION_NUM.eq(0)))
                .execute();

        asccNodes = asccNodes.stream()
                .collect(groupingBy(CcAsccNode::getGuid)).values().stream()
                .map(entities -> CcUtility.getLatestEntity(releaseId, entities))
                .collect(Collectors.toList());

        List<ULong> asccIds = asccNodes.stream()
                .map(asccNode -> ULong.valueOf(asccNode.getAsccId()))
                .collect(Collectors.toList());

        Result<AsccRecord> asccRecordResult = dslContext.selectFrom(ASCC)
                .where(ASCC.ASCC_ID.in(asccIds))
                .fetch();

        for (AsccRecord history : asccRecordResult) {
            history.setAsccId(null);
            history.setRevisionTrackingNum(history.getRevisionTrackingNum() + 1);
            history.setRevisionAction((byte) RevisionAction.Update.getValue());
            history.setCreatedBy(userId);
            history.setLastUpdatedBy(userId);
            history.setCreationTimestamp(timestamp);
            history.setLastUpdateTimestamp(timestamp);
            history.setOwnerUserId(targetAppUserId);

            dslContext.insertInto(ASCC).set(history).execute();
        }
    }

    private void updateBccOwnerUserId(long extensionId, Long releaseId, ULong targetAppUserId,
                                      ULong userId, Timestamp timestamp) {
        ULong currentAccId = dslContext.select(Tables.ACC.CURRENT_ACC_ID)
                .from(Tables.ACC)
                .where(ACC.ACC_ID.eq(ULong.valueOf(extensionId)))
                .fetchOneInto(ULong.class);

        List<CcBccNode> bccNodes = dslContext.select(
                BCC.BCC_ID,
                BCC.CURRENT_BCC_ID,
                BCC.GUID,
                BCC.REVISION_NUM,
                BCC.REVISION_TRACKING_NUM,
                BCC.RELEASE_ID)
                .from(BCC)
                .where(and(
                        BCC.FROM_ACC_ID.eq(currentAccId),
                        BCC.REVISION_NUM.greaterThan(0)))
                .fetchInto(CcBccNode.class);

        if (bccNodes.isEmpty()) {
            return;
        }

        // Update a state of the 'current record'.
        dslContext.update(BCC)
                .set(BCC.OWNER_USER_ID, targetAppUserId)
                .set(BCC.LAST_UPDATED_BY, userId)
                .set(BCC.LAST_UPDATE_TIMESTAMP, timestamp)
                .where(and(
                        BCC.FROM_ACC_ID.eq(currentAccId),
                        BCC.REVISION_NUM.eq(0)))
                .execute();

        bccNodes = bccNodes.stream()
                .collect(groupingBy(CcBccNode::getGuid)).values().stream()
                .map(entities -> CcUtility.getLatestEntity(releaseId, entities))
                .collect(Collectors.toList());

        List<ULong> bccIds = bccNodes.stream()
                .map(bccNode -> ULong.valueOf(bccNode.getBccId()))
                .collect(Collectors.toList());

        Result<BccRecord> bccRecordResult = dslContext.selectFrom(BCC)
                .where(BCC.BCC_ID.in(bccIds))
                .fetch();

        for (BccRecord history : bccRecordResult) {
            history.setBccId(null);
            history.setRevisionTrackingNum(history.getRevisionTrackingNum() + 1);
            history.setRevisionAction((byte) RevisionAction.Update.getValue());
            history.setCreatedBy(userId);
            history.setLastUpdatedBy(userId);
            history.setCreationTimestamp(timestamp);
            history.setLastUpdateTimestamp(timestamp);
            history.setOwnerUserId(targetAppUserId);

            dslContext.insertInto(BCC).set(history).execute();
        }
    }

    public CcNode getLastRevisionCc(AuthenticatedPrincipal user, String type, long id) {
        if (type.equals("ascc")) {
            ULong currentAsccId = dslContext.select(ASCC.CURRENT_ASCC_ID)
                    .from(ASCC)
                    .where(ASCC.ASCC_ID.eq(ULong.valueOf(id)))
                    .fetchOneInto(ULong.class);
            return dslContext.select(
                    ASCC.ASCC_ID,
                    ASCC.CURRENT_ASCC_ID,
                    ASCC.GUID,
                    ASCC.REVISION_NUM,
                    ASCC.REVISION_TRACKING_NUM,
                    ASCC.CARDINALITY_MIN,
                    ASCC.CARDINALITY_MAX,
                    ASCC.RELEASE_ID)
                    .from(ASCC)
                    .where(and(ASCC.CURRENT_ASCC_ID.eq(currentAsccId),
                            ASCC.STATE.eq(CcState.Published.getValue())))
                    .orderBy(ASCC.ASCC_ID.desc())
                    .limit(1)
                    .fetchOneInto(CcAsccNode.class);
        } else if (type.equals("bcc")) {
            ULong currentBccId = dslContext.select(BCC.CURRENT_BCC_ID)
                    .from(BCC)
                    .where(BCC.BCC_ID.eq(ULong.valueOf(id)))
                    .fetchOneInto(ULong.class);
            return dslContext.select(
                    BCC.BCC_ID,
                    BCC.CURRENT_BCC_ID,
                    BCC.GUID,
                    BCC.REVISION_NUM,
                    BCC.REVISION_TRACKING_NUM,
                    BCC.CARDINALITY_MIN,
                    BCC.CARDINALITY_MAX,
                    BCC.RELEASE_ID,
                    BCC.IS_NILLABLE.as("nillable")
            ).from(BCC).where(and(
                    BCC.CURRENT_BCC_ID.eq(currentBccId),
                    BCC.STATE.eq(CcState.Published.getValue())))
                    .orderBy(BCC.BCC_ID.desc())
                    .limit(1)
                    .fetchOneInto(CcBccNode.class);
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
