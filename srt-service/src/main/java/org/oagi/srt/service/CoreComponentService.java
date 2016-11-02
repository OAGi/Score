package org.oagi.srt.service;

import org.oagi.srt.provider.CoreComponentProvider;
import org.oagi.srt.repository.AssociationCoreComponentRepository;
import org.oagi.srt.repository.BasicCoreComponentRepository;
import org.oagi.srt.repository.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.List;

import static org.oagi.srt.repository.entity.RevisionAction.Update;

@Service
@Transactional(readOnly = true)
public class CoreComponentService {

    @Autowired
    private AssociationCoreComponentRepository asccRepository;

    @Autowired
    private BasicCoreComponentRepository bccRepository;

    public List<CoreComponent> getCoreComponents(
            AggregateCoreComponent acc, CoreComponentProvider coreComponentProvider) {
        long accId = acc.getAccId();
        return getCoreComponents(accId, coreComponentProvider);
    }

    public List<CoreComponent> getCoreComponents(
            long accId, CoreComponentProvider coreComponentProvider) {
        List<BasicCoreComponent> bcc_tmp_assoc = coreComponentProvider.getBCCs(accId);
        List<AssociationCoreComponent> ascc_tmp_assoc = coreComponentProvider.getASCCs(accId);

        List<CoreComponent> coreComponents = gatheringBySeqKey(bcc_tmp_assoc, ascc_tmp_assoc);
        return coreComponents;
    }

    public List<CoreComponent> getCoreComponentsWithoutAttributes(
            AggregateCoreComponent acc, CoreComponentProvider coreComponentProvider) {
        long accId = acc.getAccId();
        return getCoreComponentsWithoutAttributes(accId, coreComponentProvider);
    }

    public List<CoreComponent> getCoreComponentsWithoutAttributes(
            long accId, CoreComponentProvider coreComponentProvider) {
        List<BasicCoreComponent> bcc_tmp_assoc = coreComponentProvider.getBCCsWithoutAttributes(accId);
        List<AssociationCoreComponent> ascc_tmp_assoc = coreComponentProvider.getASCCs(accId);

        List<CoreComponent> coreComponents = gatheringBySeqKey(bcc_tmp_assoc, ascc_tmp_assoc);
        return coreComponents;
    }

    private List<CoreComponent> gatheringBySeqKey(
            List<BasicCoreComponent> bccList, List<AssociationCoreComponent> asccList
    ) {
        int size = bccList.size() + asccList.size();
        List<CoreComponent> tmp_assoc = new ArrayList(size);
        tmp_assoc.addAll(bccList);
        tmp_assoc.addAll(asccList);

        List<CoreComponent> coreComponents = Arrays.asList(new CoreComponent[size]);

        int attribute_cnt = 0;
        for (BasicCoreComponent basicCoreComponent : bccList) {
            if (basicCoreComponent.getSeqKey() == 0) {
                coreComponents.set(attribute_cnt, basicCoreComponent);
                attribute_cnt++;
            }
        }

        for (CoreComponent coreComponent : tmp_assoc) {
            if (coreComponent instanceof BasicCoreComponent) {
                BasicCoreComponent basicCoreComponent = (BasicCoreComponent) coreComponent;
                if (basicCoreComponent.getSeqKey() > 0) {
                    coreComponents.set(basicCoreComponent.getSeqKey() - 1 + attribute_cnt, basicCoreComponent);
                }
            } else {
                AssociationCoreComponent associationCoreComponent = (AssociationCoreComponent) coreComponent;
                coreComponents.set(associationCoreComponent.getSeqKey() - 1 + attribute_cnt, associationCoreComponent);
            }
        }

        return new ArrayList(coreComponents);
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

        switch (bcc.getEntityType()) {
            case Element:
                int seqKey = findAppropriateSeqKey(bcc);
                bcc.setSeqKey(seqKey);
                long fromAccId = bcc.getFromAccId();
                increaseSeqKeyGreaterThan(fromAccId, seqKey);
                break;
            case Attribute:
                bcc.setSeqKey(0);
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

    private int findAppropriateSeqKey(BasicCoreComponent latestBcc) {
        long bccId = latestBcc.getBccId();
        BasicCoreComponent latestHistory = bccRepository.findLatestOneByCurrentBccIdAndSeqKeyIsNotZero(bccId);
        return (latestHistory != null) ? latestHistory.getSeqKey() : 0;
    }

    private void increaseSeqKeyGreaterThan(long fromAccId, int seqKey) {
        asccRepository.increaseSeqKeyByFromAccIdAndSeqKey(fromAccId, seqKey);
        bccRepository.increaseSeqKeyByFromAccIdAndSeqKey(fromAccId, seqKey);
    }
}
