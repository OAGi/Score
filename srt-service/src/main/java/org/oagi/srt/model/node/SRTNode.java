package org.oagi.srt.model.node;

import org.oagi.srt.model.Reloadable;

import java.util.Collection;

public interface SRTNode extends Reloadable {

    public String getId();

    public boolean hasChild();

    public Collection<? extends SRTNode> getChildren();

    public void setAttribute(String key, Object attr);

    public Object getAttribute(String key);

}
