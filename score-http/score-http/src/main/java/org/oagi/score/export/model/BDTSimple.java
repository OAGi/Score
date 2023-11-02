package org.oagi.score.export.model;

import org.jooq.types.ULong;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.CdtAwdPriRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.DtManifestRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.DtRecord;

public interface BDTSimple {

    public ULong getBdtManifestId();

    public ULong getBdtId();

    public boolean isDefaultBDT();

    public String getName();

    public String getGuid();

    public String getBaseDTName();


    public DtManifestRecord getDataTypeManifest();
    public DtRecord getDataType();
    public DtManifestRecord getBaseDataTypeManifest();
    public DtRecord getBaseDataType();


    public CdtAwdPriRecord getDefaultCdtAwdPri();

    public String getCdtPriName();

}
