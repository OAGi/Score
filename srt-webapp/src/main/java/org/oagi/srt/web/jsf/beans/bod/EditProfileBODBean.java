package org.oagi.srt.web.jsf.beans.bod;

import org.oagi.srt.model.bie.BBIENode;
import org.oagi.srt.model.bie.TopLevelNode;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.oagi.srt.service.BusinessInformationEntityService;
import org.oagi.srt.web.handler.UIHandler;
import org.oagi.srt.web.jsf.component.treenode.BIETreeNodeHandler;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
@Transactional(readOnly = true)
public class EditProfileBODBean extends UIHandler {

    @Autowired
    private BIETreeNodeHandler bieTreeNodeHandler;
    @Autowired
    private BusinessInformationEntityService bieService;
    @Autowired
    private TopLevelAbieRepository topLevelAbieRepository;
    @Autowired
    private AssociationCoreComponentPropertyRepository asccpRepository;
    @Autowired
    private XSDBuiltInTypeRepository xbtRepository;
    @Autowired
    private CodeListRepository codeListRepository;
    @Autowired
    private BusinessDataTypePrimitiveRestrictionRepository bdtPriRestriRepository;
    @Autowired
    private CoreDataTypeAllowedPrimitiveExpressionTypeMapRepository cdtAwdPriXpsTypeMapRepository;

    private TreeNode treeNode;
    private TreeNode selectedTreeNode;

    private String selectedCodeListName;

    @PostConstruct
    public void init() {
        Long topLevelAbieId = Long.parseLong(
                FacesContext.getCurrentInstance().getExternalContext()
                        .getRequestParameterMap().get("topLevelAbieId"));
        TopLevelAbie topLevelAbie = topLevelAbieRepository.findOne(topLevelAbieId);
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

    public String getPrimitiveType(BBIENode node) {
        List<BusinessDataTypePrimitiveRestriction> ccs = node.getBdtPriRestriList();
        String primitiveType = null;
        for (BusinessDataTypePrimitiveRestriction cc : ccs) {
            if (cc.getCdtAwdPriXpsTypeMapId() > 0L) {
                primitiveType = "XSD Builtin Type";
            } else {
                primitiveType = "Code List";
            }
        }
        return primitiveType;
    }

    public Map<String, Long> getBdtPrimitiveRestrictions(BBIENode node) {
        List<BusinessDataTypePrimitiveRestriction> ccs = node.getBdtPriRestriList();
        Map<String, Long> bdtPrimitiveRestrictions = new HashMap();
        for (BusinessDataTypePrimitiveRestriction cc : ccs) {
            if (cc.getCdtAwdPriXpsTypeMapId() > 0L) {
                CoreDataTypeAllowedPrimitiveExpressionTypeMap vo =
                        cdtAwdPriXpsTypeMapRepository.findOne(cc.getCdtAwdPriXpsTypeMapId());
                XSDBuiltInType xbt = xbtRepository.findOne(vo.getXbtId());
                bdtPrimitiveRestrictions.put(xbt.getName(), cc.getBdtPriRestriId());
            } else {
                CodeList code = codeListRepository.findOne(cc.getCodeListId());
                bdtPrimitiveRestrictions.put(code.getName(), cc.getBdtPriRestriId());
            }
        }

        return bdtPrimitiveRestrictions;
    }

    public Map<String, Long> getCodeLists(BBIENode node) {
        long bdtPrimitiveRestrictionId = node.getBdtPrimitiveRestrictionId();
        List<BusinessDataTypePrimitiveRestriction> bdtPriRestriList =
                bdtPriRestriRepository.findByCdtAwdPriXpsTypeMapId(bdtPrimitiveRestrictionId);
        BusinessDataTypePrimitiveRestriction aBDTPrimitiveRestrictionVO = (bdtPriRestriList.isEmpty()) ? null : bdtPriRestriList.get(0);
        CodeList codeList = (aBDTPrimitiveRestrictionVO != null) ? codeListRepository.findOne(aBDTPrimitiveRestrictionVO.getCodeListId()) : null;
        List<CodeList> codeLists = (codeList != null) ? Arrays.asList(codeList) : Collections.emptyList();
        return codeLists.stream()
                .collect(Collectors.toMap(e -> e.getName(), e -> e.getCodeListId()));
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
}
