package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.ContextSchemeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ContextSchemeValueRepository extends JpaRepository<ContextSchemeValue, Integer> {

    @Query("select c from ContextSchemeValue c where c.ownerCtxSchemeId = ?1")
    public List<ContextSchemeValue> findByOwnerCtxSchemeId(int ownerCtxSchemeId);

    @Query("delete from ContextSchemeValue c where c.ownerCtxSchemeId = ?1")
    public void deleteByOwnerCtxSchemeId(int ownerCtxSchemeId);
}
