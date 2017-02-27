package org.oagi.srt.model.node;

import org.oagi.srt.repository.entity.*;

public interface BBIEPNode extends BIENode {

    public ASBIEPNode getParent();

    public BasicBusinessInformationEntityProperty getBbiep();

    public BasicCoreComponentProperty getBccp();

    public BasicBusinessInformationEntity getBbie();

    public BasicCoreComponent getBcc();

    public DataType getBdt();

    public BasicBusinessInformationEntityRestrictionType getRestrictionType();
    public void setRestrictionType(BasicBusinessInformationEntityRestrictionType restrictionType);
}
