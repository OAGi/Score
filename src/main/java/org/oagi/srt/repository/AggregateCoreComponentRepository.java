package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.AggregateCoreComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AggregateCoreComponentRepository extends JpaRepository<AggregateCoreComponent, Integer> {

    @Query("select a from AggregateCoreComponent a where a.guid = ?1")
    public AggregateCoreComponent findOneByGuid(String guid);

    @Query("select a from AggregateCoreComponent a where a.accId = ?1 and a.revisionNum = ?2")
    public AggregateCoreComponent findOneByAccIdAndRevisionNum(int accId, int revisionNum);
}
