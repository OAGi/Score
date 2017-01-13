package org.oagi.srt.service.treenode;

import org.oagi.srt.repository.entity.CoreComponentProperty;
import org.oagi.srt.repository.entity.CoreComponentRelation;

public interface CoreComponentPropertyTreeNode<R extends CoreComponentProperty, A extends CoreComponentRelation>
        extends CoreComponentTreeNode<R> {

    public AggregateCoreComponentTreeNode getParent();

    public A getRawRelation();

}
