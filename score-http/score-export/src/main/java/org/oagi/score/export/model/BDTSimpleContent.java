package org.oagi.score.export.model;

import org.jooq.types.ULong;
import org.oagi.score.common.util.Utility;
import org.oagi.score.provider.ImportedDataProvider;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.DtRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.DtScRecord;

import java.util.List;
import java.util.stream.Collectors;

public class BDTSimpleContent extends AbstractBDTSimple {

    private DtRecord dataType;

    private DtRecord baseDataType;

    private boolean isDefaultBDT;

    private List<BDTSC> dtScList;

    private ImportedDataProvider importedDataProvider;

    public BDTSimpleContent(DtRecord dataType, DtRecord baseDataType, boolean isDefaultBDT,
                            List<DtScRecord> dtScList,
                            ImportedDataProvider importedDataProvider) {
        super(importedDataProvider);

        this.dataType = dataType;
        this.baseDataType = baseDataType;
        this.isDefaultBDT = isDefaultBDT;
        this.importedDataProvider = importedDataProvider;
        this.dtScList = map(dtScList);
    }

    private List<BDTSC> map(List<DtScRecord> dtScList) {
        return dtScList.stream()
                .map(dtSc -> new BDTSC(dtSc, importedDataProvider))
                .collect(Collectors.toList());
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
        return dataType.getGuid();
    }

    public String getBaseDTName() {
        return Utility.denToName(baseDataType.getDen());
    }

    public List<BDTSC> getDtScList() {
        return dtScList;
    }
}
