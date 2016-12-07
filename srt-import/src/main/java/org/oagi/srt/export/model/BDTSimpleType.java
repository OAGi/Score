package org.oagi.srt.export.model;

import org.oagi.srt.common.util.Utility;
import org.oagi.srt.repository.entity.DataType;

public class BDTSimpleType implements BDTSimple {

    private DataType dataType;

    private DataType baseDataType;

    public BDTSimpleType(DataType dataType, DataType baseDataType) {
        this.dataType = dataType;
        this.baseDataType = baseDataType;
    }

    @Override
    public long getBdtId() {
        return dataType.getDtId();
    }

    public String getName() {
        return Utility.denToName(dataType.getDen());
    }

    public String getGuid() {
        return dataType.getGuid();
    }

    public String getBaseDTName() {
        return Utility.denToName(baseDataType.getDen());
    }
}
