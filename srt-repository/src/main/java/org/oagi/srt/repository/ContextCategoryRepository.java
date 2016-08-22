package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.ContextCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ContextCategoryRepository extends JpaRepository<ContextCategory, Long> {

    @Query("select c from ContextCategory c where c.name like %?1%")
    public List<ContextCategory> findByNameContaining(String name);
}
