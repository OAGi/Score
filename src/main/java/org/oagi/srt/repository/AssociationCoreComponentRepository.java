package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.AssociationCoreComponent;

import java.util.List;

public interface AssociationCoreComponentRepository {

    public List<AssociationCoreComponent> findByFromAccId(int fromAccId);

    public AssociationCoreComponent findOneByAsccId(int asccId);

    public void save(AssociationCoreComponent associationCoreComponent);
}
