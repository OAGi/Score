package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.ContextScheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ContextSchemeRepository extends JpaRepository<ContextScheme, Long> {

    @Query("select c from ContextScheme c where c.contextCategory.ctxCategoryId = ?1")
    public List<ContextScheme> findByCtxCategoryId(long ctxCategoryId);

    @Query("select c from ContextScheme c where c.schemeId = ?1 and c.schemeAgencyId = ?2")
    public List<ContextScheme> findBySchemeIdAndSchemeAgencyId(String schemeId, String schemeAgencyId);

    @Query("select c from ContextScheme c where c.schemeName = ?1 and c.schemeAgencyId = ?2")
    public List<ContextScheme> findBySchemeNameAndSchemeAgencyId(String schemeName, String schemeAgencyId);
}
