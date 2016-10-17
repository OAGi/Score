package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.BasicBusinessInformationEntity;
import org.oagi.srt.repository.entity.BasicBusinessInformationEntityProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BasicBusinessInformationEntityPropertyRepository
        extends JpaRepository<BasicBusinessInformationEntityProperty, Long>,
        BulkInsertRepository<BasicBusinessInformationEntityProperty> {

    @Query("select b from BasicBusinessInformationEntityProperty b where b.ownerTopLevelAbie.topLevelAbieId = ?1")
    public List<BasicBusinessInformationEntityProperty> findByOwnerTopLevelAbieId(long ownerTopLevelAbieId);
}
