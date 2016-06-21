package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.BusinessContextValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BusinessContextValueRepository extends JpaRepository<BusinessContextValue, Integer> {

    @Query("select b from BusinessContextValue b where b.ctxSchemeValueId = ?1")
    public List<BusinessContextValue> findByCtxSchemeValueId(int ctxSchemeValueId);

    @Query("select b from BusinessContextValue b where b.bizCtxId = ?1")
    public List<BusinessContextValue> findByBizCtxId(int bizCtxId);
}
