package org.oagi.srt.model.treenode;

import org.oagi.srt.repository.entity.AssociationCoreComponent;
import org.oagi.srt.repository.entity.AssociationCoreComponentProperty;

public interface AssociationCoreComponentPropertyTreeNode extends CoreComponentPropertyTreeNode {

    public AssociationCoreComponent getAssociationCoreComponent();
    public AssociationCoreComponent getAscc();

    public AssociationCoreComponentProperty getAssociationCoreComponentProperty();
    public AssociationCoreComponentProperty getAsccp();

    public AggregateCoreComponentTreeNode getType();
}
