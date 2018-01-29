package org.oagi.srt.export.model;

import org.oagi.srt.provider.ImportedDataProvider;
import org.oagi.srt.repository.entity.BusinessDataTypePrimitiveRestriction;
import org.oagi.srt.repository.entity.CoreDataTypeAllowedPrimitive;
import org.oagi.srt.repository.entity.CoreDataTypeAllowedPrimitiveExpressionTypeMap;

import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractBDTSimple implements BDTSimple {

    private ImportedDataProvider importedDataProvider;

    public AbstractBDTSimple(ImportedDataProvider importedDataProvider) {
        this.importedDataProvider = importedDataProvider;
    }

    private BusinessDataTypePrimitiveRestriction getDefaultBdtPriRestri() {
        List<BusinessDataTypePrimitiveRestriction> bdtPriRestriList =
                importedDataProvider.findBdtPriRestriListByDtId(getBdtId());

        List<BusinessDataTypePrimitiveRestriction> defaultBdtPriRestri = bdtPriRestriList.stream()
                .filter(e -> e.isDefault())
                .collect(Collectors.toList());
        if (defaultBdtPriRestri.size() != 1) {
            throw new IllegalStateException();
        }

        return defaultBdtPriRestri.get(0);
    }

    private CoreDataTypeAllowedPrimitiveExpressionTypeMap getDefaultCdtAwdPriTypeMap() {
        BusinessDataTypePrimitiveRestriction defaultBdtPriRestri = getDefaultBdtPriRestri();
        return importedDataProvider.findCdtAwdPriXpsTypeMapById(
                defaultBdtPriRestri.getCdtAwdPriXpsTypeMapId()
        );
    }

    public CoreDataTypeAllowedPrimitive getDefaultCdtAwdPri() {
        CoreDataTypeAllowedPrimitiveExpressionTypeMap cdtAwdPriXpsTypeMap = getDefaultCdtAwdPriTypeMap();
        return importedDataProvider.findCdtAwdPri(cdtAwdPriXpsTypeMap.getCdtAwdPriId());
    }

    public String getCdtPriName() {
        CoreDataTypeAllowedPrimitive cdtAwdPri = getDefaultCdtAwdPri();
        return importedDataProvider.findCdtPri(cdtAwdPri.getCdtPriId()).getName();
    }

    public String getXbtName() {
        CoreDataTypeAllowedPrimitiveExpressionTypeMap cdtAwdPriXpsTypeMap = getDefaultCdtAwdPriTypeMap();
        return importedDataProvider.findXbt(cdtAwdPriXpsTypeMap.getXbtId()).getBuiltInType();
    }

}
