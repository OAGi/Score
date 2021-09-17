package org.oagi.score.export.model;

import org.jooq.types.ULong;
import org.oagi.score.common.util.Utility;
import org.oagi.score.provider.ImportedDataProvider;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.BdtPriRestriRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.DtRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.XbtRecord;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BDTSimpleType extends AbstractBDTSimple {

    private DtRecord dataType;

    private DtRecord baseDataType;

    private boolean isDefaultBDT;

    private List<BdtPriRestriRecord> bdtPriRestriList;

    private List<XbtRecord> xbtList;

    private ImportedDataProvider importedDataProvider;

    public BDTSimpleType(DtRecord dataType, DtRecord baseDataType, boolean isDefaultBDT, ImportedDataProvider importedDataProvider) {
        this(dataType, baseDataType, isDefaultBDT, null, null, importedDataProvider);
    }

    public BDTSimpleType(DtRecord dataType, DtRecord baseDataType, boolean isDefaultBDT,
                         List<BdtPriRestriRecord> bdtPriRestriList,
                         List<XbtRecord> xbtList,
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
        return Utility.denToName(dataType.getDen());
    }

    public String getGuid() {
        return GUID_PREFIX + dataType.getGuid();
    }

    public String getBaseDTName() {
        return Utility.denToName(baseDataType.getDen());
    }

    public boolean isTimepointCDT() {
        String dataTypeTerm = dataType.getDataTypeTerm();
        return (dataTypeTerm.contains("Date") || dataTypeTerm.contains("Time") || dataTypeTerm.contains("Duration"));
    }

    public boolean isBaseDT_CDT() {
        return "Core" == baseDataType.getType();
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
