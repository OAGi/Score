package org.oagi.srt.web.jsf.component.treenode;

import org.oagi.srt.model.BIENodeVisitor;
import org.oagi.srt.model.Node;
import org.oagi.srt.model.bie.ASBIENode;
import org.oagi.srt.model.bie.BBIENode;
import org.oagi.srt.model.bie.BBIESCNode;
import org.oagi.srt.model.bie.TopLevelNode;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

public class TreeBIENodeVisitor implements BIENodeVisitor {

    private TreeNode root = new DefaultTreeNode();

    public TreeNode getRoot() {
        return root;
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
        Node parent = node.getParent();
        TreeNode parentTreeNode = (parent != null) ? (TreeNode) parent.getAttribute("treeNode") : root;
        TreeNode treeNode = new DefaultTreeNode(type, node, parentTreeNode);
        node.setAttribute("treeNode", treeNode);
    }

    @Override
    public void endNode() {
    }
}
