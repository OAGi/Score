package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.AggregateCoreComponent;
import org.oagi.srt.repository.entity.CoreComponentState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface AggregateCoreComponentRepository extends JpaRepository<AggregateCoreComponent, Long> {

    @Query("select a from AggregateCoreComponent a where a.revisionNum = ?1")
    public List<AggregateCoreComponent> findAllWithRevisionNum(int revisionNum);

    @Query("select a from AggregateCoreComponent a where a.guid = ?1")
    public AggregateCoreComponent findOneByGuid(String guid);

    @Query("select case when count(a) > 0 then true else false end from AggregateCoreComponent a where a.guid = ?1")
    public boolean existsByGuid(String guid);

    @Query("select new AggregateCoreComponent(a.accId, a.den) from AggregateCoreComponent a where a.guid = ?1")
    public AggregateCoreComponent findAccIdAndDenByGuid(String guid);

    @Query("select a from AggregateCoreComponent a where a.accId = ?1 and a.revisionNum = ?2")
    public AggregateCoreComponent findOneByAccIdAndRevisionNum(long accId, int revisionNum);

    @Query("select a from AggregateCoreComponent a where a.accId = ?1 and a.revisionNum = ?2 and a.state = ?3")
    public AggregateCoreComponent findOneByAccIdAndRevisionNumAndState(long accId, int revisionNum, CoreComponentState state);

    @Query("select new AggregateCoreComponent(a.accId, a.basedAccId, a.definition) from AggregateCoreComponent a " +
            "where a.accId = ?1 and a.revisionNum = ?2")
    public AggregateCoreComponent findAccIdAndBasedAccIdAndDefinitionByAccIdAndRevisionNum(long accId, int revisionNum);

    @Query("select a from AggregateCoreComponent a where a.revisionNum = ?1 order by a.creationTimestamp desc")
    public List<AggregateCoreComponent> findAllByRevisionNum(int revisionNum);

    @Query("select a from AggregateCoreComponent a where a.revisionNum = ?1 and a.state in ?2 order by a.creationTimestamp desc")
    public List<AggregateCoreComponent> findAllByRevisionNumAndStates(int revisionNum, Collection<CoreComponentState> states);

    @Query("select a from AggregateCoreComponent a where a.currentAccId = ?1 and a.revisionTrackingNum = (" +
            "select MAX(a.revisionTrackingNum) from AggregateCoreComponent a where a.currentAccId = ?1 group by a.currentAccId)")
    public AggregateCoreComponent findLatestOneByCurrentAccId(long currentAccId);

    @Modifying
    @Query("delete from AggregateCoreComponent a where a.currentAccId = ?1")
    public void deleteByCurrentAccId(long currentAccId);
}
