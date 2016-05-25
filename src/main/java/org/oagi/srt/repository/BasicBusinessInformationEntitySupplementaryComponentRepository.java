package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.BasicBusinessInformationEntitySupplementaryComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BasicBusinessInformationEntitySupplementaryComponentRepository
        extends JpaRepository<BasicBusinessInformationEntitySupplementaryComponent, Integer> {

    @Query("select b from BasicBusinessInformationEntitySupplementaryComponent b where b.bbieId = ?1")
    public List<BasicBusinessInformationEntitySupplementaryComponent> findByBbieId(int bbieId);

    @Query("select b from BasicBusinessInformationEntitySupplementaryComponent b where b.bbieId = ?1 and b.used = ?2")
    public List<BasicBusinessInformationEntitySupplementaryComponent> findByBbieIdAndUsed(int bbieId, boolean used);
}
