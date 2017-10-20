package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.Release;
import org.oagi.srt.repository.entity.ReleaseState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReleaseRepository extends JpaRepository<Release, Long> {

    @Query("select r from Release r where r.releaseNum = ?1")
    public Release findOneByReleaseNum(String releaseNum);

    @Query("select r.releaseId from Release r where r.releaseNum = ?1")
    public int findReleaseIdByReleaseNum(String releaseNum);

    @Query("select case when count(r) > 0 then true else false end from Release r where r.releaseNum = ?1 and r.releaseId <> ?2")
    boolean existsByReleaseNumExceptReleaseId(String releaseNum, long releaseId);

    @Query("select r from Release r where r.state = ?1 order by r.lastUpdateTimestamp desc")
    List<Release> findByState(ReleaseState state);
}
