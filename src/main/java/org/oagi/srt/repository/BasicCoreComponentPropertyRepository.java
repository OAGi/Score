package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.BasicCoreComponentProperty;

public interface BasicCoreComponentPropertyRepository {

    public BasicCoreComponentProperty findOneByBccpId(int bccpId);

    public BasicCoreComponentProperty findOneByBccpIdAndRevisionNum(int bccpId, int revisionNum);
}
