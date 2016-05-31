package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.DataType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface DataTypeRepository extends JpaRepository<DataType, Integer> {

    @Query("select count(d) from DataType d where d.type = ?1")
    public long countByType(int type);

    @Query("select d from DataType d where d.type = ?1")
    public List<DataType> findByType(int type);

    @Query("select d from DataType d where d.dataTypeTerm = ?1")
    public List<DataType> findByDataTypeTerm(String dataTypeTerm);

    @Query("select d from DataType d where d.guid in ?1")
    public List<DataType> findByGuidIn(Collection<String> guids);

    @Query("select d from DataType d where d.dataTypeTerm = ?1 and d.type = ?2")
    public DataType findOneByDataTypeTermAndType(String dataTypeTerm, int type);

    @Query("select d from DataType d where d.guid = ?1")
    public DataType findOneByGuid(String guid);

    @Query("select d from DataType d where d.guid = ?1 and d.type = ?2")
    public DataType findOneByGuidAndType(String guid, int type);

    @Query("select d from DataType d where d.den = ?1")
    public List<DataType> findByDen(String den);

    @Query("select d from DataType d where d.type = ?1 and d.den = ?2")
    public DataType findOneByTypeAndDen(int type, String den);

    @Query("select d from DataType d where d.guid = ?1 and d.den = ?2")
    public DataType findOneByGuidAndDen(String guid, String den);
}
