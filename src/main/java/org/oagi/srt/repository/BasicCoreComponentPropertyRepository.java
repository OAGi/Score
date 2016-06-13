package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.BasicCoreComponentProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BasicCoreComponentPropertyRepository extends JpaRepository<BasicCoreComponentProperty, Integer> {

    @Query("select b from BasicCoreComponentProperty b where b.bccpId = ?1 and b.revisionNum = ?2")
    public BasicCoreComponentProperty findOneByBccpIdAndRevisionNum(int bccpId, int revisionNum);

    @Query("select b from BasicCoreComponentProperty b where b.propertyTerm = ?1")
    public BasicCoreComponentProperty findOneByPropertyTerm(String propertyTerm);

    @Query("select b from BasicCoreComponentProperty b where b.guid = ?1")
    public BasicCoreComponentProperty findOneByGuid(String guid);
}
