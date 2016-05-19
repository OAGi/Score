package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.BasicCoreComponent;

import java.util.List;

public interface BasicCoreComponentRepository {

    public List<BasicCoreComponent> findByFromAccId(int fromAccId);

    public List<BasicCoreComponent> findByDenStartsWith(String den);

    public BasicCoreComponent findOneByBccId(int bccId);

    public BasicCoreComponent findOnebyGuidAndToBccpId(String guid, int toBccpId);

    public BasicCoreComponent findOnebyGuidAndFromAccIdAndToBccpId(String guid, int fromAccId, int toBccpId);

    public void save(BasicCoreComponent basicCoreComponent);

    public void update(BasicCoreComponent basicCoreComponent);
}
