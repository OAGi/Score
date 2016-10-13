package org.oagi.srt.web.jsf.component.treenode;

import org.oagi.srt.model.Node;
import org.oagi.srt.model.NodeVisitor;
import org.oagi.srt.model.bod.ASBIENode;
import org.oagi.srt.model.bod.BBIENode;
import org.oagi.srt.model.bod.BBIESCNode;
import org.oagi.srt.model.bod.TopLevelNode;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

public class TreeNodeVisitor implements NodeVisitor {

    private TreeNode root = new DefaultTreeNode();

    public TreeNode getRoot() {
        return root;
    }

    @Override
    public void visit(Node node) {
        Node parent = node.getParent();
        TreeNode parentTreeNode = (parent != null) ? (TreeNode) parent.getAttribute("treeNode") : root;
        TreeNode treeNode;
        if (node instanceof TopLevelNode) {
            treeNode = new DefaultTreeNode("ABIE", node, parentTreeNode);
        } else if (node instanceof ASBIENode) {
            treeNode = new DefaultTreeNode("ASBIE", node, parentTreeNode);
        } else if (node instanceof BBIENode) {
            treeNode = new DefaultTreeNode("BBIE", node, parentTreeNode);
        } else if (node instanceof BBIESCNode) {
            treeNode = new DefaultTreeNode("BBIESC", node, parentTreeNode);
        } else {
            throw new IllegalStateException("Unknown node: " + node);
        }

        node.setAttribute("treeNode", treeNode);
    }

}
