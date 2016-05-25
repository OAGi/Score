package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.BasicCoreComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BasicCoreComponentRepository extends JpaRepository<BasicCoreComponent, Integer> {

    @Query("select b from BasicCoreComponent b where b.fromAccId = ?1")
    public List<BasicCoreComponent> findByFromAccId(int fromAccId);

    @Query("select b from BasicCoreComponent b where b.den like ?1%")
    public List<BasicCoreComponent> findByDenStartsWith(String den);

    @Query("select b from BasicCoreComponent b where b.guid = ?1 and b.toBccpId = ?2")
    public BasicCoreComponent findOnebyGuidAndToBccpId(String guid, int toBccpId);

    @Query("select b from BasicCoreComponent b where b.guid = ?1 and b.fromAccId = ?2 and b.toBccpId = ?3")
    public BasicCoreComponent findOnebyGuidAndFromAccIdAndToBccpId(String guid, int fromAccId, int toBccpId);
}
