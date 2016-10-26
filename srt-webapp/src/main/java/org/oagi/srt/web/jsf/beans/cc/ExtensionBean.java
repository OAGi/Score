package org.oagi.srt.web.jsf.beans.cc;

import org.oagi.srt.model.CCNode;
import org.oagi.srt.model.CCNodeVisitor;
import org.oagi.srt.model.LazyNode;
import org.oagi.srt.model.Node;
import org.oagi.srt.model.cc.ACCNode;
import org.oagi.srt.model.cc.ASCCPNode;
import org.oagi.srt.model.cc.BCCPNode;
import org.oagi.srt.model.cc.impl.BaseASCCPNode;
import org.oagi.srt.repository.AggregateCoreComponentRepository;
import org.oagi.srt.repository.AssociationCoreComponentPropertyRepository;
import org.oagi.srt.repository.AssociationCoreComponentRepository;
import org.oagi.srt.repository.entity.*;
import org.oagi.srt.service.ExtensionService;
import org.oagi.srt.service.NodeService;
import org.oagi.srt.web.handler.UIHandler;
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
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.Map;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
@Transactional(readOnly = true)
public class ExtensionBean extends UIHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ExtensionService extensionService;

    @Autowired
    private NodeService nodeService;

    @Autowired
    private AggregateCoreComponentRepository accRepository;

    @Autowired
    private AssociationCoreComponentRepository asccRepository;

    @Autowired
    private AssociationCoreComponentPropertyRepository asccpRepository;

    private AggregateCoreComponent targetAcc;
    private AssociationCoreComponentProperty rootAsccp;
    private AggregateCoreComponent userExtensionAcc;

    private TreeNode treeNode;
    private TreeNode selectedTreeNode;

    private DataType selectedBdt;

    @PostConstruct
    public void init() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, String> requestParameterMap = externalContext.getRequestParameterMap();

        String accId = requestParameterMap.get("accId");
        AggregateCoreComponent targetAcc = accRepository.findOne(Long.parseLong(accId));
        setTargetAcc(targetAcc);

        TreeNode treeNode = createLazyTreeNode(targetAcc);
        setTreeNode(treeNode);

        String rootAsccpId = requestParameterMap.get("rootAsccpId");
        setRootAsccp(asccpRepository.findOne(Long.parseLong(rootAsccpId)));

        setUserExtensionAcc(extensionService.findUserExtensionAcc(targetAcc));
    }

    public AggregateCoreComponent getTargetAcc() {
        return targetAcc;
    }

    public void setTargetAcc(AggregateCoreComponent targetAcc) {
        this.targetAcc = targetAcc;
    }

    public AssociationCoreComponentProperty getRootAsccp() {
        return rootAsccp;
    }

    public void setRootAsccp(AssociationCoreComponentProperty rootAsccp) {
        this.rootAsccp = rootAsccp;
    }

    public AggregateCoreComponent getUserExtensionAcc() {
        return userExtensionAcc;
    }

    public void setUserExtensionAcc(AggregateCoreComponent userExtensionAcc) {
        this.userExtensionAcc = userExtensionAcc;
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

    public TreeNode createLazyTreeNode(AggregateCoreComponent acc) {
        CCNode ccNode = nodeService.createLazyCCNode(acc);
        return createLazyTreeNode(ccNode);
    }

    private TreeNode createLazyTreeNode(CCNode ccNode) {
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
            visitNode(asccpNode);
        }

        @Override
        public void visitACCNode(ACCNode accNode) {
            AggregateCoreComponent acc = accNode.getAcc();
            if (acc.equals(targetAcc)) {
                visitNode(accNode, accNode.getType() + "-Extension");
            } else {
                visitNode(accNode);
            }
        }

        @Override
        public void visitBCCPNode(BCCPNode bccNode) {
            visitNode(bccNode);
        }

        private void visitNode(CCNode node) {
            visitNode(node, node.getType());
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

    public void prepareAppendAscc() {
        ASCCPNode asccpNode = new BaseASCCPNode()
        TreeNode treeNode = new DefaultTreeNode();

    }

    public void appendAscc() {
        AggregateCoreComponent ueAcc = getUserExtensionAcc();
        User user = loadAuthentication();
        AssociationCoreComponent ueAscc = extensionService.appendAsccTo(ueAcc, user);
    }

    public void prepareAppendBcc() {

    }

    public void appendBcc() {
        AggregateCoreComponent ueAcc = getUserExtensionAcc();
        User user = loadAuthentication();
        BasicCoreComponent ueBcc = extensionService.appendBccTo(ueAcc, user, selectedBdt);
    }
}
