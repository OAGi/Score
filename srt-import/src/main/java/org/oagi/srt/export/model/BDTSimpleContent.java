package org.oagi.srt.export.model;

import org.oagi.srt.common.util.Utility;
import org.oagi.srt.provider.ImportedDataProvider;
import org.oagi.srt.repository.entity.BusinessDataTypePrimitiveRestriction;
import org.oagi.srt.repository.entity.CoreDataTypeAllowedPrimitiveExpressionTypeMap;
import org.oagi.srt.repository.entity.DataType;
import org.oagi.srt.repository.entity.DataTypeSupplementaryComponent;

import java.util.List;
import java.util.stream.Collectors;

public class BDTSimpleContent implements BDTSimple {

    private DataType dataType;

    private DataType baseDataType;

    private boolean isDefaultBDT;

    private List<BDTSC> dtScList;

    private ImportedDataProvider importedDataProvider;

    public BDTSimpleContent(DataType dataType, DataType baseDataType, boolean isDefaultBDT,
                            List<DataTypeSupplementaryComponent> dtScList,
                            ImportedDataProvider importedDataProvider) {
        this.dataType = dataType;
        this.baseDataType = baseDataType;
        this.isDefaultBDT = isDefaultBDT;
        this.importedDataProvider = importedDataProvider;
        this.dtScList = map(dtScList);
    }

    private List<BDTSC> map(List<DataTypeSupplementaryComponent> dtScList) {
        return dtScList.stream()
                .map(dtSc -> new BDTSC(dtSc, importedDataProvider))
                .collect(Collectors.toList());
    }

    @Override
    public long getBdtId() {
        return dataType.getDtId();
    }

    @Override
    public boolean isDefaultBDT() {
        return isDefaultBDT;
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

    public String getXbtName() {
        List<BusinessDataTypePrimitiveRestriction> bdtPriRestriList =
                importedDataProvider.findBdtPriRestriListByDtId(getBdtId());

        List<BusinessDataTypePrimitiveRestriction> defaultBdtPriRestri = bdtPriRestriList.stream()
                .filter(e -> e.isDefault())
                .collect(Collectors.toList());
        if (defaultBdtPriRestri.size() != 1) {
            throw new IllegalStateException();
        }
        CoreDataTypeAllowedPrimitiveExpressionTypeMap cdtAwdPriXpsTypeMap =
                importedDataProvider.findCdtAwdPriXpsTypeMapById(
                        defaultBdtPriRestri.get(0).getCdtAwdPriXpsTypeMapId()
                );
        return importedDataProvider.findXbt(cdtAwdPriXpsTypeMap.getXbtId()).getBuiltInType();
    }

    public List<BDTSC> getDtScList() {
        return dtScList;
    }
}
