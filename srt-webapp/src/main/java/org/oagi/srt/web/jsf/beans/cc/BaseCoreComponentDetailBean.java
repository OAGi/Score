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

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.util.*;

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

    public TreeNode createTreeNode(AggregateCoreComponent acc, boolean enableShowingGroup) {
        ACCNode accNode = nodeService.createCoreComponentTreeNode(acc, enableShowingGroup);
        TreeNode root = new DefaultTreeNode();
        toTreeNode(accNode, root);
        return root;
    }

    public TreeNode createTreeNode(AssociationCoreComponentProperty asccp, boolean enableShowingGroup) {
        ASCCPNode asccpNode = nodeService.createCoreComponentTreeNode(asccp, enableShowingGroup);
        TreeNode root = new DefaultTreeNode();
        toTreeNode(asccpNode, root);
        return root;
    }

    public DefaultTreeNode createTreeNode(BasicCoreComponentProperty bccp, boolean enableShowingGroup) {
        BCCPNode bccpNode = nodeService.createCoreComponentTreeNode(bccp, enableShowingGroup);
        DefaultTreeNode root = new DefaultTreeNode();
        toTreeNode(bccpNode, root);
        return root;
    }

    TreeNode toTreeNode(CCNode node, TreeNode parent) {
        setNodeName(node);

        TreeNodeTypeNameResolver treeNodeTypeNameResolver = getTreeNodeTypeNameResolver(node);
        String type = treeNodeTypeNameResolver.getType();
        TreeNode treeNode = new DefaultTreeNode(type, node, parent);
        if (node.hasChild() || ((node instanceof ACCNode) && ((ACCNode) node).getBase() != null)) {
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
                boolean isGroup = nodeService.isGroup(acc);
                type = "ACC" + (isGroup ? "-Group" : "");
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
            boolean isGroup = nodeService.isGroup(node.getType().getAcc());
            return "ASCCP" + (isGroup ? "-Group" : "");
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
        CCNode ccNode = (CCNode) treeNode.getData();
        Boolean expanded = (Boolean) ccNode.getAttribute("expanded");
        if (expanded == null || expanded == false) {
            if (ccNode.hasChild() || ((ccNode instanceof ACCNode) && ((ACCNode) ccNode).getBase() != null)) {
                clearChildren(treeNode);
            }

            if (ccNode instanceof ACCNode) {
                ACCNode accNode = (ACCNode) ccNode;
                ACCNode baseAccNode = accNode.getBase();
                if (baseAccNode != null) {
                    toTreeNode(baseAccNode, treeNode);
                }
            }
            if (ccNode.hasChild()) {
                for (CCNode child : ccNode.getChildren()) {
                    toTreeNode(child, treeNode);
                }
            }
            ccNode.setAttribute("expanded", true);
        }
    }

    public void expand(TreeNode node) {
        DefaultTreeNode treeNode = (DefaultTreeNode) node;
        CCNode ccNode = (CCNode) treeNode.getData();
        Boolean expanded = (Boolean) ccNode.getAttribute("expanded");
        if (expanded == null || expanded == false) {
            if (ccNode.hasChild() || ((ccNode instanceof ACCNode) && ((ACCNode) ccNode).getBase() != null)) {
                clearChildren(treeNode);
            }

            if (ccNode instanceof ACCNode) {
                ACCNode accNode = (ACCNode) ccNode;
                ACCNode baseAccNode = accNode.getBase();
                if (baseAccNode != null) {
                    toTreeNode(baseAccNode, treeNode);
                }
            }
            if (ccNode.hasChild()) {
                for (CCNode child : ccNode.getChildren()) {
                    toTreeNode(child, treeNode);
                }
            }
            ccNode.setAttribute("expanded", true);
        }
    }

    public void copyExpandedState(TreeNode source, TreeNode target) {
        if (!source.isLeaf() && !target.isLeaf()) { // TODO: MIRO check if this cause problem with setting leaf node as selected after refresh
            target.setExpanded(source.isExpanded());
            target.setSelected(source.isSelected());
            if (source.isExpanded()) {
                expand(target);
            }

            for (TreeNode sourceChild : source.getChildren()) {
                if (sourceChild.isExpanded()) {
                    TreeNode targetChild = findChildNodeById(target, ((SRTNode) sourceChild.getData()).getId());
                    if (targetChild != null) {
                        copyExpandedState(sourceChild, targetChild);
                    }
                }
            }
        }
    }

    public TreeNode findChildNodeById(TreeNode parent, String id) {
        for (TreeNode tn : parent.getChildren()) {
            SRTNode srtNode = (SRTNode) tn.getData();
            if (srtNode != null) {
                String srtNodeId = srtNode.getId();
                if (srtNodeId.equals(id)) {
                    return tn;
                }
            }
        }
        return null;
    }

    public TreeNode findChildNodeAnywhereById(TreeNode parent, String id) {
        TreeNode node = findChildNodeById(parent, id);
        if (node == null) {
            for (TreeNode childNode : parent.getChildren()) {
                node = findChildNodeAnywhereById(childNode, id);
            }
        }
        return node;
    }

    private void clearChildren(DefaultTreeNode treeNode) {
        if (!treeNode.getChildren().isEmpty()) {
            treeNode.setChildren(new ArrayList());
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

    public void validate(CCNode ccNode) {
        try {
            ccNode.validate();
        } catch (Throwable t) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", t.getMessage()));
            throw t;
        }
    }

    public void reorderTreeNode(TreeNode treeNode) {
        List<TreeNode> children = treeNode.getChildren();
        Collections.sort(children, (a, b) -> {
            int s1 = getSeqKey(a);
            int s2 = getSeqKey(b);
            int compareTo = s1 - s2;
            if (compareTo != 0) {
                return compareTo;
            } else {
                if (a.getData() instanceof BDTSCNode || b.getData() instanceof BDTSCNode) {
                    return 0;
                } else {
                    Date aTs = getCreationTimestamp(a);
                    Date bTs = getCreationTimestamp(b);
                    return aTs.compareTo(bTs);
                }
            }
        });

        /*
         * This implementations bring from {@code org.primefaces.model.TreeNodeChildren}
         * to clarify children's order for node selection
         */
        for (int i = 0, len = children.size(); i < len; ++i) {
            TreeNode child = children.get(i);
            String childRowKey = (treeNode.getParent() == null) ? String.valueOf(i) : treeNode.getRowKey() + "_" + i;
            child.setRowKey(childRowKey);
        }

        for (TreeNode child : children) {
            reorderTreeNode(child);
        }
    }

    private int getSeqKey(TreeNode treeNode) {
        Object data = treeNode.getData();
        if (data instanceof ASCCPNode) {
            return ((ASCCPNode) data).getAscc().getSeqKey();
        } else if (data instanceof BCCPNode) {
            return ((BCCPNode) data).getBcc().getSeqKey();
        }
        return -1;
    }

    private Date getCreationTimestamp(TreeNode treeNode) {
        Object data = treeNode.getData();
        if (data instanceof ASCCPNode) {
            return ((ASCCPNode) data).getAsccp().getCreationTimestamp();
        } else if (data instanceof BCCPNode) {
            return ((BCCPNode) data).getBccp().getCreationTimestamp();
        } else if (data instanceof ACCNode) {
            return ((ACCNode) data).getAcc().getCreationTimestamp();
        }
        return null;
    }
}
