package org.oagi.srt.model.treenode;

import org.oagi.srt.repository.entity.DataTypeSupplementaryComponent;

public interface BusinessDataTypeSupplementaryComponentTreeNode extends CoreComponentTreeNode {

    public BasicCoreComponentPropertyTreeNode getParent();

    public DataTypeSupplementaryComponent getDataTypeSupplementaryComponent();
}
