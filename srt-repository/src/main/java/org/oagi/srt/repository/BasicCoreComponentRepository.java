package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.BasicCoreComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BasicCoreComponentRepository extends JpaRepository<BasicCoreComponent, Long> {

    @Query("select b from BasicCoreComponent b where b.revisionNum = ?1")
    public List<BasicCoreComponent> findAllWithRevisionNum(int revisionNum);

    @Query("select b from BasicCoreComponent b where b.fromAccId = ?1")
    public List<BasicCoreComponent> findByFromAccId(long fromAccId);

    @Query("select b from BasicCoreComponent b where b.fromAccId = ?1 and b.seqKey != 0")
    public List<BasicCoreComponent> findByFromAccIdAndSeqKeyIsNotZero(long fromAccId);

    @Query("select b from BasicCoreComponent b where b.toBccpId = ?1 and b.entityType = ?2")
    public List<BasicCoreComponent> findByToBccpIdAndEntityType(long toBccpId, int entityType);

    @Query("select b from BasicCoreComponent b where b.den like ?1%")
    public List<BasicCoreComponent> findByDenStartsWith(String den);

    @Query("select b from BasicCoreComponent b where b.guid = ?1 and b.fromAccId = ?2 and b.toBccpId = ?3")
    public BasicCoreComponent findOneByGuidAndFromAccIdAndToBccpId(String guid, long fromAccId, long toBccpId);

    @Query("select case when count(b) > 0 then true else false end from BasicCoreComponent b where b.guid = ?1 and b.toBccpId = ?2")
    public boolean existsByGuidAndToBccpId(String guid, long toBccpId);

    @Query("select case when count(b) > 0 then true else false end from BasicCoreComponent b where b.guid = ?1 and b.fromAccId = ?2 and b.toBccpId = ?3")
    public boolean existsByGuidAndFromAccIdAndToBccpId(String guid, long fromAccId, long toBccpId);

    @Query("select count(a) from BasicCoreComponent a where a.fromAccId = ?1")
    public int countByFromAccId(long fromAccId);
}
