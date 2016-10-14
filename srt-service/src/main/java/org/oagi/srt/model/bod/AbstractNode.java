package org.oagi.srt.model.bod;

import org.oagi.srt.model.Node;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractNode implements Node {

    private final int seqKey;
    private Node parent;
    private Map<String, Object> attributes = new HashMap();

    public AbstractNode(int seqKey) {
        this.seqKey = seqKey;
    }

    public AbstractNode(int seqKey, Node parent) {
        this.seqKey = seqKey;
        this.parent = parent;
        parent.addChild(this);
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
        throw new UnsupportedOperationException();
    }

    @Override
    public List<? extends Node> getChildren() {
        return Collections.emptyList();
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
