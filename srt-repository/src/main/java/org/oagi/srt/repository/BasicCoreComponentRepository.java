package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.AggregateCoreComponent;
import org.oagi.srt.repository.entity.BasicCoreComponent;
import org.oagi.srt.repository.entity.CoreComponentState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface BasicCoreComponentRepository extends JpaRepository<BasicCoreComponent, Long> {

    @Query("select b from BasicCoreComponent b where b.revisionNum = ?1")
    public List<BasicCoreComponent> findAllWithRevisionNum(int revisionNum);

    @Query("select b from BasicCoreComponent b where b.fromAccId = ?1")
    public List<BasicCoreComponent> findByFromAccId(long fromAccId);

    @Query("select b from BasicCoreComponent b where b.fromAccId = ?1 and b.releaseId is null")
    public List<BasicCoreComponent> findByFromAccIdAndReleaseIdIsNull(long fromAccId);

    @Query("select b from BasicCoreComponent b where b.fromAccId = ?1 and b.revisionNum = ?2")
    public List<BasicCoreComponent> findByFromAccIdAndRevisionNum(long fromAccId, int revisionNum);

    @Query("select b from BasicCoreComponent b where b.fromAccId = ?1 and b.revisionNum > 0 and b.releaseId = ?2")
    public List<BasicCoreComponent> findByFromAccIdAndReleaseId(long fromAccId, long releaseId);

    @Query("select b from BasicCoreComponent b where b.fromAccId = ?1 and b.revisionNum > 0 and b.releaseId <= ?2")
    public List<BasicCoreComponent> findByFromAccIdAndReleaseIdLessThanEqual(long fromAccId, long releaseId);

    @Query("select count(b) from BasicCoreComponent b where b.fromAccId = ?1 and b.revisionNum = ?2")
    public int countByFromAccIdAndRevisionNum(long fromAccId, int revisionNum);

    @Query("select b from BasicCoreComponent b where b.fromAccId = ?1 and b.revisionNum = ?2 and b.state = ?3")
    public List<BasicCoreComponent> findByFromAccIdAndRevisionNumAndState(long fromAccId, int revisionNum, CoreComponentState state);

    @Query("select b from BasicCoreComponent b where b.fromAccId = ?1 and b.revisionNum > 0 and b.releaseId = ?2 and b.state = ?3")
    public List<BasicCoreComponent> findByFromAccIdAndReleaseIdAndState(long fromAccId, long releaseId, CoreComponentState state);

    @Query("select b from BasicCoreComponent b where b.fromAccId = ?1 and b.revisionNum > 0 and b.releaseId <= ?2 and b.state = ?3")
    public List<BasicCoreComponent> findByFromAccIdAndReleaseIdLessThanEqualAndState(long fromAccId, long releaseId, CoreComponentState state);

    @Query("select count(b) from BasicCoreComponent b where b.fromAccId = ?1 and b.revisionNum = ?2 and b.state = ?3")
    public int countByFromAccIdAndRevisionNumAndState(long fromAccId, int revisionNum, CoreComponentState state);

    @Query("select b from BasicCoreComponent b where b.fromAccId = ?1 and b.revisionNum = ?2 and b.seqKey != 0")
    public List<BasicCoreComponent> findByFromAccIdAndRevisionNumAndSeqKeyIsNotZero(long fromAccId, int revisionNum);

    @Query("select b from BasicCoreComponent b where b.toBccpId = ?1 and b.entityType = ?2")
    public List<BasicCoreComponent> findByToBccpIdAndEntityType(long toBccpId, int entityType);

    @Query("select b from BasicCoreComponent b where b.den like ?1%")
    public List<BasicCoreComponent> findByDenStartsWith(String den);

    @Query("select b from BasicCoreComponent b where b.fromAccId = ?1 and b.toBccpId = ?2")
    public List<BasicCoreComponent> findByFromAccIdAndToBccpId(long fromAccId, long toBccpId);

    @Query("select b from BasicCoreComponent b where b.guid = ?1 and b.fromAccId = ?2 and b.toBccpId = ?3")
    public BasicCoreComponent findOneByGuidAndFromAccIdAndToBccpId(String guid, long fromAccId, long toBccpId);

    @Query("select case when count(b) > 0 then true else false end from BasicCoreComponent b where b.guid = ?1 and b.toBccpId = ?2")
    public boolean existsByGuidAndToBccpId(String guid, long toBccpId);

    @Query("select case when count(b) > 0 then true else false end from BasicCoreComponent b where b.guid = ?1 and b.fromAccId = ?2 and b.toBccpId = ?3")
    public boolean existsByGuidAndFromAccIdAndToBccpId(String guid, long fromAccId, long toBccpId);

    @Query("select count(a) from BasicCoreComponent a where a.fromAccId = ?1")
    public int countByFromAccId(long fromAccId);

    @Query("select a from BasicCoreComponent a where a.toBccpId = ?1")
    public List<BasicCoreComponent> findAllByToBccpId(Long bccpId);

    @Query("select b from BasicCoreComponent b where b.currentBccId = ?1 and b.revisionNum = (" +
            "select MAX(b.revisionNum) from BasicCoreComponent b where b.currentBccId = ?1 group by b.currentBccId) order by b.creationTimestamp desc")
    public List<BasicCoreComponent> findAllWithLatestRevisionNumByCurrentBccId(long currentBccId);

    @Query("select b from BasicCoreComponent b where b.fromAccId = ?1 and b.revisionNum = (" +
            "select MAX(b.revisionNum) from BasicCoreComponent b where b.fromAccId = ?1 group by b.fromAccId) order by b.creationTimestamp desc")
    public List<BasicCoreComponent> findAllWithLatestRevisionNumByFromAccId(long currentBccId);

    @Query("select b from BasicCoreComponent b where b.currentBccId = ?1 and b.revisionNum = (" +
            "select MAX(b.revisionNum) from BasicCoreComponent b where b.currentBccId = ?1 and b.seqKey > 0 group by b.currentBccId)")
    public List<BasicCoreComponent> findAllWithLatestRevisionNumByCurrentBccIdAndSeqKeyIsNotZero(long currentBccId);

    @Query("select COALESCE(MAX(b.revisionNum), 0) from BasicCoreComponent b where b.fromAccId = ?1 and b.toBccpId = ?2")
    public Integer findMaxRevisionNumByFromAccIdAndToBccpId(long fromAccId, long toBccpId);

    @Query("select COALESCE(MAX(b.revisionTrackingNum), 0) from BasicCoreComponent b where b.fromAccId = ?1 and b.toBccpId = ?2 and b.revisionNum = ?3")
    public Integer findMaxRevisionTrackingNumByFromAccIdAndToBccpIdAndRevisionNum(long fromAccId, long toBccpId, int revisionNum);

    @Query("select COALESCE(MAX(b.revisionNum), 0) from BasicCoreComponent b where b.fromAccId = ?1 and b.toBccpId = ?2 and b.releaseId = ?3")
    public Integer findMaxRevisionNumByFromAccIdAndToBccpIdAndReleaseId(long fromAccId, long toBccpId, long releaseId);

    @Query("select COALESCE(MAX(b.revisionNum), 0) from BasicCoreComponent b where b.fromAccId = ?1 and b.toBccpId = ?2 and b.releaseId <= ?3")
    public Integer findMaxRevisionNumByFromAccIdAndToBccpIdAndReleaseIdLessThanEqual(long fromAccId, long toBccpId, long releaseId);

    @Query("select COALESCE(MAX(b.revisionTrackingNum), 0) from BasicCoreComponent b where b.fromAccId = ?1 and b.toBccpId = ?2 and b.revisionNum = ?3 and b.releaseId = ?4")
    public Integer findMaxRevisionTrackingNumByFromAccIdAndToBccpIdAndRevisionNumAndReleaseId(long fromAccId, long toBccpId, int revisionNum, long releaseId);

    @Query("select COALESCE(MAX(b.revisionTrackingNum), 0) from BasicCoreComponent b where b.fromAccId = ?1 and b.toBccpId = ?2 and b.revisionNum = ?3 and b.releaseId <= ?4")
    public Integer findMaxRevisionTrackingNumByFromAccIdAndToBccpIdAndRevisionNumAndReleaseIdLessThanEqual(long fromAccId, long toBccpId, int revisionNum, long releaseId);

    @Modifying
    @Query("update BasicCoreComponent b set b.state = ?2 where b.fromAccId = ?1")
    public void updateStateByFromAccId(long fromAccId, CoreComponentState state);

    @Modifying
    @Query("delete from BasicCoreComponent b where b.fromAccId = ?1 and b.toBccpId = ?2 and b.revisionNum = ?3")
    public void deleteByFromAccIdAndToBccpIdAndRevisionNum(long fromAccId, long toBccpId, int revisionNum);

    @Modifying
    @Query("delete from BasicCoreComponent b where b.fromAccId = ?1 and b.toBccpId = ?2 and b.revisionNum = ?3 and b.revisionTrackingNum <> ?4")
    public void deleteByFromAccIdAndToBccpIdAndRevisionNumAndNotRevisionTrackingNum(long fromAccId, long toBccpId, int revisionNum, int revisionTrackingNum);

    @Modifying
    @Query("update BasicCoreComponent b set b.state = ?5 where b.fromAccId = ?1 and b.toBccpId = ?2 and b.revisionNum = ?3 and b.revisionTrackingNum = ?4")
    public void deleteByFromAccIdAndToBccpIdAndRevisionNumAndNotRevisionTrackingNum(long fromAccId, long toBccpId, int revisionNum, int revisionTrackingNum, CoreComponentState state);

    @Modifying
    @Query("update BasicCoreComponent b set b.seqKey = b.seqKey + 1 " +
            "where b.fromAccId = ?1 and b.seqKey > ?2 and b.revisionNum = 0")
    public void increaseSeqKeyByFromAccIdAndSeqKeyGreaterThan(long fromAccId, int seqKey);

    @Modifying
    @Query("update BasicCoreComponent b set b.seqKey = b.seqKey - 1 " +
            "where b.fromAccId = ?1 and b.seqKey > ?2 and b.revisionNum = 0")
    public void decreaseSeqKeyByFromAccIdAndSeqKeyGreaterThan(long fromAccId, int seqKey);

    @Modifying
    @Query("delete from BasicCoreComponent b where b.currentBccId = ?1")
    public void deleteByCurrentBccId(long currentBccId);

    @Modifying
    @Query("delete from BasicCoreComponent b where b.fromAccId = ?1")
    public void deleteByFromAccId(long fromAccId);

    @Query("select b from BasicCoreComponent b where b.revisionNum = ?1 order by b.creationTimestamp desc")
    public List<BasicCoreComponent> findAllByRevisionNum(int revisionNum);

    @Query("select b from BasicCoreComponent b where b.revisionNum = ?1 and b.state in ?2 order by b.creationTimestamp desc")
    public List<BasicCoreComponent> findAllByRevisionNumAndStates(int revisionNum, Collection<CoreComponentState> states);

    @Query("select b from BasicCoreComponent b where b.toBccpId = ?1 and b.revisionNum = ?2")
    public List<BasicCoreComponent> findByToBccpIdAndRevisionNum(long toBccpId, int revisionNum);

    @Query("select b from BasicCoreComponent b where b.currentBccId = ?1 and b.revisionNum = ?2 and b.revisionTrackingNum = ?3")
    BasicCoreComponent findOneByCurrentBccIdAndRevisions(long currentBccId, int revisionNum, int i);

    @Query("select count(b) from BasicCoreComponent b where b.currentBccId = ?1")
    public int countByCurrentBccId(long currentBccId);

    @Query("select b.revisionNum from BasicCoreComponent b where b.bccId = ?1")
    int findRevisionNumByBccId(long bccId);

    @Modifying
    @Query("update BasicCoreComponent b set b.releaseId = ?2 where b.bccId = ?1")
    void updateReleaseByBccId(long bccId, Long releaseId);

    @Query("select b from BasicCoreComponent b where b.currentBccId = (select x.currentBccId from BasicCoreComponent x where x.bccId = ?1) and b.revisionNum < ?2 and b.releaseId is null")
    List<BasicCoreComponent> findPreviousNonReleasedRevisions(long id, int revisionNum);

    @Query("select b from BasicCoreComponent b where b.currentBccId = (select x.currentBccId from BasicCoreComponent x where x.bccId = ?1) and b.revisionNum > ?2 and b.releaseId is null")
    List<BasicCoreComponent> findFollowingNonReleasedRevisions(long id, int revisionNum);

    @Query("select b from BasicCoreComponent b where b.releaseId = ?1")
    List<BasicCoreComponent> findByReleaseId(long releaseId);

    @Query("select b from BasicCoreComponent b where b.currentBccId = ?1")
    List<BasicCoreComponent> findByCurrentBccId(long currentBccId);

    @Modifying
    @Query("update BasicCoreComponent b set b.seqKey = ?4 where b.currentBccId = ?1 and b.revisionNum = ?2 and b.revisionTrackingNum = ?3")
    void updateSeqKeyByCurrentBccIdAndRevisionNumAndRevisionTrackingNum(long currentBccId, int revisionNum, int revisionTrackingNum, int seqKey);
}
