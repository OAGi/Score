package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.AggregateCoreComponent;

public interface AggregateCoreComponentRepository {

    public AggregateCoreComponent findOneByAccId(int accId);

    public AggregateCoreComponent findOneByGuid(String guid);

    public AggregateCoreComponent findOneByAccIdAndRevisionNum(int accId, int revisionNum);

    public void save(AggregateCoreComponent aggregateCoreComponent);
}
