package org.oagi.srt.web.jsf.beans.bod;

import org.oagi.srt.model.treenode.*;
import org.oagi.srt.repository.AssociationCoreComponentPropertyRepository;
import org.oagi.srt.repository.BusinessContextRepository;
import org.oagi.srt.repository.ModuleRepository;
import org.oagi.srt.repository.TopLevelConceptRepository;
import org.oagi.srt.repository.entity.*;
import org.oagi.srt.service.BusinessInformationEntityService;
import org.oagi.srt.service.TreeNodeService;
import org.oagi.srt.service.TreeNodeService.ProgressListener;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FlowEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultTreeNode;
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
public class CreateProfileBODBean extends AbstractProfileBODBean {

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
    private TreeNodeService treeNodeService;
    @Autowired
    private AssociationCoreComponentPropertyRepository asccpRepository;
    @Autowired
    private ModuleRepository moduleRepository;

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
    public Map<BasicBusinessInformationEntityRestrictionType, BasicBusinessInformationEntityRestrictionType> getAvailablePrimitiveRestrictions(BasicBusinessInformationEntityPropertyTreeNode node) {
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

                        createTreeNode(selectedASCCP, selectedBusinessContext);

                        requestContext.execute("$(document.getElementById(PF('btnBack').id)).show()");
                        requestContext.execute("$(document.getElementById(PF('btnNext').id)).hide()");
                        requestContext.execute("$(document.getElementById(PF('btnSubmit').id)).show()");
                    }

                    break;
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
        try {
            progressListener = new ProgressListener();

            AssociationBusinessInformationEntityPropertyTreeNode topLevelNode = getTopLevelNode();
            treeNodeService.submit(topLevelNode, getCurrentUser(), progressListener);

            return "/views/profile_bod/list.xhtml?faces-redirect=true";
        } finally {
            RequestContext requestContext = RequestContext.getCurrentInstance();
            requestContext.execute("PF('loadingBlock').hide()");
        }
    }
}
