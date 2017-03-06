package org.oagi.srt.web.jsf.beans.cc;

import org.oagi.srt.model.node.*;
import org.oagi.srt.repository.UserRepository;
import org.oagi.srt.repository.entity.*;
import org.oagi.srt.service.NodeService;
import org.oagi.srt.web.handler.UIHandler;
import org.oagi.srt.web.jsf.component.treenode.TreeNodeTypeNameResolver;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.oagi.srt.repository.entity.CoreComponentState.Published;

@Component
public abstract class BaseCoreComponentDetailBean extends UIHandler {

    @Autowired
    private NodeService nodeService;

    @Autowired
    private UserRepository userRepository;

    public User getOwnerUser(AggregateCoreComponent acc) {
        long ownerUserId = acc.getOwnerUserId();
        return userRepository.findOne(ownerUserId);
    }

    public Map<String, OagisComponentType> availableOagisComponentTypes(AggregateCoreComponent acc) {
        User owner = getOwnerUser(acc);

        Map<String, OagisComponentType> availableOagisComponentTypes = new LinkedHashMap();
        availableOagisComponentTypes.put("Base", OagisComponentType.Base);
        availableOagisComponentTypes.put("Semantics", OagisComponentType.Semantics);
        if (owner.isOagisDeveloperIndicator()) {
            availableOagisComponentTypes.put("Extension", OagisComponentType.Extension);
        }
        availableOagisComponentTypes.put("Semantic Group", OagisComponentType.SemanticGroup);

        return availableOagisComponentTypes;
    }

    public TreeNode createTreeNode(AggregateCoreComponent acc) {
        ACCNode accNode = nodeService.createCoreComponentTreeNode(acc);
        TreeNode root = new DefaultTreeNode();
        toTreeNode(accNode, root);
        return root;
    }

    public TreeNode createTreeNode(AssociationCoreComponentProperty asccp) {
        ASCCPNode asccpNode = nodeService.createCoreComponentTreeNode(asccp);
        TreeNode root = new DefaultTreeNode();
        toTreeNode(asccpNode, root);
        return root;
    }

    public TreeNode createTreeNode(BasicCoreComponentProperty bccp) {
        BCCPNode bccpNode = nodeService.createCoreComponentTreeNode(bccp);
        TreeNode root = new DefaultTreeNode();
        toTreeNode(bccpNode, root);
        return root;
    }

    TreeNode toTreeNode(CCNode node, TreeNode parent) {
        setNodeName(node);

        TreeNodeTypeNameResolver treeNodeTypeNameResolver = getTreeNodeTypeNameResolver(node);
        String type = treeNodeTypeNameResolver.getType();
        TreeNode treeNode = new DefaultTreeNode(type, node, parent);
        if (node.hasChild()) {
            new DefaultTreeNode(null, treeNode); // append a dummy child
        }
        return treeNode;
    }

    void setNodeName(CCNode node) {
        TreeNodeTypeNameResolver treeNodeTypeNameResolver = getTreeNodeTypeNameResolver(node);
        String name = treeNodeTypeNameResolver.getName();
        node.setAttribute("name", name);
    }

    TreeNodeTypeNameResolver getTreeNodeTypeNameResolver(CCNode node) {
        if (node instanceof ACCNode) {
            return new AggregateCoreComponentTreeNodeTypeNameResolver((ACCNode) node);
        } else if (node instanceof ASCCPNode) {
            return new AssociationCoreComponentPropertyTreeNodeTypeNameResolver((ASCCPNode) node);
        } else if (node instanceof BCCPNode) {
            return new BasicCoreComponentPropertyTreeNodeTypeNameResolver((BCCPNode) node);
        } else if (node instanceof BDTSCNode) {
            return new BusinessDataTypeSupplementaryComponentTreeNodeTypeNameResolver((BDTSCNode) node);
        } else {
            throw new IllegalStateException();
        }
    }

    protected AggregateCoreComponent getTargetAcc() {
        return null;
    }

    private class AggregateCoreComponentTreeNodeTypeNameResolver implements TreeNodeTypeNameResolver {

        private ACCNode node;

        public AggregateCoreComponentTreeNodeTypeNameResolver(ACCNode node) {
            this.node = node;
        }

        @Override
        public String getType() {
            AggregateCoreComponent acc = node.getAcc();
            AggregateCoreComponent targetAcc = getTargetAcc();
            String type;

            if (targetAcc != null && targetAcc.equals(acc)) {
                type = "ACC-Target";
            } else {
                type = "ACC";
            }
            return type;
        }

        @Override
        public String getName() {
            AggregateCoreComponent acc = node.getAcc();
            return acc.getDen();
        }
    }

    private class AssociationCoreComponentPropertyTreeNodeTypeNameResolver implements TreeNodeTypeNameResolver {

        private ASCCPNode node;

        public AssociationCoreComponentPropertyTreeNodeTypeNameResolver(ASCCPNode node) {
            this.node = node;
        }

        @Override
        public String getType() {
            return "ASCCP";
        }

        @Override
        public String getName() {
            AssociationCoreComponentProperty asccp = node.getAsccp();
            return asccp.getPropertyTerm();
        }
    }

    private class BasicCoreComponentPropertyTreeNodeTypeNameResolver implements TreeNodeTypeNameResolver {

        private BCCPNode node;

        public BasicCoreComponentPropertyTreeNodeTypeNameResolver(BCCPNode node) {
            this.node = node;
        }

        @Override
        public String getType() {
            BasicCoreComponent bcc = node.getBcc();
            String type;
            if (bcc != null) {
                type = "BCCP" + (bcc.getEntityType() == BasicCoreComponentEntityType.Attribute ? "-Attribute" : "");
            } else {
                type = "BCCP";
            }
            return type;
        }

        @Override
        public String getName() {
            BasicCoreComponentProperty bccp = node.getBccp();
            return bccp.getPropertyTerm();
        }
    }

    private class BusinessDataTypeSupplementaryComponentTreeNodeTypeNameResolver implements TreeNodeTypeNameResolver {

        private BDTSCNode node;

        public BusinessDataTypeSupplementaryComponentTreeNodeTypeNameResolver(
                BDTSCNode node) {
            this.node = node;
        }

        @Override
        public String getType() {
            return "BDTSC";
        }

        @Override
        public String getName() {
            DataTypeSupplementaryComponent bdtSc = node.getBdtSc();
            String name = bdtSc.getPropertyTerm() + ". " + bdtSc.getRepresentationTerm();
            return name;
        }
    }

    public void expand(NodeExpandEvent expandEvent) {
        DefaultTreeNode treeNode = (DefaultTreeNode) expandEvent.getTreeNode();
        CCNode coreComponentTreeNode = (CCNode) treeNode.getData();
        Boolean expanded = (Boolean) coreComponentTreeNode.getAttribute("expanded");
        if (expanded == null || expanded == false) {
            if (coreComponentTreeNode.hasChild()) {
                treeNode.setChildren(new ArrayList()); // clear children

                for (CCNode child : coreComponentTreeNode.getChildren()) {
                    toTreeNode(child, treeNode);
                }
            }
            coreComponentTreeNode.setAttribute("expanded", true);
        }
    }

    void updateState(TreeNode treeNode, CoreComponentState state, User requester) {
        long lastUpdatedBy = requester.getAppUserId();
        Object data = treeNode.getData();

        if (data instanceof ACCNode) {
            ACCNode accNode = ((ACCNode) data);
            AggregateCoreComponent acc = accNode.getAcc();
            if (acc != null && acc.getState() != Published) {
                acc.setState(state);
                acc.setLastUpdatedBy(lastUpdatedBy);
                acc.afterLoaded();
            }
        } else if (data instanceof ASCCPNode) {
            ASCCPNode asccpNode = ((ASCCPNode) data);
            AssociationCoreComponent ascc = asccpNode.getAscc();
            if (ascc != null && ascc.getState() != Published) {
                ascc.setState(state);
                ascc.setLastUpdatedBy(lastUpdatedBy);
                ascc.afterLoaded();
            }

            AssociationCoreComponentProperty asccp = asccpNode.getAsccp();
            if (asccp != null && asccp.getState() != Published) {
                asccp.setState(state);
                asccp.setLastUpdatedBy(lastUpdatedBy);
                asccp.afterLoaded();
            }
        } else if (data instanceof BCCPNode) {
            BCCPNode bccpNode = ((BCCPNode) data);

            BasicCoreComponent bcc = bccpNode.getBcc();
            if (bcc != null && bcc.getState() != Published) {
                bcc.setState(state);
                bcc.setLastUpdatedBy(lastUpdatedBy);
                bcc.afterLoaded();
            }

            BasicCoreComponentProperty bccp = bccpNode.getBccp();
            if (bccp != null && bccp.getState() != Published) {
                bccp.setState(state);
                bccp.setLastUpdatedBy(lastUpdatedBy);
                bccp.afterLoaded();
            }
        }

        for (TreeNode child : treeNode.getChildren()) {
            updateState(child, state, requester);
        }
    }
}
