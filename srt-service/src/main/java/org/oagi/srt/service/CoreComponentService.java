package org.oagi.srt.service;

import org.oagi.srt.common.util.Utility;
import org.oagi.srt.provider.CoreComponentProvider;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.oagi.srt.repository.entity.listener.CreatorModifierAwareEventListener;
import org.springframework.beans.factory.annotation.Autowired;
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
    private UndoService undoService;

    @Autowired
    private AggregateCoreComponentRepository accRepository;

    @Autowired
    private AggregateBusinessInformationEntityRepository abieRepository;

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

    @Autowired
    private CoreComponentsRepository coreComponentsRepository;

    @Autowired
    private CoreComponentDAO coreComponentDAO;

    public List<CoreComponents> getCoreComponents(
            List<String> types, List<CoreComponentState> states, Release release, Sort.Order order) {
        if (release == null) {
            release = Release.CURRENT_RELEASE;
        }
        return coreComponentsRepository.findAll(types, states, release, order);
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
    public AggregateCoreComponent newAggregateCoreComponentRevision(User user, AggregateCoreComponent acc) {
        long requesterId = user.getAppUserId();
        if (acc.getOwnerUserId() != requesterId) {
            throw new IllegalArgumentException("Only allowed this operation by owner.");
        }
        acc.setState(CoreComponentState.Editing);

        CreatorModifierAwareEventListener eventListener = new CreatorModifierAwareEventListener(user);
        acc.addPersistEventListener(eventListener);

        acc = accRepository.saveAndFlush(acc);

        AggregateCoreComponent accHistory = acc.clone();
        Long currentAccId = acc.getAccId();
        List<AggregateCoreComponent> latestHistoryAccList = accRepository.findAllWithLatestRevisionNumByCurrentAccId(currentAccId);
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
        accHistory.setReleaseId(null);

        accRepository.save(accHistory);

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
    public AssociationCoreComponentProperty newAssociationCoreComponentPropertyRevision(User user, AssociationCoreComponentProperty asccp) {
        long requesterId = user.getAppUserId();
        if (asccp.getOwnerUserId() != requesterId) {
            throw new IllegalArgumentException("Only allowed this operation by owner.");
        }
        asccp.setState(CoreComponentState.Editing);

        CreatorModifierAwareEventListener eventListener = new CreatorModifierAwareEventListener(user);
        asccp.addPersistEventListener(eventListener);

        asccp = asccpRepository.saveAndFlush(asccp);

        AssociationCoreComponentProperty asccpHistory = asccp.clone();
        Long currentAsccpId = asccp.getAsccpId();
        List<AssociationCoreComponentProperty> latestHistoryAsccpList = asccpRepository.findAllWithLatestRevisionNumByCurrentAsccpId(currentAsccpId);
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
        asccpHistory.setReleaseId(null);

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
    public BasicCoreComponentProperty newBasicCoreComponentPropertyRevision(User user, BasicCoreComponentProperty bccp) {
        long requesterId = user.getAppUserId();
        if (bccp.getOwnerUserId() != requesterId) {
            throw new IllegalArgumentException("Only allowed this operation by owner.");
        }
        bccp.setState(CoreComponentState.Editing);

        CreatorModifierAwareEventListener eventListener = new CreatorModifierAwareEventListener(user);
        bccp.addPersistEventListener(eventListener);

        bccp = bccpRepository.saveAndFlush(bccp);

        BasicCoreComponentProperty bccpHistory = bccp.clone();
        Long currentBccpId = bccp.getBccpId();
        List<BasicCoreComponentProperty> latestHistoryBccpList = bccpRepository.findAllWithLatestRevisionNumByCurrentBccpId(currentBccpId);
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
        bccpHistory.setReleaseId(null);

        bccpRepository.save(bccpHistory);

        return bccp;
    }

    @Transactional(rollbackFor = Throwable.class)
    public AssociationCoreComponent newAssociationCoreComponentRevision(User user, AssociationCoreComponent ascc) {
        long requesterId = user.getAppUserId();
        if (ascc.getOwnerUserId() != requesterId) {
            throw new IllegalArgumentException("Only allowed this operation by owner.");
        }
        ascc.setState(CoreComponentState.Editing);

        CreatorModifierAwareEventListener eventListener = new CreatorModifierAwareEventListener(user);
        ascc.addPersistEventListener(eventListener);

        ascc = asccRepository.saveAndFlush(ascc);

        AssociationCoreComponent asccHistory = ascc.clone();
        Long currentAsccId = ascc.getAsccId();
        List<AssociationCoreComponent> latestHistoryAsccList = asccRepository.findAllWithLatestRevisionNumByCurrentAsccId(currentAsccId);
        if (latestHistoryAsccList.isEmpty()) {
            throw new IllegalStateException("There is no history for this element.");
        }

        int latestRevisionNum = latestHistoryAsccList.stream()
                .mapToInt(e -> e.getRevisionNum())
                .max().orElse(0);
        asccHistory.setRevisionNum(latestRevisionNum + 1);
        int revisionTrackingNum = 1;
        asccHistory.setRevisionTrackingNum(revisionTrackingNum);
        asccHistory.setRevisionAction(Insert);
        asccHistory.setCurrentAsccId(ascc.getAsccId());
        asccHistory.setReleaseId(null);

        asccRepository.save(asccHistory);

        return ascc;
    }

    @Transactional(rollbackFor = Throwable.class)
    public BasicCoreComponent newBasicCoreComponentRevision(User user, BasicCoreComponent bcc) {
        long requesterId = user.getAppUserId();
        if (bcc.getOwnerUserId() != requesterId) {
            throw new IllegalArgumentException("Only allowed this operation by owner.");
        }
        bcc.setState(CoreComponentState.Editing);

        CreatorModifierAwareEventListener eventListener = new CreatorModifierAwareEventListener(user);
        bcc.addPersistEventListener(eventListener);

        bcc = bccRepository.saveAndFlush(bcc);

        BasicCoreComponent bccHistory = bcc.clone();
        Long currentBccId = bcc.getBccId();
        List<BasicCoreComponent> latestHistoryBccList = bccRepository.findAllWithLatestRevisionNumByCurrentBccId(currentBccId);
        if (latestHistoryBccList.isEmpty()) {
            throw new IllegalStateException("There is no history for this element.");
        }

        int latestRevisionNum = latestHistoryBccList.stream()
                .mapToInt(e -> e.getRevisionNum())
                .max().orElse(0);
        bccHistory.setRevisionNum(latestRevisionNum + 1);
        int revisionTrackingNum = 1;
        bccHistory.setRevisionTrackingNum(revisionTrackingNum);
        bccHistory.setRevisionAction(Insert);
        bccHistory.setCurrentBccId(bcc.getBccId());
        bccHistory.setReleaseId(null);

        bccRepository.save(bccHistory);

        return bcc;
    }

    @Transactional(rollbackFor = Throwable.class)
    public void update(AggregateCoreComponent acc, User requester) {
        long requesterId = requester.getAppUserId();
        long ownerId = acc.getOwnerUserId();
        if (requesterId != ownerId) {
            throw new IllegalStateException("This operation only allows for the owner of this element.");
        }
        long currentAccId = acc.getAccId();
        List<AggregateCoreComponent> latestHistoryAccList = accRepository.findAllWithLatestRevisionNumByCurrentAccId(currentAccId);
        if (latestHistoryAccList.isEmpty()) {
            throw new IllegalStateException("There is no history for this element.");
        }

        AggregateCoreComponent oldAcc = accRepository.findOne(acc.getId()).clone();
        AggregateCoreComponent newAcc = acc.clone();

        acc = coreComponentDAO.save(acc);

        if (isBasedAccIdOnlyChange(oldAcc, newAcc)) {
            return; // do not create history records if based ACC was the only change
        }

        int latestRevisionTrackingNum = latestHistoryAccList.stream()
                .mapToInt(e -> e.getRevisionTrackingNum())
                .max().orElse(0);
        int nextRevisionTrackingNum = latestRevisionTrackingNum + 1;
        AggregateCoreComponent accHistory = acc.clone();
        accHistory.setRevisionNum(latestHistoryAccList.get(0).getRevisionNum());
        accHistory.setRevisionTrackingNum(nextRevisionTrackingNum);
        accHistory.setRevisionAction(Update);
        accHistory.setLastUpdatedBy(requesterId);
        accHistory.setCurrentAccId(currentAccId);
        accHistory.setReleaseId(null);

        accRepository.saveAndFlush(accHistory);

        long accId = acc.getAccId();
        // update ascc DEN
        List<AssociationCoreComponent> dirtyAsccList = new ArrayList();
        for (AssociationCoreComponent ascc : asccRepository.findByFromAccId(accId)) {
            AssociationCoreComponentProperty asccp = asccpRepository.findOne(ascc.getToAsccpId());
            ascc.setDen(acc, asccp);
            if (ascc.isDirty()) {
                dirtyAsccList.add(ascc);
            }
        }
        if (!dirtyAsccList.isEmpty()) {
            asccRepository.save(dirtyAsccList);
        }

        // update asccp DEN
        List<AssociationCoreComponentProperty> dirtyAsccpList = new ArrayList();
        for (AssociationCoreComponentProperty asccp : asccpRepository.findByRoleOfAccId(accId)) {
            asccp.setRoleOfAcc(acc);
            if (asccp.isDirty()) {
                dirtyAsccpList.add(asccp);
            }
        }
        if (!dirtyAsccpList.isEmpty()) {
            asccpRepository.save(dirtyAsccpList);
        }

        // update bcc DEN
        List<BasicCoreComponent> dirtyBccList = new ArrayList();
        for (BasicCoreComponent bcc : bccRepository.findByFromAccId(accId)) {
            BasicCoreComponentProperty bccp = bccpRepository.findOne(bcc.getToBccpId());
            bcc.setDen(acc, bccp);
            if (bcc.isDirty()) {
                dirtyBccList.add(bcc);
            }
        }
        if (!dirtyBccList.isEmpty()) {
            bccRepository.save(dirtyBccList);
        }

        // to check RevisionTrackingNum
        latestHistoryAccList = accRepository.findAllWithLatestRevisionNumByCurrentAccId(currentAccId);
        int actualRevisionTrackingNum = latestHistoryAccList.stream()
                .mapToInt(e -> e.getRevisionTrackingNum())
                .max().orElse(0);
        if (actualRevisionTrackingNum != nextRevisionTrackingNum) {
            throw new ConcurrentModificationException("AggregateCoreComponent was modified outside of this operation");
        }
    }

    private boolean isBasedAccIdOnlyChange(AggregateCoreComponent oldAcc, AggregateCoreComponent newAcc) {
        oldAcc.setBasedAccId(null);
        newAcc.setBasedAccId(null);

        if (oldAcc.hashCode() == newAcc.hashCode()) { // everything else is the same, only change was basedAccId
            return true;
        } else { // difference is in some other field
            return false;
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void updateState(AggregateCoreComponent acc,
                            CoreComponentState state,
                            User requester) {
        if (acc.getOwnerUserId() != requester.getAppUserId()) {
            throw new IllegalStateException("This operation only allows for the owner of this element.");
        }

        updateAccState(acc, state, requester);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void updateState(AssociationCoreComponentProperty asccp,
                            CoreComponentState state,
                            User requester) {
        if (asccp.getOwnerUserId() != requester.getAppUserId()) {
            throw new IllegalStateException("This operation only allows for the owner of this element.");
        }

        // #Issue 437
        if (state == Published) {
            AggregateCoreComponent acc = accRepository.findOne(asccp.getRoleOfAccId());
            if (acc.getState() != Published) {
                throw new IllegalStateException("This state cannot be updated until the associated ACC is in Publish state.");
            }
        }

        updateAsccpState(asccp, state, requester);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void updateState(BasicCoreComponentProperty bccp,
                            CoreComponentState state,
                            User requester) {
        if (bccp.getOwnerUserId() != requester.getAppUserId()) {
            throw new IllegalStateException("This operation only allows for the owner of this element.");
        }

        updateBccpState(bccp, state, requester);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void discard(AggregateCoreComponent acc, User requester) {
        long requesterId = requester.getAppUserId();
        long ownerId = acc.getOwnerUserId();
        if (requesterId != ownerId) {
            throw new IllegalStateException("This operation only allows for the owner of this element.");
        }
        long currentAccId = acc.getAccId();
        List<AggregateCoreComponent> latestHistoryAccList = accRepository.findAllWithLatestRevisionNumByCurrentAccId(currentAccId);
        if (latestHistoryAccList.isEmpty()) {
            throw new IllegalStateException("There is no history for this element.");
        }

        if (!asccpRepository.findByRoleOfAccId(currentAccId).isEmpty()) {
            throw new IllegalStateException("Not allowed to discard the ACC which has related with ASCCP");
        }
        if (!abieRepository.findByBasedAccId(currentAccId).isEmpty()) {
            throw new IllegalStateException("Not allowed to discard the ACC which has related with ABIE");
        }

        coreComponentDAO.delete(acc);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void update(AssociationCoreComponent ascc, User requester) {
        long requesterId = requester.getAppUserId();
        long ownerId = ascc.getOwnerUserId();
        if (requesterId != ownerId) {
            throw new IllegalStateException("This operation only allows for the owner of this element.");
        }
        long currentAsccId = ascc.getAsccId();
        List<AssociationCoreComponent> latestHistoryAsccList = asccRepository.findAllWithLatestRevisionNumByCurrentAsccId(currentAsccId);
        if (latestHistoryAsccList == null) {
            throw new IllegalStateException("There is no history for this element.");
        }

        AssociationCoreComponent oldAscc = asccRepository.findOne(ascc.getId()).clone();
        int oldSeqKey = oldAscc.getSeqKey();
        AssociationCoreComponent newAscc = ascc.clone();

        ascc = coreComponentDAO.save(ascc);

        if (isSeqKeyOnlyChange(oldAscc, newAscc)) {
            return; // do not create history records if seq key was the only change
        }

        int latestRevisionTrackingNum = latestHistoryAsccList.stream()
                .mapToInt(e -> e.getRevisionTrackingNum())
                .max().orElse(0);
        int nextRevisionTrackingNum = latestRevisionTrackingNum + 1;
        AssociationCoreComponent asccHistory = ascc.clone();
        asccHistory.setRevisionNum(latestHistoryAsccList.get(0).getRevisionNum());
        asccHistory.setRevisionTrackingNum(nextRevisionTrackingNum);
        asccHistory.setRevisionAction(Update);
        asccHistory.setLastUpdatedBy(requesterId);
        asccHistory.setCurrentAsccId(currentAsccId);
        asccHistory.setSeqKey(oldSeqKey); // if seq key update was combined with other updates, revert it to original seq key
        asccHistory.setReleaseId(null);

        asccRepository.saveAndFlush(asccHistory);

        // to check RevisionTrackingNum
        latestHistoryAsccList = asccRepository.findAllWithLatestRevisionNumByCurrentAsccId(currentAsccId);
        int actualRevisionTrackingNum = latestHistoryAsccList.stream()
                .mapToInt(e -> e.getRevisionTrackingNum())
                .max().orElse(0);
        if (actualRevisionTrackingNum != nextRevisionTrackingNum) {
            throw new ConcurrentModificationException("AssociationCoreComponent was modified outside of this operation");
        }
    }

    private boolean isSeqKeyOnlyChange(AssociationCoreComponent oldAscc, AssociationCoreComponent newAscc) {
        oldAscc.setSeqKey(0);
        newAscc.setSeqKey(0);

        if (oldAscc.hashCode() == newAscc.hashCode()) { // everything else is the same, only change was seqkey
            return true;
        } else { // difference is in some other field
            return false;
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void discard(AssociationCoreComponent ascc, User requester) {
        long requesterId = requester.getAppUserId();
        long ownerId = ascc.getOwnerUserId();
        if (requesterId != ownerId) {
            throw new IllegalStateException("This operation only allows for the owner of this element.");
        }
        long currentAsccId = ascc.getAsccId();
        List<AssociationCoreComponent> latestHistoryAsccList = asccRepository.findAllWithLatestRevisionNumByCurrentAsccId(currentAsccId);
        if (latestHistoryAsccList == null) {
            throw new IllegalStateException("There is no history for this element.");
        }

        long asccId = ascc.getAsccId();
        coreComponentDAO.deleteByCurrentAsccId(asccId); // To remove history
        int seqKey = ascc.getSeqKey();
        coreComponentDAO.delete(ascc);

        long fromAccId = ascc.getFromAccId();
        decreaseSeqKeyGreaterThan(fromAccId, seqKey);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void update(BasicCoreComponent bcc, User requester) {
        long requesterId = requester.getAppUserId();
        long ownerId = bcc.getOwnerUserId();
        if (requesterId != ownerId) {
            throw new IllegalStateException("This operation only allows for the owner of this element.");
        }
        long currentBccId = bcc.getBccId();
        List<BasicCoreComponent> latestHistoryBccList = bccRepository.findAllWithLatestRevisionNumByCurrentBccId(currentBccId);
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

                    increaseSeqKeyGreaterThan(fromAccId, seqKey - 1);
                }

                break;
            case Attribute:
                if (bcc.getSeqKey() > 0) {
                    seqKey = bcc.getSeqKey();
                    bcc.setSeqKey(0);

                    decreaseSeqKeyGreaterThan(fromAccId, seqKey);
                }

                break;
        }

        BasicCoreComponent oldBcc = bccRepository.findOne(bcc.getId()).clone();
        int oldSeqKey = oldBcc.getSeqKey();
        BasicCoreComponent newBcc = bcc.clone();

        bcc = coreComponentDAO.save(bcc);

        if (isSeqKeyOnlyChange(oldBcc, newBcc)) {
            return; // do not create history records if seq key was the only change
        }

        int latestRevisionTrackingNum = latestHistoryBccList.stream()
                .mapToInt(e -> e.getRevisionTrackingNum())
                .max().orElse(0);
        int nextRevisionTrackingNum = latestRevisionTrackingNum + 1;
        BasicCoreComponent bccHistory = bcc.clone();
        bccHistory.setRevisionNum(latestHistoryBccList.get(0).getRevisionNum());
        bccHistory.setRevisionTrackingNum(nextRevisionTrackingNum);
        bccHistory.setRevisionAction(Update);
        bccHistory.setLastUpdatedBy(requesterId);
        bccHistory.setCurrentBccId(currentBccId);
        bccHistory.setSeqKey(oldSeqKey); // if seq key update was combined with other updates, revert it to original seq key
        bccHistory.setReleaseId(null);

        bccRepository.saveAndFlush(bccHistory);

        // to check RevisionTrackingNum
        latestHistoryBccList = bccRepository.findAllWithLatestRevisionNumByCurrentBccId(currentBccId);
        int actualRevisionTrackingNum = latestHistoryBccList.stream()
                .mapToInt(e -> e.getRevisionTrackingNum())
                .max().orElse(0);
        if (actualRevisionTrackingNum != nextRevisionTrackingNum) {
            throw new ConcurrentModificationException("BasicCoreComponent was modified outside of this operation");
        }
    }

    private boolean isSeqKeyOnlyChange(BasicCoreComponent oldBcc, BasicCoreComponent newBcc) {
        oldBcc.setSeqKey(0);
        newBcc.setSeqKey(0);

        if (oldBcc.hashCode() == newBcc.hashCode()) { // everything else is the same, only change was seqkey
            return true;
        } else { // difference is in some other field
            return false;
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void discard(BasicCoreComponent bcc, User requester) {
        long requesterId = requester.getAppUserId();
        long ownerId = bcc.getOwnerUserId();
        if (requesterId != ownerId) {
            throw new IllegalStateException("This operation only allows for the owner of this element.");
        }
        long currentBccId = bcc.getBccId();
        List<BasicCoreComponent> latestHistoryBccList = bccRepository.findAllWithLatestRevisionNumByCurrentBccId(currentBccId);
        if (latestHistoryBccList.isEmpty()) {
            throw new IllegalStateException("There is no history for this element.");
        }

        long bccId = bcc.getBccId();
        coreComponentDAO.deleteByCurrentBccId(bccId); // To remove history
        int seqKey = bcc.getSeqKey();
        coreComponentDAO.delete(bcc);

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
            throw new IllegalStateException("This operation only allows for the owner of this element.");
        }
        long currentAsccpId = asccp.getAsccpId();
        List<AssociationCoreComponentProperty> latestHistoryAsccpList =
                asccpRepository.findAllWithLatestRevisionNumByCurrentAsccpId(currentAsccpId);
        if (latestHistoryAsccpList == null) {
            throw new IllegalStateException("There is no history for this element.");
        }

        coreComponentDAO.save(asccp);
        asccRepository.findAllByToAsccpId(asccp.getAsccpId()).forEach(e -> {
            AggregateCoreComponent acc = accRepository.findOne(e.getFromAccId());
            e.setDen(acc, asccp);
            coreComponentDAO.save(e);
        });

        int latestRevisionTrackingNum = latestHistoryAsccpList.stream()
                .mapToInt(e -> e.getRevisionTrackingNum())
                .max().orElse(0);
        int nextRevisionTrackingNum = latestRevisionTrackingNum + 1;
        AssociationCoreComponentProperty asccpHistory = asccp.clone();
        asccpHistory.setRevisionNum(latestHistoryAsccpList.get(0).getRevisionNum());
        asccpHistory.setRevisionTrackingNum(nextRevisionTrackingNum);
        asccpHistory.setRevisionAction(Update);
        asccpHistory.setLastUpdatedBy(requesterId);
        asccpHistory.setCurrentAsccpId(currentAsccpId);
        asccpHistory.setReleaseId(null);

        asccpRepository.saveAndFlush(asccpHistory);

        // to check RevisionTrackingNum
        latestHistoryAsccpList = asccpRepository.findAllWithLatestRevisionNumByCurrentAsccpId(currentAsccpId);
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
            throw new IllegalStateException("This operation only allows for the owner of this element.");
        }
        long currentAsccpId = asccp.getAsccpId();
        List<AssociationCoreComponentProperty> latestHistoryAsccpList =
                asccpRepository.findAllWithLatestRevisionNumByCurrentAsccpId(currentAsccpId);
        if (latestHistoryAsccpList == null) {
            throw new IllegalStateException("There is no history for this element.");
        }

        long asccpId = asccp.getAsccpId();
        coreComponentDAO.deleteByCurrentAsccpId(asccpId); // To remove history
        coreComponentDAO.delete(asccp);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void update(BasicCoreComponentProperty bccp, User requester) {
        long requesterId = requester.getAppUserId();
        long ownerId = bccp.getOwnerUserId();
        if (requesterId != ownerId) {
            throw new IllegalStateException("This operation only allows for the owner of this element.");
        }
        long currentBccpId = bccp.getBccpId();
        List<BasicCoreComponentProperty> latestHistoryBccpList =
                bccpRepository.findAllWithLatestRevisionNumByCurrentBccpId(currentBccpId);
        if (latestHistoryBccpList.isEmpty()) {
            throw new IllegalStateException("There is no history for this element.");
        }

        coreComponentDAO.save(bccp);
        bccRepository.findAllByToBccpId(bccp.getBccpId()).forEach(e -> {
            AggregateCoreComponent acc = accRepository.findOne(e.getFromAccId());
            e.setDen(acc, bccp);
            coreComponentDAO.save(e);
        });

        int latestRevisionTrackingNum = latestHistoryBccpList.stream()
                .mapToInt(e -> e.getRevisionTrackingNum())
                .max().orElse(0);
        int nextRevisionTrackingNum = latestRevisionTrackingNum + 1;
        BasicCoreComponentProperty bccpHistory = bccp.clone();
        bccpHistory.setRevisionNum(latestHistoryBccpList.get(0).getRevisionNum());
        bccpHistory.setRevisionTrackingNum(nextRevisionTrackingNum);
        bccpHistory.setRevisionAction(Update);
        bccpHistory.setLastUpdatedBy(requesterId);
        bccpHistory.setCurrentBccpId(currentBccpId);
        bccpHistory.setReleaseId(null);

        bccpRepository.saveAndFlush(bccpHistory);

        // to check RevisionTrackingNum
        latestHistoryBccpList = bccpRepository.findAllWithLatestRevisionNumByCurrentBccpId(currentBccpId);
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
            throw new IllegalStateException("This operation only allows for the owner of this element.");
        }
        long currentBccpId = bccp.getBccpId();
        List<BasicCoreComponentProperty> latestHistoryBccpList = bccpRepository.findAllWithLatestRevisionNumByCurrentBccpId(currentBccpId);
        if (latestHistoryBccpList.isEmpty()) {
            throw new IllegalStateException("There is no history for this element.");
        }

        long bccpId = bccp.getBccpId();
        coreComponentDAO.deleteByCurrentBccpId(bccpId); // To remove history
        coreComponentDAO.delete(bccp);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void discard(CoreComponents coreComponents, User requester) {
        switch (coreComponents.getType()) {
            case "ACC":
                AggregateCoreComponent acc = accRepository.findOne(coreComponents.getId());
                if (undoService.hasMultipleRevisions(acc)) {
                    undoService.revertToPreviousRevision(acc);
                } else {
                    discard(acc, requester);
                }
                break;

            case "ASCCP":
                AssociationCoreComponentProperty asccp = asccpRepository.findOne(coreComponents.getId());
                if (undoService.hasMultipleRevisions(asccp)) {
                    undoService.revertToPreviousRevision(asccp);
                } else {
                    discard(asccp, requester);
                }
                break;

            case "BCCP":
                BasicCoreComponentProperty bccp = bccpRepository.findOne(coreComponents.getId());
                if (undoService.hasMultipleRevisions(bccp)) {
                    undoService.revertToPreviousRevision(bccp);
                } else {
                    discard(bccp, requester);
                }
                break;

            default:
                throw new UnsupportedOperationException();
        }
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
            throw new IllegalStateException("This operation only allows for the owner of this element.");
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

        List<AssociationCoreComponent> asccList = asccRepository.findByFromAccIdAndRevisionNum(fromAccId, 0);
        for (AssociationCoreComponent ascc : asccList) {
            if (ascc.getState() != Published) {
                ascc.setState(state);
                ascc.setLastUpdatedBy(lastUpdatedBy);
            }
        }
        asccRepository.save(asccList.stream()
                .filter(e -> e.isDirty()).collect(Collectors.toList()));

        List<BasicCoreComponent> bccList = bccRepository.findByFromAccIdAndRevisionNum(fromAccId, 0);
        for (BasicCoreComponent bcc : bccList) {
            if (bcc.getState() != Published) {
                bcc.setState(state);
                bcc.setLastUpdatedBy(lastUpdatedBy);
            }
        }
        bccRepository.save(bccList.stream()
                .filter(e -> e.isDirty()).collect(Collectors.toList()));

        if (state == Published) {
            for (AssociationCoreComponent ascc : asccList) {
                deleteAsccHistoryRecords(fromAccId, ascc.getToAsccpId());
            }

            for (BasicCoreComponent bcc : bccList) {
                deleteBccHistoryRecords(fromAccId, bcc.getToBccpId());
                updateBccHistoryRecordState(fromAccId, bcc.getToBccpId(), state);
            }
        }

        for (AssociationCoreComponent ascc : asccList) {
            updateAsccHistoryRecordState(fromAccId, ascc.getToAsccpId(), state);
        }

        for (BasicCoreComponent bcc : bccList) {
            updateBccHistoryRecordState(fromAccId, bcc.getToBccpId(), state);
        }
    }

    private void updateAccState(AggregateCoreComponent acc,
                                CoreComponentState state,
                                User requester) {
        long lastUpdatedBy = requester.getAppUserId();

        if (acc.getState() != Published) {
            acc.setState(state);
            acc.setLastUpdatedBy(lastUpdatedBy);
            accRepository.save(acc);
        }

        updateChildrenState(acc, state, requester);

        if (state == Published) {
            deleteAccHistoryRecords(acc.getAccId());
        }
        updateAccHistoryRecordState(acc.getAccId(), state);
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

        if (state == Published) {
            deleteAsccpHistoryRecords(asccp.getAsccpId());
        }
        updateAsccpHistoryRecordState(asccp.getAsccpId(), state);

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

        if (state == Published) {
            deleteBccpHistoryRecords(bccp.getBccpId());
        }
        updateBccpHistoryRecordState(bccp.getBccpId(), state);
    }

    private void deleteAccHistoryRecords(long currentAccId) {
        int revisionNum = accRepository.findMaxRevisionNumByCurrentAccId(currentAccId);
        int revisionTrackingNum = accRepository.findMaxRevisionTrackingNumByCurrentAccIdAndRevisionNum(currentAccId, revisionNum);

        accRepository.deleteByCurrentAccIdAndRevisionNumAndNotRevisionTrackingNum(currentAccId, revisionNum, revisionTrackingNum);
    }

    private void updateAccHistoryRecordState(long currentAccId, CoreComponentState state) {
        int revisionNum = accRepository.findMaxRevisionNumByCurrentAccId(currentAccId);
        int revisionTrackingNum = accRepository.findMaxRevisionTrackingNumByCurrentAccIdAndRevisionNum(currentAccId, revisionNum);

        accRepository.updateStateByCurrentAccIdAndRevisionNumAndRevisionTrackingNum(currentAccId, revisionNum, revisionTrackingNum, state);
    }

    private void deleteAsccHistoryRecords(long fromAccId, long toAsccpid) {
        int revisionNum = asccRepository.findMaxRevisionNumByFromAccIdAndToAsccpId(fromAccId, toAsccpid);
        int revisionTrackingNum = asccRepository.findMaxRevisionTrackingNumByFromAccIdAndToAsccpIdAndRevisionNum(fromAccId, toAsccpid, revisionNum);

        asccRepository.deleteByFromAccIdAndToAsccpIdAndRevisionNumAndNotRevisionTrackingNum(fromAccId, toAsccpid, revisionNum, revisionTrackingNum);
    }

    private void updateAsccHistoryRecordState(long fromAccId, long toAsccpid, CoreComponentState state) {
        int revisionNum = asccRepository.findMaxRevisionNumByFromAccIdAndToAsccpId(fromAccId, toAsccpid);
        int revisionTrackingNum = asccRepository.findMaxRevisionTrackingNumByFromAccIdAndToAsccpIdAndRevisionNum(fromAccId, toAsccpid, revisionNum);

        asccRepository.updateStateByFromAccIdAndToAsccpIdAndRevisionNumAndNotRevisionTrackingNum(fromAccId, toAsccpid, revisionNum, revisionTrackingNum, state);
    }

    private void deleteBccHistoryRecords(long fromAccId, long toBccpId) {
        int revisionNum = bccRepository.findMaxRevisionNumByFromAccIdAndToBccpId(fromAccId, toBccpId);
        int revisionTrackingNum = bccRepository.findMaxRevisionTrackingNumByFromAccIdAndToBccpIdAndRevisionNum(fromAccId, toBccpId, revisionNum);

        bccRepository.deleteByFromAccIdAndToBccpIdAndRevisionNumAndNotRevisionTrackingNum(fromAccId, toBccpId, revisionNum, revisionTrackingNum);
    }

    private void updateBccHistoryRecordState(long fromAccId, long toBccpId, CoreComponentState state) {
        int revisionNum = bccRepository.findMaxRevisionNumByFromAccIdAndToBccpId(fromAccId, toBccpId);
        int revisionTrackingNum = bccRepository.findMaxRevisionTrackingNumByFromAccIdAndToBccpIdAndRevisionNum(fromAccId, toBccpId, revisionNum);

        bccRepository.deleteByFromAccIdAndToBccpIdAndRevisionNumAndNotRevisionTrackingNum(fromAccId, toBccpId, revisionNum, revisionTrackingNum, state);
    }

    private void deleteAsccpHistoryRecords(long currentAsccpId) {
        int revisionNum = asccpRepository.findMaxRevisionNumByCurrentAsccpId(currentAsccpId);
        int revisionTrackingNum = asccpRepository.findMaxRevisionTrackingNumByCurrentAsccpIdAndRevisionNum(currentAsccpId, revisionNum);

        asccpRepository.deleteByCurrentAsccpIdAndRevisionNumAndNotRevisionTrackingNum(currentAsccpId, revisionNum, revisionTrackingNum);
    }

    private void updateAsccpHistoryRecordState(long currentAsccpId, CoreComponentState state) {
        int revisionNum = asccpRepository.findMaxRevisionNumByCurrentAsccpId(currentAsccpId);
        int revisionTrackingNum = asccpRepository.findMaxRevisionTrackingNumByCurrentAsccpIdAndRevisionNum(currentAsccpId, revisionNum);

        asccpRepository.updateStateByCurrentAsccpIdAndRevisionNumAndRevisionTrackingNum(currentAsccpId, revisionNum, revisionTrackingNum, state);
    }

    private void deleteBccpHistoryRecords(long currentBccpId) {
        int revisionNum = bccpRepository.findMaxRevisionNumByCurrentBccpId(currentBccpId);
        int revisionTrackingNum = bccpRepository.findMaxRevisionTrackingNumByCurrentBccpIdAndRevisionNum(currentBccpId, revisionNum);

        bccpRepository.deleteByCurrentBccpIdAndRevisionNumAndNotRevisionTrackingNum(currentBccpId, revisionNum, revisionTrackingNum);
    }

    private void updateBccpHistoryRecordState(long currentBccpId, CoreComponentState state) {
        int revisionNum = bccpRepository.findMaxRevisionNumByCurrentBccpId(currentBccpId);
        int revisionTrackingNum = bccpRepository.findMaxRevisionTrackingNumByCurrentBccpIdAndRevisionNum(currentBccpId, revisionNum);

        bccpRepository.updateStateByCurrentBccpIdAndRevisionNumAndRevisionTrackingNum(currentBccpId, revisionNum, revisionTrackingNum, state);
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
            List<Long> toAsccpId = asccRepository.findToAsccpIdByFromAccId(fromAccIds);
            List<Long> roleOfAccIdList =
                    !toAsccpId.isEmpty() ? asccpRepository.findRoleOfAccIdByAsccpId(toAsccpId) : Collections.emptyList();
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
                    accId = accRepository.findBasedAccIdByAccId(accId);
                }
            });
            fromAccIds = tempAccIds;
        }
    }

    public int getMaxSeqKeyOfChildren(AggregateCoreComponent acc) {
        long accId = acc.getAccId();

        return Math.max(
                asccRepository.findByFromAccIdAndRevisionNum(accId, 0).stream()
                        .mapToInt(e -> e.getSeqKey()).max().orElse(0),
                bccRepository.findByFromAccIdAndRevisionNum(accId, 0).stream()
                        .mapToInt(e -> e.getSeqKey()).max().orElse(0)
        );
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

    public boolean hasMultipleRevisions(CoreComponents coreComponents) {
        return undoService.hasMultipleRevisions(coreComponents);
    }

    public String getFullRevisionNum(CoreComponents cc, Release release) {
        StringBuilder sb = new StringBuilder("");
        int maxRevisionNum = 0;
        int maxRevisionTrackingNum = 0;
        long releaseId = release.getReleaseId();

        if (releaseId == 0L) {
            return getFullRevisionNum(cc);
        }

        switch (cc.getType()) {
            case "ACC":
                maxRevisionNum = accRepository.findMaxRevisionNumByAccIdAndLessThanReleaseId(cc.getId(), releaseId);
                maxRevisionTrackingNum = accRepository.findMaxRevisionTrackingNumByAccIdAndRevisionNumAndLessThanReleaseId(cc.getId(), maxRevisionNum, releaseId);
                break;
            case "ASCC":
                AssociationCoreComponent ascc = asccRepository.findOne(cc.getId());
                maxRevisionNum = asccRepository.findMaxRevisionNumByFromAccIdAndToAsccpIdAndLessThanReleaseId(ascc.getFromAccId(), ascc.getToAsccpId(), releaseId);
                maxRevisionTrackingNum = asccRepository.findMaxRevisionTrackingNumByFromAccIdAndToAsccpIdAndRevisionNumAndLessThanReleaseId(ascc.getFromAccId(), ascc.getToAsccpId(), maxRevisionNum, releaseId);
                break;
            case "ASCCP":
                maxRevisionNum = asccpRepository.findMaxRevisionNumByAsccpIdAndLessThanReleaseId(cc.getId(), releaseId);
                maxRevisionTrackingNum = asccpRepository.findMaxRevisionTrackingNumByAsccpIdAndRevisionNumAndLessThanReleaseId(cc.getId(), maxRevisionNum, releaseId);
                break;
            case "BCC":
                BasicCoreComponent bcc = bccRepository.findOne(cc.getId());
                maxRevisionNum = bccRepository.findMaxRevisionNumByFromAccIdAndToBccpIdAndLessThanReleaseId(bcc.getFromAccId(), bcc.getToBccpId(), releaseId);
                maxRevisionTrackingNum = bccRepository.findMaxRevisionTrackingNumByFromAccIdAndToBccpIdAndRevisionNumAndLessThanReleaseId(bcc.getFromAccId(), bcc.getToBccpId(), maxRevisionNum, releaseId);
                break;
            case "BCCP":
                maxRevisionNum = bccpRepository.findMaxRevisionNumByBccpIdAndLessThanReleaseId(cc.getId(), releaseId);
                maxRevisionTrackingNum = bccpRepository.findMaxRevisionTrackingNumByBccpIdAndRevisionNumAndLessThanReleaseId(cc.getId(), maxRevisionNum, releaseId);
                break;
        }

        if (maxRevisionNum > 0) {
            sb.append(maxRevisionNum);
            sb.append(".");
            sb.append(maxRevisionTrackingNum);
        }

        return sb.toString();
    }

    public String getFullRevisionNum(CoreComponents cc) {
        StringBuilder sb = new StringBuilder("");
        int maxRevisionNum = 0;
        int maxRevisionTrackingNum = 0;

        switch (cc.getType()) {
            case "ACC":
                maxRevisionNum = accRepository.findMaxRevisionNumByCurrentAccId(cc.getId());
                maxRevisionTrackingNum = accRepository.findMaxRevisionTrackingNumByCurrentAccIdAndRevisionNum(cc.getId(), maxRevisionNum);
                break;
            case "ASCC":
                AssociationCoreComponent ascc = asccRepository.findOne(cc.getId());
                maxRevisionNum = asccRepository.findMaxRevisionNumByFromAccIdAndToAsccpId(ascc.getFromAccId(), ascc.getToAsccpId());
                maxRevisionTrackingNum = asccRepository.findMaxRevisionTrackingNumByFromAccIdAndToAsccpIdAndRevisionNum(ascc.getFromAccId(), ascc.getToAsccpId(), maxRevisionNum);
                break;
            case "ASCCP":
                maxRevisionNum = asccpRepository.findMaxRevisionNumByCurrentAsccpId(cc.getId());
                maxRevisionTrackingNum = asccpRepository.findMaxRevisionTrackingNumByCurrentAsccpIdAndRevisionNum(cc.getId(), maxRevisionNum);
                break;
            case "BCC":
                BasicCoreComponent bcc = bccRepository.findOne(cc.getId());
                maxRevisionNum = bccRepository.findMaxRevisionNumByFromAccIdAndToBccpId(bcc.getFromAccId(), bcc.getToBccpId());
                maxRevisionTrackingNum = bccRepository.findMaxRevisionTrackingNumByFromAccIdAndToBccpIdAndRevisionNum(bcc.getFromAccId(), bcc.getToBccpId(), maxRevisionNum);
                break;
            case "BCCP":
                maxRevisionNum = bccpRepository.findMaxRevisionNumByCurrentBccpId(cc.getId());
                maxRevisionTrackingNum = bccpRepository.findMaxRevisionTrackingNumByCurrentBccpIdAndRevisionNum(cc.getId(), maxRevisionNum);
                break;
        }

        if (maxRevisionNum > 0) {
            sb.append(maxRevisionNum);
            sb.append(".");
            sb.append(maxRevisionTrackingNum);
        }

        return sb.toString();
    }
}
