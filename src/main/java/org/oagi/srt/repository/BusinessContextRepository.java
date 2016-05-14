package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.BusinessContext;

public interface BusinessContextRepository {

    public BusinessContext findOneByBusinessContextId(int businessContextId);
}
