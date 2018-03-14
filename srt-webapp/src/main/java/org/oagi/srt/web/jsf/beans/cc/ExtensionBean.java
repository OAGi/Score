package org.oagi.srt.web.jsf.beans.cc;

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
import org.oagi.srt.common.lucene.CaseSensitiveStandardAnalyzer;
import org.oagi.srt.model.node.ASCCPNode;
import org.oagi.srt.model.node.BCCPNode;
import org.oagi.srt.model.node.CCNode;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.oagi.srt.service.CoreComponentService;
import org.oagi.srt.service.ExtensionService;
import org.oagi.srt.service.NodeService;
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
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.oagi.srt.common.util.Utility.*;
import static org.oagi.srt.repository.entity.BasicCoreComponentEntityType.Attribute;
import static org.oagi.srt.repository.entity.CoreComponentState.Published;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
@Transactional(readOnly = true)
public class ExtensionBean extends BaseCoreComponentDetailBean {

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
    private CoreComponentBeanHelper coreComponentBeanHelper;

    private long releaseId;
    private AggregateCoreComponent targetAcc;
    private AggregateCoreComponent userExtensionAcc;

    private TreeNode treeNode;
    private TreeNode selectedTreeNode;

    private DataType selectedBdt;

    private static final String PROPERTY_TERM_FIELD = "property_term";
    private static final String MODULE_FIELD = "module";
    private static final String DEFINITION_FIELD = "definition";

    @PostConstruct
    public void init() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, String> requestParameterMap = externalContext.getRequestParameterMap();

        String accId = requestParameterMap.get("accId");
        AggregateCoreComponent ueAcc = accRepository.findOne(Long.parseLong(accId));
        setUserExtensionAcc(ueAcc);

        List<AssociationCoreComponentProperty> asccpList =
                asccpRepository.findByRoleOfAccId(ueAcc.getAccId()).stream()
                        .filter(e -> e.getReleaseId() == 0L)
                        .collect(Collectors.toList());
        assert asccpList.size() == 1;

        List<AssociationCoreComponent> asccList =
                asccRepository.findByToAsccpIdAndRevisionNumAndState(asccpList.get(0).getAsccpId(), 1, Published);
        assert asccList.size() == 1;

        AssociationCoreComponent ascc = asccList.get(0);
        releaseId = ascc.getReleaseId();

        AggregateCoreComponent targetAcc = coreComponentService.findAcc(ascc.getFromAccId(), releaseId);
        setTargetAcc(targetAcc);

        boolean enableShowingGroup = true;
        TreeNode treeNode = createTreeNode(targetAcc, releaseId, enableShowingGroup);
        setTreeNode(treeNode);
    }

    @Override
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

    /*
     * Begin Append ASCC
     */

    @Autowired
    private AssociationCoreComponentPropertyRepository asccpRepository;
    @Autowired
    private SimpleASCCPRepository simpleASCCPRepository;

    private boolean preparedAppendAscc;
    private List<SimpleASCCP> allAsccpList;
    private Map<Long, SimpleASCCP> allAsccpMap;
    private List<SimpleASCCP> asccpList;

    private String selectedAsccpPropertyTerm;
    private String selectedAsccpDefinition;
    private String selectedAsccpModule;

    private SimpleASCCP selectedAsccp;

    private Directory asccp_directory, asccp_definition_index;

    private void resetAsccpStates() {
        this.selectedAsccp = null;
        this.asccpCheckBoxes = new HashMap();
    }

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
        if (selectedAsccp != null) {
            getAsccpCheckBox(selectedAsccp.getAsccpId()).setChecked(true);
        }
    }

    public void onAsccpRowSelect(SelectEvent event) {
        getAsccpCheckBox(((SimpleASCCP) event.getObject()).getAsccpId()).setChecked(true);
    }

    public void onAsccpRowUnselect(UnselectEvent event) {
        getAsccpCheckBox(((SimpleASCCP) event.getObject()).getAsccpId()).setChecked(false);
    }

    private Map<Long, Boolean> asccpCheckBoxes = new HashMap();
    public class AsccpCheckBox {
        private Long asccpId;

        public AsccpCheckBox(Long asccpId) {
            this.asccpId = asccpId;
        }

        public boolean isChecked() {
            return asccpCheckBoxes.getOrDefault(asccpId, false);
        }

        public void setChecked(boolean value) {
            asccpCheckBoxes = new HashMap();

            if (value) {
                SimpleASCCP previousOne = getSelectedAsccp();
                if (previousOne != null) {
                    asccpCheckBoxes.put(previousOne.getAsccpId(), false);
                }

                selectedAsccp = allAsccpMap.get(asccpId);
            } else {
                selectedAsccp = null;
            }

            asccpCheckBoxes.put(asccpId, value);
        }
    }

    public AsccpCheckBox getAsccpCheckBox(Long asccpId) {
        return new AsccpCheckBox(asccpId);
    }

    public void prepareAppendAscc() {
        allAsccpList = simpleASCCPRepository.findAll();

        /*
         * Issue #477
         *
         * The OAGi developers should be able to select only OAGi developers' components
         * when making a reference to a component
         */
        allAsccpList = coreComponentBeanHelper.filterByUser(allAsccpList, getCurrentUser(), SimpleASCCP.class);
        allAsccpMap = allAsccpList.stream()
                .collect(Collectors.toMap(SimpleASCCP::getAsccpId, Function.identity()));

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

    public void onClickAppendASCCButton() {
        setSelectedAsccpPropertyTerm(null);
        setSelectedAsccpDefinition(null);
        setSelectedAsccpModule(null);

        resetAsccpStates();
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

                Query q = new QueryParser(DEFINITION_FIELD, new CaseSensitiveStandardAnalyzer()).parse(definition);
                TopDocs topDocs = searcher.search(q, asccpList.size());
                if (topDocs.totalHits == 0L) {
                    definition = Arrays.stream(definition.split(" "))
                            .map(e -> suggestWord(e, asccp_definition_index, DEFINITION_FIELD))
                            .collect(Collectors.joining(" "));

                    q = new QueryParser(DEFINITION_FIELD, new CaseSensitiveStandardAnalyzer()).parse(definition);
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
        resetAsccpStates();
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
        AggregateCoreComponent pAcc = getUserExtensionAcc();

        if (extensionService.exists(pAcc, tAsccp)) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "You cannot associate the same component."));
            return;
        }

        User user = getCurrentUser();
        ExtensionService.AppendAsccResult result = extensionService.appendAsccTo(pAcc, tAsccp, releaseId, user);

        TreeNode rootNode = getRootNode();
        ((CCNode) rootNode.getData()).reload();

        ASCCPNode asccpNode =
                nodeService.createCoreComponentTreeNode(result.getAscc(), true);
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
    @Autowired
    private SimpleBCCPRepository simpleBCCPRepository;

    private boolean preparedAppendBcc;
    private List<SimpleBCCP> allBccpList;
    private Map<Long, SimpleBCCP> allBccpMap;
    private List<SimpleBCCP> bccpList;

    private String selectedBccpPropertyTerm;
    private String selectedBccpDefinition;
    private String selectedBccpModule;

    private SimpleBCCP selectedBccp;

    private Directory bccp_directory, bccp_definition_index;

    private void resetBccpStates() {
        this.selectedBccp = null;
        this.bccpCheckBoxes = new HashMap();
    }

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
        if (selectedBccp != null) {
            getBccpCheckBox(selectedBccp.getBccpId()).setChecked(true);
        }
    }

    public void onBccpRowSelect(SelectEvent event) {
        getBccpCheckBox(((SimpleBCCP) event.getObject()).getBccpId()).setChecked(true);
    }

    public void onBccpRowUnselect(UnselectEvent event) {
        getBccpCheckBox(((SimpleBCCP) event.getObject()).getBccpId()).setChecked(false);
    }

    private Map<Long, Boolean> bccpCheckBoxes = new HashMap();
    public class BccpCheckBox {
        private Long bccpId;

        public BccpCheckBox(Long bccpId) {
            this.bccpId = bccpId;
        }

        public boolean isChecked() {
            return bccpCheckBoxes.getOrDefault(bccpId, false);
        }

        public void setChecked(boolean value) {
            bccpCheckBoxes = new HashMap();

            if (value) {
                SimpleBCCP previousOne = getSelectedBccp();
                if (previousOne != null) {
                    bccpCheckBoxes.put(previousOne.getBccpId(), false);
                }

                selectedBccp = allBccpMap.get(bccpId);
            } else {
                selectedBccp = null;
            }

            bccpCheckBoxes.put(bccpId, value);
        }
    }

    public BccpCheckBox getBccpCheckBox(Long asccpId) {
        return new BccpCheckBox(asccpId);
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
        allBccpMap = allBccpList.stream()
                .collect(Collectors.toMap(SimpleBCCP::getBccpId, Function.identity()));

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

    public void onClickAppendBCCButton() {
        setSelectedBccpPropertyTerm(null);
        setSelectedBccpDefinition(null);
        setSelectedBccpModule(null);

        resetBccpStates();
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

                Query q = new QueryParser(DEFINITION_FIELD, new CaseSensitiveStandardAnalyzer()).parse(definition);
                TopDocs topDocs = searcher.search(q, bccpList.size());
                if (topDocs.totalHits == 0L) {
                    definition = Arrays.stream(definition.split(" "))
                            .map(e -> suggestWord(e, bccp_definition_index, DEFINITION_FIELD))
                            .collect(Collectors.joining(" "));

                    q = new QueryParser(DEFINITION_FIELD, new CaseSensitiveStandardAnalyzer()).parse(definition);
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
        resetBccpStates();
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
        AggregateCoreComponent pAcc = getUserExtensionAcc();

        if (extensionService.exists(pAcc, tBccp)) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "You cannot associate the same component."));
            return;
        }

        User user = getCurrentUser();
        ExtensionService.AppendBccResult result = extensionService.appendBccTo(pAcc, tBccp, releaseId, user);

        TreeNode rootNode = getRootNode();
        ((CCNode) rootNode.getData()).reload();

        BCCPNode bccpNode =
                nodeService.createCoreComponentTreeNode(result.getBcc(), false);
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
}
