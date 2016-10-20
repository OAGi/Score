package org.oagi.srt.model;

public interface BIENode extends Node {

    public void accept(BIENodeVisitor visitor);
}
