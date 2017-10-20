package org.oagi.srt.service;

import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public List<CoreComponents> getDeltaForRelease(Release release, User currentUser) {
        return coreComponentsRepository.findDeltaForRelease(release, currentUser);
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
//            bccpRepository.updateReleaseByBccpId(bccp.getId(), release.getReleaseId());
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
//            bccRepository.updateReleaseByBccId(bcc.getId(), release.getReleaseId());
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
//            asccpRepository.updateReleaseByAsccpId(asccp.getId(), release.getReleaseId());
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
//            asccRepository.updateReleaseByAsccId(ccs.getId(), release.getReleaseId());
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
//            accRepository.updateReleaseByAccId(acc.getId(), release.getReleaseId());
            accRepository.delete(acc.getId());
            accRepository.flush();
        }

        for (AggregateCoreComponent acc : followingNonreleased) { // nullify following non-released revisions
            accRepository.updateReleaseByAccId(acc.getId(), null);
        }
    }

    public void makeReleaseFinal(Releases release) {
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
                moveCCsBetweenReleases(r, null);
            }

            releaseRepository.delete(r.getReleaseId());
            releaseRepository.flush();
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
            accRepository.saveAndFlush(acc);
        }

        for (AssociationCoreComponent ascc : asccs) {
            ascc.setReleaseId(releaseId);
            asccRepository.saveAndFlush(ascc);
        }

        for (AssociationCoreComponentProperty asccp : asccps) {
            asccp.setReleaseId(releaseId);
            asccpRepository.saveAndFlush(asccp);
        }

        for (BasicCoreComponent bcc : bccs) {
            bcc.setReleaseId(releaseId);
            bccRepository.saveAndFlush(bcc);
        }

        for (BasicCoreComponentProperty bccp : bccps) {
            bccp.setReleaseId(releaseId);
            bccpRepository.saveAndFlush(bccp);
        }
    }
}

