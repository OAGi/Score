package org.oagi.srt.export.model;

import org.oagi.srt.common.util.Utility;
import org.oagi.srt.provider.ImportedDataProvider;
import org.oagi.srt.repository.entity.BusinessDataTypePrimitiveRestriction;
import org.oagi.srt.repository.entity.DataType;
import org.oagi.srt.repository.entity.DataTypeType;
import org.oagi.srt.repository.entity.XSDBuiltInType;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BDTSimpleType extends AbstractBDTSimple {

    private DataType dataType;

    private DataType baseDataType;

    private boolean isDefaultBDT;

    private List<BusinessDataTypePrimitiveRestriction> bdtPriRestriList;

    private List<XSDBuiltInType> xbtList;

    private ImportedDataProvider importedDataProvider;

    public BDTSimpleType(DataType dataType, DataType baseDataType, boolean isDefaultBDT, ImportedDataProvider importedDataProvider) {
        this(dataType, baseDataType, isDefaultBDT, null, null, importedDataProvider);
    }

    public BDTSimpleType(DataType dataType, DataType baseDataType, boolean isDefaultBDT,
                         List<BusinessDataTypePrimitiveRestriction> bdtPriRestriList,
                         List<XSDBuiltInType> xbtList,
                         ImportedDataProvider importedDataProvider) {
        super(importedDataProvider);

        this.dataType = dataType;
        this.baseDataType = baseDataType;
        this.isDefaultBDT = isDefaultBDT;
        this.bdtPriRestriList = (bdtPriRestriList != null) ? bdtPriRestriList : Collections.emptyList();
        this.xbtList = (xbtList != null) ? xbtList : Collections.emptyList();
        this.importedDataProvider = importedDataProvider;
    }

    @Override
    public long getBdtId() {
        return dataType.getDtId();
    }

    @Override
    public boolean isDefaultBDT() {
        return isDefaultBDT;
    }

    @Override
    public DataType getDataType() {
        return dataType;
    }

    @Override
    public DataType getBaseDataType() {
        return baseDataType;
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

    public boolean isTimepointCDT() {
        String dataTypeTerm = dataType.getDataTypeTerm();
        return (dataTypeTerm.contains("Date") || dataTypeTerm.contains("Time") || dataTypeTerm.contains("Duration"));
    }

    public boolean isBaseDT_CDT() {
        return DataTypeType.CoreDataType == baseDataType.getType();
    }

    public int count_BDT_PRI_RESTRI() {
        return bdtPriRestriList.size();
    }

    public List<String> getXbtBuiltInTypes() {
        return xbtList.stream()
                .map(e -> e.getBuiltInType())
                .collect(Collectors.toList());
    }
}
