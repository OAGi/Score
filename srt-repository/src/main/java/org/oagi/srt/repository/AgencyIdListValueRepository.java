package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.AgencyIdListValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AgencyIdListValueRepository extends JpaRepository<AgencyIdListValue, Long> {

    @Query("select a from AgencyIdListValue a where a.ownerListId = ?1")
    public List<AgencyIdListValue> findByOwnerListId(long ownerListId);

    @Query("select a from AgencyIdListValue a where a.value = ?1")
    public AgencyIdListValue findOneByValue(String value);
}
