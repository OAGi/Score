package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.AssociationCoreComponent;
import org.oagi.srt.repository.entity.AssociationCoreComponentProperty;
import org.oagi.srt.repository.entity.CoreComponentState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AssociationCoreComponentRepository extends JpaRepository<AssociationCoreComponent, Long> {

    @Query("select a from AssociationCoreComponent a where a.revisionNum = ?1")
    public List<AssociationCoreComponent> findAllWithRevisionNum(int revisionNum);

    @Query("select a from AssociationCoreComponent a where a.fromAccId = ?1")
    public List<AssociationCoreComponent> findByFromAccId(long fromAccId);

    @Query("select a from AssociationCoreComponent a where a.fromAccId = ?1 and a.revisionNum = ?2")
    public List<AssociationCoreComponent> findByFromAccIdAndRevisionNum(long fromAccId, int revisionNum);

    @Query("select a from AssociationCoreComponent a where a.fromAccId = ?1 and a.revisionNum = ?2 and a.state = ?3")
    public List<AssociationCoreComponent> findByFromAccIdAndRevisionNumAndState(long fromAccId, int revisionNum, CoreComponentState state);

    @Query("select a from AssociationCoreComponent a where a.toAsccpId = ?1 and a.revisionNum = ?2 and a.state = ?3")
    public List<AssociationCoreComponent> findByToAsccpIdAndRevisionNumAndState(long toAsccpId, int revisionNum, CoreComponentState state);

    @Query("select a from AssociationCoreComponent a where a.definition = ?1")
    public List<AssociationCoreComponent> findByDefinition(String definition);

    @Query("select a from AssociationCoreComponent a where a.den like ?1%")
    public List<AssociationCoreComponent> findByDenStartsWith(String den);

    @Query("select a from AssociationCoreComponent a where a.den like %?1%")
    public List<AssociationCoreComponent> findByDenContaining(String den);

    @Query("select a from AssociationCoreComponent a where a.fromAccId = ?1 and a.toAsccpId = ?2")
    public List<AssociationCoreComponent> findByFromAccIdAndToAsccpId(long fromAccId, long toAsccpId);

    @Query("select a from AssociationCoreComponent a where a.guid = ?1 and a.fromAccId = ?2 and a.toAsccpId = ?3")
    public AssociationCoreComponent findOneByGuidAndFromAccIdAndToAsccpId(String guid, long fromAccId, long toAsccpId);

    @Query("select case when count(a) > 0 then true else false end from AssociationCoreComponent a where a.guid = ?1 and a.fromAccId = ?2 and a.toAsccpId = ?3")
    public boolean existsByGuidAndFromAccIdAndToAsccpId(String guid, long fromAccId, long toAsccpId);

    @Query("select count(a) from AssociationCoreComponent a where a.fromAccId = ?1")
    public int countByFromAccId(long fromAccId);

    @Query("select a from AssociationCoreComponent a where a.currentAsccId = ?1 and a.revisionTrackingNum = (" +
            "select MAX(a.revisionTrackingNum) from AssociationCoreComponent a where a.currentAsccId = ?1 group by a.currentAsccId)")
    public AssociationCoreComponent findLatestOneByCurrentAsccId(long currentAsccId);

    @Modifying
    @Query("update AssociationCoreComponent a set a.seqKey = a.seqKey + 1 " +
            "where a.fromAccId = ?1 and a.seqKey > ?2 and a.revisionNum = 0")
    public void increaseSeqKeyByFromAccIdAndSeqKey(long fromAccId, int seqKey);

    @Query("select a from AssociationCoreComponent a where a.toAsccpId = ?1 and a.revisionNum = ?2")
    public List<AssociationCoreComponent> findByToAsccpIdAndRevisionNum(long toAsccpId, int revisionNum);
}
