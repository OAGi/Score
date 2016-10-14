package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.AssociationBusinessInformationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AssociationBusinessInformationEntityRepository
        extends JpaRepository<AssociationBusinessInformationEntity, Integer>,
        BulkInsertRepository<AssociationBusinessInformationEntity> {

    @Query("select a from AssociationBusinessInformationEntity a where a.fromAbie.abieId = ?1")
    public List<AssociationBusinessInformationEntity> findByFromAbieId(long fromAbieId);

    @Query("select a from AssociationBusinessInformationEntity a where a.ownerTopLevelAbie.topLevelAbieId = ?1 and a.used = true")
    public List<AssociationBusinessInformationEntity> findByOwnerTopLevelAbieIdAndUsedIsTrue(long ownerTopLevelAbieId);
}
