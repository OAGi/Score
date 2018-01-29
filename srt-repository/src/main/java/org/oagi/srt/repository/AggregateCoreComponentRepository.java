package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.AggregateCoreComponent;
import org.oagi.srt.repository.entity.CoreComponentState;
import org.oagi.srt.repository.entity.CoreComponents;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface AggregateCoreComponentRepository extends JpaRepository<AggregateCoreComponent, Long> {

    @Query("select a from AggregateCoreComponent a where a.accId in ?1")
    public List<AggregateCoreComponent> findByAccIdIn(List<Long> accIds);

    @Query("select a from AggregateCoreComponent a where a.revisionNum = ?1")
    public List<AggregateCoreComponent> findAllWithRevisionNum(int revisionNum);

    @Query("select a from AggregateCoreComponent a where a.guid = ?1")
    public AggregateCoreComponent findOneByGuid(String guid);

    @Query("select case when count(a) > 0 then true else false end from AggregateCoreComponent a where a.guid = ?1")
    public boolean existsByGuid(String guid);

    @Query("select new AggregateCoreComponent(a.accId, a.den) from AggregateCoreComponent a where a.revisionNum = 0 and a.guid = ?1")
    public AggregateCoreComponent findAccIdAndDenByGuid(String guid);

    @Query("select a from AggregateCoreComponent a where a.accId = ?1 and a.revisionNum = ?2 and a.state = ?3")
    public AggregateCoreComponent findOneByAccIdAndRevisionNumAndState(long accId, int revisionNum, CoreComponentState state);

    @Query("select a from AggregateCoreComponent a where a.revisionNum = ?1 order by a.creationTimestamp desc")
    public List<AggregateCoreComponent> findAllByRevisionNum(int revisionNum);

    @Query("select a from AggregateCoreComponent a where a.revisionNum = ?1 and a.state in ?2 order by a.creationTimestamp desc")
    public List<AggregateCoreComponent> findAllByRevisionNumAndStates(int revisionNum, Collection<CoreComponentState> states);

    @Query("select a from AggregateCoreComponent a where a.currentAccId = ?1 and a.revisionNum = (" +
            "select MAX(a.revisionNum) from AggregateCoreComponent a where a.currentAccId = ?1 group by a.currentAccId) order by a.creationTimestamp desc")
    public List<AggregateCoreComponent> findAllWithLatestRevisionNumByCurrentAccId(long currentAccId);

    @Query("select a.basedAccId from AggregateCoreComponent a where a.accId = ?1")
    public Long findBasedAccIdByAccId(long accId);

    @Query("select COALESCE(MAX(a.revisionNum), 0) from AggregateCoreComponent a where a.currentAccId = ?1")
    public Integer findMaxRevisionNumByCurrentAccId(long currentAccId);

    @Query("select COALESCE(MAX(a.revisionTrackingNum), 0) from AggregateCoreComponent a where a.currentAccId = ?1 and a.revisionNum = ?2")
    public Integer findMaxRevisionTrackingNumByCurrentAccIdAndRevisionNum(long currentAccId, int revisionNum);

    @Query("select COALESCE(MAX(a.revisionNum), 0) from AggregateCoreComponent a where a.currentAccId = ?1 and a.releaseId = ?2")
    public Integer findMaxRevisionNumByCurrentAccIdAndReleaseId(long currentAccId, long releaseId);

    @Query("select COALESCE(MAX(a.revisionTrackingNum), 0) from AggregateCoreComponent a where a.currentAccId = ?1 and a.revisionNum = ?2 and a.releaseId = ?3")
    public Integer findMaxRevisionTrackingNumByCurrentAccIdAndRevisionNumAndReleaseId(long currentAccId, int revisionNum, long releaseId);

    @Query("select COALESCE(MAX(a.revisionNum), 0) from AggregateCoreComponent a where a.accId = ?1 and a.releaseId = ?2")
    public Integer findMaxRevisionNumByAccIdAndReleaseId(long accId, long releaseId);

    @Query("select COALESCE(MAX(a.revisionNum), 0) from AggregateCoreComponent a where a.accId = ?1 and a.releaseId <= ?2")
    public Integer findMaxRevisionNumByAccIdAndLessThanReleaseId(long accId, long releaseId);

    @Query("select COALESCE(MAX(a.revisionTrackingNum), 0) from AggregateCoreComponent a where a.accId = ?1 and a.revisionNum = ?2 and a.releaseId = ?3")
    public Integer findMaxRevisionTrackingNumByAccIdAndRevisionNumAndReleaseId(long ccId, int revisionNum, long releaseId);

    @Query("select COALESCE(MAX(a.revisionTrackingNum), 0) from AggregateCoreComponent a where a.accId = ?1 and a.revisionNum = ?2 and a.releaseId <= ?3")
    public Integer findMaxRevisionTrackingNumByAccIdAndRevisionNumAndLessThanReleaseId(long ccId, int revisionNum, long releaseId);

    @Query("select COALESCE(MAX(a.revisionNum), 0) from AggregateCoreComponent a where a.accId = ?1")
    public Integer findMaxRevisionNumByAccId(long accId);

    @Query("select COALESCE(MAX(a.revisionTrackingNum), 0) from AggregateCoreComponent a where a.accId = ?1 and a.revisionNum = ?2")
    public Integer findMaxRevisionTrackingNumByAccIdAndRevisionNum(long ccId, int revisionNum);

    @Modifying
    @Query("delete from AggregateCoreComponent a where a.currentAccId = ?1 and a.revisionNum = ?2")
    public void deleteByCurrentAccIdAndRevisionNum(long currentAccId, int revisionNum);

    @Modifying
    @Query("delete from AggregateCoreComponent a where a.currentAccId = ?1 and a.revisionNum = ?2 and a.revisionTrackingNum <> ?3")
    public void deleteByCurrentAccIdAndRevisionNumAndNotRevisionTrackingNum(long currentAccId, int revisionNum, int revisionTrackingNum);

    @Modifying
    @Query("delete from AggregateCoreComponent a where a.currentAccId = ?1")
    public void deleteByCurrentAccId(long currentAccId);

    @Modifying
    @Query("delete from AggregateCoreComponent a where a.accId = ?1")
    public void deleteByAccId(long accId);

    @Modifying
    @Query("update AggregateCoreComponent a set a.state = ?4 where a.currentAccId = ?1 and a.revisionNum = ?2 and a.revisionTrackingNum = ?3")
    public void updateStateByCurrentAccIdAndRevisionNumAndRevisionTrackingNum(long currentAccId, int revisionNum, int revisionTrackingNum, CoreComponentState state);

    @Query("select a from AggregateCoreComponent a where a.currentAccId = ?1 and a.revisionNum = ?2 and a.revisionTrackingNum = ?3")
    AggregateCoreComponent findOneByCurrentAccIdAndRevisions(long currentAccId, int revisionNum, int i);

    @Query("select a.revisionNum from AggregateCoreComponent a where a.accId = ?1")
    int findRevisionNumByAccId(long accId);

    @Modifying
    @Query("update AggregateCoreComponent a set a.releaseId = ?2 where a.accId = ?1")
    void updateReleaseByAccId(long accId, Long releaseId);

    @Query("select a from AggregateCoreComponent a where a.currentAccId = (select x.currentAccId from AggregateCoreComponent x where x.accId = ?1) and a.revisionNum < ?2 and a.releaseId is null")
    List<AggregateCoreComponent> findPreviousNonReleasedRevisions(long id, int revisionNum);

    @Query("select a from AggregateCoreComponent a where a.currentAccId = (select x.currentAccId from AggregateCoreComponent x where x.accId = ?1) and a.revisionNum > ?2 and a.releaseId is null")
    List<AggregateCoreComponent> findFollowingNonReleasedRevisions(long id, int revisionNum);

    @Query("select a from AggregateCoreComponent a where a.releaseId = ?1")
    List<AggregateCoreComponent> findByReleaseId(long releaseId);

    @Query("select a from AggregateCoreComponent a where a.currentAccId = ?1 and a.releaseId = ?2")
    List<AggregateCoreComponent> findByCurrentAccIdAndReleaseId(long currentAccId, long releaseId);

    @Query("select a from AggregateCoreComponent a where a.currentAccId = ?1 and a.releaseId <= ?2")
    List<AggregateCoreComponent> findByCurrentAccIdAndReleaseIdLessThanEqual(long currentAccId, long releaseId);

    @Query("select a from AggregateCoreComponent a where a.currentAccId = ?1 and a.releaseId = ?2 and a.state = ?3")
    List<AggregateCoreComponent> findByCurrentAccIdAndReleaseIdAndState(long currentAccId, long releaseId, CoreComponentState state);

    @Query("select a from AggregateCoreComponent a where a.currentAccId = ?1 and a.releaseId <= ?2 and a.state = ?3")
    List<AggregateCoreComponent> findByCurrentAccIdAndReleaseIdLessThanEqualAndState(long currentAccId, long releaseId, CoreComponentState state);

    @Query("select a from AggregateCoreComponent a where a.currentAccId = ?1")
    List<AggregateCoreComponent> findByCurrentAccId(long currentAccId);
}
