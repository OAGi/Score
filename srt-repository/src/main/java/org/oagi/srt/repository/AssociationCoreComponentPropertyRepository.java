package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.AssociationCoreComponent;
import org.oagi.srt.repository.entity.AssociationCoreComponentProperty;
import org.oagi.srt.repository.entity.CoreComponentState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

import static org.oagi.srt.common.SRTConstants.ANY_ASCCP_DEN;

public interface AssociationCoreComponentPropertyRepository extends JpaRepository<AssociationCoreComponentProperty, Long> {

    @Query("select a from AssociationCoreComponentProperty a where a.revisionNum = ?1")
    public List<AssociationCoreComponentProperty> findAllWithRevisionNum(int revisionNum);

    @Query("select a from AssociationCoreComponentProperty a order by a.propertyTerm asc")
    public List<AssociationCoreComponentProperty> findAllOrderByPropertyTermAsc();

    @Query("select a from AssociationCoreComponentProperty a where a.propertyTerm = ?1")
    public List<AssociationCoreComponentProperty> findByPropertyTermContaining(String propertyTerm);

    @Query("select a from AssociationCoreComponentProperty a where a.asccpId = ?1 and a.revisionNum = ?2 and a.state = ?3")
    public AssociationCoreComponentProperty findOneByAsccpIdAndRevisionNumAndState(long asccpId, int revisionNum, CoreComponentState state);

    @Query("select new AssociationCoreComponentProperty(a.asccpId, a.roleOfAccId, a.definition) " +
            "from AssociationCoreComponentProperty a where a.asccpId = ?1 and a.revisionNum = ?2")
    public AssociationCoreComponentProperty findAsccpIdAndRoleOfAccIdAndDefinitionByAsccpIdAndRevisionNum(long asccpId, int revisionNum);

    @Query("select a from AssociationCoreComponentProperty a where a.roleOfAccId = ?1 and a.revisionNum = 0")
    public AssociationCoreComponentProperty findOneByRoleOfAccId(long roleOfAccId);

    @Query("select a from AssociationCoreComponentProperty a where a.guid = ?1")
    public AssociationCoreComponentProperty findOneByGuid(String guid);

    @Query("select case when count(a) > 0 then true else false end from AssociationCoreComponentProperty a where a.guid = ?1")
    public boolean existsByGuid(String guid);

    @Query("select new AssociationCoreComponentProperty(a.asccpId, a.den) from AssociationCoreComponentProperty a where a.guid = ?1")
    public AssociationCoreComponentProperty findAsccpIdAndDenByGuid(String guid);

    @Query("select distinct asccp.propertyTerm from " +
            "AggregateBusinessInformationEntity abie, " +
            "TopLevelAbie topLevelAbie," +
            "AssociationBusinessInformationEntityProperty asbiep, " +
            "AssociationCoreComponentProperty asccp where " +
            "topLevelAbie.abie.abieId = abie.abieId and " +
            "abie.abieId = asbiep.roleOfAbieId and " +
            "asbiep.basedAsccpId = asccp.asccpId and " +
            "asccp.propertyTerm like %?1%")
    public List<String> findPropertyTermByPropertyTermContains(String propertyTerm);

    @Query("select a from AssociationCoreComponentProperty a where a.den = '" + ANY_ASCCP_DEN + "'")
    public AssociationCoreComponentProperty findAny();

    @Query("select a from AssociationCoreComponentProperty a where a.asccpId in ?1")
    public List<AssociationCoreComponentProperty> findByAsccpId(Collection<Long> asccpId);

    @Query("select a from AssociationCoreComponentProperty a where a.revisionNum = ?1 order by a.creationTimestamp desc")
    public List<AssociationCoreComponentProperty> findAllByRevisionNum(int revisionNum);

    @Query("select a from AssociationCoreComponentProperty a where a.revisionNum = ?1 and a.state in ?2 order by a.creationTimestamp desc")
    public List<AssociationCoreComponentProperty> findAllByRevisionNumAndStates(int revisionNum, Collection<CoreComponentState> states);

    @Query("select a from AssociationCoreComponentProperty a where a.currentAsccpId = ?1 and a.revisionTrackingNum = (" +
            "select MAX(a.revisionTrackingNum) from AssociationCoreComponentProperty a where a.currentAsccpId = ?1 group by a.currentAsccpId)")
    public AssociationCoreComponentProperty findLatestOneByCurrentAsccpId(long currentAsccpId);

    @Modifying
    @Query("delete from AssociationCoreComponentProperty a where a.currentAsccpId = ?1")
    public void deleteByCurrentAsccpId(long currentAsccpId);
}
