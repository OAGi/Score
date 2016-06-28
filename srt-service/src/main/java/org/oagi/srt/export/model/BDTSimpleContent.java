package org.oagi.srt.export.model;

import org.oagi.srt.common.util.Utility;
import org.oagi.srt.repository.entity.DataType;
import org.oagi.srt.repository.entity.DataTypeSupplementaryComponent;

import java.util.List;

public class BDTSimpleContent implements BDTSimple {

    private DataType dataType;

    private DataType baseDataType;

    private List<DataTypeSupplementaryComponent> dtScList;

    public BDTSimpleContent(DataType dataType, DataType baseDataType, List<DataTypeSupplementaryComponent> dtScList) {
        this.dataType = dataType;
        this.baseDataType = baseDataType;
        this.dtScList = dtScList;
    }

    public String getName() {
        return denToName(dataType.getDen());
    }

    public String getGuid() {
        return dataType.getGuid();
    }

    public String getBaseDTName() {
        return denToName(baseDataType.getDen());
    }

    private String denToName(String den) {
        String name = Utility.denToTypeName(den);
        if (name.contains("Type_ ")) {
            name = name.replace("Type_ ", "") + "Type";
        }
        return name.replaceAll(" ", "").replaceAll("_", "");
    }
}
