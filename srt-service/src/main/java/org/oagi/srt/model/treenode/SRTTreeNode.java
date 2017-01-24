package org.oagi.srt.model.treenode;

import org.oagi.srt.model.Reloadable;

import java.util.Collection;

public interface SRTTreeNode extends Reloadable {

    public String getId();

    public boolean hasChild();

    public Collection<? extends SRTTreeNode> getChildren();

    public void setAttribute(String key, Object attr);

    public Object getAttribute(String key);

}
