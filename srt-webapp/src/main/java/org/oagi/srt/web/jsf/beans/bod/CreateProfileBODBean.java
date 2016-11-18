package org.oagi.srt.web.jsf.beans.bod;

import org.oagi.srt.model.bie.BBIENode;
import org.oagi.srt.model.bie.BBIERestrictionType;
import org.oagi.srt.model.bie.BBIESCNode;
import org.oagi.srt.model.bie.impl.BaseTopLevelNode;
import org.oagi.srt.repository.AssociationCoreComponentPropertyRepository;
import org.oagi.srt.repository.BusinessContextRepository;
import org.oagi.srt.repository.TopLevelConceptRepository;
import org.oagi.srt.repository.entity.*;
import org.oagi.srt.service.BusinessInformationEntityService;
import org.oagi.srt.web.jsf.component.treenode.BIETreeNodeHandler;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FlowEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
@Transactional(readOnly = true)
public class CreateProfileBODBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TopLevelConceptRepository topLevelConceptRepository;
    private String currentStep;

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
    private BusinessInformationEntityService bieService;
    @Autowired
    private BIETreeNodeHandler bieTreeNodeHandler;
    @Autowired
    private AssociationCoreComponentPropertyRepository asccpRepository;

    private TreeNode treeNode;
    private TreeNode selectedTreeNode;

    public List<TopLevelConcept> getAllTopLevelConcepts() {
        if (allTopLevelConcepts == null) {
            allTopLevelConcepts = topLevelConceptRepository.findAll();
        }
        return allTopLevelConcepts;
    }

    public void setAllTopLevelConcepts(List<TopLevelConcept> allTopLevelConcepts) {
        this.allTopLevelConcepts = allTopLevelConcepts;
    }

    public String getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(String currentStep) {
        this.currentStep = currentStep;
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
        if (topLevelConcepts == null) {
            setTopLevelConcepts(getAllTopLevelConcepts().stream()
                    .sorted((a, b) -> a.getPropertyTerm().compareTo(b.getPropertyTerm()))
                    .collect(Collectors.toList()));
        }
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
        String q = (query != null) ? query.trim() : null;

        if (StringUtils.isEmpty(q)) {
            return getAllTopLevelConcepts().stream()
                    .map(e -> e.getPropertyTerm())
                    .collect(Collectors.toList());
        } else {
            String[] split = q.split(" ");

            return getAllTopLevelConcepts().stream()
                    .map(e -> e.getPropertyTerm())
                    .distinct()
                    .filter(e -> {
                        e = e.toLowerCase();
                        for (String s : split) {
                            if (!e.contains(s.toLowerCase())) {
                                return false;
                            }
                        }
                        return true;
                    })
                    .collect(Collectors.toList());
        }
    }

    public void search() {
        String selectedPropertyTerm = StringUtils.trimWhitespace(getSelectedPropertyTerm());
        if (StringUtils.isEmpty(selectedPropertyTerm)) {
            setTopLevelConcepts(getAllTopLevelConcepts().stream()
                    .sorted((a, b) -> a.getPropertyTerm().compareTo(b.getPropertyTerm()))
                    .collect(Collectors.toList()));
        } else {
            setTopLevelConcepts(
                    getAllTopLevelConcepts().stream()
                            .filter(e -> e.getPropertyTerm().toLowerCase().contains(selectedPropertyTerm.toLowerCase()))
                            .collect(Collectors.toList())
            );
        }
    }

    public List<BusinessContext> getBusinessContexts() {
        if (businessContexts == null) {
            setBusinessContexts(businessContextRepository.findAll());
        }
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

    public BaseTopLevelNode getTopLevelNode() {
        TreeNode treeNode = getTreeNode();
        return (BaseTopLevelNode) treeNode.getChildren().get(0).getData();
    }

    public TreeNode getSelectedTreeNode() {
        return selectedTreeNode;
    }

    public void setSelectedTreeNode(TreeNode selectedTreeNode) {
        this.selectedTreeNode = selectedTreeNode;
    }

    /*
     * handle BBIE Type
     */
    public Map<BBIERestrictionType, BBIERestrictionType> getAvailablePrimitiveRestrictions(BBIENode node) {
        return bieService.getAvailablePrimitiveRestrictions(node);
    }

    private BBIENode getSelectedBBIENode() {
        TreeNode treeNode = getSelectedTreeNode();
        Object data = treeNode.getData();
        if (!(data instanceof BBIENode)) {
            return null;
        }
        return (BBIENode) data;
    }

    public String getBbieXbtName() {
        return bieService.getBdtPrimitiveRestrictionName(getSelectedBBIENode());
    }

    public void setBbieXbtName(String name) {
        bieService.setBdtPrimitiveRestriction(getSelectedBBIENode(), name);
    }

    public void onSelectBbieXbtName(SelectEvent event) {
        setBbieXbtName(event.getObject().toString());
    }

    public List<String> completeInputForBbieXbt(String query) {
        BBIENode node = getSelectedBBIENode();
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
        return bieService.getCodeListName(getSelectedBBIENode());
    }

    public void setBbieCodeListName(String name) {
        BBIENode node = getSelectedBBIENode();
        Map<String, CodeList> codeListMap = bieService.getCodeLists(node);
        CodeList codeList = codeListMap.get(name);
        if (codeList != null) {
            node.setCodeListId(codeList.getCodeListId());
        }
    }

    public void onSelectBbieCodeListName(SelectEvent event) {
        setBbieCodeListName(event.getObject().toString());
    }

    public List<String> completeInputForBbieCodeList(String query) {
        BBIENode node = getSelectedBBIENode();
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
        return bieService.getBbieAgencyIdListName(getSelectedBBIENode());
    }

    public void setBbieAgencyIdListName(String name) {
        BBIENode node = getSelectedBBIENode();
        Map<String, AgencyIdList> agencyIdListMap = bieService.getAgencyIdListIds(node);
        AgencyIdList agencyIdList = agencyIdListMap.get(name);
        if (agencyIdList != null) {
            node.setAgencyIdListId(agencyIdList.getAgencyIdListId());
        }
    }

    public void onSelectBbieAgencyIdListName(SelectEvent event) {
        setBbieAgencyIdListName(event.getObject().toString());
    }

    public List<String> completeInputForBbieAgencyIdList(String query) {
        BBIENode node = getSelectedBBIENode();
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
    public Map<BBIERestrictionType, BBIERestrictionType> getAvailableScPrimitiveRestrictions(BBIESCNode node) {
        return bieService.getAvailablePrimitiveRestrictions(node);
    }

    private BBIESCNode getSelectedBbieScNode() {
        TreeNode treeNode = getSelectedTreeNode();
        Object data = treeNode.getData();
        if (!(data instanceof BBIESCNode)) {
            return null;
        }
        return (BBIESCNode) data;
    }

    public String getBbieScXbtName() {
        return bieService.getBdtScPrimitiveRestrictionName(getSelectedBbieScNode());
    }

    public void setBbieScXbtName(String name) {
        bieService.setBdtScPrimitiveRestriction(getSelectedBbieScNode(), name);
    }

    public void onSelectBbieScXbtName(SelectEvent event) {
        setBbieScXbtName(event.getObject().toString());
    }

    public List<String> completeInputForBbieScXbt(String query) {
        BBIESCNode node = getSelectedBbieScNode();
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
        return bieService.getCodeListName(getSelectedBbieScNode());
    }

    public void setBbieScCodeListName(String name) {
        BBIESCNode node = getSelectedBbieScNode();
        Map<String, CodeList> codeListMap = bieService.getCodeLists(node);
        CodeList codeList = codeListMap.get(name);
        if (codeList != null) {
            node.setCodeListId(codeList.getCodeListId());
        }
    }

    public void onSelectBbieScCodeListName(SelectEvent event) {
        setBbieScCodeListName(event.getObject().toString());
    }

    public List<String> completeInputForBbieScCodeList(String query) {
        BBIESCNode node = getSelectedBbieScNode();
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
        return bieService.getBbieAgencyIdListName(getSelectedBbieScNode());
    }

    public void setBbieScAgencyIdListName(String name) {
        BBIESCNode node = getSelectedBbieScNode();
        Map<String, AgencyIdList> agencyIdListMap = bieService.getAgencyIdListIds(node);
        AgencyIdList agencyIdList = agencyIdListMap.get(name);
        if (agencyIdList != null) {
            node.setAgencyIdListId(agencyIdList.getAgencyIdListId());
        }
    }

    public void onSelectBbieScAgencyIdListName(SelectEvent event) {
        setBbieScAgencyIdListName(event.getObject().toString());
    }

    public List<String> completeInputForBbieScAgencyIdList(String query) {
        BBIESCNode node = getSelectedBbieScNode();
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
     * handle command buttons
     */
    public String onFlowProcess(FlowEvent event) {
        RequestContext requestContext = RequestContext.getCurrentInstance();
        String nextStep;
        try {
            nextStep = event.getNewStep();

            switch (nextStep) {
                case "step_1":
                    requestContext.execute("$(document.getElementById(PF('btnBack').id)).hide()");
                    requestContext.execute("$(document.getElementById(PF('btnNext').id)).show()");
                    requestContext.execute("$(document.getElementById(PF('btnSubmit').id)).hide()");

                    break;

                case "step_2":
                    if (selectedTopLevelConcept == null) {
                        FacesContext.getCurrentInstance().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                                        "'Top-Level Concept' must be selected."));
                        nextStep = event.getOldStep();
                        requestContext.execute("$(document.getElementById(PF('btnBack').id)).hide()");
                        requestContext.execute("$(document.getElementById(PF('btnNext').id)).show()");
                        requestContext.execute("$(document.getElementById(PF('btnSubmit').id)).hide()");
                    } else {
                        requestContext.execute("$(document.getElementById(PF('btnBack').id)).show()");
                        requestContext.execute("$(document.getElementById(PF('btnNext').id)).show()");
                        requestContext.execute("$(document.getElementById(PF('btnSubmit').id)).hide()");
                    }

                    break;

                case "step_3":
                    try {
                        if (selectedBusinessContext == null) {
                            FacesContext.getCurrentInstance().addMessage(null,
                                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                                            "'Business Context' must be selected."));
                            nextStep = event.getOldStep();

                            requestContext.execute("$(document.getElementById(PF('btnBack').id)).show()");
                            requestContext.execute("$(document.getElementById(PF('btnNext').id)).show()");
                            requestContext.execute("$(document.getElementById(PF('btnSubmit').id)).hide()");
                        } else {
                            AssociationCoreComponentProperty selectedASCCP =
                                    asccpRepository.findOne(selectedTopLevelConcept.getAsccpId());
                            setTreeNode(bieTreeNodeHandler.createTreeNode(selectedASCCP, selectedBusinessContext));

                            requestContext.execute("$(document.getElementById(PF('btnBack').id)).show()");
                            requestContext.execute("$(document.getElementById(PF('btnNext').id)).hide()");
                            requestContext.execute("$(document.getElementById(PF('btnSubmit').id)).show()");
                        }

                        break;
                    } finally {
                        /*
                         * Hide loading dialog
                         */
                        requestContext.execute("PF('loadingBlock').hide()");
                    }
            }

            setCurrentStep(nextStep);
            return nextStep;
        } finally {
            enableButtons();
        }
    }

    private void enableButtons() {
        RequestContext requestContext = RequestContext.getCurrentInstance();
        requestContext.execute("$(document.getElementById(PF('btnBack').id)).prop(\"disabled\", false).removeClass('ui-state-disabled');");
        requestContext.execute("$(document.getElementById(PF('btnNext').id)).prop(\"disabled\", false).removeClass('ui-state-disabled');");
        requestContext.execute("$(document.getElementById(PF('btnSubmit').id)).prop(\"disabled\", false).removeClass('ui-state-disabled');");
    }

    private ProgressListener progressListener;

    public int getSubmitProgress() {
        if (progressListener == null) {
            return 0;
        } else {
            return progressListener.getProgress();
        }
    }

    public String getProgressStatus() {
        if (progressListener == null) {
            return "";
        } else {
            return progressListener.getProgressStatus();
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public String submit() {
        progressListener = new ProgressListener();
        bieTreeNodeHandler.submit(getTopLevelNode(), progressListener);

        return "/views/profile_bod/list.xhtml?faces-redirect=true";
    }
}
