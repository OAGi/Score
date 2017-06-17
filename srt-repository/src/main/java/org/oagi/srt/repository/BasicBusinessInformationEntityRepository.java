package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.BasicBusinessInformationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BasicBusinessInformationEntityRepository
        extends JpaRepository<BasicBusinessInformationEntity, Long> {

    @Query("select b from BasicBusinessInformationEntity b where b.fromAbieId = ?1")
    public List<BasicBusinessInformationEntity> findByFromAbieId(long fromAbieId);

    @Query("select count(b) from BasicBusinessInformationEntity b where b.fromAbieId = ?1")
    public int countByFromAbieId(long fromAbieId);

    @Query("select b from BasicBusinessInformationEntity b where b.ownerTopLevelAbieId = ?1")
    public List<BasicBusinessInformationEntity> findByOwnerTopLevelAbieId(long ownerTopLevelAbieId);

    @Query("select b from BasicBusinessInformationEntity b where b.ownerTopLevelAbieId = ?1 and b.used = true")
    public List<BasicBusinessInformationEntity> findByOwnerTopLevelAbieIdAndUsedIsTrue(long ownerTopLevelAbieId);

    @Query("select b from BasicBusinessInformationEntity b where b.basedBccId = ?1 and b.fromAbieId = ?2 and b.ownerTopLevelAbieId = ?3")
    public BasicBusinessInformationEntity findOneByBasedBccIdAndFromAbieIdAndOwnerTopLevelAbieId(long basedBccId, long fromAbieId, long ownerTopLevelAbieId);

    @Query("select b.definitionId from BasicBusinessInformationEntity b where b.ownerTopLevelAbieId = ?1")
    public List<Long> findDefinitionIdByOwnerTopLevelAbieId(long ownerTopLevelAbieId);

    @Modifying
    @Query("delete from BasicBusinessInformationEntity b where b.ownerTopLevelAbieId = ?1")
    public void deleteByOwnerTopLevelAbieId(long ownerTopLevelAbieId);

}
