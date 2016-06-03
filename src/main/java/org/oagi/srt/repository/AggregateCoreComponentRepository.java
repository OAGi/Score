package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.AggregateCoreComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AggregateCoreComponentRepository extends JpaRepository<AggregateCoreComponent, Integer> {

    @Query("select a from AggregateCoreComponent a where a.guid = ?1")
    public AggregateCoreComponent findOneByGuid(String guid);

    @Query("select case when count(a) > 0 then true else false end from AggregateCoreComponent a where a.guid = ?1")
    public boolean existsByGuid(String guid);

    @Query("select new AggregateCoreComponent(a.accId, a.den) from AggregateCoreComponent a where a.guid = ?1")
    public AggregateCoreComponent findAccIdAndDenByGuid(String guid);

    @Query("select a from AggregateCoreComponent a where a.accId = ?1 and a.revisionNum = ?2")
    public AggregateCoreComponent findOneByAccIdAndRevisionNum(int accId, int revisionNum);

    @Query("select new AggregateCoreComponent(a.accId, a.basedAccId, a.definition) from AggregateCoreComponent a " +
            "where a.accId = ?1 and a.revisionNum = ?2")
    public AggregateCoreComponent findAccIdAndBasedAccIdAndDefinitionByAccIdAndRevisionNum(int accId, int revisionNum);
}
