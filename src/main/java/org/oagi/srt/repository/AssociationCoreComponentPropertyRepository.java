package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.AssociationCoreComponentProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AssociationCoreComponentPropertyRepository extends JpaRepository<AssociationCoreComponentProperty, Integer> {

    @Query("select a from AssociationCoreComponentProperty a where a.propertyTerm = ?1")
    public List<AssociationCoreComponentProperty> findByPropertyTermContaining(String propertyTerm);

    @Query("select a from AssociationCoreComponentProperty a where a.asccpId = ?1 and a.revisionNum = ?2")
    public AssociationCoreComponentProperty findOneByAsccpIdAndRevisionNum(int asccpId, int revisionNum);

    @Query("select a from AssociationCoreComponentProperty a where a.roleOfAccId = ?1")
    public AssociationCoreComponentProperty findOneByRoleOfAccId(int roleOfAccId);

    @Query("select a from AssociationCoreComponentProperty a where a.guid = ?1")
    public AssociationCoreComponentProperty findOneByGuid(String guid);
}
