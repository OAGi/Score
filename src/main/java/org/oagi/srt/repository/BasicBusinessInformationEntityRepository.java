package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.BasicBusinessInformationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BasicBusinessInformationEntityRepository extends JpaRepository<BasicBusinessInformationEntity, Integer> {

    @Query("select b from BasicBusinessInformationEntity b where b.fromAbieId = ?1")
    public List<BasicBusinessInformationEntity> findByFromAbieId(int fromAbieId);

    @Query("select b from BasicBusinessInformationEntity b where b.fromAbieId = ?1 and b.used = ?2")
    public List<BasicBusinessInformationEntity> findByFromAbieIdAndUsed(int fromAbieId, boolean used);
}
