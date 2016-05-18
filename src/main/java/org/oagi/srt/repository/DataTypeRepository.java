package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.DataType;

public interface DataTypeRepository {

    public DataType findOneByDtId(int dtId);

    public DataType findOneByDataTypeTermAndType(String dataTypeTerm, int type);

    public DataType findOneByGuid(String guid);

    public DataType findOneByDen(String den);

    public void save(DataType dataType);
}
