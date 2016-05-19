package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.BasicBusinessInformationEntity;

import java.util.List;

public interface BasicBusinessInformationEntityRepository {

    public int findGreatestId();

    public List<BasicBusinessInformationEntity> findByFromAbieId(int fromAbieId);

    public List<BasicBusinessInformationEntity> findByFromAbieIdAndUsed(int fromAbieId, boolean used);

    public void save(BasicBusinessInformationEntity basicBusinessInformationEntity);

    public void update(BasicBusinessInformationEntity basicBusinessInformationEntity);
}
