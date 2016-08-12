package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.AggregateBusinessInformationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AggregateBusinessInformationEntityRepository
        extends JpaRepository<AggregateBusinessInformationEntity, Integer>,
        BulkInsertRepository<AggregateBusinessInformationEntity> {

    @Query("select a from AggregateBusinessInformationEntity a where a.ownerTopLevelAbieId = ?1")
    public List<AggregateBusinessInformationEntity> findByOwnerTopLevelAbieId(int ownerTopLevelAbieId);
}
