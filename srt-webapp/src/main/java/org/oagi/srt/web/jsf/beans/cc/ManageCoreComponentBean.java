package org.oagi.srt.web.jsf.beans.cc;

import org.oagi.srt.model.CCNode;
import org.oagi.srt.model.CCNodeVisitor;
import org.oagi.srt.model.Node;
import org.oagi.srt.model.cc.ACCNode;
import org.oagi.srt.model.cc.ASCCPNode;
import org.oagi.srt.model.cc.BCCPNode;
import org.oagi.srt.service.NodeService;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
@Transactional(readOnly = true)
public class ManageCoreComponentBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private NodeService nodeService;

    private TreeNode treeNode;
    private TreeNode selectedTreeNode;

    @PostConstruct
    public void init() {
        treeNode = new DefaultTreeNode();
        setTreeNode(treeNode);

        Long asccpId = Long.parseLong(
                FacesContext.getCurrentInstance().getExternalContext()
                        .getRequestParameterMap().get("asccpId"));

        long s = System.currentTimeMillis();
        CCNode ccNode = nodeService.createCCNode(asccpId);
        logger.info("Nodes are structured - elapsed time: " + (System.currentTimeMillis() - s) + " ms");
        s = System.currentTimeMillis();

        TreeNodeBuilder treeNodeBuilder = new TreeNodeBuilder(treeNode);
        ccNode.accept(treeNodeBuilder);
        logger.info("TreeNodes are structured - elapsed time: " + (System.currentTimeMillis() - s) + " ms");
    }

    public TreeNode getTreeNode() {
        return treeNode;
    }

    public void setTreeNode(TreeNode treeNode) {
        this.treeNode = treeNode;
    }

    public TreeNode getSelectedTreeNode() {
        return selectedTreeNode;
    }

    public void setSelectedTreeNode(TreeNode selectedTreeNode) {
        this.selectedTreeNode = selectedTreeNode;
    }

    public void expand(NodeExpandEvent expandEvent) {
        DefaultTreeNode treeNode = (DefaultTreeNode) expandEvent.getTreeNode();
    }

    private class TreeNodeBuilder implements CCNodeVisitor {

        private TreeNode root;

        public TreeNodeBuilder(TreeNode root) {
            this.root = root;
        }

        @Override
        public void visitASCCPNode(ASCCPNode asccpNode) {
            visitNode(asccpNode, "ASCCP");
        }

        @Override
        public void visitACCNode(ACCNode accNode) {
            visitNode(accNode, "ACC");
        }

        @Override
        public void visitBCCNode(BCCPNode bccNode) {
            visitNode(bccNode, "BCCP");
        }

        private TreeNode visitNode(CCNode node, String type) {
            Node parent = node.getParent();
            TreeNode parentTreeNode = (parent != null) ? (TreeNode) parent.getAttribute("treeNode") : root;
            TreeNode treeNode = new DefaultTreeNode(type, node, parentTreeNode);
            node.setAttribute("treeNode", treeNode);
            return treeNode;
        }
    }
}
