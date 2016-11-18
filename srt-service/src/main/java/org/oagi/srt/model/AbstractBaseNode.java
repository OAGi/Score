package org.oagi.srt.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractBaseNode implements Node {

    private final int seqKey;
    private Node parent;
    private Map<String, Object> attributes = new HashMap();

    public AbstractBaseNode(int seqKey) {
        this.seqKey = seqKey;
    }

    public AbstractBaseNode(int seqKey, Node parent) {
        this.seqKey = seqKey;
        setParent(parent);
    }

    private void setParent(Node parent) {
        this.parent = parent;
        if (parent != null) {
            parent.addChild(this);
        }
    }

    @Override
    public int getSeqKey() {
        return seqKey;
    }

    @Override
    public Node getParent() {
        return parent;
    }

    @Override
    public <T extends Node> void addChild(T child) {
    }

    @Override
    public List<? extends Node> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public void clearChildren() {
    }

    @Override
    public void setAttribute(String key, Object attr) {
        attributes.put(key, attr);
    }

    @Override
    public Object getAttribute(String key) {
        return attributes.get(key);
    }
}
