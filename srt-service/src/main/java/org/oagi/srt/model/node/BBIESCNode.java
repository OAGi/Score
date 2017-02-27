package org.oagi.srt.model.node;

import org.oagi.srt.repository.entity.BasicBusinessInformationEntityRestrictionType;
import org.oagi.srt.repository.entity.BasicBusinessInformationEntitySupplementaryComponent;
import org.oagi.srt.repository.entity.DataTypeSupplementaryComponent;

public interface BBIESCNode extends BIENode {

    public BBIEPNode getParent();

    public BasicBusinessInformationEntitySupplementaryComponent getBbieSc();

    public DataTypeSupplementaryComponent getBdtSc();

    public BasicBusinessInformationEntityRestrictionType getRestrictionType();
    public void setRestrictionType(BasicBusinessInformationEntityRestrictionType restrictionType);

}
