package org.oagi.srt.service;

import org.oagi.srt.common.util.VersionStringComparator;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
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
        return releaseRepository.findById(namespaceId).orElse(null);
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
                AssociationCoreComponent ascc = asccRepository.findById(cc.getId()).orElse(null);
                maxRevisionNum = asccRepository.findMaxRevisionNumByFromAccIdAndToAsccpIdAndReleaseId(ascc.getFromAccId(), ascc.getToAsccpId(), releaseId);
                maxRevisionTrackingNum = asccRepository.findMaxRevisionTrackingNumByFromAccIdAndToAsccpIdAndRevisionNumAndReleaseId(ascc.getFromAccId(), ascc.getToAsccpId(), maxRevisionNum, releaseId);
                break;
            case "ASCCP":
                maxRevisionNum = asccpRepository.findMaxRevisionNumByAsccpIdAndReleaseId(cc.getId(), releaseId);
                maxRevisionTrackingNum = asccpRepository.findMaxRevisionTrackingNumByAsccpIdAndRevisionNumAndReleaseId(cc.getId(), maxRevisionNum, releaseId);
                break;
            case "BCC":
                BasicCoreComponent bcc = bccRepository.findById(cc.getId()).orElse(null);
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
                AssociationCoreComponent ascc = asccRepository.findById(cc.getId()).orElse(null);
                maxRevisionNum = asccRepository.findMaxRevisionNumByFromAccIdAndToAsccpId(ascc.getFromAccId(), ascc.getToAsccpId());
                maxRevisionTrackingNum = asccRepository.findMaxRevisionTrackingNumByFromAccIdAndToAsccpIdAndRevisionNum(ascc.getFromAccId(), ascc.getToAsccpId(), maxRevisionNum);
                break;
            case "ASCCP":
                maxRevisionNum = asccpRepository.findMaxRevisionNumByAsccpId(cc.getId());
                maxRevisionTrackingNum = asccpRepository.findMaxRevisionTrackingNumByAsccpIdAndRevisionNum(cc.getId(), maxRevisionNum);
                break;
            case "BCC":
                BasicCoreComponent bcc = bccRepository.findById(cc.getId()).orElse(null);
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
        List<CoreComponents> delta = coreComponentsRepository.findDeltaForRelease(release);
        delta.removeIf(cc -> !isLatestRevision(cc.getCoreComponentsId()));
        return delta;
    }

    private boolean isLatestRevision(CoreComponentsId ccId){
        int maxRevisionNum;
        int maxRevisionTrackingNum;

        switch (ccId.getType()) {
            case "ACC":
                AggregateCoreComponent acc = accRepository.findById(ccId.getId()).orElse(null);
                maxRevisionNum = accRepository.findMaxRevisionNumByCurrentAccId(acc.getCurrentAccId());
                maxRevisionTrackingNum = accRepository.findMaxRevisionTrackingNumByCurrentAccIdAndRevisionNum(acc.getCurrentAccId(), maxRevisionNum);
                return acc.getRevisionNum() == maxRevisionNum && acc.getRevisionTrackingNum() == maxRevisionTrackingNum;
            case "ASCC":
                AssociationCoreComponent ascc = asccRepository.findById(ccId.getId()).orElse(null);
                maxRevisionNum = asccRepository.findMaxRevisionNumByFromAccIdAndToAsccpId(ascc.getFromAccId(), ascc.getToAsccpId());
                maxRevisionTrackingNum = asccRepository.findMaxRevisionTrackingNumByFromAccIdAndToAsccpIdAndRevisionNum(ascc.getFromAccId(), ascc.getToAsccpId(), maxRevisionNum);
                return ascc.getRevisionNum() == maxRevisionNum && ascc.getRevisionTrackingNum() == maxRevisionTrackingNum;
            case "ASCCP":
                AssociationCoreComponentProperty asccp = asccpRepository.findById(ccId.getId()).orElse(null);
                maxRevisionNum = asccpRepository.findMaxRevisionNumByCurrentAsccpId(asccp.getCurrentAsccpId());
                maxRevisionTrackingNum = asccpRepository.findMaxRevisionTrackingNumByCurrentAsccpIdAndRevisionNum(asccp.getCurrentAsccpId(), maxRevisionNum);
                return asccp.getRevisionNum() == maxRevisionNum && asccp.getRevisionTrackingNum() == maxRevisionTrackingNum;
            case "BCC":
                BasicCoreComponent bcc = bccRepository.findById(ccId.getId()).orElse(null);
                maxRevisionNum = bccRepository.findMaxRevisionNumByFromAccIdAndToBccpId(bcc.getFromAccId(), bcc.getToBccpId());
                maxRevisionTrackingNum = bccRepository.findMaxRevisionTrackingNumByFromAccIdAndToBccpIdAndRevisionNum(bcc.getFromAccId(), bcc.getToBccpId(), maxRevisionNum);
                return bcc.getRevisionNum() == maxRevisionNum && bcc.getRevisionTrackingNum() == maxRevisionTrackingNum;
            case "BCCP":
                BasicCoreComponentProperty bccp = bccpRepository.findById(ccId.getId()).orElse(null);
                maxRevisionNum = bccpRepository.findMaxRevisionNumByCurrentBccpId(bccp.getCurrentBccpId());
                maxRevisionTrackingNum = bccpRepository.findMaxRevisionTrackingNumByCurrentBccpIdAndRevisionNum(bccp.getCurrentBccpId(), maxRevisionNum);
                return bccp.getRevisionNum() == maxRevisionNum && bccp.getRevisionTrackingNum() == maxRevisionTrackingNum;
        }

        return false;
    }

    @Transactional
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
    }

    private void addBccRevisionToRelease(CoreComponents cc, Release release) {
        bccRepository.updateReleaseByBccId(cc.getId(), release.getReleaseId());
    }

    private void addBccRevisionToRelease(Long bccId, Release release) {
        bccRepository.updateReleaseByBccId(bccId, release.getReleaseId());
    }

    private void addAsccpRevisionToRelease(CoreComponents cc, Release release) {
        asccpRepository.updateReleaseByAsccpId(cc.getId(), release.getReleaseId());
    }

    private void addAsccRevisionToRelease(CoreComponents cc, Release release) {
        asccRepository.updateReleaseByAsccId(cc.getId(), release.getReleaseId());
    }

    private void addAsccRevisionToRelease(Long asccId, Release release) {
        asccRepository.updateReleaseByAsccId(asccId, release.getReleaseId());
    }

    private void addAccRevisionToRelease(CoreComponents cc, Release release) {
        accRepository.updateReleaseByAccId(cc.getId(), release.getReleaseId());
    }

    @Transactional
    public void makeReleaseFinal(Releases release, boolean moveToCandidateState) {
        List<Release> currentDraftReleases = releaseRepository.findByState(release.getState());

        for (Release r : currentDraftReleases) {
            if (r.getReleaseId() == release.getReleaseId()) { //  final release
                r.setState(ReleaseState.Final);
                releaseRepository.save(r);
                releaseRepository.flush();
                continue;
            }

            if (r.getLastUpdateTimestamp().before(release.getLastUpdateTimestamp())) { // draft is before final release
                moveCCsBetweenReleases(r, release, CoreComponentState.Published);
            }

            if (r.getLastUpdateTimestamp().after(release.getLastUpdateTimestamp())) { // draft is after final release
                if (moveToCandidateState) {
                    moveCCsBetweenReleases(r, null, CoreComponentState.Candidate);
                } else {
                    moveCCsBetweenReleases(r, null, CoreComponentState.Published);
                }
            }

            releaseRepository.deleteById(r.getReleaseId());
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

            AggregateCoreComponent currentACC = accRepository.findById(acc.getCurrentAccId()).orElse(null);
            if (isLatestRevision(acc, currentACC)) { // do not update current record because of acc if there are later releases (Issue #463)
                currentACC.setState(CoreComponentState.Published);
                accRepository.saveAndFlush(currentACC);
            }

            // move all children assocs to publish state
            asccRepository.updateStateByFromAccId(acc.getCurrentAccId(), CoreComponentState.Published);
            bccRepository.updateStateByFromAccId(acc.getCurrentAccId(), CoreComponentState.Published);

            asccRepository.updateStateByFromAccId(currentACC.getCurrentAccId(), CoreComponentState.Published);
            bccRepository.updateStateByFromAccId(currentACC.getCurrentAccId(), CoreComponentState.Published);
        }

        for (AssociationCoreComponent ascc : asccs) {
            ascc.setState(CoreComponentState.Published);
            asccRepository.saveAndFlush(ascc);

            AssociationCoreComponent currentAscc = asccRepository.findById(ascc.getCurrentAsccId()).orElse(null);
            currentAscc.setState(CoreComponentState.Published);
            asccRepository.saveAndFlush(currentAscc);
        }

        for (AssociationCoreComponentProperty asccp : asccps) {
            asccp.setState(CoreComponentState.Published);
            asccpRepository.saveAndFlush(asccp);

            AssociationCoreComponentProperty currentAsccp = asccpRepository.findById(asccp.getCurrentAsccpId()).orElse(null);
            currentAsccp.setState(CoreComponentState.Published);
            asccpRepository.saveAndFlush(currentAsccp);
        }

        for (BasicCoreComponent bcc : bccs) {
            bcc.setState(CoreComponentState.Published);
            bccRepository.saveAndFlush(bcc);

            BasicCoreComponent currentBcc = bccRepository.findById(bcc.getCurrentBccId()).orElse(null);
            currentBcc.setState(CoreComponentState.Published);
            bccRepository.saveAndFlush(currentBcc);
        }

        for (BasicCoreComponentProperty bccp : bccps) {
            bccp.setState(CoreComponentState.Published);
            bccpRepository.saveAndFlush(bccp);

            BasicCoreComponentProperty currentBccp = bccpRepository.findById(bccp.getCurrentBccpId()).orElse(null);
            currentBccp.setState(CoreComponentState.Published);
            bccpRepository.saveAndFlush(currentBccp);
        }
    }

    private boolean isLatestRevision(AggregateCoreComponent acc, AggregateCoreComponent currentACC) {
        if (acc.getCurrentAccId() != currentACC.getAccId()) {
            return false;
        }

        int maxRevisionNum = accRepository.findMaxRevisionNumByCurrentAccId(currentACC.getAccId());
        int maxRevisionTrackingNum = accRepository.findMaxRevisionTrackingNumByCurrentAccIdAndRevisionNum(currentACC.getAccId(), maxRevisionNum);

        if (acc.getRevisionNum() != maxRevisionNum || acc.getRevisionTrackingNum() != maxRevisionTrackingNum) {
            return false;
        }

        return true;
    }

    private void changeCCsStateByRelease(Release release, CoreComponentState state) {
        List<AggregateCoreComponent> accs = accRepository.findByReleaseId(release.getReleaseId());
        List<AssociationCoreComponent> asccs = asccRepository.findByReleaseId(release.getReleaseId());
        List<AssociationCoreComponentProperty> asccps = asccpRepository.findByReleaseId(release.getReleaseId());
        List<BasicCoreComponent> bccs = bccRepository.findByReleaseId(release.getReleaseId());
        List<BasicCoreComponentProperty> bccps = bccpRepository.findByReleaseId(release.getReleaseId());

        accs.stream().forEach(acc -> acc.setState(state));
        accRepository.saveAll(accs);

        asccs.stream().forEach(ascc -> ascc.setState(state));
        asccRepository.saveAll(asccs);

        asccps.stream().forEach(asccp -> asccp.setState(state));
        asccpRepository.saveAll(asccps);

        bccs.stream().forEach(bcc -> bcc.setState(state));
        bccRepository.saveAll(bccs);

        bccps.stream().forEach(bccp -> bccp.setState(state));
        bccpRepository.saveAll(bccps);
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
                accRepository.deleteById(acc.getCurrentAccId());
                accRepository.flush();
            }
        }

        for (AssociationCoreComponent ascc : asccs) {
            asccRepository.delete(ascc);
            asccRepository.flush();

            if (asccRepository.findByCurrentAsccId(ascc.getCurrentAsccId()).isEmpty()) { // ascc was the only revision, remove current as well
                asccRepository.deleteById(ascc.getCurrentAsccId());
                asccRepository.flush();
            }
        }

        for (AssociationCoreComponentProperty asccp : asccps) {
            asccpRepository.delete(asccp);
            asccpRepository.flush();

            if (asccpRepository.findByCurrentAsccpId(asccp.getCurrentAsccpId()).isEmpty()) { // acsccp was the only revision, remove current as well
                asccpRepository.deleteById(asccp.getCurrentAsccpId());
                asccpRepository.flush();
            }
        }

        for (BasicCoreComponent bcc : bccs) {
            bccRepository.delete(bcc);
            bccRepository.flush();

            if (bccRepository.findByCurrentBccId(bcc.getCurrentBccId()).isEmpty()) { // bcc was the only revision, remove current as well
                bccRepository.deleteById(bcc.getCurrentBccId());
                bccRepository.flush();
            }
        }

        for (BasicCoreComponentProperty bccp : bccps) {
            bccpRepository.delete(bccp);
            bccpRepository.flush();

            if (bccpRepository.findByCurrentBccpId(bccp.getCurrentBccpId()).isEmpty()) { // bccp was the only revision, remove current as well
                bccpRepository.deleteById(bccp.getCurrentBccpId());
                bccpRepository.flush();
            }
        }
    }

    private void moveCCsBetweenReleases(Release fromRel, Releases toRel, CoreComponentState ccState) {
        List<AggregateCoreComponent> accs = accRepository.findByReleaseId(fromRel.getReleaseId());
        List<AssociationCoreComponent> asccs = asccRepository.findByReleaseId(fromRel.getReleaseId());
        List<AssociationCoreComponentProperty> asccps = asccpRepository.findByReleaseId(fromRel.getReleaseId());
        List<BasicCoreComponent> bccs = bccRepository.findByReleaseId(fromRel.getReleaseId());
        List<BasicCoreComponentProperty> bccps = bccpRepository.findByReleaseId(fromRel.getReleaseId());

        Long releaseId = (toRel == null) ? null : toRel.getReleaseId();

        for (AggregateCoreComponent acc : accs) {
            acc.setReleaseId(releaseId);
            acc.setState(ccState);
            accRepository.saveAndFlush(acc);

            AggregateCoreComponent currentACC = accRepository.findById(acc.getCurrentAccId()).orElse(null);
            if (currentACC != null) {
                currentACC.setState(ccState);
                accRepository.saveAndFlush(currentACC);
            }
        }

        for (AssociationCoreComponent ascc : asccs) {
            ascc.setReleaseId(releaseId);
            ascc.setState(CoreComponentState.Published);
            asccRepository.saveAndFlush(ascc);

            AssociationCoreComponent currentAscc = asccRepository.findById(ascc.getCurrentAsccId()).orElse(null);
            if (currentAscc != null) {
                currentAscc.setState(ccState);
                asccRepository.saveAndFlush(currentAscc);
            }
        }

        for (AssociationCoreComponentProperty asccp : asccps) {
            asccp.setReleaseId(releaseId);
            asccp.setState(ccState);
            asccpRepository.saveAndFlush(asccp);

            AssociationCoreComponentProperty currentAsccp = asccpRepository.findById(asccp.getCurrentAsccpId()).orElse(null);
            if (currentAsccp != null) {
                currentAsccp.setState(ccState);
                asccpRepository.saveAndFlush(currentAsccp);
            }
        }

        for (BasicCoreComponent bcc : bccs) {
            bcc.setReleaseId(releaseId);
            bcc.setState(ccState);
            bccRepository.saveAndFlush(bcc);

            BasicCoreComponent currentBcc = bccRepository.findById(bcc.getCurrentBccId()).orElse(null);
            if (currentBcc != null) {
                currentBcc.setState(ccState);
                bccRepository.saveAndFlush(currentBcc);
            }
        }

        for (BasicCoreComponentProperty bccp : bccps) {
            bccp.setReleaseId(releaseId);
            bccp.setState(ccState);
            bccpRepository.saveAndFlush(bccp);

            BasicCoreComponentProperty currentBccp = bccpRepository.findById(bccp.getCurrentBccpId()).orElse(null);
            if (currentBccp != null) {
                currentBccp.setState(ccState);
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

    @Transactional
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
            moveCCsBetweenReleases(releaseToDelete, followingRelease, CoreComponentState.Published);
        } else {
            moveCCsBetweenReleases(releaseToDelete, null, CoreComponentState.Published);
        }

        releaseRepository.delete(releaseToDelete);
        releaseRepository.flush();
    }

    public int compareRelease(Long a, Long b) {
        return new ReleaseComparator().compare(a, b);
    }

    public class ReleaseComparator implements Comparator<Long> {
        @Override
        public int compare(Long a, Long b) {
            Release aRelease = releaseRepository.findById(a).orElse(null);
            Release bRelease = releaseRepository.findById(b).orElse(null);

            return new VersionStringComparator().compare(aRelease.getReleaseNum(), bRelease.getReleaseNum());
        }
    }
}

