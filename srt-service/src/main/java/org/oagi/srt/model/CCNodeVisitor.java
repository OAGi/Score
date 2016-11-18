package org.oagi.srt.model;

import org.oagi.srt.model.cc.ACCNode;
import org.oagi.srt.model.cc.ASCCPNode;
import org.oagi.srt.model.cc.BCCPNode;
import org.oagi.srt.model.cc.BDTSCNode;

public interface CCNodeVisitor {

    public void visitASCCPNode(ASCCPNode asccpNode);

    public void visitACCNode(ACCNode accNode);

    public void visitBCCPNode(BCCPNode bccpNode);

    public void visitBDTSCNode(BDTSCNode bdtscNode);

}
