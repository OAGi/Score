package org.oagi.srt.model;

import org.oagi.srt.model.bod.ASBIENode;
import org.oagi.srt.model.bod.BBIENode;
import org.oagi.srt.model.bod.BBIESCNode;
import org.oagi.srt.model.bod.TopLevelNode;

public interface NodeVisitor {

    public void startNode(TopLevelNode topLevelNode);

    public void visitASBIENode(ASBIENode asbieNode);

    public void visitBBIENode(BBIENode bbieNode);

    public void visitBBIESCNode(BBIESCNode bbiescNode);

    public void endNode();
}
