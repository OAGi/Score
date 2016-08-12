package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.AssociationBusinessInformationEntityProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AssociationBusinessInformationEntityPropertyRepository
        extends JpaRepository<AssociationBusinessInformationEntityProperty, Integer>,
        BulkInsertRepository<AssociationBusinessInformationEntityProperty> {

    @Query("select a from AssociationBusinessInformationEntityProperty a where a.roleOfAbieId = ?1")
    public AssociationBusinessInformationEntityProperty findOneByRoleOfAbieId(int roleOfAbieId);

    @Query("select a from AssociationBusinessInformationEntityProperty a where a.ownerTopLevelAbieId = ?1")
    public List<AssociationBusinessInformationEntityProperty> findByOwnerTopLevelAbieId(int ownerTopLevelAbieId);
}
