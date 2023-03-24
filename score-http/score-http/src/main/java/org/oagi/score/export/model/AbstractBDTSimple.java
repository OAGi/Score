package org.oagi.score.export.model;

import org.oagi.score.repo.api.impl.jooq.entity.tables.records.BdtPriRestriRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.CdtAwdPriRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.CdtAwdPriXpsTypeMapRecord;
import org.oagi.score.repository.provider.DataProvider;

import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractBDTSimple implements BDTSimple {

    private DataProvider dataProvider;

    public AbstractBDTSimple(DataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    private BdtPriRestriRecord getDefaultBdtPriRestri() {
        List<BdtPriRestriRecord> bdtPriRestriList =
                dataProvider.findBdtPriRestriListByDtManifestId(getBdtManifestId());

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
        return dataProvider.findCdtAwdPriXpsTypeMapById(
                defaultBdtPriRestri.getCdtAwdPriXpsTypeMapId()
        );
    }

    public CdtAwdPriRecord getDefaultCdtAwdPri() {
        CdtAwdPriXpsTypeMapRecord cdtAwdPriXpsTypeMap = getDefaultCdtAwdPriTypeMap();
        return dataProvider.findCdtAwdPri(cdtAwdPriXpsTypeMap.getCdtAwdPriId());
    }

    public String getCdtPriName() {
        CdtAwdPriRecord cdtAwdPri = getDefaultCdtAwdPri();
        return dataProvider.findCdtPri(cdtAwdPri.getCdtPriId()).getName();
    }

    public String getXbtName() {
        CdtAwdPriXpsTypeMapRecord cdtAwdPriXpsTypeMap = getDefaultCdtAwdPriTypeMap();
        return dataProvider.findXbt(cdtAwdPriXpsTypeMap.getXbtId()).getBuiltinType();
    }

}
