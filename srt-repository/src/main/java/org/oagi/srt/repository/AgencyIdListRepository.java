package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.AgencyIdList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface AgencyIdListRepository extends JpaRepository<AgencyIdList, Integer> {

    @Query("select a from AgencyIdList a where a.guid = ?1")
    public AgencyIdList findOneByGuid(String guid);

    @Query("select a from AgencyIdList a where a.name = ?1")
    public AgencyIdList findOneByName(String name);

    @Modifying
    @Query("update AgencyIdList a set a.agencyIdListId = ?1")
    public void updateAgencyId(int agencyIdListValue);
}
