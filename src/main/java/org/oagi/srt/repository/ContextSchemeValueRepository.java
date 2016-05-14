package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.ContextSchemeValue;

import java.util.List;

public interface ContextSchemeValueRepository {

    public List<ContextSchemeValue> findByContextSchemeId(int contextSchemeId);

    public void save(ContextSchemeValue contextSchemeValue);

    public void deleteByContextSchemeId(int contextSchemeId);

    public void deleteByContextSchemeValueId(int contextSchemeValueId);
}
