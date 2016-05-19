package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.AgencyIdList;

import java.util.List;

public interface AgencyIdListRepository {

    public List<AgencyIdList> findAll();

    public AgencyIdList findOneByAgencyIdListId(int agencyIdListId);

    public AgencyIdList findOneByGuid(String guid);

    public AgencyIdList findOneByName(String name);

    public void save(AgencyIdList agencyIdList);

    public void updateAgencyId(int agencyIdListValue);
}
