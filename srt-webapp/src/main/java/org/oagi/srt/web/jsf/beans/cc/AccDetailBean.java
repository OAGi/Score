package org.oagi.srt.web.jsf.beans.cc;

import org.oagi.srt.model.node.*;
import org.oagi.srt.repository.AggregateCoreComponentRepository;
import org.oagi.srt.repository.AssociationCoreComponentPropertyRepository;
import org.oagi.srt.repository.BasicCoreComponentPropertyRepository;
import org.oagi.srt.repository.ModuleRepository;
import org.oagi.srt.repository.entity.*;
import org.oagi.srt.service.CoreComponentService;
import org.oagi.srt.service.ExtensionService;
import org.oagi.srt.service.NamespaceService;
import org.oagi.srt.service.NodeService;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
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
import java.util.*;
import java.util.stream.Collectors;

import static org.oagi.srt.repository.entity.BasicCoreComponentEntityType.Attribute;
import static org.oagi.srt.repository.entity.CoreComponentState.Editing;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
@Transactional(readOnly = true)
public class AccDetailBean extends BaseCoreComponentDetailBean {

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
    private NamespaceService namespaceService;

    @Autowired
    private ModuleRepository moduleRepository;

    private AggregateCoreComponent targetAcc;
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

        if (targetAcc == null) {
            throw new IllegalStateException();
        }
        if (Editing == targetAcc.getState() && getCurrentUser().getAppUserId() != targetAcc.getOwnerUserId()) {
            throw new IllegalStateException();
        }

        setTargetAcc(targetAcc);

        TreeNode treeNode = createTreeNode(targetAcc);
        setTreeNode(treeNode);
    }

    public AggregateCoreComponent getTargetAcc() {
        return targetAcc;
    }

    public void setTargetAcc(AggregateCoreComponent targetAcc) {
        this.targetAcc = targetAcc;
    }

    public boolean isDirty() {
        return isDirty(getRootNode());
    }

    public boolean isDirty(TreeNode treeNode) {
        if (treeNode == null) {
            return false;
        }

        Object data = treeNode.getData();
        if (data instanceof ACCNode) {
            ACCNode accNode = (ACCNode) data;
            AggregateCoreComponent acc = accNode.getAcc();
            if (acc.isDirty()) {
                return true;
            }
        } else if (data instanceof ASCCPNode) {
            ASCCPNode asccpNode = (ASCCPNode) data;
            AssociationCoreComponent ascc = asccpNode.getAscc();
            if (ascc.isDirty()) {
                return true;
            }
        } else if (data instanceof BCCPNode) {
            BCCPNode bccpNode = (BCCPNode) data;
            BasicCoreComponent bcc = bccpNode.getBcc();
            if (bcc.isDirty()) {
                return true;
            }
        }

        for (TreeNode childNode : treeNode.getChildren()) {
            if (isDirty(childNode)) {
                return true;
            }
        }

        return false;
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
        return (treeNode.getChildCount() > 0) ? treeNode.getChildren().get(0) : null;
    }

    public TreeNode getSelectedTreeNode() {
        return selectedTreeNode;
    }

    public void setSelectedTreeNode(TreeNode selectedTreeNode) {
        this.selectedTreeNode = selectedTreeNode;
        setPreparedAppendAscc(false);
        setPreparedAppendBcc(false);
    }

    public AggregateCoreComponent getSelectedAggregateCoreComponent() {
        TreeNode treeNode = getSelectedTreeNode();
        if (treeNode != null) {
            Object data = treeNode.getData();
            if (data instanceof ACCNode) {
                ACCNode accTreeNode = (ACCNode) data;
                return accTreeNode.getAcc();
            }
        }

        return null;
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

    private boolean preparedAppend;

    public boolean isPreparedAppend() {
        return preparedAppend;
    }

    public void setPreparedAppend(boolean preparedAppend) {
        this.preparedAppend = preparedAppend;
    }

    public List<Namespace> availableNamespaces(AggregateCoreComponent acc) {
        User owner = getOwnerUser(acc);
        if (owner.isOagisDeveloperIndicator()) {
            return Collections.emptyList();
        }

        List<Namespace> namespaces = namespaceService.findAll();
        return namespaces.stream().filter(e -> !e.isStdNmsp())
                .collect(Collectors.toList());
    }

    public List<Namespace> completeNamespace(String query) {
        AggregateCoreComponent acc = getSelectedAggregateCoreComponent();
        List<Namespace> namespaces = availableNamespaces(acc);
        if (StringUtils.isEmpty(query)) {
            return namespaces;
        } else {
            return namespaces.stream()
                    .filter(e -> e.getUri().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
        }
    }

    public void onSelectNamespace(SelectEvent event) {
        Namespace namespace = (Namespace) event.getObject();

        AggregateCoreComponent acc = getSelectedAggregateCoreComponent();
        if (namespace != null) {
            acc.setNamespaceId(namespace.getNamespaceId());
        }
    }

    public boolean canBeAbstract(AggregateCoreComponent acc) {
        long basedAccId = acc.getBasedAccId();
        if (basedAccId <= 0L) {
            return false;
        }

        AggregateCoreComponent basedAcc = accRepository.findOne(basedAccId);
        if (basedAcc == null) {
            return false;
        }

        return basedAcc.isAbstract();
    }

    @Transactional(rollbackFor = Throwable.class)
    public void updateAcc(TreeNode treeNode) {
        ACCNode accNode = (ACCNode) treeNode.getData();
        AggregateCoreComponent acc = accNode.getAcc();
        User requester = getCurrentUser();

        try {
            coreComponentService.update(acc, requester);
            acc.afterLoaded();
        } catch (Throwable t) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", t.getMessage()));
            throw t;
        }

        for (TreeNode child : treeNode.getChildren()) {
            if (isDirty(child)) {
                Object data = child.getData();
                if (data instanceof ASCCPNode) {
                    updateAscc(child);
                } else if (data instanceof BCCPNode) {
                    updateBcc(child);
                }
            }
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public String discardAcc(TreeNode treeNode) {
        ACCNode accNode = (ACCNode) treeNode.getData();
        if (!accNode.getChildren().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "Not allowed discard the ACC which has children"));
            return null;
        }

        AggregateCoreComponent acc = accNode.getAcc();
        User requester = getCurrentUser();

        try {
            coreComponentService.discard(acc, requester);
        } catch (Throwable t) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", t.getMessage()));
            throw t;
        }

        setSelectedTreeNode(null);

        TreeNode parent = treeNode.getParent();
        List<TreeNode> children = parent.getChildren();
        children.remove(treeNode);

        TreeNode root = getTreeNode();
        if (root.getChildCount() == 0) {
            return "/views/core_component/list.jsf?faces-redirect=true";
        } else {
            reorderTreeNode(root);
        }

        return null;
    }

    public void onChangeObjectClassTerm(ACCNode accNode) {
        for (CCNode child : accNode.getChildren()) {
            if (child instanceof ASCCPNode) {
                ASCCPNode asccpNode = (ASCCPNode) child;
                AggregateCoreComponent acc = accNode.getAcc();
                AssociationCoreComponent ascc = asccpNode.getAscc();
                AssociationCoreComponentProperty asccp = asccpNode.getAsccp();
                ascc.setDen(acc, asccp);
            } else if (child instanceof BCCPNode) {
                BCCPNode bccpNode = (BCCPNode) child;
                AggregateCoreComponent acc = accNode.getAcc();
                BasicCoreComponent bcc = bccpNode.getBcc();
                BasicCoreComponentProperty bccp = bccpNode.getBccp();
                bcc.setDen(acc, bccp);
            }
        }

        setNodeName(accNode);
    }

    /*
     * Begin Append ASCC
     */

    @Autowired
    private AssociationCoreComponentPropertyRepository asccpRepository;
    private boolean preparedAppendAscc;
    private List<AssociationCoreComponentProperty> allAsccpList;
    private List<AssociationCoreComponentProperty> asccpList;
    private String selectedAsccpPropertyTerm;
    private AssociationCoreComponentProperty selectedAsccp;

    public boolean isPreparedAppendAscc() {
        return preparedAppendAscc;
    }

    public void setPreparedAppendAscc(boolean preparedAppendAscc) {
        this.preparedAppendAscc = preparedAppendAscc;
        setPreparedAppend(preparedAppendAscc);
    }

    public List<AssociationCoreComponentProperty> getAsccpList() {
        return asccpList;
    }

    public void setAsccpList(List<AssociationCoreComponentProperty> asccpList) {
        this.asccpList = asccpList;
    }

    public String getSelectedAsccpPropertyTerm() {
        return selectedAsccpPropertyTerm;
    }

    public void setSelectedAsccpPropertyTerm(String selectedAsccpPropertyTerm) {
        this.selectedAsccpPropertyTerm = selectedAsccpPropertyTerm;
    }

    public AssociationCoreComponentProperty getSelectedAsccp() {
        return selectedAsccp;
    }

    public void setSelectedAsccp(AssociationCoreComponentProperty selectedAsccp) {
        this.selectedAsccp = selectedAsccp;
    }

    public void onAsccpRowSelect(SelectEvent event) {
        setSelectedAsccp((AssociationCoreComponentProperty) event.getObject());
    }

    public void onAsccpRowUnselect(UnselectEvent event) {
        setSelectedAsccp(null);
    }

    public void prepareAppendAscc() {
        allAsccpList = asccpRepository.findAll().stream()
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
        AssociationCoreComponentProperty selectedAsccpLookup = getSelectedAsccp();
        if (selectedAsccpLookup == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "You have to choose valid association core component."));
            return;
        }

        AssociationCoreComponentProperty tAsccp = asccpRepository.findOne(selectedAsccpLookup.getAsccpId());
        AggregateCoreComponent pAcc = getTargetAcc();

        if (extensionService.exists(pAcc, tAsccp)) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "You cannot associate the same component."));
            return;
        }

        User user = getCurrentUser();
        ExtensionService.AppendAsccResult result = extensionService.appendAsccTo(pAcc, tAsccp, user);

        TreeNode rootNode = getRootNode();
        ((CCNode) rootNode.getData()).reload();

        ASCCPNode asccpNode = nodeService.createCoreComponentTreeNode(result.getAscc());
        TreeNode child = toTreeNode(asccpNode, rootNode);

        getSelectedTreeNode().setSelected(false);
        child.setSelected(true);
        setSelectedTreeNode(child);
    }

    // End Append ASCC



    /*
     * Begin Append BCC
     */

    @Autowired
    private BasicCoreComponentPropertyRepository bccpRepository;
    private boolean preparedAppendBcc;
    private List<BasicCoreComponentProperty> allBccpList;
    private List<BasicCoreComponentProperty> bccpList;
    private String selectedBccpPropertyTerm;
    private BasicCoreComponentProperty selectedBccp;

    public boolean isPreparedAppendBcc() {
        return preparedAppendBcc;
    }

    public void setPreparedAppendBcc(boolean preparedAppendBcc) {
        this.preparedAppendBcc = preparedAppendBcc;
        setPreparedAppend(preparedAppendBcc);
    }

    public List<BasicCoreComponentProperty> getBccpList() {
        return bccpList;
    }

    public void setBccpList(List<BasicCoreComponentProperty> bccpList) {
        this.bccpList = bccpList;
    }

    public String getSelectedBccpPropertyTerm() {
        return selectedBccpPropertyTerm;
    }

    public void setSelectedBccpPropertyTerm(String selectedBccpPropertyTerm) {
        this.selectedBccpPropertyTerm = selectedBccpPropertyTerm;
    }

    public BasicCoreComponentProperty getSelectedBccp() {
        return selectedBccp;
    }

    public void setSelectedBccp(BasicCoreComponentProperty selectedBccp) {
        this.selectedBccp = selectedBccp;
    }

    public void onBccpRowSelect(SelectEvent event) {
        setSelectedBccp((BasicCoreComponentProperty) event.getObject());
    }

    public void onBccpRowUnselect(UnselectEvent event) {
        setSelectedBccp(null);
    }

    public void prepareAppendBcc() {
        allBccpList = bccpRepository.findAll().stream()
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
        BasicCoreComponentProperty selectedBccpLookup = getSelectedBccp();
        if (selectedBccpLookup == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "You have to choose valid basic core component."));
            return;
        }

        BasicCoreComponentProperty tBccp = bccpRepository.findOne(selectedBccpLookup.getBccpId());
        AggregateCoreComponent pAcc = getTargetAcc();

        if (extensionService.exists(pAcc, tBccp)) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "You cannot associate the same component."));
            return;
        }

        User user = getCurrentUser();
        ExtensionService.AppendBccResult result = extensionService.appendBccTo(pAcc, tBccp, user);

        TreeNode rootNode = getRootNode();
        ((CCNode) rootNode.getData()).reload();

        BCCPNode bccpNode = nodeService.createCoreComponentTreeNode(result.getBcc());
        TreeNode child = toTreeNode(bccpNode, rootNode);

        getSelectedTreeNode().setSelected(false);
        child.setSelected(true);
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
    public void discardAscc(TreeNode treeNode) {
        ASCCPNode asccpNode = (ASCCPNode) treeNode.getData();
        AssociationCoreComponent ascc = asccpNode.getAscc();
        User requester = getCurrentUser();

        try {
            coreComponentService.discard(ascc, requester);
        } catch (Throwable t) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", t.getMessage()));
            throw t;
        }

        setSelectedTreeNode(null);

        TreeNode parent = treeNode.getParent();
        List<TreeNode> children = parent.getChildren();
        children.remove(treeNode);

        TreeNode root = getTreeNode();
        reorderTreeNode(root);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void updateBcc(TreeNode treeNode) {
        BCCPNode bccpNode = (BCCPNode) treeNode.getData();
        BasicCoreComponent bcc = bccpNode.getBcc();
        if (!bccpNode.getChildren().isEmpty() && Attribute == bcc.getEntityType()) {
            throw new IllegalStateException("Only BBIE without SCs can be made Attribute.");
        }

        User requester = getCurrentUser();

        try {
            coreComponentService.update(bcc, requester);
            bcc.afterLoaded();
        } catch (Throwable t) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", t.getMessage()));
            throw t;
        }

        String type = getTreeNodeTypeNameResolver(bccpNode).getType();
        treeNode.setType(type);

        TreeNode root = getTreeNode();
        reorderTreeNode(root);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void discardBcc(TreeNode treeNode) {
        BCCPNode bccpNode = (BCCPNode) treeNode.getData();
        BasicCoreComponent bcc = bccpNode.getBcc();
        User requester = getCurrentUser();

        try {
            coreComponentService.discard(bcc, requester);
        } catch (Throwable t) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", t.getMessage()));
            throw t;
        }

        setSelectedTreeNode(null);

        TreeNode parent = treeNode.getParent();
        List<TreeNode> children = parent.getChildren();
        children.remove(treeNode);

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
                if (a instanceof BDTSCNode || b instanceof BDTSCNode) {
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

    @Transactional(rollbackFor = Throwable.class)
    public void updateState(CoreComponentState state) {
        User requester = getCurrentUser();
        try {
            AggregateCoreComponent eAcc = getTargetAcc();

            coreComponentService.updateState(eAcc, state, requester);

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
}

