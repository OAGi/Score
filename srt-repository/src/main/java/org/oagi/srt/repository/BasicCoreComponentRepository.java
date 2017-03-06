package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.AssociationCoreComponent;
import org.oagi.srt.repository.entity.BasicCoreComponent;
import org.oagi.srt.repository.entity.CoreComponentState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface BasicCoreComponentRepository extends JpaRepository<BasicCoreComponent, Long> {

    @Query("select b from BasicCoreComponent b where b.revisionNum = ?1")
    public List<BasicCoreComponent> findAllWithRevisionNum(int revisionNum);

    @Query("select b from BasicCoreComponent b where b.fromAccId = ?1")
    public List<BasicCoreComponent> findByFromAccId(long fromAccId);

    @Query("select b from BasicCoreComponent b where b.fromAccId = ?1 and b.revisionNum = ?2")
    public List<BasicCoreComponent> findByFromAccIdAndRevisionNum(long fromAccId, int revisionNum);

    @Query("select count(b) from BasicCoreComponent b where b.fromAccId = ?1 and b.revisionNum = ?2")
    public int countByFromAccIdAndRevisionNum(long fromAccId, int revisionNum);

    @Query("select b from BasicCoreComponent b where b.fromAccId = ?1 and b.revisionNum = ?2 and b.state = ?3")
    public List<BasicCoreComponent> findByFromAccIdAndRevisionNumAndState(long fromAccId, int revisionNum, CoreComponentState state);

    @Query("select count(b) from BasicCoreComponent b where b.fromAccId = ?1 and b.revisionNum = ?2 and b.state = ?3")
    public int countByFromAccIdAndRevisionNumAndState(long fromAccId, int revisionNum, CoreComponentState state);

    @Query("select b from BasicCoreComponent b where b.fromAccId = ?1 and b.revisionNum = ?2 and b.seqKey != 0")
    public List<BasicCoreComponent> findByFromAccIdAndRevisionNumAndSeqKeyIsNotZero(long fromAccId, int revisionNum);

    @Query("select b from BasicCoreComponent b where b.toBccpId = ?1 and b.entityType = ?2")
    public List<BasicCoreComponent> findByToBccpIdAndEntityType(long toBccpId, int entityType);

    @Query("select b from BasicCoreComponent b where b.den like ?1%")
    public List<BasicCoreComponent> findByDenStartsWith(String den);

    @Query("select b from BasicCoreComponent b where b.fromAccId = ?1 and b.toBccpId = ?2")
    public List<BasicCoreComponent> findByFromAccIdAndToBccpId(long fromAccId, long toBccpId);

    @Query("select b from BasicCoreComponent b where b.guid = ?1 and b.fromAccId = ?2 and b.toBccpId = ?3")
    public BasicCoreComponent findOneByGuidAndFromAccIdAndToBccpId(String guid, long fromAccId, long toBccpId);

    @Query("select case when count(b) > 0 then true else false end from BasicCoreComponent b where b.guid = ?1 and b.toBccpId = ?2")
    public boolean existsByGuidAndToBccpId(String guid, long toBccpId);

    @Query("select case when count(b) > 0 then true else false end from BasicCoreComponent b where b.guid = ?1 and b.fromAccId = ?2 and b.toBccpId = ?3")
    public boolean existsByGuidAndFromAccIdAndToBccpId(String guid, long fromAccId, long toBccpId);

    @Query("select count(a) from BasicCoreComponent a where a.fromAccId = ?1")
    public int countByFromAccId(long fromAccId);

    @Query("select b from BasicCoreComponent b where b.currentBccId = ?1 and b.revisionNum = (" +
            "select MAX(b.revisionNum) from BasicCoreComponent b where b.currentBccId = ?1 group by b.currentBccId)")
    public List<BasicCoreComponent> findAllWithLatestRevisionNumByCurrentBccId(long currentBccId);

    @Query("select b from BasicCoreComponent b where b.currentBccId = ?1 and b.revisionNum = (" +
            "select MAX(b.revisionNum) from BasicCoreComponent b where b.currentBccId = ?1 and b.seqKey > 0 group by b.currentBccId)")
    public List<BasicCoreComponent> findAllWithLatestRevisionNumByCurrentBccIdAndSeqKeyIsNotZero(long currentBccId);

    @Modifying
    @Query("update BasicCoreComponent b set b.seqKey = b.seqKey + 1 " +
            "where b.fromAccId = ?1 and b.seqKey > ?2 and b.revisionNum = 0")
    public void increaseSeqKeyByFromAccIdAndSeqKeyGreaterThan(long fromAccId, int seqKey);

    @Modifying
    @Query("update BasicCoreComponent b set b.seqKey = b.seqKey - 1 " +
            "where b.fromAccId = ?1 and b.seqKey > ?2 and b.revisionNum = 0")
    public void decreaseSeqKeyByFromAccIdAndSeqKeyGreaterThan(long fromAccId, int seqKey);

    @Modifying
    @Query("delete from BasicCoreComponent b where b.currentBccId = ?1")
    public void deleteByCurrentBccId(long currentBccId);

    @Query("select b from BasicCoreComponent b where b.revisionNum = ?1 order by b.creationTimestamp desc")
    public List<BasicCoreComponent> findAllByRevisionNum(int revisionNum);

    @Query("select b from BasicCoreComponent b where b.revisionNum = ?1 and b.state in ?2 order by b.creationTimestamp desc")
    public List<BasicCoreComponent> findAllByRevisionNumAndStates(int revisionNum, Collection<CoreComponentState> states);

    @Query("select b from BasicCoreComponent b where b.toBccpId = ?1 and b.revisionNum = ?2")
    public List<BasicCoreComponent> findByToBccpIdAndRevisionNum(long toBccpId, int revisionNum);
}
