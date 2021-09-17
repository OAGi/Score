package org.oagi.score.export.model;

import org.oagi.score.provider.ImportedDataProvider;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.BdtPriRestriRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.CdtAwdPriRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.CdtAwdPriXpsTypeMapRecord;

import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractBDTSimple implements BDTSimple {

    private ImportedDataProvider importedDataProvider;

    public AbstractBDTSimple(ImportedDataProvider importedDataProvider) {
        this.importedDataProvider = importedDataProvider;
    }

    private BdtPriRestriRecord getDefaultBdtPriRestri() {
        List<BdtPriRestriRecord> bdtPriRestriList =
                importedDataProvider.findBdtPriRestriListByDtId(getBdtId());

        List<BdtPriRestriRecord> defaultBdtPriRestri = bdtPriRestriList.stream()
                .filter(e -> e.getIsDefault() == 1)
                .collect(Collectors.toList());
        if (defaultBdtPriRestri.size() != 1) {
            throw new IllegalStateException();
        }

        return defaultBdtPriRestri.get(0);
    }

    private CdtAwdPriXpsTypeMapRecord getDefaultCdtAwdPriTypeMap() {
        BdtPriRestriRecord defaultBdtPriRestri = getDefaultBdtPriRestri();
        return importedDataProvider.findCdtAwdPriXpsTypeMapById(
                defaultBdtPriRestri.getCdtAwdPriXpsTypeMapId()
        );
    }

    public CdtAwdPriRecord getDefaultCdtAwdPri() {
        CdtAwdPriXpsTypeMapRecord cdtAwdPriXpsTypeMap = getDefaultCdtAwdPriTypeMap();
        return importedDataProvider.findCdtAwdPri(cdtAwdPriXpsTypeMap.getCdtAwdPriId());
    }

    public String getCdtPriName() {
        CdtAwdPriRecord cdtAwdPri = getDefaultCdtAwdPri();
        return importedDataProvider.findCdtPri(cdtAwdPri.getCdtPriId()).getName();
    }

    public String getXbtName() {
        CdtAwdPriXpsTypeMapRecord cdtAwdPriXpsTypeMap = getDefaultCdtAwdPriTypeMap();
        return importedDataProvider.findXbt(cdtAwdPriXpsTypeMap.getXbtId()).getBuiltinType();
    }

}
