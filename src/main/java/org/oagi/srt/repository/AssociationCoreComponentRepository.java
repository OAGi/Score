package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.AssociationCoreComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AssociationCoreComponentRepository extends JpaRepository<AssociationCoreComponent, Integer> {

    @Query("select a from AssociationCoreComponent a where a.fromAccId = ?1")
    public List<AssociationCoreComponent> findByFromAccId(int fromAccId);

    @Query("select a from AssociationCoreComponent a where a.definition = ?1")
    public List<AssociationCoreComponent> findByDefinition(String definition);

    @Query("select a from AssociationCoreComponent a where a.den like ?1%")
    public List<AssociationCoreComponent> findByDenStartsWith(String den);

    @Query("select a from AssociationCoreComponent a where a.den like %?1%")
    public List<AssociationCoreComponent> findByDenContaining(String den);

    @Query("select a from AssociationCoreComponent a where a.guid = ?1 and a.fromAccId = ?2 and a.toAsccpId = ?3")
    public AssociationCoreComponent findOneByGuidAndFromAccIdAndToAsccpId(String guid, int fromAccId, int toAsccpId);

    @Query("select case when count(a) > 0 then true else false end from AssociationCoreComponent a where a.guid = ?1 and a.fromAccId = ?2 and a.toAsccpId = ?3")
    public boolean existsByGuidAndFromAccIdAndToAsccpId(String guid, int fromAccId, int toAsccpId);
}
