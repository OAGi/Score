package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.ContextScheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ContextSchemeRepository extends JpaRepository<ContextScheme, Long> {

    @Query("select c from ContextScheme c where c.ctxCategoryId = ?1")
    public List<ContextScheme> findByCtxCategoryId(long ctxCategoryId);
}
