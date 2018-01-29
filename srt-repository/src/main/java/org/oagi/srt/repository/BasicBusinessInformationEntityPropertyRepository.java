package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.BasicBusinessInformationEntityProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BasicBusinessInformationEntityPropertyRepository
        extends JpaRepository<BasicBusinessInformationEntityProperty, Long> {

    @Query("select b from BasicBusinessInformationEntityProperty b where b.ownerTopLevelAbieId = ?1")
    public List<BasicBusinessInformationEntityProperty> findByOwnerTopLevelAbieId(long ownerTopLevelAbieId);

    @Query("select b from BasicBusinessInformationEntityProperty b where b.basedBccpId = ?1 and b.ownerTopLevelAbieId = ?2")
    public BasicBusinessInformationEntityProperty findOneByBasedBccpIdAndOwnerTopLevelAbieId(long basedBccpId, long ownerTopLevelAbieId);

    @Modifying
    @Query("delete from BasicBusinessInformationEntityProperty b where b.ownerTopLevelAbieId = ?1")
    public void deleteByOwnerTopLevelAbieId(long ownerTopLevelAbieId);
}
