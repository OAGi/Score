package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.BasicCoreComponentProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BasicCoreComponentPropertyRepository extends JpaRepository<BasicCoreComponentProperty, Integer> {

    @Query("select new BasicCoreComponentProperty(b.bccpId, b.den) from BasicCoreComponentProperty b where b.propertyTerm = ?1 and b.bdtId = ?2")
    public BasicCoreComponentProperty findBccpIdAndDenByPropertyTermAndBdtId(String propertyTerm, int bdtId);

    @Query("select b from BasicCoreComponentProperty b where b.guid = ?1")
    public BasicCoreComponentProperty findOneByGuid(String guid);

    @Query("select new BasicCoreComponentProperty(b.bccpId, b.den) from BasicCoreComponentProperty b where b.guid = ?1")
    public BasicCoreComponentProperty findBccpIdAndDenByGuid(String guid);
}
