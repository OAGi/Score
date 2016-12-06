package org.oagi.srt.model.bie;

import org.oagi.srt.model.BIENode;
import org.oagi.srt.repository.entity.*;

public interface BBIENode extends BIENode {

    public BasicBusinessInformationEntity getBbie();
    public void setBbie(BasicBusinessInformationEntity bbie);

    public BusinessDataTypePrimitiveRestriction getBdtPriRestri();
    public void setBdtPriRestri(BusinessDataTypePrimitiveRestriction bdtPriRestri);

    public void setCodeListId(long codeListId);
    public long getCodeListId();

    public long getAgencyIdListId();
    public void setAgencyIdListId(long agencyIdListId);

    public BasicBusinessInformationEntityProperty getBbiep();
    public void setBbiep(BasicBusinessInformationEntityProperty bbiep);

    public BasicCoreComponentProperty getBccp();
    public void setBccp(BasicCoreComponentProperty bccp);

    public DataType getBdt();
    public void setBdt(DataType bdt);

    public void setRestrictionType(BBIERestrictionType restrictionType);
    public BBIERestrictionType getRestrictionType();
}
