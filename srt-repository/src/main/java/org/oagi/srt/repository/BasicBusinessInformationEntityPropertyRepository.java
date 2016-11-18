package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.BasicBusinessInformationEntityProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BasicBusinessInformationEntityPropertyRepository
        extends JpaRepository<BasicBusinessInformationEntityProperty, Long>,
        BulkInsertRepository<BasicBusinessInformationEntityProperty> {

    @Query("select b from BasicBusinessInformationEntityProperty b where b.ownerTopLevelAbieId = ?1")
    public List<BasicBusinessInformationEntityProperty> findByOwnerTopLevelAbieId(long ownerTopLevelAbieId);

    @Modifying
    @Query("delete from BasicBusinessInformationEntityProperty a where a.ownerTopLevelAbieId = ?1")
    public void deleteByOwnerTopLevelAbieId(long ownerTopLevelAbieId);
}
