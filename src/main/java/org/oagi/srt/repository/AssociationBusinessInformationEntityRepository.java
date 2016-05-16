package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.AssociationBusinessInformationEntity;

import java.util.List;

public interface AssociationBusinessInformationEntityRepository {

    public int findGreatestId();

    public List<AssociationBusinessInformationEntity> findByFromAbieId(int fromAbieId);

    public void save(AssociationBusinessInformationEntity associationBusinessInformationEntity);

    public void update(AssociationBusinessInformationEntity associationBusinessInformationEntity);
}
