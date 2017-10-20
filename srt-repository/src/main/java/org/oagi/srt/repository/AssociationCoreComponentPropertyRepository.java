package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.AssociationCoreComponentProperty;
import org.oagi.srt.repository.entity.CoreComponentState;
import org.oagi.srt.repository.entity.CoreComponents;
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

    @Query("select a from AssociationCoreComponentProperty a where a.roleOfAccId = ?1 and a.revisionNum = 0")
    public List<AssociationCoreComponentProperty> findByRoleOfAccId(long roleOfAccId);

    @Query("select a from AssociationCoreComponentProperty a where a.guid = ?1")
    public AssociationCoreComponentProperty findOneByGuid(String guid);

    @Query("select case when count(a) > 0 then true else false end from AssociationCoreComponentProperty a where a.guid = ?1")
    public boolean existsByGuid(String guid);

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

    @Query("select a.roleOfAccId from AssociationCoreComponentProperty a where a.asccpId in ?1")
    public List<Long> findRoleOfAccIdByAsccpId(Collection<Long> asccpId);

    @Query("select a from AssociationCoreComponentProperty a where a.revisionNum = ?1 order by a.creationTimestamp desc")
    public List<AssociationCoreComponentProperty> findAllByRevisionNum(int revisionNum);

    @Query("select a from AssociationCoreComponentProperty a where a.revisionNum = ?1 and a.state in ?2 order by a.creationTimestamp desc")
    public List<AssociationCoreComponentProperty> findAllByRevisionNumAndStates(int revisionNum, Collection<CoreComponentState> states);

    @Query("select a from AssociationCoreComponentProperty a where a.currentAsccpId = ?1 and a.revisionNum = (" +
            "select MAX(a.revisionNum) from AssociationCoreComponentProperty a where a.currentAsccpId = ?1 group by a.currentAsccpId) order by a.creationTimestamp desc")
    public List<AssociationCoreComponentProperty> findAllWithLatestRevisionNumByCurrentAsccpId(long currentAsccpId);

    @Query("select a from AssociationCoreComponentProperty a where a.currentAsccpId = ?1 and a.revisionNum = ?2 and a.revisionTrackingNum = ?3")
    public AssociationCoreComponentProperty findOneByCurrentAsccpIdAndRevisions(long currentAsccpId, int revisionNum, int revisionTrackingNum);

    @Query("select COALESCE(MAX(a.revisionNum), 0) from AssociationCoreComponentProperty a where a.currentAsccpId = ?1")
    public Integer findMaxRevisionNumByCurrentAsccpId(long currentAsccpId);

    @Query("select COALESCE(MAX(a.revisionTrackingNum), 0) from AssociationCoreComponentProperty a where a.currentAsccpId = ?1 and a.revisionNum = ?2")
    public Integer findMaxRevisionTrackingNumByCurrentAsccpIdAndRevisionNum(long currentAsccpId, int revisionNum);

    @Query("select COALESCE(MAX(a.revisionNum), 0) from AssociationCoreComponentProperty a where a.currentAsccpId = ?1 and a.releaseId = ?2")
    public Integer findMaxRevisionNumByCurrentAsccpIdAndReleaseId(long currentAsccpId, long releaseId);

    @Query("select COALESCE(MAX(a.revisionTrackingNum), 0) from AssociationCoreComponentProperty a where a.currentAsccpId = ?1 and a.revisionNum = ?2 and a.releaseId = ?3")
    public Integer findMaxRevisionTrackingNumByCurrentAsccpIdAndRevisionNumAndReleaseId(long currentAsccpId, int revisionNum, long releaseId);

    @Query("select COALESCE(MAX(a.revisionNum), 0) from AssociationCoreComponentProperty a where a.asccpId = ?1 and a.releaseId = ?2")
    public Integer findMaxRevisionNumByAsccpIdAndReleaseId(long asccpId, long releaseId);

    @Query("select COALESCE(MAX(a.revisionTrackingNum), 0) from AssociationCoreComponentProperty a where a.asccpId = ?1 and a.revisionNum = ?2 and a.releaseId = ?3")
    public Integer findMaxRevisionTrackingNumByAsccpIdAndRevisionNumAndReleaseId(long currentAsccpId, int revisionNum, long releaseId);

    @Query("select COALESCE(MAX(a.revisionNum), 0) from AssociationCoreComponentProperty a where a.asccpId = ?1")
    public Integer findMaxRevisionNumByAsccpId(long asccpId);

    @Query("select COALESCE(MAX(a.revisionTrackingNum), 0) from AssociationCoreComponentProperty a where a.asccpId = ?1 and a.revisionNum = ?2")
    public Integer findMaxRevisionTrackingNumByAsccpIdAndRevisionNum(long currentAsccpId, int revisionNum);

    @Modifying
    @Query("delete from AssociationCoreComponentProperty a where a.currentAsccpId = ?1 and a.revisionNum = ?2")
    public void deleteByCurrentAsccpIdAndRevisionNum(long currentAsccpId, int revisionNum);

    @Modifying
    @Query("delete from AssociationCoreComponentProperty a where a.currentAsccpId = ?1 and a.revisionNum = ?2 and a.revisionTrackingNum <> ?3")
    public void deleteByCurrentAsccpIdAndRevisionNumAndNotRevisionTrackingNum(long currentAsccpId, int revisionNum, int revisionTrackingNum);

    @Modifying
    @Query("update AssociationCoreComponentProperty a set a.state = ?4 where a.currentAsccpId = ?1 and a.revisionNum = ?2 and a.revisionTrackingNum = ?3")
    public void updateStateByCurrentAsccpIdAndRevisionNumAndNotRevisionTrackingNum(long currentAsccpId, int revisionNum, int revisionTrackingNum, CoreComponentState state);

    @Modifying
    @Query("delete from AssociationCoreComponentProperty a where a.currentAsccpId = ?1")
    public void deleteByCurrentAsccpId(long currentAsccpId);

    @Query("select a.revisionNum from AssociationCoreComponentProperty a where a.asccpId = ?1")
    int findRevisionNumByAsccpId(long asccpId);

    @Modifying
    @Query("update AssociationCoreComponentProperty a set a.releaseId = ?2 where a.asccpId = ?1")
    void updateReleaseByAsccpId(long asccpId, Long releaseId);

    @Query("select a from AssociationCoreComponentProperty a where a.currentAsccpId = (select x.currentAsccpId from AssociationCoreComponentProperty x where x.asccpId = ?1) and a.revisionNum < ?2 and a.releaseId is null")
    List<AssociationCoreComponentProperty> findPreviousNonReleasedRevisions(long id, int revisionNum);


    @Query("select a from AssociationCoreComponentProperty a where a.currentAsccpId = (select x.currentAsccpId from AssociationCoreComponentProperty x where x.asccpId = ?1) and a.revisionNum > ?2 and a.releaseId is null")
    List<AssociationCoreComponentProperty> findFollowingNonReleasedRevisions(long id, int revisionNum);

    @Query("select a from AssociationCoreComponentProperty a where a.releaseId = ?1")
    List<AssociationCoreComponentProperty> findByReleaseId(long releaseId);
}
