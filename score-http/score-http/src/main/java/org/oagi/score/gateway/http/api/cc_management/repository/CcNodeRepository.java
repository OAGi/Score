package org.oagi.score.gateway.http.api.cc_management.repository;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jooq.*;
import org.jooq.types.UInteger;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.cc_management.data.CcASCCPType;
import org.oagi.score.gateway.http.api.cc_management.data.node.*;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.*;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.oagi.score.repo.component.dt.BdtReadRepository;
import org.oagi.score.repository.UserRepository;
import org.oagi.score.service.common.data.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.*;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.service.common.data.BCCEntityType.Attribute;

@Repository
public class CcNodeRepository {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private BdtReadRepository bdtReadRepository;

    @Autowired
    private ManifestRepository manifestRepository;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private UserRepository userRepository;

    private SelectOnConditionStep<Record16<ULong, String, String, ULong, Integer, String, String, Byte, String, ULong, UInteger, UInteger, ULong, String, ULong, ULong>> getSelectJoinStepForAccNode() {
        return dslContext.select(
                        ACC.ACC_ID,
                        ACC.GUID,
                        ACC_MANIFEST.DEN.as("name"),
                        ACC_MANIFEST.BASED_ACC_MANIFEST_ID,
                        ACC.OAGIS_COMPONENT_TYPE,
                        ACC.OBJECT_CLASS_TERM,
                        ACC.STATE,
                        ACC.IS_DEPRECATED,
                        ACC.TYPE.as("accType"),
                        ACC_MANIFEST.LOG_ID,
                        LOG.REVISION_NUM,
                        LOG.REVISION_TRACKING_NUM,
                        ACC_MANIFEST.RELEASE_ID,
                        RELEASE.RELEASE_NUM,
                        ACC.OWNER_USER_ID,
                        ACC_MANIFEST.ACC_MANIFEST_ID.as("manifest_id"))
                .from(ACC)
                .join(ACC_MANIFEST)
                .on(ACC.ACC_ID.eq(ACC_MANIFEST.ACC_ID))
                .join(RELEASE)
                .on(ACC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(LOG)
                .on(ACC_MANIFEST.LOG_ID.eq(LOG.LOG_ID));
    }

    public CcAccNode getAccNodeByAccId(AuthenticatedPrincipal user, BigInteger accId, BigInteger releaseId) {
        AccManifestRecord accManifestRecord =
                dslContext.selectFrom(ACC_MANIFEST)
                        .where(and(
                                ACC_MANIFEST.ACC_ID.eq(ULong.valueOf(accId)),
                                ACC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId))
                        ))
                        .fetchOne();

        return getAccNodeByAccManifestId(user, accManifestRecord.getAccManifestId().toBigInteger());
    }

    public CcAccNode getAccNodeByAccManifestId(AuthenticatedPrincipal user, BigInteger accManifestId) {
        CcAccNode accNode = getSelectJoinStepForAccNode()
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(ULong.valueOf(accManifestId)))
                .fetchOneInto(CcAccNode.class);
        return arrangeAccNode(user, accNode);
    }

    public CcAccNode getAccNodeFromAsccByAsccpId(AuthenticatedPrincipal user, BigInteger toAsccpId, ULong releaseId) {
        CcAsccNode asccNode = dslContext.select(
                        ASCC.ASCC_ID,
                        ASCC_MANIFEST.FROM_ACC_MANIFEST_ID,
                        ASCC.SEQ_KEY,
                        ASCC_MANIFEST.RELEASE_ID)
                .from(ASCC)
                .join(ASCC_MANIFEST)
                .on(ASCC.ASCC_ID.eq(ASCC_MANIFEST.ASCC_ID))
                .join(ASCCP_MANIFEST)
                .on(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                .where(and(
                        ASCCP_MANIFEST.ASCCP_ID.eq(ULong.valueOf(toAsccpId)),
                        ASCC_MANIFEST.RELEASE_ID.eq(releaseId)
                ))
                .fetchOneInto(CcAsccNode.class);

        AccManifestRecord accManifestRecord = dslContext.select(ACC_MANIFEST.fields())
                .from(ACC_MANIFEST)
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(ULong.valueOf(asccNode.getFromAccManifestId())))
                .fetchOneInto(AccManifestRecord.class);

        return getAccNodeByAccManifestId(user, accManifestRecord.getAccManifestId().toBigInteger());
    }

    private CcAccNode arrangeAccNode(AuthenticatedPrincipal user, CcAccNode accNode) {
        OagisComponentType oagisComponentType =
                OagisComponentType.valueOf(accNode.getOagisComponentType());
        accNode.setGroup(oagisComponentType.isGroup());
        boolean isWorkingRelease = accNode.getReleaseNum().equals("Working");
        accNode.setAccess(AccessPrivilege.toAccessPrivilege(
                sessionService.getAppUserByUsername(user), sessionService.getAppUserByUsername(accNode.getOwnerUserId()),
                accNode.getState(), isWorkingRelease));
        accNode.setHasChild(hasChild(accNode));
        accNode.setHasExtension(hasExtension(user, accNode));

        return accNode;
    }

    private boolean hasChild(CcAccNode ccAccNode) {
        if (ccAccNode.getBasedAccManifestId() != null) {
            return true;
        }
        if (ccAccNode.getManifestId().longValue() == 0L) {
            return false;
        }
        long asccCount = dslContext.selectCount()
                .from(ASCC_MANIFEST)
                .join(ASCC).on(ASCC_MANIFEST.ASCC_ID.eq(ASCC.ASCC_ID))
                .where(and(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(ULong.valueOf(ccAccNode.getManifestId())),
                        ASCC.STATE.notEqual(CcState.Deleted.name())))
                .fetchOneInto(long.class);
        if (asccCount > 0) {
            return true;
        }

        long bccCount = dslContext.selectCount()
                .from(BCC_MANIFEST)
                .join(BCC).on(BCC_MANIFEST.BCC_ID.eq(BCC.BCC_ID))
                .where(and(BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(ULong.valueOf(ccAccNode.getManifestId())),
                        BCC.STATE.notEqual(CcState.Deleted.name())))
                .fetchOneInto(long.class);
        return bccCount > 0;
    }

    private boolean hasExtension(AuthenticatedPrincipal user, CcAccNode ccAccNode) {
        ULong accManifestId = ULong.valueOf(ccAccNode.getManifestId());
        if (dslContext.selectCount()
                .from(ASCC_MANIFEST)
                .join(ASCCP_MANIFEST).on(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                .where(and(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(accManifestId),
                        ASCCP.TYPE.eq(CcASCCPType.Extension.name())))
                .fetchOneInto(long.class) > 0) {
            return true;
        } else {
            if (ccAccNode.getBasedAccManifestId() != null) {
                return hasExtension(user, getAccNodeByAccManifestId(user, ccAccNode.getBasedAccManifestId()));
            } else {
                return false;
            }
        }
    }

    private SelectOnConditionStep<Record15<ULong, String, String, ULong, String, String, ULong, UInteger, UInteger,
            ULong, String, ULong, ULong, ULong, ULong>> selectOnConditionStepForAsccpNode() {
        return dslContext.select(
                        ASCCP.ASCCP_ID,
                        ASCCP.GUID,
                        ASCCP.PROPERTY_TERM.as("name"),
                        ACC_MANIFEST.ACC_ID.as("role_of_acc_id"),
                        ASCCP.STATE,
                        ASCCP.TYPE.as("asccpType"),
                        ASCCP_MANIFEST.LOG_ID,
                        LOG.REVISION_NUM,
                        LOG.REVISION_TRACKING_NUM,
                        ASCCP_MANIFEST.RELEASE_ID,
                        RELEASE.RELEASE_NUM,
                        ASCCP_MANIFEST.ASCCP_MANIFEST_ID.as("manifest_id"),
                        ASCCP.OWNER_USER_ID,
                        ASCCP.PREV_ASCCP_ID,
                        ASCCP.NEXT_ASCCP_ID)
                .from(ASCCP)
                .join(ASCCP_MANIFEST)
                .on(ASCCP.ASCCP_ID.eq(ASCCP_MANIFEST.ASCCP_ID))
                .join(RELEASE)
                .on(ASCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(LOG)
                .on(ASCCP_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .join(ACC_MANIFEST)
                .on(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID));
    }

    public CcAsccpNode getAsccpNodeByAsccpManifestId(AuthenticatedPrincipal user, BigInteger manifestId) {
        CcAsccpNode asccpNode = selectOnConditionStepForAsccpNode()
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(ULong.valueOf(manifestId)))
                .fetchOneInto(CcAsccpNode.class);

        AppUser requester = sessionService.getAppUserByUsername(user);
        AppUser owner = sessionService.getAppUserByUsername(asccpNode.getOwnerUserId());
        boolean isWorkingRelease = asccpNode.getReleaseNum().equals("Working");
        asccpNode.setAccess(AccessPrivilege.toAccessPrivilege(requester, owner, asccpNode.getState(), isWorkingRelease));
        asccpNode.setHasChild(true); // role_of_acc_id must not be null.

        return asccpNode;
    }

    public CcAsccpNode getAsccpNodeByRoleOfAccId(BigInteger roleOfAccId, ULong releaseId) {
        CcAsccpNode asccpNode = selectOnConditionStepForAsccpNode()
                .where(and(
                        ACC_MANIFEST.ACC_ID.eq(ULong.valueOf(roleOfAccId)),
                        ASCCP_MANIFEST.RELEASE_ID.eq(releaseId)
                ))
                .fetchOneInto(CcAsccpNode.class);
        if (asccpNode == null) {
            return null;
        }
        asccpNode.setState(asccpNode.getState());
        asccpNode.setHasChild(true); // role_of_acc_id must not be null.

        return asccpNode;
    }

    private SelectOnConditionStep<Record14<
            ULong, String, String, ULong, String,
            ULong, UInteger, UInteger, ULong, String,
            ULong, ULong, ULong, ULong>> selectOnConditionStepForBccpNode() {
        return dslContext.select(
                        BCCP.BCCP_ID,
                        BCCP.GUID,
                        BCCP.PROPERTY_TERM.as("name"),
                        DT_MANIFEST.DT_ID.as("bdt_id"),
                        BCCP.STATE,
                        BCCP_MANIFEST.LOG_ID,
                        LOG.REVISION_NUM,
                        LOG.REVISION_TRACKING_NUM,
                        BCCP_MANIFEST.RELEASE_ID,
                        RELEASE.RELEASE_NUM,
                        BCCP_MANIFEST.BCCP_MANIFEST_ID.as("manifest_id"),
                        BCCP.OWNER_USER_ID,
                        BCCP.PREV_BCCP_ID,
                        BCCP.NEXT_BCCP_ID)
                .from(BCCP)
                .join(BCCP_MANIFEST)
                .on(BCCP.BCCP_ID.eq(BCCP_MANIFEST.BCCP_ID))
                .join(RELEASE)
                .on(BCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(LOG)
                .on(BCCP_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .join(DT_MANIFEST)
                .on(BCCP_MANIFEST.BDT_MANIFEST_ID.eq(DT_MANIFEST.DT_MANIFEST_ID));
    }

    private SelectOnConditionStep<Record15<ULong, String, String, String, String, ULong, UInteger, UInteger, ULong, String, ULong, ULong, ULong, ULong, ULong>> selectOnConditionStepForBdtNode() {
        return dslContext.select(
                        DT.DT_ID.as("bdt_id"),
                        DT.GUID,
                        DT_MANIFEST.DEN.as("name"),
                        DT_MANIFEST.DEN,
                        DT.STATE,
                        DT_MANIFEST.LOG_ID,
                        LOG.REVISION_NUM,
                        LOG.REVISION_TRACKING_NUM,
                        DT_MANIFEST.RELEASE_ID,
                        RELEASE.RELEASE_NUM,
                        DT_MANIFEST.DT_MANIFEST_ID.as("manifest_id"),
                        DT_MANIFEST.BASED_DT_MANIFEST_ID.as("based_manifest_id"),
                        DT.OWNER_USER_ID,
                        DT.PREV_DT_ID,
                        DT.NEXT_DT_ID)
                .from(DT)
                .join(DT_MANIFEST)
                .on(DT.DT_ID.eq(DT_MANIFEST.DT_ID))
                .join(RELEASE)
                .on(DT_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(LOG)
                .on(DT_MANIFEST.LOG_ID.eq(LOG.LOG_ID));
    }

    private SelectOnConditionStep<Record13<ULong, String, String, String, ULong, UInteger, UInteger, ULong, String, ULong, ULong, ULong, ULong>> selectOnConditionStepForDtScNode() {
        return dslContext.select(
                        DT_SC.DT_SC_ID.as("dt_sc_id"),
                        DT_SC.GUID,
                        concat(DT_SC.PROPERTY_TERM, val(" "), DT_SC.REPRESENTATION_TERM).as("name"),
                        DT.STATE,
                        DT_MANIFEST.LOG_ID,
                        LOG.REVISION_NUM,
                        LOG.REVISION_TRACKING_NUM,
                        DT_SC_MANIFEST.RELEASE_ID,
                        RELEASE.RELEASE_NUM,
                        DT_SC_MANIFEST.DT_SC_MANIFEST_ID.as("manifest_id"),
                        DT.OWNER_USER_ID,
                        DT_SC.PREV_DT_SC_ID,
                        DT_SC.NEXT_DT_SC_ID)
                .from(DT_SC)
                .join(DT_SC_MANIFEST)
                .on(DT_SC.DT_SC_ID.eq(DT_SC_MANIFEST.DT_SC_ID))
                .join(DT_MANIFEST)
                .on(DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID.eq(DT_MANIFEST.DT_MANIFEST_ID))
                .join(DT)
                .on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                .join(RELEASE)
                .on(DT_SC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(LOG)
                .on(DT_MANIFEST.LOG_ID.eq(LOG.LOG_ID));
    }

    public CcBccpNode getBccpNodeByBccpManifestId(AuthenticatedPrincipal user, BigInteger manifestId) {
        CcBccpNode bccpNode = selectOnConditionStepForBccpNode()
                .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(ULong.valueOf(manifestId)))
                .fetchOneInto(CcBccpNode.class);

        AppUser requester = sessionService.getAppUserByUsername(user);
        AppUser owner = sessionService.getAppUserByUsername(bccpNode.getOwnerUserId());
        boolean isWorkingRelease = bccpNode.getReleaseNum().equals("Working");
        bccpNode.setAccess(AccessPrivilege.toAccessPrivilege(requester, owner, bccpNode.getState(), isWorkingRelease));
        bccpNode.setHasChild(hasChild(bccpNode));

        return bccpNode;
    }

    public CcBdtNode getBdtNodeByBdtManifestId(AuthenticatedPrincipal user, BigInteger manifestId) {
        CcBdtNode bdtNode = selectOnConditionStepForBdtNode()
                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(ULong.valueOf(manifestId)))
                .fetchOneInto(CcBdtNode.class);

        AppUser requester = sessionService.getAppUserByUsername(user);
        AppUser owner = sessionService.getAppUserByUsername(bdtNode.getOwnerUserId());
        boolean isWorkingRelease = bdtNode.getReleaseNum().equals("Working");
        bdtNode.setAccess(AccessPrivilege.toAccessPrivilege(requester, owner, bdtNode.getState(), isWorkingRelease));
        bdtNode.setHasChild(hasChild(bdtNode));

        return bdtNode;
    }

    public CcBdtScNode getDtScNodeByManifestId(AuthenticatedPrincipal user, BigInteger manifestId) {
        CcBdtScNode dtScNode = selectOnConditionStepForDtScNode()
                .where(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.eq(ULong.valueOf(manifestId)))
                .fetchOneInto(CcBdtScNode.class);

        AppUser requester = sessionService.getAppUserByUsername(user);
        AppUser owner = sessionService.getAppUserByUsername(dtScNode.getOwnerUserId());
        boolean isWorkingRelease = dtScNode.getReleaseNum().equals("Working");
        dtScNode.setAccess(AccessPrivilege.toAccessPrivilege(requester, owner, dtScNode.getState(), isWorkingRelease));

        return dtScNode;
    }

    private boolean hasChild(CcBccpNode bccpNode) {
        BigInteger bdtId = bccpNode.getBdtId();
        int dtScCount = dslContext.selectCount().from(DT_SC)
                .where(and(
                        DT_SC.OWNER_DT_ID.eq(ULong.valueOf(bdtId)),
                        or(
                                DT_SC.CARDINALITY_MIN.ne(0),
                                DT_SC.CARDINALITY_MAX.ne(0)
                        ))).fetchOneInto(Integer.class);
        return (dtScCount > 0);
    }

    private boolean hasChild(CcBdtNode bdtNode) {
        BigInteger bdtId = bdtNode.getId();
        int dtScCount = dslContext.selectCount().from(DT_SC)
                .where(and(
                        DT_SC.OWNER_DT_ID.eq(ULong.valueOf(bdtId)),
                        or(
                                DT_SC.CARDINALITY_MIN.ne(0),
                                DT_SC.CARDINALITY_MAX.ne(0)
                        ))).fetchOneInto(Integer.class);
        return (dtScCount > 0);
    }

    public OagisComponentType getOagisComponentTypeByAccId(BigInteger accId) {
        int oagisComponentType = dslContext.select(ACC.OAGIS_COMPONENT_TYPE)
                .from(ACC).where(ACC.ACC_ID.eq(ULong.valueOf(accId)))
                .fetchOneInto(Integer.class);
        return OagisComponentType.valueOf(oagisComponentType);
    }

    private List<CcAsccpNode> getAsccpNodes(AuthenticatedPrincipal user, BigInteger fromAccManifestId) {
        List<CcAsccNode> asccNodes = dslContext.select(
                        ASCC.ASCC_ID,
                        ASCC_MANIFEST.ASCC_MANIFEST_ID.as("manifest_id"),
                        ASCC_MANIFEST.FROM_ACC_MANIFEST_ID,
                        ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID,
                        ASCC.GUID,
                        ASCC.SEQ_KEY,
                        ASCC.STATE.as("raw_state"),
                        ASCC_MANIFEST.RELEASE_ID)
                .from(ASCC)
                .join(ASCC_MANIFEST)
                .on(ASCC.ASCC_ID.eq(ASCC_MANIFEST.ASCC_ID))
                .where(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(ULong.valueOf(fromAccManifestId)))
                .fetchInto(CcAsccNode.class);

        if (asccNodes.isEmpty()) {
            return Collections.emptyList();
        }

        return asccNodes.stream().map(asccNode -> {
            ULong manifestId =
                    dslContext.select(ASCCP_MANIFEST.ASCCP_MANIFEST_ID)
                            .from(ASCCP_MANIFEST)
                            .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(ULong.valueOf(asccNode.getToAsccpManifestId())))
                            .fetchOneInto(ULong.class);

            CcAsccpNode asccpNode =
                    getAsccpNodeByAsccpManifestId(user, manifestId.toBigInteger());
            asccpNode.setSeqKey(asccNode.getSeqKey());
            asccpNode.setAsccId(asccNode.getAsccId());
            asccpNode.setAsccManifestId(asccNode.getManifestId());
            return asccpNode;
        }).collect(Collectors.toList());
    }

    private List<CcBccpNode> getBccpNodes(AuthenticatedPrincipal user, long fromAccManifestId) {
        List<CcBccNode> bccNodes = dslContext.select(
                        BCC.BCC_ID,
                        BCC_MANIFEST.BCC_MANIFEST_ID.as("manifest_id"),
                        BCC.GUID,
                        BCC_MANIFEST.FROM_ACC_MANIFEST_ID,
                        BCC_MANIFEST.TO_BCCP_MANIFEST_ID,
                        BCC.SEQ_KEY,
                        BCC.ENTITY_TYPE,
                        BCC.STATE.as("raw_state"),
                        BCC_MANIFEST.RELEASE_ID)
                .from(BCC)
                .join(BCC_MANIFEST)
                .on(BCC.BCC_ID.eq(BCC_MANIFEST.BCC_ID))
                .join(BCCP_MANIFEST)
                .on(BCC_MANIFEST.TO_BCCP_MANIFEST_ID.eq(BCCP_MANIFEST.BCCP_MANIFEST_ID))
                .where(BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(ULong.valueOf(fromAccManifestId)))
                .fetchInto(CcBccNode.class);

        if (bccNodes.isEmpty()) {
            return Collections.emptyList();
        }

        return bccNodes.stream().map(bccNode -> {
            ULong manifestId =
                    dslContext.select(BCCP_MANIFEST.BCCP_MANIFEST_ID)
                            .from(BCCP_MANIFEST)
                            .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq((ULong.valueOf(bccNode.getToBccpManifestId()))))
                            .fetchOneInto(ULong.class);

            CcBccpNode bccpNode = getBccpNodeByBccpManifestId(user, manifestId.toBigInteger());
            bccpNode.setSeqKey(bccNode.getSeqKey());
            bccpNode.setAttribute(bccNode.getEntityType() == Attribute);
            bccpNode.setBccId(bccNode.getBccId());
            bccpNode.setBccManifestId(bccNode.getManifestId());
            return bccpNode;
        }).collect(Collectors.toList());
    }

    public CcAccNodeDetail getAccNodeDetail(AuthenticatedPrincipal user, CcAccNode accNode) {
        CcAccNodeDetail accNodeDetail = dslContext.select(
                        ACC.ACC_ID,
                        ACC.GUID,
                        ACC.OBJECT_CLASS_TERM,
                        ACC_MANIFEST.DEN,
                        ACC.OAGIS_COMPONENT_TYPE.as("oagis_component_type"),
                        ACC.IS_ABSTRACT.as("abstracted"),
                        ACC.IS_DEPRECATED.as("deprecated"),
                        ACC.DEFINITION,
                        ACC.DEFINITION_SOURCE,
                        ACC.NAMESPACE_ID,
                        ACC_MANIFEST.ACC_MANIFEST_ID.as("manifest_id"),
                        ACC_MANIFEST.REPLACEMENT_ACC_MANIFEST_ID.as("replacement_acc_manifest_id"),
                        ACC.STATE,
                        APP_USER.LOGIN_ID.as("owner"),
                        ACC_MANIFEST.RELEASE_ID,
                        RELEASE.RELEASE_NUM,
                        ACC_MANIFEST.LOG_ID,
                        LOG.REVISION_NUM,
                        LOG.REVISION_TRACKING_NUM)
                .from(ACC_MANIFEST)
                .join(ACC)
                .on(ACC.ACC_ID.eq(ACC_MANIFEST.ACC_ID))
                .join(APP_USER)
                .on(ACC.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .join(RELEASE)
                .on(ACC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(LOG)
                .on(ACC_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(ULong.valueOf(accNode.getManifestId())))
                .fetchOneInto(CcAccNodeDetail.class);
        fillOutAboutSince(accNodeDetail, accNode.getManifestId());
        fillOutAboutLastChanged(accNodeDetail, accNode.getManifestId());
        return accNodeDetail;
    }

    private void fillOutAboutSince(CcAccNodeDetail accNode, BigInteger accManifestId) {
        Record4<ULong, ULong, String, ULong> record = dslContext.select(ACC_MANIFEST.ACC_MANIFEST_ID,
                        ACC_MANIFEST.RELEASE_ID, RELEASE.RELEASE_NUM, ACC_MANIFEST.PREV_ACC_MANIFEST_ID)
                .from(ACC_MANIFEST)
                .join(RELEASE).on(ACC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(ULong.valueOf(accManifestId)))
                .fetchOptional().orElse(null);
        if (record == null) {
            return;
        }

        ULong prevAccManifestId = record.get(ACC_MANIFEST.PREV_ACC_MANIFEST_ID);
        if (prevAccManifestId != null) {
            fillOutAboutSince(accNode, prevAccManifestId.toBigInteger());
        } else {
            accNode.setSinceManifestId(record.get(ACC_MANIFEST.ACC_MANIFEST_ID).toBigInteger());
            accNode.setSinceReleaseId(record.get(ACC_MANIFEST.RELEASE_ID).toBigInteger());
            accNode.setSinceReleaseNum(record.get(RELEASE.RELEASE_NUM));
        }
    }

    private void fillOutAboutLastChanged(CcAccNodeDetail accNode, BigInteger accManifestId) {
        Record6<ULong, ULong, ULong, ULong, ULong, String> record = dslContext.select(ACC_MANIFEST.ACC_MANIFEST_ID, ACC_MANIFEST.ACC_ID,
                        ACC_MANIFEST.as("prev").ACC_MANIFEST_ID, ACC_MANIFEST.as("prev").ACC_ID,
                        ACC_MANIFEST.as("prev").RELEASE_ID, RELEASE.RELEASE_NUM)
                .from(ACC_MANIFEST)
                .join(ACC_MANIFEST.as("prev")).on(ACC_MANIFEST.PREV_ACC_MANIFEST_ID.eq(ACC_MANIFEST.as("prev").ACC_MANIFEST_ID))
                .join(RELEASE).on(ACC_MANIFEST.as("prev").RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(ULong.valueOf(accManifestId)))
                .fetchOptional().orElse(null);
        if (record == null) {
            return;
        }

        if (record.get(ACC_MANIFEST.ACC_ID).equals(record.get(ACC_MANIFEST.as("prev").ACC_ID))) {
            fillOutAboutLastChanged(accNode, record.get(ACC_MANIFEST.as("prev").ACC_MANIFEST_ID).toBigInteger());
        } else {
            accNode.setLastChangedManifestId(record.get(ACC_MANIFEST.as("prev").ACC_MANIFEST_ID).toBigInteger());
            accNode.setLastChangedReleaseId(record.get(ACC_MANIFEST.as("prev").RELEASE_ID).toBigInteger());
            accNode.setLastChangedReleaseNum(record.get(RELEASE.RELEASE_NUM));
        }
    }

    public CcAsccpNodeDetail getAsccpNodeDetail(AuthenticatedPrincipal user, CcAsccpNode asccpNode) {
        CcAsccpNodeDetail asccpNodeDetail = new CcAsccpNodeDetail();

        BigInteger asccManifestId = asccpNode.getAsccManifestId();
        if (asccManifestId.longValue() > 0L) {
            CcAsccpNodeDetail.Ascc ascc = dslContext.select(
                            ASCC_MANIFEST.ASCC_MANIFEST_ID.as("manifest_id"),
                            ASCC.ASCC_ID,
                            ASCC.GUID,
                            ASCC_MANIFEST.DEN,
                            ASCC.CARDINALITY_MIN,
                            ASCC.CARDINALITY_MAX,
                            ASCC.IS_DEPRECATED.as("deprecated"),
                            ASCC_MANIFEST.REPLACEMENT_ASCC_MANIFEST_ID,
                            ASCC.DEFINITION,
                            ASCC.DEFINITION_SOURCE,
                            ACC.STATE,
                            APP_USER.LOGIN_ID.as("owner"),
                            ACC_MANIFEST.RELEASE_ID,
                            RELEASE.RELEASE_NUM,
                            ACC_MANIFEST.LOG_ID,
                            LOG.REVISION_NUM,
                            LOG.REVISION_TRACKING_NUM)
                    .from(ASCC_MANIFEST)
                    .join(ASCC)
                    .on(ASCC.ASCC_ID.eq(ASCC_MANIFEST.ASCC_ID))
                    .join(ACC_MANIFEST)
                    .on(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID))
                    .join(ACC)
                    .on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                    .join(APP_USER)
                    .on(ACC.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                    .join(RELEASE)
                    .on(ACC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                    .join(LOG)
                    .on(ACC_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                    .where(ASCC_MANIFEST.ASCC_MANIFEST_ID.eq(ULong.valueOf(asccManifestId)))
                    .fetchOneInto(CcAsccpNodeDetail.Ascc.class);
            fillOutAboutSince(ascc, asccManifestId);
            fillOutAboutLastChanged(ascc, asccManifestId);
            asccpNodeDetail.setAscc(ascc);
        }

        BigInteger asccpManifestId = asccpNode.getManifestId();
        CcAsccpNodeDetail.Asccp asccp = dslContext.select(
                        ASCCP_MANIFEST.ASCCP_MANIFEST_ID.as("manifest_id"),
                        ASCCP.ASCCP_ID,
                        ASCCP.GUID,
                        ASCCP.PROPERTY_TERM,
                        ASCCP_MANIFEST.DEN,
                        ASCCP.NAMESPACE_ID,
                        ASCCP.REUSABLE_INDICATOR.as("reusable"),
                        ASCCP.IS_DEPRECATED.as("deprecated"),
                        ASCCP_MANIFEST.REPLACEMENT_ASCCP_MANIFEST_ID,
                        ASCCP.IS_NILLABLE.as("nillable"),
                        ASCCP.DEFINITION,
                        ASCCP.DEFINITION_SOURCE,
                        ASCCP.STATE,
                        APP_USER.LOGIN_ID.as("owner"),
                        ASCCP_MANIFEST.RELEASE_ID,
                        RELEASE.RELEASE_NUM,
                        ASCCP_MANIFEST.LOG_ID,
                        LOG.REVISION_NUM,
                        LOG.REVISION_TRACKING_NUM)
                .from(ASCCP_MANIFEST)
                .join(ASCCP)
                .on(ASCCP.ASCCP_ID.eq(ASCCP_MANIFEST.ASCCP_ID))
                .join(APP_USER)
                .on(ASCCP.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .join(RELEASE)
                .on(ASCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(LOG)
                .on(ASCCP_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(ULong.valueOf(asccpManifestId)))
                .fetchOneInto(CcAsccpNodeDetail.Asccp.class);
        fillOutAboutSince(asccp, asccpManifestId);
        fillOutAboutLastChanged(asccp, asccpManifestId);
        asccpNodeDetail.setAsccp(asccp);

        return asccpNodeDetail;
    }

    private void fillOutAboutSince(CcAsccpNodeDetail.Ascc asccNode, BigInteger asccManifestId) {
        Record4<ULong, ULong, String, ULong> record = dslContext.select(ASCC_MANIFEST.ASCC_MANIFEST_ID,
                        ASCC_MANIFEST.RELEASE_ID, RELEASE.RELEASE_NUM, ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID)
                .from(ASCC_MANIFEST)
                .join(RELEASE).on(ASCC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(ASCC_MANIFEST.ASCC_MANIFEST_ID.eq(ULong.valueOf(asccManifestId)))
                .fetchOptional().orElse(null);
        if (record == null) {
            return;
        }

        ULong prevAsccManifestId = record.get(ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID);
        if (prevAsccManifestId != null) {
            fillOutAboutSince(asccNode, prevAsccManifestId.toBigInteger());
        } else {
            asccNode.setSinceManifestId(record.get(ASCC_MANIFEST.ASCC_MANIFEST_ID).toBigInteger());
            asccNode.setSinceReleaseId(record.get(ASCC_MANIFEST.RELEASE_ID).toBigInteger());
            asccNode.setSinceReleaseNum(record.get(RELEASE.RELEASE_NUM));
        }
    }

    private void fillOutAboutLastChanged(CcAsccpNodeDetail.Ascc asccNode, BigInteger asccManifestId) {
        Record6<ULong, ULong, ULong, ULong, ULong, String> record = dslContext.select(ASCC_MANIFEST.ASCC_MANIFEST_ID, ASCC_MANIFEST.ASCC_ID,
                        ASCC_MANIFEST.as("prev").ASCC_MANIFEST_ID, ASCC_MANIFEST.as("prev").ASCC_ID,
                        ASCC_MANIFEST.as("prev").RELEASE_ID, RELEASE.RELEASE_NUM)
                .from(ASCC_MANIFEST)
                .join(ASCC_MANIFEST.as("prev")).on(ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID.eq(ASCC_MANIFEST.as("prev").ASCC_MANIFEST_ID))
                .join(RELEASE).on(ASCC_MANIFEST.as("prev").RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(ASCC_MANIFEST.ASCC_MANIFEST_ID.eq(ULong.valueOf(asccManifestId)))
                .fetchOptional().orElse(null);
        if (record == null) {
            return;
        }

        if (record.get(ASCC_MANIFEST.ASCC_ID).equals(record.get(ASCC_MANIFEST.as("prev").ASCC_ID))) {
            fillOutAboutLastChanged(asccNode, record.get(ASCC_MANIFEST.as("prev").ASCC_MANIFEST_ID).toBigInteger());
        } else {
            asccNode.setLastChangedManifestId(record.get(ASCC_MANIFEST.as("prev").ASCC_MANIFEST_ID).toBigInteger());
            asccNode.setLastChangedReleaseId(record.get(ASCC_MANIFEST.as("prev").RELEASE_ID).toBigInteger());
            asccNode.setLastChangedReleaseNum(record.get(RELEASE.RELEASE_NUM));
        }
    }

    private void fillOutAboutSince(CcAsccpNodeDetail.Asccp asccpNode, BigInteger asccpManifestId) {
        Record4<ULong, ULong, String, ULong> record = dslContext.select(ASCCP_MANIFEST.ASCCP_MANIFEST_ID,
                        ASCCP_MANIFEST.RELEASE_ID, RELEASE.RELEASE_NUM, ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID)
                .from(ASCCP_MANIFEST)
                .join(RELEASE).on(ASCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(ULong.valueOf(asccpManifestId)))
                .fetchOptional().orElse(null);
        if (record == null) {
            return;
        }

        ULong prevAsccpManifestId = record.get(ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID);
        if (prevAsccpManifestId != null) {
            fillOutAboutSince(asccpNode, prevAsccpManifestId.toBigInteger());
        } else {
            asccpNode.setSinceManifestId(record.get(ASCCP_MANIFEST.ASCCP_MANIFEST_ID).toBigInteger());
            asccpNode.setSinceReleaseId(record.get(ASCCP_MANIFEST.RELEASE_ID).toBigInteger());
            asccpNode.setSinceReleaseNum(record.get(RELEASE.RELEASE_NUM));
        }
    }

    private void fillOutAboutLastChanged(CcAsccpNodeDetail.Asccp asccpNode, BigInteger asccpManifestId) {
        Record6<ULong, ULong, ULong, ULong, ULong, String> record = dslContext.select(ASCCP_MANIFEST.ASCCP_MANIFEST_ID, ASCCP_MANIFEST.ASCCP_ID,
                        ASCCP_MANIFEST.as("prev").ASCCP_MANIFEST_ID, ASCCP_MANIFEST.as("prev").ASCCP_ID,
                        ASCCP_MANIFEST.as("prev").RELEASE_ID, RELEASE.RELEASE_NUM)
                .from(ASCCP_MANIFEST)
                .join(ASCCP_MANIFEST.as("prev")).on(ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.as("prev").ASCCP_MANIFEST_ID))
                .join(RELEASE).on(ASCCP_MANIFEST.as("prev").RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(ULong.valueOf(asccpManifestId)))
                .fetchOptional().orElse(null);
        if (record == null) {
            return;
        }

        if (record.get(ASCCP_MANIFEST.ASCCP_ID).equals(record.get(ASCCP_MANIFEST.as("prev").ASCCP_ID))) {
            fillOutAboutLastChanged(asccpNode, record.get(ASCCP_MANIFEST.as("prev").ASCCP_MANIFEST_ID).toBigInteger());
        } else {
            asccpNode.setLastChangedManifestId(record.get(ASCCP_MANIFEST.as("prev").ASCCP_MANIFEST_ID).toBigInteger());
            asccpNode.setLastChangedReleaseId(record.get(ASCCP_MANIFEST.as("prev").RELEASE_ID).toBigInteger());
            asccpNode.setLastChangedReleaseNum(record.get(RELEASE.RELEASE_NUM));
        }
    }

    public CcAsccpNodeDetail.Asccp getAsccp(BigInteger asccpManifestId) {
        CcAsccpNodeDetail.Asccp asccp = dslContext.select(
                        ASCCP.ASCCP_ID,
                        ASCCP.PROPERTY_TERM,
                        ASCCP_MANIFEST.DEN,
                        ASCCP.DEFINITION,
                        ASCCP.GUID,
                        ASCCP.ROLE_OF_ACC_ID)
                .from(ASCCP)
                .join(ASCCP_MANIFEST).on(ASCCP.ASCCP_ID.eq(ASCCP_MANIFEST.ASCCP_ID))
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(ULong.valueOf(asccpManifestId)))
                .fetchOneInto(CcAsccpNodeDetail.Asccp.class);
        return asccp;
    }

    public AsccpManifestRecord getAsccpManifestById(long manifestId) {
        return dslContext.selectFrom(ASCCP_MANIFEST)
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(ULong.valueOf(manifestId)))
                .fetchOne();
    }

    public CcBccpNodeDetail getBccpNodeDetail(AuthenticatedPrincipal user, CcBccpNode bccpNode) {
        CcBccpNodeDetail bccpNodeDetail = new CcBccpNodeDetail();

        BigInteger bccManifestId = bccpNode.getBccManifestId();
        if (bccManifestId.longValue() > 0L) {
            CcBccpNodeDetail.Bcc bcc = dslContext.select(
                            BCC.BCC_ID,
                            BCC.GUID,
                            BCC_MANIFEST.DEN,
                            BCC.ENTITY_TYPE,
                            BCC.CARDINALITY_MIN,
                            BCC.CARDINALITY_MAX,
                            BCC.IS_DEPRECATED.as("deprecated"),
                            BCC.DEFAULT_VALUE,
                            BCC.FIXED_VALUE,
                            BCC.DEFINITION,
                            BCC.DEFINITION_SOURCE,
                            BCC_MANIFEST.BCC_MANIFEST_ID.as("manifest_id"),
                            ACC.STATE,
                            APP_USER.LOGIN_ID.as("owner"),
                            ACC_MANIFEST.RELEASE_ID,
                            RELEASE.RELEASE_NUM,
                            ACC_MANIFEST.LOG_ID,
                            LOG.REVISION_NUM,
                            LOG.REVISION_TRACKING_NUM)
                    .from(BCC_MANIFEST)
                    .join(BCC)
                    .on(BCC_MANIFEST.BCC_ID.eq(BCC.BCC_ID))
                    .join(ACC_MANIFEST)
                    .on(BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID))
                    .join(ACC)
                    .on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                    .join(APP_USER)
                    .on(ACC.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                    .join(RELEASE)
                    .on(ACC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                    .join(LOG)
                    .on(ACC_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                    .where(BCC_MANIFEST.BCC_MANIFEST_ID.eq(ULong.valueOf(bccManifestId)))
                    .fetchOneInto(CcBccpNodeDetail.Bcc.class);
            fillOutAboutSince(bcc, bccManifestId);
            fillOutAboutLastChanged(bcc, bccManifestId);
            bccpNodeDetail.setBcc(bcc);
        }

        BigInteger bccpManifestId = bccpNode.getManifestId();
        CcBccpNodeDetail.Bccp bccp = dslContext.select(
                        BCCP.BCCP_ID,
                        BCCP.GUID,
                        BCCP.PROPERTY_TERM,
                        BCCP_MANIFEST.DEN,
                        BCCP.IS_NILLABLE.as("nillable"),
                        BCCP.IS_DEPRECATED.as("deprecated"),
                        BCCP.NAMESPACE_ID,
                        BCCP.DEFAULT_VALUE,
                        BCCP.FIXED_VALUE,
                        BCCP.DEFINITION,
                        BCCP.DEFINITION_SOURCE,
                        BCCP_MANIFEST.BCCP_MANIFEST_ID.as("manifest_id"),
                        BCCP_MANIFEST.REPLACEMENT_BCCP_MANIFEST_ID,
                        BCCP.STATE,
                        APP_USER.LOGIN_ID.as("owner"),
                        BCCP_MANIFEST.RELEASE_ID,
                        RELEASE.RELEASE_NUM,
                        BCCP_MANIFEST.LOG_ID,
                        LOG.REVISION_NUM,
                        LOG.REVISION_TRACKING_NUM)
                .from(BCCP_MANIFEST)
                .join(BCCP)
                .on(BCCP.BCCP_ID.eq(BCCP_MANIFEST.BCCP_ID))
                .join(APP_USER)
                .on(BCCP.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .join(RELEASE)
                .on(BCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(LOG)
                .on(BCCP_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(ULong.valueOf(bccpManifestId)))
                .fetchOneInto(CcBccpNodeDetail.Bccp.class);
        fillOutAboutSince(bccp, bccpManifestId);
        fillOutAboutLastChanged(bccp, bccpManifestId);
        bccpNodeDetail.setBccp(bccp);

        CcBccpNodeDetail.Bdt bdt = dslContext.select(
                        DT.DT_ID.as("bdt_id"),
                        DT_MANIFEST.DT_MANIFEST_ID.as("manifest_id"),
                        DT.GUID,
                        DT.DATA_TYPE_TERM,
                        DT.REPRESENTATION_TERM,
                        DT.QUALIFIER,
                        DT.FACET_MIN_LENGTH,
                        DT.FACET_MAX_LENGTH,
                        DT.FACET_PATTERN,
                        DT.NAMESPACE_ID,
                        DT_MANIFEST.DEN,
                        DT.DEFINITION,
                        DT.DEFINITION_SOURCE,
                        DT.STATE,
                        APP_USER.LOGIN_ID.as("owner"),
                        DT_MANIFEST.RELEASE_ID,
                        RELEASE.RELEASE_NUM,
                        DT_MANIFEST.LOG_ID,
                        LOG.REVISION_NUM,
                        LOG.REVISION_TRACKING_NUM)
                .from(DT)
                .join(DT_MANIFEST)
                .on(DT.DT_ID.eq(DT_MANIFEST.DT_ID))
                .join(BCCP_MANIFEST)
                .on(DT_MANIFEST.DT_MANIFEST_ID.eq(BCCP_MANIFEST.BDT_MANIFEST_ID))
                .join(APP_USER)
                .on(DT.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .join(RELEASE)
                .on(DT_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(LOG)
                .on(DT_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(ULong.valueOf(bccpManifestId)))
                .fetchOneInto(CcBccpNodeDetail.Bdt.class);
        fillOutAboutSince(bdt, bdt.getManifestId());
        fillOutAboutLastChanged(bdt, bdt.getManifestId());
        bccpNodeDetail.setBdt(bdt);

        int cardinalityMaxOfDtScListSum = dslContext.select(DT_SC.CARDINALITY_MAX)
                .from(BCCP_MANIFEST)
                .join(DT_MANIFEST).on(BCCP_MANIFEST.BDT_MANIFEST_ID.eq(DT_MANIFEST.DT_MANIFEST_ID))
                .join(DT_SC_MANIFEST).on(DT_MANIFEST.DT_MANIFEST_ID.eq(DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID))
                .join(DT_SC).on(DT_SC_MANIFEST.DT_SC_ID.eq(DT_SC.DT_SC_ID))
                .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(ULong.valueOf(bccpManifestId)))
                .fetchStreamInto(Integer.class).reduce(0, Integer::sum);
        bdt.setHasNoSc(cardinalityMaxOfDtScListSum == 0);

        // TODO: Replace `bdt` in `bccpNodeDetail` with `bdtNodeDetail`
        CcBdtNode bdtNode = new CcBdtNode();
        bdtNode.setManifestId(dslContext.select(BCCP_MANIFEST.BDT_MANIFEST_ID)
                .from(BCCP_MANIFEST)
                .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(ULong.valueOf(bccpManifestId)))
                .fetchOneInto(BigInteger.class));
        CcBdtNodeDetail bdtNodeDetail = this.getBdtNodeDetail(user, bdtNode);
        bdt.setBdtPriRestriList(bdtNodeDetail.getBdtPriRestriList());

        return bccpNodeDetail;
    }

    private void fillOutAboutSince(CcBccpNodeDetail.Bcc bccNode, BigInteger bccManifestId) {
        Record4<ULong, ULong, String, ULong> record = dslContext.select(BCC_MANIFEST.BCC_MANIFEST_ID,
                        BCC_MANIFEST.RELEASE_ID, RELEASE.RELEASE_NUM, BCC_MANIFEST.PREV_BCC_MANIFEST_ID)
                .from(BCC_MANIFEST)
                .join(RELEASE).on(BCC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(BCC_MANIFEST.BCC_MANIFEST_ID.eq(ULong.valueOf(bccManifestId)))
                .fetchOptional().orElse(null);
        if (record == null) {
            return;
        }

        ULong prevBccManifestId = record.get(BCC_MANIFEST.PREV_BCC_MANIFEST_ID);
        if (prevBccManifestId != null) {
            fillOutAboutSince(bccNode, prevBccManifestId.toBigInteger());
        } else {
            bccNode.setSinceManifestId(record.get(BCC_MANIFEST.BCC_MANIFEST_ID).toBigInteger());
            bccNode.setSinceReleaseId(record.get(BCC_MANIFEST.RELEASE_ID).toBigInteger());
            bccNode.setSinceReleaseNum(record.get(RELEASE.RELEASE_NUM));
        }
    }

    private void fillOutAboutLastChanged(CcBccpNodeDetail.Bcc bccNode, BigInteger bccManifestId) {
        Record6<ULong, ULong, ULong, ULong, ULong, String> record = dslContext.select(BCC_MANIFEST.BCC_MANIFEST_ID, BCC_MANIFEST.BCC_ID,
                        BCC_MANIFEST.as("prev").BCC_MANIFEST_ID, BCC_MANIFEST.as("prev").BCC_ID,
                        BCC_MANIFEST.as("prev").RELEASE_ID, RELEASE.RELEASE_NUM)
                .from(BCC_MANIFEST)
                .join(BCC_MANIFEST.as("prev")).on(BCC_MANIFEST.PREV_BCC_MANIFEST_ID.eq(BCC_MANIFEST.as("prev").BCC_MANIFEST_ID))
                .join(RELEASE).on(BCC_MANIFEST.as("prev").RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(BCC_MANIFEST.BCC_MANIFEST_ID.eq(ULong.valueOf(bccManifestId)))
                .fetchOptional().orElse(null);
        if (record == null) {
            return;
        }

        if (record.get(BCC_MANIFEST.BCC_ID).equals(record.get(BCC_MANIFEST.as("prev").BCC_ID))) {
            fillOutAboutLastChanged(bccNode, record.get(BCC_MANIFEST.as("prev").BCC_MANIFEST_ID).toBigInteger());
        } else {
            bccNode.setLastChangedManifestId(record.get(BCC_MANIFEST.as("prev").BCC_MANIFEST_ID).toBigInteger());
            bccNode.setLastChangedReleaseId(record.get(BCC_MANIFEST.as("prev").RELEASE_ID).toBigInteger());
            bccNode.setLastChangedReleaseNum(record.get(RELEASE.RELEASE_NUM));
        }
    }

    private void fillOutAboutSince(CcBccpNodeDetail.Bccp bccpNode, BigInteger bccpManifestId) {
        Record4<ULong, ULong, String, ULong> record = dslContext.select(BCCP_MANIFEST.BCCP_MANIFEST_ID,
                        BCCP_MANIFEST.RELEASE_ID, RELEASE.RELEASE_NUM, BCCP_MANIFEST.PREV_BCCP_MANIFEST_ID)
                .from(BCCP_MANIFEST)
                .join(RELEASE).on(BCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(ULong.valueOf(bccpManifestId)))
                .fetchOptional().orElse(null);
        if (record == null) {
            return;
        }

        ULong prevBccpManifestId = record.get(BCCP_MANIFEST.PREV_BCCP_MANIFEST_ID);
        if (prevBccpManifestId != null) {
            fillOutAboutSince(bccpNode, prevBccpManifestId.toBigInteger());
        } else {
            bccpNode.setSinceManifestId(record.get(BCCP_MANIFEST.BCCP_MANIFEST_ID).toBigInteger());
            bccpNode.setSinceReleaseId(record.get(BCCP_MANIFEST.RELEASE_ID).toBigInteger());
            bccpNode.setSinceReleaseNum(record.get(RELEASE.RELEASE_NUM));
        }
    }

    private void fillOutAboutLastChanged(CcBccpNodeDetail.Bccp bccpNode, BigInteger bccpManifestId) {
        Record6<ULong, ULong, ULong, ULong, ULong, String> record = dslContext.select(BCCP_MANIFEST.BCCP_MANIFEST_ID, BCCP_MANIFEST.BCCP_ID,
                        BCCP_MANIFEST.as("prev").BCCP_MANIFEST_ID, BCCP_MANIFEST.as("prev").BCCP_ID,
                        BCCP_MANIFEST.as("prev").RELEASE_ID, RELEASE.RELEASE_NUM)
                .from(BCCP_MANIFEST)
                .join(BCCP_MANIFEST.as("prev")).on(BCCP_MANIFEST.PREV_BCCP_MANIFEST_ID.eq(BCCP_MANIFEST.as("prev").BCCP_MANIFEST_ID))
                .join(RELEASE).on(BCCP_MANIFEST.as("prev").RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(ULong.valueOf(bccpManifestId)))
                .fetchOptional().orElse(null);
        if (record == null) {
            return;
        }

        if (record.get(BCCP_MANIFEST.BCCP_ID).equals(record.get(BCCP_MANIFEST.as("prev").BCCP_ID))) {
            fillOutAboutLastChanged(bccpNode, record.get(BCCP_MANIFEST.as("prev").BCCP_MANIFEST_ID).toBigInteger());
        } else {
            bccpNode.setLastChangedManifestId(record.get(BCCP_MANIFEST.as("prev").BCCP_MANIFEST_ID).toBigInteger());
            bccpNode.setLastChangedReleaseId(record.get(BCCP_MANIFEST.as("prev").RELEASE_ID).toBigInteger());
            bccpNode.setLastChangedReleaseNum(record.get(RELEASE.RELEASE_NUM));
        }
    }

    private void fillOutAboutSince(CcBccpNodeDetail.Bdt bdtNode, BigInteger dtManifestId) {
        Record4<ULong, ULong, String, ULong> record = dslContext.select(DT_MANIFEST.DT_MANIFEST_ID,
                        DT_MANIFEST.RELEASE_ID, RELEASE.RELEASE_NUM, DT_MANIFEST.PREV_DT_MANIFEST_ID)
                .from(DT_MANIFEST)
                .join(RELEASE).on(DT_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(ULong.valueOf(dtManifestId)))
                .fetchOptional().orElse(null);
        if (record == null) {
            return;
        }

        ULong prevDtManifestId = record.get(DT_MANIFEST.PREV_DT_MANIFEST_ID);
        if (prevDtManifestId != null) {
            fillOutAboutSince(bdtNode, prevDtManifestId.toBigInteger());
        } else {
            bdtNode.setSinceManifestId(record.get(DT_MANIFEST.DT_MANIFEST_ID).toBigInteger());
            bdtNode.setSinceReleaseId(record.get(DT_MANIFEST.RELEASE_ID).toBigInteger());
            bdtNode.setSinceReleaseNum(record.get(RELEASE.RELEASE_NUM));
        }
    }

    private void fillOutAboutLastChanged(CcBccpNodeDetail.Bdt bdtNode, BigInteger bdtManifestId) {
        Record6<ULong, ULong, ULong, ULong, ULong, String> record = dslContext.select(DT_MANIFEST.DT_MANIFEST_ID, DT_MANIFEST.DT_ID,
                        DT_MANIFEST.as("prev").DT_MANIFEST_ID, DT_MANIFEST.as("prev").DT_ID,
                        DT_MANIFEST.as("prev").RELEASE_ID, RELEASE.RELEASE_NUM)
                .from(DT_MANIFEST)
                .join(DT_MANIFEST.as("prev")).on(DT_MANIFEST.PREV_DT_MANIFEST_ID.eq(DT_MANIFEST.as("prev").DT_MANIFEST_ID))
                .join(RELEASE).on(DT_MANIFEST.as("prev").RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(ULong.valueOf(bdtManifestId)))
                .fetchOptional().orElse(null);
        if (record == null) {
            return;
        }

        if (record.get(DT_MANIFEST.DT_ID).equals(record.get(DT_MANIFEST.as("prev").DT_ID))) {
            fillOutAboutLastChanged(bdtNode, record.get(DT_MANIFEST.as("prev").DT_MANIFEST_ID).toBigInteger());
        } else {
            bdtNode.setLastChangedManifestId(record.get(DT_MANIFEST.as("prev").DT_MANIFEST_ID).toBigInteger());
            bdtNode.setLastChangedReleaseId(record.get(DT_MANIFEST.as("prev").RELEASE_ID).toBigInteger());
            bdtNode.setLastChangedReleaseNum(record.get(RELEASE.RELEASE_NUM));
        }
    }

    private Map<String, CcBdtPriRestri> getPriResriMapByDtManifestId(ULong dtManifestId) {
        Map<String, CcBdtPriRestri> bdtPriRestriMap = new HashMap<>();

        List<BdtPriRestriRecord> bdtPriRestriRecords = dslContext.selectFrom(BDT_PRI_RESTRI)
                .where(BDT_PRI_RESTRI.BDT_MANIFEST_ID.eq(dtManifestId))
                .fetch();

        for (BdtPriRestriRecord bdtPriRestriRecord : bdtPriRestriRecords) {
            CcBdtPriRestri ccBdtPriRestri = new CcBdtPriRestri();
            ccBdtPriRestri.setBdtPriRestriId(bdtPriRestriRecord.getBdtPriRestriId().toBigInteger());
            if (bdtPriRestriRecord.getCdtAwdPriXpsTypeMapId() != null) {
                ccBdtPriRestri.setType(PrimitiveRestriType.Primitive);

                Record3<String, ULong, String> result = dslContext.select(CDT_PRI.NAME.as("CDT_PRI_NAME"), XBT.XBT_ID, XBT.NAME).from(XBT)
                        .join(CDT_AWD_PRI_XPS_TYPE_MAP).on(XBT.XBT_ID.eq(CDT_AWD_PRI_XPS_TYPE_MAP.XBT_ID))
                        .join(CDT_AWD_PRI).on(CDT_AWD_PRI_XPS_TYPE_MAP.CDT_AWD_PRI_ID.eq(CDT_AWD_PRI.CDT_AWD_PRI_ID))
                        .join(CDT_PRI).on(CDT_AWD_PRI.CDT_PRI_ID.eq(CDT_PRI.CDT_PRI_ID))
                        .where(CDT_AWD_PRI_XPS_TYPE_MAP.CDT_AWD_PRI_XPS_TYPE_MAP_ID.eq(bdtPriRestriRecord.getCdtAwdPriXpsTypeMapId()))
                        .fetchOne();

                ccBdtPriRestri.setPrimitiveName(result.get(CDT_PRI.NAME.as("CDT_PRI_NAME")));
                ccBdtPriRestri.setXbtId(result.get(XBT.XBT_ID).toBigInteger());
                ccBdtPriRestri.setXbtName(result.get(XBT.NAME));

                bdtPriRestriMap.put(PrimitiveRestriType.Primitive.toString() + bdtPriRestriRecord.getCdtAwdPriXpsTypeMapId(), ccBdtPriRestri);
            } else if (bdtPriRestriRecord.getCodeListManifestId() != null) {
                ccBdtPriRestri.setType(PrimitiveRestriType.CodeList);
                ccBdtPriRestri.setCodeListManifestId(bdtPriRestriRecord.getCodeListManifestId().toBigInteger());
                ccBdtPriRestri.setCodeListName(dslContext.select(CODE_LIST.NAME).from(CODE_LIST)
                        .join(CODE_LIST_MANIFEST).on(CODE_LIST.CODE_LIST_ID.eq(CODE_LIST_MANIFEST.CODE_LIST_ID))
                        .where(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(bdtPriRestriRecord.getCodeListManifestId()))
                        .fetchOneInto(String.class));

                bdtPriRestriMap.put(PrimitiveRestriType.CodeList.toString() + bdtPriRestriRecord.getCodeListManifestId(), ccBdtPriRestri);
            } else if (bdtPriRestriRecord.getAgencyIdListManifestId() != null) {
                ccBdtPriRestri.setType(PrimitiveRestriType.AgencyIdList);
                ccBdtPriRestri.setAgencyIdListManifestId(bdtPriRestriRecord.getAgencyIdListManifestId().toBigInteger());
                ccBdtPriRestri.setAgencyIdListName(dslContext.select(AGENCY_ID_LIST.NAME).from(AGENCY_ID_LIST)
                        .join(AGENCY_ID_LIST_MANIFEST).on(AGENCY_ID_LIST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID))
                        .where(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(bdtPriRestriRecord.getAgencyIdListManifestId()))
                        .fetchOneInto(String.class));

                bdtPriRestriMap.put(PrimitiveRestriType.AgencyIdList.toString() + bdtPriRestriRecord.getAgencyIdListManifestId(), ccBdtPriRestri);
            }

            ccBdtPriRestri.setDefault(bdtPriRestriRecord.getIsDefault() == (byte) 1);
        }

        return bdtPriRestriMap;
    }

    public CcBdtNodeDetail getBdtNodeDetail(AuthenticatedPrincipal user, CcBdtNode bdtNode) {
        BigInteger manifestId = bdtNode.getManifestId();

        CcBdtNodeDetail detail = dslContext.select(
                        DT.DT_ID.as("bdtId"),
                        DT_MANIFEST.DT_MANIFEST_ID.as("manifestId"),
                        DT.GUID,
                        DT.REPRESENTATION_TERM,
                        DT.DATA_TYPE_TERM,
                        DT.QUALIFIER,
                        DT.as("based").DT_ID.as("basedBdtId"),
                        DT_MANIFEST.as("basedManifest").DT_MANIFEST_ID.as("basedBdtManifestId"),
                        DT_MANIFEST.as("basedManifest").DEN.as("basedBdtDen"),
                        DT.as("based").STATE.as("basedBdtState"),
                        DT.SIX_DIGIT_ID,
                        DT.FACET_MIN_LENGTH,
                        DT.FACET_MAX_LENGTH,
                        DT.FACET_PATTERN,
                        DT.CONTENT_COMPONENT_DEFINITION,
                        DT.COMMONLY_USED,
                        DT.IS_DEPRECATED.as("deprecated"),
                        DT_MANIFEST.REPLACEMENT_DT_MANIFEST_ID,
                        DT_MANIFEST.DEN,
                        DT.DEFINITION,
                        DT.DEFINITION_SOURCE,
                        DT.STATE,
                        DT.NAMESPACE_ID,
                        APP_USER.LOGIN_ID.as("owner"),
                        DT_MANIFEST.RELEASE_ID,
                        RELEASE.RELEASE_NUM,
                        DT_MANIFEST.LOG_ID,
                        LOG.REVISION_NUM,
                        LOG.REVISION_TRACKING_NUM)
                .from(DT_MANIFEST)
                .join(DT)
                .on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                .leftOuterJoin(DT_MANIFEST.as("basedManifest"))
                .on(DT_MANIFEST.as("basedManifest").DT_MANIFEST_ID.eq(DT_MANIFEST.BASED_DT_MANIFEST_ID))
                .leftOuterJoin(DT.as("based"))
                .on(DT.as("based").DT_ID.eq(DT_MANIFEST.as("basedManifest").DT_ID))
                .join(APP_USER)
                .on(DT.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .join(RELEASE)
                .on(DT_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(LOG)
                .on(DT_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(ULong.valueOf(manifestId)))
                .fetchOneInto(CcBdtNodeDetail.class);
        fillOutAboutSince(detail, manifestId);
        fillOutAboutLastChanged(detail, manifestId);

        List<String> specs = dslContext.select(REF_SPEC.SPEC)
                .from(DT)
                .join(CDT_REF_SPEC).on(DT.DT_ID.eq(CDT_REF_SPEC.CDT_ID))
                .join(REF_SPEC).on(CDT_REF_SPEC.REF_SPEC_ID.eq(REF_SPEC.REF_SPEC_ID))
                .where(DT.DT_ID.eq(ULong.valueOf(detail.getBdtId()))).fetchInto(String.class);

        detail.setSpec(String.join(", ", specs));

        // BDT
        if (detail.getBasedBdtManifestId() != null) {
            Map<String, CcBdtPriRestri> priResriMap = getPriResriMapByDtManifestId(ULong.valueOf(detail.getManifestId()));
            Map<String, CcBdtPriRestri> basePriResriMap = getPriResriMapByDtManifestId(ULong.valueOf(detail.getBasedBdtManifestId()));
            priResriMap.keySet().stream().forEach(key -> {
                if (basePriResriMap.get(key) != null) {
                    priResriMap.get(key).setInherited(true);
                } else {
                    priResriMap.get(key).setInherited(false);
                }
            });

            detail.setBdtPriRestriList(new ArrayList<>(priResriMap.values()));
            detail.getBdtPriRestriList().sort(Comparator.comparing(CcBdtPriRestri::getBdtPriRestriId));
        } else {
            detail.setBdtPriRestriList(dslContext.select(
                            CDT_AWD_PRI.CDT_AWD_PRI_ID,
                            CDT_AWD_PRI_XPS_TYPE_MAP.IS_DEFAULT,
                            CDT_PRI.NAME.as("CDT_PRI_NAME"),
                            XBT.XBT_ID,
                            XBT.NAME)
                    .from(CDT_AWD_PRI)
                    .join(CDT_PRI).on(CDT_AWD_PRI.CDT_PRI_ID.eq(CDT_PRI.CDT_PRI_ID))
                    .join(CDT_AWD_PRI_XPS_TYPE_MAP).on(CDT_AWD_PRI.CDT_AWD_PRI_ID.eq(CDT_AWD_PRI_XPS_TYPE_MAP.CDT_AWD_PRI_ID))
                    .join(XBT).on(CDT_AWD_PRI_XPS_TYPE_MAP.XBT_ID.eq(XBT.XBT_ID))
                    .where(CDT_AWD_PRI.CDT_ID.eq(ULong.valueOf(detail.getBdtId())))
                    .fetch().stream().map(e -> {
                        CcBdtPriRestri ccBdtPriRestri = new CcBdtPriRestri();
                        ccBdtPriRestri.setType(PrimitiveRestriType.Primitive);
                        ccBdtPriRestri.setPrimitiveName(e.get(CDT_PRI.NAME.as("CDT_PRI_NAME")));
                        ccBdtPriRestri.setCdtAwdPriId(e.get(CDT_AWD_PRI.CDT_AWD_PRI_ID).toBigInteger());
                        ccBdtPriRestri.setDefault(e.get(CDT_AWD_PRI.IS_DEFAULT) == (byte) 1);
                        ccBdtPriRestri.setXbtId(e.get(XBT.XBT_ID).toBigInteger());
                        ccBdtPriRestri.setXbtName(e.get(XBT.NAME));
                        return ccBdtPriRestri;
                    }).collect(Collectors.toList()));
        }
        return detail;
    }

    private Map<String, CcBdtScPriRestri> getPriScRestriMapByDtScManifestId(ULong dtScManifestId) {
        Map<String, CcBdtScPriRestri> bdtScPriRestriMap = new HashMap<>();

        List<BdtScPriRestriRecord> bdtScPriRestriRecords = dslContext.selectFrom(BDT_SC_PRI_RESTRI)
                .where(BDT_SC_PRI_RESTRI.BDT_SC_MANIFEST_ID.eq(dtScManifestId))
                .fetch();

        for (BdtScPriRestriRecord bdtScPriRestriRecord : bdtScPriRestriRecords) {
            CcBdtScPriRestri ccBdtScPriRestri = new CcBdtScPriRestri();
            ccBdtScPriRestri.setBdtScPriRestriId(bdtScPriRestriRecord.getBdtScPriRestriId().toBigInteger());
            if (bdtScPriRestriRecord.getCdtScAwdPriXpsTypeMapId() != null) {
                ccBdtScPriRestri.setType(PrimitiveRestriType.Primitive);

                Record3<String, ULong, String> result = dslContext.select(CDT_PRI.NAME.as("CDT_PRI_NAME"), XBT.XBT_ID, XBT.NAME).from(XBT)
                        .join(CDT_SC_AWD_PRI_XPS_TYPE_MAP).on(XBT.XBT_ID.eq(CDT_SC_AWD_PRI_XPS_TYPE_MAP.XBT_ID))
                        .join(CDT_SC_AWD_PRI).on(CDT_SC_AWD_PRI_XPS_TYPE_MAP.CDT_SC_AWD_PRI_ID.eq(CDT_SC_AWD_PRI.CDT_SC_AWD_PRI_ID))
                        .join(CDT_PRI).on(CDT_SC_AWD_PRI.CDT_PRI_ID.eq(CDT_PRI.CDT_PRI_ID))
                        .where(CDT_SC_AWD_PRI_XPS_TYPE_MAP.CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID.eq(bdtScPriRestriRecord.getCdtScAwdPriXpsTypeMapId()))
                        .fetchOne();

                ccBdtScPriRestri.setPrimitiveName(result.get(CDT_PRI.NAME.as("CDT_PRI_NAME")));
                ccBdtScPriRestri.setXbtId(result.get(XBT.XBT_ID).toBigInteger());
                ccBdtScPriRestri.setXbtName(result.get(XBT.NAME));

                bdtScPriRestriMap.put(PrimitiveRestriType.Primitive.toString() + bdtScPriRestriRecord.getCdtScAwdPriXpsTypeMapId(), ccBdtScPriRestri);
            } else if (bdtScPriRestriRecord.getCodeListManifestId() != null) {
                ccBdtScPriRestri.setType(PrimitiveRestriType.CodeList);
                ccBdtScPriRestri.setCodeListManifestId(bdtScPriRestriRecord.getCodeListManifestId().toBigInteger());
                ccBdtScPriRestri.setCodeListName(dslContext.select(CODE_LIST.NAME).from(CODE_LIST)
                        .join(CODE_LIST_MANIFEST).on(CODE_LIST.CODE_LIST_ID.eq(CODE_LIST_MANIFEST.CODE_LIST_ID))
                        .where(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(bdtScPriRestriRecord.getCodeListManifestId()))
                        .fetchOneInto(String.class));

                bdtScPriRestriMap.put(PrimitiveRestriType.CodeList.toString() + bdtScPriRestriRecord.getCodeListManifestId(), ccBdtScPriRestri);
            } else if (bdtScPriRestriRecord.getAgencyIdListManifestId() != null) {
                ccBdtScPriRestri.setType(PrimitiveRestriType.AgencyIdList);
                ccBdtScPriRestri.setAgencyIdListManifestId(bdtScPriRestriRecord.getAgencyIdListManifestId().toBigInteger());
                ccBdtScPriRestri.setAgencyIdListName(dslContext.select(AGENCY_ID_LIST.NAME).from(AGENCY_ID_LIST)
                        .join(AGENCY_ID_LIST_MANIFEST).on(AGENCY_ID_LIST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID))
                        .where(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(bdtScPriRestriRecord.getAgencyIdListManifestId()))
                        .fetchOneInto(String.class));

                bdtScPriRestriMap.put(PrimitiveRestriType.AgencyIdList.toString() + bdtScPriRestriRecord.getAgencyIdListManifestId(), ccBdtScPriRestri);
            }

            ccBdtScPriRestri.setDefault(bdtScPriRestriRecord.getIsDefault() == (byte) 1);
        }

        return bdtScPriRestriMap;
    }

    private void fillOutAboutSince(CcBdtNodeDetail bdtNode, BigInteger dtManifestId) {
        Record4<ULong, ULong, String, ULong> record = dslContext.select(DT_MANIFEST.DT_MANIFEST_ID,
                        DT_MANIFEST.RELEASE_ID, RELEASE.RELEASE_NUM, DT_MANIFEST.PREV_DT_MANIFEST_ID)
                .from(DT_MANIFEST)
                .join(RELEASE).on(DT_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(ULong.valueOf(dtManifestId)))
                .fetchOptional().orElse(null);
        if (record == null) {
            return;
        }

        ULong prevDtManifestId = record.get(DT_MANIFEST.PREV_DT_MANIFEST_ID);
        if (prevDtManifestId != null) {
            fillOutAboutSince(bdtNode, prevDtManifestId.toBigInteger());
        } else {
            bdtNode.setSinceManifestId(record.get(DT_MANIFEST.DT_MANIFEST_ID).toBigInteger());
            bdtNode.setSinceReleaseId(record.get(DT_MANIFEST.RELEASE_ID).toBigInteger());
            bdtNode.setSinceReleaseNum(record.get(RELEASE.RELEASE_NUM));
        }
    }

    private void fillOutAboutLastChanged(CcBdtNodeDetail bdtNode, BigInteger dtManifestId) {
        Record6<ULong, ULong, ULong, ULong, ULong, String> record = dslContext.select(DT_MANIFEST.DT_MANIFEST_ID, DT_MANIFEST.DT_ID,
                        DT_MANIFEST.as("prev").DT_MANIFEST_ID, DT_MANIFEST.as("prev").DT_ID,
                        DT_MANIFEST.as("prev").RELEASE_ID, RELEASE.RELEASE_NUM)
                .from(DT_MANIFEST)
                .join(DT_MANIFEST.as("prev")).on(DT_MANIFEST.PREV_DT_MANIFEST_ID.eq(DT_MANIFEST.as("prev").DT_MANIFEST_ID))
                .join(RELEASE).on(DT_MANIFEST.as("prev").RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(ULong.valueOf(dtManifestId)))
                .fetchOptional().orElse(null);
        if (record == null) {
            return;
        }

        if (record.get(DT_MANIFEST.DT_ID).equals(record.get(DT_MANIFEST.as("prev").DT_ID))) {
            fillOutAboutLastChanged(bdtNode, record.get(DT_MANIFEST.as("prev").DT_MANIFEST_ID).toBigInteger());
        } else {
            bdtNode.setLastChangedManifestId(record.get(DT_MANIFEST.as("prev").DT_MANIFEST_ID).toBigInteger());
            bdtNode.setLastChangedReleaseId(record.get(DT_MANIFEST.as("prev").RELEASE_ID).toBigInteger());
            bdtNode.setLastChangedReleaseNum(record.get(RELEASE.RELEASE_NUM));
        }
    }

    public CcBdtScNodeDetail getBdtScNodeDetail(AuthenticatedPrincipal user, CcBdtScNode bdtScNode) {
        BigInteger manifestId = bdtScNode.getManifestId();
        CcBdtScNodeDetail detail = dslContext.select(
                        DT_SC_MANIFEST.DT_SC_MANIFEST_ID.as("manifestId"),
                        DT_SC.DT_SC_ID.as("bdt_sc_id"),
                        DT_SC.GUID,
                        concat(DT_SC.PROPERTY_TERM, val(". "), DT_SC.REPRESENTATION_TERM).as("den"),
                        DT_SC.OBJECT_CLASS_TERM,
                        DT_SC.BASED_DT_SC_ID,
                        DT_SC.PROPERTY_TERM,
                        DT_SC.REPRESENTATION_TERM,
                        DT_SC.CARDINALITY_MIN,
                        DT_SC.CARDINALITY_MAX,
                        DT_SC.as("prev").CARDINALITY_MIN.as("prev_cardinality_min"),
                        DT_SC.as("prev").CARDINALITY_MAX.as("prev_cardinality_max"),
                        DT_SC.as("base").CARDINALITY_MIN.as("base_cardinality_min"),
                        DT_SC.as("base").CARDINALITY_MAX.as("base_cardinality_max"),
                        DT_SC.DEFINITION,
                        DT_SC.DEFINITION_SOURCE,
                        DT_SC.DEFAULT_VALUE,
                        DT_SC.FIXED_VALUE,
                        DT_SC.FACET_MIN_LENGTH,
                        DT_SC.FACET_MAX_LENGTH,
                        DT_SC.FACET_PATTERN,
                        DT_SC.IS_DEPRECATED.as("deprecated"),
                        DT_SC_MANIFEST.REPLACEMENT_DT_SC_MANIFEST_ID,
                        DT.STATE,
                        APP_USER.LOGIN_ID.as("owner"),
                        DT_MANIFEST.RELEASE_ID,
                        RELEASE.RELEASE_NUM,
                        DT_MANIFEST.LOG_ID,
                        LOG.REVISION_NUM,
                        LOG.REVISION_TRACKING_NUM,
                        DT_MANIFEST.BASED_DT_MANIFEST_ID,
                        DT_SC_MANIFEST.BASED_DT_SC_MANIFEST_ID)
                .from(DT_SC_MANIFEST)
                .join(DT_SC)
                .on(DT_SC_MANIFEST.DT_SC_ID.eq(DT_SC.DT_SC_ID))
                .join(DT_MANIFEST)
                .on(DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID.eq(DT_MANIFEST.DT_MANIFEST_ID))
                .join(DT)
                .on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                .join(APP_USER)
                .on(DT.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .join(RELEASE)
                .on(DT_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(LOG)
                .on(DT_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .leftJoin(DT_SC.as("prev")).on(DT_SC.PREV_DT_SC_ID.eq(DT_SC.as("prev").DT_SC_ID))
                .leftJoin(DT_SC.as("base")).on(DT_SC.BASED_DT_SC_ID.eq(DT_SC.as("base").DT_SC_ID))
                .where(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.eq(ULong.valueOf(manifestId)))
                .fetchOneInto(CcBdtScNodeDetail.class);
        fillOutAboutSince(detail, manifestId);
        fillOutAboutLastChanged(detail, manifestId);

        List<String> specs = dslContext.select(REF_SPEC.SPEC)
                .from(DT_SC)
                .join(CDT_SC_REF_SPEC).on(DT_SC.DT_SC_ID.eq(CDT_SC_REF_SPEC.CDT_SC_ID))
                .join(REF_SPEC).on(CDT_SC_REF_SPEC.REF_SPEC_ID.eq(REF_SPEC.REF_SPEC_ID))
                .where(DT_SC.DT_SC_ID.eq(ULong.valueOf(detail.getBdtScId()))).fetchInto(String.class);

        detail.setSpec(String.join(", ", specs));

        if (detail.getBasedDtManifestId() != null) {
            Map<String, CcBdtScPriRestri> priResriMap = getPriScRestriMapByDtScManifestId(ULong.valueOf(detail.getManifestId()));
            if (detail.getBasedDtScManifestId() != null) {
                Map<String, CcBdtScPriRestri> basePriResriMap = getPriScRestriMapByDtScManifestId(ULong.valueOf(detail.getBasedDtScManifestId()));
                priResriMap.keySet().stream().forEach(key -> {
                    if (basePriResriMap.get(key) != null) {
                        priResriMap.get(key).setInherited(true);
                    } else {
                        priResriMap.get(key).setInherited(false);
                    }
                });
            }
            detail.setBdtScPriRestriList(new ArrayList<>(priResriMap.values()));
            detail.getBdtScPriRestriList().sort(Comparator.comparing(CcBdtScPriRestri::getBdtScPriRestriId));
        } else {
            detail.setBdtScPriRestriList(dslContext.select(
                            CDT_SC_AWD_PRI.CDT_SC_AWD_PRI_ID,
                            CDT_SC_AWD_PRI_XPS_TYPE_MAP.IS_DEFAULT,
                            CDT_PRI.NAME.as("CDT_PRI_NAME"),
                            XBT.XBT_ID,
                            XBT.NAME)
                    .from(CDT_SC_AWD_PRI)
                    .join(CDT_PRI).on(CDT_SC_AWD_PRI.CDT_PRI_ID.eq(CDT_PRI.CDT_PRI_ID))
                    .join(CDT_SC_AWD_PRI_XPS_TYPE_MAP).on(CDT_SC_AWD_PRI_XPS_TYPE_MAP.CDT_SC_AWD_PRI_ID.eq(CDT_SC_AWD_PRI.CDT_SC_AWD_PRI_ID))
                    .join(XBT).on(CDT_SC_AWD_PRI_XPS_TYPE_MAP.XBT_ID.eq(XBT.XBT_ID))
                    .where(CDT_SC_AWD_PRI.CDT_SC_ID.eq(ULong.valueOf(detail.getBdtScId())))
                    .fetch().stream().map(e -> {
                        CcBdtScPriRestri ccBdtScPriRestri = new CcBdtScPriRestri();
                        ccBdtScPriRestri.setType(PrimitiveRestriType.Primitive);
                        ccBdtScPriRestri.setPrimitiveName(e.get(CDT_PRI.NAME.as("CDT_PRI_NAME")));
                        ccBdtScPriRestri.setCdtScAwdPriId(e.get(CDT_SC_AWD_PRI.CDT_SC_AWD_PRI_ID).toBigInteger());
                        ccBdtScPriRestri.setDefault(e.get(CDT_SC_AWD_PRI_XPS_TYPE_MAP.IS_DEFAULT) == (byte) 1);
                        ccBdtScPriRestri.setXbtId(e.get(XBT.XBT_ID).toBigInteger());
                        ccBdtScPriRestri.setXbtName(e.get(XBT.NAME));
                        return ccBdtScPriRestri;
                    }).collect(Collectors.toList()));
        }

        return detail;
    }

    private void fillOutAboutSince(CcBdtScNodeDetail bdtScNode, BigInteger dtScManifestId) {
        Record4<ULong, ULong, String, ULong> record = dslContext.select(DT_SC_MANIFEST.DT_SC_MANIFEST_ID,
                        DT_SC_MANIFEST.RELEASE_ID, RELEASE.RELEASE_NUM, DT_SC_MANIFEST.PREV_DT_SC_MANIFEST_ID)
                .from(DT_SC_MANIFEST)
                .join(RELEASE).on(DT_SC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.eq(ULong.valueOf(dtScManifestId)))
                .fetchOptional().orElse(null);
        if (record == null) {
            return;
        }

        ULong prevDtScManifestId = record.get(DT_SC_MANIFEST.PREV_DT_SC_MANIFEST_ID);
        if (prevDtScManifestId != null) {
            fillOutAboutSince(bdtScNode, prevDtScManifestId.toBigInteger());
        } else {
            bdtScNode.setSinceManifestId(record.get(DT_SC_MANIFEST.DT_SC_MANIFEST_ID).toBigInteger());
            bdtScNode.setSinceReleaseId(record.get(DT_SC_MANIFEST.RELEASE_ID).toBigInteger());
            bdtScNode.setSinceReleaseNum(record.get(RELEASE.RELEASE_NUM));
        }
    }

    private void fillOutAboutLastChanged(CcBdtScNodeDetail bdtScNode, BigInteger dtScManifestId) {
        Record6<ULong, ULong, ULong, ULong, ULong, String> record = dslContext.select(DT_SC_MANIFEST.DT_SC_MANIFEST_ID, DT_SC_MANIFEST.DT_SC_ID,
                        DT_SC_MANIFEST.as("prev").DT_SC_MANIFEST_ID, DT_SC_MANIFEST.as("prev").DT_SC_ID,
                        DT_SC_MANIFEST.as("prev").RELEASE_ID, RELEASE.RELEASE_NUM)
                .from(DT_SC_MANIFEST)
                .join(DT_SC_MANIFEST.as("prev")).on(DT_SC_MANIFEST.PREV_DT_SC_MANIFEST_ID.eq(DT_SC_MANIFEST.as("prev").DT_SC_MANIFEST_ID))
                .join(RELEASE).on(DT_SC_MANIFEST.as("prev").RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.eq(ULong.valueOf(dtScManifestId)))
                .fetchOptional().orElse(null);
        if (record == null) {
            return;
        }

        if (record.get(DT_SC_MANIFEST.DT_SC_ID).equals(record.get(DT_SC_MANIFEST.as("prev").DT_SC_ID))) {
            fillOutAboutLastChanged(bdtScNode, record.get(DT_SC_MANIFEST.as("prev").DT_SC_MANIFEST_ID).toBigInteger());
        } else {
            bdtScNode.setLastChangedManifestId(record.get(DT_SC_MANIFEST.as("prev").DT_SC_MANIFEST_ID).toBigInteger());
            bdtScNode.setLastChangedReleaseId(record.get(DT_SC_MANIFEST.as("prev").RELEASE_ID).toBigInteger());
            bdtScNode.setLastChangedReleaseNum(record.get(RELEASE.RELEASE_NUM));
        }
    }

    public boolean bdtScHasRepresentationTermSameAs(String representationTerm, BigInteger bdtScManifestId) {
        if (bdtScManifestId != null) {
            String bdtScRepresentationTerm = dslContext.select(DT_SC.REPRESENTATION_TERM).from(DT_SC)
                    .join(DT_SC_MANIFEST).on(DT_SC.DT_SC_ID.eq(DT_SC_MANIFEST.DT_SC_ID))
                    .where(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.eq(ULong.valueOf(bdtScManifestId)))
                    .fetchOneInto(String.class);
            return StringUtils.equals(representationTerm, bdtScRepresentationTerm);
        }
        return false;
    }

    public List<CcBdtScPriRestri> getDefaultPrimitiveValues(String representationTerm) {
        DtRecord cdtRecord = dslContext.selectFrom(DT)
                .where(and(
                        DT.BASED_DT_ID.isNull(),
                        DT.DATA_TYPE_TERM.eq(representationTerm)))
                .fetchOne();

        Map<ULong, CdtAwdPriRecord> cdtAwdPriRecordMapById = dslContext.selectFrom(CDT_AWD_PRI)
                .where(CDT_AWD_PRI.CDT_ID.eq(cdtRecord.getDtId()))
                .fetchStream().collect(Collectors.toMap(CdtAwdPriRecord::getCdtAwdPriId, Function.identity()));

        Map<ULong, CdtPriRecord> cdtPriRecordMapById = dslContext.selectFrom(CDT_PRI)
                .where(CDT_PRI.CDT_PRI_ID.in(cdtAwdPriRecordMapById.values().stream()
                        .map(e -> e.getCdtPriId()).collect(Collectors.toList())))
                .fetchStream().collect(Collectors.toMap(CdtPriRecord::getCdtPriId, Function.identity()));

        List<CdtAwdPriXpsTypeMapRecord> cdtAwdPriXpsTypeMapRecords = dslContext.selectFrom(CDT_AWD_PRI_XPS_TYPE_MAP)
                .where(CDT_AWD_PRI_XPS_TYPE_MAP.CDT_AWD_PRI_ID.in(
                        cdtAwdPriRecordMapById.values().stream()
                                .map(e -> e.getCdtAwdPriId()).collect(Collectors.toList())
                ))
                .fetch();

        Map<ULong, List<CdtAwdPriXpsTypeMapRecord>> cdtAwdPriXpsTypeMapRecordMapByCdtAwdPriId =
                cdtAwdPriXpsTypeMapRecords.stream().collect(Collectors.groupingBy(CdtAwdPriXpsTypeMapRecord::getCdtAwdPriId));

        Map<ULong, XbtRecord> xbtRecordMapById = dslContext.selectFrom(XBT)
                .where(XBT.XBT_ID.in(cdtAwdPriXpsTypeMapRecords.stream().map(e -> e.getXbtId()).collect(Collectors.toList())))
                .fetchStream().collect(Collectors.toMap(XbtRecord::getXbtId, Function.identity()));

        List<CcBdtScPriRestri> bdtScPriRestriList = new ArrayList();
        for (CdtAwdPriXpsTypeMapRecord cdtAwdPriXpsTypeMapRecord : cdtAwdPriXpsTypeMapRecords) {
            CcBdtScPriRestri bdtScPriResri = new CcBdtScPriRestri();
            bdtScPriResri.setType(PrimitiveRestriType.Primitive);
            bdtScPriResri.setDefault(cdtAwdPriXpsTypeMapRecord.getIsDefault() == (byte) 1);

            CdtAwdPriRecord cdtAwdPriRecord = cdtAwdPriRecordMapById.get(cdtAwdPriXpsTypeMapRecord.getCdtAwdPriId());
            CdtPriRecord cdtPriRecord = cdtPriRecordMapById.get(cdtAwdPriRecord.getCdtPriId());
            bdtScPriResri.setPrimitiveName(cdtPriRecord.getName());

            XbtRecord xbtRecord = xbtRecordMapById.get(cdtAwdPriXpsTypeMapRecord.getXbtId());
            bdtScPriResri.setXbtId(xbtRecord.getXbtId().toBigInteger());
            bdtScPriResri.setXbtName(xbtRecord.getName());

            bdtScPriRestriList.add(bdtScPriResri);
        }

        return bdtScPriRestriList;
    }

    public AccManifestRecord getAccManifestByAcc(BigInteger accId, BigInteger releaseId) {
        return dslContext.selectFrom(ACC_MANIFEST)
                .where(and(
                        ACC_MANIFEST.ACC_ID.eq(ULong.valueOf(accId)),
                        ACC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId))
                ))
                .fetchOne();
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class AsccForAccHasChild extends TrackableImpl {
        private BigInteger asccId = BigInteger.ZERO;
        private String guid;

        @Override
        public BigInteger getId() {
            return asccId;
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class BccForAccHasChild extends TrackableImpl {
        private BigInteger bccId = BigInteger.ZERO;
        private String guid;

        @Override
        public BigInteger getId() {
            return bccId;
        }
    }

    public boolean isAccManifestUsed(BigInteger accManifestId) {
        int cnt = dslContext.selectCount()
                .from(ASCCP_MANIFEST)
                .join(ACC_MANIFEST)
                .on(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID))
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(ULong.valueOf(accManifestId)))
                .fetchOptionalInto(Integer.class).orElse(0);
        if (cnt > 0) {
            return true;
        }

        cnt = dslContext.selectCount()
                .from(ACC_MANIFEST)
                .join(ACC_MANIFEST.as("base"))
                .on(ACC_MANIFEST.BASED_ACC_MANIFEST_ID.eq(ACC_MANIFEST.as("base").ACC_MANIFEST_ID))
                .where(ACC_MANIFEST.as("base").ACC_MANIFEST_ID.eq(ULong.valueOf(accManifestId)))
                .fetchOptionalInto(Integer.class).orElse(0);
        if (cnt > 0) {
            return true;
        }

        cnt = dslContext.selectCount()
                .from(ASCC_MANIFEST)
                .join(ACC_MANIFEST)
                .on(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID))
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(ULong.valueOf(accManifestId)))
                .fetchOptionalInto(Integer.class).orElse(0);
        if (cnt > 0) {
            return true;
        }

        cnt = dslContext.selectCount()
                .from(BCC_MANIFEST)
                .join(ACC_MANIFEST)
                .on(BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID))
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(ULong.valueOf(accManifestId)))
                .fetchOptionalInto(Integer.class).orElse(0);
        if (cnt > 0) {
            return true;
        }

        cnt = dslContext.selectCount()
                .from(ABIE)
                .where(ABIE.BASED_ACC_MANIFEST_ID.eq(ULong.valueOf(accManifestId)))
                .fetchOptionalInto(Integer.class).orElse(0);
        return cnt > 0;
    }

    public boolean isAsccpManifestUsed(long asccpManifestId) {
        int cnt = dslContext.selectCount()
                .from(ASCC_MANIFEST)
                .join(ASCCP_MANIFEST)
                .on(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(ULong.valueOf(asccpManifestId)))
                .fetchOptionalInto(Integer.class).orElse(0);
        if (cnt > 0) {
            return true;
        }

        cnt = dslContext.selectCount()
                .from(ASBIEP)
                .where(ASBIEP.BASED_ASCCP_MANIFEST_ID.eq(ULong.valueOf(asccpManifestId)))
                .fetchOptionalInto(Integer.class).orElse(0);
        return cnt > 0;
    }

    public boolean isBccpManifestUsed(BigInteger bccpManifestId) {
        int cnt = dslContext.selectCount()
                .from(BCC_MANIFEST)
                .join(BCCP_MANIFEST)
                .on(BCC_MANIFEST.TO_BCCP_MANIFEST_ID.eq(BCCP_MANIFEST.BCCP_MANIFEST_ID))
                .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(ULong.valueOf(bccpManifestId)))
                .fetchOptionalInto(Integer.class).orElse(0);
        if (cnt > 0) {
            return true;
        }

        cnt = dslContext.selectCount()
                .from(BBIEP)
                .where(BBIEP.BASED_BCCP_MANIFEST_ID.eq(ULong.valueOf(bccpManifestId)))
                .fetchOptionalInto(Integer.class).orElse(0);
        return cnt > 0;
    }

    public void deleteAccRecords(AccManifestRecord accManifestRecord) {
        String guid = dslContext.select(ACC.GUID)
                .from(ACC)
                .where(ACC.ACC_ID.eq(accManifestRecord.getAccId()))
                .fetchOneInto(String.class);

        List<ULong> accIds = dslContext.select(ACC.ACC_ID)
                .from(ACC)
                .where(ACC.GUID.eq(guid))
                .fetchInto(ULong.class);

        List<ULong> accManifestIds = dslContext.select(ACC_MANIFEST.ACC_MANIFEST_ID)
                .from(ACC_MANIFEST)
                .where(and(
                        ACC_MANIFEST.ACC_ID.in(accIds),
                        ACC_MANIFEST.RELEASE_ID.eq(accManifestRecord.getReleaseId())
                ))
                .fetchInto(ULong.class);

        dslContext.deleteFrom(ASCC_MANIFEST)
                .where(and(
                        ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.in(accManifestIds),
                        ASCC_MANIFEST.RELEASE_ID.eq(accManifestRecord.getReleaseId())
                ))
                .execute();
        dslContext.deleteFrom(ASCC)
                .where(ASCC.FROM_ACC_ID.in(accIds))
                .execute();
        dslContext.deleteFrom(BCC_MANIFEST)
                .where(and(
                        BCC_MANIFEST.FROM_ACC_MANIFEST_ID.in(accManifestIds),
                        BCC_MANIFEST.RELEASE_ID.eq(accManifestRecord.getReleaseId())
                ))
                .execute();
        dslContext.deleteFrom(BCC)
                .where(BCC.FROM_ACC_ID.in(accIds))
                .execute();
        dslContext.deleteFrom(ACC_MANIFEST)
                .where(ACC_MANIFEST.ACC_ID.in(accIds))
                .execute();
        dslContext.deleteFrom(ACC)
                .where(ACC.ACC_ID.in(accIds))
                .execute();
    }

    public void deleteAsccpRecords(BigInteger asccpId) {
        String guid = dslContext.select(ASCCP.GUID)
                .from(ASCCP)
                .where(ASCCP.ASCCP_ID.eq(ULong.valueOf(asccpId)))
                .fetchOneInto(String.class);

        List<ULong> asccpIds = dslContext.select(ASCCP.ASCCP_ID)
                .from(ASCCP)
                .where(ASCCP.GUID.eq(guid))
                .fetchInto(ULong.class);

        dslContext.deleteFrom(ASCCP)
                .where(ASCCP.ASCCP_ID.in(asccpIds))
                .execute();
    }

    public void deleteBccpRecords(BigInteger bccpId) {
        String guid = dslContext.select(BCCP.GUID)
                .from(BCCP)
                .where(BCCP.BCCP_ID.eq(ULong.valueOf(bccpId)))
                .fetchOneInto(String.class);

        List<ULong> asccpIds = dslContext.select(BCCP.BCCP_ID)
                .from(BCCP)
                .where(BCCP.GUID.eq(guid))
                .fetchInto(ULong.class);

        dslContext.deleteFrom(BCCP)
                .where(BCCP.BCCP_ID.in(asccpIds))
                .execute();
    }

    private void insert(AccRecord accRecord) {
        ULong prevAccId = accRecord.getAccId();
        accRecord.setAccId(null);
        accRecord.setPrevAccId(prevAccId);
        accRecord.insert();

        ULong nextAccId = accRecord.getAccId();
        dslContext.update(ACC)
                .set(ACC.NEXT_ACC_ID, nextAccId)
                .where(ACC.ACC_ID.eq(prevAccId))
                .execute();
    }

    private void insert(AsccpRecord asccpRecord) {
        ULong prevAsccpId = asccpRecord.getAsccpId();
        asccpRecord.setAsccpId(null);
        asccpRecord.setPrevAsccpId(prevAsccpId);
        asccpRecord.insert();

        ULong nextAsccpId = asccpRecord.getAsccpId();
        dslContext.update(ASCCP)
                .set(ASCCP.NEXT_ASCCP_ID, nextAsccpId)
                .where(ASCCP.ASCCP_ID.eq(prevAsccpId))
                .execute();
    }

    private void insert(BccpRecord bccpRecord) {
        ULong prevBccpId = bccpRecord.getBccpId();
        bccpRecord.setBccpId(null);
        bccpRecord.setPrevBccpId(prevBccpId);
        bccpRecord.insert();

        ULong nextBccpId = bccpRecord.getBccpId();
        dslContext.update(BCCP)
                .set(BCCP.NEXT_BCCP_ID, nextBccpId)
                .where(BCCP.BCCP_ID.eq(prevBccpId))
                .execute();
    }

    public void duplicateAssociationValidate(AuthenticatedPrincipal user, ULong accManifestId, ULong asccpManifestId, ULong bccpManifestId) {
        AccManifestRecord accManifest = manifestRepository.getAccManifestById(accManifestId);

        if (asccpManifestId != null) {
            boolean exist = dslContext.selectCount()
                    .from(ASCC_MANIFEST)
                    .where(and(
                            ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(accManifestId),
                            ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.eq(accManifestId),
                            ASCC_MANIFEST.RELEASE_ID.eq(accManifest.getReleaseId())
                    )).fetchOneInto(Integer.class) > 0;
            if (exist) {
                throw new IllegalArgumentException("You cannot associate the same component.");
            }
        }
        if (bccpManifestId != null) {
            boolean exist = dslContext.selectCount()
                    .from(BCC_MANIFEST)
                    .where(and(
                            BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(accManifestId),
                            BCC_MANIFEST.TO_BCCP_MANIFEST_ID.eq(bccpManifestId),
                            BCC_MANIFEST.RELEASE_ID.eq(accManifest.getReleaseId())
                    )).fetchOneInto(Integer.class) > 0;
            if (exist) {
                throw new IllegalArgumentException("You cannot associate the same component.");
            }
        }

        CcAccNode accNode = getAccNodeByAccManifestId(user, accManifest.getAccManifestId().toBigInteger());
        OagisComponentType oagisComponentType = getOagisComponentTypeByAccId(accNode.getAccId());
        if (oagisComponentType.isGroup()) {
            CcAsccpNode roleByAsccpNode = getAsccpNodeByRoleOfAccId(accNode.getAccId(), accManifest.getReleaseId());
            if (roleByAsccpNode == null) {
                return;
            }
            ULong baseAccManifestId = dslContext.select(ACC_MANIFEST.ACC_MANIFEST_ID)
                    .from(ASCC_MANIFEST)
                    .join(ACC_MANIFEST)
                    .on(and(
                            ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID),
                            ASCC_MANIFEST.RELEASE_ID.eq(ACC_MANIFEST.RELEASE_ID)
                    ))
                    .join(ASCCP_MANIFEST)
                    .on(and(
                            ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID),
                            ASCC_MANIFEST.RELEASE_ID.eq(ASCCP_MANIFEST.RELEASE_ID)
                    ))
                    .where(and(
                            ASCCP_MANIFEST.ASCCP_ID.eq(ULong.valueOf(roleByAsccpNode.getAsccpId())),
                            ASCC_MANIFEST.RELEASE_ID.eq(accManifest.getReleaseId())
                    ))
                    .fetchOneInto(ULong.class);
            if (baseAccManifestId != null) {
                ULong edgeAccManifestId = getEdgeAccManifestId(baseAccManifestId);

                if (edgeAccManifestId != null) {
                    duplicateAssociationValidate(user, edgeAccManifestId, asccpManifestId, bccpManifestId);
                }
            }
        }
    }

    public ULong getEdgeAccManifestId(ULong accManifestId) {
        Record4 record = dslContext.select(
                        ACC_MANIFEST.as("base_acc").ACC_MANIFEST_ID,
                        ACC.OAGIS_COMPONENT_TYPE,
                        ACC.ACC_ID,
                        ACC_MANIFEST.as("base_acc").BASED_ACC_MANIFEST_ID)
                .from(ACC_MANIFEST)
                .join(ACC_MANIFEST.as("base_acc"))
                .on(and(
                        ACC_MANIFEST.as("base_acc").ACC_MANIFEST_ID.eq(ACC_MANIFEST.BASED_ACC_MANIFEST_ID),
                        ACC_MANIFEST.as("base_acc").RELEASE_ID.eq(ACC_MANIFEST.RELEASE_ID)
                ))
                .join(ACC).on(ACC_MANIFEST.as("base_acc").ACC_ID.eq(ACC.ACC_ID))
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(accManifestId))
                .fetchOne();

        if (record == null) {
            return null;
        }

        if (record.get(ACC.OAGIS_COMPONENT_TYPE) == OagisComponentType.Extension.getValue()
                && record.get(ACC_MANIFEST.BASED_ACC_MANIFEST_ID) != null) {
            return getEdgeAccManifestId(record.get(ACC_MANIFEST.ACC_MANIFEST_ID));
        }

        return record.get(ACC_MANIFEST.ACC_MANIFEST_ID);
    }

    public CcState getAccState(BigInteger manifestId) {
        return CcState.valueOf(
                dslContext.select(ACC.STATE)
                        .from(ACC)
                        .join(ACC_MANIFEST).on(ACC.ACC_ID.eq(ACC_MANIFEST.ACC_ID))
                        .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(ULong.valueOf(manifestId)))
                        .fetchOneInto(String.class)
        );
    }

    public CcState getAsccpState(BigInteger manifestId) {
        return CcState.valueOf(
                dslContext.select(ASCCP.STATE)
                        .from(ASCCP)
                        .join(ASCCP_MANIFEST).on(ASCCP.ASCCP_ID.eq(ASCCP_MANIFEST.ASCCP_ID))
                        .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(ULong.valueOf(manifestId)))
                        .fetchOneInto(String.class)
        );
    }

    public CcState getBccpState(BigInteger manifestId) {
        return CcState.valueOf(
                dslContext.select(BCCP.STATE)
                        .from(BCCP)
                        .join(BCCP_MANIFEST).on(BCCP.BCCP_ID.eq(BCCP_MANIFEST.BCCP_ID))
                        .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(ULong.valueOf(manifestId)))
                        .fetchOneInto(String.class)
        );
    }

    public CcState getDtState(BigInteger manifestId) {
        return CcState.valueOf(
                dslContext.select(DT.STATE)
                        .from(DT)
                        .join(DT_MANIFEST).on(DT.DT_ID.eq(DT_MANIFEST.DT_ID))
                        .where(DT_MANIFEST.DT_MANIFEST_ID.eq(ULong.valueOf(manifestId)))
                        .fetchOneInto(String.class)
        );
    }
}