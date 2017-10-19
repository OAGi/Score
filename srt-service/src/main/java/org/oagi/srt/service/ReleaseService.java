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
    private AggregateBusinessInformationEntityRepository abieRepository;

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
    public void update(Release release) {
        releaseRepository.saveAndFlush(release);
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
}

