package org.oagi.srt.model.treenode;

import java.util.Collection;

public interface BusinessInformationEntityTreeNode extends SRTTreeNode {

    public Collection<? extends BusinessInformationEntityTreeNode> getChildren();
}
