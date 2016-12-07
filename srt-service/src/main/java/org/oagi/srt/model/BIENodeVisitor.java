package org.oagi.srt.model;

import org.oagi.srt.model.bie.ASBIENode;
import org.oagi.srt.model.bie.BBIENode;
import org.oagi.srt.model.bie.BBIESCNode;
import org.oagi.srt.model.bie.TopLevelNode;

public interface BIENodeVisitor {

    public void startNode(TopLevelNode topLevelNode);

    public void visitASBIENode(ASBIENode asbieNode);

    public void visitBBIENode(BBIENode bbieNode);

    public void visitBBIESCNode(BBIESCNode bbiescNode);

    public void endNode();
}
