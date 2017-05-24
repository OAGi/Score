package org.oagi.srt.web.jsf.beans.bod;

import org.oagi.srt.model.node.ASBIEPNode;
import org.oagi.srt.repository.BusinessContextRepository;
import org.oagi.srt.repository.ModuleRepository;
import org.oagi.srt.repository.ProfileBODRepository;
import org.oagi.srt.repository.TopLevelAbieRepository;
import org.oagi.srt.repository.entity.BusinessContext;
import org.oagi.srt.repository.entity.ProfileBOD;
import org.oagi.srt.repository.entity.TopLevelAbie;
import org.oagi.srt.service.NodeService;
import org.oagi.srt.service.NodeService.ProgressListener;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FlowEvent;
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
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
@Transactional(readOnly = true)
public class CopyProfileBODBean extends AbstractProfileBODBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ProfileBODRepository profileBODRepository;
    private String currentStep;

    /*
     * To control Wizard Button
     */
    private boolean btnBackDisable;
    private boolean btnNextDisable;

    /*
     * for 'Select Top-Level ABIE' Step
     */
    private List<ProfileBOD> allProfileBODs;
    private String selectedPropertyTerm;
    private List<ProfileBOD> topLevelConcepts;
    private ProfileBOD selectedProfileBOD;

    /*
     * for 'Select Business Context' Step
     */
    @Autowired
    private BusinessContextRepository businessContextRepository;
    private List<BusinessContext> businessContexts;
    private BusinessContext selectedBusinessContext;

    /*
     * for 'Copy BIE' Step
     */
    @Autowired
    private TopLevelAbieRepository topLevelAbieRepository;
    @Autowired
    private NodeService nodeService;
    @Autowired
    private ModuleRepository moduleRepository;

    public List<ProfileBOD> getAllProfileBODs() {
        if (allProfileBODs == null) {
            allProfileBODs = profileBODRepository.findAll();
        }
        return allProfileBODs;
    }

    public void setAllProfileBODs(List<ProfileBOD> allProfileBODs) {
        this.allProfileBODs = allProfileBODs;
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

    public List<ProfileBOD> getProfileBODs() {
        if (topLevelConcepts == null) {
            setProfileBODs(getAllProfileBODs().stream()
                    .sorted((a, b) -> a.getPropertyTerm().compareTo(b.getPropertyTerm()))
                    .collect(Collectors.toList()));
        }
        return topLevelConcepts;
    }

    public void setProfileBODs(List<ProfileBOD> topLevelConcepts) {
        this.topLevelConcepts = topLevelConcepts;
    }

    public ProfileBOD getSelectedProfileBOD() {
        return selectedProfileBOD;
    }

    public void setSelectedProfileBOD(ProfileBOD selectedProfileBOD) {
        this.selectedProfileBOD = selectedProfileBOD;
    }

    public List<String> completeInput(String query) {
        String q = (query != null) ? query.trim() : null;

        if (StringUtils.isEmpty(q)) {
            return getAllProfileBODs().stream()
                    .map(e -> e.getPropertyTerm())
                    .collect(Collectors.toList());
        } else {
            String[] split = q.split(" ");

            return getAllProfileBODs().stream()
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
            setProfileBODs(getAllProfileBODs().stream()
                    .sorted((a, b) -> a.getPropertyTerm().compareTo(b.getPropertyTerm()))
                    .collect(Collectors.toList()));
        } else {
            setProfileBODs(
                    getAllProfileBODs().stream()
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
                    if (selectedProfileBOD == null) {
                        FacesContext.getCurrentInstance().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                                        "'Profile BOD' must be selected."));
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
                            TopLevelAbie topLevelAbie =
                                    topLevelAbieRepository.findOne(selectedProfileBOD.getTopLevelAbieId());

                            createTreeNode(topLevelAbie);

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
    public String copy() {
        try {
            progressListener = new ProgressListener();

            ASBIEPNode topLevelNode = getTopLevelNode();
            nodeService.validate(topLevelNode);
            nodeService.copy(topLevelNode, getCurrentUser(), selectedBusinessContext, progressListener);

            return "/views/profile_bod/list.jsf?faces-redirect=true";
        } finally {
            RequestContext requestContext = RequestContext.getCurrentInstance();
            requestContext.execute("PF('loadingBlock').hide()");
        }
    }
}
