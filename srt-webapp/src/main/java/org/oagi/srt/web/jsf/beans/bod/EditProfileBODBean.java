package org.oagi.srt.web.jsf.beans.bod;

import org.oagi.srt.model.node.ASBIEPNode;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.oagi.srt.service.BusinessInformationEntityService;
import org.oagi.srt.service.ExtensionService;
import org.oagi.srt.service.NodeService;
import org.primefaces.context.RequestContext;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.util.List;
import java.util.Stack;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
@Transactional(readOnly = true)
public class EditProfileBODBean extends AbstractProfileBODBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private NodeService nodeService;
    @Autowired
    private BusinessInformationEntityService bieService;
    @Autowired
    private ExtensionService extensionService;
    @Autowired
    private TopLevelAbieRepository topLevelAbieRepository;
    @Autowired
    private AggregateCoreComponentRepository accRepository;
    @Autowired
    private AssociationCoreComponentRepository asccRepository;
    @Autowired
    private AssociationCoreComponentPropertyRepository asccpRepository;
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

    public String getModule(long moduleId) {
        return moduleRepository.findModuleByModuleId(moduleId);
    }

    /*
     * handle command buttons.
     */
    @Transactional(readOnly = false, rollbackFor = Throwable.class)
    public void update() {
        ASBIEPNode topLevelNode = getTopLevelNode();
        try {
            nodeService.update(topLevelNode, getCurrentUser());
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Updated successfully."));
        } catch (Throwable t) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", t.getMessage()));
            throw t;
        }
    }

    public void afterUpdate() {
        ASBIEPNode topLevelNode = getTopLevelNode();
        nodeService.afterUpdate(topLevelNode);
        onChangeData(topLevelNode);
    }

    @Transactional(rollbackFor = Throwable.class)
    public String updateState(AggregateBusinessInformationEntityState state) throws IOException {
        try {
            ASBIEPNode topLevelNode = getTopLevelNode();
            long topLevelAbieId = topLevelNode.getType().getAbie().getOwnerTopLevelAbieId();
            bieService.updateState(topLevelAbieId, state);

            // Issue #439: To update the screen status.
            setTopLevelAbie(topLevelAbieRepository.findOne(topLevelAbieId));
        } catch (Throwable t) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", t.getMessage()));
            throw t;
        }

        return "/views/profile_bod/list.jsf?faces-redirect=true";
    }

    @Transactional(rollbackFor = Throwable.class)
    public void createABIEExtension(boolean isLocally) throws IOException {
        TreeNode treeNode = getSelectedTreeNode();
        ASBIEPNode asbieNode = (ASBIEPNode) treeNode.getData();
        AssociationCoreComponentProperty asccp = asbieNode.getAsccp();
        User user = getCurrentUser();
        long releaseId = topLevelAbie.getReleaseId();

        AggregateCoreComponent eAcc = extensionService.getExtensionAcc(asccp, releaseId, isLocally);
        AggregateCoreComponent ueAcc = extensionService.getExistsUserExtension(eAcc, releaseId);
        if (ueAcc != null) {
            CoreComponentState ueAccState = ueAcc.getState();

            boolean isSameBetweenRequesterAndOwner = user.getAppUserId() == ueAcc.getOwnerUserId();
            if (ueAccState == CoreComponentState.Editing) {
                if (!isSameBetweenRequesterAndOwner) {
                    User ueAccOwner = userRepository.findOne(ueAcc.getOwnerUserId());
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "The component is currently edited by another user - " + ueAccOwner.getName()));
                    return;
                }
            }

            if (ueAccState == CoreComponentState.Editing || ueAccState == CoreComponentState.Candidate) {
                redirectABIEExtension(ueAcc);
                return;
            }
        }

        try {
            ueAcc = extensionService.appendUserExtension(asccp, releaseId, user, isLocally);
        } catch (PermissionDeniedDataAccessException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
            throw e;
        } catch (Throwable t) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", t.getMessage()));
            throw t;
        }

        redirectABIEExtension(ueAcc);
    }

    public CoreComponentState getABIEExtensionState(boolean isLocally) {
        TreeNode treeNode = getSelectedTreeNode();
        if (treeNode == null) {
            return null;
        }
        ASBIEPNode asbieNode =
                (ASBIEPNode) treeNode.getData();
        AssociationCoreComponentProperty asccp = asbieNode.getAsccp();
        long releaseId = topLevelAbie.getReleaseId();

        AggregateCoreComponent eAcc = extensionService.getExtensionAcc(asccp, releaseId, isLocally);
        AggregateCoreComponent ueAcc = extensionService.getExistsUserExtension(eAcc, releaseId);
        return (ueAcc != null) ? ueAcc.getState() : null;
    }

    public void redirectABIEExtension(AggregateCoreComponent ueAcc) throws IOException {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        externalContext.redirect("/core_component/extension/" + ueAcc.getAccId());
    }
}
