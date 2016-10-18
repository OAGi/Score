package org.oagi.srt.model;

public interface LazyNode extends Node {

    public int getChildrenCount();

    public boolean isFetched();

    public void fetch();
}
