package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.AgencyIdList;

public interface AgencyIdListRepository {

    public void save(AgencyIdList agencyIdList);

    public void updateAgencyId(int agencyIdListValue);
}
