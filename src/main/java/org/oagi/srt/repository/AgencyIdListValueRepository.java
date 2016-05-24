package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.AgencyIdListValue;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AgencyIdListValueRepository extends CrudRepository<AgencyIdListValue, Integer> {

    @Query("select a from AgencyIdListValue a where a.ownerListId = ?1")
    public List<AgencyIdListValue> findByOwnerListId(int ownerListId);

    @Query("select a from AgencyIdListValue a where a.value = ?1")
    public AgencyIdListValue findOneByValue(String value);
}
