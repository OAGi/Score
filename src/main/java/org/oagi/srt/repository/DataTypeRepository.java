package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.DataType;

import java.util.List;

public interface DataTypeRepository {

    public List<DataType> findByType(int type);

    public List<DataType> findByDataTypeTerm(String dataTypeTerm);

    public DataType findOneByDtId(int dtId);

    public DataType findOneByDataTypeTermAndType(String dataTypeTerm, int type);

    public DataType findOneByGuid(String guid);

    public DataType findOneByGuidAndType(String guid, int type);

    public DataType findOneByDen(String den);

    public DataType findOneByTypeAndDen(int type, String den);

    public DataType findOneByGuidAndDen(String guid, String den);

    public void save(DataType dataType);
}
