package org.oagi.srt.service.treenode;

import org.oagi.srt.repository.entity.CoreComponent;
import org.oagi.srt.repository.entity.Namespace;

public interface CoreComponentTreeNode<R extends CoreComponent> {

    public R getRaw();

    public String getId();

    public Namespace getNamespace();

}
