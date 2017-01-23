package org.oagi.srt.service.treenode;

import org.oagi.srt.repository.entity.Namespace;

import java.util.Collection;

public interface CoreComponentTreeNode extends Reloadable {

    public String getId();

    public Namespace getNamespace();

    public boolean hasChild();

    public Collection<? extends CoreComponentTreeNode> getChildren();

    public void setAttribute(String key, Object attr);

    public Object getAttribute(String key);

}
