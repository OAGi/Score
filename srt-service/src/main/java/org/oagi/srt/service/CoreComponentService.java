package org.oagi.srt.service;

import org.oagi.srt.common.util.Utility;
import org.oagi.srt.provider.CoreComponentProvider;
import org.oagi.srt.repository.BusinessInformationEntityUserExtensionRevisionRepository;
import org.oagi.srt.repository.CoreComponentsRepository;
import org.oagi.srt.repository.TopLevelAbieRepository;
import org.oagi.srt.repository.entity.*;
import org.oagi.srt.repository.entity.listener.CreatorModifierAwareEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static org.oagi.srt.repository.entity.CoreComponentState.Published;
import static org.oagi.srt.repository.entity.RevisionAction.Insert;
import static org.oagi.srt.repository.entity.RevisionAction.Update;

@Service
@Transactional(readOnly = true)
public class CoreComponentService {

    @Autowired
    private TopLevelAbieRepository topLevelAbieRepository;

    @Autowired
    private BusinessInformationEntityUserExtensionRevisionRepository bieUserExtRevisionRepository;

    @Autowired
    private CoreComponentDAO ccDAO;

    @Autowired
    private BusinessInformationEntityDAO bieDAO;

    @Autowired
    private DataTypeDAO dtDAO;

    @Autowired
    private CoreComponentsRepository coreComponentsRepository;

    public List<CoreComponents> getCoreComponents(
            List<String> types, List<CoreComponentState> states, Sort.Order order) {
        return coreComponentsRepository.findAll(types, states, order);
    }

    public List<CoreComponentRelation> getCoreComponents(
            AggregateCoreComponent acc, CoreComponentProvider coreComponentProvider) {
        long accId = acc.getAccId();
        return getCoreComponents(accId, coreComponentProvider);
    }

    public List<CoreComponentRelation> getCoreComponents(
            long accId, CoreComponentProvider coreComponentProvider) {
        List<BasicCoreComponent> bcc_tmp_assoc = coreComponentProvider.getBCCs(accId);
        List<AssociationCoreComponent> ascc_tmp_assoc = coreComponentProvider.getASCCs(accId);

        List<CoreComponentRelation> coreComponents = gatheringBySeqKey(bcc_tmp_assoc, ascc_tmp_assoc);
        return coreComponents;
    }

    public List<CoreComponentRelation> getCoreComponentsWithoutAttributes(
            AggregateCoreComponent acc, CoreComponentProvider coreComponentProvider) {
        long accId = acc.getAccId();
        return getCoreComponentsWithoutAttributes(accId, coreComponentProvider);
    }

    public List<CoreComponentRelation> getCoreComponentsWithoutAttributes(
            long accId, CoreComponentProvider coreComponentProvider) {
        List<BasicCoreComponent> bcc_tmp_assoc = coreComponentProvider.getBCCsWithoutAttributes(accId);
        List<AssociationCoreComponent> ascc_tmp_assoc = coreComponentProvider.getASCCs(accId);

        List<CoreComponentRelation> coreComponents = gatheringBySeqKey(bcc_tmp_assoc, ascc_tmp_assoc);
        return coreComponents;
    }

    private List<CoreComponentRelation> gatheringBySeqKey(
            List<BasicCoreComponent> bccList, List<AssociationCoreComponent> asccList
    ) {
        int size = bccList.size() + asccList.size();
        List<CoreComponentRelation> tmp_assoc = new ArrayList(size);
        tmp_assoc.addAll(bccList);
        tmp_assoc.addAll(asccList);
        Collections.sort(tmp_assoc, (a, b) -> a.getSeqKey() - b.getSeqKey());

        List<CoreComponentRelation> coreComponents = new ArrayList(size);
        for (BasicCoreComponent basicCoreComponent : bccList) {
            if (BasicCoreComponentEntityType.Attribute == basicCoreComponent.getEntityType()) {
                coreComponents.add(basicCoreComponent);
            }
        }

        for (CoreComponentRelation coreComponent : tmp_assoc) {
            if (coreComponent instanceof BasicCoreComponent) {
                BasicCoreComponent basicCoreComponent = (BasicCoreComponent) coreComponent;
                if (BasicCoreComponentEntityType.Element == basicCoreComponent.getEntityType()) {
                    coreComponents.add(basicCoreComponent);
                }
            } else {
                AssociationCoreComponent associationCoreComponent = (AssociationCoreComponent) coreComponent;
                coreComponents.add(associationCoreComponent);
            }
        }

        return coreComponents;
    }

    @Transactional(rollbackFor = Throwable.class)
    public AggregateCoreComponent newAggregateCoreComponent(User user) {
        long requesterId = user.getAppUserId();

        AggregateCoreComponent acc = new AggregateCoreComponent();
        acc.setGuid(Utility.generateGUID());
        acc.setObjectClassTerm("A new ACC Object");
        acc.setOagisComponentType(OagisComponentType.Semantics);
        acc.setState(CoreComponentState.Editing);
        acc.setOwnerUserId(requesterId);

        CreatorModifierAwareEventListener eventListener = new CreatorModifierAwareEventListener(user);
        acc.addPersistEventListener(eventListener);

        acc = ccDAO.save(acc);

        AggregateCoreComponent accHistory = acc.clone(true);
        int revisionNum = 1;
        accHistory.setRevisionNum(revisionNum);
        int revisionTrackingNum = 1;
        accHistory.setRevisionTrackingNum(revisionTrackingNum);
        accHistory.setRevisionAction(Insert);
        accHistory.setCurrentAccId(acc.getAccId());

        ccDAO.save(accHistory);

        return acc;
    }

    @Transactional(rollbackFor = Throwable.class)
    public AggregateCoreComponent newAggregateCoreComponentRevision(User user, AggregateCoreComponent acc) {
        long requesterId = user.getAppUserId();
        if (acc.getOwnerUserId() != requesterId) {
            throw new IllegalArgumentException("Only allowed this operation by owner.");
        }
        acc.setState(CoreComponentState.Editing);

        CreatorModifierAwareEventListener eventListener = new CreatorModifierAwareEventListener(user);
        acc.addPersistEventListener(eventListener);

        acc = ccDAO.save(acc);

        AggregateCoreComponent accHistory = acc.clone(true);
        Long currentAccId = acc.getAccId();
        List<AggregateCoreComponent> latestHistoryAccList = ccDAO.findAccWithLatestRevisionNumByCurrentAccId(currentAccId);
        if (latestHistoryAccList.isEmpty()) {
            throw new IllegalStateException("There is no history for this element.");
        }

        int latestRevisionNum = latestHistoryAccList.stream()
                .mapToInt(e -> e.getRevisionNum())
                .max().orElse(0);
        accHistory.setRevisionNum(latestRevisionNum + 1);
        int revisionTrackingNum = 1;
        accHistory.setRevisionTrackingNum(revisionTrackingNum);
        accHistory.setRevisionAction(Insert);
        accHistory.setCurrentAccId(acc.getAccId());

        ccDAO.save(accHistory);

        return acc;
    }

    @Transactional(rollbackFor = Throwable.class)
    public AssociationCoreComponentProperty newAssociationCoreComponentProperty(User user, AggregateCoreComponent roleOfAcc) {
        long requesterId = user.getAppUserId();

        AssociationCoreComponentProperty asccp = new AssociationCoreComponentProperty();
        asccp.setGuid(Utility.generateGUID());
        asccp.setPropertyTerm("A new ASCCP property", roleOfAcc);
        asccp.setState(CoreComponentState.Editing);
        asccp.setReusableIndicator(true); // Default value should be true.
        asccp.setOwnerUserId(requesterId);
        long namespaceId = roleOfAcc.getNamespaceId();
        if (namespaceId > 0L) {
            asccp.setNamespaceId(namespaceId);
        }

        CreatorModifierAwareEventListener eventListener = new CreatorModifierAwareEventListener(user);
        asccp.addPersistEventListener(eventListener);

        asccp = ccDAO.save(asccp);

        AssociationCoreComponentProperty asccpHistory = asccp.clone(true);
        int revisionNum = 1;
        asccpHistory.setRevisionNum(revisionNum);
        int revisionTrackingNum = 1;
        asccpHistory.setRevisionTrackingNum(revisionTrackingNum);
        asccpHistory.setRevisionAction(Insert);
        asccpHistory.setCurrentAsccpId(asccp.getAsccpId());

        ccDAO.save(asccpHistory);

        return asccp;
    }

    @Transactional(rollbackFor = Throwable.class)
    public AssociationCoreComponentProperty newAssociationCoreComponentPropertyRevision(User user, AssociationCoreComponentProperty asccp) {
        long requesterId = user.getAppUserId();
        if (asccp.getOwnerUserId() != requesterId) {
            throw new IllegalArgumentException("Only allowed this operation by owner.");
        }
        asccp.setState(CoreComponentState.Editing);

        CreatorModifierAwareEventListener eventListener = new CreatorModifierAwareEventListener(user);
        asccp.addPersistEventListener(eventListener);

        asccp = ccDAO.save(asccp);

        AssociationCoreComponentProperty asccpHistory = asccp.clone(true);
        Long currentAsccpId = asccp.getAsccpId();
        List<AssociationCoreComponentProperty> latestHistoryAsccpList = ccDAO.findAsccpWithLatestRevisionNumByCurrentAsccpId(currentAsccpId);
        if (latestHistoryAsccpList.isEmpty()) {
            throw new IllegalStateException("There is no history for this element.");
        }

        int latestRevisionNum = latestHistoryAsccpList.stream()
                .mapToInt(e -> e.getRevisionNum())
                .max().orElse(0);
        asccpHistory.setRevisionNum(latestRevisionNum + 1);
        int revisionTrackingNum = 1;
        asccpHistory.setRevisionTrackingNum(revisionTrackingNum);
        asccpHistory.setRevisionAction(Insert);
        asccpHistory.setCurrentAsccpId(asccp.getAsccpId());

        ccDAO.save(asccpHistory);

        return asccp;
    }

    @Transactional(rollbackFor = Throwable.class)
    public BasicCoreComponentProperty newBasicCoreComponentProperty(User user, DataType bdt) {
        long requesterId = user.getAppUserId();

        BasicCoreComponentProperty bccp = new BasicCoreComponentProperty();
        bccp.setGuid(Utility.generateGUID());
        String propertyTerm = "A new BCCP property";
        bccp.setPropertyTerm(propertyTerm);
        bccp.setRepresentationTerm(bdt.getDataTypeTerm());
        bccp.setBdtId(bdt.getDtId());
        bccp.setDen(Utility.firstToUpperCase(propertyTerm) + ". " + bdt.getDataTypeTerm());
        bccp.setState(CoreComponentState.Editing);
        bccp.setOwnerUserId(requesterId);

        CreatorModifierAwareEventListener eventListener = new CreatorModifierAwareEventListener(user);
        bccp.addPersistEventListener(eventListener);

        bccp = ccDAO.save(bccp);

        BasicCoreComponentProperty bccpHistory = bccp.clone(true);
        int revisionNum = 1;
        bccpHistory.setRevisionNum(revisionNum);
        int revisionTrackingNum = 1;
        bccpHistory.setRevisionTrackingNum(revisionTrackingNum);
        bccpHistory.setRevisionAction(Insert);
        bccpHistory.setCurrentBccpId(bccp.getBccpId());

        ccDAO.save(bccpHistory);

        return bccp;
    }

    @Transactional(rollbackFor = Throwable.class)
    public BasicCoreComponentProperty newBasicCoreComponentPropertyRevision(User user, BasicCoreComponentProperty bccp) {
        long requesterId = user.getAppUserId();
        if (bccp.getOwnerUserId() != requesterId) {
            throw new IllegalArgumentException("Only allowed this operation by owner.");
        }
        bccp.setState(CoreComponentState.Editing);

        CreatorModifierAwareEventListener eventListener = new CreatorModifierAwareEventListener(user);
        bccp.addPersistEventListener(eventListener);

        bccp = ccDAO.save(bccp);

        BasicCoreComponentProperty bccpHistory = bccp.clone(true);
        Long currentBccpId = bccp.getBccpId();
        List<BasicCoreComponentProperty> latestHistoryBccpList = ccDAO.findBccpWithLatestRevisionNumByCurrentBccpId(currentBccpId);
        if (latestHistoryBccpList.isEmpty()) {
            throw new IllegalStateException("There is no history for this element.");
        }

        int latestRevisionNum = latestHistoryBccpList.stream()
                .mapToInt(e -> e.getRevisionNum())
                .max().orElse(0);
        bccpHistory.setRevisionNum(latestRevisionNum + 1);
        int revisionTrackingNum = 1;
        bccpHistory.setRevisionTrackingNum(revisionTrackingNum);
        bccpHistory.setRevisionAction(Insert);
        bccpHistory.setCurrentBccpId(bccp.getBccpId());

        ccDAO.save(bccpHistory);

        return bccp;
    }

    @Transactional(rollbackFor = Throwable.class)
    public void update(AggregateCoreComponent acc, User requester) {
        long requesterId = requester.getAppUserId();
        long ownerId = acc.getOwnerUserId();
        if (requesterId != ownerId) {
            throw new PermissionDeniedDataAccessException(
                    "This operation only allows for the owner of this element.", new IllegalArgumentException());
        }
        long currentAccId = acc.getAccId();
        List<AggregateCoreComponent> latestHistoryAccList = ccDAO.findAccWithLatestRevisionNumByCurrentAccId(currentAccId);
        if (latestHistoryAccList.isEmpty()) {
            throw new IllegalStateException("There is no history for this element.");
        }

        ccDAO.save(acc);

        int latestRevisionTrackingNum = latestHistoryAccList.stream()
                .mapToInt(e -> e.getRevisionTrackingNum())
                .max().orElse(0);
        int nextRevisionTrackingNum = latestRevisionTrackingNum + 1;
        AggregateCoreComponent accHistory = acc.clone(true);
        accHistory.setRevisionNum(latestHistoryAccList.get(0).getRevisionNum());
        accHistory.setRevisionTrackingNum(nextRevisionTrackingNum);
        accHistory.setRevisionAction(Update);
        accHistory.setLastUpdatedBy(requesterId);
        accHistory.setCurrentAccId(currentAccId);

        ccDAO.save(accHistory);

        // to check RevisionTrackingNum
        latestHistoryAccList = ccDAO.findAccWithLatestRevisionNumByCurrentAccId(currentAccId);
        int actualRevisionTrackingNum = latestHistoryAccList.stream()
                .mapToInt(e -> e.getRevisionTrackingNum())
                .max().orElse(0);
        if (actualRevisionTrackingNum != nextRevisionTrackingNum) {
            throw new ConcurrentModificationException("AggregateCoreComponent was modified outside of this operation");
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void updateState(AggregateCoreComponent acc,
                            CoreComponentState state,
                            User requester) {
        if (acc.getOwnerUserId() != requester.getAppUserId()) {
            throw new PermissionDeniedDataAccessException(
                    "This operation only allows for the owner of this element.", new IllegalArgumentException());
        }

        updateAccState(acc, state, requester);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void updateState(AssociationCoreComponentProperty asccp,
                            CoreComponentState state,
                            User requester) {
        if (asccp.getOwnerUserId() != requester.getAppUserId()) {
            throw new PermissionDeniedDataAccessException(
                    "This operation only allows for the owner of this element.", new IllegalArgumentException());
        }

        updateAsccpState(asccp, state, requester);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void updateState(BasicCoreComponentProperty bccp,
                            CoreComponentState state,
                            User requester) {
        if (bccp.getOwnerUserId() != requester.getAppUserId()) {
            throw new PermissionDeniedDataAccessException(
                    "This operation only allows for the owner of this element.", new IllegalArgumentException());
        }

        updateBccpState(bccp, state, requester);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void discard(AggregateCoreComponent acc, User requester) {
        long requesterId = requester.getAppUserId();
        long ownerId = acc.getOwnerUserId();
        if (requesterId != ownerId) {
            throw new PermissionDeniedDataAccessException(
                    "This operation only allows for the owner of this element.", new IllegalArgumentException());
        }
        long currentAccId = acc.getAccId();
        List<AggregateCoreComponent> latestHistoryAccList = ccDAO.findAccWithLatestRevisionNumByCurrentAccId(currentAccId);
        if (latestHistoryAccList.isEmpty()) {
            throw new IllegalStateException("There is no history for this element.");
        }

        if (!ccDAO.findAsccpByRoleOfAccId(currentAccId).isEmpty()) {
            throw new IllegalStateException("Not allowed to discard the ACC which has related with ASCCP");
        }
        if (!bieDAO.findAbieByBasedAccId(currentAccId).isEmpty()) {
            throw new IllegalStateException("Not allowed to discard the ACC which has related with ABIE");
        }

        long accId = acc.getAccId();
        ccDAO.deleteByCurrentAccId(accId); // To remove history
        ccDAO.delete(acc);

        ccDAO.deleteAsccByFromAccId(accId);
        ccDAO.deleteBccByFromAccId(accId);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void update(AssociationCoreComponent ascc, User requester) {
        long requesterId = requester.getAppUserId();
        long ownerId = ascc.getOwnerUserId();
        if (requesterId != ownerId) {
            throw new PermissionDeniedDataAccessException(
                    "This operation only allows for the owner of this element.", new IllegalArgumentException());
        }
        long currentAsccId = ascc.getAsccId();
        List<AssociationCoreComponent> latestHistoryAsccList = ccDAO.findAsccWithLatestRevisionNumByCurrentAsccId(currentAsccId);
        if (latestHistoryAsccList == null) {
            throw new IllegalStateException("There is no history for this element.");
        }

        ccDAO.save(ascc);

        int latestRevisionTrackingNum = latestHistoryAsccList.stream()
                .mapToInt(e -> e.getRevisionTrackingNum())
                .max().orElse(0);
        int nextRevisionTrackingNum = latestRevisionTrackingNum + 1;
        AssociationCoreComponent asccHistory = ascc.clone(true);
        asccHistory.setRevisionNum(latestHistoryAsccList.get(0).getRevisionNum());
        asccHistory.setRevisionTrackingNum(nextRevisionTrackingNum);
        asccHistory.setRevisionAction(Update);
        asccHistory.setLastUpdatedBy(requesterId);
        asccHistory.setCurrentAsccId(currentAsccId);

        ccDAO.save(asccHistory);

        // to check RevisionTrackingNum
        latestHistoryAsccList = ccDAO.findAsccWithLatestRevisionNumByCurrentAsccId(currentAsccId);
        int actualRevisionTrackingNum = latestHistoryAsccList.stream()
                .mapToInt(e -> e.getRevisionTrackingNum())
                .max().orElse(0);
        if (actualRevisionTrackingNum != nextRevisionTrackingNum) {
            throw new ConcurrentModificationException("AssociationCoreComponent was modified outside of this operation");
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void discard(AssociationCoreComponent ascc, User requester) {
        long requesterId = requester.getAppUserId();
        long ownerId = ascc.getOwnerUserId();
        if (requesterId != ownerId) {
            throw new PermissionDeniedDataAccessException(
                    "This operation only allows for the owner of this element.", new IllegalArgumentException());
        }
        long currentAsccId = ascc.getAsccId();
        List<AssociationCoreComponent> latestHistoryAsccList = ccDAO.findAsccWithLatestRevisionNumByCurrentAsccId(currentAsccId);
        if (latestHistoryAsccList == null) {
            throw new IllegalStateException("There is no history for this element.");
        }

        long asccId = ascc.getAsccId();
        ccDAO.deleteByCurrentAsccId(asccId); // To remove history
        int seqKey = ascc.getSeqKey();
        ccDAO.delete(ascc);

        long fromAccId = ascc.getFromAccId();
        ccDAO.decreaseSeqKeyByFromAccIdAndSeqKeyGreaterThan(fromAccId, seqKey);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void update(BasicCoreComponent bcc, User requester) {
        long requesterId = requester.getAppUserId();
        long ownerId = bcc.getOwnerUserId();
        if (requesterId != ownerId) {
            throw new PermissionDeniedDataAccessException(
                    "This operation only allows for the owner of this element.", new IllegalArgumentException());
        }
        long currentBccId = bcc.getBccId();
        List<BasicCoreComponent> latestHistoryBccList = ccDAO.findBccWithLatestRevisionNumByCurrentBccId(currentBccId);
        if (latestHistoryBccList.isEmpty()) {
            throw new IllegalStateException("There is no history for this element.");
        }

        int seqKey;
        long fromAccId = bcc.getFromAccId();

        // Only if EntityType property would be changed
        switch (bcc.getEntityType()) {
            case Element:
                if (bcc.getSeqKey() == 0) {
                    seqKey = findAppropriateSeqKey(bcc);
                    bcc.setSeqKey(seqKey);

                    ccDAO.increaseSeqKeyByFromAccIdAndSeqKeyGreaterThan(fromAccId, seqKey - 1);
                }

                break;
            case Attribute:
                if (bcc.getSeqKey() > 0) {
                    seqKey = bcc.getSeqKey();
                    bcc.setSeqKey(0);

                    ccDAO.decreaseSeqKeyByFromAccIdAndSeqKeyGreaterThan(fromAccId, seqKey);
                }

                break;
        }

        ccDAO.save(bcc);

        int latestRevisionTrackingNum = latestHistoryBccList.stream()
                .mapToInt(e -> e.getRevisionTrackingNum())
                .max().orElse(0);
        int nextRevisionTrackingNum = latestRevisionTrackingNum + 1;
        BasicCoreComponent bccHistory = bcc.clone(true);
        bccHistory.setRevisionNum(latestHistoryBccList.get(0).getRevisionNum());
        bccHistory.setRevisionTrackingNum(nextRevisionTrackingNum);
        bccHistory.setRevisionAction(Update);
        bccHistory.setLastUpdatedBy(requesterId);
        bccHistory.setCurrentBccId(currentBccId);

        ccDAO.save(bccHistory);

        // to check RevisionTrackingNum
        latestHistoryBccList = ccDAO.findBccWithLatestRevisionNumByCurrentBccId(currentBccId);
        int actualRevisionTrackingNum = latestHistoryBccList.stream()
                .mapToInt(e -> e.getRevisionTrackingNum())
                .max().orElse(0);
        if (actualRevisionTrackingNum != nextRevisionTrackingNum) {
            throw new ConcurrentModificationException("BasicCoreComponent was modified outside of this operation");
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void discard(BasicCoreComponent bcc, User requester) {
        long requesterId = requester.getAppUserId();
        long ownerId = bcc.getOwnerUserId();
        if (requesterId != ownerId) {
            throw new PermissionDeniedDataAccessException(
                    "This operation only allows for the owner of this element.", new IllegalArgumentException());
        }
        long currentBccId = bcc.getBccId();
        List<BasicCoreComponent> latestHistoryBccList = ccDAO.findBccWithLatestRevisionNumByCurrentBccId(currentBccId);
        if (latestHistoryBccList.isEmpty()) {
            throw new IllegalStateException("There is no history for this element.");
        }

        long bccId = bcc.getBccId();
        ccDAO.deleteByCurrentBccId(bccId); // To remove history
        int seqKey = bcc.getSeqKey();
        ccDAO.delete(bcc);

        if (seqKey > 0) {
            long fromAccId = bcc.getFromAccId();
            ccDAO.decreaseSeqKeyByFromAccIdAndSeqKeyGreaterThan(fromAccId, seqKey);
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void update(AssociationCoreComponentProperty asccp, User requester) {
        long requesterId = requester.getAppUserId();
        long ownerId = asccp.getOwnerUserId();
        if (requesterId != ownerId) {
            throw new PermissionDeniedDataAccessException(
                    "This operation only allows for the owner of this element.", new IllegalArgumentException());
        }
        long currentAsccpId = asccp.getAsccpId();
        List<AssociationCoreComponentProperty> latestHistoryAsccpList =
                ccDAO.findAsccpWithLatestRevisionNumByCurrentAsccpId(currentAsccpId);
        if (latestHistoryAsccpList == null) {
            throw new IllegalStateException("There is no history for this element.");
        }

        ccDAO.save(asccp);
        ccDAO.findAsccByToAsccpId(asccp.getAsccpId()).forEach(e -> {
            AggregateCoreComponent acc = ccDAO.findAcc(e.getFromAccId());
            e.setDen(acc, asccp);
            ccDAO.save(e);
        });

        int latestRevisionTrackingNum = latestHistoryAsccpList.stream()
                .mapToInt(e -> e.getRevisionTrackingNum())
                .max().orElse(0);
        int nextRevisionTrackingNum = latestRevisionTrackingNum + 1;
        AssociationCoreComponentProperty asccpHistory = asccp.clone(true);
        asccpHistory.setRevisionNum(latestHistoryAsccpList.get(0).getRevisionNum());
        asccpHistory.setRevisionTrackingNum(nextRevisionTrackingNum);
        asccpHistory.setRevisionAction(Update);
        asccpHistory.setLastUpdatedBy(requesterId);
        asccpHistory.setCurrentAsccpId(currentAsccpId);

        ccDAO.save(asccpHistory);

        // to check RevisionTrackingNum
        latestHistoryAsccpList = ccDAO.findAsccpWithLatestRevisionNumByCurrentAsccpId(currentAsccpId);
        int actualRevisionTrackingNum = latestHistoryAsccpList.stream()
                .mapToInt(e -> e.getRevisionTrackingNum())
                .max().orElse(0);
        if (actualRevisionTrackingNum != nextRevisionTrackingNum) {
            throw new ConcurrentModificationException("AssociationCoreComponentProperty was modified outside of this operation");
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void discard(AssociationCoreComponentProperty asccp, User requester) {
        long requesterId = requester.getAppUserId();
        long ownerId = asccp.getOwnerUserId();
        if (requesterId != ownerId) {
            throw new PermissionDeniedDataAccessException(
                    "This operation only allows for the owner of this element.", new IllegalArgumentException());
        }
        long currentAsccpId = asccp.getAsccpId();
        List<AssociationCoreComponentProperty> latestHistoryAsccpList =
                ccDAO.findAsccpWithLatestRevisionNumByCurrentAsccpId(currentAsccpId);
        if (latestHistoryAsccpList == null) {
            throw new IllegalStateException("There is no history for this element.");
        }

        long asccpId = asccp.getAsccpId();
        ccDAO.deleteByCurrentAsccpId(asccpId); // To remove history
        ccDAO.delete(asccp);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void update(BasicCoreComponentProperty bccp, User requester) {
        long requesterId = requester.getAppUserId();
        long ownerId = bccp.getOwnerUserId();
        if (requesterId != ownerId) {
            throw new PermissionDeniedDataAccessException(
                    "This operation only allows for the owner of this element.", new IllegalArgumentException());
        }
        long currentBccpId = bccp.getBccpId();
        List<BasicCoreComponentProperty> latestHistoryBccpList =
                ccDAO.findBccpWithLatestRevisionNumByCurrentBccpId(currentBccpId);
        if (latestHistoryBccpList.isEmpty()) {
            throw new IllegalStateException("There is no history for this element.");
        }

        ccDAO.save(bccp);
        ccDAO.findBccByToBccpId(bccp.getBccpId()).forEach(e -> {
            AggregateCoreComponent acc = ccDAO.findAcc(e.getFromAccId());
            e.setDen(acc, bccp);
            ccDAO.save(e);
        });

        int latestRevisionTrackingNum = latestHistoryBccpList.stream()
                .mapToInt(e -> e.getRevisionTrackingNum())
                .max().orElse(0);
        int nextRevisionTrackingNum = latestRevisionTrackingNum + 1;
        BasicCoreComponentProperty bccpHistory = bccp.clone(true);
        bccpHistory.setRevisionNum(latestHistoryBccpList.get(0).getRevisionNum());
        bccpHistory.setRevisionTrackingNum(nextRevisionTrackingNum);
        bccpHistory.setRevisionAction(Update);
        bccpHistory.setLastUpdatedBy(requesterId);
        bccpHistory.setCurrentBccpId(currentBccpId);

        ccDAO.save(bccpHistory);

        // to check RevisionTrackingNum
        latestHistoryBccpList = ccDAO.findBccpWithLatestRevisionNumByCurrentBccpId(currentBccpId);
        int actualRevisionTrackingNum = latestHistoryBccpList.stream()
                .mapToInt(e -> e.getRevisionTrackingNum())
                .max().orElse(0);
        if (actualRevisionTrackingNum != nextRevisionTrackingNum) {
            throw new ConcurrentModificationException("BasicCoreComponentProperty was modified outside of this operation");
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void discard(BasicCoreComponentProperty bccp, User requester) {
        long requesterId = requester.getAppUserId();
        long ownerId = bccp.getOwnerUserId();
        if (requesterId != ownerId) {
            throw new PermissionDeniedDataAccessException(
                    "This operation only allows for the owner of this element.", new IllegalArgumentException());
        }
        long currentBccpId = bccp.getBccpId();
        List<BasicCoreComponentProperty> latestHistoryBccpList = ccDAO.findBccpWithLatestRevisionNumByCurrentBccpId(currentBccpId);
        if (latestHistoryBccpList.isEmpty()) {
            throw new IllegalStateException("There is no history for this element.");
        }

        long bccpId = bccp.getBccpId();
        ccDAO.deleteByCurrentBccpId(bccpId); // To remove history
        ccDAO.delete(bccp);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void discard(CoreComponents coreComponents, User requester) {
        switch(coreComponents.getType()) {
            case "ACC":
                AggregateCoreComponent acc = ccDAO.findAcc(coreComponents.getId());
                discard(acc, requester);
                break;

            case "ASCCP":
                AssociationCoreComponentProperty asccp = ccDAO.findAsccp(coreComponents.getId());
                discard(asccp, requester);
                break;

            case "BCCP":
                BasicCoreComponentProperty bccp = ccDAO.findBccp(coreComponents.getId());
                discard(bccp, requester);
                break;

            default:
                throw new UnsupportedOperationException();
        }
    }

    private int findAppropriateSeqKey(BasicCoreComponent latestBcc) {
        long bccId = latestBcc.getBccId();
        List<BasicCoreComponent> latestHistoryBccList = ccDAO.findBccWithLatestRevisionNumByCurrentBccId(bccId);
        return latestHistoryBccList.stream()
                .mapToInt(e -> e.getSeqKey())
                .max().orElseGet(() -> 0);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void updateState(AggregateCoreComponent eAcc,
                            AggregateCoreComponent ueAcc,
                            CoreComponentState state,
                            User requester) {
        if (ueAcc.getOwnerUserId() != requester.getAppUserId()) {
            throw new PermissionDeniedDataAccessException(
                    "This operation only allows for the owner of this element.", new IllegalArgumentException());
        }

        updateAccState(ueAcc, state, requester);

        if (state == Published) {
            storeBieUserExtRevisions(eAcc, ueAcc);
        }
    }

    private void updateChildrenState(AggregateCoreComponent acc,
                                     CoreComponentState state,
                                     User requester) {
        long fromAccId = acc.getAccId();
        long lastUpdatedBy = requester.getAppUserId();

        List<AssociationCoreComponent> asccList = ccDAO.findAsccByFromAccIdAndRevisionNumIsZero(fromAccId);
        for (AssociationCoreComponent ascc : asccList) {
            if (ascc.getState() != Published) {
                ascc.setState(state);
                ascc.setLastUpdatedBy(lastUpdatedBy);
            }
        }
        ccDAO.saveAsccList(asccList.stream()
                .filter(e -> e.isDirty()).collect(Collectors.toList()));

        List<BasicCoreComponent> bccList = ccDAO.findBccByFromAccIdAndRevisionNumIsZero(fromAccId);
        for (BasicCoreComponent bcc : bccList) {
            if (bcc.getState() != Published) {
                bcc.setState(state);
                bcc.setLastUpdatedBy(lastUpdatedBy);
            }
        }
        ccDAO.saveBccList(bccList.stream()
                .filter(e -> e.isDirty()).collect(Collectors.toList()));
    }

    private void updateAccState(AggregateCoreComponent acc,
                                CoreComponentState state,
                                User requester) {
        long lastUpdatedBy = requester.getAppUserId();

        if (acc.getState() != Published) {
            acc.setState(state);
            acc.setLastUpdatedBy(lastUpdatedBy);
            ccDAO.save(acc);
        }

        updateChildrenState(acc, state, requester);
    }

    private void updateAsccpState(AssociationCoreComponentProperty asccp,
                                  CoreComponentState state,
                                  User requester) {
        if (asccp == null) {
            return;
        }

        long lastUpdatedBy = requester.getAppUserId();

        if (asccp.getState() != Published) {
            asccp.setState(state);
            asccp.setLastUpdatedBy(lastUpdatedBy);
            ccDAO.save(asccp);
        }
    }

    private void updateBccpState(BasicCoreComponentProperty bccp,
                                 CoreComponentState state,
                                 User requester) {
        if (bccp == null) {
            return;
        }

        long lastUpdatedBy = requester.getAppUserId();

        if (bccp.getState() != Published) {
            bccp.setState(state);
            bccp.setLastUpdatedBy(lastUpdatedBy);
            ccDAO.save(bccp);
        }
    }

    private void storeBieUserExtRevisions(AggregateCoreComponent eAcc, AggregateCoreComponent ueAcc) {
        if (!eAcc.isExtension()) {
            return;
        }

        List<TopLevelAbie> topLevelAbies = topLevelAbieRepository.findAll().stream()
                .filter(e -> e.getState() != AggregateBusinessInformationEntityState.Published)
                .collect(Collectors.toList());

        List<BusinessInformationEntityUserExtensionRevision> bieUserExtRevisionList = new ArrayList();
        for (TopLevelAbie topLevelAbie : topLevelAbies) {
            boolean hasExtensionAsccpAsChild = hasExtensionAsccpAsChild(topLevelAbie, eAcc);
            if (hasExtensionAsccpAsChild) {
                BusinessInformationEntityUserExtensionRevision bieUserExtRevision =
                        new BusinessInformationEntityUserExtensionRevision();
                bieUserExtRevision.setTopLevelAbie(topLevelAbie);
                bieUserExtRevision.setExtAcc(eAcc);
                bieUserExtRevision.setUserExtAcc(ueAcc);
                bieUserExtRevision.setRevisedIndicator(false);
                bieUserExtRevisionList.add(bieUserExtRevision);
            }
        }

        bieUserExtRevisionRepository.save(bieUserExtRevisionList);
    }

    private boolean hasExtensionAsccpAsChild(TopLevelAbie topLevelAbie, AggregateCoreComponent eAcc) {
        AggregateBusinessInformationEntity abie = topLevelAbie.getAbie();
        long basedAccId = abie.getBasedAccId();
        long eAccId = eAcc.getAccId();

        return existsASCCPRecursivelyByRoleOfAccId(basedAccId, eAccId);
    }

    private boolean existsASCCPRecursivelyByRoleOfAccId(long basedAccId, long eAccId) {
        Collection<Long> fromAccIds = Arrays.asList(basedAccId);

        while (true) {
            List<Long> toAsccpId = ccDAO.findAsccToAsccpIdByFromAccId(fromAccIds);
            List<Long> roleOfAccIdList =
                    !toAsccpId.isEmpty() ? ccDAO.findAsccpRoleOfAccIdByAsccpId(toAsccpId) : Collections.emptyList();
            if (roleOfAccIdList.isEmpty()) {
                return false;
            }

            if (roleOfAccIdList.contains(eAccId)) {
                return true;
            }

            List<Long> tempAccIds = new ArrayList();
            roleOfAccIdList.forEach(accId -> {
                while (accId != null && accId > 0L) {
                    tempAccIds.add(accId);
                    accId = ccDAO.findAccBasedAccIdByAccId(accId);
                }
            });
            fromAccIds = tempAccIds;
        }
    }

    public int getMaxSeqKeyOfChildren(AggregateCoreComponent acc) {
        long accId = acc.getAccId();

        return Math.max(
                ccDAO.findAsccByFromAccIdAndRevisionNumIsZero(accId).stream()
                        .mapToInt(e -> e.getSeqKey()).max().orElse(0),
                ccDAO.findBccByFromAccIdAndRevisionNumIsZero(accId).stream()
                        .mapToInt(e -> e.getSeqKey()).max().orElse(0)
        );
    }

    public int findOriginalCardinalityMin(AssociationBusinessInformationEntity asbie) {
        long basedAsccId = asbie.getBasedAsccId();
        AssociationCoreComponent ascc = ccDAO.findAscc(basedAsccId);
        return ascc.getCardinalityMin();
    }

    public int findOriginalCardinalityMax(AssociationBusinessInformationEntity asbie) {
        long basedAsccId = asbie.getBasedAsccId();
        AssociationCoreComponent ascc = ccDAO.findAscc(basedAsccId);
        return ascc.getCardinalityMax();
    }

    public int findOriginalCardinalityMin(BasicBusinessInformationEntity bbie) {
        long basedBccId = bbie.getBasedBccId();
        BasicCoreComponent bcc = ccDAO.findBcc(basedBccId);
        return bcc.getCardinalityMin();
    }

    public int findOriginalCardinalityMax(BasicBusinessInformationEntity bbie) {
        long basedBccId = bbie.getBasedBccId();
        BasicCoreComponent bcc = ccDAO.findBcc(basedBccId);
        return bcc.getCardinalityMax();
    }

    public int findOriginalCardinalityMin(BasicBusinessInformationEntitySupplementaryComponent bbieSc) {
        long dtScId = bbieSc.getDtScId();
        DataTypeSupplementaryComponent dtSc = dtDAO.findDtSc(dtScId);
        return dtSc.getCardinalityMin();
    }

    public int findOriginalCardinalityMax(BasicBusinessInformationEntitySupplementaryComponent bbieSc) {
        long dtScId = bbieSc.getDtScId();
        DataTypeSupplementaryComponent dtSc = dtDAO.findDtSc(dtScId);
        return dtSc.getCardinalityMax();
    }

    public void transferOwner(AggregateCoreComponent acc, User newOwner) {
        long oldOwnerId = acc.getOwnerUserId();
        long newOwnerId = newOwner.getAppUserId();

        if (oldOwnerId == newOwnerId) {
            return;
        }

        acc.setOwnerUserId(newOwnerId);
        ccDAO.save(acc);
    }
}
