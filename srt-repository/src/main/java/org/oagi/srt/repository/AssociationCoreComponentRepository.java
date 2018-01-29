package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.AggregateCoreComponent;
import org.oagi.srt.repository.entity.AssociationCoreComponent;
import org.oagi.srt.repository.entity.CoreComponentState;
import org.oagi.srt.repository.entity.CoreComponents;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface AssociationCoreComponentRepository extends JpaRepository<AssociationCoreComponent, Long> {

    @Query("select a from AssociationCoreComponent a where a.revisionNum = ?1")
    public List<AssociationCoreComponent> findAllWithRevisionNum(int revisionNum);

    @Query("select a from AssociationCoreComponent a where a.fromAccId = ?1")
    public List<AssociationCoreComponent> findByFromAccId(long fromAccId);

    @Query("select a from AssociationCoreComponent a where a.fromAccId = ?1 and a.releaseId is null")
    public List<AssociationCoreComponent> findByFromAccIdWithNullRelease(long fromAccId);

    @Query("select a from AssociationCoreComponent a where a.fromAccId = ?1 and a.revisionNum = ?2")
    public List<AssociationCoreComponent> findByFromAccIdAndRevisionNum(long fromAccId, int revisionNum);

    @Query("select a from AssociationCoreComponent a where a.fromAccId in ?1 and a.revisionNum = ?2")
    public List<AssociationCoreComponent> findByFromAccIdInAndRevisionNum(List<Long> fromAccIds, int revisionNum);

    @Query("select a from AssociationCoreComponent a where a.fromAccId = ?1 and a.revisionNum > 0 and a.releaseId = ?2")
    public List<AssociationCoreComponent> findByFromAccIdAndReleaseId(long fromAccId, long releaseId);

    @Query("select a from AssociationCoreComponent a where a.fromAccId = ?1 and a.revisionNum > 0 and a.releaseId <= ?2")
    public List<AssociationCoreComponent> findByFromAccIdAndReleaseIdLessThanEqual(long fromAccId, long releaseId);

    @Query("select count(a) from AssociationCoreComponent a where a.fromAccId = ?1 and a.revisionNum = ?2")
    public int countByFromAccIdAndRevisionNum(long fromAccId, int revisionNum);

    @Query("select a from AssociationCoreComponent a where a.fromAccId = ?1 and a.revisionNum = ?2 and a.state = ?3")
    public List<AssociationCoreComponent> findByFromAccIdAndRevisionNumAndState(long fromAccId, int revisionNum, CoreComponentState state);

    @Query("select a from AssociationCoreComponent a where a.fromAccId = ?1 and a.revisionNum > 0 and a.releaseId = ?2 and a.state = ?3")
    public List<AssociationCoreComponent> findByFromAccIdAndReleaseIdAndState(long fromAccId, long releaseId, CoreComponentState state);

    @Query("select a from AssociationCoreComponent a where a.fromAccId = ?1 and a.revisionNum > 0 and a.releaseId <= ?2 and a.state = ?3")
    public List<AssociationCoreComponent> findByFromAccIdAndReleaseIdLessThanEqualAndState(long fromAccId, long releaseId, CoreComponentState state);

    @Query("select count(a) from AssociationCoreComponent a where a.fromAccId = ?1 and a.revisionNum = ?2 and a.state = ?3")
    public int countByFromAccIdAndRevisionNumAndState(long fromAccId, int revisionNum, CoreComponentState state);

    @Query("select a from AssociationCoreComponent a where a.toAsccpId = ?1 and a.revisionNum = ?2 and a.state = ?3")
    public List<AssociationCoreComponent> findByToAsccpIdAndRevisionNumAndState(long toAsccpId, int revisionNum, CoreComponentState state);

    @Query("select a from AssociationCoreComponent a where a.fromAccId = ?1 and a.toAsccpId = ?2 and a.revisionNum = ?3 and a.state = ?4")
    public AssociationCoreComponent findByFromAccIdAndToAsccpIdAndRevisionNumAndState(long fromAccId, long toAsccpId, int revisionNum, CoreComponentState state);

    @Query("select a from AssociationCoreComponent a where a.den like ?1%")
    public List<AssociationCoreComponent> findByDenStartsWith(String den);

    @Query("select a from AssociationCoreComponent a where a.den like %?1%")
    public List<AssociationCoreComponent> findByDenContaining(String den);

    @Query("select a from AssociationCoreComponent a where a.fromAccId = ?1 and a.toAsccpId = ?2")
    public List<AssociationCoreComponent> findByFromAccIdAndToAsccpId(long fromAccId, long toAsccpId);

    @Query("select a from AssociationCoreComponent a where a.guid = ?1 and a.fromAccId = ?2 and a.toAsccpId = ?3")
    public AssociationCoreComponent findOneByGuidAndFromAccIdAndToAsccpId(String guid, long fromAccId, long toAsccpId);

    @Query("select case when count(a) > 0 then true else false end from AssociationCoreComponent a where a.guid = ?1 and a.fromAccId = ?2 and a.toAsccpId = ?3")
    public boolean existsByGuidAndFromAccIdAndToAsccpId(String guid, long fromAccId, long toAsccpId);

    @Query("select count(a) from AssociationCoreComponent a where a.fromAccId = ?1")
    public int countByFromAccId(long fromAccId);

    @Query("select a from AssociationCoreComponent a where a.currentAsccId = ?1 and a.revisionNum = (" +
            "select MAX(a.revisionNum) from AssociationCoreComponent a where a.currentAsccId = ?1 group by a.currentAsccId) order by a.creationTimestamp desc")
    public List<AssociationCoreComponent> findAllWithLatestRevisionNumByCurrentAsccId(long currentAsccId);

    @Query("select a from AssociationCoreComponent a where a.fromAccId = ?1 and a.revisionNum = (" +
            "select MAX(a.revisionNum) from AssociationCoreComponent a where a.fromAccId = ?1 group by a.fromAccId) order by a.creationTimestamp desc")
    public List<AssociationCoreComponent> findAllWithLatestRevisionNumByFromAccId(long currentAsccId);

    @Query("select a from AssociationCoreComponent a where a.fromAccId in ?1")
    public List<AssociationCoreComponent> findByFromAccId(Collection<Long> fromAccId);

    @Query("select a.toAsccpId from AssociationCoreComponent a where a.fromAccId in ?1")
    public List<Long> findToAsccpIdByFromAccId(Collection<Long> fromAccId);

    @Query("select a from AssociationCoreComponent a where a.toAsccpId = ?1")
    public List<AssociationCoreComponent> findAllByToAsccpId(Long fromAccId);

    @Query("select COALESCE(MAX(a.revisionNum), 0) from AssociationCoreComponent a where a.fromAccId = ?1 and a.toAsccpId = ?2")
    public Integer findMaxRevisionNumByFromAccIdAndToAsccpId(long fromAccId, long toAsccpId);

    @Query("select COALESCE(MAX(a.revisionTrackingNum), 0) from AssociationCoreComponent a where a.fromAccId = ?1 and a.toAsccpId = ?2 and a.revisionNum = ?3")
    public Integer findMaxRevisionTrackingNumByFromAccIdAndToAsccpIdAndRevisionNum(long fromAccId, long toAsccpId, int revisionNum);

    @Query("select COALESCE(MAX(a.revisionNum), 0) from AssociationCoreComponent a where a.fromAccId = ?1 and a.toAsccpId = ?2 and a.releaseId = ?3")
    public Integer findMaxRevisionNumByFromAccIdAndToAsccpIdAndReleaseId(long fromAccId, long toAsccpId, long releaseId);

    @Query("select COALESCE(MAX(a.revisionNum), 0) from AssociationCoreComponent a where a.fromAccId = ?1 and a.toAsccpId = ?2 and a.releaseId <= ?3")
    public Integer findMaxRevisionNumByFromAccIdAndToAsccpIdAndReleaseIdLessThanEqual(long fromAccId, long toAsccpId, long releaseId);

    @Query("select COALESCE(MAX(a.revisionTrackingNum), 0) from AssociationCoreComponent a where a.fromAccId = ?1 and a.toAsccpId = ?2 and a.revisionNum = ?3 and a.releaseId = ?4")
    public Integer findMaxRevisionTrackingNumByFromAccIdAndToAsccpIdAndRevisionNumAndReleaseId(long fromAccId, long toAsccpId, int revisionNum,  long releaseId);

    @Query("select COALESCE(MAX(a.revisionTrackingNum), 0) from AssociationCoreComponent a where a.fromAccId = ?1 and a.toAsccpId = ?2 and a.revisionNum = ?3 and a.releaseId <= ?4")
    public Integer findMaxRevisionTrackingNumByFromAccIdAndToAsccpIdAndRevisionNumAndReleaseIdLessThanEqual(long fromAccId, long toAsccpId, int revisionNum, long releaseId);

    @Modifying
    @Query("delete from AssociationCoreComponent a where a.fromAccId = ?1 and a.toAsccpId = ?2 and a.revisionNum = ?3")
    public void deleteByFromAccIdAndToAsccpIdAndRevisionNum(long fromAccId, long toAsccpId, int revisionNum);

    @Modifying
    @Query("delete from AssociationCoreComponent a where a.fromAccId = ?1 and a.toAsccpId = ?2 and a.revisionNum = ?3 and a.revisionTrackingNum <> ?4")
    public void deleteByFromAccIdAndToAsccpIdAndRevisionNumAndNotRevisionTrackingNum(long fromAccId, long toAsccpId, int revisionNum, int revisionTrackingNum);

    @Modifying
    @Query("update AssociationCoreComponent a set a.state = ?2 where a.fromAccId = ?1")
    public void updateStateByFromAccId(long fromAccId, CoreComponentState state);

    @Modifying
    @Query("update AssociationCoreComponent a set a.state = ?5 where a.fromAccId = ?1 and a.toAsccpId = ?2 and a.revisionNum = ?3 and a.revisionTrackingNum = ?4")
    public void updateStateByFromAccIdAndToAsccpIdAndRevisionNumAndNotRevisionTrackingNum(long fromAccId, long toAsccpId, int revisionNum, int revisionTrackingNum, CoreComponentState state);

    @Modifying
    @Query("update AssociationCoreComponent a set a.seqKey = a.seqKey + 1 " +
            "where a.fromAccId = ?1 and a.seqKey > ?2 and a.revisionNum = 0")
    public void increaseSeqKeyByFromAccIdAndSeqKeyGreaterThan(long fromAccId, int seqKey);

    @Modifying
    @Query("update AssociationCoreComponent a set a.seqKey = a.seqKey - 1 " +
            "where a.fromAccId = ?1 and a.seqKey > ?2 and a.revisionNum = 0")
    public void decreaseSeqKeyByFromAccIdAndSeqKeyGreaterThan(long fromAccId, int seqKey);

    @Query("select a from AssociationCoreComponent a where a.toAsccpId = ?1 and a.revisionNum = ?2")
    public List<AssociationCoreComponent> findByToAsccpIdAndRevisionNum(long toAsccpId, int revisionNum);

    @Modifying
    @Query("delete from AssociationCoreComponent a where a.currentAsccId = ?1")
    public void deleteByCurrentAsccId(long currentAsccId);

    @Modifying
    @Query("delete from AssociationCoreComponent a where a.fromAccId = ?1")
    public void deleteByFromAccId(long fromAccId);

    @Query("select a from AssociationCoreComponent a where a.revisionNum = ?1 order by a.creationTimestamp desc")
    public List<AssociationCoreComponent> findAllByRevisionNum(int revisionNum);

    @Query("select a from AssociationCoreComponent a where a.revisionNum = ?1 and a.state in ?2 order by a.creationTimestamp desc")
    public List<AssociationCoreComponent> findAllByRevisionNumAndStates(int revisionNum, Collection<CoreComponentState> states);

    @Query("select a from AssociationCoreComponent a where a.currentAsccId = ?1 and a.revisionNum = ?2 and a.revisionTrackingNum = ?3")
    AssociationCoreComponent findOneByCurrentAsccIdAndRevisions(long currentAsccId, int revisionNum, int revisionTrackingNum);

    @Query("select count(a) from AssociationCoreComponent a where a.currentAsccId = ?1")
    public int countByCurrentAsccId(long currentAsccId);

    @Query("select a.revisionNum from AssociationCoreComponent a where a.asccId = ?1")
    int findRevisionNumByAsccId(long asccId);

    @Modifying
    @Query("update AssociationCoreComponent a set a.releaseId = ?2 where a.asccId = ?1")
    void updateReleaseByAsccId(long asccId, Long releaseId);

    @Query("select a from AssociationCoreComponent a where a.currentAsccId = (select x.currentAsccId from AssociationCoreComponent x where x.asccId = ?1) and a.revisionNum < ?2 and a.releaseId is null")
    List<AssociationCoreComponent> findPreviousNonReleasedRevisions(long id, int maxRevisionNum);


    @Query("select a from AssociationCoreComponent a where a.currentAsccId = (select x.currentAsccId from AssociationCoreComponent x where x.asccId = ?1) and a.revisionNum > ?2 and a.releaseId is null")
    List<AssociationCoreComponent> findFollowingNonReleasedRevisions(long id, int maxRevisionNum);

    @Query("select a from AssociationCoreComponent a where a.releaseId = ?1")
    List<AssociationCoreComponent> findByReleaseId(long releaseId);

    @Query("select a from AssociationCoreComponent a where a.currentAsccId = ?1")
    List<AssociationCoreComponent> findByCurrentAsccId(long currentAsccId);

    @Modifying
    @Query("update AssociationCoreComponent a set a.seqKey = ?4 where a.currentAsccId = ?1 and a.revisionNum = ?2 and a.revisionTrackingNum = ?3")
    void updateSeqKeyByCurrentAsccIdAndRevisionNumAndRevisionTrackingNum(long curentAsccId, int revisionNum, int revisionTrackingNum, int seqKey);
}
