package org.oagi.srt.web.jsf.beans.bod;

import org.oagi.srt.common.util.Utility;
import org.oagi.srt.model.treenode.AssociationBusinessInformationEntityPropertyTreeNode;
import org.oagi.srt.model.treenode.BasicBusinessInformationEntityPropertyTreeNode;
import org.oagi.srt.model.treenode.BasicBusinessInformationEntitySupplementaryComponentTreeNode;
import org.oagi.srt.model.treenode.BusinessInformationEntityTreeNode;
import org.oagi.srt.repository.entity.BasicBusinessInformationEntity;
import org.oagi.srt.repository.entity.DataTypeSupplementaryComponent;
import org.oagi.srt.web.handler.UIHandler;
import org.oagi.srt.web.jsf.component.treenode.TreeNodeTypeNameResolver;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

abstract class AbstractProfileBODBean extends UIHandler {

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
}
