package org.oagi.srt.service.treenode;

import org.oagi.srt.repository.entity.AssociationCoreComponent;
import org.oagi.srt.repository.entity.AssociationCoreComponentProperty;

public interface AssociationCoreComponentPropertyTreeNode extends CoreComponentPropertyTreeNode {

    public AssociationCoreComponent getAssociationCoreComponent();

    public AssociationCoreComponentProperty getAssociationCoreComponentProperty();

    public AggregateCoreComponentTreeNode getType();
}
