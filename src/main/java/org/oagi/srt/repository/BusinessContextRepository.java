package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.BusinessContext;

import java.util.List;

public interface BusinessContextRepository {

    public List<BusinessContext> findAll();

    public BusinessContext findOneByBusinessContextId(int businessContextId);

    public void save(BusinessContext businessContext);
}
