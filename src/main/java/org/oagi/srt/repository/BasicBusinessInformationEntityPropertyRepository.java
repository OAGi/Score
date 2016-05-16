package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.BasicBusinessInformationEntityProperty;

public interface BasicBusinessInformationEntityPropertyRepository {

    public int findGreatestId();

    public BasicBusinessInformationEntityProperty findOneByBbiepId(int bbiepId);

    public void save(BasicBusinessInformationEntityProperty basicBusinessInformationEntityProperty);

    public void update(BasicBusinessInformationEntityProperty basicBusinessInformationEntityProperty);
}
