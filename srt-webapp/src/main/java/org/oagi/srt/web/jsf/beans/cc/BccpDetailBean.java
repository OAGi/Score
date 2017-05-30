package org.oagi.srt.web.jsf.beans.cc;

import org.oagi.srt.model.node.*;
import org.oagi.srt.repository.AggregateCoreComponentRepository;
import org.oagi.srt.repository.AssociationCoreComponentPropertyRepository;
import org.oagi.srt.repository.BasicCoreComponentPropertyRepository;
import org.oagi.srt.repository.DataTypeRepository;
import org.oagi.srt.repository.entity.*;
import org.oagi.srt.service.CoreComponentService;
import org.primefaces.event.NodeSelectEvent;
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

import static org.oagi.srt.repository.entity.CoreComponentState.Editing;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
@Transactional(readOnly = true)
public class BccpDetailBean extends BaseCoreComponentDetailBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private CoreComponentService coreComponentService;

    @Autowired
    private AggregateCoreComponentRepository accRepository;

    @Autowired
    private AssociationCoreComponentPropertyRepository asccpRepository;

    @Autowired
    private BasicCoreComponentPropertyRepository bccpRepository;

    @Autowired
    private DataTypeRepository dataTypeRepository;

    private BasicCoreComponentProperty targetBccp;

    private TreeNode treeNode;
    private TreeNode selectedTreeNode;

    @PostConstruct
    public void init() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, String> requestParameterMap = externalContext.getRequestParameterMap();

        String bccpId = requestParameterMap.get("bccpId");
        BasicCoreComponentProperty targetBccp = bccpRepository.findOne(Long.parseLong(bccpId));

        if (targetBccp == null) {
            throw new IllegalStateException();
        }
        if (Editing == targetBccp.getState() && getCurrentUser().getAppUserId() != targetBccp.getOwnerUserId()) {
            throw new IllegalStateException();
        }

        long bdtId = targetBccp.getBdtId();
        DataType bdt = dataTypeRepository.findOne(bdtId);
        targetBccp.setBdt(bdt);
        targetBccp.afterLoaded();

        setTargetBccp(targetBccp);

        TreeNode treeNode = createTreeNode(targetBccp, true);
        setTreeNode(treeNode);
    }

    public BasicCoreComponentProperty getTargetBccp() {
        return targetBccp;
    }

    public void setTargetBccp(BasicCoreComponentProperty targetBccp) {
        this.targetBccp = targetBccp;
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

    public void onChangePropertyTerm(BCCPNode bccpNode) {
        setNodeName(bccpNode);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void updateState(CoreComponentState state) {
        User requester = getCurrentUser();
        try {
            BasicCoreComponentProperty tBccp = getTargetBccp();
            coreComponentService.updateState(tBccp, state, requester);

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
    public void updateBccp() {
        BCCPNode bccpNode = (BCCPNode) getRootNode().getData();
        BasicCoreComponentProperty bccp = bccpNode.getBccp();
        User requester = getCurrentUser();

        try {
            coreComponentService.update(bccp, requester);
            bccp.afterLoaded();
        } catch (Throwable t) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", t.getMessage()));
            throw t;
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void discardBccp(TreeNode treeNode) throws IOException {
        BCCPNode bccpNode = (BCCPNode) treeNode.getData();
        BasicCoreComponentProperty bccp = bccpNode.getBccp();
        User requester = getCurrentUser();

        try {
            coreComponentService.discard(bccp, requester);
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
    public boolean hasPreviousRevision(BasicCoreComponentProperty bccp) {
        Long bccpId = bccp.getBccpId();
        List<BasicCoreComponentProperty> latestRevisionNumBccps =
                bccpRepository.findAllWithLatestRevisionNumByCurrentBccpId(bccpId);
        return !latestRevisionNumBccps.isEmpty();
    }

    @Transactional
    public void createNewRevision(BasicCoreComponentProperty bccp) throws IOException {
        User requester = getCurrentUser();
        bccp = coreComponentService.newBasicCoreComponentPropertyRevision(requester, bccp);
        setTargetBccp(bccp);

        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        externalContext.redirect("/core_component/bccp/" + bccp.getBccpId());
    }
}

