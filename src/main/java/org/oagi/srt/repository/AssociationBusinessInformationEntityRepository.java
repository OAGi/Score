package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.AssociationBusinessInformationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AssociationBusinessInformationEntityRepository
        extends JpaRepository<AssociationBusinessInformationEntity, Integer> {

    @Query("select a from AssociationBusinessInformationEntity a where a.fromAbieId = ?1")
    public List<AssociationBusinessInformationEntity> findByFromAbieId(int fromAbieId);

    @Query("select a from AssociationBusinessInformationEntity a where a.fromAbieId = ?1 and a.used = ?2")
    public List<AssociationBusinessInformationEntity> findByFromAbieIdAndUsed(int fromAbieId, boolean used);
}
