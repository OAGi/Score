package org.oagi.srt.model.treenode;

import org.oagi.srt.repository.entity.AssociationBusinessInformationEntity;
import org.oagi.srt.repository.entity.AssociationBusinessInformationEntityProperty;
import org.oagi.srt.repository.entity.AssociationCoreComponent;
import org.oagi.srt.repository.entity.AssociationCoreComponentProperty;

public interface AssociationBusinessInformationEntityPropertyTreeNode extends BusinessInformationEntityTreeNode {

    public AssociationBusinessInformationEntityProperty getAssociationBusinessInformationEntityProperty();

    public AssociationCoreComponentProperty getAssociationCoreComponentProperty();

    public AssociationBusinessInformationEntity getAssociationBusinessInformationEntity();

    public AssociationCoreComponent getAssociationCoreComponent();

    public AggregateBusinessInformationEntityTreeNode getType();
}
