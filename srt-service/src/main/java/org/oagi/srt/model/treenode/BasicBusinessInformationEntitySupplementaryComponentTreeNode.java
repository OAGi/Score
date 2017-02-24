package org.oagi.srt.model.treenode;

import org.oagi.srt.repository.entity.BasicBusinessInformationEntityRestrictionType;
import org.oagi.srt.repository.entity.BasicBusinessInformationEntitySupplementaryComponent;
import org.oagi.srt.repository.entity.DataTypeSupplementaryComponent;

public interface BasicBusinessInformationEntitySupplementaryComponentTreeNode extends BusinessInformationEntityTreeNode {

    public BasicBusinessInformationEntityPropertyTreeNode getParent();

    public BasicBusinessInformationEntitySupplementaryComponent getBasicBusinessInformationEntitySupplementaryComponent();
    public BasicBusinessInformationEntitySupplementaryComponent getBbieSc();

    public DataTypeSupplementaryComponent getBusinessDataTypeSupplementaryComponent();
    public DataTypeSupplementaryComponent getBdtSc();

    public BasicBusinessInformationEntityRestrictionType getRestrictionType();
    public void setRestrictionType(BasicBusinessInformationEntityRestrictionType restrictionType);

}
