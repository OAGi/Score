package org.oagi.srt.web.jsf.component.treenode;

import org.oagi.srt.model.LazyNode;
import org.oagi.srt.model.Node;
import org.oagi.srt.model.NodeVisitor;
import org.oagi.srt.model.bod.ASBIENode;
import org.oagi.srt.model.bod.BBIENode;
import org.oagi.srt.model.bod.BBIESCNode;
import org.oagi.srt.model.bod.TopLevelNode;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import java.util.ArrayList;

public class LazyTreeNodeVisitor implements NodeVisitor {

    private DefaultTreeNode parent;

    public TreeNode getParent() {
        return parent;
    }

    public LazyTreeNodeVisitor() {
        parent = new DefaultTreeNode();
    }

    public LazyTreeNodeVisitor(DefaultTreeNode parent) {
        this.parent = parent;
    }

    @Override
    public void startNode(TopLevelNode topLevelNode) {
        visit(topLevelNode, "ABIE");
    }

    @Override
    public void visitASBIENode(ASBIENode asbieNode) {
        visit(asbieNode, "ASBIE");
    }

    @Override
    public void visitBBIENode(BBIENode bbieNode) {
        visit(bbieNode, "BBIE");
    }

    @Override
    public void visitBBIESCNode(BBIESCNode bbiescNode) {
        visit(bbiescNode, "BBIESC");
    }

    private void visit(Node node, String type) {
        TreeNode treeNode = new DefaultTreeNode(type, node, this.parent);
        if (node instanceof LazyNode) {
            LazyNode lazyNode = (LazyNode) node;
            if (!lazyNode.isFetched()) {
                for (int i = 0, len = lazyNode.getChildrenCount(); i < len; ++i) {
                    new DefaultTreeNode(null, treeNode);
                }
            }
        }
    }

    @Override
    public void endNode() {
    }
}
