package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.AggregateBusinessInformationEntity;

import java.util.List;

public interface AggregateBusinessInformationEntityRepository {

    public List<AggregateBusinessInformationEntity> findByTopLevel(boolean topLevel);

    public AggregateBusinessInformationEntity findOneByAbieId(int abieId);

    public void save(AggregateBusinessInformationEntity aggregateBusinessInformationEntity);

    public void update(AggregateBusinessInformationEntity aggregateBusinessInformationEntity);
}
