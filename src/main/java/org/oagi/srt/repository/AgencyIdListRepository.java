package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.AgencyIdList;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface AgencyIdListRepository extends CrudRepository<AgencyIdList, Integer> {

    @Query("select a from AgencyIdList a where a.guid = ?1")
    public AgencyIdList findOneByGuid(String guid);

    @Query("select a from AgencyIdList a where a.name = ?1")
    public AgencyIdList findOneByName(String name);

    @Modifying
    @Query("update AgencyIdList a set a.agencyId = ?1")
    public void updateAgencyId(int agencyIdListValue);
}
