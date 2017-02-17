package org.oagi.srt.web.jsf.beans.bod;

import org.oagi.srt.model.BIENode;
import org.oagi.srt.model.treenode.AssociationBusinessInformationEntityPropertyTreeNode;
import org.oagi.srt.model.treenode.BasicBusinessInformationEntityPropertyTreeNode;
import org.oagi.srt.model.treenode.BasicBusinessInformationEntityRestrictionType;
import org.oagi.srt.model.treenode.BasicBusinessInformationEntitySupplementaryComponentTreeNode;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.oagi.srt.service.BusinessInformationEntityService;
import org.oagi.srt.service.ExtensionService;
import org.oagi.srt.service.NodeService;
import org.oagi.srt.service.TreeNodeService;
import org.oagi.srt.web.jsf.component.treenode.BIETreeNodeHandler;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

import static org.oagi.srt.repository.entity.AggregateBusinessInformationEntityState.Candidate;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
@Transactional(readOnly = true)
public class EditProfileBODBean extends AbstractProfileBODBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TreeNodeService treeNodeService;
    @Autowired
    private BIETreeNodeHandler bieTreeNodeHandler;
    @Autowired
    private BusinessInformationEntityService bieService;
    @Autowired
    private ExtensionService extensionService;
    @Autowired
    private NodeService nodeService;
    @Autowired
    private TopLevelAbieRepository topLevelAbieRepository;
    @Autowired
    private AggregateBusinessInformationEntityRepository abieRepository;
    @Autowired
    private AggregateCoreComponentRepository accRepository;
    @Autowired
    private AssociationCoreComponentRepository asccRepository;
    @Autowired
    private AssociationCoreComponentPropertyRepository asccpRepository;
    @Autowired
    private AssociationBusinessInformationEntityRepository asbieRepository;
    @Autowired
    private AssociationBusinessInformationEntityPropertyRepository asbiepRepository;
    @Autowired
    private BusinessInformationEntityUserExtensionRevisionRepository bieUserExtRevisionRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ModuleRepository moduleRepository;

    private TopLevelAbie topLevelAbie;
    private List<BusinessInformationEntityUserExtensionRevision> bieUserExtRevisionList;
    private TreeNode selectedTreeNode;

    private String selectedCodeListName;

    @PostConstruct
    public void init() {
        Long topLevelAbieId = Long.parseLong(
                FacesContext.getCurrentInstance().getExternalContext()
                        .getRequestParameterMap().get("topLevelAbieId"));
        TopLevelAbie topLevelAbie = topLevelAbieRepository.findOne(topLevelAbieId);
        if (topLevelAbie == null) {
            return;
        }
        setTopLevelAbie(topLevelAbie);

        createTreeNode(topLevelAbie);

        List<BusinessInformationEntityUserExtensionRevision> bieUserExtRevisionList =
                bieUserExtRevisionRepository.findByTopLevelAbieId(topLevelAbieId);
        setBieUserExtRevisionList(bieUserExtRevisionList);

        if (!bieUserExtRevisionList.isEmpty()) {
            RequestContext.getCurrentInstance().execute("PF('notifyExtensionChange').show()");
        }
    }

    public TopLevelAbie getTopLevelAbie() {
        return topLevelAbie;
    }

    public void setTopLevelAbie(TopLevelAbie topLevelAbie) {
        this.topLevelAbie = topLevelAbie;
    }

    public List<BusinessInformationEntityUserExtensionRevision> getBieUserExtRevisionList() {
        return bieUserExtRevisionList;
    }

    public void setBieUserExtRevisionList(List<BusinessInformationEntityUserExtensionRevision> bieUserExtRevisionList) {
        this.bieUserExtRevisionList = bieUserExtRevisionList;
    }

    public String encode(BusinessInformationEntityUserExtensionRevision bieUserExtRevision) {
        TopLevelAbie topLevelAbie = bieUserExtRevision.getTopLevelAbie();
        AssociationCoreComponentProperty asccp = getAsccpOfTopLevelNode(topLevelAbie);

        Stack<AssociationCoreComponentProperty> asccpStack = new Stack();
        asccpStack.push(asccp);

        AggregateBusinessInformationEntity abie = topLevelAbie.getAbie();
        AggregateCoreComponent acc = accRepository.findOne(abie.getBasedAccId());
        AggregateCoreComponent eAcc = bieUserExtRevision.getExtAcc();
        traverseToFindTargetAcc(asccpStack, acc, eAcc);

        StringBuilder sb = new StringBuilder();
        for (int i = 0, len = asccpStack.size(); i < len; ++i) {
            asccp = asccpStack.get(i);
            sb.append("<span class=\"ASCCP\">" + asccp.getPropertyTerm() + "</span>");
            if ((i + 1) != len) {
                sb.append("<i class=\"fa fa-angle-double-right\" aria-hidden=\"true\" style=\"padding: 0 5px 0 5px;\"></i>");
            }
        }

        return sb.toString();
    }

    @Transactional(rollbackFor = Throwable.class)
    public void uptakeExtensions(List<BusinessInformationEntityUserExtensionRevision> bieUserExtRevisionList) {
        try {
            TopLevelAbie topLevelAbie = getTopLevelAbie();
            if (Candidate == topLevelAbie.getState()) {
                RequestContext.getCurrentInstance().execute("PF('confirmChangeStateToEditing').show()");
            } else {
                for (BusinessInformationEntityUserExtensionRevision bieUserExtRevision : bieUserExtRevisionList) {
                    BIENode userExtBieNode = nodeService.createBIENode(bieUserExtRevision);
                    bieTreeNodeHandler.append(userExtBieNode, topLevelAbie);
                }

                discard(bieUserExtRevisionList);
            }
        } finally {
            RequestContext.getCurrentInstance().execute("PF('loadingBlock').hide()");
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void uptakeExtensionsWithChangingState(List<BusinessInformationEntityUserExtensionRevision> bieUserExtRevisionList,
                                                  AggregateBusinessInformationEntityState state) {
        updateState(state);

        TopLevelAbie topLevelAbie = getTopLevelAbie();
        topLevelAbie = topLevelAbieRepository.findOne(topLevelAbie.getTopLevelAbieId());
        setTopLevelAbie(topLevelAbie);

        uptakeExtensions(bieUserExtRevisionList);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void discard(List<BusinessInformationEntityUserExtensionRevision> bieUserExtRevisionList) {
        bieUserExtRevisionRepository.delete(bieUserExtRevisionList);
        setBieUserExtRevisionList(null);
    }

    private AssociationCoreComponentProperty getAsccpOfTopLevelNode(TopLevelAbie topLevelAbie) {
        AggregateBusinessInformationEntity abie = topLevelAbie.getAbie();
        AssociationBusinessInformationEntityProperty asbiep = asbiepRepository.findOneByRoleOfAbieId(abie.getAbieId());
        AssociationCoreComponentProperty asccp = asccpRepository.findOne(asbiep.getBasedAsccpId());
        return asccp;
    }

    private boolean traverseToFindTargetAcc(Stack<AssociationCoreComponentProperty> asccpStack,
                                            AggregateCoreComponent sourceAcc,
                                            AggregateCoreComponent targetAcc) {

        long targetAccId = targetAcc.getAccId();
        long fromAccId = sourceAcc.getAccId();

        List<AssociationCoreComponent> asccList = asccRepository.findByFromAccId(fromAccId);
        for (AssociationCoreComponent ascc : asccList) {
            long toAsccpId = ascc.getToAsccpId();
            AssociationCoreComponentProperty asccp = asccpRepository.findOne(toAsccpId);
            asccpStack.push(asccp);

            long roleOfAccId = asccp.getRoleOfAccId();
            if (targetAccId == roleOfAccId) {
                // we've found it
                return true;

            } else {
                fromAccId = asccp.getRoleOfAccId();
                sourceAcc = accRepository.findOne(fromAccId);
                boolean result = traverseToFindTargetAcc(asccpStack, sourceAcc, targetAcc);
                if (result) {
                    return true;
                } else {
                    asccpStack.pop();
                }
            }
        }

        return false;
    }

    public TreeNode getSelectedTreeNode() {
        return selectedTreeNode;
    }

    public void setSelectedTreeNode(TreeNode selectedTreeNode) {
        this.selectedTreeNode = selectedTreeNode;
    }

    public String getModule(long moduleId) {
        return moduleRepository.findModuleByModuleId(moduleId);
    }

    /*
     * handle BBIE Type
     */
    public Map<BasicBusinessInformationEntityRestrictionType, BasicBusinessInformationEntityRestrictionType>
                                getAvailablePrimitiveRestrictions(BasicBusinessInformationEntityPropertyTreeNode node) {
        return bieService.getAvailablePrimitiveRestrictions(node);
    }

    private BasicBusinessInformationEntityPropertyTreeNode getSelectedBasicBusinessInformationEntityPropertyTreeNode() {
        TreeNode treeNode = getSelectedTreeNode();
        Object data = treeNode.getData();
        if (!(data instanceof BasicBusinessInformationEntityPropertyTreeNode)) {
            return null;
        }
        return (BasicBusinessInformationEntityPropertyTreeNode) data;
    }

    public String getBbieXbtName() {
        return bieService.getBdtPrimitiveRestrictionName(getSelectedBasicBusinessInformationEntityPropertyTreeNode());
    }

    public void setBbieXbtName(String name) {
        bieService.setBdtPrimitiveRestriction(getSelectedBasicBusinessInformationEntityPropertyTreeNode(), name);
    }

    public void onSelectBbieXbtName(SelectEvent event) {
        setBbieXbtName(event.getObject().toString());
    }

    public List<String> completeInputForBbieXbt(String query) {
        BasicBusinessInformationEntityPropertyTreeNode node = getSelectedBasicBusinessInformationEntityPropertyTreeNode();
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
        BasicBusinessInformationEntityPropertyTreeNode node = getSelectedBasicBusinessInformationEntityPropertyTreeNode();
        Map<String, CodeList> codeListMap = bieService.getCodeLists(node);
        CodeList codeList = codeListMap.get(name);
        if (codeList != null) {
            node.getBasicBusinessInformationEntity().setCodeListId(codeList.getCodeListId());
        }
    }

    public void onSelectBbieCodeListName(SelectEvent event) {
        setBbieCodeListName(event.getObject().toString());
    }

    public List<String> completeInputForBbieCodeList(String query) {
        BasicBusinessInformationEntityPropertyTreeNode node = getSelectedBasicBusinessInformationEntityPropertyTreeNode();
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
        BasicBusinessInformationEntityPropertyTreeNode node = getSelectedBasicBusinessInformationEntityPropertyTreeNode();
        Map<String, AgencyIdList> agencyIdListMap = bieService.getAgencyIdListIds(node);
        AgencyIdList agencyIdList = agencyIdListMap.get(name);
        if (agencyIdList != null) {
            node.getBasicBusinessInformationEntity().setAgencyIdListId(agencyIdList.getAgencyIdListId());
        }
    }

    public void onSelectBbieAgencyIdListName(SelectEvent event) {
        setBbieAgencyIdListName(event.getObject().toString());
    }

    public List<String> completeInputForBbieAgencyIdList(String query) {
        BasicBusinessInformationEntityPropertyTreeNode node = getSelectedBasicBusinessInformationEntityPropertyTreeNode();
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
    public Map<BasicBusinessInformationEntityRestrictionType, BasicBusinessInformationEntityRestrictionType> getAvailableScPrimitiveRestrictions(BasicBusinessInformationEntitySupplementaryComponentTreeNode node) {
        return bieService.getAvailablePrimitiveRestrictions(node);
    }

    private BasicBusinessInformationEntitySupplementaryComponentTreeNode getSelectedBasicBusinessInformationEntitySupplementaryComponentTreeNode() {
        TreeNode treeNode = getSelectedTreeNode();
        Object data = treeNode.getData();
        if (!(data instanceof BasicBusinessInformationEntitySupplementaryComponentTreeNode)) {
            return null;
        }
        return (BasicBusinessInformationEntitySupplementaryComponentTreeNode) data;
    }

    public String getBbieScXbtName() {
        return bieService.getBdtScPrimitiveRestrictionName(getSelectedBasicBusinessInformationEntitySupplementaryComponentTreeNode());
    }

    public void setBbieScXbtName(String name) {
        bieService.setBdtScPrimitiveRestriction(getSelectedBasicBusinessInformationEntitySupplementaryComponentTreeNode(), name);
    }

    public void onSelectBbieScXbtName(SelectEvent event) {
        setBbieScXbtName(event.getObject().toString());
    }

    public List<String> completeInputForBbieScXbt(String query) {
        BasicBusinessInformationEntitySupplementaryComponentTreeNode node = getSelectedBasicBusinessInformationEntitySupplementaryComponentTreeNode();
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
        BasicBusinessInformationEntitySupplementaryComponentTreeNode node = getSelectedBasicBusinessInformationEntitySupplementaryComponentTreeNode();
        Map<String, CodeList> codeListMap = bieService.getCodeLists(node);
        CodeList codeList = codeListMap.get(name);
        if (codeList != null) {
            node.getBasicBusinessInformationEntitySupplementaryComponent().setCodeListId(codeList.getCodeListId());
        }
    }

    public void onSelectBbieScCodeListName(SelectEvent event) {
        setBbieScCodeListName(event.getObject().toString());
    }

    public List<String> completeInputForBbieScCodeList(String query) {
        BasicBusinessInformationEntitySupplementaryComponentTreeNode node = getSelectedBasicBusinessInformationEntitySupplementaryComponentTreeNode();
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
        BasicBusinessInformationEntitySupplementaryComponentTreeNode node = getSelectedBasicBusinessInformationEntitySupplementaryComponentTreeNode();
        Map<String, AgencyIdList> agencyIdListMap = bieService.getAgencyIdListIds(node);
        AgencyIdList agencyIdList = agencyIdListMap.get(name);
        if (agencyIdList != null) {
            node.getBasicBusinessInformationEntitySupplementaryComponent().setAgencyIdListId(agencyIdList.getAgencyIdListId());
        }
    }

    public void onSelectBbieScAgencyIdListName(SelectEvent event) {
        setBbieScAgencyIdListName(event.getObject().toString());
    }

    public List<String> completeInputForBbieScAgencyIdList(String query) {
        BasicBusinessInformationEntitySupplementaryComponentTreeNode node = getSelectedBasicBusinessInformationEntitySupplementaryComponentTreeNode();
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
     * handle command buttons.
     */
    @Transactional(readOnly = false, rollbackFor = Throwable.class)
    public void update() {
        AssociationBusinessInformationEntityPropertyTreeNode topLevelNode = getTopLevelNode();
        try {
            treeNodeService.validate(topLevelNode);
            treeNodeService.update(topLevelNode, getCurrentUser());
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Updated successfully."));
        } catch (Throwable t) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", t.getMessage()));
            throw t;
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public String updateState(AggregateBusinessInformationEntityState state) {
        update();

        try {
            AssociationBusinessInformationEntityPropertyTreeNode topLevelNode = getTopLevelNode();
            long topLevelAbieId = topLevelNode.getType().getAbie().getOwnerTopLevelAbieId();
            bieService.updateState(topLevelAbieId, state);

            return "/views/profile_bod/list.xhtml?faces-redirect=true";
        } catch (Throwable t) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", t.getMessage()));
            throw t;
        }
    }

    public String createABIEExtension(boolean isLocally) {
        TreeNode treeNode = getSelectedTreeNode();
        AssociationBusinessInformationEntityPropertyTreeNode asbieNode =
                (AssociationBusinessInformationEntityPropertyTreeNode) treeNode.getData();
        AssociationCoreComponentProperty asccp = asbieNode.getAsccp();
        User user = getCurrentUser();

        AggregateCoreComponent eAcc = extensionService.getExtensionAcc(asccp, isLocally);
        AggregateCoreComponent ueAcc = extensionService.getExistsUserExtension(eAcc);
        if (ueAcc != null) {
            CoreComponentState ueAccState = ueAcc.getState();

            if ( user.getAppUserId() == ueAcc.getOwnerUserId() ) {
                return redirectABIEExtension(isLocally, eAcc);
            }

            if ( (ueAccState == CoreComponentState.Candidate || ueAccState == CoreComponentState.Published) ) {
                return redirectABIEExtension(isLocally, eAcc);
            }

            User ueAccOwner = userRepository.findOne(ueAcc.getOwnerUserId());
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "The component is currently edited by another user - " + ueAccOwner.getName()));
            return null;
        }

        try {
            eAcc = extensionService.appendUserExtension(asccp, user, isLocally);
        } catch (PermissionDeniedDataAccessException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
            throw e;
        } catch (Throwable t) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", t.getMessage()));
            throw t;
        }

        return redirectABIEExtension(isLocally, eAcc);
    }

    public CoreComponentState getABIEExtensionState(boolean isLocally) {
        TreeNode treeNode = getSelectedTreeNode();
        if (treeNode == null) {
            return null;
        }
        AssociationBusinessInformationEntityPropertyTreeNode asbieNode =
                (AssociationBusinessInformationEntityPropertyTreeNode) treeNode.getData();
        AssociationCoreComponentProperty asccp = asbieNode.getAsccp();

        AggregateCoreComponent eAcc = extensionService.getExtensionAcc(asccp, isLocally);
        AggregateCoreComponent ueAcc = extensionService.getExistsUserExtension(eAcc);
        return (ueAcc != null) ? ueAcc.getState() : null;
    }

    public String redirectABIEExtension(boolean isLocally) {
        return redirectABIEExtension(isLocally, null);
    }

    public String redirectABIEExtension(boolean isLocally, AggregateCoreComponent eAcc) {
        TreeNode treeNode = getSelectedTreeNode();
        AssociationBusinessInformationEntityPropertyTreeNode asbieNode =
                (AssociationBusinessInformationEntityPropertyTreeNode) treeNode.getData();
        AssociationCoreComponentProperty asccp = asbieNode.getAsccp();

        if (eAcc == null) {
            eAcc = extensionService.getExtensionAcc(asccp, isLocally);
        }

        return "/views/core_component/extension.jsf?accId=" + eAcc.getAccId() + "&faces-redirect=true";
    }
}
