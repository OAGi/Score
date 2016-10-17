package org.oagi.srt.web.jsf.beans.bod;

import org.oagi.srt.model.Node;
import org.oagi.srt.model.bod.BBIENode;
import org.oagi.srt.model.bod.TopLevelNode;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.AggregateBusinessInformationEntity;
import org.oagi.srt.repository.entity.BusinessDataTypePrimitiveRestriction;
import org.oagi.srt.repository.entity.CodeList;
import org.oagi.srt.repository.entity.TopLevelAbie;
import org.oagi.srt.web.jsf.component.treenode.BIETreeNodeHandler;
import org.primefaces.model.TreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.util.List;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
@Transactional(readOnly = true)
public class EditProfileBODBean {

    @Autowired
    private BIETreeNodeHandler bieTreeNodeHandler;
    @Autowired
    private TopLevelAbieRepository topLevelAbieRepository;
    @Autowired
    private AssociationCoreComponentPropertyRepository asccpRepository;
    @Autowired
    private XSDBuiltInTypeRepository xbtRepository;
    @Autowired
    private CodeListRepository codeListRepository;
    @Autowired
    private CoreDataTypeAllowedPrimitiveExpressionTypeMapRepository cdtAwdPriXpsTypeMapRepository;

    private TreeNode treeNode;
    private TreeNode selectedTreeNode;

    @PostConstruct
    public void init() {
        Long topLevelAbieId = Long.parseLong(
                FacesContext.getCurrentInstance().getExternalContext()
                        .getRequestParameterMap().get("topLevelAbieId"));
        TopLevelAbie topLevelAbie = topLevelAbieRepository.findOne(topLevelAbieId);
        setTreeNode(
                bieTreeNodeHandler.createTreeNode(topLevelAbie)
        );
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

    public void setRestrictionType(String restrictionType) {
        switch (restrictionType) {
            case "Primitive":
                break;
            case "Code":
                break;
        }
    }

    public String getRestrictionType() {
        TreeNode selectedTreeNode = getSelectedTreeNode();
        if (selectedTreeNode == null) {
            return null;
        }

        Node node = (Node) selectedTreeNode.getData();
        if (node instanceof BBIENode) {
            BBIENode bbieNode = (BBIENode) node;
            if (bbieNode.getBbie().getBdtPriRestri().getBdtPriRestriId() > 0L) {
                return "Primitive";
            } else {
                return "Code";
            }
        } else {
            return null;
        }
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

    public String getCodeListName(Node node) {
        CodeList codeList = (CodeList) node.getAttribute("codeList");
        return (codeList != null) ? codeList.getName() : null;
    }

}
