package org.oagi.srt.model.node;

import org.oagi.srt.repository.entity.Usable;

import java.util.Collection;

public interface BIENode extends SRTNode, Usable {

    public Collection<? extends BIENode> getChildren();

    public void accept(BIENodeVisitor visitor);

    public void validate();
}
