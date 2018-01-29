package org.oagi.srt.export.model;

import org.oagi.srt.repository.entity.CoreDataTypeAllowedPrimitive;
import org.oagi.srt.repository.entity.DataType;

public interface BDTSimple {

    public long getBdtId();

    public boolean isDefaultBDT();

    public String getName();

    public String getGuid();

    public String getBaseDTName();


    public DataType getDataType();

    public DataType getBaseDataType();


    public CoreDataTypeAllowedPrimitive getDefaultCdtAwdPri();

    public String getCdtPriName();

}
