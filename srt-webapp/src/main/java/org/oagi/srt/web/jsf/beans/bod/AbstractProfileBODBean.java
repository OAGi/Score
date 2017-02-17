package org.oagi.srt.web.jsf.beans.bod;

import org.oagi.srt.common.util.Utility;
import org.oagi.srt.model.treenode.AssociationBusinessInformationEntityPropertyTreeNode;
import org.oagi.srt.model.treenode.BasicBusinessInformationEntityPropertyTreeNode;
import org.oagi.srt.model.treenode.BasicBusinessInformationEntitySupplementaryComponentTreeNode;
import org.oagi.srt.model.treenode.BusinessInformationEntityTreeNode;
import org.oagi.srt.repository.entity.*;
import org.oagi.srt.service.CoreComponentService;
import org.oagi.srt.service.TreeNodeService;
import org.oagi.srt.web.handler.UIHandler;
import org.oagi.srt.web.jsf.component.treenode.TreeNodeTypeNameResolver;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.util.ArrayList;

@Component
abstract class AbstractProfileBODBean extends UIHandler {

    @Autowired
    private TreeNodeService treeNodeService;

    @Autowired
    private CoreComponentService coreComponentService;

    private TreeNode treeNode;

    TreeNode createTreeNode(AssociationCoreComponentProperty asccp, BusinessContext bixCtx) {
        AssociationBusinessInformationEntityPropertyTreeNode topLevelNode =
                treeNodeService.createBusinessInformationEntityTreeNode(asccp, bixCtx);
        return createTreeNode(topLevelNode);
    }

    TreeNode createTreeNode(TopLevelAbie topLevelAbie) {
        AssociationBusinessInformationEntityPropertyTreeNode topLevelNode =
                treeNodeService.createBusinessInformationEntityTreeNode(topLevelAbie);
        return createTreeNode(topLevelNode);
    }

    private TreeNode createTreeNode(AssociationBusinessInformationEntityPropertyTreeNode topLevelNode) {
        topLevelNode.setAttribute("isTopLevel", true);

        TreeNode root = new DefaultTreeNode();
        toTreeNode(topLevelNode, root);
        setTreeNode(root);

        return root;
    }

    public TreeNode getTreeNode() {
        return treeNode;
    }

    public void setTreeNode(TreeNode treeNode) {
        this.treeNode = treeNode;
    }

    public AssociationBusinessInformationEntityPropertyTreeNode getTopLevelNode() {
        TreeNode treeNode = getTreeNode();
        return (AssociationBusinessInformationEntityPropertyTreeNode) treeNode.getChildren().get(0).getData();
    }

    public void expand(NodeExpandEvent expandEvent) {
        DefaultTreeNode treeNode = (DefaultTreeNode) expandEvent.getTreeNode();

        BusinessInformationEntityTreeNode bieNode = (BusinessInformationEntityTreeNode) treeNode.getData();
        Boolean expanded = (Boolean) bieNode.getAttribute("expanded");
        if (expanded == null || expanded == false) {
            if (bieNode.hasChild()) {
                treeNode.setChildren(new ArrayList()); // clear children

                for (BusinessInformationEntityTreeNode child : bieNode.getChildren()) {
                    toTreeNode(child, treeNode);
                }
            }
            bieNode.setAttribute("expanded", true);
        }
    }

    TreeNode toTreeNode(BusinessInformationEntityTreeNode node, TreeNode parent) {
        TreeNodeTypeNameResolver typeNameResolver = getTreeNodeTypeNameResolver(node);
        String name = typeNameResolver.getName();
        node.setAttribute("name", name);

        String type = typeNameResolver.getType();
        TreeNode treeNode = new DefaultTreeNode(type, node, parent);
        if (node.hasChild()) {
            new DefaultTreeNode(null, treeNode);
        }
        return treeNode;
    }

    public TreeNodeTypeNameResolver getTreeNodeTypeNameResolver(BusinessInformationEntityTreeNode node) {
        if (node instanceof AssociationBusinessInformationEntityPropertyTreeNode) {
            return new AssociationBusinessInformationEntityPropertyTreeNodeTypeNameResolver(
                    (AssociationBusinessInformationEntityPropertyTreeNode) node);
        } else if (node instanceof BasicBusinessInformationEntityPropertyTreeNode) {
            return new BasicBusinessInformationEntityPropertyTreeNodeTypeNameResolver(
                    (BasicBusinessInformationEntityPropertyTreeNode) node);
        } else if (node instanceof BasicBusinessInformationEntitySupplementaryComponentTreeNode) {
            return new BasicBusinessInformationEntitySupplementaryComponentTreeNodeTypeNameResolver(
                    (BasicBusinessInformationEntitySupplementaryComponentTreeNode) node);
        } else {
            throw new IllegalStateException();
        }
    }

    private class AssociationBusinessInformationEntityPropertyTreeNodeTypeNameResolver implements TreeNodeTypeNameResolver {

        private AssociationBusinessInformationEntityPropertyTreeNode asbiepNode;

        public AssociationBusinessInformationEntityPropertyTreeNodeTypeNameResolver(
                AssociationBusinessInformationEntityPropertyTreeNode asbiepNode) {
            this.asbiepNode = asbiepNode;
        }

        @Override
        public String getType() {
            Boolean isTopLevel = (Boolean) asbiepNode.getAttribute("isTopLevel");
            if (isTopLevel != null && isTopLevel) {
                return "ABIE";
            }
            return ("Extension".equals(getName())) ? "ASBIE-Extension" : "ASBIE";
        }

        @Override
        public String getName() {
            return asbiepNode.getAssociationCoreComponentProperty().getPropertyTerm();
        }
    }

    private class BasicBusinessInformationEntityPropertyTreeNodeTypeNameResolver implements TreeNodeTypeNameResolver {

        private BasicBusinessInformationEntityPropertyTreeNode bbiepNode;

        public BasicBusinessInformationEntityPropertyTreeNodeTypeNameResolver(
                BasicBusinessInformationEntityPropertyTreeNode bbiepNode) {
            this.bbiepNode = bbiepNode;
        }

        @Override
        public String getType() {
            BasicBusinessInformationEntity bbie = bbiepNode.getBasicBusinessInformationEntity();
            boolean isAttribute = (bbie.getSeqKey() == 0);
            return (isAttribute ? "BBIE-Attribute" : "BBIE");
        }

        @Override
        public String getName() {
            return bbiepNode.getBasicCoreComponentProperty().getPropertyTerm();
        }
    }

    private class BasicBusinessInformationEntitySupplementaryComponentTreeNodeTypeNameResolver implements TreeNodeTypeNameResolver {

        private BasicBusinessInformationEntitySupplementaryComponentTreeNode bbieScNode;

        public BasicBusinessInformationEntitySupplementaryComponentTreeNodeTypeNameResolver(
                BasicBusinessInformationEntitySupplementaryComponentTreeNode bbieScNode) {
            this.bbieScNode = bbieScNode;
        }

        @Override
        public String getType() {
            return "BBIESC";
        }

        @Override
        public String getName() {
            DataTypeSupplementaryComponent bdtSc = bbieScNode.getBusinessDataTypeSupplementaryComponent();
            if (bdtSc.getRepresentationTerm().equalsIgnoreCase("Text") ||
                    bdtSc.getPropertyTerm().contains(bdtSc.getRepresentationTerm())) {
                return Utility.spaceSeparator(bdtSc.getPropertyTerm());
            } else {
                return Utility.spaceSeparator(bdtSc.getPropertyTerm().concat(bdtSc.getRepresentationTerm()));
            }
        }
    }

    public void validate(BusinessInformationEntityTreeNode bieNode) {
        try {
            bieNode.validate();
        } catch (Throwable t) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", t.getMessage()));
            throw t;
        }
    }
}
