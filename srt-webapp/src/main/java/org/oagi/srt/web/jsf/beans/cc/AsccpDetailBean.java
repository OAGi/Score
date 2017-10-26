package org.oagi.srt.web.jsf.beans.cc;

import org.oagi.srt.model.node.*;
import org.oagi.srt.repository.AggregateCoreComponentRepository;
import org.oagi.srt.repository.AssociationCoreComponentPropertyRepository;
import org.oagi.srt.repository.entity.*;
import org.oagi.srt.service.CoreComponentService;
import org.oagi.srt.service.UndoService;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.util.*;

import static org.oagi.srt.repository.entity.CoreComponentState.Editing;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
@Transactional(readOnly = true)
public class AsccpDetailBean extends BaseCoreComponentDetailBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private UndoService undoService;

    @Autowired
    private CoreComponentService coreComponentService;

    @Autowired
    private AggregateCoreComponentRepository accRepository;

    @Autowired
    private AssociationCoreComponentPropertyRepository asccpRepository;

    private AssociationCoreComponentProperty targetAsccp;

    private TreeNode treeNode;
    private TreeNode selectedTreeNode;
    private boolean setSelectedTreeNodeAfterRefresh = false;
    private TreeNode selectedNodeAfterRefresh;

    @PostConstruct
    public void init() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, String> requestParameterMap = externalContext.getRequestParameterMap();

        String asccpId = requestParameterMap.get("asccpId");
        AssociationCoreComponentProperty targetAsccp = asccpRepository.findOne(Long.parseLong(asccpId));

        if (targetAsccp == null) {
            throw new IllegalStateException();
        }
        if (Editing == targetAsccp.getState() && getCurrentUser().getAppUserId() != targetAsccp.getOwnerUserId()) {
            throw new IllegalStateException();
        }

        long roleOfAccId = targetAsccp.getRoleOfAccId();
        AggregateCoreComponent roleOfAcc = accRepository.findOne(roleOfAccId);
        targetAsccp.setRoleOfAcc(roleOfAcc);
        targetAsccp.afterLoaded();

        setTargetAsccp(targetAsccp);

        TreeNode treeNode = createTreeNode(targetAsccp, true);
        setTreeNode(treeNode);
    }

    public AssociationCoreComponentProperty getTargetAsccp() {
        return targetAsccp;
    }

    public void setTargetAsccp(AssociationCoreComponentProperty targetAsccp) {
        this.targetAsccp = targetAsccp;
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

    public void onChangePropertyTerm(ASCCPNode asccpNode) {
        setNodeName(asccpNode);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void updateState(CoreComponentState state) {
        User requester = getCurrentUser();
        try {
            AssociationCoreComponentProperty tAsccp = getTargetAsccp();
            coreComponentService.updateState(tAsccp, state, requester);

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

    @Transactional(rollbackFor = Throwable.class)
    public void updateAsccp() {
        ASCCPNode asccpNode = (ASCCPNode) getRootNode().getData();
        AssociationCoreComponentProperty asccp = asccpNode.getAsccp();
        User requester = getCurrentUser();

        try {
            coreComponentService.update(asccp, requester);
            asccp.afterLoaded();
        } catch (Throwable t) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", t.getMessage()));
            throw t;
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void discardAsccp(TreeNode treeNode) throws IOException {
        ASCCPNode asccpNode = (ASCCPNode) treeNode.getData();
        AssociationCoreComponentProperty asccp = asccpNode.getAsccp();
        User requester = getCurrentUser();

        try {
            coreComponentService.discard(asccp, requester);
        } catch (Throwable t) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", t.getMessage()));
            throw t;
        }

        back();
    }

    public void back() throws IOException {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        externalContext.redirect("/core_component");
    }

    /*
     * Create New Revision
     */
    public boolean hasPreviousRevision(AssociationCoreComponentProperty asccp) {
        Long asccpId = asccp.getAsccpId();
        List<AssociationCoreComponentProperty> latestRevisionNumAsccps =
                asccpRepository.findAllWithLatestRevisionNumByCurrentAsccpId(asccpId);
        return !latestRevisionNumAsccps.isEmpty();
    }

    public boolean isPreviousRevisionNotInsert(AssociationCoreComponentProperty asccp) {
        return undoService.isPreviousRevisionNotInsert(asccp);
    }

    @Transactional
    public void createNewRevision(AssociationCoreComponentProperty asccp) throws IOException {
        User requester = getCurrentUser();
        asccp = coreComponentService.newAssociationCoreComponentPropertyRevision(requester, asccp);
        setTargetAsccp(asccp);

        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        externalContext.redirect("/core_component/asccp/" + asccp.getAsccpId());
    }

    public void undoLastAction() {
        undoService.undoLastAction(getTargetAsccp());
        refreshTreeNode();
    }

    private void refreshTreeNode() {
        TreeNode treeNode = createTreeNode(targetAsccp, true);
        copyExpandedState(this.treeNode, treeNode);
        setTreeNode(treeNode);
        setSelectedTreeNodeAfterRefresh = true;
        selectedNodeAfterRefresh = findChildNodeAnywhereById(treeNode,((SRTNode)getSelectedTreeNode().getData()).getId());
    }
}

