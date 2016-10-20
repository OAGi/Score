package org.oagi.srt.model;

import org.oagi.srt.model.cc.ACCNode;
import org.oagi.srt.model.cc.ASCCPNode;
import org.oagi.srt.model.cc.BCCPNode;

public interface CCNodeVisitor {

    public void visitASCCPNode(ASCCPNode asccpNode);

    public void visitACCNode(ACCNode accNode);

    public void visitBCCNode(BCCPNode bccNode);

}
