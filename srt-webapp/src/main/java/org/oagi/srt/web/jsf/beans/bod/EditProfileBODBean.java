package org.oagi.srt.web.jsf.beans.bod;

import org.oagi.srt.model.bie.*;
import org.oagi.srt.repository.TopLevelAbieRepository;
import org.oagi.srt.repository.entity.AssociationCoreComponentProperty;
import org.oagi.srt.repository.entity.TopLevelAbie;
import org.oagi.srt.repository.entity.User;
import org.oagi.srt.service.BusinessInformationEntityService;
import org.oagi.srt.service.ExtensionService;
import org.oagi.srt.web.handler.UIHandler;
import org.oagi.srt.web.jsf.component.treenode.BIETreeNodeHandler;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.model.DefaultTreeNode;
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
import javax.faces.context.FacesContext;
import java.util.Map;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
@Transactional(readOnly = true)
public class EditProfileBODBean extends UIHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private BIETreeNodeHandler bieTreeNodeHandler;
    @Autowired
    private BusinessInformationEntityService bieService;
    @Autowired
    private ExtensionService extensionService;
    @Autowired
    private TopLevelAbieRepository topLevelAbieRepository;

    private TreeNode treeNode;
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
        TreeNode treeNode = bieTreeNodeHandler.createTreeNode(topLevelAbie);
        setTreeNode(treeNode);
    }

    public TreeNode getTreeNode() {
        return treeNode;
    }

    public void setTreeNode(TreeNode treeNode) {
        this.treeNode = treeNode;
    }

    public TopLevelNode getTopLevelNode() {
        TreeNode treeNode = getTreeNode();
        return (TopLevelNode) treeNode.getChildren().get(0).getData();
    }

    public TreeNode getSelectedTreeNode() {
        return selectedTreeNode;
    }

    public void setSelectedTreeNode(TreeNode selectedTreeNode) {
        this.selectedTreeNode = selectedTreeNode;
    }

    public Map<BBIERestrictionType, BBIERestrictionType> getAvailablePrimitiveRestrictions(BBIENode node) {
        return bieService.getAvailablePrimitiveRestrictions(node);
    }

    public Map<String, Long> getBdtPrimitiveRestrictions(BBIENode node) {
        return bieService.getBdtPrimitiveRestrictions(node);
    }

    public Map<String, Long> getCodeLists(BBIENode node) {
        return bieService.getCodeLists(node);
    }

    public Map<BBIERestrictionType, BBIERestrictionType> getAvailableScPrimitiveRestrictions(BBIESCNode node) {
        return bieService.getAvailablePrimitiveRestrictions(node);
    }

    public Map<String, Long> getBdtScPrimitiveRestrictions(BBIESCNode node) {
        return bieService.getBdtScPrimitiveRestrictions(node);
    }

    public Map<String, Long> getCodeLists(BBIESCNode node) {
        return bieService.getCodeLists(node);
    }

    public Map<String, Long> getAgencyIdListIds(BBIESCNode node) {
        return bieService.getAgencyIdListIds(node);
    }

    public void expand(NodeExpandEvent expandEvent) {
        DefaultTreeNode treeNode = (DefaultTreeNode) expandEvent.getTreeNode();
        bieTreeNodeHandler.expandLazyTreeNode(treeNode);
    }

    @Transactional(readOnly = false, rollbackFor = Throwable.class)
    public void update() {
        try {
            bieTreeNodeHandler.update(getTopLevelNode());
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Updated successfully."));
        } catch (Throwable t) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", t.getMessage()));
            throw t;
        }
    }

    @Transactional(readOnly = false, rollbackFor = Throwable.class)
    public String publish() {
        try {
            TopLevelNode topLevelNode = getTopLevelNode();
            long topLevelAbieId = topLevelNode.getAbie().getOwnerTopLevelAbieId();
            bieService.publish(topLevelAbieId);

            return "/views/profile_bod/list.xhtml?faces-redirect=true";
        } catch (Throwable t) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", t.getMessage()));
            throw t;
        }
    }

    public String createABIEExtensionLocally() {
        return createABIEExtension(true);
    }

    public String createABIEExtensionGlobally() {
        return createABIEExtension(false);
    }

    public String createABIEExtension(boolean isLocally) {
        TreeNode treeNode = getSelectedTreeNode();
        ASBIENode asbieNode = (ASBIENode) treeNode.getData();
        AssociationCoreComponentProperty asccp = asbieNode.getAsccp();
        User user = loadAuthentication();

        extensionService.appendUserExtensionIfAbsent(asccp, user, isLocally);

        return "/views/core_component/edit_cc.xhtml?asccpId=" + asccp.getAsccpId() + "&faces-redirect=true";
    }
}
