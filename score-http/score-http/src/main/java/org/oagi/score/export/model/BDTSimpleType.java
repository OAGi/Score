package org.oagi.score.export.model;

import org.jooq.types.ULong;
import org.oagi.score.provider.ImportedDataProvider;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.BdtPriRestriRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.DtManifestRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.DtRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.XbtRecord;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BDTSimpleType extends AbstractBDTSimple {

    private DtManifestRecord dtManifestRecord;

    private DtRecord dataType;

    private DtManifestRecord baseDtManifestRecord;

    private DtRecord baseDataType;

    private boolean isDefaultBDT;

    private List<BdtPriRestriRecord> bdtPriRestriList;

    private List<XbtRecord> xbtList;

    private ImportedDataProvider importedDataProvider;

    public BDTSimpleType(DtManifestRecord dtManifestRecord, DtRecord dataType,
                         DtManifestRecord baseDtManifestRecord, DtRecord baseDataType,
                         boolean isDefaultBDT, ImportedDataProvider importedDataProvider) {
        this(dtManifestRecord, dataType, baseDtManifestRecord, baseDataType,
                isDefaultBDT, null, null, importedDataProvider);
    }

    public BDTSimpleType(DtManifestRecord dtManifestRecord, DtRecord dataType,
                         DtManifestRecord baseDtManifestRecord, DtRecord baseDataType,
                         boolean isDefaultBDT,
                         List<BdtPriRestriRecord> bdtPriRestriList,
                         List<XbtRecord> xbtList,
                         ImportedDataProvider importedDataProvider) {
        super(importedDataProvider);

        this.dtManifestRecord = dtManifestRecord;
        this.dataType = dataType;
        this.baseDtManifestRecord = baseDtManifestRecord;
        this.baseDataType = baseDataType;
        this.isDefaultBDT = isDefaultBDT;
        this.bdtPriRestriList = (bdtPriRestriList != null) ? bdtPriRestriList : Collections.emptyList();
        this.xbtList = (xbtList != null) ? xbtList : Collections.emptyList();
        this.importedDataProvider = importedDataProvider;
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

    public boolean isTimepointCDT() {
        String dataTypeTerm = dataType.getDataTypeTerm();
        return (dataTypeTerm.contains("Date") || dataTypeTerm.contains("Time") || dataTypeTerm.contains("Duration"));
    }

    public boolean isBaseDT_CDT() {
        return baseDataType.getBasedDtId() == null;
    }

    public int count_BDT_PRI_RESTRI() {
        return bdtPriRestriList.size();
    }

    public List<String> getXbtBuiltInTypes() {
        return xbtList.stream()
                .map(e -> e.getBuiltinType())
                .collect(Collectors.toList());
    }
}
