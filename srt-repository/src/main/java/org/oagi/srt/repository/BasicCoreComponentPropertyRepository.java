package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.BasicCoreComponent;
import org.oagi.srt.repository.entity.BasicCoreComponentProperty;
import org.oagi.srt.repository.entity.CoreComponentState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface BasicCoreComponentPropertyRepository extends JpaRepository<BasicCoreComponentProperty, Long> {

    @Query("select a from BasicCoreComponentProperty a where a.revisionNum = ?1")
    public List<BasicCoreComponentProperty> findAllWithRevisionNum(int revisionNum);

    @Query("select a from BasicCoreComponentProperty a where a.bccpId = ?1 and a.revisionNum = ?2 and a.state = ?3")
    public BasicCoreComponentProperty findOneByBccpIdAndRevisionNumAndState(long bccpId, int revisionNum, CoreComponentState state);

    @Query("select new BasicCoreComponentProperty(b.bccpId, b.den) from BasicCoreComponentProperty b where b.propertyTerm = ?1 and b.bdtId = ?2")
    public BasicCoreComponentProperty findBccpIdAndDenByPropertyTermAndBdtId(String propertyTerm, long bdtId);

    @Query("select b from BasicCoreComponentProperty b where b.guid = ?1")
    public BasicCoreComponentProperty findOneByGuid(String guid);

    @Query("select case when count(b) > 0 then true else false end from BasicCoreComponentProperty b where b.guid = ?1")
    public boolean existsByGuid(String guid);

    @Query("select new BasicCoreComponentProperty(b.bccpId, b.den) from BasicCoreComponentProperty b where b.guid = ?1")
    public BasicCoreComponentProperty findBccpIdAndDenByGuid(String guid);

    @Query("select b from BasicCoreComponentProperty b where b.revisionNum = ?1 order by b.creationTimestamp desc")
    public List<BasicCoreComponentProperty> findAllByRevisionNum(int revisionNum);

    @Query("select b from BasicCoreComponentProperty b where b.revisionNum = ?1 and b.state in ?2 order by b.creationTimestamp desc")
    public List<BasicCoreComponentProperty> findAllByRevisionNumAndStates(int revisionNum, Collection<CoreComponentState> states);
}
