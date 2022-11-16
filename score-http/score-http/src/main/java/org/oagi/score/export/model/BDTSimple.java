package org.oagi.score.export.model;

import org.jooq.types.ULong;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.CdtAwdPriRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.DtRecord;

public interface BDTSimple {

    final String GUID_PREFIX = "oagis-id-";

    public ULong getBdtManifestId();

    public ULong getBdtId();

    public boolean isDefaultBDT();

    public String getName();

    public String getGuid();

    public String getBaseDTName();


    public DtRecord getDataType();

    public DtRecord getBaseDataType();


    public CdtAwdPriRecord getDefaultCdtAwdPri();

    public String getCdtPriName();

}
