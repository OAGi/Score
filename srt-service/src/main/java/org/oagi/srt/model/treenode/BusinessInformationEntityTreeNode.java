package org.oagi.srt.model.treenode;

import org.oagi.srt.repository.entity.Usable;

import java.util.Collection;

public interface BusinessInformationEntityTreeNode extends SRTTreeNode, Usable {

    public Collection<? extends BusinessInformationEntityTreeNode> getChildren();
}
