package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.AggregateBusinessInformationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AggregateBusinessInformationEntityRepository
        extends JpaRepository<AggregateBusinessInformationEntity, Long>,
        BulkInsertRepository<AggregateBusinessInformationEntity> {

    @Query("select a from AggregateBusinessInformationEntity a where a.ownerTopLevelAbieId = ?1")
    public List<AggregateBusinessInformationEntity> findByOwnerTopLevelAbieId(long ownerTopLevelAbieId);

    @Query("select a from AggregateBusinessInformationEntity a where a.bizCtxId = ?1")
    public List<AggregateBusinessInformationEntity> findByBizCtxId(long bizCtxId);

    @Modifying
    @Query("delete from AggregateBusinessInformationEntity a where a.ownerTopLevelAbieId = ?1")
    public void deleteByOwnerTopLevelAbieId(long ownerTopLevelAbieId);
}
