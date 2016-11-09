package org.oagi.srt.web.jsf.beans.cc;

import org.oagi.srt.model.*;
import org.oagi.srt.model.cc.ACCNode;
import org.oagi.srt.model.cc.ASCCPNode;
import org.oagi.srt.model.cc.BCCPNode;
import org.oagi.srt.model.cc.BDTSCNode;
import org.oagi.srt.model.cc.impl.BaseACCNode;
import org.oagi.srt.model.cc.impl.BaseBCCPNode;
import org.oagi.srt.model.cc.impl.LazyASCCPNode;
import org.oagi.srt.model.cc.impl.LazyBCCPNode;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.oagi.srt.service.CoreComponentService;
import org.oagi.srt.service.ExtensionService;
import org.oagi.srt.service.NodeService;
import org.oagi.srt.web.handler.UIHandler;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.oagi.srt.repository.entity.CoreComponentState.Published;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
@Transactional(readOnly = true)
public class ExtensionBean extends UIHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private CoreComponentService coreComponentService;

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

    @Autowired
    private BasicCoreComponentPropertyRepository bccpRepository;

    @Autowired
    private DataTypeRepository dataTypeRepository;

    @Autowired
    private DataTypeSupplementaryComponentRepository dtScRepository;

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

        TreeNode treeNode = createTreeNode(targetAcc);
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

    public TreeNode getRootNode() {
        return treeNode.getChildren().get(0);
    }

    public TreeNode getSelectedTreeNode() {
        return selectedTreeNode;
    }

    public void setSelectedTreeNode(TreeNode selectedTreeNode) {
        this.selectedTreeNode = selectedTreeNode;
        setPreparedAppendAscc(false);
        setPreparedAppendBcc(false);
    }

    public void onSelectTreeNode(NodeSelectEvent selectEvent) {
        setSelectedTreeNode(selectEvent.getTreeNode());
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

    public TreeNode createTreeNode(AggregateCoreComponent acc) {
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

            List<? extends Node> children = lazyNode.getChildren();
            for (Node child : children) {
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
            if (targetAcc.equals(acc)) {
                visitNode(accNode, accNode.getType() + "-Extension");
            } else {
                visitNode(accNode);
            }
        }

        @Override
        public void visitBCCPNode(BCCPNode bccpNode) {
            visitNode(bccpNode);
        }

        @Override
        public void visitBDTSCNode(BDTSCNode bdtscNode) {
            visitNode(bdtscNode);
        }

        private void visitNode(CCNode node) {
            visitNode(node, node.getType());
        }

        private void visitNode(CCNode node, String type) {
            if (exists(node)) {
                return;
            }

            if (node.getName().contains("User Extension")) { // To hide 'User Extension' nodes
                ((LazyCCNode) node).fetch();

                List<? extends Node> children = node.getChildren();
                for (Node child : children) {
                    visitNode((CCNode) child);
                }
            } else {
                TreeNode treeNode = new DefaultTreeNode(type, node, this.parent);
                if (node instanceof LazyNode) {
                    LazyNode lazyNode = (LazyNode) node;
                    for (int i = 0, len = lazyNode.getChildrenCount(); i < len; ++i) {
                        new DefaultTreeNode(null, treeNode);
                    }
                }
            }
        }

        private boolean exists(CCNode node) {
            for (TreeNode child : this.parent.getChildren()) {
                if (node.equals(child.getData())) {
                    return true;
                }
            }
            return false;
        }
    }

    private boolean preparedAppend;

    public boolean isPreparedAppend() {
        return preparedAppend;
    }

    public void setPreparedAppend(boolean preparedAppend) {
        this.preparedAppend = preparedAppend;
    }

    /*
     * Begin Append ASCC
     */

    @Autowired
    private AssociationCoreComponentPropertyForLookupRepository asccpLookupRepository;
    private boolean preparedAppendAscc;
    private List<AssociationCoreComponentPropertyForLookup> allAsccpList;
    private List<AssociationCoreComponentPropertyForLookup> asccpList;
    private String selectedAsccpPropertyTerm;
    private AssociationCoreComponentPropertyForLookup selectedAsccp;

    public boolean isPreparedAppendAscc() {
        return preparedAppendAscc;
    }

    public void setPreparedAppendAscc(boolean preparedAppendAscc) {
        this.preparedAppendAscc = preparedAppendAscc;
        setPreparedAppend(preparedAppendAscc);
    }

    public List<AssociationCoreComponentPropertyForLookup> getAsccpList() {
        return asccpList;
    }

    public void setAsccpList(List<AssociationCoreComponentPropertyForLookup> asccpList) {
        this.asccpList = asccpList;
    }

    public String getSelectedAsccpPropertyTerm() {
        return selectedAsccpPropertyTerm;
    }

    public void setSelectedAsccpPropertyTerm(String selectedAsccpPropertyTerm) {
        this.selectedAsccpPropertyTerm = selectedAsccpPropertyTerm;
    }

    public AssociationCoreComponentPropertyForLookup getSelectedAsccp() {
        return selectedAsccp;
    }

    public void setSelectedAsccp(AssociationCoreComponentPropertyForLookup selectedAsccp) {
        this.selectedAsccp = selectedAsccp;
    }

    public void onAsccpRowSelect(SelectEvent event) {
        setSelectedAsccp((AssociationCoreComponentPropertyForLookup) event.getObject());
    }

    public void onAsccpRowUnselect(UnselectEvent event) {
        setSelectedAsccp(null);
    }

    public void prepareAppendAscc() {
        allAsccpList = asccpLookupRepository.findAll().stream()
                .filter(e -> !e.isDeprecated())
                .filter(e -> e.isReusableIndicator())
                .collect(Collectors.toList());
        setAsccpList(
                allAsccpList.stream()
                        .sorted((a, b) -> a.getPropertyTerm().compareTo(b.getPropertyTerm()))
                        .collect(Collectors.toList())
        );
        setPreparedAppendAscc(true);
    }

    public List<String> completeInputAsccp(String query) {
        return allAsccpList.stream()
                .map(e -> e.getPropertyTerm())
                .distinct()
                .filter(e -> e.toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    public void onSelectAsccpPropertyTerm(SelectEvent event) {
        setSelectedAsccpPropertyTerm(event.getObject().toString());
    }

    public void searchAsccp() {
        String selectedPropertyTerm = StringUtils.trimWhitespace(getSelectedAsccpPropertyTerm());
        if (StringUtils.isEmpty(selectedPropertyTerm)) {
            setAsccpList(allAsccpList.stream()
                    .sorted((a, b) -> a.getPropertyTerm().compareTo(b.getPropertyTerm()))
                    .collect(Collectors.toList()));
        } else {
            setAsccpList(
                    allAsccpList.stream()
                            .filter(e -> e.getPropertyTerm().toLowerCase().contains(selectedPropertyTerm.toLowerCase()))
                            .collect(Collectors.toList())
            );
        }
        setPreparedAppendAscc(true);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void appendAscc() {
        AssociationCoreComponentPropertyForLookup selectedAsccpLookup = getSelectedAsccp();
        if (selectedAsccpLookup == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "You have to choose valid association core component."));
            return;
        }

        AssociationCoreComponentProperty tAsccp = asccpRepository.findOne(selectedAsccpLookup.getAsccpId());
        AggregateCoreComponent pAcc = getUserExtensionAcc();

        if (extensionService.exists(pAcc, tAsccp)) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "You cannot associate the same component."));
            return;
        }

        User user = getCurrentUser();
        TreeNode rootNode = getRootNode();
        ACCNode rootAccNode = (ACCNode) rootNode.getData();
        ExtensionService.AppendAsccResult result = extensionService.appendAsccTo(pAcc, tAsccp, user);

        LazyASCCPNode asccpNode = nodeService.createLazyASCCPNode(
                new BaseACCNode(rootAccNode, pAcc), result.getAscc(), tAsccp);
        TreeNode child = new DefaultTreeNode(asccpNode.getType(), asccpNode, rootNode);
        for (int i = 0, len = asccpNode.getChildrenCount(); i < len; ++i) {
            new DefaultTreeNode(null, child);
        }
        setSelectedTreeNode(child);
    }

    // End Append ASCC



    /*
     * Begin Append BCC
     */

    @Autowired
    private BasicCoreComponentPropertyForLookupRepository bccpLookupRepository;
    private boolean preparedAppendBcc;
    private List<BasicCoreComponentPropertyForLookup> allBccpList;
    private List<BasicCoreComponentPropertyForLookup> bccpList;
    private String selectedBccpPropertyTerm;
    private BasicCoreComponentPropertyForLookup selectedBccp;

    public boolean isPreparedAppendBcc() {
        return preparedAppendBcc;
    }

    public void setPreparedAppendBcc(boolean preparedAppendBcc) {
        this.preparedAppendBcc = preparedAppendBcc;
        setPreparedAppend(preparedAppendBcc);
    }

    public List<BasicCoreComponentPropertyForLookup> getBccpList() {
        return bccpList;
    }

    public void setBccpList(List<BasicCoreComponentPropertyForLookup> bccpList) {
        this.bccpList = bccpList;
    }

    public String getSelectedBccpPropertyTerm() {
        return selectedBccpPropertyTerm;
    }

    public void setSelectedBccpPropertyTerm(String selectedBccpPropertyTerm) {
        this.selectedBccpPropertyTerm = selectedBccpPropertyTerm;
    }

    public BasicCoreComponentPropertyForLookup getSelectedBccp() {
        return selectedBccp;
    }

    public void setSelectedBccp(BasicCoreComponentPropertyForLookup selectedBccp) {
        this.selectedBccp = selectedBccp;
    }

    public void onBccpRowSelect(SelectEvent event) {
        setSelectedBccp((BasicCoreComponentPropertyForLookup) event.getObject());
    }

    public void onBccpRowUnselect(UnselectEvent event) {
        setSelectedBccp(null);
    }

    public void prepareAppendBcc() {
        allBccpList = bccpLookupRepository.findAll().stream()
                .filter(e -> !e.isDeprecated())
                .collect(Collectors.toList());
        setBccpList(
                allBccpList.stream()
                        .sorted((a, b) -> a.getPropertyTerm().compareTo(b.getPropertyTerm()))
                        .collect(Collectors.toList())
        );
        setPreparedAppendBcc(true);
    }

    public List<String> completeInputBccp(String query) {
        return allBccpList.stream()
                .map(e -> e.getPropertyTerm())
                .distinct()
                .filter(e -> e.toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    public void onSelectBccpPropertyTerm(SelectEvent event) {
        setSelectedBccpPropertyTerm(event.getObject().toString());
    }

    public void searchBccp() {
        String selectedPropertyTerm = StringUtils.trimWhitespace(getSelectedBccpPropertyTerm());
        if (StringUtils.isEmpty(selectedPropertyTerm)) {
            setBccpList(allBccpList.stream()
                    .sorted((a, b) -> a.getPropertyTerm().compareTo(b.getPropertyTerm()))
                    .collect(Collectors.toList()));
        } else {
            setBccpList(
                    allBccpList.stream()
                            .filter(e -> e.getPropertyTerm().toLowerCase().contains(selectedPropertyTerm.toLowerCase()))
                            .collect(Collectors.toList())
            );
        }
        setPreparedAppendBcc(true);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void appendBcc() {
        BasicCoreComponentPropertyForLookup selectedBccpLookup = getSelectedBccp();
        if (selectedBccpLookup == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "You have to choose valid basic core component."));
            return;
        }

        BasicCoreComponentProperty tBccp = bccpRepository.findOne(selectedBccpLookup.getBccpId());
        AggregateCoreComponent pAcc = getUserExtensionAcc();

        if (extensionService.exists(pAcc, tBccp)) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "You cannot associate the same component."));
            return;
        }

        User user = getCurrentUser();
        TreeNode rootNode = getRootNode();
        ACCNode rootAccNode = (ACCNode) rootNode.getData();
        ExtensionService.AppendBccResult result = extensionService.appendBccTo(pAcc, tBccp, user);

        long bdtId = tBccp.getBdtId();
        DataType bdt = dataTypeRepository.findOne(bdtId);

        LazyBCCPNode bccpNode = nodeService.createLazyBCCPNode(
                new BaseACCNode(rootAccNode, pAcc), result.getBcc(), tBccp, bdt);
        TreeNode child = new DefaultTreeNode(bccpNode.getType(), bccpNode, rootNode);
        for (int i = 0, len = bccpNode.getChildrenCount(); i < len; ++i) {
            new DefaultTreeNode(null, child);
        }
        setSelectedTreeNode(child);
    }

    // End Append BCC

    @Transactional(rollbackFor = Throwable.class)
    public void updateAscc(TreeNode treeNode) {
        ASCCPNode asccpNode = (ASCCPNode) treeNode.getData();
        AssociationCoreComponent ascc = asccpNode.getAscc();
        User requester = getCurrentUser();

        try {
            coreComponentService.update(ascc, requester);
            ascc.afterLoaded();
        } catch (Throwable t) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", t.getMessage()));
            throw t;
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void updateBcc(TreeNode treeNode) {
        BCCPNode bccpNode = (BCCPNode) treeNode.getData();
        BasicCoreComponent bcc = bccpNode.getBcc();
        User requester = getCurrentUser();

        try {
            coreComponentService.update(bcc, requester);
            bcc.afterLoaded();
        } catch (Throwable t) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", t.getMessage()));
            throw t;
        }

        treeNode.setType(bccpNode.getType());

        TreeNode root = getTreeNode();
        reorderTreeNode(root);
    }

    private void reorderTreeNode(TreeNode treeNode) {
        List<TreeNode> children = treeNode.getChildren();
        Collections.sort(children, (a, b) -> {
            int s1 = getSeqKey(a);
            int s2 = getSeqKey(b);
            int compareTo = s1 - s2;
            if (compareTo != 0) {
                return compareTo;
            } else {
                return getTerm(a).compareTo(getTerm(b));
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

    private String getTerm(TreeNode treeNode) {
        Object data = treeNode.getData();
        if (data instanceof ASCCPNode) {
            return ((ASCCPNode) data).getAsccp().getPropertyTerm();
        } else if (data instanceof BCCPNode) {
            return ((BCCPNode) data).getBccp().getPropertyTerm();
        } else if (data instanceof ACCNode) {
            return ((ACCNode) data).getAcc().getObjectClassTerm();
        }
        return "";
    }

    @Transactional(rollbackFor = Throwable.class)
    public void updateState(CoreComponentState state) {
        User requester = getCurrentUser();
        try {
            AggregateCoreComponent eAcc = getTargetAcc();
            AggregateCoreComponent ueAcc = getUserExtensionAcc();

            coreComponentService.updateState(eAcc, ueAcc, state, requester);

            TreeNode root = getTreeNode();
            updateState(root, state, requester);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "State changed to '" + state + "' successfully."));
        } catch (Throwable t) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", t.getMessage()));
            throw t;
        }
    }

    private void updateState(TreeNode treeNode, CoreComponentState state, User requester) {
        long lastUpdatedBy = requester.getAppUserId();
        Object data = treeNode.getData();

        if (data instanceof ACCNode) {
            ACCNode accNode = ((ACCNode) data);
            AggregateCoreComponent acc = accNode.getAcc();
            if (acc.getState() != Published) {
                acc.setState(state);
                acc.setLastUpdatedBy(lastUpdatedBy);
                acc.afterLoaded();
            }
        } else if (data instanceof ASCCPNode) {
            ASCCPNode asccpNode = ((ASCCPNode) data);
            AssociationCoreComponent ascc = asccpNode.getAscc();
            if (ascc.getState() != Published) {
                ascc.setState(state);
                ascc.setLastUpdatedBy(lastUpdatedBy);
                ascc.afterLoaded();
            }

            AssociationCoreComponentProperty asccp = asccpNode.getAsccp();
            if (asccp.getState() != Published) {
                asccp.setState(state);
                asccp.setLastUpdatedBy(lastUpdatedBy);
                asccp.afterLoaded();
            }
        } else if (data instanceof BCCPNode) {
            BCCPNode bccpNode = ((BCCPNode) data);

            BasicCoreComponent bcc = bccpNode.getBcc();
            if (bcc.getState() != Published) {
                bcc.setState(state);
                bcc.setLastUpdatedBy(lastUpdatedBy);
                bcc.afterLoaded();
            }

            BasicCoreComponentProperty bccp = bccpNode.getBccp();
            if (bccp.getState() != Published) {
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
