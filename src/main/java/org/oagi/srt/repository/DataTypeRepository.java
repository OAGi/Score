package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.DataType;

public interface DataTypeRepository {

    public DataType findOneByDtId(int dtId);
}
