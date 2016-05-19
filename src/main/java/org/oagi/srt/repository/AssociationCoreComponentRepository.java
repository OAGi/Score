package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.AssociationCoreComponent;

import java.util.List;

public interface AssociationCoreComponentRepository {

    public List<AssociationCoreComponent> findByFromAccId(int fromAccId);

    public List<AssociationCoreComponent> findByDefinition(String definition);

    public List<AssociationCoreComponent> findByDenStartsWith(String den);

    public List<AssociationCoreComponent> findByDenContaining(String den);

    public AssociationCoreComponent findOneByAsccId(int asccId);

    public AssociationCoreComponent findOneByGuid(String guid);

    public void save(AssociationCoreComponent associationCoreComponent);

    public void update(AssociationCoreComponent associationCoreComponent);
}
