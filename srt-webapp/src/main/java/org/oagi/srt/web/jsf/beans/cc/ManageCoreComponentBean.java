package org.oagi.srt.web.jsf.beans.cc;

import org.oagi.srt.model.CCNode;
import org.oagi.srt.model.CCNodeVisitor;
import org.oagi.srt.model.LazyNode;
import org.oagi.srt.model.Node;
import org.oagi.srt.model.cc.ACCNode;
import org.oagi.srt.model.cc.ASCCPNode;
import org.oagi.srt.model.cc.BCCPNode;
import org.oagi.srt.repository.entity.CoreComponentState;
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
import java.util.ArrayList;

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
        Long asccpId = Long.parseLong(
                FacesContext.getCurrentInstance().getExternalContext()
                        .getRequestParameterMap().get("asccpId"));

        TreeNode treeNode = createLazyTreeNode(asccpId);
        setTreeNode(treeNode);
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

    public CoreComponentState getState() {
        TreeNode treeNode = getSelectedTreeNode();
        if (treeNode == null) {
            return null;
        }
        CCNode node = (CCNode) treeNode.getData();
        if (node instanceof ASCCPNode) {
            ASCCPNode asccpNode = (ASCCPNode) node;
            return asccpNode.getAsccp().getState();
        } else {
            return null;
        }
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
        public void visitBCCPNode(BCCPNode bccNode) {
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

    private TreeNode createBaseTreeNode(long asccpId) {
        TreeNode treeNode = new DefaultTreeNode();

        long s = System.currentTimeMillis();
        CCNode ccNode = nodeService.createCCNode(asccpId);
        logger.info("Nodes are structured - elapsed time: " + (System.currentTimeMillis() - s) + " ms");
        s = System.currentTimeMillis();

        TreeNodeBuilder treeNodeBuilder = new TreeNodeBuilder(treeNode);
        ccNode.accept(treeNodeBuilder);
        logger.info("TreeNodes are structured - elapsed time: " + (System.currentTimeMillis() - s) + " ms");

        return treeNode;
    }

    public TreeNode createLazyTreeNode(long asccpId) {
        CCNode ccNode = nodeService.createLazyCCNode(asccpId);

        LazyTreeNodeBuilder lazyTreeNodeVisitor = new LazyTreeNodeBuilder();
        ccNode.accept(lazyTreeNodeVisitor);
        return lazyTreeNodeVisitor.getParent();
    }

    public void expand(NodeExpandEvent expandEvent) {
        DefaultTreeNode treeNode = (DefaultTreeNode) expandEvent.getTreeNode();
        LazyNode lazyNode = (LazyNode) treeNode.getData();
        if (!lazyNode.isFetched()) {
            lazyNode.fetch();

            LazyTreeNodeBuilder lazyTreeNodeBuilder = new LazyTreeNodeBuilder(treeNode);
            treeNode.setChildren(new ArrayList()); // clear children

            for (Node child : lazyNode.getChildren()) {
                ((CCNode) child).accept(lazyTreeNodeBuilder);
            }
        }
    }

    private class LazyTreeNodeBuilder implements CCNodeVisitor {

        private DefaultTreeNode parent;

        public TreeNode getParent() {
            return parent;
        }

        public LazyTreeNodeBuilder() {
            parent = new DefaultTreeNode();
        }

        public LazyTreeNodeBuilder(DefaultTreeNode parent) {
            this.parent = parent;
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
        public void visitBCCPNode(BCCPNode bccNode) {
            visitNode(bccNode, "BCCP");
        }

        private void visitNode(CCNode node, String type) {
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
    }
}
