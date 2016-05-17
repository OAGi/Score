package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.AgencyIdListValue;

import java.util.List;

public interface AgencyIdListValueRepository {

    public List<AgencyIdListValue> findAll();

    public AgencyIdListValue findOneByAgencyIdListValueId(int agencyIdListValueId);

    public AgencyIdListValue findOneByValue(String value);

    public void save(AgencyIdListValue agencyIdListValue);
}
