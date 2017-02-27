package org.oagi.srt.model.node;

import org.oagi.srt.repository.entity.Namespace;

import java.util.Collection;

public interface CCNode extends SRTNode {

    public Collection<? extends CCNode> getChildren();

    public Namespace getNamespace();
    public void setNamespace(Namespace namespace);

}
