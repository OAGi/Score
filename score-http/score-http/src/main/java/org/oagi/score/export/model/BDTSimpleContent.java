package org.oagi.score.export.model;

import org.jooq.types.ULong;
import org.oagi.score.provider.ImportedDataProvider;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.DtManifestRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.DtRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.DtScManifestRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.DtScRecord;

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

    private ImportedDataProvider importedDataProvider;

    public BDTSimpleContent(DtManifestRecord dtManifestRecord, DtRecord dataType,
                            DtManifestRecord baseDtManifestRecord, DtRecord baseDataType,
                            boolean isDefaultBDT,
                            Map<DtScManifestRecord, DtScRecord> dtScMap,
                            ImportedDataProvider importedDataProvider) {
        super(importedDataProvider);

        this.dtManifestRecord = dtManifestRecord;
        this.dataType = dataType;
        this.baseDtManifestRecord = baseDtManifestRecord;
        this.baseDataType = baseDataType;
        this.isDefaultBDT = isDefaultBDT;
        this.importedDataProvider = importedDataProvider;
        this.dtScList = map(dtScMap);
    }

    private List<BDTSC> map(Map<DtScManifestRecord, DtScRecord> dtScMap) {
        return dtScMap.entrySet().stream()
                .map(entry -> new BDTSC(entry.getKey(), entry.getValue(), importedDataProvider))
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
    public DtRecord getBaseDataType() {
        return baseDataType;
    }

    public String getName() {
        return ModelUtils.getTypeName(dataType);
    }

    public String getGuid() {
        return GUID_PREFIX + dataType.getGuid();
    }

    public String getBaseDTName() {
        return ModelUtils.getTypeName(baseDataType);
    }

    public List<BDTSC> getDtScList() {
        return dtScList;
    }
}
