package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.AggregateBusinessInformationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface AggregateBusinessInformationEntityRepository
        extends JpaRepository<AggregateBusinessInformationEntity, Integer>, BulkInsertRepository<AggregateBusinessInformationEntity> {

    @Query("select a from AggregateBusinessInformationEntity a where a.topLevel = ?1")
    public List<AggregateBusinessInformationEntity> findByTopLevel(boolean topLevel);

    @Query("select a from AggregateBusinessInformationEntity a where a.abieId IN ?1")
    public List<AggregateBusinessInformationEntity> findByAbieIdIn(Collection<Integer> abieIds);
}
