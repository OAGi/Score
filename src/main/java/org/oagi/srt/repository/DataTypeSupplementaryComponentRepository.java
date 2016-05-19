package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.DataTypeSupplementaryComponent;

import java.util.List;

public interface DataTypeSupplementaryComponentRepository {

    public List<DataTypeSupplementaryComponent> findAll();

    public List<DataTypeSupplementaryComponent> findByOwnerDtId(int ownerDtId);

    public DataTypeSupplementaryComponent findOneByGuid(String guid);

    public DataTypeSupplementaryComponent findOneByDtScId(int dtScId);

    public DataTypeSupplementaryComponent findOneByGuidAndOwnerDtId(String guid, int ownerDtId);

    public DataTypeSupplementaryComponent findOneByOwnerDtIdAndPropertyTermAndRepresentationTerm(
            int ownerDtId, String propertyTerm, String representationTerm
    );

    public DataTypeSupplementaryComponent findOneByOwnerDtIdAndBasedDtScId(int ownerDtId, int basedDtScId);

    public void save(DataTypeSupplementaryComponent dataTypeSupplementaryComponent);

    public void update(DataTypeSupplementaryComponent dataTypeSupplementaryComponent);
}
