package org.oagi.srt.model;

import org.oagi.srt.model.bie.Fetcher;

import java.util.Collections;
import java.util.List;

public abstract class AbstractLazyNode implements LazyNode {
    private Node delegate;
    private Fetcher fetcher;
    private int childrenCount;
    private Node parent;

    public AbstractLazyNode(Node delegate, Fetcher fetcher, int childrenCount, Node parent) {
        this.delegate = delegate;
        this.fetcher = fetcher;
        this.childrenCount = childrenCount;
        if (childrenCount > 0) {
            setAttribute("fetched", false);
        } else {
            setAttribute("fetched", true);
        }

        setParent(parent);
    }

    private void setParent(Node parent) {
        this.parent = parent;
        if (parent != null) {
            parent.addChild(this);
        }
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public String getType() {
        return delegate.getType();
    }

    @Override
    public int getSeqKey() {
        return delegate.getSeqKey();
    }

    @Override
    public Node getParent() {
        return parent;
    }

    @Override
    public <T extends Node> void addChild(T child) {
        delegate.addChild(child);
    }

    @Override
    public void setAttribute(String key, Object attr) {
        delegate.setAttribute(key, attr);
    }

    @Override
    public Object getAttribute(String key) {
        return delegate.getAttribute(key);
    }

    @Override
    public boolean isFetched() {
        Boolean fetched = (Boolean) getAttribute("fetched");
        return (fetched != null && fetched == true) ? true : false;
    }

    @Override
    public void fetch() {
        if (isFetched()) {
            return;
        }
        fetcher.fetch(this);
        setAttribute("fetched", true);
    }

    @Override
    public int getChildrenCount() {
        return childrenCount;
    }

    @Override
    public List<? extends Node> getChildren() {
        if (!isFetched()) {
            return Collections.emptyList();
        } else {
            return delegate.getChildren();
        }
    }

    @Override
    public void clearChildren() {
        delegate.clearChildren();
    }
}
