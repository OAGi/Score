package org.oagi.srt.web.jsf.component.treenode;

import org.oagi.srt.model.BIENodeVisitor;
import org.oagi.srt.model.LazyNode;
import org.oagi.srt.model.Node;
import org.oagi.srt.model.bie.ASBIENode;
import org.oagi.srt.model.bie.BBIENode;
import org.oagi.srt.model.bie.BBIESCNode;
import org.oagi.srt.model.bie.TopLevelNode;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

public class LazyTreeBIENodeVisitor implements BIENodeVisitor {

    private DefaultTreeNode parent;

    public TreeNode getParent() {
        return parent;
    }

    public LazyTreeBIENodeVisitor() {
        parent = new DefaultTreeNode();
    }

    public LazyTreeBIENodeVisitor(DefaultTreeNode parent) {
        this.parent = parent;
    }

    @Override
    public void startNode(TopLevelNode topLevelNode) {
        visit(topLevelNode, "ABIE");
    }

    @Override
    public void visitASBIENode(ASBIENode asbieNode) {
        String type = ("Extension".equals(asbieNode.getName())) ? "ASBIE-Extension" : "ASBIE";
        visit(asbieNode, type);
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
