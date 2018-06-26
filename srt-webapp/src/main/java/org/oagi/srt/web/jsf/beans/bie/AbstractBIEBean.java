package org.oagi.srt.web.jsf.beans.bie;

import org.oagi.srt.common.util.Utility;
import org.oagi.srt.model.node.*;
import org.oagi.srt.repository.entity.*;
import org.oagi.srt.service.BusinessInformationEntityService;
import org.oagi.srt.service.NodeService;
import org.oagi.srt.web.handler.UIHandler;
import org.oagi.srt.web.jsf.component.treenode.TreeNodeTypeNameResolver;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
abstract class AbstractBIEBean extends UIHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private NodeService nodeService;

    @Autowired
    private BusinessInformationEntityService bieService;

    private TreeNode treeNode;
    private boolean canUpdate;

    private TreeNode selectedTreeNode;
    private String selectedCodeListName;

    TreeNode createTreeNode(AssociationCoreComponentProperty asccp, Release release, BusinessContext bixCtx) {
        ASBIEPNode topLevelNode = nodeService.createBusinessInformationEntityTreeNode(asccp, release, bixCtx);
        return createTreeNode(topLevelNode);
    }

    TreeNode createTreeNode(TopLevelAbie topLevelAbie) {
        return createTreeNode(topLevelAbie, false);
    }

    TreeNode createTreeNode(TopLevelAbie topLevelAbie, boolean hideUnusedNodes) {
        ASBIEPNode topLevelNode = nodeService.createBusinessInformationEntityTreeNode(topLevelAbie, hideUnusedNodes);
        return createTreeNode(topLevelNode);
    }

    private TreeNode createTreeNode(ASBIEPNode topLevelNode) {
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

    public ASBIEPNode getTopLevelNode() {
        TreeNode treeNode = getTreeNode();
        return (ASBIEPNode) treeNode.getChildren().get(0).getData();
    }

    public boolean canUpdate() {
        return canUpdate;
    }

    public void onChangeData(BIENode bieTreeNode) {
        try {
            validate(bieTreeNode);
        } catch (Throwable t) {
            canUpdate = false;
            throw t;
        }

        AtomicInteger dirtyCount = new AtomicInteger();
        getTopLevelNode().accept(new BIENodeVisitor() {
            @Override
            public void visit(ABIENode abieNode) {
                AggregateBusinessInformationEntity abie = abieNode.getAbie();
                if (abie != null && abie.isDirty()) {
                    dirtyCount.incrementAndGet();
                }
            }

            @Override
            public void visit(ASBIEPNode asbiepNode) {
                AssociationBusinessInformationEntity asbie = asbiepNode.getAsbie();
                if (asbie != null && asbie.isDirty()) {
                    dirtyCount.incrementAndGet();
                }
                AssociationBusinessInformationEntityProperty asbiep = asbiepNode.getAsbiep();
                if (asbiep != null && asbiep.isDirty()) {
                    dirtyCount.incrementAndGet();
                }
            }

            @Override
            public void visit(BBIEPNode bbiepNode) {
                BasicBusinessInformationEntity bbie = bbiepNode.getBbie();
                if (bbie != null && bbie.isDirty()) {
                    dirtyCount.incrementAndGet();
                }
                BasicBusinessInformationEntityProperty bbiep = bbiepNode.getBbiep();
                if (bbiep != null && bbiep.isDirty()) {
                    dirtyCount.incrementAndGet();
                }
            }

            @Override
            public void visit(BBIESCNode bbieScNode) {
                BasicBusinessInformationEntitySupplementaryComponent bbieSc = bbieScNode.getBbieSc();
                if (bbieSc != null && bbieSc.isDirty()) {
                    dirtyCount.incrementAndGet();
                }
            }
        });

        canUpdate = (dirtyCount.get() > 0) ? true : false;
    }

    public void validate(BIENode bieNode) {
        try {
            bieNode.validate();
        } catch (Throwable t) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", t.getMessage()));
            throw t;
        }
    }

    public void expand(NodeExpandEvent expandEvent) {
        long s = System.currentTimeMillis();
        try {
            DefaultTreeNode treeNode = (DefaultTreeNode) expandEvent.getTreeNode();

            BIENode bieNode = (BIENode) treeNode.getData();
            Boolean expanded = (Boolean) bieNode.getAttribute("expanded");
            if (expanded == null || expanded == false) {
                if (bieNode.hasChild()) {
                    treeNode.setChildren(new ArrayList()); // clear children

                    for (BIENode child : bieNode.getChildren()) {
                        toTreeNode(child, treeNode);
                    }
                }
                bieNode.setAttribute("expanded", true);
            }
        } finally {
            logger.debug("Expanding tree node took " + (System.currentTimeMillis() - s) + " ms");
        }
    }

    TreeNode toTreeNode(BIENode node, TreeNode parent) {
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

    public TreeNodeTypeNameResolver getTreeNodeTypeNameResolver(BIENode node) {
        if (node instanceof ASBIEPNode) {
            return new AssociationBusinessInformationEntityPropertyTreeNodeTypeNameResolver(
                    (ASBIEPNode) node);
        } else if (node instanceof BBIEPNode) {
            return new BasicBusinessInformationEntityPropertyTreeNodeTypeNameResolver(
                    (BBIEPNode) node);
        } else if (node instanceof BBIESCNode) {
            return new BasicBusinessInformationEntitySupplementaryComponentTreeNodeTypeNameResolver(
                    (BBIESCNode) node);
        } else {
            throw new IllegalStateException();
        }
    }

    private class AssociationBusinessInformationEntityPropertyTreeNodeTypeNameResolver implements TreeNodeTypeNameResolver {

        private ASBIEPNode asbiepNode;

        public AssociationBusinessInformationEntityPropertyTreeNodeTypeNameResolver(
                ASBIEPNode asbiepNode) {
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
            return asbiepNode.getAsccp().getPropertyTerm();
        }
    }

    private class BasicBusinessInformationEntityPropertyTreeNodeTypeNameResolver implements TreeNodeTypeNameResolver {

        private BBIEPNode bbiepNode;

        public BasicBusinessInformationEntityPropertyTreeNodeTypeNameResolver(
                BBIEPNode bbiepNode) {
            this.bbiepNode = bbiepNode;
        }

        @Override
        public String getType() {
            BasicBusinessInformationEntity bbie = bbiepNode.getBbie();
            boolean isAttribute = (bbie.getSeqKey() == 0);
            return (isAttribute ? "BBIE-Attribute" : "BBIE");
        }

        @Override
        public String getName() {
            return bbiepNode.getBccp().getPropertyTerm();
        }
    }

    private class BasicBusinessInformationEntitySupplementaryComponentTreeNodeTypeNameResolver implements TreeNodeTypeNameResolver {

        private BBIESCNode bbieScNode;

        public BasicBusinessInformationEntitySupplementaryComponentTreeNodeTypeNameResolver(
                BBIESCNode bbieScNode) {
            this.bbieScNode = bbieScNode;
        }

        @Override
        public String getType() {
            return "BBIESC";
        }

        @Override
        public String getName() {
            DataTypeSupplementaryComponent bdtSc = bbieScNode.getBdtSc();
            if (bdtSc.getRepresentationTerm().equalsIgnoreCase("Text") ||
                    bdtSc.getPropertyTerm().contains(bdtSc.getRepresentationTerm())) {
                return Utility.spaceSeparator(bdtSc.getPropertyTerm());
            } else {
                return Utility.spaceSeparator(bdtSc.getPropertyTerm().concat(bdtSc.getRepresentationTerm()));
            }
        }
    }

    public TreeNode getSelectedTreeNode() {
        return selectedTreeNode;
    }

    public void setSelectedTreeNode(TreeNode selectedTreeNode) {
        this.selectedTreeNode = selectedTreeNode;
    }

    /*
     * handle BBIE Type
     */
    public Map<BasicBusinessInformationEntityRestrictionType, BasicBusinessInformationEntityRestrictionType>
    getAvailablePrimitiveRestrictions(BBIEPNode node) {
        return bieService.getAvailablePrimitiveRestrictions(node);
    }

    private BBIEPNode getSelectedBasicBusinessInformationEntityPropertyTreeNode() {
        TreeNode treeNode = getSelectedTreeNode();
        Object data = treeNode.getData();
        if (!(data instanceof BBIEPNode)) {
            return null;
        }
        return (BBIEPNode) data;
    }

    public String getBbieXbtName() {
        return bieService.getBdtPrimitiveRestrictionName(getSelectedBasicBusinessInformationEntityPropertyTreeNode());
    }

    public void setBbieXbtName(String name) {
        bieService.setBdtPrimitiveRestriction(getSelectedBasicBusinessInformationEntityPropertyTreeNode(), name);
    }

    public void onSelectBbieXbtName(SelectEvent event) {
        setBbieXbtName(event.getObject().toString());
        onChangeData(getSelectedBasicBusinessInformationEntityPropertyTreeNode());
    }

    public List<String> completeInputForBbieXbt(String query) {
        BBIEPNode node = getSelectedBasicBusinessInformationEntityPropertyTreeNode();
        Map<String, BusinessDataTypePrimitiveRestriction> bdtPrimitiveRestrictions =
                bieService.getBdtPrimitiveRestrictions(node);
        if (StringUtils.isEmpty(query)) {
            return new ArrayList(bdtPrimitiveRestrictions.keySet());
        } else {
            return bdtPrimitiveRestrictions.keySet().stream()
                    .filter(e -> e.toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
        }
    }

    public String getBbieCodeListName() {
        return bieService.getCodeListName(getSelectedBasicBusinessInformationEntityPropertyTreeNode());
    }

    public void setBbieCodeListName(String name) {
        BBIEPNode node = getSelectedBasicBusinessInformationEntityPropertyTreeNode();
        Map<String, CodeList> codeListMap = bieService.getCodeLists(node);
        CodeList codeList = codeListMap.get(name);
        if (codeList != null) {
            node.getBbie().setCodeListId(codeList.getCodeListId());
        }
    }

    public void onSelectBbieCodeListName(SelectEvent event) {
        setBbieCodeListName(event.getObject().toString());
        onChangeData(getSelectedBasicBusinessInformationEntityPropertyTreeNode());
    }

    public List<String> completeInputForBbieCodeList(String query) {
        BBIEPNode node = getSelectedBasicBusinessInformationEntityPropertyTreeNode();
        Map<String, CodeList> codeLists = bieService.getCodeLists(node);
        if (StringUtils.isEmpty(query)) {
            return new ArrayList(codeLists.keySet());
        } else {
            return codeLists.keySet().stream()
                    .filter(e -> e.toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
        }
    }

    public String getBbieAgencyIdListName() {
        return bieService.getBbieAgencyIdListName(getSelectedBasicBusinessInformationEntityPropertyTreeNode());
    }

    public void setBbieAgencyIdListName(String name) {
        BBIEPNode node = getSelectedBasicBusinessInformationEntityPropertyTreeNode();
        Map<String, AgencyIdList> agencyIdListMap = bieService.getAgencyIdListIds(node);
        AgencyIdList agencyIdList = agencyIdListMap.get(name);
        if (agencyIdList != null) {
            node.getBbie().setAgencyIdListId(agencyIdList.getAgencyIdListId());
        }
    }

    public void onSelectBbieAgencyIdListName(SelectEvent event) {
        setBbieAgencyIdListName(event.getObject().toString());
        onChangeData(getSelectedBasicBusinessInformationEntityPropertyTreeNode());
    }

    public List<String> completeInputForBbieAgencyIdList(String query) {
        BBIEPNode node = getSelectedBasicBusinessInformationEntityPropertyTreeNode();
        Map<String, AgencyIdList> agencyIdListMap = bieService.getAgencyIdListIds(node);
        if (StringUtils.isEmpty(query)) {
            return new ArrayList(agencyIdListMap.keySet());
        } else {
            return agencyIdListMap.keySet().stream()
                    .filter(e -> e.toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
        }
    }

    /*
     * handle BBIESC Type
     */
    public Map<BasicBusinessInformationEntityRestrictionType, BasicBusinessInformationEntityRestrictionType> getAvailableScPrimitiveRestrictions(BBIESCNode node) {
        return bieService.getAvailablePrimitiveRestrictions(node);
    }

    private BBIESCNode getSelectedBasicBusinessInformationEntitySupplementaryComponentTreeNode() {
        TreeNode treeNode = getSelectedTreeNode();
        Object data = treeNode.getData();
        if (!(data instanceof BBIESCNode)) {
            return null;
        }
        return (BBIESCNode) data;
    }

    public String getBbieScXbtName() {
        return bieService.getBdtScPrimitiveRestrictionName(getSelectedBasicBusinessInformationEntitySupplementaryComponentTreeNode());
    }

    public void setBbieScXbtName(String name) {
        bieService.setBdtScPrimitiveRestriction(getSelectedBasicBusinessInformationEntitySupplementaryComponentTreeNode(), name);
    }

    public void onSelectBbieScXbtName(SelectEvent event) {
        setBbieScXbtName(event.getObject().toString());
        onChangeData(getSelectedBasicBusinessInformationEntitySupplementaryComponentTreeNode());
    }

    public List<String> completeInputForBbieScXbt(String query) {
        BBIESCNode node = getSelectedBasicBusinessInformationEntitySupplementaryComponentTreeNode();
        Map<String, BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtScPrimitiveRestrictions =
                bieService.getBdtScPrimitiveRestrictions(node);
        if (StringUtils.isEmpty(query)) {
            return new ArrayList(bdtScPrimitiveRestrictions.keySet());
        } else {
            return bdtScPrimitiveRestrictions.keySet().stream()
                    .filter(e -> e.toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
        }
    }

    public String getBbieScCodeListName() {
        return bieService.getCodeListName(getSelectedBasicBusinessInformationEntitySupplementaryComponentTreeNode());
    }

    public void setBbieScCodeListName(String name) {
        BBIESCNode node = getSelectedBasicBusinessInformationEntitySupplementaryComponentTreeNode();
        Map<String, CodeList> codeListMap = bieService.getCodeLists(node);
        CodeList codeList = codeListMap.get(name);
        if (codeList != null) {
            node.getBbieSc().setCodeListId(codeList.getCodeListId());
        }
    }

    public void onSelectBbieScCodeListName(SelectEvent event) {
        setBbieScCodeListName(event.getObject().toString());
        onChangeData(getSelectedBasicBusinessInformationEntitySupplementaryComponentTreeNode());
    }

    public List<String> completeInputForBbieScCodeList(String query) {
        BBIESCNode node = getSelectedBasicBusinessInformationEntitySupplementaryComponentTreeNode();
        Map<String, CodeList> codeLists = bieService.getCodeLists(node);
        if (StringUtils.isEmpty(query)) {
            return new ArrayList(codeLists.keySet());
        } else {
            return codeLists.keySet().stream()
                    .filter(e -> e.toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
        }
    }

    public String getBbieScAgencyIdListName() {
        return bieService.getBbieAgencyIdListName(getSelectedBasicBusinessInformationEntitySupplementaryComponentTreeNode());
    }

    public void setBbieScAgencyIdListName(String name) {
        BBIESCNode node = getSelectedBasicBusinessInformationEntitySupplementaryComponentTreeNode();
        Map<String, AgencyIdList> agencyIdListMap = bieService.getAgencyIdListIds(node);
        AgencyIdList agencyIdList = agencyIdListMap.get(name);
        if (agencyIdList != null) {
            node.getBbieSc().setAgencyIdListId(agencyIdList.getAgencyIdListId());
        }
    }

    public void onSelectBbieScAgencyIdListName(SelectEvent event) {
        setBbieScAgencyIdListName(event.getObject().toString());
        onChangeData(getSelectedBasicBusinessInformationEntitySupplementaryComponentTreeNode());
    }

    public List<String> completeInputForBbieScAgencyIdList(String query) {
        BBIESCNode node = getSelectedBasicBusinessInformationEntitySupplementaryComponentTreeNode();
        Map<String, AgencyIdList> agencyIdListMap = bieService.getAgencyIdListIds(node);
        if (StringUtils.isEmpty(query)) {
            return new ArrayList(agencyIdListMap.keySet());
        } else {
            return agencyIdListMap.keySet().stream()
                    .filter(e -> e.toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
        }
    }
}
