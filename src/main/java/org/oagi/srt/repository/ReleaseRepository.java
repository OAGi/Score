package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.Release;

public interface ReleaseRepository {

    public Release findOneByReleaseNum(String releaseNum);
}
