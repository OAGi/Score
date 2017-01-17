package org.oagi.srt.service.treenode;

import org.oagi.srt.repository.entity.AggregateCoreComponent;

import java.util.Collection;

public interface AggregateCoreComponentTreeNode
        extends CoreComponentTreeNode<AggregateCoreComponent> {

    public AggregateCoreComponentTreeNode getBase();

    public boolean hasChild();

    public Collection<? extends CoreComponentPropertyTreeNode> getChildren();

}
