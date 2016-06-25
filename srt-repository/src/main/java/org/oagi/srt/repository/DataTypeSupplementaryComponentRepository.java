package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.DataTypeSupplementaryComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface DataTypeSupplementaryComponentRepository extends JpaRepository<DataTypeSupplementaryComponent, Integer> {

    @Query("select d from DataTypeSupplementaryComponent d where d.ownerDtId = ?1")
    public List<DataTypeSupplementaryComponent> findByOwnerDtId(int ownerDtId);

    @Query("select d from DataTypeSupplementaryComponent d where d.ownerDtId in ?1")
    public List<DataTypeSupplementaryComponent> findByOwnerDtIdIn(Collection<Integer> ownerDtIds);

    @Query("select d from DataTypeSupplementaryComponent d where d.guid = ?1")
    public DataTypeSupplementaryComponent findOneByGuid(String guid);

    @Query("select d from DataTypeSupplementaryComponent d where d.guid in ?1")
    public List<DataTypeSupplementaryComponent> findByGuidIn(Collection<String> guids);

    @Query("select d from DataTypeSupplementaryComponent d where d.guid = ?1 and d.ownerDtId = ?2")
    public DataTypeSupplementaryComponent findOneByGuidAndOwnerDtId(String guid, int ownerDtId);

    @Query("select d from DataTypeSupplementaryComponent d, DataType dt where d.ownerDtId = dt.dtId and dt.dataTypeTerm = ?1 and d.propertyTerm = ?2")
    public DataTypeSupplementaryComponent findOneByOwnerDataTypeTermAndPropertyTerm(
            String ownerDataTypeTerm, String propertyTerm
    );

    @Query("select d from DataTypeSupplementaryComponent d where d.ownerDtId = ?1 and d.propertyTerm = ?2 and d.representationTerm = ?3")
    public DataTypeSupplementaryComponent findOneByOwnerDtIdAndPropertyTermAndRepresentationTerm(
            int ownerDtId, String propertyTerm, String representationTerm
    );

    @Query("select d from DataTypeSupplementaryComponent d where d.ownerDtId = ?1 and d.basedDtScId = ?2")
    public DataTypeSupplementaryComponent findOneByOwnerDtIdAndBasedDtScId(int ownerDtId, int basedDtScId);
}
