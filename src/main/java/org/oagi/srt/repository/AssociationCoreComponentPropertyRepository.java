package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.AssociationCoreComponentProperty;

import java.util.List;

public interface AssociationCoreComponentPropertyRepository {

    public List<AssociationCoreComponentProperty> findAll();

    public List<AssociationCoreComponentProperty> findByPropertyTermContaining(String propertyTerm);

    public AssociationCoreComponentProperty findOneByAsccpId(int asccpId);

    public AssociationCoreComponentProperty findOneByAsccpIdAndRevisionNum(int asccpId, int revisionNum);

    public AssociationCoreComponentProperty findOneByRoleOfAccId(int roleOfAccId);

    public AssociationCoreComponentProperty findOneByGuid(String guid);

    public void save(AssociationCoreComponentProperty associationCoreComponentProperty);
}
