package org.oagi.srt.service.treenode;

import org.oagi.srt.repository.entity.BasicCoreComponent;
import org.oagi.srt.repository.entity.BasicCoreComponentProperty;
import org.oagi.srt.repository.entity.DataType;

public interface BasicCoreComponentPropertyTreeNode
        extends CoreComponentPropertyTreeNode<BasicCoreComponentProperty, BasicCoreComponent> {

    public DataType getDataType();
}
