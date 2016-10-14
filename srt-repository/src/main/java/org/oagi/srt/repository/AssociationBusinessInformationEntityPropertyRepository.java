package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.AssociationBusinessInformationEntityProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AssociationBusinessInformationEntityPropertyRepository
        extends JpaRepository<AssociationBusinessInformationEntityProperty, Long>,
        BulkInsertRepository<AssociationBusinessInformationEntityProperty> {

    @Query("select a from AssociationBusinessInformationEntityProperty a where a.roleOfAbie.abieId = ?1")
    public AssociationBusinessInformationEntityProperty findOneByRoleOfAbieId(long roleOfAbieId);

    @Query("select a from AssociationBusinessInformationEntityProperty a where a.ownerTopLevelAbie.topLevelAbieId = ?1")
    public List<AssociationBusinessInformationEntityProperty> findByOwnerTopLevelAbieId(long ownerTopLevelAbieId);
}
