package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.AssociationCoreComponentProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

import static org.oagi.srt.common.SRTConstants.ANY_ASCCP_DEN;

public interface AssociationCoreComponentPropertyRepository extends JpaRepository<AssociationCoreComponentProperty, Integer> {

    @Query("select a from AssociationCoreComponentProperty a order by a.propertyTerm asc")
    public List<AssociationCoreComponentProperty> findAllOrderByPropertyTermAsc();

    @Query("select a from AssociationCoreComponentProperty a where a.propertyTerm = ?1")
    public List<AssociationCoreComponentProperty> findByPropertyTermContaining(String propertyTerm);

    @Query("select a from AssociationCoreComponentProperty a where a.asccpId = ?1 and a.revisionNum = ?2")
    public AssociationCoreComponentProperty findOneByAsccpIdAndRevisionNum(int asccpId, int revisionNum);

    @Query("select new AssociationCoreComponentProperty(a.asccpId, a.roleOfAccId, a.definition) " +
            "from AssociationCoreComponentProperty a where a.asccpId = ?1 and a.revisionNum = ?2")
    public AssociationCoreComponentProperty findAsccpIdAndRoleOfAccIdAndDefinitionByAsccpIdAndRevisionNum(int asccpId, int revisionNum);

    @Query("select a from AssociationCoreComponentProperty a where a.roleOfAccId = ?1")
    public AssociationCoreComponentProperty findOneByRoleOfAccId(int roleOfAccId);

    @Query("select a from AssociationCoreComponentProperty a where a.guid = ?1")
    public AssociationCoreComponentProperty findOneByGuid(String guid);

    @Query("select case when count(a) > 0 then true else false end from AssociationCoreComponentProperty a where a.guid = ?1")
    public boolean existsByGuid(String guid);

    @Query("select new AssociationCoreComponentProperty(a.asccpId, a.den) from AssociationCoreComponentProperty a where a.guid = ?1")
    public AssociationCoreComponentProperty findAsccpIdAndDenByGuid(String guid);

    @Query("select distinct asccp.propertyTerm from " +
            "AggregateBusinessInformationEntity abie, " +
            "BusinessObjectDocument bod," +
            "AssociationBusinessInformationEntityProperty asbiep, " +
            "AssociationCoreComponentProperty asccp where " +
            "bod.topLevelAbieId = abie.abieId and " +
            "abie.abieId = asbiep.roleOfAbieId and " +
            "asbiep.basedAsccpId = asccp.asccpId and " +
            "asccp.propertyTerm like %?1%")
    public List<String> findPropertyTermByropertyTermContains(String propertyTerm);

    @Query("select a from AssociationCoreComponentProperty a where a.den = '" + ANY_ASCCP_DEN + "'")
    public AssociationCoreComponentProperty findAny();
}
