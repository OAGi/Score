package org.oagi.srt.web.jsf.beans.bod;

import org.oagi.srt.model.Node;
import org.oagi.srt.model.bod.BBIENode;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.oagi.srt.web.jsf.component.treenode.CreateBIETreeNode;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FlowEvent;
import org.primefaces.model.TreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
public class CreateProfileBODBean {

    @Autowired
    private TopLevelConceptRepository topLevelConceptRepository;

    /*
     * To control Wizard Button
     */
    private boolean btnBackDisable;
    private boolean btnNextDisable;

    /*
     * for 'Select Top-Level Concept' Step
     */
    private List<TopLevelConcept> allTopLevelConcepts;
    private String selectedPropertyTerm;
    private List<TopLevelConcept> topLevelConcepts;
    private TopLevelConcept selectedTopLevelConcept;

    /*
     * for 'Select Business Context' Step
     */
    @Autowired
    private BusinessContextRepository businessContextRepository;
    private List<BusinessContext> businessContexts;
    private BusinessContext selectedBusinessContext;

    /*
     * for 'Create BIE' Step
     */
    @Autowired
    private CreateBIETreeNode createBIETreeNode;
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
        allTopLevelConcepts = topLevelConceptRepository.findAll();
        setTopLevelConcepts(allTopLevelConcepts.stream()
                .sorted((a, b) -> a.getPropertyTerm().compareTo(b.getPropertyTerm()))
                .collect(Collectors.toList()));
        setBusinessContexts(
                businessContextRepository.findAll()
        );

        setSelectedTopLevelConcept(
                topLevelConceptRepository.findOne(222L)
        );
        setSelectedBusinessContext(
                businessContextRepository.findOne(1L)
        );
        AssociationCoreComponentProperty selectedASCCP =
                asccpRepository.findOne(selectedTopLevelConcept.getAsccpId());
        treeNode = createBIETreeNode.createTreeNode(selectedASCCP, selectedBusinessContext);
    }

    public boolean isBtnBackDisable() {
        return btnBackDisable;
    }

    public void setBtnBackDisable(boolean btnBackDisable) {
        this.btnBackDisable = btnBackDisable;
    }

    public boolean isBtnNextDisable() {
        return btnNextDisable;
    }

    public void setBtnNextDisable(boolean btnNextDisable) {
        this.btnNextDisable = btnNextDisable;
    }

    public String getSelectedPropertyTerm() {
        return selectedPropertyTerm;
    }

    public void setSelectedPropertyTerm(String selectedPropertyTerm) {
        this.selectedPropertyTerm = selectedPropertyTerm;
    }

    public List<TopLevelConcept> getTopLevelConcepts() {
        return topLevelConcepts;
    }

    public void setTopLevelConcepts(List<TopLevelConcept> topLevelConcepts) {
        this.topLevelConcepts = topLevelConcepts;
    }

    public TopLevelConcept getSelectedTopLevelConcept() {
        return selectedTopLevelConcept;
    }

    public void setSelectedTopLevelConcept(TopLevelConcept selectedTopLevelConcept) {
        this.selectedTopLevelConcept = selectedTopLevelConcept;
    }

    public List<String> completeInput(String query) {
        return allTopLevelConcepts.stream()
                .map(e -> e.getPropertyTerm())
                .distinct()
                .filter(e -> e.toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    public void search() {
        String selectedPropertyTerm = StringUtils.trimWhitespace(getSelectedPropertyTerm());
        if (StringUtils.isEmpty(selectedPropertyTerm)) {
            setTopLevelConcepts(allTopLevelConcepts.stream()
                    .sorted((a, b) -> a.getPropertyTerm().compareTo(b.getPropertyTerm()))
                    .collect(Collectors.toList()));
        } else {
            setTopLevelConcepts(
                    allTopLevelConcepts.stream()
                            .filter(e -> e.getPropertyTerm().toLowerCase().contains(selectedPropertyTerm.toLowerCase()))
                            .collect(Collectors.toList())
            );
        }
    }

    public List<BusinessContext> getBusinessContexts() {
        return businessContexts;
    }

    public void setBusinessContexts(List<BusinessContext> businessContexts) {
        this.businessContexts = businessContexts;
    }

    public BusinessContext getSelectedBusinessContext() {
        return selectedBusinessContext;
    }

    public void setSelectedBusinessContext(BusinessContext selectedBusinessContext) {
        this.selectedBusinessContext = selectedBusinessContext;
    }

    public TreeNode getTreeNode() {
        return treeNode;
    }

    public void setTreeNode(TreeNode treeNode) {
        this.treeNode = treeNode;
    }

    public TreeNode getSelectedTreeNode() {
        return selectedTreeNode;
    }

    public void setSelectedTreeNode(TreeNode selectedTreeNode) {
        this.selectedTreeNode = selectedTreeNode;
    }

    public String onFlowProcess(FlowEvent event) {
        try {
            String newStep = event.getNewStep();

            switch (newStep) {
                case "step_2":
                    if (selectedTopLevelConcept == null) {
                        FacesContext.getCurrentInstance().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                                        "'Top-Level Concept' must be selected."));
                        return event.getOldStep();
                    }
                    break;
                case "step_3":
                    if (selectedBusinessContext == null) {
                        FacesContext.getCurrentInstance().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                                        "'Business Context' must be selected."));
                        return event.getOldStep();
                    }

                    AssociationCoreComponentProperty selectedASCCP =
                            asccpRepository.findOne(selectedTopLevelConcept.getAsccpId());
                    treeNode = createBIETreeNode.createTreeNode(selectedASCCP, selectedBusinessContext);

                    /*
                     * Hide loading dialog
                     */
                    RequestContext requestContext = RequestContext.getCurrentInstance();
                    requestContext.execute("PF('loadingBlock').hide()");
                    break;
            }

            return newStep;
        } finally {
            /*
             * Enable buttons
             */
            RequestContext requestContext = RequestContext.getCurrentInstance();
            requestContext.execute("$(document.getElementById(PF('btnBack').id)).prop(\"disabled\", false).removeClass('ui-state-disabled');");
            requestContext.execute("$(document.getElementById(PF('btnNext').id)).prop(\"disabled\", false).removeClass('ui-state-disabled');");
        }
    }

    public String getRestrictionType(Node node) {
        return null;
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
