package org.oagi.srt.model.bod.visitor;

import org.oagi.srt.model.Node;
import org.oagi.srt.model.NodeVisitor;
import org.oagi.srt.model.bod.ASBIENode;
import org.oagi.srt.model.bod.BBIENode;
import org.oagi.srt.model.bod.BBIESCNode;
import org.oagi.srt.model.bod.TopLevelNode;

import java.util.Collections;

public class NodeSortVisitor implements NodeVisitor {

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
