package org.oagi.srt.service;

import org.oagi.srt.common.util.Utility;
import org.oagi.srt.provider.CoreComponentProvider;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.oagi.srt.repository.entity.listener.CreatorModifierAwareEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.PermissionDeniedDataAccessException;
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
    private AggregateCoreComponentRepository accRepository;

    @Autowired
    private AssociationCoreComponentRepository asccRepository;

    @Autowired
    private AssociationCoreComponentPropertyRepository asccpRepository;

    @Autowired
    private BasicCoreComponentRepository bccRepository;

    @Autowired
    private BasicCoreComponentPropertyRepository bccpRepository;

    @Autowired
    private DataTypeSupplementaryComponentRepository dtScRepository;

    @Autowired
    private TopLevelAbieRepository topLevelAbieRepository;

    @Autowired
    private BusinessInformationEntityUserExtensionRevisionRepository bieUserExtRevisionRepository;

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

        acc = accRepository.saveAndFlush(acc);

        AggregateCoreComponent accHistory = acc.clone();
        int revisionNum = 1;
        accHistory.setRevisionNum(revisionNum);
        int revisionTrackingNum = 1;
        accHistory.setRevisionTrackingNum(revisionTrackingNum);
        accHistory.setRevisionAction(Insert);
        accHistory.setCurrentAccId(acc.getAccId());

        accRepository.save(accHistory);

        return acc;
    }

    @Transactional(rollbackFor = Throwable.class)
    public AssociationCoreComponentProperty newAssociationCoreComponentProperty(User user, AggregateCoreComponent roleOfAcc) {
        long requesterId = user.getAppUserId();

        AssociationCoreComponentProperty asccp = new AssociationCoreComponentProperty();
        asccp.setGuid(Utility.generateGUID());
        asccp.setPropertyTerm("A new ASCCP property");
        asccp.setRoleOfAccId(roleOfAcc.getAccId());
        asccp.setState(CoreComponentState.Editing);
        asccp.setOwnerUserId(requesterId);
        long namespaceId = roleOfAcc.getNamespaceId();
        if (namespaceId > 0L) {
            asccp.setNamespaceId(namespaceId);
        }

        CreatorModifierAwareEventListener eventListener = new CreatorModifierAwareEventListener(user);
        asccp.addPersistEventListener(eventListener);

        asccp = asccpRepository.saveAndFlush(asccp);

        AssociationCoreComponentProperty asccpHistory = asccp.clone();
        int revisionNum = 1;
        asccpHistory.setRevisionNum(revisionNum);
        int revisionTrackingNum = 1;
        asccpHistory.setRevisionTrackingNum(revisionTrackingNum);
        asccpHistory.setRevisionAction(Insert);
        asccpHistory.setCurrentAsccpId(asccp.getAsccpId());

        asccpRepository.save(asccpHistory);

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

        bccp = bccpRepository.saveAndFlush(bccp);

        BasicCoreComponentProperty bccpHistory = bccp.clone();
        int revisionNum = 1;
        bccpHistory.setRevisionNum(revisionNum);
        int revisionTrackingNum = 1;
        bccpHistory.setRevisionTrackingNum(revisionTrackingNum);
        bccpHistory.setRevisionAction(Insert);
        bccpHistory.setCurrentBccpId(bccp.getBccpId());

        bccpRepository.save(bccpHistory);

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
        List<AggregateCoreComponent> latestHistoryAccList = accRepository.findAllWithLatestRevisionNumByCurrentAccId(currentAccId);
        if (latestHistoryAccList.isEmpty()) {
            throw new IllegalStateException("There is no history for this element.");
        }

        accRepository.save(acc);

        int latestRevisionTrackingNum = latestHistoryAccList.stream()
                .mapToInt(e -> e.getRevisionTrackingNum())
                .max().getAsInt();
        int nextRevisionTrackingNum = latestRevisionTrackingNum + 1;
        AggregateCoreComponent accHistory = acc.clone();
        accHistory.setRevisionNum(latestHistoryAccList.get(0).getRevisionNum());
        accHistory.setRevisionTrackingNum(nextRevisionTrackingNum);
        accHistory.setRevisionAction(Update);
        accHistory.setLastUpdatedBy(requesterId);
        accHistory.setCurrentAccId(currentAccId);

        accRepository.saveAndFlush(accHistory);

        // to check RevisionTrackingNum
        latestHistoryAccList = accRepository.findAllWithLatestRevisionNumByCurrentAccId(currentAccId);
        int actualRevisionTrackingNum = latestHistoryAccList.stream()
                .mapToInt(e -> e.getRevisionTrackingNum())
                .max().getAsInt();
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

        updateChildrenState(acc, state, requester);
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
        List<AggregateCoreComponent> latestHistoryAccList = accRepository.findAllWithLatestRevisionNumByCurrentAccId(currentAccId);
        if (latestHistoryAccList.isEmpty()) {
            throw new IllegalStateException("There is no history for this element.");
        }

        long accId = acc.getAccId();
        accRepository.deleteByCurrentAccId(accId); // To remove history
        accRepository.delete(acc);
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
        List<AssociationCoreComponent> latestHistoryAsccList = asccRepository.findAllWithLatestRevisionNumByCurrentAsccId(currentAsccId);
        if (latestHistoryAsccList == null) {
            throw new IllegalStateException("There is no history for this element.");
        }

        asccRepository.save(ascc);

        int latestRevisionTrackingNum = latestHistoryAsccList.stream()
                .mapToInt(e -> e.getRevisionTrackingNum())
                .max().getAsInt();
        int nextRevisionTrackingNum = latestRevisionTrackingNum + 1;
        AssociationCoreComponent asccHistory = ascc.clone();
        asccHistory.setRevisionNum(latestHistoryAsccList.get(0).getRevisionNum());
        asccHistory.setRevisionTrackingNum(nextRevisionTrackingNum);
        asccHistory.setRevisionAction(Update);
        asccHistory.setLastUpdatedBy(requesterId);
        asccHistory.setCurrentAsccId(currentAsccId);

        asccRepository.saveAndFlush(asccHistory);

        // to check RevisionTrackingNum
        latestHistoryAsccList = asccRepository.findAllWithLatestRevisionNumByCurrentAsccId(currentAsccId);
        int actualRevisionTrackingNum = latestHistoryAsccList.stream()
                .mapToInt(e -> e.getRevisionTrackingNum())
                .max().getAsInt();
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
        List<AssociationCoreComponent> latestHistoryAsccList = asccRepository.findAllWithLatestRevisionNumByCurrentAsccId(currentAsccId);
        if (latestHistoryAsccList == null) {
            throw new IllegalStateException("There is no history for this element.");
        }

        long asccId = ascc.getAsccId();
        asccRepository.deleteByCurrentAsccId(asccId); // To remove history
        int seqKey = ascc.getSeqKey();
        asccRepository.delete(ascc);

        long fromAccId = ascc.getFromAccId();
        decreaseSeqKeyGreaterThan(fromAccId, seqKey);
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
        List<BasicCoreComponent> latestHistoryBccList = bccRepository.findAllWithLatestRevisionNumByCurrentBccId(currentBccId);
        if (latestHistoryBccList.isEmpty()) {
            throw new IllegalStateException("There is no history for this element.");
        }

        int seqKey;
        long fromAccId = bcc.getFromAccId();

        switch (bcc.getEntityType()) {
            case Element:
                seqKey = findAppropriateSeqKey(bcc);
                bcc.setSeqKey(seqKey);

                increaseSeqKeyGreaterThan(fromAccId, seqKey - 1);
                break;
            case Attribute:
                seqKey = bcc.getSeqKey();
                bcc.setSeqKey(0);

                decreaseSeqKeyGreaterThan(fromAccId, seqKey);
                break;
        }

        bccRepository.save(bcc);

        int latestRevisionTrackingNum = latestHistoryBccList.stream()
                .mapToInt(e -> e.getRevisionTrackingNum())
                .max().getAsInt();
        int nextRevisionTrackingNum = latestRevisionTrackingNum + 1;
        BasicCoreComponent bccHistory = bcc.clone();
        bccHistory.setRevisionNum(latestHistoryBccList.get(0).getRevisionNum());
        bccHistory.setRevisionTrackingNum(nextRevisionTrackingNum);
        bccHistory.setRevisionAction(Update);
        bccHistory.setLastUpdatedBy(requesterId);
        bccHistory.setCurrentBccId(currentBccId);

        bccRepository.saveAndFlush(bccHistory);

        // to check RevisionTrackingNum
        latestHistoryBccList = bccRepository.findAllWithLatestRevisionNumByCurrentBccId(currentBccId);
        int actualRevisionTrackingNum = latestHistoryBccList.stream()
                .mapToInt(e -> e.getRevisionTrackingNum())
                .max().getAsInt();
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
        List<BasicCoreComponent> latestHistoryBccList = bccRepository.findAllWithLatestRevisionNumByCurrentBccId(currentBccId);
        if (latestHistoryBccList.isEmpty()) {
            throw new IllegalStateException("There is no history for this element.");
        }

        long bccId = bcc.getBccId();
        bccRepository.deleteByCurrentBccId(bccId); // To remove history
        int seqKey = bcc.getSeqKey();
        bccRepository.delete(bcc);

        if (seqKey > 0) {
            long fromAccId = bcc.getFromAccId();
            decreaseSeqKeyGreaterThan(fromAccId, seqKey);
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
                asccpRepository.findAllWithLatestRevisionNumByCurrentAsccpId(currentAsccpId);
        if (latestHistoryAsccpList == null) {
            throw new IllegalStateException("There is no history for this element.");
        }

        asccpRepository.save(asccp);
        asccRepository.findAllByToAsccpId(asccp.getAsccpId()).forEach(e -> {
            AggregateCoreComponent acc = accRepository.findOne(e.getFromAccId());
            e.setDen(acc, asccp);
            asccRepository.save(e);
        });

        int latestRevisionTrackingNum = latestHistoryAsccpList.stream()
                .mapToInt(e -> e.getRevisionTrackingNum())
                .max().getAsInt();
        int nextRevisionTrackingNum = latestRevisionTrackingNum + 1;
        AssociationCoreComponentProperty asccpHistory = asccp.clone();
        asccpHistory.setRevisionNum(latestHistoryAsccpList.get(0).getRevisionNum());
        asccpHistory.setRevisionTrackingNum(nextRevisionTrackingNum);
        asccpHistory.setRevisionAction(Update);
        asccpHistory.setLastUpdatedBy(requesterId);
        asccpHistory.setCurrentAsccpId(currentAsccpId);

        asccpRepository.saveAndFlush(asccpHistory);

        // to check RevisionTrackingNum
        latestHistoryAsccpList = asccpRepository.findAllWithLatestRevisionNumByCurrentAsccpId(currentAsccpId);
        int actualRevisionTrackingNum = latestHistoryAsccpList.stream()
                .mapToInt(e -> e.getRevisionTrackingNum())
                .max().getAsInt();
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
                asccpRepository.findAllWithLatestRevisionNumByCurrentAsccpId(currentAsccpId);
        if (latestHistoryAsccpList == null) {
            throw new IllegalStateException("There is no history for this element.");
        }

        long asccpId = asccp.getAsccpId();
        asccpRepository.deleteByCurrentAsccpId(asccpId); // To remove history
        asccpRepository.delete(asccp);
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
                bccpRepository.findAllWithLatestRevisionNumByCurrentBccpId(currentBccpId);
        if (latestHistoryBccpList.isEmpty()) {
            throw new IllegalStateException("There is no history for this element.");
        }

        bccpRepository.save(bccp);
        bccRepository.findAllByToBccpId(bccp.getBccpId()).forEach(e -> {
            AggregateCoreComponent acc = accRepository.findOne(e.getFromAccId());
            e.setDen(acc, bccp);
            bccRepository.save(e);
        });

        int latestRevisionTrackingNum = latestHistoryBccpList.stream()
                .mapToInt(e -> e.getRevisionTrackingNum())
                .max().getAsInt();
        int nextRevisionTrackingNum = latestRevisionTrackingNum + 1;
        BasicCoreComponentProperty bccpHistory = bccp.clone();
        bccpHistory.setRevisionNum(latestHistoryBccpList.get(0).getRevisionNum());
        bccpHistory.setRevisionTrackingNum(nextRevisionTrackingNum);
        bccpHistory.setRevisionAction(Update);
        bccpHistory.setLastUpdatedBy(requesterId);
        bccpHistory.setCurrentBccpId(currentBccpId);

        bccpRepository.saveAndFlush(bccpHistory);

        // to check RevisionTrackingNum
        latestHistoryBccpList = bccpRepository.findAllWithLatestRevisionNumByCurrentBccpId(currentBccpId);
        int actualRevisionTrackingNum = latestHistoryBccpList.stream()
                .mapToInt(e -> e.getRevisionTrackingNum())
                .max().getAsInt();
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
        List<BasicCoreComponentProperty> latestHistoryBccpList = bccpRepository.findAllWithLatestRevisionNumByCurrentBccpId(currentBccpId);
        if (latestHistoryBccpList.isEmpty()) {
            throw new IllegalStateException("There is no history for this element.");
        }

        long bccpId = bccp.getBccpId();
        bccpRepository.deleteByCurrentBccpId(bccpId); // To remove history
        bccpRepository.delete(bccp);
    }

    private int findAppropriateSeqKey(BasicCoreComponent latestBcc) {
        long bccId = latestBcc.getBccId();
        List<BasicCoreComponent> latestHistoryBccList = bccRepository.findAllWithLatestRevisionNumByCurrentBccId(bccId);
        return latestHistoryBccList.stream()
                .mapToInt(e -> e.getSeqKey())
                .max().orElseGet(() -> 0);
    }

    private void increaseSeqKeyGreaterThan(long fromAccId, int seqKey) {
        asccRepository.increaseSeqKeyByFromAccIdAndSeqKeyGreaterThan(fromAccId, seqKey);
        bccRepository.increaseSeqKeyByFromAccIdAndSeqKeyGreaterThan(fromAccId, seqKey);
    }

    private void decreaseSeqKeyGreaterThan(long fromAccId, int seqKey) {
        asccRepository.decreaseSeqKeyByFromAccIdAndSeqKeyGreaterThan(fromAccId, seqKey);
        bccRepository.decreaseSeqKeyByFromAccIdAndSeqKeyGreaterThan(fromAccId, seqKey);
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

        updateChildrenState(ueAcc, state, requester);
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

        List<AssociationCoreComponent> asccList = asccRepository.findByFromAccIdAndRevisionNum(fromAccId, 0);
        List<AssociationCoreComponentProperty> asccpList = new ArrayList();
        for (AssociationCoreComponent ascc : asccList) {
            if (ascc.getState() != Published) {
                ascc.setState(state);
                ascc.setLastUpdatedBy(lastUpdatedBy);
            }

            long toAsccpId = ascc.getToAsccpId();
            AssociationCoreComponentProperty toAsccp = asccpRepository.findOne(toAsccpId);
            if (toAsccp.getState() != Published) {
                toAsccp.setState(state);
                toAsccp.setLastUpdatedBy(lastUpdatedBy);
                asccpList.add(toAsccp);
            }
        }
        asccRepository.save(asccList.stream()
                .filter(e -> e.isDirty()).collect(Collectors.toList()));
        asccpRepository.save(asccpList);

        List<BasicCoreComponent> bccList = bccRepository.findByFromAccIdAndRevisionNum(fromAccId, 0);
        List<BasicCoreComponentProperty> bccpList = new ArrayList();
        for (BasicCoreComponent bcc : bccList) {
            if (bcc.getState() != Published) {
                bcc.setState(state);
                bcc.setLastUpdatedBy(lastUpdatedBy);
            }

            long toBccpId = bcc.getToBccpId();
            BasicCoreComponentProperty toBccp = bccpRepository.findOne(toBccpId);
            if (toBccp.getState() != Published) {
                toBccp.setState(state);
                toBccp.setLastUpdatedBy(lastUpdatedBy);
                bccpList.add(toBccp);
            }
        }
        bccRepository.save(bccList.stream()
                .filter(e -> e.isDirty()).collect(Collectors.toList()));
        bccpRepository.save(bccpList);
    }

    private void updateAccState(AggregateCoreComponent acc,
                                CoreComponentState state,
                                User requester) {
        long roleOfAccId = acc.getAccId();
        long lastUpdatedBy = requester.getAppUserId();

        if (acc.getState() != Published) {
            acc.setState(state);
            acc.setLastUpdatedBy(lastUpdatedBy);
            accRepository.save(acc);
        }

        AssociationCoreComponentProperty asccp = asccpRepository.findOneByRoleOfAccId(roleOfAccId);
        updateAsccpState(asccp, state, requester);
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
            asccpRepository.save(asccp);
        }

        long asccpId = asccp.getAsccpId();
        List<AssociationCoreComponent> asccList = asccRepository.findByToAsccpIdAndRevisionNum(asccpId, 0);
        for (AssociationCoreComponent ascc : asccList) {
            if (ascc.getState() != Published) {
                ascc.setState(state);
                ascc.setLastUpdatedBy(lastUpdatedBy);
            }
        }

        asccRepository.save(asccList.stream()
                .filter(e -> e.isDirty()).collect(Collectors.toList()));
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
            bccpRepository.save(bccp);
        }

        long bccpId = bccp.getBccpId();
        List<BasicCoreComponent> bccList = bccRepository.findByToBccpIdAndRevisionNum(bccpId, 0);
        for (BasicCoreComponent bcc : bccList) {
            if (bcc.getState() != Published) {
                bcc.setState(state);
                bcc.setLastUpdatedBy(lastUpdatedBy);
            }
        }

        bccRepository.save(bccList.stream()
                .filter(e -> e.isDirty()).collect(Collectors.toList()));
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

        AssociationCoreComponentProperty asccp = findAssociationCoreComponentPropertyRecursivelyByRoleOfAccId(basedAccId, eAccId);
        return (asccp != null) ? true : false;
    }

    private AssociationCoreComponentProperty findAssociationCoreComponentPropertyRecursivelyByRoleOfAccId(long basedAccId, long eAccId) {
        Collection<Long> fromAccIds = Arrays.asList(basedAccId);

        while (true) {
            List<AssociationCoreComponent> asccList = asccRepository.findByFromAccId(fromAccIds);
            List<AssociationCoreComponentProperty> asccpList =
                    !asccList.isEmpty()
                            ? asccpRepository.findByAsccpId(asccList.stream().map(AssociationCoreComponent::getToAsccpId).collect(Collectors.toList()))
                            : Collections.emptyList();
            if (asccpList.isEmpty()) {
                return null;
            }

            for (AssociationCoreComponentProperty asccp : asccpList) {
                if (asccp.getRoleOfAccId() == eAccId) {
                    return asccp;
                }
            }

            fromAccIds = asccpList.stream().map(AssociationCoreComponentProperty::getRoleOfAccId).collect(Collectors.toList());
        }
    }

    public int findOriginalCardinalityMin(AssociationBusinessInformationEntity asbie) {
        long basedAsccId = asbie.getBasedAsccId();
        AssociationCoreComponent ascc = asccRepository.findOne(basedAsccId);
        return ascc.getCardinalityMin();
    }

    public int findOriginalCardinalityMax(AssociationBusinessInformationEntity asbie) {
        long basedAsccId = asbie.getBasedAsccId();
        AssociationCoreComponent ascc = asccRepository.findOne(basedAsccId);
        return ascc.getCardinalityMax();
    }

    public int findOriginalCardinalityMin(BasicBusinessInformationEntity bbie) {
        long basedBccId = bbie.getBasedBccId();
        BasicCoreComponent bcc = bccRepository.findOne(basedBccId);
        return bcc.getCardinalityMin();
    }

    public int findOriginalCardinalityMax(BasicBusinessInformationEntity bbie) {
        long basedBccId = bbie.getBasedBccId();
        BasicCoreComponent bcc = bccRepository.findOne(basedBccId);
        return bcc.getCardinalityMax();
    }

    public int findOriginalCardinalityMin(BasicBusinessInformationEntitySupplementaryComponent bbieSc) {
        long dtScId = bbieSc.getDtScId();
        DataTypeSupplementaryComponent dtSc = dtScRepository.findOne(dtScId);
        return dtSc.getCardinalityMin();
    }

    public int findOriginalCardinalityMax(BasicBusinessInformationEntitySupplementaryComponent bbieSc) {
        long dtScId = bbieSc.getDtScId();
        DataTypeSupplementaryComponent dtSc = dtScRepository.findOne(dtScId);
        return dtSc.getCardinalityMax();
    }

    public void transferOwner(AggregateCoreComponent acc, User newOwner) {
        long oldOwnerId = acc.getOwnerUserId();
        long newOwnerId = newOwner.getAppUserId();

        if (oldOwnerId == newOwnerId) {
            return;
        }

        acc.setOwnerUserId(newOwnerId);
        accRepository.save(acc);
    }
}
