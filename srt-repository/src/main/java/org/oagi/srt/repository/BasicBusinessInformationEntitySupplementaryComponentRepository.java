package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.BasicBusinessInformationEntityProperty;
import org.oagi.srt.repository.entity.BasicBusinessInformationEntitySupplementaryComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BasicBusinessInformationEntitySupplementaryComponentRepository
        extends JpaRepository<BasicBusinessInformationEntitySupplementaryComponent, Long>,
        BulkInsertRepository<BasicBusinessInformationEntitySupplementaryComponent> {

    @Query("select b from BasicBusinessInformationEntitySupplementaryComponent b where b.bbieId = ?1")
    public List<BasicBusinessInformationEntitySupplementaryComponent> findByBbieId(long bbieId);

    @Query("select b from BasicBusinessInformationEntitySupplementaryComponent b where b.ownerTopLevelAbieId = ?1")
    public List<BasicBusinessInformationEntitySupplementaryComponent> findByOwnerTopLevelAbieId(long ownerTopLevelAbieId);

    @Query("select b from BasicBusinessInformationEntitySupplementaryComponent b where b.ownerTopLevelAbieId = ?1 and b.used = true")
    public List<BasicBusinessInformationEntitySupplementaryComponent> findByOwnerTopLevelAbieIdAndUsedIsTrue(long ownerTopLevelAbieId);
}
