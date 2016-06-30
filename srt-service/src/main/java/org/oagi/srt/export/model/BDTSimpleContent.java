package org.oagi.srt.export.model;

import org.oagi.srt.common.util.Utility;
import org.oagi.srt.provider.ImportedDataProvider;
import org.oagi.srt.repository.entity.DataType;
import org.oagi.srt.repository.entity.DataTypeSupplementaryComponent;

import java.util.List;
import java.util.stream.Collectors;

public class BDTSimpleContent implements BDTSimple {

    private DataType dataType;

    private DataType baseDataType;

    private List<BDTSC> dtScList;

    private ImportedDataProvider importedDataProvider;

    public BDTSimpleContent(DataType dataType, DataType baseDataType,
                            List<DataTypeSupplementaryComponent> dtScList,
                            ImportedDataProvider importedDataProvider) {
        this.importedDataProvider = importedDataProvider;
        this.dataType = dataType;
        this.baseDataType = baseDataType;
        this.dtScList = map(dtScList);
    }

    private List<BDTSC> map(List<DataTypeSupplementaryComponent> dtScList) {
        return dtScList.stream()
                .map(dtSc -> new BDTSC(dtSc, importedDataProvider))
                .collect(Collectors.toList());
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

    public List<BDTSC> getDtScList() {
        return dtScList;
    }
}
