package org.oagi.srt.web.jsf.beans.cc;

import org.oagi.srt.model.node.ACCNode;
import org.oagi.srt.model.node.ASCCPNode;
import org.oagi.srt.model.node.BCCPNode;
import org.oagi.srt.model.node.CCNode;
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

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
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

    private LinkedList<TreeNode> treeNodeLinkedList = new LinkedList();
    private int treeNodeLinkedListIndex = -1;
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

        TreeNode treeNode = createTreeNode(targetBccp);
        setTreeNode(treeNode);
    }

    public BasicCoreComponentProperty getTargetBccp() {
        return targetBccp;
    }

    public void setTargetBccp(BasicCoreComponentProperty targetBccp) {
        this.targetBccp = targetBccp;
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
    public void updateBccp(TreeNode treeNode) {
        BCCPNode bccpNode = (BCCPNode) treeNode.getData();
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
    public String discardBccp(TreeNode treeNode) {
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

        return "/views/core_component/list.xhtml?faces-redirect=true";
    }
}

