package org.oagi.srt.model;

import java.util.List;

public interface Node {

    public String getName();

    public String getType();

    public int getSeqKey();

    public Node getParent();

    public <T extends Node> void addChild(T child);

    public List<? extends Node> getChildren();

    public void clearChildren();

    public void setAttribute(String key, Object attr);

    public Object getAttribute(String key);

}
