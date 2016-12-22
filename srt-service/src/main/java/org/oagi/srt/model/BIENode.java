package org.oagi.srt.model;

import org.oagi.srt.repository.entity.Usable;

public interface BIENode extends Node, Usable {

    public void accept(BIENodeVisitor visitor);
}
