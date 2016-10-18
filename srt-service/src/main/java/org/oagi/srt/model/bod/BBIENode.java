package org.oagi.srt.model.bod;

import org.oagi.srt.model.Node;
import org.oagi.srt.repository.entity.*;

import java.util.List;

public interface BBIENode extends Node {

    public BasicBusinessInformationEntity getBbie();
    public void setBbie(BasicBusinessInformationEntity bbie);

    public BasicBusinessInformationEntityProperty getBbiep();
    public void setBbiep(BasicBusinessInformationEntityProperty bbiep);

    public BasicCoreComponentProperty getBccp();
    public void setBccp(BasicCoreComponentProperty bccp);

    public DataType getBdt();
    public void setBdt(DataType bdt);

    public void setBdtPrimitiveRestrictionId(long bdtPrimitiveRestrictionId);
    public long getBdtPrimitiveRestrictionId();

    public List<BusinessDataTypePrimitiveRestriction> getBdtPriRestriList();
    public void setBdtPriRestriList(List<BusinessDataTypePrimitiveRestriction> bdtPriRestriList);
}
