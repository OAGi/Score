package org.oagi.srt.model.treenode;

import org.oagi.srt.repository.entity.AssociationBusinessInformationEntity;
import org.oagi.srt.repository.entity.AssociationBusinessInformationEntityProperty;
import org.oagi.srt.repository.entity.AssociationCoreComponent;
import org.oagi.srt.repository.entity.AssociationCoreComponentProperty;

public interface AssociationBusinessInformationEntityPropertyTreeNode extends BusinessInformationEntityTreeNode {

    public AssociationBusinessInformationEntityPropertyTreeNode getParent();

    public AssociationBusinessInformationEntityProperty getAssociationBusinessInformationEntityProperty();
    public AssociationBusinessInformationEntityProperty getAsbiep();

    public AssociationCoreComponentProperty getAssociationCoreComponentProperty();
    public AssociationCoreComponentProperty getAsccp();

    public AssociationBusinessInformationEntity getAssociationBusinessInformationEntity();
    public AssociationBusinessInformationEntity getAsbie();

    public AssociationCoreComponent getAssociationCoreComponent();
    public AssociationCoreComponent getAscc();

    public AggregateBusinessInformationEntityTreeNode getType();
}
