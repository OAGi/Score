package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.ContextCategory;

import java.util.List;

public interface ContextCategoryRepository {

    public List<ContextCategory> findAll();

    public void save(ContextCategory contextCategory);

    public void update(ContextCategory contextCategory);

    public void deleteByContextCategoryId(int contextCategoryId);
}
