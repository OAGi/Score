package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.Release;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReleaseRepository extends JpaRepository<Release, Integer> {

    @Query("select r from Release r where r.releaseNum = ?1")
    public Release findOneByReleaseNum(String releaseNum);

    @Query("select r.releaseId from Release r where r.releaseNum = ?1")
    public int findReleaseIdByReleaseNum(String releaseNum);
}
