package org.oagi.srt.model.treenode;

import org.oagi.srt.model.bie.BBIERestrictionType;
import org.oagi.srt.repository.entity.*;

public interface BasicBusinessInformationEntityPropertyTreeNode extends BusinessInformationEntityTreeNode {

    public AssociationBusinessInformationEntityPropertyTreeNode getParent();

    public BasicBusinessInformationEntityProperty getBasicBusinessInformationEntityProperty();
    public BasicBusinessInformationEntityProperty getBbiep();

    public BasicCoreComponentProperty getBasicCoreComponentProperty();
    public BasicCoreComponentProperty getBccp();

    public BasicBusinessInformationEntity getBasicBusinessInformationEntity();
    public BasicBusinessInformationEntity getBbie();

    public BasicCoreComponent getBasicCoreComponent();
    public BasicCoreComponent getBcc();

    public DataType getBusinessDataType();
    public DataType getBdt();

    public BasicBusinessInformationEntityRestrictionType getRestrictionType();
    public void setRestrictionType(BasicBusinessInformationEntityRestrictionType restrictionType);
}
