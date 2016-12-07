package org.oagi.srt.model;

public interface CCNode extends Node {

    public void accept(CCNodeVisitor visitor);
}
