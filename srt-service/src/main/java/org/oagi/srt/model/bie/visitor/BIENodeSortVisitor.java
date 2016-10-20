package org.oagi.srt.model.bie.visitor;

import org.oagi.srt.model.BIENodeVisitor;
import org.oagi.srt.model.Node;
import org.oagi.srt.model.bie.ASBIENode;
import org.oagi.srt.model.bie.BBIENode;
import org.oagi.srt.model.bie.BBIESCNode;
import org.oagi.srt.model.bie.TopLevelNode;

import java.util.Collections;

public class BIENodeSortVisitor implements BIENodeVisitor {

    @Override
    public void startNode(TopLevelNode topLevelNode) {
        sortChildren(topLevelNode);
    }

    @Override
    public void visitASBIENode(ASBIENode asbieNode) {
        sortChildren(asbieNode);
    }

    @Override
    public void visitBBIENode(BBIENode bbieNode) {
        sortChildren(bbieNode);
    }

    @Override
    public void visitBBIESCNode(BBIESCNode bbiescNode) {
    }

    @Override
    public void endNode() {
    }

    private void sortChildren(Node node) {
        Collections.sort(node.getChildren(), (a, b) -> a.getSeqKey() - b.getSeqKey());
    }
}
