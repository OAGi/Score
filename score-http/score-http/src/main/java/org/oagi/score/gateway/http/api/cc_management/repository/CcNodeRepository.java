package org.oagi.score.gateway.http.api.cc_management.repository;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jooq.*;
import org.jooq.types.UInteger;
import org.jooq.types.ULong;
import org.oagi.score.service.common.data.AppUser;
import org.oagi.score.service.common.data.OagisComponentType;
import org.oagi.score.data.SeqKeySupportable;
import org.oagi.score.gateway.http.api.cc_management.data.CcASCCPType;
import org.oagi.score.service.common.data.CcState;
import org.oagi.score.gateway.http.api.cc_management.data.node.*;
import org.oagi.score.service.common.data.AccessPrivilege;
import org.oagi.score.service.common.data.TrackableImpl;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.*;
import org.oagi.score.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.*;
import static org.oagi.score.service.common.data.BCCEntityType.Attribute;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

@Repository
public class CcNodeRepository {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private ManifestRepository manifestRepository;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private UserRepository userRepository;

    private SelectOnConditionStep<Record15<ULong, String, String, ULong, Integer, String, String, String, ULong,
            UInteger, UInteger, ULong, String, ULong, ULong>> getSelectJoinStepForAccNode() {
        return dslContext.select(
                ACC.ACC_ID,
                ACC.GUID,
                ACC.DEN.as("name"),
                ACC_MANIFEST.BASED_ACC_MANIFEST_ID,
                ACC.OAGIS_COMPONENT_TYPE,
                ACC.OBJECT_CLASS_TERM,
                ACC.STATE,
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
                sessionService.getAppUser(user), sessionService.getAppUser(accNode.getOwnerUserId()),
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

        AppUser requester = sessionService.getAppUser(user);
        AppUser owner = sessionService.getAppUser(asccpNode.getOwnerUserId());
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

    public CcBccpNode getBccpNodeByBccpManifestId(AuthenticatedPrincipal user, BigInteger manifestId) {
        CcBccpNode bccpNode = selectOnConditionStepForBccpNode()
                .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(ULong.valueOf(manifestId)))
                .fetchOneInto(CcBccpNode.class);

        AppUser requester = sessionService.getAppUser(user);
        AppUser owner = sessionService.getAppUser(bccpNode.getOwnerUserId());
        boolean isWorkingRelease = bccpNode.getReleaseNum().equals("Working");
        bccpNode.setAccess(AccessPrivilege.toAccessPrivilege(requester, owner, bccpNode.getState(), isWorkingRelease));
        bccpNode.setHasChild(hasChild(bccpNode));

        return bccpNode;
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

    public List<? extends CcNode> getDescendants(AuthenticatedPrincipal user, CcAccNode accNode) {
        if (accNode == null) {
            return Collections.emptyList();
        }

        List<CcNode> descendants = new ArrayList();

        AccManifestRecord accManifestRecord = manifestRepository.getAccManifestById(
                ULong.valueOf(accNode.getManifestId()));

        BigInteger releaseId = accNode.getReleaseId();

        if (accManifestRecord.getBasedAccManifestId() != null) {
            CcAccNode basedAccNode = getAccNodeByAccManifestId(user, accManifestRecord.getBasedAccManifestId().toBigInteger());
            descendants.add(basedAccNode);
        }
        List<SeqKeySupportable> seqKeySupportableList = new ArrayList();
        seqKeySupportableList.addAll(
                getAsccpNodes(user, accManifestRecord.getAccManifestId().toBigInteger())
        );
        seqKeySupportableList.addAll(
                getBccpNodes(user, accManifestRecord.getAccManifestId().longValue())
        );
        seqKeySupportableList.sort(Comparator.comparingInt(SeqKeySupportable::getSeqKey));

        int seqKey = 1;
        for (SeqKeySupportable e : seqKeySupportableList) {
            if (e instanceof CcAsccpNode) {
                CcAsccpNode asccpNode = (CcAsccpNode) e;
                OagisComponentType oagisComponentType = getOagisComponentTypeByAccId(asccpNode.getRoleOfAccId());
                if (oagisComponentType.equals(OagisComponentType.UserExtensionGroup)) {
                    CcAccNode uegAccNode = getAccNodeByAccId(user, asccpNode.getRoleOfAccId(), releaseId);
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
        return dslContext.select(
                ACC.ACC_ID,
                ACC.GUID,
                ACC.OBJECT_CLASS_TERM,
                ACC.DEN,
                ACC.OAGIS_COMPONENT_TYPE.as("oagisComponentType"),
                ACC.IS_ABSTRACT.as("abstracted"),
                ACC.IS_DEPRECATED.as("deprecated"),
                ACC.DEFINITION,
                ACC.DEFINITION_SOURCE,
                ACC.NAMESPACE_ID,
                ACC_MANIFEST.ACC_MANIFEST_ID.as("manifest_id"),
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
    }

    public CcAsccpNodeDetail getAsccpNodeDetail(AuthenticatedPrincipal user, CcAsccpNode asccpNode) {
        CcAsccpNodeDetail asccpNodeDetail = new CcAsccpNodeDetail();

        BigInteger asccManifestId = asccpNode.getAsccManifestId();
        if (asccManifestId.longValue() > 0L) {
            CcAsccpNodeDetail.Ascc ascc = dslContext.select(
                    ASCC_MANIFEST.ASCC_MANIFEST_ID.as("manifest_id"),
                    ASCC.ASCC_ID,
                    ASCC.GUID,
                    ASCC.DEN,
                    ASCC.CARDINALITY_MIN,
                    ASCC.CARDINALITY_MAX,
                    ASCC.IS_DEPRECATED.as("deprecated"),
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
            asccpNodeDetail.setAscc(ascc);
        }

        BigInteger asccpManifestIdId = asccpNode.getManifestId();
        CcAsccpNodeDetail.Asccp asccp = dslContext.select(
                ASCCP_MANIFEST.ASCCP_MANIFEST_ID.as("manifest_id"),
                ASCCP.ASCCP_ID,
                ASCCP.GUID,
                ASCCP.PROPERTY_TERM,
                ASCCP.DEN,
                ASCCP.NAMESPACE_ID,
                ASCCP.REUSABLE_INDICATOR.as("reusable"),
                ASCCP.IS_DEPRECATED.as("deprecated"),
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
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(ULong.valueOf(asccpManifestIdId)))
                .fetchOneInto(CcAsccpNodeDetail.Asccp.class);
        asccpNodeDetail.setAsccp(asccp);

        return asccpNodeDetail;
    }

    public CcAsccpNodeDetail.Asccp getAsccp(BigInteger asccpId) {
        CcAsccpNodeDetail.Asccp asccp = dslContext.select(
                ASCCP.ASCCP_ID,
                ASCCP.DEN,
                ASCCP.PROPERTY_TERM,
                ASCCP.DEFINITION,
                ASCCP.GUID,
                ASCCP.ROLE_OF_ACC_ID)
                .from(ASCCP)
                .where(ASCCP.ASCCP_ID.eq(ULong.valueOf(asccpId)))
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
                    BCC.DEN,
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
            bccpNodeDetail.setBcc(bcc);
        }

        BigInteger bccpManifestId = bccpNode.getManifestId();
        CcBccpNodeDetail.Bccp bccp = dslContext.select(
                BCCP.BCCP_ID,
                BCCP.GUID,
                BCCP.PROPERTY_TERM,
                BCCP.DEN,
                BCCP.IS_NILLABLE.as("nillable"),
                BCCP.IS_DEPRECATED.as("deprecated"),
                BCCP.NAMESPACE_ID,
                BCCP.DEFAULT_VALUE,
                BCCP.FIXED_VALUE,
                BCCP.DEFINITION,
                BCCP.DEFINITION_SOURCE,
                BCCP_MANIFEST.BCCP_MANIFEST_ID.as("manifest_id"),
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
        bccpNodeDetail.setBccp(bccp);

        CcBccpNodeDetail.Bdt bdt = dslContext.select(
                DT.DT_ID.as("bdt_id"),
                DT_MANIFEST.DT_MANIFEST_ID.as("manifest_id"),
                DT.GUID,
                DT.DATA_TYPE_TERM,
                DT.QUALIFIER,
                DT.DEN,
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
        bccpNodeDetail.setBdt(bdt);

        int cardinalityMaxOfDtScListSum = dslContext.select(DT_SC.CARDINALITY_MAX)
                .from(BCCP_MANIFEST)
                .join(BCCP).on(BCCP_MANIFEST.BCCP_ID.eq(BCCP.BCCP_ID))
                .join(DT).on(BCCP.BDT_ID.eq(DT.DT_ID))
                .join(DT_SC).on(DT.DT_ID.eq(DT_SC.OWNER_DT_ID))
                .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(ULong.valueOf(bccpManifestId)))
                .fetchStreamInto(Integer.class).reduce(0, Integer::sum);
        bdt.setHasNoSc(cardinalityMaxOfDtScListSum == 0);

        return bccpNodeDetail;
    }

    public CcBdtScNodeDetail getBdtScNodeDetail(AuthenticatedPrincipal user, CcBdtScNode bdtScNode) {
        BigInteger manifestId = bdtScNode.getManifestId();
        return dslContext.select(
                DT_SC_MANIFEST.DT_SC_MANIFEST_ID.as("manifestId"),
                DT_SC.DT_SC_ID.as("bdt_sc_id"),
                DT_SC.GUID,
                concat(DT_SC.PROPERTY_TERM, val(". "), DT_SC.REPRESENTATION_TERM).as("den"),
                DT_SC.CARDINALITY_MIN,
                DT_SC.CARDINALITY_MAX,
                DT_SC.DEFINITION,
                DT_SC.DEFINITION_SOURCE,
                DT_SC.DEFAULT_VALUE,
                DT_SC.FIXED_VALUE,
                DT.STATE,
                APP_USER.LOGIN_ID.as("owner"),
                DT_MANIFEST.RELEASE_ID,
                RELEASE.RELEASE_NUM,
                DT_MANIFEST.LOG_ID,
                LOG.REVISION_NUM,
                LOG.REVISION_TRACKING_NUM)
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
                .where(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.eq(ULong.valueOf(manifestId)))
                .fetchOneInto(CcBdtScNodeDetail.class);
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
}