package org.oagi.score.export.model;

import org.jooq.types.ULong;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.DtManifestRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.DtRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.DtScManifestRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.DtScRecord;
import org.oagi.score.repository.provider.DataProvider;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BDTSimpleContent extends AbstractBDTSimple {

    private DtManifestRecord dtManifestRecord;

    private DtRecord dataType;

    private DtManifestRecord baseDtManifestRecord;

    private DtRecord baseDataType;

    private boolean isDefaultBDT;

    private List<BDTSC> dtScList;

    private DataProvider dataProvider;

    public BDTSimpleContent(DtManifestRecord dtManifestRecord, DtRecord dataType,
                            DtManifestRecord baseDtManifestRecord, DtRecord baseDataType,
                            boolean isDefaultBDT,
                            Map<DtScManifestRecord, DtScRecord> dtScMap,
                            DataProvider dataProvider) {
        super(dataProvider);

        this.dtManifestRecord = dtManifestRecord;
        this.dataType = dataType;
        this.baseDtManifestRecord = baseDtManifestRecord;
        this.baseDataType = baseDataType;
        this.isDefaultBDT = isDefaultBDT;
        this.dataProvider = dataProvider;
        this.dtScList = map(dtScMap);
    }

    private List<BDTSC> map(Map<DtScManifestRecord, DtScRecord> dtScMap) {
        return dtScMap.entrySet().stream()
                .map(entry -> new BDTSC(entry.getKey(), entry.getValue(), dataProvider))
                .collect(Collectors.toList());
    }

    public ULong getBdtManifestId() {
        return this.dtManifestRecord.getDtManifestId();
    }

    @Override
    public ULong getBdtId() {
        return dataType.getDtId();
    }

    @Override
    public boolean isDefaultBDT() {
        return isDefaultBDT;
    }

    @Override
    public DtRecord getDataType() {
        return dataType;
    }

    @Override
    public DtManifestRecord getBaseDataTypeManifest() {
        return baseDtManifestRecord;
    }

    @Override
    public DtRecord getBaseDataType() {
        return baseDataType;
    }

    public String getName() {
        return ModelUtils.getTypeName(dtManifestRecord, dataType);
    }

    public String getGuid() {
        return dataType.getGuid();
    }

    public String getBaseDTName() {
        return ModelUtils.getTypeName(baseDtManifestRecord, baseDataType);
    }

    @Override
    public DtManifestRecord getDataTypeManifest() {
        return dtManifestRecord;
    }

    public ULong getNamespaceId() {
        return dataType.getNamespaceId();
    }

    public List<BDTSC> getDtScList() {
        return dtScList;
    }
}
