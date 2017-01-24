package org.oagi.srt.model.treenode;

import org.oagi.srt.repository.entity.BasicBusinessInformationEntitySupplementaryComponent;

public interface BasicBusinessInformationEntitySupplementaryComponentTreeNode extends BusinessInformationEntityTreeNode {

    public BasicBusinessInformationEntityPropertyTreeNode getParent();

    public BasicBusinessInformationEntitySupplementaryComponent getBasicBusinessInformationEntitySupplementaryComponent();
}
