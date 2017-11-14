package org.oagi.srt.web.jsf.beans.bod;

import org.oagi.srt.model.node.ASBIEPNode;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.oagi.srt.service.NodeService;
import org.oagi.srt.service.NodeService.ProgressListener;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FlowEvent;
import org.primefaces.event.SelectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import java.util.*;
import java.util.function.Function;
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
    @Autowired
    private ReleaseRepository releaseRepository;
    private Release release = Release.WORKING_RELEASE;

    private List<TopLevelConcept> allTopLevelConcepts;
    private Map<Long, TopLevelConcept> allTopLevelConceptMap;
    private String selectedPropertyTerm;
    private List<TopLevelConcept> topLevelConcepts;
    private TopLevelConcept selectedTopLevelConcept;

    /*
     * for 'Select Business Context' Step
     */
    @Autowired
    private BusinessContextRepository businessContextRepository;
    private List<BusinessContext> businessContexts;
    private Map<Long, BusinessContext> businessContextMap;
    private BusinessContext selectedBusinessContext;

    /*
     * for 'Create BIE' Step
     */
    @Autowired
    private NodeService nodeService;
    @Autowired
    private AssociationCoreComponentPropertyRepository asccpRepository;
    @Autowired
    private ModuleRepository moduleRepository;

    public Release getRelease() {
        return release;
    }

    public void setRelease(Release release) {
        this.release = release;
    }

    public void onReleaseChange(AjaxBehaviorEvent behaviorEvent) {
        reset();
    }

    public List<Release> getReleases() {
        List<Release> releases = new ArrayList();
        releases.add(Release.WORKING_RELEASE);
        releases.addAll(
                releaseRepository.findAll(new Sort(Sort.Direction.ASC, "releaseId")).stream()
                        .filter(e -> e.getState() == ReleaseState.Final)
                        .collect(Collectors.toList())
        );
        return releases;
    }

    public List<TopLevelConcept> getAllTopLevelConcepts() {
        if (allTopLevelConcepts == null) {
            allTopLevelConcepts = topLevelConceptRepository.findAll(getRelease());
            allTopLevelConceptMap = allTopLevelConcepts.stream()
                    .collect(Collectors.toMap(TopLevelConcept::getAsccpId, Function.identity()));
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
                    .sorted(Comparator.comparing(TopLevelConcept::getPropertyTerm))
                    .collect(Collectors.toList()));
        }
        return topLevelConcepts;
    }

    private void reset() {
        allTopLevelConcepts = null;
        topLevelConcepts = null;
        setSelectedPropertyTerm(null);
        setSelectedTopLevelConcept(null);
        topLevelConceptCheckBoxes = new HashMap();
    }

    public void setTopLevelConcepts(List<TopLevelConcept> topLevelConcepts) {
        this.topLevelConcepts = topLevelConcepts;
    }

    public TopLevelConcept getSelectedTopLevelConcept() {
        return selectedTopLevelConcept;
    }

    public void setSelectedTopLevelConcept(TopLevelConcept selectedTopLevelConcept) {
        if (selectedTopLevelConcept != null) {
            getTopLevelConceptCheckBox(selectedTopLevelConcept.getAsccpId()).setChecked(true);
        }
    }

    public void onTopLevelConceptSelect(SelectEvent event) {
        getTopLevelConceptCheckBox(((TopLevelConcept) event.getObject()).getAsccpId()).setChecked(true);
    }

    public void onTopLevelConceptUnselect(SelectEvent event) {
        getTopLevelConceptCheckBox(((TopLevelConcept) event.getObject()).getAsccpId()).setChecked(false);
    }

    private Map<Long, Boolean> topLevelConceptCheckBoxes = new HashMap();
    public class TopLevelConceptCheckBox {
        private Long asccpId;

        public TopLevelConceptCheckBox(Long asccpId) {
            this.asccpId = asccpId;
        }

        public boolean isChecked() {
            return topLevelConceptCheckBoxes.getOrDefault(asccpId, false);
        }

        public void setChecked(boolean value) {
            topLevelConceptCheckBoxes = new HashMap();

            if (value) {
                TopLevelConcept previousOne = getSelectedTopLevelConcept();
                if (previousOne != null) {
                    topLevelConceptCheckBoxes.put(previousOne.getAsccpId(), false);
                }

                selectedTopLevelConcept = allTopLevelConceptMap.get(asccpId);
            } else {
                selectedTopLevelConcept = null;
            }

            topLevelConceptCheckBoxes.put(asccpId, value);
        }
    }

    public TopLevelConceptCheckBox getTopLevelConceptCheckBox(Long asccpId) {
        return new TopLevelConceptCheckBox(asccpId);
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
        this.businessContextMap = businessContexts.stream()
                .collect(Collectors.toMap(BusinessContext::getBizCtxId, Function.identity()));
    }

    public BusinessContext getSelectedBusinessContext() {
        return selectedBusinessContext;
    }

    public void setSelectedBusinessContext(BusinessContext selectedBusinessContext) {
        if (selectedBusinessContext != null) {
            getBusinessContextCheckBox(selectedBusinessContext.getBizCtxId()).setChecked(true);
        }
    }

    public void onBusinessContextSelect(SelectEvent event) {
        getBusinessContextCheckBox(((BusinessContext) event.getObject()).getBizCtxId()).setChecked(true);
    }

    public void onBusinessContextUnselect(SelectEvent event) {
        getBusinessContextCheckBox(((BusinessContext) event.getObject()).getBizCtxId()).setChecked(false);
    }

    private Map<Long, Boolean> bizCtxCheckBoxes = new HashMap();
    public class BusinessContextCheckBox {
        private Long bizCtxId;

        public BusinessContextCheckBox(Long bizCtxId) {
            this.bizCtxId = bizCtxId;
        }

        public boolean isChecked() {
            return bizCtxCheckBoxes.getOrDefault(bizCtxId, false);
        }

        public void setChecked(boolean value) {
            bizCtxCheckBoxes = new HashMap();

            if (value) {
                BusinessContext previousOne = getSelectedBusinessContext();
                if (previousOne != null) {
                    bizCtxCheckBoxes.put(previousOne.getBizCtxId(), false);
                }

                selectedBusinessContext = businessContextMap.get(bizCtxId);
            } else {
                selectedBusinessContext = null;
            }

            bizCtxCheckBoxes.put(bizCtxId, value);
        }
    }

    public BusinessContextCheckBox getBusinessContextCheckBox(Long bizCtxId) {
        return new BusinessContextCheckBox(bizCtxId);
    }

    public String getModule(long moduleId) {
        return moduleRepository.findModuleByModuleId(moduleId);
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

            ASBIEPNode topLevelNode = getTopLevelNode();
            nodeService.validate(topLevelNode);
            nodeService.submit(topLevelNode, getCurrentUser(), progressListener);

            return "/views/profile_bod/list.jsf?faces-redirect=true";
        } finally {
            RequestContext requestContext = RequestContext.getCurrentInstance();
            requestContext.execute("PF('loadingBlock').hide()");
        }
    }
}
