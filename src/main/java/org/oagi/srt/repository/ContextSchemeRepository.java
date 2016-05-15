package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.ContextScheme;

import java.util.List;

public interface ContextSchemeRepository {

    public List<ContextScheme> findAll();

    public List<ContextScheme> findByContextCategoryId(int contextCategoryId);

    public void update(ContextScheme contextScheme);

    public void save(ContextScheme contextScheme);

    public void deleteByContextSchemeId(int contextSchemeId);
}
