package org.oagi.srt.model.treenode;

import org.oagi.srt.repository.entity.Namespace;

import java.util.Collection;

public interface CoreComponentTreeNode extends SRTTreeNode {

    public Collection<? extends CoreComponentTreeNode> getChildren();

    public Namespace getNamespace();

}
