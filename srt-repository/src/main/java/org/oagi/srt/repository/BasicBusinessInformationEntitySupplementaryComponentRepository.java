package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.BasicBusinessInformationEntitySupplementaryComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BasicBusinessInformationEntitySupplementaryComponentRepository
        extends JpaRepository<BasicBusinessInformationEntitySupplementaryComponent, Long> {

    @Query("select b from BasicBusinessInformationEntitySupplementaryComponent b where b.bbieId = ?1")
    public List<BasicBusinessInformationEntitySupplementaryComponent> findByBbieId(long bbieId);

    @Query("select count(b) from BasicBusinessInformationEntitySupplementaryComponent b where b.bbieId = ?1")
    public int countByBbieId(long bbieId);

    @Query("select b from BasicBusinessInformationEntitySupplementaryComponent b where b.ownerTopLevelAbieId = ?1")
    public List<BasicBusinessInformationEntitySupplementaryComponent> findByOwnerTopLevelAbieId(long ownerTopLevelAbieId);

    @Query("select b from BasicBusinessInformationEntitySupplementaryComponent b where b.ownerTopLevelAbieId = ?1 and b.used = true")
    public List<BasicBusinessInformationEntitySupplementaryComponent> findByOwnerTopLevelAbieIdAndUsedIsTrue(long ownerTopLevelAbieId);

    @Query("select b from BasicBusinessInformationEntitySupplementaryComponent b where b.bbieId = ?1 and b.dtScId = ?2 and b.ownerTopLevelAbieId = ?3")
    public BasicBusinessInformationEntitySupplementaryComponent findOneByBbieIdAndDtScIdAndOwnerTopLevelAbieId(long bbieId, long dtScId, long ownerTopLevelAbieId);

    @Query("select b.definitionId from BasicBusinessInformationEntitySupplementaryComponent b where b.ownerTopLevelAbieId = ?1")
    public List<Long> findDefinitionIdByOwnerTopLevelAbieId(long ownerTopLevelAbieId);

    @Modifying
    @Query("delete from BasicBusinessInformationEntitySupplementaryComponent b where b.ownerTopLevelAbieId = ?1")
    public void deleteByOwnerTopLevelAbieId(long ownerTopLevelAbieId);
}
