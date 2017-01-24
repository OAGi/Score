package org.oagi.srt.model.treenode;

import org.oagi.srt.repository.entity.*;

public interface BasicBusinessInformationEntityPropertyTreeNode extends BusinessInformationEntityTreeNode {

    public BasicBusinessInformationEntityProperty getBasicBusinessInformationEntityProperty();

    public BasicCoreComponentProperty getBasicCoreComponentProperty();

    public BasicBusinessInformationEntity getBasicBusinessInformationEntity();

    public BasicCoreComponent getBasicCoreComponent();

    public DataType getDataType();
}
