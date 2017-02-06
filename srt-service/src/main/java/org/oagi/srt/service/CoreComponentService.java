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
    private TopLevelAbieRepository topLevelAbieRepository;

    @Autowired
    private AggregateBusinessInformationEntityRepository abieRepository;

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
    public void update(AssociationCoreComponent ascc, User requester) {
        long requesterId = requester.getAppUserId();
        long ownerId = ascc.getCreatedBy();
        if (requesterId != ownerId) {
            throw new PermissionDeniedDataAccessException(
                    "This operation only allows for the owner of this element.", new IllegalArgumentException());
        }
        long currentAsccId = ascc.getAsccId();
        AssociationCoreComponent latestAscc = asccRepository.findLatestOneByCurrentAsccId(currentAsccId);
        if (latestAscc == null) {
            throw new IllegalStateException("There is no history for this element.");
        }

        asccRepository.save(ascc);

        int nextRevisionTrackingNum = latestAscc.getRevisionTrackingNum() + 1;
        AssociationCoreComponent asccHistory = ascc.clone();
        asccHistory.setRevisionNum(latestAscc.getRevisionNum());
        asccHistory.setRevisionTrackingNum(nextRevisionTrackingNum);
        asccHistory.setRevisionAction(Update);
        asccHistory.setLastUpdatedBy(requesterId);
        asccHistory.setCurrentAsccId(currentAsccId);

        asccRepository.saveAndFlush(asccHistory);

        // to check RevisionTrackingNum
        latestAscc = asccRepository.findLatestOneByCurrentAsccId(currentAsccId);
        long actualRevisionTrackingNum = latestAscc.getRevisionTrackingNum();
        if (actualRevisionTrackingNum != nextRevisionTrackingNum) {
            throw new ConcurrentModificationException("AssociationCoreComponent was modified outside of this operation");
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void discard(AssociationCoreComponent ascc, User requester) {
        long requesterId = requester.getAppUserId();
        long ownerId = ascc.getCreatedBy();
        if (requesterId != ownerId) {
            throw new PermissionDeniedDataAccessException(
                    "This operation only allows for the owner of this element.", new IllegalArgumentException());
        }
        long currentAsccId = ascc.getAsccId();
        AssociationCoreComponent latestAscc = asccRepository.findLatestOneByCurrentAsccId(currentAsccId);
        if (latestAscc == null) {
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
        long ownerId = bcc.getCreatedBy();
        if (requesterId != ownerId) {
            throw new PermissionDeniedDataAccessException(
                    "This operation only allows for the owner of this element.", new IllegalArgumentException());
        }
        long currentBccId = bcc.getBccId();
        BasicCoreComponent latestBcc = bccRepository.findLatestOneByCurrentBccId(currentBccId);
        if (latestBcc == null) {
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

        int nextRevisionTrackingNum = latestBcc.getRevisionTrackingNum() + 1;
        BasicCoreComponent bccHistory = bcc.clone();
        bccHistory.setRevisionNum(latestBcc.getRevisionNum());
        bccHistory.setRevisionTrackingNum(nextRevisionTrackingNum);
        bccHistory.setRevisionAction(Update);
        bccHistory.setLastUpdatedBy(requesterId);
        bccHistory.setCurrentBccId(currentBccId);

        bccRepository.saveAndFlush(bccHistory);

        // to check RevisionTrackingNum
        latestBcc = bccRepository.findLatestOneByCurrentBccId(currentBccId);
        long actualRevisionTrackingNum = latestBcc.getRevisionTrackingNum();
        if (actualRevisionTrackingNum != nextRevisionTrackingNum) {
            throw new ConcurrentModificationException("BasicCoreComponent was modified outside of this operation");
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void discard(BasicCoreComponent bcc, User requester) {
        long requesterId = requester.getAppUserId();
        long ownerId = bcc.getCreatedBy();
        if (requesterId != ownerId) {
            throw new PermissionDeniedDataAccessException(
                    "This operation only allows for the owner of this element.", new IllegalArgumentException());
        }
        long currentBccId = bcc.getBccId();
        BasicCoreComponent latestBcc = bccRepository.findLatestOneByCurrentBccId(currentBccId);
        if (latestBcc == null) {
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

    private int findAppropriateSeqKey(BasicCoreComponent latestBcc) {
        long bccId = latestBcc.getBccId();
        BasicCoreComponent latestHistory = bccRepository.findLatestOneByCurrentBccIdAndSeqKeyIsNotZero(bccId);
        return (latestHistory != null) ? latestHistory.getSeqKey() : 0;
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
        if (ueAcc.getCreatedBy() != requester.getAppUserId()) {
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

    @Transactional
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
}
