package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.BasicCoreComponentProperty;
import org.oagi.srt.repository.entity.CoreComponentState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface BasicCoreComponentPropertyRepository extends JpaRepository<BasicCoreComponentProperty, Long> {

    @Query("select b from BasicCoreComponentProperty b where b.revisionNum = ?1")
    public List<BasicCoreComponentProperty> findAllWithRevisionNum(int revisionNum);

    @Query("select b from BasicCoreComponentProperty b where b.bccpId = ?1 and b.revisionNum = ?2 and b.state = ?3")
    public BasicCoreComponentProperty findOneByBccpIdAndRevisionNumAndState(long bccpId, int revisionNum, CoreComponentState state);

    @Query("select new BasicCoreComponentProperty(b.bccpId, b.den) from BasicCoreComponentProperty b where b.propertyTerm = ?1 and b.bdtId = ?2")
    public BasicCoreComponentProperty findBccpIdAndDenByPropertyTermAndBdtId(String propertyTerm, long bdtId);

    @Query("select b from BasicCoreComponentProperty b where b.guid = ?1")
    public BasicCoreComponentProperty findOneByGuid(String guid);

    @Query("select case when count(b) > 0 then true else false end from BasicCoreComponentProperty b where b.guid = ?1")
    public boolean existsByGuid(String guid);

    @Query("select new BasicCoreComponentProperty(b.bccpId, b.den) from BasicCoreComponentProperty b where b.guid = ?1")
    public BasicCoreComponentProperty findBccpIdAndDenByGuid(String guid);

    @Query("select b from BasicCoreComponentProperty b where b.revisionNum = ?1 order by b.creationTimestamp desc")
    public List<BasicCoreComponentProperty> findAllByRevisionNum(int revisionNum);

    @Query("select b from BasicCoreComponentProperty b where b.revisionNum = ?1 and b.state in ?2 order by b.creationTimestamp desc")
    public List<BasicCoreComponentProperty> findAllByRevisionNumAndStates(int revisionNum, Collection<CoreComponentState> states);

    @Query("select b from BasicCoreComponentProperty b where b.currentBccpId = ?1 and b.revisionNum = (" +
            "select MAX(b.revisionNum) from BasicCoreComponentProperty b where b.currentBccpId = ?1 group by b.currentBccpId) order by b.creationTimestamp desc")
    public List<BasicCoreComponentProperty> findAllWithLatestRevisionNumByCurrentBccpId(long currentBccpId);

    @Query("select b from BasicCoreComponentProperty b where b.currentBccpId = ?1 and b.revisionNum = ?2 and b.revisionTrackingNum = ?3")
    public BasicCoreComponentProperty findOneByCurrentBccpIdAndRevisions(long currentBccpId, int revisionNum, int revisionTrackingNum);

    @Query("select COALESCE(MAX(b.revisionNum), 0) from BasicCoreComponentProperty b where b.currentBccpId = ?1")
    public Integer findMaxRevisionNumByCurrentBccpId(long currentBccpId);

    @Query("select COALESCE(MAX(b.revisionTrackingNum), 0) from BasicCoreComponentProperty b where b.currentBccpId = ?1 and b.revisionNum = ?2")
    public Integer findMaxRevisionTrackingNumByCurrentBccpIdAndRevisionNum(long currentBccpId, int revisionNum);

    @Query("select COALESCE(MAX(b.revisionNum), 0) from BasicCoreComponentProperty b where b.currentBccpId = ?1 and b.releaseId = ?2")
    public Integer findMaxRevisionNumByCurrentBccpIdAndReleaseId(long currentBccpId, long releaseId);

    @Query("select COALESCE(MAX(b.revisionTrackingNum), 0) from BasicCoreComponentProperty b where b.currentBccpId = ?1 and b.revisionNum = ?2 and b.releaseId = ?3")
    public Integer findMaxRevisionTrackingNumByCurrentBccpIdAndRevisionNumAndReleaseId(long currentBccpId, int revisionNum, long releaseId);

    @Query("select COALESCE(MAX(b.revisionNum), 0) from BasicCoreComponentProperty b where b.bccpId = ?1 and b.releaseId = ?2")
    public Integer findMaxRevisionNumByBccpIdAndReleaseId(long currentBccpId, long releaseId);

    @Query("select COALESCE(MAX(b.revisionTrackingNum), 0) from BasicCoreComponentProperty b where b.bccpId = ?1 and b.revisionNum = ?2 and b.releaseId = ?3")
    public Integer findMaxRevisionTrackingNumByBccpIdAndRevisionNumAndReleaseId(long currentBccpId, int revisionNum, long releaseId);

    @Query("select COALESCE(MAX(b.revisionNum), 0) from BasicCoreComponentProperty b where b.bccpId = ?1")
    public Integer findMaxRevisionNumByBccpId(long currentBccpId);

    @Query("select COALESCE(MAX(b.revisionTrackingNum), 0) from BasicCoreComponentProperty b where b.bccpId = ?1 and b.revisionNum = ?2")
    public Integer findMaxRevisionTrackingNumByBccpIdAndRevisionNum(long currentBccpId, int revisionNum);

    @Modifying
    @Query("delete from BasicCoreComponentProperty b where b.currentBccpId = ?1 and b.revisionNum = ?2")
    public void deleteByCurrentBccpIdAndRevisionNum(long currentBccpId, int revisionNum);

    @Modifying
    @Query("delete from BasicCoreComponentProperty b where b.currentBccpId = ?1 and b.revisionNum = ?2 and b.revisionTrackingNum <> ?3")
    public void deleteByCurrentBccpIdAndRevisionNumAndNotRevisionTrackingNum(long currentBccpId, int revisionNum, int revisionTrackingNum);

    @Modifying
    @Query("delete from BasicCoreComponentProperty b where b.currentBccpId = ?1")
    public void deleteByCurrentBccpId(long currentBccpId);
}
