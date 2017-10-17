package org.oagi.srt.service;

import org.oagi.srt.repository.ReleaseRepository;
import org.oagi.srt.repository.ReleasesRepository;
import org.oagi.srt.repository.entity.Release;
import org.oagi.srt.repository.entity.Releases;
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
}

