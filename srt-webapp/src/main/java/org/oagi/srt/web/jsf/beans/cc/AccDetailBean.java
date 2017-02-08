package org.oagi.srt.web.jsf.beans.cc;

import org.oagi.srt.model.treenode.*;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.oagi.srt.service.CoreComponentService;
import org.oagi.srt.service.ExtensionService;
import org.oagi.srt.service.NamespaceService;
import org.oagi.srt.service.TreeNodeService;
import org.oagi.srt.web.handler.UIHandler;
import org.oagi.srt.web.jsf.component.treenode.TreeNodeTypeNameResolver;
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
import java.util.*;
import java.util.stream.Collectors;

import static org.oagi.srt.repository.entity.BasicCoreComponentEntityType.Element;
import static org.oagi.srt.repository.entity.CoreComponentState.Published;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
@Transactional(readOnly = true)
public class AccDetailBean extends UIHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private CoreComponentService coreComponentService;

    @Autowired
    private ExtensionService extensionService;

    @Autowired
    private TreeNodeService treeNodeService;

    @Autowired
    private AggregateCoreComponentRepository accRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NamespaceService namespaceService;

    @Autowired
    private ModuleRepository moduleRepository;

    private AggregateCoreComponent targetAcc;
    private AggregateCoreComponent userExtensionAcc;

    private LinkedList<TreeNode> treeNodeLinkedList = new LinkedList();
    private int treeNodeLinkedListIndex = -1;
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
    }

    public AggregateCoreComponent getTargetAcc() {
        return targetAcc;
    }

    public void setTargetAcc(AggregateCoreComponent targetAcc) {
        this.targetAcc = targetAcc;
    }

    public AggregateCoreComponent getUserExtensionAcc() {
        return userExtensionAcc;
    }

    public void setUserExtensionAcc(AggregateCoreComponent userExtensionAcc) {
        this.userExtensionAcc = userExtensionAcc;
    }

    public TreeNode getTreeNode() {
        return treeNodeLinkedList.get(treeNodeLinkedListIndex);
    }

    public void setTreeNode(TreeNode treeNode) {
        while (treeNodeLinkedListIndex + 1 < treeNodeLinkedList.size()) {
            treeNodeLinkedList.removeLast();
        }
        treeNodeLinkedList.add(++treeNodeLinkedListIndex, treeNode);

    }

    public boolean canBack() {
        return (treeNodeLinkedListIndex > 0);
    }

    public boolean canForward() {
        return (treeNodeLinkedListIndex + 1) < treeNodeLinkedList.size();
    }

    public void navigateBack() {
        treeNodeLinkedListIndex--;
    }

    public void navigateForward() {
        treeNodeLinkedListIndex++;
    }

    public void navigateForward(AggregateCoreComponent acc) {
        TreeNode treeNode = createTreeNode(acc);
        setTreeNode(treeNode);
    }

    public TreeNode getRootNode() {
        return treeNodeLinkedList.get(0).getChildren().get(0);
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
            if (data instanceof AggregateCoreComponentTreeNode) {
                AggregateCoreComponentTreeNode accTreeNode = (AggregateCoreComponentTreeNode) data;
                return accTreeNode.getAggregateCoreComponent();
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
        CoreComponentTreeNode node = (CoreComponentTreeNode) treeNode.getData();
        if (node instanceof AssociationCoreComponentPropertyTreeNode) {
            AssociationCoreComponentPropertyTreeNode asccpNode = (AssociationCoreComponentPropertyTreeNode) node;
            return asccpNode.getAssociationCoreComponentProperty().getState();
        } else {
            return null;
        }
    }

    public TreeNode createTreeNode(AggregateCoreComponent acc) {
        AggregateCoreComponentTreeNode accNode =
                treeNodeService.createCoreComponentTreeNode(acc);
        TreeNode root = new DefaultTreeNode();
        toTreeNode(accNode, root);
        return root;
    }

    private TreeNode toTreeNode(CoreComponentTreeNode node, TreeNode parent) {
        setNodeName(node);

        TreeNodeTypeNameResolver treeNodeTypeNameResolver = getTreeNodeTypeNameResolver(node);
        String type = treeNodeTypeNameResolver.getType();
        TreeNode treeNode = new DefaultTreeNode(type, node, parent);
        if (node.hasChild()) {
            new DefaultTreeNode(null, treeNode); // append a dummy child
        }
        return treeNode;
    }

    private void setNodeName(CoreComponentTreeNode node) {
        TreeNodeTypeNameResolver treeNodeTypeNameResolver = getTreeNodeTypeNameResolver(node);
        String name = treeNodeTypeNameResolver.getName();
        node.setAttribute("name", name);
    }

    private TreeNodeTypeNameResolver getTreeNodeTypeNameResolver(CoreComponentTreeNode node) {
        if (node instanceof AggregateCoreComponentTreeNode) {
            return new AggregateCoreComponentTreeNodeTypeNameResolver((AggregateCoreComponentTreeNode) node);
        } else if (node instanceof AssociationCoreComponentPropertyTreeNode) {
            return new AssociationCoreComponentPropertyTreeNodeTypeNameResolver((AssociationCoreComponentPropertyTreeNode) node);
        } else if (node instanceof BasicCoreComponentPropertyTreeNode) {
            return new BasicCoreComponentPropertyTreeNodeTypeNameResolver((BasicCoreComponentPropertyTreeNode) node);
        } else if (node instanceof BusinessDataTypeSupplementaryComponentTreeNode) {
            return new BusinessDataTypeSupplementaryComponentTreeNodeTypeNameResolver((BusinessDataTypeSupplementaryComponentTreeNode) node);
        } else {
            throw new IllegalStateException();
        }
    }

    private class AggregateCoreComponentTreeNodeTypeNameResolver implements TreeNodeTypeNameResolver {

        private AggregateCoreComponentTreeNode node;

        public AggregateCoreComponentTreeNodeTypeNameResolver(
                AggregateCoreComponentTreeNode node) {
            this.node = node;
        }

        @Override
        public String getType() {
            AggregateCoreComponent acc = node.getAggregateCoreComponent();
            String type = (targetAcc.equals(acc)) ? "ACC-Extension" : "ACC";
            return type;
        }

        @Override
        public String getName() {
            AggregateCoreComponent acc = node.getAggregateCoreComponent();
            return acc.getDen();
        }
    }

    private class AssociationCoreComponentPropertyTreeNodeTypeNameResolver implements TreeNodeTypeNameResolver {

        private AssociationCoreComponentPropertyTreeNode node;

        public AssociationCoreComponentPropertyTreeNodeTypeNameResolver(
                AssociationCoreComponentPropertyTreeNode node) {
            this.node = node;
        }

        @Override
        public String getType() {
            return "ASCCP";
        }

        @Override
        public String getName() {
            AssociationCoreComponentProperty asccp = node.getAssociationCoreComponentProperty();
            return asccp.getPropertyTerm();
        }
    }

    private class BasicCoreComponentPropertyTreeNodeTypeNameResolver implements TreeNodeTypeNameResolver {

        private BasicCoreComponentPropertyTreeNode node;

        public BasicCoreComponentPropertyTreeNodeTypeNameResolver(
                BasicCoreComponentPropertyTreeNode node) {
            this.node = node;
        }

        @Override
        public String getType() {
            BasicCoreComponent bcc = node.getBasicCoreComponent();
            String type = "BCCP" + (bcc.getEntityType() == BasicCoreComponentEntityType.Attribute ? "-Attribute" : "");
            return type;
        }

        @Override
        public String getName() {
            BasicCoreComponentProperty bccp = node.getBasicCoreComponentProperty();
            return bccp.getPropertyTerm();
        }
    }

    private class BusinessDataTypeSupplementaryComponentTreeNodeTypeNameResolver implements TreeNodeTypeNameResolver {

        private BusinessDataTypeSupplementaryComponentTreeNode node;

        public BusinessDataTypeSupplementaryComponentTreeNodeTypeNameResolver(
                BusinessDataTypeSupplementaryComponentTreeNode node) {
            this.node = node;
        }

        @Override
        public String getType() {
            return "BDTSC";
        }

        @Override
        public String getName() {
            DataTypeSupplementaryComponent bdtSc = node.getBusinessDataTypeSupplementaryComponent();
            String name = bdtSc.getPropertyTerm() + ". " + bdtSc.getRepresentationTerm();
            return name;
        }
    }

    public void expand(NodeExpandEvent expandEvent) {
        DefaultTreeNode treeNode = (DefaultTreeNode) expandEvent.getTreeNode();
        CoreComponentTreeNode coreComponentTreeNode = (CoreComponentTreeNode) treeNode.getData();
        Boolean expanded = (Boolean) coreComponentTreeNode.getAttribute("expanded");
        if (expanded == null || expanded == false) {
            if (coreComponentTreeNode.hasChild()) {
                treeNode.setChildren(new ArrayList()); // clear children

                for (CoreComponentTreeNode child : coreComponentTreeNode.getChildren()) {
                    toTreeNode(child, treeNode);
                }
            }
            coreComponentTreeNode.setAttribute("expanded", true);
        }
    }

    private boolean preparedAppend;

    public boolean isPreparedAppend() {
        return preparedAppend;
    }

    public void setPreparedAppend(boolean preparedAppend) {
        this.preparedAppend = preparedAppend;
    }

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
        AggregateCoreComponentTreeNode accNode = (AggregateCoreComponentTreeNode) treeNode.getData();
        AggregateCoreComponent acc = accNode.getAggregateCoreComponent();
        User requester = getCurrentUser();

        try {
            coreComponentService.update(acc, requester);
            acc.afterLoaded();
        } catch (Throwable t) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", t.getMessage()));
            throw t;
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void discardAcc(TreeNode treeNode) {
        AggregateCoreComponentTreeNode accNode = (AggregateCoreComponentTreeNode) treeNode.getData();
        AggregateCoreComponent acc = accNode.getAggregateCoreComponent();
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
        reorderTreeNode(root);
    }

    public void onChangeObjectClassTerm(AggregateCoreComponentTreeNode accNode) {
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
        AggregateCoreComponent pAcc = getUserExtensionAcc();

        if (extensionService.exists(pAcc, tAsccp)) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "You cannot associate the same component."));
            return;
        }

        User user = getCurrentUser();
        ExtensionService.AppendAsccResult result = extensionService.appendAsccTo(pAcc, tAsccp, user);

        TreeNode rootNode = getRootNode();
        ((CoreComponentTreeNode) rootNode.getData()).reload();

        AssociationCoreComponentPropertyTreeNode asccpNode =
                treeNodeService.createCoreComponentTreeNode(result.getAscc());
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
        AggregateCoreComponent pAcc = getUserExtensionAcc();

        if (extensionService.exists(pAcc, tBccp)) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "You cannot associate the same component."));
            return;
        }

        User user = getCurrentUser();
        ExtensionService.AppendBccResult result = extensionService.appendBccTo(pAcc, tBccp, user);

        TreeNode rootNode = getRootNode();
        ((CoreComponentTreeNode) rootNode.getData()).reload();

        BasicCoreComponentPropertyTreeNode bccpNode =
                treeNodeService.createCoreComponentTreeNode(result.getBcc());
        TreeNode child = toTreeNode(bccpNode, rootNode);

        getSelectedTreeNode().setSelected(false);
        child.setSelected(true);
        setSelectedTreeNode(child);
    }

    // End Append BCC

    @Transactional(rollbackFor = Throwable.class)
    public void updateAscc(TreeNode treeNode) {
        AssociationCoreComponentPropertyTreeNode asccpNode = (AssociationCoreComponentPropertyTreeNode) treeNode.getData();
        AssociationCoreComponent ascc = asccpNode.getAssociationCoreComponent();
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
        AssociationCoreComponentPropertyTreeNode asccpNode = (AssociationCoreComponentPropertyTreeNode) treeNode.getData();
        AssociationCoreComponent ascc = asccpNode.getAssociationCoreComponent();
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
        BasicCoreComponentPropertyTreeNode bccpNode = (BasicCoreComponentPropertyTreeNode) treeNode.getData();
        BasicCoreComponent bcc = bccpNode.getBasicCoreComponent();
        if (!bccpNode.getChildren().isEmpty() && Element == bcc.getEntityType()) {
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
        BasicCoreComponentPropertyTreeNode bccpNode = (BasicCoreComponentPropertyTreeNode) treeNode.getData();
        BasicCoreComponent bcc = bccpNode.getBasicCoreComponent();
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
                return getCreationTimestamp(a).compareTo(getCreationTimestamp(b));
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
        if (data instanceof AssociationCoreComponentPropertyTreeNode) {
            return ((AssociationCoreComponentPropertyTreeNode) data).getAssociationCoreComponent().getSeqKey();
        } else if (data instanceof BasicCoreComponentPropertyTreeNode) {
            return ((BasicCoreComponentPropertyTreeNode) data).getBasicCoreComponent().getSeqKey();
        }
        return -1;
    }

    private Date getCreationTimestamp(TreeNode treeNode) {
        Object data = treeNode.getData();
        if (data instanceof AssociationCoreComponentPropertyTreeNode) {
            return ((AssociationCoreComponentPropertyTreeNode) data).getAssociationCoreComponentProperty().getCreationTimestamp();
        } else if (data instanceof BasicCoreComponentPropertyTreeNode) {
            return ((BasicCoreComponentPropertyTreeNode) data).getBasicCoreComponentProperty().getCreationTimestamp();
        } else if (data instanceof AggregateCoreComponentTreeNode) {
            return ((AggregateCoreComponentTreeNode) data).getAggregateCoreComponent().getCreationTimestamp();
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

    private void updateState(TreeNode treeNode, CoreComponentState state, User requester) {
        long lastUpdatedBy = requester.getAppUserId();
        Object data = treeNode.getData();

        if (data instanceof AggregateCoreComponentTreeNode) {
            AggregateCoreComponentTreeNode accNode = ((AggregateCoreComponentTreeNode) data);
            AggregateCoreComponent acc = accNode.getAggregateCoreComponent();
            if (acc.getState() != Published) {
                acc.setState(state);
                acc.setLastUpdatedBy(lastUpdatedBy);
                acc.afterLoaded();
            }
        } else if (data instanceof AssociationCoreComponentPropertyTreeNode) {
            AssociationCoreComponentPropertyTreeNode asccpNode = ((AssociationCoreComponentPropertyTreeNode) data);
            AssociationCoreComponent ascc = asccpNode.getAssociationCoreComponent();
            if (ascc.getState() != Published) {
                ascc.setState(state);
                ascc.setLastUpdatedBy(lastUpdatedBy);
                ascc.afterLoaded();
            }

            AssociationCoreComponentProperty asccp = asccpNode.getAssociationCoreComponentProperty();
            if (asccp.getState() != Published) {
                asccp.setState(state);
                asccp.setLastUpdatedBy(lastUpdatedBy);
                asccp.afterLoaded();
            }
        } else if (data instanceof BasicCoreComponentPropertyTreeNode) {
            BasicCoreComponentPropertyTreeNode bccpNode = ((BasicCoreComponentPropertyTreeNode) data);

            BasicCoreComponent bcc = bccpNode.getBasicCoreComponent();
            if (bcc.getState() != Published) {
                bcc.setState(state);
                bcc.setLastUpdatedBy(lastUpdatedBy);
                bcc.afterLoaded();
            }

            BasicCoreComponentProperty bccp = bccpNode.getBasicCoreComponentProperty();
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

