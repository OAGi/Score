package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.AssociationBusinessInformationEntityProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AssociationBusinessInformationEntityPropertyRepository
        extends JpaRepository<AssociationBusinessInformationEntityProperty, Integer> {

    @Query("select a from AssociationBusinessInformationEntityProperty a where a.roleOfAbieId = ?1")
    public AssociationBusinessInformationEntityProperty findOneByRoleOfAbieId(int roleOfAbieId);
}
