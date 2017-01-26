package org.oagi.srt.model.treenode;

import org.oagi.srt.repository.entity.BasicCoreComponent;
import org.oagi.srt.repository.entity.BasicCoreComponentProperty;
import org.oagi.srt.repository.entity.DataType;

public interface BasicCoreComponentPropertyTreeNode extends CoreComponentPropertyTreeNode {

    public BasicCoreComponent getBasicCoreComponent();
    public BasicCoreComponent getBcc();

    public BasicCoreComponentProperty getBasicCoreComponentProperty();
    public BasicCoreComponentProperty getBccp();

    public DataType getBusinessDataType();
    public DataType getBdt();
}
