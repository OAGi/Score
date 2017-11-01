package org.oagi.srt.service;

import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReleaseService {

    @Autowired
    private ReleaseRepository releaseRepository;

    @Autowired
    private ReleasesRepository releasesRepository;

    @Autowired
    private AggregateCoreComponentRepository accRepository;

    @Autowired
    private CoreComponentsRepository coreComponentsRepository;

    @Autowired
    private AssociationCoreComponentRepository asccRepository;

    @Autowired
    private AssociationCoreComponentPropertyRepository asccpRepository;

    @Autowired
    private BasicCoreComponentRepository bccRepository;

    @Autowired
    private BasicCoreComponentPropertyRepository bccpRepository;

    public List<Release> findAll() {
        return releaseRepository.findAll();
    }

    public Release findById(long namespaceId) {
        return releaseRepository.findOne(namespaceId);
    }

    public List<Release> findAll(Sort.Direction direction, String property) {
        return releaseRepository.findAll(new Sort(new Sort.Order(direction, property)));
    }

    public List<Releases> findAllReleases() {
        return releasesRepository.findAll();
    }

    public boolean isExistsReleaseNum(String releaseNum, long releaseId) {
        return releaseRepository.existsByReleaseNumExceptReleaseId((releaseNum != null) ? releaseNum.trim() : null, releaseId);
    }

    @Transactional
    public Release update(Release release) {
        return releaseRepository.saveAndFlush(release);
    }

    public Release findByReleaseNum(String releaseNum) {
        return releaseRepository.findOneByReleaseNum(releaseNum);
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
                maxRevisionNum = accRepository.findMaxRevisionNumByAccIdAndReleaseId(cc.getId(), releaseId);
                maxRevisionTrackingNum = accRepository.findMaxRevisionTrackingNumByAccIdAndRevisionNumAndReleaseId(cc.getId(), maxRevisionNum, releaseId);
                break;
            case "ASCC":
                AssociationCoreComponent ascc = asccRepository.findOne(cc.getId());
                maxRevisionNum = asccRepository.findMaxRevisionNumByFromAccIdAndToAsccpIdAndReleaseId(ascc.getFromAccId(), ascc.getToAsccpId(), releaseId);
                maxRevisionTrackingNum = asccRepository.findMaxRevisionTrackingNumByFromAccIdAndToAsccpIdAndRevisionNumAndReleaseId(ascc.getFromAccId(), ascc.getToAsccpId(), maxRevisionNum, releaseId);
                break;
            case "ASCCP":
                maxRevisionNum = asccpRepository.findMaxRevisionNumByAsccpIdAndReleaseId(cc.getId(), releaseId);
                maxRevisionTrackingNum = asccpRepository.findMaxRevisionTrackingNumByAsccpIdAndRevisionNumAndReleaseId(cc.getId(), maxRevisionNum, releaseId);
                break;
            case "BCC":
                BasicCoreComponent bcc = bccRepository.findOne(cc.getId());
                maxRevisionNum = bccRepository.findMaxRevisionNumByFromAccIdAndToBccpIdAndReleaseId(bcc.getFromAccId(), bcc.getToBccpId(), releaseId);
                maxRevisionTrackingNum = bccRepository.findMaxRevisionTrackingNumByFromAccIdAndToBccpIdAndRevisionNumAndReleaseId(bcc.getFromAccId(), bcc.getToBccpId(), maxRevisionNum, releaseId);
                break;
            case "BCCP":
                maxRevisionNum = bccpRepository.findMaxRevisionNumByBccpIdAndReleaseId(cc.getId(), releaseId);
                maxRevisionTrackingNum = bccpRepository.findMaxRevisionTrackingNumByBccpIdAndRevisionNumAndReleaseId(cc.getId(), maxRevisionNum, releaseId);
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
                maxRevisionNum = accRepository.findMaxRevisionNumByAccId(cc.getId());
                maxRevisionTrackingNum = accRepository.findMaxRevisionTrackingNumByAccIdAndRevisionNum(cc.getId(), maxRevisionNum);
                break;
            case "ASCC":
                AssociationCoreComponent ascc = asccRepository.findOne(cc.getId());
                maxRevisionNum = asccRepository.findMaxRevisionNumByFromAccIdAndToAsccpId(ascc.getFromAccId(), ascc.getToAsccpId());
                maxRevisionTrackingNum = asccRepository.findMaxRevisionTrackingNumByFromAccIdAndToAsccpIdAndRevisionNum(ascc.getFromAccId(), ascc.getToAsccpId(), maxRevisionNum);
                break;
            case "ASCCP":
                maxRevisionNum = asccpRepository.findMaxRevisionNumByAsccpId(cc.getId());
                maxRevisionTrackingNum = asccpRepository.findMaxRevisionTrackingNumByAsccpIdAndRevisionNum(cc.getId(), maxRevisionNum);
                break;
            case "BCC":
                BasicCoreComponent bcc = bccRepository.findOne(cc.getId());
                maxRevisionNum = bccRepository.findMaxRevisionNumByFromAccIdAndToBccpId(bcc.getFromAccId(), bcc.getToBccpId());
                maxRevisionTrackingNum = bccRepository.findMaxRevisionTrackingNumByFromAccIdAndToBccpIdAndRevisionNum(bcc.getFromAccId(), bcc.getToBccpId(), maxRevisionNum);
                break;
            case "BCCP":
                maxRevisionNum = bccpRepository.findMaxRevisionNumByBccpId(cc.getId());
                maxRevisionTrackingNum = bccpRepository.findMaxRevisionTrackingNumByBccpIdAndRevisionNum(cc.getId(), maxRevisionNum);
                break;
        }

        if (maxRevisionNum > 0) {
            sb.append(maxRevisionNum);
            sb.append(".");
            sb.append(maxRevisionTrackingNum);
        }

        return sb.toString();
    }

    public List<CoreComponents> getDeltaForRelease(Release release) {
        return coreComponentsRepository.findDeltaForRelease(release);
    }


    public void addRevisionToRelease(CoreComponents cc, Release release) {
        switch (cc.getType()) {
            case "ACC":
                addAccRevisionToRelease(cc, release);
                break;
            case "ASCC":
                addAsccRevisionToRelease(cc, release);
                break;
            case "ASCCP":
                addAsccpRevisionToRelease(cc, release);
                break;
            case "BCC":
                addBccRevisionToRelease(cc, release);
                break;
            case "BCCP":
                addBccpRevisionToRelease(cc, release);
                break;
        }
    }

    private void addBccpRevisionToRelease(CoreComponents cc, Release release) {
        bccpRepository.updateReleaseByBccpId(cc.getId(), release.getReleaseId());

        int revisionNum = bccpRepository.findRevisionNumByBccpId(cc.getId());
        List<BasicCoreComponentProperty> previousNonreleased = bccpRepository.findPreviousNonReleasedRevisions(cc.getId(), revisionNum);
        List<BasicCoreComponentProperty> followingNonreleased = bccpRepository.findFollowingNonReleasedRevisions(cc.getId(), revisionNum);

        for (BasicCoreComponentProperty bccp : previousNonreleased) { // remove previous non-released revisions
            bccpRepository.delete(bccp.getId());
            bccpRepository.flush();
        }

        for (BasicCoreComponentProperty bccp : followingNonreleased) { // nullify following non-released revisions
            bccpRepository.updateReleaseByBccpId(bccp.getId(), null);
        }
    }

    private void addBccRevisionToRelease(CoreComponents cc, Release release) {
        bccRepository.updateReleaseByBccId(cc.getId(), release.getReleaseId());

        int revisionNum = bccRepository.findRevisionNumByBccId(cc.getId());
        List<BasicCoreComponent> previousNonreleased = bccRepository.findPreviousNonReleasedRevisions(cc.getId(), revisionNum);
        List<BasicCoreComponent> followingNonreleased = bccRepository.findFollowingNonReleasedRevisions(cc.getId(), revisionNum);

        for (BasicCoreComponent bcc : previousNonreleased) { // remove previous non-released revisions
            bccRepository.delete(bcc.getId());
            bccRepository.flush();
        }

        for (BasicCoreComponent bcc : followingNonreleased) { // nullify following non-released revisions
            bccRepository.updateReleaseByBccId(bcc.getId(), null);
        }
    }

    private void addBccRevisionToRelease(Long bccId, Release release) {
        bccRepository.updateReleaseByBccId(bccId, release.getReleaseId());

        int revisionNum = bccRepository.findRevisionNumByBccId(bccId);
        List<BasicCoreComponent> previousNonreleased = bccRepository.findPreviousNonReleasedRevisions(bccId, revisionNum);
        List<BasicCoreComponent> followingNonreleased = bccRepository.findFollowingNonReleasedRevisions(bccId, revisionNum);

        for (BasicCoreComponent bcc : previousNonreleased) { // remove previous non-released revisions
            bccRepository.delete(bcc.getId());
            bccRepository.flush();
        }

        for (BasicCoreComponent bcc : followingNonreleased) { // nullify following non-released revisions
            bccRepository.updateReleaseByBccId(bcc.getId(), null);
        }
    }

    private void addAsccpRevisionToRelease(CoreComponents cc, Release release) {
        asccpRepository.updateReleaseByAsccpId(cc.getId(), release.getReleaseId());

        int revisionNum = asccpRepository.findRevisionNumByAsccpId(cc.getId());
        List<AssociationCoreComponentProperty> previousNonreleased = asccpRepository.findPreviousNonReleasedRevisions(cc.getId(), revisionNum);
        List<AssociationCoreComponentProperty> followingNonreleased = asccpRepository.findFollowingNonReleasedRevisions(cc.getId(), revisionNum);

        for (AssociationCoreComponentProperty asccp : previousNonreleased) { // remove previous non-released revisions
            asccpRepository.delete(asccp.getId());
            asccpRepository.flush();
        }

        for (AssociationCoreComponentProperty asccp : followingNonreleased) { // nullify following non-released revisions
            asccpRepository.updateReleaseByAsccpId(asccp.getId(), null);
        }
    }

    private void addAsccRevisionToRelease(CoreComponents cc, Release release) {
        asccRepository.updateReleaseByAsccId(cc.getId(), release.getReleaseId());

        int revisionNum = asccRepository.findRevisionNumByAsccId(cc.getId());
        List<AssociationCoreComponent> previousNonreleased = asccRepository.findPreviousNonReleasedRevisions(cc.getId(), revisionNum);
        List<AssociationCoreComponent> followingNonreleased = asccRepository.findFollowingNonReleasedRevisions(cc.getId(), revisionNum);

        for (AssociationCoreComponent ascc : previousNonreleased) { // remove previous non-released revisions
            asccRepository.delete(ascc.getId());
            asccRepository.flush();
        }

        for (AssociationCoreComponent ascc : followingNonreleased) { // nullify following non-released revisions
            asccRepository.updateReleaseByAsccId(ascc.getId(), null);
        }
    }

    private void addAsccRevisionToRelease(Long asccId, Release release) {
        asccRepository.updateReleaseByAsccId(asccId, release.getReleaseId());

        int revisionNum = asccRepository.findRevisionNumByAsccId(asccId);
        List<AssociationCoreComponent> previousNonreleased = asccRepository.findPreviousNonReleasedRevisions(asccId, revisionNum);
        List<AssociationCoreComponent> followingNonreleased = asccRepository.findFollowingNonReleasedRevisions(asccId, revisionNum);

        for (AssociationCoreComponent ascc : previousNonreleased) { // remove previous non-released revisions
            asccRepository.delete(ascc.getId());
            asccRepository.flush();
        }

        for (AssociationCoreComponent ascc : followingNonreleased) { // nullify following non-released revisions
            asccRepository.updateReleaseByAsccId(ascc.getId(), null);
        }
    }

    private void addAccRevisionToRelease(CoreComponents cc, Release release) {
        accRepository.updateReleaseByAccId(cc.getId(), release.getReleaseId());

        int revisionNum = accRepository.findRevisionNumByAccId(cc.getId());
        List<AggregateCoreComponent> previousNonreleased = accRepository.findPreviousNonReleasedRevisions(cc.getId(), revisionNum);
        List<AggregateCoreComponent> followingNonreleased = accRepository.findFollowingNonReleasedRevisions(cc.getId(), revisionNum);

        for (AggregateCoreComponent acc : previousNonreleased) { // remove previous non-released revisions
            accRepository.delete(acc.getId());
            accRepository.flush();
        }

        for (AggregateCoreComponent acc : followingNonreleased) { // nullify following non-released revisions
            accRepository.updateReleaseByAccId(acc.getId(), null);
        }

        // add children associations to release
//        AggregateCoreComponent acc = accRepository.findOne(cc.getId());
//
//        List<AssociationCoreComponent> asccList = asccRepository.findByFromAccId(acc.getCurrentAccId());
//        for (AssociationCoreComponent ascc : asccList) {
//            addAsccRevisionToRelease(ascc.getAsccId(), release);
//        }
//
//        List<BasicCoreComponent> bccList = bccRepository.findByFromAccId(acc.getCurrentAccId());
//        for (BasicCoreComponent bcc : bccList) {
//            addBccRevisionToRelease(bcc.getBccId(), release);
//        }
    }

    public void makeReleaseFinal(Releases release, boolean purge) {
        List<Release> currentDraftReleases = releaseRepository.findByState(release.getState());

        for (Release r : currentDraftReleases) {
            if (r.getReleaseId() == release.getReleaseId()) { //  final release
                r.setState(ReleaseState.Final);
                releaseRepository.save(r);
                releaseRepository.flush();
                continue;
            }

            if (r.getLastUpdateTimestamp().before(release.getLastUpdateTimestamp())) { // draft is before final release
                moveCCsBetweenReleases(r, release);
            }

            if (r.getLastUpdateTimestamp().after(release.getLastUpdateTimestamp())) { // draft is after final release
                if (purge) {
                    deleteCCsByRelease(r);
                } else {
                    moveCCsBetweenReleases(r, null);
                }
            }

            releaseRepository.delete(r.getReleaseId());
            releaseRepository.flush();
        }

        moveCCsToPublishedStateByRelease(release.getReleaseId());
    }

    private void moveCCsToPublishedStateByRelease(Long fromRelId) {
        List<AggregateCoreComponent> accs = accRepository.findByReleaseId(fromRelId);
        List<AssociationCoreComponent> asccs = asccRepository.findByReleaseId(fromRelId);
        List<AssociationCoreComponentProperty> asccps = asccpRepository.findByReleaseId(fromRelId);
        List<BasicCoreComponent> bccs = bccRepository.findByReleaseId(fromRelId);
        List<BasicCoreComponentProperty> bccps = bccpRepository.findByReleaseId(fromRelId);

        for (AggregateCoreComponent acc : accs) {
            acc.setState(CoreComponentState.Published);
            accRepository.saveAndFlush(acc);

            AggregateCoreComponent currentACC = accRepository.findOne(acc.getCurrentAccId());
            currentACC.setState(CoreComponentState.Published);
            accRepository.saveAndFlush(currentACC);

            //move all children assocs to publish state
            List<AssociationCoreComponent> asccList = asccRepository.findByFromAccId(acc.getCurrentAccId());
            for (AssociationCoreComponent ascc : asccList) {
                ascc.setState(CoreComponentState.Published);
                asccRepository.saveAndFlush(ascc);
            }

            List<BasicCoreComponent> bccList = bccRepository.findByFromAccId(acc.getCurrentAccId());
            for (BasicCoreComponent bcc : bccList) {
                bcc.setState(CoreComponentState.Published);
                bccRepository.saveAndFlush(bcc);
            }

            List<AssociationCoreComponent> asccList2 = asccRepository.findByFromAccId(currentACC.getCurrentAccId());
            for (AssociationCoreComponent ascc : asccList2) {
                ascc.setState(CoreComponentState.Published);
                asccRepository.saveAndFlush(ascc);
            }

            List<BasicCoreComponent> bccList2 = bccRepository.findByFromAccId(currentACC.getCurrentAccId());
            for (BasicCoreComponent bcc : bccList2) {
                bcc.setState(CoreComponentState.Published);
                bccRepository.saveAndFlush(bcc);
            }
        }

        for (AssociationCoreComponent ascc : asccs) {
            ascc.setState(CoreComponentState.Published);
            asccRepository.saveAndFlush(ascc);

            AssociationCoreComponent currentAscc = asccRepository.findOne(ascc.getCurrentAsccId());
            currentAscc.setState(CoreComponentState.Published);
            asccRepository.saveAndFlush(currentAscc);
        }

        for (AssociationCoreComponentProperty asccp : asccps) {
            asccp.setState(CoreComponentState.Published);
            asccpRepository.saveAndFlush(asccp);

            AssociationCoreComponentProperty currentAsccp = asccpRepository.findOne(asccp.getCurrentAsccpId());
            currentAsccp.setState(CoreComponentState.Published);
            asccpRepository.saveAndFlush(currentAsccp);
        }

        for (BasicCoreComponent bcc : bccs) {
            bcc.setState(CoreComponentState.Published);
            bccRepository.saveAndFlush(bcc);

            BasicCoreComponent currentBcc = bccRepository.findOne(bcc.getCurrentBccId());
            currentBcc.setState(CoreComponentState.Published);
            bccRepository.saveAndFlush(currentBcc);
        }

        for (BasicCoreComponentProperty bccp : bccps) {
            bccp.setState(CoreComponentState.Published);
            bccpRepository.saveAndFlush(bccp);

            BasicCoreComponentProperty currentBccp = bccpRepository.findOne(bccp.getCurrentBccpId());
            currentBccp.setState(CoreComponentState.Published);
            bccpRepository.saveAndFlush(currentBccp);
        }
    }

    private void deleteCCsByRelease(Release fromRel) {
        List<AggregateCoreComponent> accs = accRepository.findByReleaseId(fromRel.getReleaseId());
        List<AssociationCoreComponent> asccs = asccRepository.findByReleaseId(fromRel.getReleaseId());
        List<AssociationCoreComponentProperty> asccps = asccpRepository.findByReleaseId(fromRel.getReleaseId());
        List<BasicCoreComponent> bccs = bccRepository.findByReleaseId(fromRel.getReleaseId());
        List<BasicCoreComponentProperty> bccps = bccpRepository.findByReleaseId(fromRel.getReleaseId());

        for (AggregateCoreComponent acc : accs) {
            accRepository.delete(acc);
            accRepository.flush();

            if (accRepository.findByCurrentAccId(acc.getCurrentAccId()).isEmpty()) { // acc was the only revision, remove current as well
                accRepository.delete(acc.getCurrentAccId());
                accRepository.flush();
            }
        }

        for (AssociationCoreComponent ascc : asccs) {
            asccRepository.delete(ascc);
            asccRepository.flush();

            if (asccRepository.findByCurrentAsccId(ascc.getCurrentAsccId()).isEmpty()) { // ascc was the only revision, remove current as well
                asccRepository.delete(ascc.getCurrentAsccId());
                asccRepository.flush();
            }
        }

        for (AssociationCoreComponentProperty asccp : asccps) {
            asccpRepository.delete(asccp);
            asccpRepository.flush();

            if (asccpRepository.findByCurrentAsccpId(asccp.getCurrentAsccpId()).isEmpty()) { // acsccp was the only revision, remove current as well
                asccpRepository.delete(asccp.getCurrentAsccpId());
                asccpRepository.flush();
            }
        }

        for (BasicCoreComponent bcc : bccs) {
            bccRepository.delete(bcc);
            bccRepository.flush();

            if (bccRepository.findByCurrentBccId(bcc.getCurrentBccId()).isEmpty()) { // bcc was the only revision, remove current as well
                bccRepository.delete(bcc.getCurrentBccId());
                bccRepository.flush();
            }
        }

        for (BasicCoreComponentProperty bccp : bccps) {
            bccpRepository.delete(bccp);
            bccpRepository.flush();

            if (bccpRepository.findByCurrentBccpId(bccp.getCurrentBccpId()).isEmpty()) { // bccp was the only revision, remove current as well
                bccpRepository.delete(bccp.getCurrentBccpId());
                bccpRepository.flush();
            }
        }
    }

    private void moveCCsBetweenReleases(Release fromRel, Releases toRel) {
        List<AggregateCoreComponent> accs = accRepository.findByReleaseId(fromRel.getReleaseId());
        List<AssociationCoreComponent> asccs = asccRepository.findByReleaseId(fromRel.getReleaseId());
        List<AssociationCoreComponentProperty> asccps = asccpRepository.findByReleaseId(fromRel.getReleaseId());
        List<BasicCoreComponent> bccs = bccRepository.findByReleaseId(fromRel.getReleaseId());
        List<BasicCoreComponentProperty> bccps = bccpRepository.findByReleaseId(fromRel.getReleaseId());

        Long releaseId = (toRel == null) ? null : toRel.getReleaseId();

        for (AggregateCoreComponent acc : accs) {
            acc.setReleaseId(releaseId);
            acc.setState(CoreComponentState.Published);
            accRepository.saveAndFlush(acc);

            AggregateCoreComponent currentACC = accRepository.findOne(acc.getCurrentAccId());
            if (currentACC != null) {
                currentACC.setState(CoreComponentState.Published);
                accRepository.saveAndFlush(currentACC);
            }
        }

        for (AssociationCoreComponent ascc : asccs) {
            ascc.setReleaseId(releaseId);
            ascc.setState(CoreComponentState.Published);
            asccRepository.saveAndFlush(ascc);

            AssociationCoreComponent currentAscc = asccRepository.findOne(ascc.getCurrentAsccId());
            if (currentAscc != null) {
                currentAscc.setState(CoreComponentState.Published);
                asccRepository.saveAndFlush(currentAscc);
            }
        }

        for (AssociationCoreComponentProperty asccp : asccps) {
            asccp.setReleaseId(releaseId);
            asccp.setState(CoreComponentState.Published);
            asccpRepository.saveAndFlush(asccp);

            AssociationCoreComponentProperty currentAsccp = asccpRepository.findOne(asccp.getCurrentAsccpId());
            if (currentAsccp != null) {
                currentAsccp.setState(CoreComponentState.Published);
                asccpRepository.saveAndFlush(currentAsccp);
            }
        }

        for (BasicCoreComponent bcc : bccs) {
            bcc.setReleaseId(releaseId);
            bcc.setState(CoreComponentState.Published);
            bccRepository.saveAndFlush(bcc);

            BasicCoreComponent currentBcc = bccRepository.findOne(bcc.getCurrentBccId());
            if (currentBcc != null) {
                currentBcc.setState(CoreComponentState.Published);
                bccRepository.saveAndFlush(currentBcc);
            }
        }

        for (BasicCoreComponentProperty bccp : bccps) {
            bccp.setReleaseId(releaseId);
            bccp.setState(CoreComponentState.Published);
            bccpRepository.saveAndFlush(bccp);

            BasicCoreComponentProperty currentBccp = bccpRepository.findOne(bccp.getCurrentBccpId());
            if (currentBccp != null) {
                currentBccp.setState(CoreComponentState.Published);
                bccpRepository.saveAndFlush(currentBccp);
            }
        }

        removeMultipleRevisionsFromRelease(releaseId);
    }

    /**
     * Check if release contains multiple revisons of the same component and remove all of them, but the latest one.
     */
    private void removeMultipleRevisionsFromRelease(Long releaseId) {
        if (releaseId == null) {
            return;
        }

        HashMap<Long, CoreComponent> ccs = new HashMap<>();

        List<AggregateCoreComponent> accs = accRepository.findByReleaseId(releaseId);
        List<AssociationCoreComponent> asccs = asccRepository.findByReleaseId(releaseId);
        List<AssociationCoreComponentProperty> asccps = asccpRepository.findByReleaseId(releaseId);
        List<BasicCoreComponent> bccs = bccRepository.findByReleaseId(releaseId);
        List<BasicCoreComponentProperty> bccps = bccpRepository.findByReleaseId(releaseId);

        for (AggregateCoreComponent acc : accs) {
            if (ccs.get(acc.getCurrentAccId()) == null) { // no other revision for this component
                ccs.put(acc.getCurrentAccId(), acc);
            } else { // there is another revision of the component - compare and remove the older one
                AggregateCoreComponent accFromMap = (AggregateCoreComponent) ccs.get(acc.getCurrentAccId());
                removeOlderRevision(accFromMap, acc);
            }
        }

        for (AssociationCoreComponent ascc : asccs) {
            if (ccs.get(ascc.getCurrentAsccId()) == null) {
                ccs.put(ascc.getCurrentAsccId(), ascc);
            } else {
                AssociationCoreComponent asccFromMap = (AssociationCoreComponent) ccs.get(ascc.getCurrentAsccId());
                removeOlderRevision(asccFromMap, ascc);
            }
        }

        for (AssociationCoreComponentProperty asccp : asccps) {
            if (ccs.get(asccp.getCurrentAsccpId()) == null) {
                ccs.put(asccp.getCurrentAsccpId(), asccp);
            } else {
                AssociationCoreComponentProperty asccpFromMap = (AssociationCoreComponentProperty) ccs.get(asccp.getCurrentAsccpId());
                removeOlderRevision(asccpFromMap, asccp);
            }
        }

        for (BasicCoreComponent bcc : bccs) {
            if (ccs.get(bcc.getCurrentBccId()) == null) {
                ccs.put(bcc.getCurrentBccId(), bcc);
            } else {
                BasicCoreComponent bccFromMap = (BasicCoreComponent) ccs.get(bcc.getCurrentBccId());
                removeOlderRevision(bccFromMap, bcc);
            }
        }

        for (BasicCoreComponentProperty bccp : bccps) {
            if (ccs.get(bccp.getCurrentBccpId()) == null) {
                ccs.put(bccp.getCurrentBccpId(), bccp);
            } else {
                BasicCoreComponentProperty bccpFromMap = (BasicCoreComponentProperty) ccs.get(bccp.getCurrentBccpId());
                removeOlderRevision(bccpFromMap, bccp);
            }
        }

    }

    private void removeOlderRevision(BasicCoreComponentProperty firstBccp, BasicCoreComponentProperty secondBccp) {
        if (firstBccp.getCurrentBccpId() != secondBccp.getCurrentBccpId() ||
                (firstBccp.getRevisionNum() == secondBccp.getRevisionNum() && firstBccp.getRevisionTrackingNum() == secondBccp.getRevisionTrackingNum())) {
            return;
        }

        if (firstBccp.getRevisionNum() > secondBccp.getRevisionNum()) { // first older
            bccpRepository.delete(secondBccp);
            bccpRepository.flush();
        } else if (firstBccp.getRevisionNum() < secondBccp.getRevisionNum()) { // second older
            bccpRepository.delete(firstBccp);
            bccpRepository.flush();
        } else if (firstBccp.getRevisionTrackingNum() > secondBccp.getRevisionTrackingNum()) { // equal revisions, compare tracking nums; first older
            bccpRepository.delete(secondBccp);
            bccpRepository.flush();
        } else { // equal revisions, compare tracking nums; second older
            bccpRepository.delete(firstBccp);
            bccpRepository.flush();
        }
    }

    private void removeOlderRevision(BasicCoreComponent firstBcc, BasicCoreComponent secondBcc) {
        if (firstBcc.getCurrentBccId() != secondBcc.getCurrentBccId() ||
                (firstBcc.getRevisionNum() == secondBcc.getRevisionNum() && firstBcc.getRevisionTrackingNum() == secondBcc.getRevisionTrackingNum())) {
            return;
        }

        if (firstBcc.getRevisionNum() > secondBcc.getRevisionNum()) { // first older
            bccRepository.delete(secondBcc);
            bccRepository.flush();
        } else if (firstBcc.getRevisionNum() < secondBcc.getRevisionNum()) { // second older
            bccRepository.delete(firstBcc);
            bccRepository.flush();
        } else if (firstBcc.getRevisionTrackingNum() > secondBcc.getRevisionTrackingNum()) { // equal revisions, compare tracking nums; first older
            bccRepository.delete(secondBcc);
            bccRepository.flush();
        } else { // equal revisions, compare tracking nums; second older
            bccRepository.delete(firstBcc);
            bccRepository.flush();
        }
    }

    private void removeOlderRevision(AssociationCoreComponentProperty firstAsccp, AssociationCoreComponentProperty secondAsccp) {
        if (firstAsccp.getCurrentAsccpId() != secondAsccp.getCurrentAsccpId() ||
                (firstAsccp.getRevisionNum() == secondAsccp.getRevisionNum() && firstAsccp.getRevisionTrackingNum() == secondAsccp.getRevisionTrackingNum())) {
            return;
        }

        if (firstAsccp.getRevisionNum() > secondAsccp.getRevisionNum()) { // first older
            asccpRepository.delete(secondAsccp);
            asccpRepository.flush();
        } else if (firstAsccp.getRevisionNum() < secondAsccp.getRevisionNum()) { // second older
            asccpRepository.delete(firstAsccp);
            asccpRepository.flush();
        } else if (firstAsccp.getRevisionTrackingNum() > secondAsccp.getRevisionTrackingNum()) { // equal revisions, compare tracking nums; first older
            asccpRepository.delete(secondAsccp);
            asccpRepository.flush();
        } else { // equal revisions, compare tracking nums; second older
            asccpRepository.delete(firstAsccp);
            asccpRepository.flush();
        }
    }

    private void removeOlderRevision(AssociationCoreComponent firstAscc, AssociationCoreComponent secondAscc) {
        if (firstAscc.getCurrentAsccId() != secondAscc.getCurrentAsccId() ||
                (firstAscc.getRevisionNum() == secondAscc.getRevisionNum() && firstAscc.getRevisionTrackingNum() == secondAscc.getRevisionTrackingNum())) {
            return;
        }

        if (firstAscc.getRevisionNum() > secondAscc.getRevisionNum()) { // first older
            asccRepository.delete(secondAscc);
            asccRepository.flush();
        } else if (firstAscc.getRevisionNum() < secondAscc.getRevisionNum()) { // second older
            asccRepository.delete(firstAscc);
            asccRepository.flush();
        } else if (firstAscc.getRevisionTrackingNum() > secondAscc.getRevisionTrackingNum()) { // equal revisions, compare tracking nums; first older
            asccRepository.delete(secondAscc);
            asccRepository.flush();
        } else { // equal revisions, compare tracking nums; second older
            asccRepository.delete(firstAscc);
            asccRepository.flush();
        }
    }

    private void removeOlderRevision(AggregateCoreComponent firstAcc, AggregateCoreComponent secondAcc) {
        if (firstAcc.getCurrentAccId() != secondAcc.getCurrentAccId() ||
                (firstAcc.getRevisionNum() == secondAcc.getRevisionNum() && firstAcc.getRevisionTrackingNum() == secondAcc.getRevisionTrackingNum())) {
            return;
        }

        if (firstAcc.getRevisionNum() > secondAcc.getRevisionNum()) { // first older
            accRepository.delete(secondAcc);
            accRepository.flush();
        } else if (firstAcc.getRevisionNum() < secondAcc.getRevisionNum()) { // second older
            accRepository.delete(firstAcc);
            accRepository.flush();
        } else if (firstAcc.getRevisionTrackingNum() > secondAcc.getRevisionTrackingNum()) { // equal revisions, compare tracking nums; first older
            accRepository.delete(secondAcc);
            accRepository.flush();
        } else { // equal revisions, compare tracking nums; second older
            accRepository.delete(firstAcc);
            accRepository.flush();
        }
    }

    public void delete(Release releaseToDelete) {
        List<Release> currentDraftReleases = releaseRepository.findByState(releaseToDelete.getState());
        Collections.reverse(currentDraftReleases);
        Releases followingRelease = null;

        for (int i = 0; i < currentDraftReleases.size() - 1; i++) {
            if (currentDraftReleases.get(i).getReleaseId() == releaseToDelete.getReleaseId()) {
                followingRelease = new Releases();
                followingRelease.setReleaseId(currentDraftReleases.get(i + 1).getReleaseId());
                break;
            }
        }

        if (followingRelease != null) {
            moveCCsBetweenReleases(releaseToDelete, followingRelease);
        } else {
            moveCCsBetweenReleases(releaseToDelete, null);
        }

        releaseRepository.delete(releaseToDelete);
        releaseRepository.flush();
    }
}

