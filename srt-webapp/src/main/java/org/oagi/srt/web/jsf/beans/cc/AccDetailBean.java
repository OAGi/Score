package org.oagi.srt.web.jsf.beans.cc;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.oagi.srt.model.node.*;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.oagi.srt.service.*;
import org.oagi.srt.web.jsf.beans.SearchFilter;
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
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.oagi.srt.common.util.Utility.*;
import static org.oagi.srt.repository.entity.BasicCoreComponentEntityType.Attribute;
import static org.oagi.srt.repository.entity.CoreComponentState.Editing;
import static org.oagi.srt.repository.entity.CoreComponentState.Published;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
@Transactional(readOnly = true)
public class AccDetailBean extends BaseCoreComponentDetailBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private UndoService undoService;

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

    @Autowired
    private CoreComponentBeanHelper coreComponentBeanHelper;

    private AggregateCoreComponent targetAcc;
    private int targetAccMaxSeqKey;

    private TreeNode treeNode;
    private TreeNode selectedTreeNode;
    private boolean setSelectedTreeNodeAfterRefresh = false;
    private TreeNode selectedNodeAfterRefresh;

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

        TreeNode treeNode = createTreeNode(targetAcc, true);
        setTreeNode(treeNode);
    }

    public AggregateCoreComponent getTargetAcc() {
        return targetAcc;
    }

    public void setTargetAcc(AggregateCoreComponent targetAcc) {
        this.targetAcc = targetAcc;
        onUpdateTargetAccChildCount();
    }

    public void onUpdateTargetAccChildCount() {
        targetAccMaxSeqKey = coreComponentService.getMaxSeqKeyOfChildren(targetAcc);
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
        if (treeNode != null && selectedTreeNode == null && setSelectedTreeNodeAfterRefresh) {
            this.selectedTreeNode = selectedNodeAfterRefresh;
            setSelectedTreeNodeAfterRefresh = false;
        } else {
            this.selectedTreeNode = selectedTreeNode;
        }
        setPreparedAppendAscc(false);
        setPreparedAppendBcc(false);
    }

    public boolean canMoveUp(TreeNode selectedTreeNode) {
        if (selectedTreeNode == null) {
            return false;
        }

        if (getRootNode() != selectedTreeNode.getParent()) {
            return false;
        }

        Object data = selectedTreeNode.getData();
        if (data instanceof ASCCPNode) {
            ASCCPNode asccpNode = (ASCCPNode) data;
            if (asccpNode.getAscc().getSeqKey() > 1) {
                return true;
            }
        } else if (data instanceof BCCPNode) {
            BCCPNode bccpNode = (BCCPNode) data;
            if (bccpNode.getBcc().getSeqKey() > 1) {
                return true;
            }
        }

        return false;
    }

    public boolean canMoveDown(TreeNode selectedTreeNode) {
        if (selectedTreeNode == null) {
            return false;
        }

        TreeNode rootNode = getRootNode();
        if (rootNode != selectedTreeNode.getParent()) {
            return false;
        }

        int maxSeqKey = targetAccMaxSeqKey;

        Object data = selectedTreeNode.getData();
        if (data instanceof ASCCPNode) {
            ASCCPNode asccpNode = (ASCCPNode) data;
            int seqKey = asccpNode.getAscc().getSeqKey();
            if (maxSeqKey > seqKey) {
                return true;
            }
        } else if (data instanceof BCCPNode) {
            BCCPNode bccpNode = (BCCPNode) data;
            BasicCoreComponent bcc = bccpNode.getBcc();
            if (Attribute == bcc.getEntityType()) {
                return false;
            }

            int seqKey = bcc.getSeqKey();
            if (maxSeqKey > seqKey) {
                return true;
            }
        }

        return false;
    }

    public void moveUp(TreeNode selectedTreeNode) {
        TreeNode rootNode = getRootNode();
        List<TreeNode> children = rootNode.getChildren();
        int i = 0;
        for (int len = children.size(); i < len; ++i) {
            TreeNode child = children.get(i);

            if (child == selectedTreeNode) {
                TreeNode previousSibling = children.get(i - 1);
                switchSeqKey(child, previousSibling);
                reorderTreeNode(rootNode);
                break;
            }
        }
    }

    public void moveDown(TreeNode selectedTreeNode) {
        TreeNode rootNode = getRootNode();
        List<TreeNode> children = rootNode.getChildren();
        int i = 0;
        for (int len = children.size(); i < len; ++i) {
            TreeNode child = children.get(i);

            if (child == selectedTreeNode) {
                TreeNode nextSibling = children.get(i + 1);
                switchSeqKey(child, nextSibling);
                reorderTreeNode(rootNode);
                break;
            }
        }
    }

    private void switchSeqKey(TreeNode node1, TreeNode node2) {
        int seqKey1 = getSeqKey(node1);
        int seqKey2 = getSeqKey(node2);

        setSeqKey(node1, seqKey2);
        setSeqKey(node2, seqKey1);
    }

    private int getSeqKey(TreeNode node) {
        Object data = node.getData();
        if (data instanceof ASCCPNode) {
            ASCCPNode asccpNode = (ASCCPNode) data;
            return asccpNode.getAscc().getSeqKey();
        } else if (data instanceof BCCPNode) {
            BCCPNode bccpNode = (BCCPNode) data;
            return bccpNode.getBcc().getSeqKey();
        }
        throw new IllegalStateException();
    }

    private void setSeqKey(TreeNode node, int seqKey) {
        Object data = node.getData();
        if (data instanceof ASCCPNode) {
            ASCCPNode asccpNode = (ASCCPNode) data;
            asccpNode.getAscc().setSeqKey(seqKey);
        } else if (data instanceof BCCPNode) {
            BCCPNode bccpNode = (BCCPNode) data;
            bccpNode.getBcc().setSeqKey(seqKey);
        }
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

    public void onChangeBccEntityType(TreeNode selectedTreeNode) {

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

    public boolean isDisabled(CoreComponent coreComponent) {
        User currentUser = getCurrentUser();
        if (coreComponent instanceof AggregateCoreComponent) {
            AggregateCoreComponent acc = (AggregateCoreComponent) coreComponent;
            if (acc.getOwnerUserId() != currentUser.getAppUserId() || acc.getState() != Editing) {
                return true;
            }
        } else if (coreComponent instanceof AssociationCoreComponent) {
            AssociationCoreComponent ascc = (AssociationCoreComponent) coreComponent;
            return isDisabled(accRepository.findOne(ascc.getFromAccId()));
        } else if (coreComponent instanceof BasicCoreComponent) {
            BasicCoreComponent bcc = (BasicCoreComponent) coreComponent;
            return isDisabled(accRepository.findOne(bcc.getFromAccId()));
        } else if (coreComponent instanceof AssociationCoreComponentProperty) {
            AssociationCoreComponentProperty asccp = (AssociationCoreComponentProperty) coreComponent;
            if (asccp.getOwnerUserId() != currentUser.getAppUserId() || asccp.getState() != Editing) {
                return true;
            }
        } else if (coreComponent instanceof BasicCoreComponentProperty) {
            BasicCoreComponentProperty bccp = (BasicCoreComponentProperty) coreComponent;
            if (bccp.getOwnerUserId() != currentUser.getAppUserId() || bccp.getState() != Editing) {
                return true;
            }
        }

        return false;
    }

    @Transactional(rollbackFor = Throwable.class)
    public void updateAcc(TreeNode treeNode) {
        ACCNode accNode = (ACCNode) treeNode.getData();
        AggregateCoreComponent acc = accNode.getAcc();
        if (acc.isDirty()) {
            updateAcc(acc);
        }

        List<TreeNode> children = treeNode.getChildren();
        for (int i = 0, len = children.size(); i < len; ++i) {
            TreeNode child = children.get(i);
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

    private void updateAcc(AggregateCoreComponent acc) {
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
    public void discardAcc(TreeNode treeNode) throws IOException {
        ACCNode accNode = (ACCNode) treeNode.getData();
        if (!accNode.getChildren().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "Not allowed to discard the ACC which has children"));
            return;
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
            back();
        } else {
            reorderTreeNode(root);
        }
    }

    public void onChangeObjectClassTerm(TreeNode accTreeNode) {
        ACCNode accNode = (ACCNode) accTreeNode.getData();
        setNodeName(accNode);

        for (CCNode child : accNode.getChildren()) {
            if (child instanceof ASCCPNode) {
                ASCCPNode asccpNode = (ASCCPNode) child;
                AggregateCoreComponent acc = accNode.getAcc();
                AssociationCoreComponent ascc = asccpNode.getAscc();
                AssociationCoreComponentProperty asccp = asccpNode.getAsccp();
                ascc.setDen(acc, asccp);

                setNodeName(asccpNode);
            } else if (child instanceof BCCPNode) {
                BCCPNode bccpNode = (BCCPNode) child;
                AggregateCoreComponent acc = accNode.getAcc();
                BasicCoreComponent bcc = bccpNode.getBcc();
                BasicCoreComponentProperty bccp = bccpNode.getBccp();
                bcc.setDen(acc, bccp);

                setNodeName(bccpNode);
            }
        }
    }

    /*
     * Begin Set Based ACC
     */

    private boolean preparedSetBasedAcc;
    private List<AggregateCoreComponent> allAccList;
    private List<AggregateCoreComponent> accList;
    private String selectedAccObjectClassTerm;
    private AggregateCoreComponent selectedAcc;

    public boolean isPreparedSetBasedAcc() {
        return preparedSetBasedAcc;
    }

    public void setPreparedSetBasedAcc(boolean preparedSetBasedAcc) {
        this.preparedSetBasedAcc = preparedSetBasedAcc;
        setPreparedAppend(preparedSetBasedAcc);
    }

    public List<AggregateCoreComponent> getAccList() {
        return accList;
    }

    public void setAccList(List<AggregateCoreComponent> accList) {
        this.accList = accList;
    }

    public String getSelectedAccObjectClassTerm() {
        return selectedAccObjectClassTerm;
    }

    public void setSelectedAccObjectClassTerm(String selectedAccObjectClassTerm) {
        this.selectedAccObjectClassTerm = selectedAccObjectClassTerm;
    }

    public AggregateCoreComponent getSelectedAcc() {
        return selectedAcc;
    }

    public void setSelectedAcc(AggregateCoreComponent selectedAcc) {
        this.selectedAcc = selectedAcc;
    }

    public void onAccRowSelect(SelectEvent event) {
        setSelectedAcc((AggregateCoreComponent) event.getObject());
    }

    public void onAccRowUnselect(UnselectEvent event) {
        setSelectedAcc(null);
    }

    public void prepareSetBasedAcc() {
        allAccList = accRepository.findAllByRevisionNumAndStates(0, Arrays.asList(Published)).stream()
                .filter(e -> getTargetAcc().isAbstract() ? e.isAbstract() : true)
                .filter(e -> e.getAccId() != getTargetAcc().getAccId())
                .collect(Collectors.toList());

        /*
         * Issue #477
         *
         * The OAGi developers should be able to select only OAGi developers' components
         * when making a reference to a component
         */
        allAccList = coreComponentBeanHelper.filterByUser(allAccList, getCurrentUser(), AggregateCoreComponent.class);

        setAccList(allAccList.stream()
                .sorted(Comparator.comparing(AggregateCoreComponent::getObjectClassTerm))
                .collect(Collectors.toList())
        );
        setPreparedSetBasedAcc(true);
    }

    public List<String> completeInputAcc(String query) {
        return allAccList.stream()
                .map(e -> e.getObjectClassTerm())
                .distinct()
                .filter(e -> {
                    String lowercaseTerm = e.toLowerCase();
                    for (String token : query.toLowerCase().split(" ")) {
                        if (!lowercaseTerm.contains(token)) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    public void onSelectAccObjectClassTerm(SelectEvent event) {
        setSelectedAccObjectClassTerm(event.getObject().toString());
    }

    public void searchAcc() {
        String selectedObjectClassTerm = StringUtils.trimWhitespace(getSelectedAccObjectClassTerm());
        if (StringUtils.isEmpty(selectedObjectClassTerm)) {
            setAccList(allAccList.stream()
                    .sorted(Comparator.comparing(AggregateCoreComponent::getObjectClassTerm))
                    .collect(Collectors.toList()));
        } else {
            setAccList(
                    allAccList.stream()
                            .filter(e -> {
                                String lowercaseTerm = e.getObjectClassTerm().toLowerCase();
                                for (String token : selectedObjectClassTerm.toLowerCase().split(" ")) {
                                    if (!lowercaseTerm.contains(token)) {
                                        return false;
                                    }
                                }
                                return true;
                            })
                            .sorted((a, b) -> compareLevenshteinDistance(selectedObjectClassTerm, a, b,
                                    AggregateCoreComponent::getObjectClassTerm))
                            .collect(Collectors.toList())
            );
        }
        setPreparedSetBasedAcc(true);
    }

    @Transactional
    public void setBasedAcc() {
        AggregateCoreComponent selectedAcc = getSelectedAcc();
        AggregateCoreComponent targetAcc = getTargetAcc();

        long previousBasedAccId = targetAcc.getBasedAccId();
        targetAcc.setBasedAccId(selectedAcc.getAccId());

        updateAcc(targetAcc);

        TreeNode root = getRootNode();
        ((CCNode) root.getData()).reload();
        List<TreeNode> children = root.getChildren();
        ACCNode accNode = nodeService.createCoreComponentTreeNode(selectedAcc, true);
        if (!children.isEmpty()) {
            if (previousBasedAccId > 0L) {
                children.remove(0);
            }
        }
        children.add(0, toTreeNode(accNode, root));
        reorderTreeNode(root);
    }

    @Transactional
    public void discardBasedAcc() {
        AggregateCoreComponent targetAcc = getTargetAcc();
        long previousBasedAccId = targetAcc.getBasedAccId();
        targetAcc.setBasedAccId(null);

        updateAcc(targetAcc);

        TreeNode root = getRootNode();
        ((CCNode) root.getData()).reload();
        List<TreeNode> children = root.getChildren();
        if (previousBasedAccId > 0L) {
            children.remove(0);
        }
        reorderTreeNode(root);
    }


    /*
     * Begin Append ASCC
     */

    @Autowired
    private AssociationCoreComponentPropertyRepository asccpRepository;
    @Autowired
    private SimpleASCCPRepository simpleASCCPRepository;

    private boolean preparedAppendAscc;
    private List<SimpleASCCP> allAsccpList;
    private List<SimpleASCCP> asccpList;

    private String selectedAsccpPropertyTerm;
    private String selectedAsccpDefinition;
    private String selectedAsccpModule;

    private SimpleASCCP selectedAsccp;

    public boolean isPreparedAppendAscc() {
        return preparedAppendAscc;
    }

    public void setPreparedAppendAscc(boolean preparedAppendAscc) {
        this.preparedAppendAscc = preparedAppendAscc;
        setPreparedAppend(preparedAppendAscc);
    }

    public List<SimpleASCCP> getAsccpList() {
        return asccpList;
    }

    public void setAsccpList(List<SimpleASCCP> asccpList) {
        this.asccpList = asccpList;
    }

    public String getSelectedAsccpPropertyTerm() {
        return selectedAsccpPropertyTerm;
    }

    public void setSelectedAsccpPropertyTerm(String selectedAsccpPropertyTerm) {
        this.selectedAsccpPropertyTerm = selectedAsccpPropertyTerm;
    }

    public String getSelectedAsccpDefinition() {
        return selectedAsccpDefinition;
    }

    public void setSelectedAsccpDefinition(String selectedAsccpDefinition) {
        this.selectedAsccpDefinition = selectedAsccpDefinition;
    }

    public String getSelectedAsccpModule() {
        return selectedAsccpModule;
    }

    public void setSelectedAsccpModule(String selectedAsccpModule) {
        this.selectedAsccpModule = selectedAsccpModule;
    }

    public SimpleASCCP getSelectedAsccp() {
        return selectedAsccp;
    }

    public void setSelectedAsccp(SimpleASCCP selectedAsccp) {
        this.selectedAsccp = selectedAsccp;
    }

    public void onAsccpRowSelect(SelectEvent event) {
        setSelectedAsccp((SimpleASCCP) event.getObject());
    }

    public void onAsccpRowUnselect(UnselectEvent event) {
        setSelectedAsccp(null);
    }

    private Directory asccp_directory, asccp_definition_index;
    private static final String PROPERTY_TERM_FIELD = "property_term";
    private static final String MODULE_FIELD = "module";
    private static final String DEFINITION_FIELD = "definition";

    public void prepareAppendAscc() {
        allAsccpList = simpleASCCPRepository.findAll();

        /*
         * Issue #477
         *
         * The OAGi developers should be able to select only OAGi developers' components
         * when making a reference to a component
         */
        allAsccpList = coreComponentBeanHelper.filterByUser(allAsccpList, getCurrentUser(), SimpleASCCP.class);

        asccp_directory = createDirectory(allAsccpList,
                new String[]{PROPERTY_TERM_FIELD, MODULE_FIELD},
                new String[]{" ", Pattern.quote("\\")},
                SimpleASCCP::getPropertyTerm, SimpleASCCP::getModule);
        asccp_definition_index = createDirectoryForText(allAsccpList,
                new String[]{DEFINITION_FIELD},
                SimpleASCCP::getDefinition);

        searchAsccp();
        setPreparedAppendAscc(true);
    }

    public void searchAsccp() {
        String propertyTerm = getSelectedAsccpPropertyTerm();
        String definition = getSelectedAsccpDefinition();
        String module = getSelectedAsccpModule();

        List<SimpleASCCP> asccpList = allAsccpList;
        try {
            if (!StringUtils.isEmpty(definition)) {
                IndexReader reader = DirectoryReader.open(asccp_definition_index);
                IndexSearcher searcher = new IndexSearcher(reader);

                Query q = new QueryParser(DEFINITION_FIELD, new StandardAnalyzer()).parse(definition);
                TopDocs topDocs = searcher.search(q, asccpList.size());
                if (topDocs.totalHits == 0L) {
                    definition = Arrays.stream(definition.split(" "))
                            .map(e -> suggestWord(e, asccp_definition_index, DEFINITION_FIELD))
                            .collect(Collectors.joining(" "));

                    q = new QueryParser(DEFINITION_FIELD, new StandardAnalyzer()).parse(definition);
                    topDocs = searcher.search(q, asccpList.size());
                }

                List<SimpleASCCP> l = new ArrayList();
                for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                    Document d = searcher.doc(scoreDoc.doc);
                    l.add(toObject(d.getBinaryValue("obj").bytes, SimpleASCCP.class));
                }
                asccpList = l;
            }
        } catch (IOException | ParseException e) {
            throw new IllegalStateException(e);
        }

        asccpList = asccpList.stream()
                .filter(new SearchFilter<>(propertyTerm, asccp_directory, PROPERTY_TERM_FIELD, " ", SimpleASCCP::getPropertyTerm))
                .filter(new SearchFilter<>(module, asccp_directory, MODULE_FIELD, "\\", SimpleASCCP::getModule))
                .sorted((a, b) -> {
                    if (!StringUtils.isEmpty(propertyTerm)) {
                        return compareLevenshteinDistance(propertyTerm, a, b, SimpleASCCP::getPropertyTerm);
                    }
                    if (!StringUtils.isEmpty(module)) {
                        return compareLevenshteinDistance(module, a, b, SimpleASCCP::getModule, Pattern.quote("\\"));
                    }

                    return 0;
                })
                .collect(Collectors.toList());

        setAsccpList(asccpList);

        setPreparedAppendAscc(true);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void appendAscc() {
        SimpleASCCP selectedAsccpLookup = getSelectedAsccp();
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

        ASCCPNode asccpNode = nodeService.createCoreComponentTreeNode(result.getAscc(), true);
        TreeNode child = toTreeNode(asccpNode, rootNode);

        getSelectedTreeNode().setSelected(false);
        child.setSelected(true);
        setSelectedTreeNode(child);

        onUpdateTargetAccChildCount();
    }

    // End Append ASCC



    /*
     * Begin Append BCC
     */

    @Autowired
    private BasicCoreComponentPropertyRepository bccpRepository;
    @Autowired
    private SimpleBCCPRepository simpleBCCPRepository;

    private boolean preparedAppendBcc;
    private List<SimpleBCCP> allBccpList;
    private List<SimpleBCCP> bccpList;

    private String selectedBccpPropertyTerm;
    private String selectedBccpDefinition;
    private String selectedBccpModule;

    private SimpleBCCP selectedBccp;

    private Directory bccp_directory, bccp_definition_index;

    public boolean isPreparedAppendBcc() {
        return preparedAppendBcc;
    }

    public void setPreparedAppendBcc(boolean preparedAppendBcc) {
        this.preparedAppendBcc = preparedAppendBcc;
        setPreparedAppend(preparedAppendBcc);
    }

    public List<SimpleBCCP> getBccpList() {
        return bccpList;
    }

    public void setBccpList(List<SimpleBCCP> bccpList) {
        this.bccpList = bccpList;
    }

    public String getSelectedBccpPropertyTerm() {
        return selectedBccpPropertyTerm;
    }

    public void setSelectedBccpPropertyTerm(String selectedBccpPropertyTerm) {
        this.selectedBccpPropertyTerm = selectedBccpPropertyTerm;
    }

    public String getSelectedBccpDefinition() {
        return selectedBccpDefinition;
    }

    public void setSelectedBccpDefinition(String selectedBccpDefinition) {
        this.selectedBccpDefinition = selectedBccpDefinition;
    }

    public String getSelectedBccpModule() {
        return selectedBccpModule;
    }

    public void setSelectedBccpModule(String selectedBccpModule) {
        this.selectedBccpModule = selectedBccpModule;
    }

    public SimpleBCCP getSelectedBccp() {
        return selectedBccp;
    }

    public void setSelectedBccp(SimpleBCCP selectedBccp) {
        this.selectedBccp = selectedBccp;
    }

    public void onBccpRowSelect(SelectEvent event) {
        setSelectedBccp((SimpleBCCP) event.getObject());
    }

    public void onBccpRowUnselect(UnselectEvent event) {
        setSelectedBccp(null);
    }


    public void prepareAppendBcc() {
        allBccpList = simpleBCCPRepository.findAll();

        /*
         * Issue #477
         *
         * The OAGi developers should be able to select only OAGi developers' components
         * when making a reference to a component
         */
        allBccpList = coreComponentBeanHelper.filterByUser(allBccpList, getCurrentUser(), SimpleBCCP.class);

        bccp_directory = createDirectory(allBccpList,
                new String[]{PROPERTY_TERM_FIELD, MODULE_FIELD},
                new String[]{" ", Pattern.quote("\\")},
                SimpleBCCP::getPropertyTerm, SimpleBCCP::getModule);
        bccp_definition_index = createDirectoryForText(allBccpList,
                new String[]{DEFINITION_FIELD},
                SimpleBCCP::getDefinition);

        searchBccp();

        setPreparedAppendBcc(true);
    }

    public void searchBccp() {
        String propertyTerm = getSelectedBccpPropertyTerm();
        String definition = getSelectedBccpDefinition();
        String module = getSelectedBccpModule();

        List<SimpleBCCP> bccpList = allBccpList;
        try {
            if (!StringUtils.isEmpty(definition)) {
                IndexReader reader = DirectoryReader.open(bccp_definition_index);
                IndexSearcher searcher = new IndexSearcher(reader);

                Query q = new QueryParser(DEFINITION_FIELD, new StandardAnalyzer()).parse(definition);
                TopDocs topDocs = searcher.search(q, bccpList.size());
                if (topDocs.totalHits == 0L) {
                    definition = Arrays.stream(definition.split(" "))
                            .map(e -> suggestWord(e, bccp_definition_index, DEFINITION_FIELD))
                            .collect(Collectors.joining(" "));

                    q = new QueryParser(DEFINITION_FIELD, new StandardAnalyzer()).parse(definition);
                    topDocs = searcher.search(q, bccpList.size());
                }

                List<SimpleBCCP> l = new ArrayList();
                for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                    Document d = searcher.doc(scoreDoc.doc);
                    l.add(toObject(d.getBinaryValue("obj").bytes, SimpleBCCP.class));
                }
                bccpList = l;
            }
        } catch (IOException | ParseException e) {
            throw new IllegalStateException(e);
        }

        bccpList = bccpList.stream()
                .filter(new SearchFilter<>(propertyTerm, bccp_directory, PROPERTY_TERM_FIELD, " ", SimpleBCCP::getPropertyTerm))
                .filter(new SearchFilter<>(module, bccp_directory, MODULE_FIELD, "\\", SimpleBCCP::getModule))
                .sorted((a, b) -> {
                    if (!StringUtils.isEmpty(propertyTerm)) {
                        return compareLevenshteinDistance(propertyTerm, a, b, SimpleBCCP::getPropertyTerm);
                    }
                    if (!StringUtils.isEmpty(module)) {
                        return compareLevenshteinDistance(module, a, b, SimpleBCCP::getModule, Pattern.quote("\\"));
                    }

                    return 0;
                })
                .collect(Collectors.toList());

        setBccpList(bccpList);

        setPreparedAppendBcc(true);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void appendBcc() {
        SimpleBCCP selectedBccpLookup = getSelectedBccp();
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

        BCCPNode bccpNode = nodeService.createCoreComponentTreeNode(result.getBcc(), true);
        TreeNode child = toTreeNode(bccpNode, rootNode);

        getSelectedTreeNode().setSelected(false);
        child.setSelected(true);
        setSelectedTreeNode(child);

        onUpdateTargetAccChildCount();
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
        discardAscc(asccpNode);
    }

    private void discardAscc(ASCCPNode asccpNode) {
        AssociationCoreComponent ascc = asccpNode.getAscc();
        User requester = getCurrentUser();

        try {
            if (hasMultipleRevisions(asccpNode)) {
                undoService.revertToPreviousRevision(ascc);
                refreshTreeNode();
            } else {
                coreComponentService.discard(ascc, requester);
            }
        } catch (Throwable t) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", t.getMessage()));
            throw t;
        }

        setSelectedTreeNode(null);

        TreeNode rootNode = getRootNode();
        List<TreeNode> children = rootNode.getChildren();
        for (TreeNode child : rootNode.getChildren()) {
            if (child.getData() == asccpNode) {
                children.remove(child);
                break;
            }
        }

        TreeNode root = getTreeNode();
        reorderTreeNode(root);

        onUpdateTargetAccChildCount();
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
        discardBcc(bccpNode);
    }

    private void discardBcc(BCCPNode bccpNode) {
        BasicCoreComponent bcc = bccpNode.getBcc();
        User requester = getCurrentUser();

        try {
            if (hasMultipleRevisions(bccpNode)) {
                undoService.revertToPreviousRevision(bcc);
                refreshTreeNode();
            } else {
                coreComponentService.discard(bcc, requester);
            }
        } catch (Throwable t) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", t.getMessage()));
            throw t;
        }

        setSelectedTreeNode(null);

        TreeNode rootNode = getRootNode();
        List<TreeNode> children = rootNode.getChildren();
        for (TreeNode child : rootNode.getChildren()) {
            if (child.getData() == bccpNode) {
                children.remove(child);
                break;
            }
        }

        TreeNode root = getTreeNode();
        reorderTreeNode(root);

        onUpdateTargetAccChildCount();
    }

    public boolean canBeDiscard(CCNode node) {
        User currentUser = getCurrentUser();

        if (node instanceof ASCCPNode) {
            ASCCPNode asccpNode = (ASCCPNode) node;
            AssociationCoreComponent ascc = asccpNode.getAscc();
            AggregateCoreComponent acc = accRepository.findOne(ascc.getFromAccId());

            if (acc.getOwnerUserId() == currentUser.getAppUserId() && Editing == ascc.getState()) {
                return true;
            }
        } else if (node instanceof BCCPNode) {
            BCCPNode bccpNode = (BCCPNode) node;
            BasicCoreComponent bcc = bccpNode.getBcc();
            AggregateCoreComponent acc = accRepository.findOne(bcc.getFromAccId());

            if (acc.getOwnerUserId() == currentUser.getAppUserId() && Editing == bcc.getState()) {
                return true;
            }
        }

        return false;
    }

    @Transactional(rollbackFor = Throwable.class)
    public void discard(CCNode node) {
        if (node instanceof ASCCPNode) {
            ASCCPNode asccpNode = (ASCCPNode) node;
            discardAscc(asccpNode);
            selectedTreeNode = null;
        } else if (node instanceof BCCPNode) {
            BCCPNode bccpNode = (BCCPNode) node;
            discardBcc(bccpNode);
            selectedTreeNode = null;
        }
    }

    public boolean canBeRevised(CCNode node) {
        if (targetAcc.getState() == Published) {
            return false;
        }

        if (node instanceof ASCCPNode) {
            ASCCPNode asccpNode = (ASCCPNode) node;
            AssociationCoreComponent ascc = asccpNode.getAscc();
            if (ascc.getDen().contains(". Extension.")) {
                return false;
            }
            if (Published == ascc.getState()) {
                return true;
            }
        } else if (node instanceof BCCPNode) {
            BCCPNode bccpNode = (BCCPNode) node;
            BasicCoreComponent bcc = bccpNode.getBcc();
            if (bcc.getDen().contains(". Extension.")) {
                return false;
            }
            if (Published == bcc.getState()) {
                return true;
            }
        }

        return false;
    }

    @Transactional(rollbackFor = Throwable.class)
    public void revise(CCNode node) {
        User requester = getCurrentUser();
        if (node instanceof ASCCPNode) {
            ASCCPNode asccpNode = (ASCCPNode) node;
            AssociationCoreComponent ascc = asccpNode.getAscc();
            coreComponentService.newAssociationCoreComponentRevision(requester, ascc);
        } else if (node instanceof BCCPNode) {
            BCCPNode bccpNode = (BCCPNode) node;
            BasicCoreComponent bcc = bccpNode.getBcc();
            coreComponentService.newBasicCoreComponentRevision(requester, bcc);
        }
        refreshTreeNode();
    }

    @Transactional(rollbackFor = Throwable.class)
    public void update() {
        updateAcc(getRootNode());
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

    public void back() throws IOException {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        externalContext.redirect("/core_component");
    }

    /*
     * Create New Revision
     */
    public boolean hasPreviousRevision(AggregateCoreComponent acc) {
        Long accId = acc.getAccId();
        List<AggregateCoreComponent> latestRevisionNumAccs =
                accRepository.findAllWithLatestRevisionNumByCurrentAccId(accId);
        return !latestRevisionNumAccs.isEmpty();
    }

    public boolean isPreviousRevisionNotInsert(AggregateCoreComponent acc) {
        return undoService.isPreviousRevisionNotInsert(acc);
    }

    @Transactional
    public void createNewRevision(AggregateCoreComponent acc) throws IOException {
        User requester = getCurrentUser();
        acc = coreComponentService.newAggregateCoreComponentRevision(requester, acc);
        setTargetAcc(acc);

        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        externalContext.redirect("/core_component/acc/" + acc.getAccId());
    }

    public void undoLastAction() {
        undoService.undoLastAction(getTargetAcc());
        refreshTreeNode();
    }

    private void refreshTreeNode() {
        TreeNode treeNode = createTreeNode(targetAcc, true);
        copyExpandedState(this.treeNode, treeNode);
        setTreeNode(treeNode);
        setSelectedTreeNodeAfterRefresh = true;
        if (getSelectedTreeNode() != null) {
            selectedNodeAfterRefresh = findChildNodeAnywhereById(treeNode, ((SRTNode) getSelectedTreeNode().getData()).getId()); // TODO: MIRO check if this cause problem with setting leaf node as selected after refresh
        }
    }

    public String getDiscardIcon(CCNode node) {
        if (node instanceof ASCCPNode) {
            AssociationCoreComponent ascc = ((ASCCPNode) node).getAscc();
            if (undoService.hasMultipleRevisions(ascc)) {
                return "fa fa-white fa-undo";
            } else {
                return "fa fa-white fa-times";
            }
        } else if (node instanceof BCCPNode) {
            BasicCoreComponent bcc = ((BCCPNode) node).getBcc();
            if (undoService.hasMultipleRevisions(bcc)) {
                return "fa fa-white fa-undo";
            } else {
                return "fa fa-white fa-times";
            }
        } else {
            return "";
        }
    }

    public boolean hasMultipleRevisions(CCNode node) {
        if (node instanceof ASCCPNode) {
            AssociationCoreComponent ascc = ((ASCCPNode) node).getAscc();
            return undoService.hasMultipleRevisions(ascc);
        } else if (node instanceof BCCPNode) {
            BasicCoreComponent bcc = ((BCCPNode) node).getBcc();
            return undoService.hasMultipleRevisions(bcc);
        } else {
            return false;
        }
    }
}

