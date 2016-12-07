package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.BusinessContextValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BusinessContextValueRepository extends JpaRepository<BusinessContextValue, Long> {

    @Query("select b from BusinessContextValue b where b.businessContext.bizCtxId = ?1")
    public List<BusinessContextValue> findByBizCtxId(long bizCtxId);

    @Query("select b from BusinessContextValue b where b.contextSchemeValue.ctxSchemeValueId = ?1")
    public List<BusinessContextValue> findByCtxSchemeValueId(long ctxSchemeValueId);

    @Modifying
    @Query("delete from BusinessContextValue b where b.businessContext.bizCtxId = ?1")
    public void deleteByBizCtxId(long bizCtxId);
}
