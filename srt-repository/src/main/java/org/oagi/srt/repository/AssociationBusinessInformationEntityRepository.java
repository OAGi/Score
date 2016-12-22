package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.AssociationBusinessInformationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AssociationBusinessInformationEntityRepository
        extends JpaRepository<AssociationBusinessInformationEntity, Integer> {

    @Query("select a from AssociationBusinessInformationEntity a where a.fromAbieId = ?1")
    public List<AssociationBusinessInformationEntity> findByFromAbieId(long fromAbieId);

    @Query("select count(a) from AssociationBusinessInformationEntity a where a.fromAbieId = ?1")
    public int countByFromAbieId(long fromAbieId);

    @Query("select a from AssociationBusinessInformationEntity a where a.ownerTopLevelAbieId = ?1")
    public List<AssociationBusinessInformationEntity> findByOwnerTopLevelAbieId(long ownerTopLevelAbieId);

    @Query("select a from AssociationBusinessInformationEntity a where a.ownerTopLevelAbieId = ?1 and a.used = true")
    public List<AssociationBusinessInformationEntity> findByOwnerTopLevelAbieIdAndUsedIsTrue(long ownerTopLevelAbieId);

    @Query("select a from AssociationBusinessInformationEntity a where a.basedAsccId = ?1 and a.fromAbieId = ?2 and a.ownerTopLevelAbieId = ?3")
    public AssociationBusinessInformationEntity findOneByBasedAsccIdAndFromAbieIdAndOwnerTopLevelAbieId(long basedAsccId, long fromAbieId, long ownerTopLevelAbieId);

    @Modifying
    @Query("delete from AssociationBusinessInformationEntity a where a.ownerTopLevelAbieId = ?1")
    public void deleteByOwnerTopLevelAbieId(long ownerTopLevelAbieId);
}
