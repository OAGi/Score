package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.ContextSchemeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ContextSchemeValueRepository extends JpaRepository<ContextSchemeValue, Long> {

    @Query("select c from ContextSchemeValue c where c.contextScheme.ctxSchemeId = ?1 order by c.value asc")
    public List<ContextSchemeValue> findByOwnerCtxSchemeId(long ownerCtxSchemeId);

    @Modifying
    @Query("delete from ContextSchemeValue c where c.contextScheme.ctxSchemeId = ?1")
    public void deleteByOwnerCtxSchemeId(long ownerCtxSchemeId);
}
