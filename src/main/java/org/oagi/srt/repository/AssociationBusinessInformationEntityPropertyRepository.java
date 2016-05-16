package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.AssociationBusinessInformationEntityProperty;

public interface AssociationBusinessInformationEntityPropertyRepository {

    public int findGreatestId();

    public AssociationBusinessInformationEntityProperty findOneByRoleOfAbieId(int roleOfAbieId);

    public AssociationBusinessInformationEntityProperty findOneByAsbiepId(int asbiepId);

    public void save(AssociationBusinessInformationEntityProperty associationBusinessInformationEntityProperty);

    public void update(AssociationBusinessInformationEntityProperty associationBusinessInformationEntityProperty);
}
