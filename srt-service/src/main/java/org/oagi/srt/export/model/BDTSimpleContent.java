package org.oagi.srt.export.model;

import org.oagi.srt.common.util.Utility;
import org.oagi.srt.repository.entity.DataType;
import org.oagi.srt.repository.entity.DataTypeSupplementaryComponent;

import java.util.List;

public class BDTSimpleContent implements BDTSimple {

    private DataType dataType;

    private DataType baseDataType;

    private List<DataTypeSupplementaryComponent> dtScList;

    private List<DataTypeSupplementaryComponent> baseDtScList;

    public BDTSimpleContent(DataType dataType, DataType baseDataType,
                            List<DataTypeSupplementaryComponent> dtScList,
                            List<DataTypeSupplementaryComponent> baseDtScList) {
        this.dataType = dataType;
        this.baseDataType = baseDataType;
        this.dtScList = dtScList;
        this.baseDtScList = baseDtScList;
    }

    @Override
    public int getBdtId() {
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

    public List<DataTypeSupplementaryComponent> getDtScList() {
        return dtScList;
    }

    public List<DataTypeSupplementaryComponent> getBaseDtScList() {
        return baseDtScList;
    }
}
