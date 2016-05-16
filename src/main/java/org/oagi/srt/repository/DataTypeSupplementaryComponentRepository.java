package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.DataTypeSupplementaryComponent;

import java.util.List;

public interface DataTypeSupplementaryComponentRepository {

    public List<DataTypeSupplementaryComponent> findByOwnerDtId(int ownerDtId);

    public DataTypeSupplementaryComponent findOneByDtScId(int dtScId);
}
