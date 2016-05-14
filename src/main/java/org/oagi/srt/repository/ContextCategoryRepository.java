package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.ContextCategory;

import java.util.List;

public interface ContextCategoryRepository {

    public List<ContextCategory> findAll();
}
