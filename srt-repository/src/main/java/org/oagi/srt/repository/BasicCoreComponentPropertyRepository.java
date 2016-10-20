package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.BasicCoreComponentProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BasicCoreComponentPropertyRepository extends JpaRepository<BasicCoreComponentProperty, Long> {

    @Query("select a from BasicCoreComponentProperty a where a.revisionNum = ?1")
    public List<BasicCoreComponentProperty> findAllWithRevisionNum(int revisionNum);

    @Query("select new BasicCoreComponentProperty(b.bccpId, b.den) from BasicCoreComponentProperty b where b.propertyTerm = ?1 and b.bdtId = ?2")
    public BasicCoreComponentProperty findBccpIdAndDenByPropertyTermAndBdtId(String propertyTerm, long bdtId);

    @Query("select b from BasicCoreComponentProperty b where b.guid = ?1")
    public BasicCoreComponentProperty findOneByGuid(String guid);

    @Query("select case when count(b) > 0 then true else false end from BasicCoreComponentProperty b where b.guid = ?1")
    public boolean existsByGuid(String guid);

    @Query("select new BasicCoreComponentProperty(b.bccpId, b.den) from BasicCoreComponentProperty b where b.guid = ?1")
    public BasicCoreComponentProperty findBccpIdAndDenByGuid(String guid);
}
