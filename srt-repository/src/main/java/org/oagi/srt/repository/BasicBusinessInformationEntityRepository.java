package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.BasicBusinessInformationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BasicBusinessInformationEntityRepository
        extends JpaRepository<BasicBusinessInformationEntity, Long>,
        BulkInsertRepository<BasicBusinessInformationEntity> {

    @Query("select b from BasicBusinessInformationEntity b where b.fromAbieId = ?1")
    public List<BasicBusinessInformationEntity> findByFromAbieId(long fromAbieId);

    @Query("select b from BasicBusinessInformationEntity b where b.ownerTopLevelAbieId = ?1 and b.used = true")
    public List<BasicBusinessInformationEntity> findByOwnerTopLevelAbieIdAndUsedIsTrue(long ownerTopLevelAbieId);

}
