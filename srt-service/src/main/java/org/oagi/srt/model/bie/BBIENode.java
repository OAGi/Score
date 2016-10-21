package org.oagi.srt.model.bie;

import org.oagi.srt.model.BIENode;
import org.oagi.srt.repository.entity.*;

import java.util.List;

public interface BBIENode extends BIENode {

    public BasicBusinessInformationEntity getBbie();
    public void setBbie(BasicBusinessInformationEntity bbie);

    public BasicBusinessInformationEntityProperty getBbiep();
    public void setBbiep(BasicBusinessInformationEntityProperty bbiep);

    public BasicCoreComponentProperty getBccp();
    public void setBccp(BasicCoreComponentProperty bccp);

    public DataType getBdt();
    public void setBdt(DataType bdt);

    public void setRestrictionType(BBIERestrictionType restrictionType);
    public BBIERestrictionType getRestrictionType();

    public void setBdtPrimitiveRestrictionId(long bdtPrimitiveRestrictionId);
    public long getBdtPrimitiveRestrictionId();

    public void setCodeListId(long codeListId);
    public long getCodeListId();
}
