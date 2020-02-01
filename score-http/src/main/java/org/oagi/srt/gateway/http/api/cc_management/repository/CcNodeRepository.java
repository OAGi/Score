package org.oagi.srt.gateway.http.api.cc_management.repository;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Record11;
import org.jooq.SelectJoinStep;
import org.jooq.types.ULong;
import org.oagi.srt.data.BCCEntityType;
import org.oagi.srt.data.OagisComponentType;
import org.oagi.srt.data.RevisionAction;
import org.oagi.srt.data.SeqKeySupportable;
import org.oagi.srt.entity.jooq.Tables;
import org.oagi.srt.entity.jooq.tables.records.AccRecord;
import org.oagi.srt.gateway.http.api.cc_management.data.CcState;
import org.oagi.srt.gateway.http.api.cc_management.data.node.*;
import org.oagi.srt.gateway.http.api.cc_management.helper.CcUtility;
import org.oagi.srt.gateway.http.api.common.data.TrackableImpl;
import org.oagi.srt.gateway.http.configuration.security.SessionService;
import org.oagi.srt.gateway.http.helper.SrtGuid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.jooq.impl.DSL.*;
import static org.oagi.srt.data.BCCEntityType.Attribute;
import static org.oagi.srt.entity.jooq.Tables.*;

@Repository
public class CcNodeRepository {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private SessionService sessionService;

    private SelectJoinStep<Record11<
            ULong, String, String, ULong, Integer,
            String, Integer, Integer, Integer, ULong,
            ULong>> getSelectJoinStepForAccNode() {
        return dslContext.select(
                Tables.ACC.ACC_ID,
                Tables.ACC.GUID,
                Tables.ACC.DEN.as("name"),
                Tables.ACC.BASED_ACC_ID,
                Tables.ACC.OAGIS_COMPONENT_TYPE,
                Tables.ACC.OBJECT_CLASS_TERM,
                Tables.ACC.STATE.as("raw_state"),
                Tables.ACC.REVISION_NUM,
                Tables.ACC.REVISION_TRACKING_NUM,
                Tables.ACC.RELEASE_ID,
                Tables.ACC.CURRENT_ACC_ID
        ).from(Tables.ACC);
    }

    public CcAccNode getAccNodeByAccId(long accId, Long releaseId) {
        CcAccNode accNode = getSelectJoinStepForAccNode()
                .where(Tables.ACC.ACC_ID.eq(ULong.valueOf(accId)))
                .fetchOneInto(CcAccNode.class);
        return arrangeAccNode(accNode, releaseId);
    }

    public Record1<ULong> getLastAccId() {
        Record1<ULong> maxId = dslContext.select(
                max(Tables.ACC.ACC_ID)
        ).from(Tables.ACC).fetchAny();
        return maxId;
    }

    public CcAccNode getAccNodeByCurrentAccId(long currentAccId, Long releaseId) {
        List<CcAccNode> accNodes = getSelectJoinStepForAccNode()
                .where(Tables.ACC.CURRENT_ACC_ID.eq(ULong.valueOf(currentAccId)))
                .fetchInto(CcAccNode.class);

        CcAccNode accNode = CcUtility.getLatestEntity(releaseId, accNodes);
        return (accNode == null) ? null : arrangeAccNode(accNode, releaseId);
    }

    public CcAccNode getAccNodeFromAsccByAsccpId(long toAsccpId, Long releaseId) {
        List<CcAsccNode> asccNodes = dslContext.select(
                Tables.ASCC.ASCC_ID,
                Tables.ASCC.CURRENT_ASCC_ID,
                Tables.ASCC.FROM_ACC_ID,
                Tables.ASCC.SEQ_KEY,
                Tables.ASCC.REVISION_NUM,
                Tables.ASCC.REVISION_TRACKING_NUM,
                Tables.ASCC.RELEASE_ID
        ).from(Tables.ASCC).where(Tables.ASCC.TO_ASCCP_ID.eq(ULong.valueOf(toAsccpId)))
                .fetchInto(CcAsccNode.class);

        CcAsccNode asccNode = CcUtility.getLatestEntity(releaseId, asccNodes);
        return getAccNodeByCurrentAccId(asccNode.getFromAccId(), releaseId);
    }

    public void createAscc(User user, long accId, long releaseId, long asccId) {
        String accObjectClassTerm = dslContext.select(ACC.OBJECT_CLASS_TERM)
                .from(ACC).where(ACC.ACC_ID.eq(ULong.valueOf(accId)))
                .fetchOneInto(String.class);

        String asccDen = dslContext.select(ASCC.DEN)
                .from(ASCC).where(ASCC.ASCC_ID.eq(ULong.valueOf(asccId)))
                .fetchOneInto(String.class);

        long to_asccpID = dslContext.select(ASCC.TO_ASCCP_ID)
                .from(ASCC).where(ASCC.ASCC_ID.eq(ULong.valueOf(asccId)))
                .fetchOneInto(long.class);
        // ULong releaseID = ULong.valueOf(releaseId);
        ULong userId = ULong.valueOf(sessionService.userId(user));
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

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
                Tables.ASCC.REVISION_ACTION).values(
                SrtGuid.randomGuid(),
                0,
                1,
                1,
                ULong.valueOf(accId),
                ULong.valueOf(to_asccpID),
                accObjectClassTerm + ". " + asccDen,
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

    public AccRecord createAcc(long requesterId) {
        ULong userId = ULong.valueOf(requesterId);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        AccRecord accRecord = new AccRecord();
        accRecord.setGuid(SrtGuid.randomGuid());
        accRecord.setObjectClassTerm("A new ACC Object");
        accRecord.setDen(accRecord.getObjectClassTerm() + ". Details");
        accRecord.setOagisComponentType(OagisComponentType.Semantics.getValue());
        accRecord.setState(CcState.Editing.getValue());
        accRecord.setRevisionNum(0);
        accRecord.setRevisionTrackingNum(0);
        accRecord.setRevisionAction((byte) 0);
        accRecord.setCreatedBy(userId);
        accRecord.setLastUpdatedBy(userId);
        accRecord.setOwnerUserId(userId);
        accRecord.setCreationTimestamp(timestamp);
        accRecord.setLastUpdateTimestamp(timestamp);

        accRecord.setAccId(
                dslContext.insertInto(Tables.ACC)
                        .set(accRecord)
                        .returning(ACC.ACC_ID).fetchOne().getAccId()
        );

        AccRecord accHistoryRecord = new AccRecord();
        accHistoryRecord.setGuid(accRecord.getGuid());
        accHistoryRecord.setObjectClassTerm(accRecord.getObjectClassTerm());
        accHistoryRecord.setDen(accRecord.getDen());
        accHistoryRecord.setOagisComponentType(accRecord.getOagisComponentType());
        accHistoryRecord.setState(accRecord.getState());
        accHistoryRecord.setRevisionNum(1);
        accHistoryRecord.setRevisionTrackingNum(1);
        accHistoryRecord.setRevisionAction((byte) RevisionAction.Insert.getValue());
        accHistoryRecord.setCreatedBy(accRecord.getCreatedBy());
        accHistoryRecord.setLastUpdatedBy(accRecord.getLastUpdatedBy());
        accHistoryRecord.setOwnerUserId(accRecord.getOwnerUserId());
        accHistoryRecord.setCreationTimestamp(accRecord.getCreationTimestamp());
        accHistoryRecord.setLastUpdateTimestamp(accRecord.getLastUpdateTimestamp());
        accHistoryRecord.setCurrentAccId(accRecord.getAccId());

        dslContext.insertInto(Tables.ACC)
                .set(accHistoryRecord)
                .execute();

        return accRecord;
    }

    public void updateAcc(User user, CcAccNode ccAccNode) {
        long userId = sessionService.userId(user);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        dslContext.update(ACC)
                .set(ACC.DEFINITION, ccAccNode.getDefinition())
                .set(ACC.DEN, ccAccNode.getDen())
                .set(ACC.OBJECT_CLASS_TERM, ccAccNode.getObjectClassTerm())
                .set(ACC.OAGIS_COMPONENT_TYPE, ccAccNode.getOagisComponentType())
                .set(ASCC.IS_DEPRECATED, (byte) ((ccAccNode.isDeprecated()) ? 1 : 0))
                .set(ACC.IS_ABSTRACT, (byte) ((ccAccNode.isAbstract()) ? 1 : 0))
                .set(ACC.LAST_UPDATED_BY, ULong.valueOf(userId))
                .set(ACC.LAST_UPDATE_TIMESTAMP, timestamp)
                .where(ACC.ACC_ID.eq(ULong.valueOf(ccAccNode.getAccId())));
    }

    public void updateAsccp(User user, CcAsccpNodeDetail.Asccp asccpNodeDetail, long id) {
        long userId = sessionService.userId(user);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        dslContext.update(ASCCP)
                .set(ASCCP.DEFINITION, asccpNodeDetail.getDefinition())
                .set(ASCCP.DEN, asccpNodeDetail.getDen())
                .set(ASCCP.IS_DEPRECATED, (byte) ((asccpNodeDetail.isDeprecated()) ? 1 : 0))
                .set(ASCCP.REUSABLE_INDICATOR, (byte) ((asccpNodeDetail.isReusable()) ? 1 : 0))
                .set(ASCCP.PROPERTY_TERM, asccpNodeDetail.getPropertyTerm())
                .set(ASCCP.LAST_UPDATED_BY, ULong.valueOf(userId))
                .set(ASCCP.LAST_UPDATE_TIMESTAMP, timestamp)
                .where(ASCCP.ASCCP_ID.eq(ULong.valueOf(id)));
    }

    private CcAccNode arrangeAccNode(CcAccNode accNode, Long releaseId) {
        OagisComponentType oagisComponentType =
                OagisComponentType.valueOf(accNode.getOagisComponentType());
        accNode.setGroup(oagisComponentType.isGroup());

        accNode.setState(CcState.valueOf(accNode.getRawState()));
        accNode.setHasChild(hasChild(accNode, releaseId));

        return accNode;
    }

    private boolean hasChild(CcAccNode accNode, Long releaseId) {
        if (accNode.getBasedAccId() != null) {
            return true;
        } else {
            Long fromAccId = (releaseId == null || releaseId == 0L) ?
                    accNode.getAccId() : accNode.getCurrentAccId();
            if (fromAccId == null) {
                return false;
            }
            List<AsccForAccHasChild> asccList = dslContext.select(
                    Tables.ASCC.ASCC_ID,
                    Tables.ASCC.CURRENT_ASCC_ID,
                    Tables.ASCC.GUID,
                    Tables.ASCC.REVISION_NUM,
                    Tables.ASCC.REVISION_TRACKING_NUM,
                    Tables.ASCC.RELEASE_ID
            ).from(Tables.ASCC).where(Tables.ASCC.FROM_ACC_ID.eq(ULong.valueOf(fromAccId)))
                    .fetchInto(AsccForAccHasChild.class);

            long asccCount = asccList.stream().collect(groupingBy(e -> e.getGuid())).values().stream()
                    .map(entities -> CcUtility.getLatestEntity(releaseId, entities))
                    .count();
            if (asccCount > 0L) {
                return true;
            }

            List<BccForAccHasChild> bccList = dslContext.select(
                    Tables.BCC.BCC_ID,
                    Tables.BCC.CURRENT_BCC_ID,
                    Tables.BCC.GUID,
                    Tables.BCC.REVISION_NUM,
                    Tables.BCC.REVISION_TRACKING_NUM,
                    Tables.BCC.RELEASE_ID
            ).from(Tables.BCC).where(Tables.BCC.FROM_ACC_ID.eq(ULong.valueOf(fromAccId)))
                    .fetchInto(BccForAccHasChild.class);

            long bccCount = bccList.stream().collect(groupingBy(e -> e.getGuid())).values().stream()
                    .map(entities -> CcUtility.getLatestEntity(releaseId, entities))
                    .count();
            return (bccCount > 0L);
        }
    }

    public Record1<ULong> getLastAsccpId() {
        Record1<ULong> maxId = dslContext.select(
                max(Tables.ASCCP.ASCCP_ID)
        ).from(Tables.ASCCP).fetchAny();
        return maxId;
    }

    public Record1<ULong> getLastBccpId() {
        Record1<ULong> maxId = dslContext.select(
                max(Tables.BCCP.BCCP_ID)
        ).from(Tables.BCCP).fetchAny();
        return maxId;
    }

    public CcAsccpNode getAsccpNodeByAsccpId(long asccpId, Long releaseId) {
        CcAsccpNode asccpNode = dslContext.select(
                Tables.ASCCP.ASCCP_ID,
                Tables.ASCCP.CURRENT_ASCCP_ID,
                Tables.ASCCP.GUID,
                Tables.ASCCP.PROPERTY_TERM.as("name"),
                Tables.ASCCP.ROLE_OF_ACC_ID,
                Tables.ASCCP.STATE.as("raw_state"),
                Tables.ASCCP.REVISION_NUM,
                Tables.ASCCP.REVISION_TRACKING_NUM,
                Tables.ASCCP.RELEASE_ID).from(Tables.ASCCP)
                .where(Tables.ASCCP.ASCCP_ID.eq(ULong.valueOf(asccpId)))
                .fetchOneInto(CcAsccpNode.class);

        asccpNode.setHasChild(true); // role_of_acc_id must not be null.

        return asccpNode;
    }

    public CcAsccpNode getAsccpNodeByCurrentAsccpId(long currentAsccpId, Long releaseId) {
        List<CcAsccpNode> asccpNodes = dslContext.select(
                Tables.ASCCP.ASCCP_ID,
                Tables.ASCCP.CURRENT_ASCCP_ID,
                Tables.ASCCP.GUID,
                Tables.ASCCP.PROPERTY_TERM.as("name"),
                Tables.ASCCP.ROLE_OF_ACC_ID,
                Tables.ASCCP.STATE.as("raw_state"),
                Tables.ASCCP.REVISION_NUM,
                Tables.ASCCP.REVISION_TRACKING_NUM,
                Tables.ASCCP.RELEASE_ID).from(Tables.ASCCP)
                .where(Tables.ASCCP.CURRENT_ASCCP_ID.eq(ULong.valueOf(currentAsccpId)))
                .fetchInto(CcAsccpNode.class);

        CcAsccpNode asccpNode = CcUtility.getLatestEntity(releaseId, asccpNodes);
        asccpNode.setHasChild(true); // role_of_acc_id must not be null.

        return asccpNode;
    }

    public CcAsccpNode getAsccpNodeByRoleOfAccId(long roleOfAccId, Long releaseId) {
        List<CcAsccpNode> asccpNodes = dslContext.select(
                Tables.ASCCP.ASCCP_ID,
                Tables.ASCCP.CURRENT_ASCCP_ID,
                Tables.ASCCP.GUID,
                Tables.ASCCP.PROPERTY_TERM.as("name"),
                Tables.ASCCP.STATE.as("raw_state"),
                Tables.ASCCP.REVISION_NUM,
                Tables.ASCCP.REVISION_TRACKING_NUM,
                Tables.ASCCP.RELEASE_ID).from(Tables.ASCCP)
                .where(Tables.ASCCP.ROLE_OF_ACC_ID.eq(ULong.valueOf(roleOfAccId)))
                .fetchInto(CcAsccpNode.class);

        CcAsccpNode asccpNode = CcUtility.getLatestEntity(releaseId, asccpNodes);
        asccpNode.setHasChild(true); // role_of_acc_id must not be null.

        return asccpNode;
    }

    public void createAsccp(User user, CcAsccpNode ccAsccpNode) {
        long roleOfAccId = ccAsccpNode.getRoleOfAccId();

        String asccpDen = dslContext.select(ACC.DEN)
                .from(ACC).where(ACC.ACC_ID.eq(ULong.valueOf(roleOfAccId)))
                .fetchOneInto(String.class);
        asccpDen = asccpDen.split("\\.")[0];
        asccpDen = "A new ASCCP property. " + asccpDen;

        ULong userId = ULong.valueOf(sessionService.userId(user));
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        dslContext.insertInto(Tables.ASCCP,
                Tables.ASCCP.GUID,
                Tables.ASCCP.PROPERTY_TERM,
                Tables.ASCCP.ROLE_OF_ACC_ID,
                Tables.ASCCP.DEN,
                Tables.ASCCP.CREATED_BY,
                Tables.ASCCP.OWNER_USER_ID,
                Tables.ASCCP.LAST_UPDATED_BY,
                Tables.ASCCP.CREATION_TIMESTAMP,
                Tables.ASCCP.LAST_UPDATE_TIMESTAMP,
                Tables.ASCCP.STATE,
                Tables.ASCCP.IS_DEPRECATED,
                Tables.ASCCP.REVISION_NUM,
                Tables.ASCCP.REVISION_TRACKING_NUM,
                Tables.ASCCP.REVISION_ACTION,
                Tables.ASCCP.RELEASE_ID).values(
                SrtGuid.randomGuid(),
                "A new ASCCP property",
                ULong.valueOf(roleOfAccId),
                asccpDen,
                userId,
                userId,
                userId,
                timestamp,
                timestamp,
                CcState.Editing.getValue(),
                Byte.valueOf((byte) 0),
                0,
                0,
                null,
                ULong.valueOf(1)).returning().fetchOne();
    }

    public CcBccpNode getBccpNodeByBccpId(long bccpId, Long releaseId) {
        CcBccpNode bccpNode = dslContext.select(
                Tables.BCCP.BCCP_ID,
                Tables.BCCP.CURRENT_BCCP_ID,
                Tables.BCCP.GUID,
                Tables.BCCP.PROPERTY_TERM.as("name"),
                Tables.BCCP.BDT_ID,
                Tables.BCCP.STATE.as("raw_state"),
                Tables.BCCP.REVISION_NUM,
                Tables.BCCP.REVISION_TRACKING_NUM,
                Tables.BCCP.RELEASE_ID).from(Tables.BCCP)
                .where(Tables.BCCP.BCCP_ID.eq(ULong.valueOf(bccpId)))
                .fetchOneInto(CcBccpNode.class);

        bccpNode.setHasChild(hasChild(bccpNode));

        return bccpNode;
    }

    public CcBccpNode getBccpNodeByCurrentBccpId(long currentBccpId, Long releaseId) {
        List<CcBccpNode> bccpNodes = dslContext.select(
                Tables.BCCP.BCCP_ID,
                Tables.BCCP.CURRENT_BCCP_ID,
                Tables.BCCP.GUID,
                Tables.BCCP.PROPERTY_TERM.as("name"),
                Tables.BCCP.BDT_ID,
                Tables.BCCP.STATE.as("raw_state"),
                Tables.BCCP.REVISION_NUM,
                Tables.BCCP.REVISION_TRACKING_NUM,
                Tables.BCCP.RELEASE_ID).from(Tables.BCCP)
                .where(Tables.BCCP.CURRENT_BCCP_ID.eq(ULong.valueOf(currentBccpId)))
                .fetchInto(CcBccpNode.class);

        CcBccpNode bccpNode = CcUtility.getLatestEntity(releaseId, bccpNodes);
        bccpNode.setHasChild(hasChild(bccpNode));
        return bccpNode;
    }

    private boolean hasChild(CcBccpNode bccpNode) {
        long bdtId = bccpNode.getBdtId();
        int dtScCount = dslContext.selectCount().from(Tables.DT_SC)
                .where(and(
                        Tables.DT_SC.OWNER_DT_ID.eq(ULong.valueOf(bdtId)),
                        or(
                                Tables.DT_SC.CARDINALITY_MIN.ne(0),
                                Tables.DT_SC.CARDINALITY_MAX.ne(0)
                        ))).fetchOneInto(Integer.class);
        return (dtScCount > 0);
    }

    public List<? extends CcNode> getDescendants(User user, CcAccNode accNode) {
        if (accNode == null) {
            return Collections.emptyList();
        }

        List<CcNode> descendants = new ArrayList();

        Long basedAccId = dslContext.select(Tables.ACC.BASED_ACC_ID).from(Tables.ACC)
                .where(Tables.ACC.ACC_ID.eq(ULong.valueOf(accNode.getAccId())))
                .fetchOneInto(Long.class);
        if (basedAccId != null) {
            Long releaseId = accNode.getReleaseId();
            CcAccNode basedAccNode;
            if (releaseId == null) {
                basedAccNode = getAccNodeByAccId(basedAccId, releaseId);
            } else {
                basedAccNode = getAccNodeByCurrentAccId(basedAccId, releaseId);
            }
            descendants.add(basedAccNode);
        }

        Long releaseId = accNode.getReleaseId();
        long fromAccId;
        if (releaseId == null) {
            fromAccId = accNode.getAccId();
        } else {
            fromAccId = dslContext.select(Tables.ACC.CURRENT_ACC_ID).from(Tables.ACC)
                    .where(Tables.ACC.ACC_ID.eq(ULong.valueOf(accNode.getAccId())))
                    .fetchOneInto(Long.class);
        }

        boolean isUserExtensionGroup = accNode.getOagisComponentType() == OagisComponentType.UserExtensionGroup.getValue();
        List<SeqKeySupportable> seqKeySupportableList = new ArrayList();
        seqKeySupportableList.addAll(
                getAsccpNodes(user, fromAccId, (isUserExtensionGroup) ? null : releaseId)
        );
        seqKeySupportableList.addAll(
                getBccpNodes(user, fromAccId, (isUserExtensionGroup) ? null : releaseId)
        );
        seqKeySupportableList.sort(Comparator.comparingInt(SeqKeySupportable::getSeqKey));

        int seqKey = 1;
        for (SeqKeySupportable e : seqKeySupportableList) {
            if (e instanceof CcAsccpNode) {
                CcAsccpNode asccpNode = (CcAsccpNode) e;
                OagisComponentType oagisComponentType = getOagisComponentTypeByAccId(asccpNode.getRoleOfAccId());
                if (oagisComponentType.equals(OagisComponentType.UserExtensionGroup)) {
                    CcAccNode uegAccNode = getAccNodeByCurrentAccId(asccpNode.getRoleOfAccId(), releaseId);
                    List<? extends CcNode> uegChildren = getDescendants(user, uegAccNode);
                    for (CcNode uegChild : uegChildren) {
                        ((SeqKeySupportable) uegChild).setSeqKey(seqKey++);
                    }
                    descendants.addAll(uegChildren);
                } else {
                    asccpNode.setSeqKey(seqKey++);
                    descendants.add(asccpNode);
                }
            } else {
                CcBccpNode bccpNode = (CcBccpNode) e;
                bccpNode.setSeqKey(seqKey++);
                descendants.add(bccpNode);
            }
        }

        return descendants;
    }

    public OagisComponentType getOagisComponentTypeByAccId(long accId) {
        int oagisComponentType = dslContext.select(Tables.ACC.OAGIS_COMPONENT_TYPE)
                .from(Tables.ACC).where(Tables.ACC.ACC_ID.eq(ULong.valueOf(accId)))
                .fetchOneInto(Integer.class);
        return OagisComponentType.valueOf(oagisComponentType);
    }

    private int getLatestRevisionAscc(long asccId) {
        return dslContext.select(
                Tables.ASCC.REVISION_NUM
        ).from(Tables.ASCC).where(ASCC.CURRENT_ASCC_ID.eq(ULong.valueOf(asccId)))
                .orderBy(ASCC.ASCC_ID.desc())
                .limit(1)
                .fetchOptionalInto(int.class).orElse(0);
    }

    private int getLatestRevisionBcc(long bccId) {
        return dslContext.select(
                Tables.BCC.REVISION_NUM
        ).from(Tables.BCC).where(BCC.CURRENT_BCC_ID.eq(ULong.valueOf(bccId)))
                .orderBy(BCC.BCC_ID.desc())
                .limit(1)
                .fetchOptionalInto(int.class).orElse(0);
    }

    private List<CcAsccpNode> getAsccpNodes(User user, long fromAccId, Long releaseId) {
        List<CcAsccNode> asccNodes = dslContext.select(
                Tables.ASCC.ASCC_ID,
                Tables.ASCC.CURRENT_ASCC_ID,
                Tables.ASCC.GUID,
                Tables.ASCC.TO_ASCCP_ID,
                Tables.ASCC.SEQ_KEY,
                Tables.ASCC.STATE.as("raw_state"),
                Tables.ASCC.REVISION_NUM,
                Tables.ASCC.REVISION_TRACKING_NUM,
                Tables.ASCC.RELEASE_ID
        ).from(Tables.ASCC).where(Tables.ASCC.FROM_ACC_ID.eq(ULong.valueOf(fromAccId)))
                .fetchInto(CcAsccNode.class);

        if (asccNodes.isEmpty()) {
            return Collections.emptyList();
        }

        asccNodes = asccNodes.stream()
                .collect(groupingBy(CcAsccNode::getGuid)).values().stream()
                .map(entities -> CcUtility.getLatestEntity(releaseId, entities))
                .collect(Collectors.toList());

        List<CcAsccpNode> asccpNodes = new ArrayList();
        for (CcAsccNode asccNode : asccNodes) {
            CcAsccpNode asccpNode;
            if (releaseId == null) {
                asccpNode = getAsccpNodeByAsccpId(asccNode.getToAsccpId(), releaseId);
            } else {
                asccpNode = getAsccpNodeByCurrentAsccpId(asccNode.getToAsccpId(), releaseId);
            }

            asccpNode.setSeqKey(asccNode.getSeqKey());
            asccpNode.setAsccId(asccNode.getAsccId());
            asccpNode.setRevisionNum(getLatestRevisionAscc(asccNode.getAsccId()));
            asccpNodes.add(asccpNode);
        }
        return asccpNodes;
    }

    private List<CcBccpNode> getBccpNodes(User user, long fromAccId, Long releaseId) {
        List<CcBccNode> bccNodes = dslContext.select(
                Tables.BCC.BCC_ID,
                Tables.BCC.CURRENT_BCC_ID,
                Tables.BCC.GUID,
                Tables.BCC.TO_BCCP_ID,
                Tables.BCC.SEQ_KEY,
                Tables.BCC.ENTITY_TYPE,
                Tables.BCC.STATE.as("raw_state"),
                Tables.BCC.REVISION_NUM,
                Tables.BCC.REVISION_TRACKING_NUM,
                Tables.BCC.RELEASE_ID
        ).from(Tables.BCC).where(Tables.BCC.FROM_ACC_ID.eq(ULong.valueOf(fromAccId)))
                .fetchInto(CcBccNode.class);

        if (bccNodes.isEmpty()) {
            return Collections.emptyList();
        }

        bccNodes = bccNodes.stream()
                .collect(groupingBy(CcBccNode::getGuid)).values().stream()
                .map(entities -> CcUtility.getLatestEntity(releaseId, entities))
                .collect(Collectors.toList());

        return bccNodes.stream().map(bccNode -> {
            CcBccpNode bccpNode;
            if (releaseId == null) {
                bccpNode = getBccpNodeByBccpId(bccNode.getToBccpId(), releaseId);
            } else {
                bccpNode = getBccpNodeByCurrentBccpId(bccNode.getToBccpId(), releaseId);
            }
            bccpNode.setSeqKey(bccNode.getSeqKey());
            bccpNode.setAttribute(BCCEntityType.valueOf(bccNode.getEntityType()) == Attribute);
            bccpNode.setBccId(bccNode.getBccId());
            bccpNode.setRevisionNum(getLatestRevisionBcc(bccNode.getBccId()));
            return bccpNode;
        }).collect(Collectors.toList());
    }

    public List<? extends CcNode> getDescendants(User user, CcAsccpNode asccpNode) {
        long asccpId = asccpNode.getAsccpId();

        long roleOfAccId = dslContext.select(Tables.ASCCP.ROLE_OF_ACC_ID).from(Tables.ASCCP)
                .where(Tables.ASCCP.ASCCP_ID.eq(ULong.valueOf(asccpId)))
                .fetchOneInto(Long.class);

        Long releaseId = asccpNode.getReleaseId();
        if (releaseId == null) {
            return Arrays.asList(getAccNodeByAccId(roleOfAccId, releaseId));
        } else {
            return Arrays.asList(getAccNodeByCurrentAccId(roleOfAccId, releaseId));
        }
    }

    public List<? extends CcNode> getDescendants(User user, CcBccpNode bccpNode) {
        long bccpId = bccpNode.getBccpId();

        return dslContext.select(
                Tables.DT_SC.DT_SC_ID.as("bdt_sc_id"),
                Tables.DT_SC.GUID,
                concat(Tables.DT_SC.PROPERTY_TERM, val(". "), Tables.DT_SC.REPRESENTATION_TERM).as("name")
        ).from(Tables.DT_SC).join(Tables.BCCP).on(Tables.DT_SC.OWNER_DT_ID.eq(Tables.BCCP.BDT_ID))
                .where(and(
                        Tables.BCCP.BCCP_ID.eq(ULong.valueOf(bccpId)),
                        or(
                                Tables.DT_SC.CARDINALITY_MIN.ne(0),
                                Tables.DT_SC.CARDINALITY_MAX.ne(0)
                        ))).fetchInto(CcBdtScNode.class);
    }

    public CcAccNodeDetail getAccNodeDetail(User user, CcAccNode accNode) {
        long accId = accNode.getAccId();

        return dslContext.select(
                Tables.ACC.ACC_ID,
                Tables.ACC.GUID,
                Tables.ACC.OBJECT_CLASS_TERM,
                Tables.ACC.DEN,
                Tables.ACC.OAGIS_COMPONENT_TYPE.as("oagisComponentType"),
                Tables.ACC.IS_ABSTRACT.as("abstracted"),
                Tables.ACC.IS_DEPRECATED.as("deprecated"),
                Tables.ACC.DEFINITION,
                Tables.ACC.DEFINITION_SOURCE
        ).from(Tables.ACC).where(Tables.ACC.ACC_ID.eq(ULong.valueOf(accId)))
                .fetchOneInto(CcAccNodeDetail.class);
    }

    public CcAsccpNodeDetail getAsccpNodeDetail(User user, CcAsccpNode asccpNode) {
        CcAsccpNodeDetail asccpNodeDetail = new CcAsccpNodeDetail();

        long asccId = asccpNode.getAsccId();
        if (asccId > 0L) {
            CcAsccpNodeDetail.Ascc ascc = dslContext.select(
                    Tables.ASCC.ASCC_ID,
                    Tables.ASCC.GUID,
                    Tables.ASCC.DEN,
                    Tables.ASCC.CARDINALITY_MIN,
                    Tables.ASCC.CARDINALITY_MAX,
                    Tables.ASCC.IS_DEPRECATED.as("deprecated"),
                    Tables.ASCC.DEFINITION,
                    Tables.ASCC.DEFINITION_SOURCE).from(Tables.ASCC)
                    .where(Tables.ASCC.ASCC_ID.eq(ULong.valueOf(asccId)))
                    .fetchOneInto(CcAsccpNodeDetail.Ascc.class);
            ascc.setRevisionNum(getLatestRevisionAscc(ascc.getAsccId()));
            asccpNodeDetail.setAscc(ascc);
        }

        long asccpId = asccpNode.getAsccpId();
        CcAsccpNodeDetail.Asccp asccp = dslContext.select(
                Tables.ASCCP.ASCCP_ID,
                Tables.ASCCP.GUID,
                Tables.ASCCP.PROPERTY_TERM,
                Tables.ASCCP.DEN,
                Tables.ASCCP.REUSABLE_INDICATOR.as("reusable"),
                Tables.ASCCP.IS_DEPRECATED.as("deprecated"),
                Tables.ASCCP.DEFINITION,
                Tables.ASCCP.DEFINITION_SOURCE).from(Tables.ASCCP)
                .where(Tables.ASCCP.ASCCP_ID.eq(ULong.valueOf(asccpId)))
                .fetchOneInto(CcAsccpNodeDetail.Asccp.class);
        asccpNodeDetail.setAsccp(asccp);

        return asccpNodeDetail;
    }

    public CcAsccpNodeDetail.Asccp getAsccp(long asccpId) {

        CcAsccpNodeDetail.Asccp asccp = dslContext.select(
                Tables.ASCCP.ASCCP_ID,
                Tables.ASCCP.DEN,
                Tables.ASCCP.PROPERTY_TERM.as("name"),
                Tables.ASCCP.RELEASE_ID.as("releaseId"),
                Tables.ASCCP.DEFINITION,
                Tables.ASCCP.GUID,
                Tables.ASCCP.ROLE_OF_ACC_ID).from(Tables.ASCCP)
                .where(Tables.ASCCP.ASCCP_ID.eq(ULong.valueOf(asccpId)))
                .fetchOneInto(CcAsccpNodeDetail.Asccp.class);

        return asccp;
    }

    public CcBccpNodeDetail getBccpNodeDetail(User user, CcBccpNode bccpNode) {
        CcBccpNodeDetail bccpNodeDetail = new CcBccpNodeDetail();

        long bccId = bccpNode.getBccId();
        if (bccId > 0L) {
            CcBccpNodeDetail.Bcc bcc = dslContext.select(
                    Tables.BCC.BCC_ID,
                    Tables.BCC.GUID,
                    Tables.BCC.DEN,
                    Tables.BCC.ENTITY_TYPE,
                    Tables.BCC.CARDINALITY_MIN,
                    Tables.BCC.CARDINALITY_MAX,
                    Tables.BCC.IS_DEPRECATED.as("deprecated"),
                    Tables.BCC.DEFAULT_VALUE,
                    Tables.BCC.FIXED_VALUE,
                    Tables.BCC.DEFINITION,
                    Tables.BCC.IS_NILLABLE.as("nillable"),
                    Tables.BCC.DEFINITION_SOURCE).from(Tables.BCC)
                    .where(Tables.BCC.BCC_ID.eq(ULong.valueOf(bccId)))
                    .fetchOneInto(CcBccpNodeDetail.Bcc.class);
            bcc.setRevisionNum(getLatestRevisionBcc(bcc.getBccId()));
            bccpNodeDetail.setBcc(bcc);
        }

        long bccpId = bccpNode.getBccpId();
        CcBccpNodeDetail.Bccp bccp = dslContext.select(
                Tables.BCCP.BCCP_ID,
                Tables.BCCP.GUID,
                Tables.BCCP.PROPERTY_TERM,
                Tables.BCCP.DEN,
                Tables.BCCP.IS_NILLABLE.as("nillable"),
                Tables.BCCP.IS_DEPRECATED.as("deprecated"),
                Tables.BCCP.DEFAULT_VALUE,
                Tables.BCCP.FIXED_VALUE,
                Tables.BCCP.DEFINITION,
                Tables.BCCP.DEFINITION_SOURCE).from(Tables.BCCP)
                .where(Tables.BCCP.BCCP_ID.eq(ULong.valueOf(bccpId)))
                .fetchOneInto(CcBccpNodeDetail.Bccp.class);
        bccpNodeDetail.setBccp(bccp);

        long bdtId = dslContext.select(Tables.BCCP.BDT_ID).from(Tables.BCCP)
                .where(Tables.BCCP.BCCP_ID.eq(ULong.valueOf(bccpId))).fetchOneInto(Long.class);

        CcBccpNodeDetail.Bdt bdt = dslContext.select(
                Tables.DT.DT_ID.as("bdt_id"),
                Tables.DT.GUID,
                Tables.DT.DATA_TYPE_TERM,
                Tables.DT.QUALIFIER,
                Tables.DT.DEN,
                Tables.DT.DEFINITION,
                Tables.DT.DEFINITION_SOURCE).from(Tables.DT)
                .where(Tables.DT.DT_ID.eq(ULong.valueOf(bdtId)))
                .fetchOneInto(CcBccpNodeDetail.Bdt.class);
        bccpNodeDetail.setBdt(bdt);

        return bccpNodeDetail;
    }

    public CcBdtScNodeDetail getBdtScNodeDetail(User user, CcBdtScNode bdtScNode) {
        long bdtScId = bdtScNode.getBdtScId();
        return dslContext.select(
                Tables.DT_SC.DT_SC_ID.as("bdt_sc_id"),
                Tables.DT_SC.GUID,
                concat(Tables.DT_SC.PROPERTY_TERM, val(". "), Tables.DT_SC.PROPERTY_TERM).as("den"),
                Tables.DT_SC.CARDINALITY_MIN,
                Tables.DT_SC.CARDINALITY_MAX,
                Tables.DT_SC.DEFINITION,
                Tables.DT_SC.DEFINITION_SOURCE,
                Tables.DT_SC.DEFAULT_VALUE,
                Tables.DT_SC.FIXED_VALUE).from(Tables.DT_SC)
                .where(Tables.DT_SC.DT_SC_ID.eq(ULong.valueOf(bdtScId)))
                .fetchOneInto(CcBdtScNodeDetail.class);
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class AsccForAccHasChild extends TrackableImpl {
        private long asccId;
        private Long currentAsccId;
        private String guid;

        @Override
        public long getId() {
            return asccId;
        }

        @Override
        public Long getCurrentId() {
            return currentAsccId;
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class BccForAccHasChild extends TrackableImpl {
        private long bccId;
        private Long currentBccId;
        private String guid;

        @Override
        public long getId() {
            return bccId;
        }

        @Override
        public Long getCurrentId() {
            return currentBccId;
        }
    }

}

