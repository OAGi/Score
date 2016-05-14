package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.BusinessContextValue;

import java.util.List;

public interface BusinessContextValueRepository {

    public List<BusinessContextValue> findByContextSchemeValueId(int contextSchemeValueId);
}
