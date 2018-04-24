package org.oagi.srt.web.jsf.beans.bie;

import org.oagi.srt.model.node.ASBIEPNode;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.BusinessContext;
import org.oagi.srt.repository.entity.ProfileBIE;
import org.oagi.srt.repository.entity.Release;
import org.oagi.srt.repository.entity.TopLevelAbie;
import org.oagi.srt.service.NodeService;
import org.oagi.srt.service.NodeService.ProgressListener;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FlowEvent;
import org.primefaces.event.SelectEvent;
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
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
@Transactional(readOnly = true)
public class CopyBIEBean extends AbstractBIEBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ProfileBIERepository profileBIERepository;
    private String currentStep;

    /*
     * To control Wizard Button
     */
    private boolean btnBackDisable;
    private boolean btnNextDisable;

    /*
     * for 'Select Top-Level ABIE' Step
     */
    private List<ProfileBIE> allProfileBIES;
    private Map<Long, ProfileBIE> allProfileBIEMap;
    private String selectedPropertyTerm;
    private List<ProfileBIE> topLevelConcepts;
    private ProfileBIE selectedProfileBIE;

    /*
     * for 'Select Business Context' Step
     */
    @Autowired
    private BusinessContextRepository businessContextRepository;
    private List<BusinessContext> businessContexts;
    private Map<Long, BusinessContext> businessContextMap;
    private BusinessContext selectedBusinessContext;

    /*
     * for 'Copy BIE' Step
     */
    @Autowired
    private TopLevelAbieRepository topLevelAbieRepository;
    @Autowired
    private ReleaseRepository releaseRepository;
    @Autowired
    private NodeService nodeService;
    @Autowired
    private ModuleRepository moduleRepository;

    public List<ProfileBIE> getAllProfileBIEs() {
        if (allProfileBIES == null) {
            allProfileBIES = profileBIERepository.findAll();
            allProfileBIEMap = allProfileBIES.stream().collect(Collectors.toMap(ProfileBIE::getTopLevelAbieId, Function.identity()));
        }
        return allProfileBIES;
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

    public void onProfileBIESelect(SelectEvent event) {
        getProfileBIECheckBox(((ProfileBIE) event.getObject()).getTopLevelAbieId()).setChecked(true);
    }

    public void onProfileBIEUnselect(SelectEvent event) {
        getProfileBIECheckBox(((ProfileBIE) event.getObject()).getTopLevelAbieId()).setChecked(false);
    }

    private Map<Long, Boolean> profileBIECheckBoxes = new HashMap();

    public class ProfileBIECheckBox {
        private Long topLevelAbieId;

        public ProfileBIECheckBox(Long bizCtxId) {
            this.topLevelAbieId = bizCtxId;
        }

        public boolean isChecked() {
            return profileBIECheckBoxes.getOrDefault(topLevelAbieId, false);
        }

        public void setChecked(boolean value) {
            profileBIECheckBoxes = new HashMap();

            RequestContext requestContext = RequestContext.getCurrentInstance();
            if (value) {
                BusinessContext previousOne = getSelectedBusinessContext();
                if (previousOne != null) {
                    profileBIECheckBoxes.put(previousOne.getBizCtxId(), false);
                }

                selectedProfileBIE = allProfileBIEMap.get(topLevelAbieId);

            } else {
                selectedProfileBIE = null;
            }

            profileBIECheckBoxes.put(topLevelAbieId, value);
        }
    }

    public ProfileBIECheckBox getProfileBIECheckBox(Long topLevelAbieId) {
        return new ProfileBIECheckBox(topLevelAbieId);
    }

    public List<ProfileBIE> getProfileBIEs() {
        if (topLevelConcepts == null) {
            setProfileBIEs(getAllProfileBIEs().stream()
                    .sorted((a, b) -> a.getPropertyTerm().compareTo(b.getPropertyTerm()))
                    .collect(Collectors.toList()));
        }
        return topLevelConcepts;
    }

    public void setProfileBIEs(List<ProfileBIE> topLevelConcepts) {
        this.topLevelConcepts = topLevelConcepts;
    }

    public ProfileBIE getSelectedProfileBIE() {
        return selectedProfileBIE;
    }

    public void setSelectedProfileBIE(ProfileBIE selectedProfileBIE) {
        this.selectedProfileBIE = selectedProfileBIE;
    }

    public List<String> completeInput(String query) {
        String q = (query != null) ? query.trim() : null;

        if (StringUtils.isEmpty(q)) {
            return getAllProfileBIEs().stream()
                    .map(e -> e.getPropertyTerm())
                    .collect(Collectors.toList());
        } else {
            String[] split = q.split(" ");

            return getAllProfileBIEs().stream()
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
            setProfileBIEs(getAllProfileBIEs().stream()
                    .sorted((a, b) -> a.getPropertyTerm().compareTo(b.getPropertyTerm()))
                    .collect(Collectors.toList()));
        } else {
            setProfileBIEs(
                    getAllProfileBIEs().stream()
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

            RequestContext requestContext = RequestContext.getCurrentInstance();
            if (value) {
                BusinessContext previousOne = getSelectedBusinessContext();
                if (previousOne != null) {
                    bizCtxCheckBoxes.put(previousOne.getBizCtxId(), false);
                }

                selectedBusinessContext = businessContextMap.get(bizCtxId);
                requestContext.execute("$(document.getElementById(PF('btnSubmit').id)).prop(\"disabled\", false).removeClass('ui-state-disabled');");

            } else {
                selectedBusinessContext = null;
                requestContext.execute("$(document.getElementById(PF('btnSubmit').id)).prop(\"disabled\", true).addClass('ui-state-disabled');");
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
                    selectedBusinessContext = null;
                    bizCtxCheckBoxes = new HashMap();

                    requestContext.execute("$(document.getElementById(PF('btnBack').id)).hide()");
                    requestContext.execute("$(document.getElementById(PF('btnNext').id)).show()");
                    requestContext.execute("$(document.getElementById(PF('btnSubmit').id)).hide()");

                    break;

                case "step_2":
                    if (selectedProfileBIE == null) {
                        FacesContext.getCurrentInstance().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                                        "'Profile BIE' must be selected."));
                        requestContext.update("growl");
                        nextStep = event.getOldStep();

                        requestContext.execute("$(document.getElementById(PF('btnBack').id)).hide()");
                        requestContext.execute("$(document.getElementById(PF('btnNext').id)).show()");
                        requestContext.execute("$(document.getElementById(PF('btnSubmit').id)).hide()");
                    } else {
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

        if ("step_2".equals(getCurrentStep())) {
            requestContext.execute("$(document.getElementById(PF('btnSubmit').id)).prop(\"disabled\", true).addClass('ui-state-disabled');");
        } else {
            requestContext.execute("$(document.getElementById(PF('btnSubmit').id)).prop(\"disabled\", false).removeClass('ui-state-disabled');");
        }
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
    public void copy() throws IOException {
        RequestContext requestContext = RequestContext.getCurrentInstance();
        try {
            TopLevelAbie sourceTopLevelAbie = topLevelAbieRepository.findOne(selectedProfileBIE.getTopLevelAbieId());
            createTreeNode(sourceTopLevelAbie);

            progressListener = new ProgressListener();

            ASBIEPNode topLevelNode = getTopLevelNode();
            nodeService.validate(topLevelNode);

            long releaseId = selectedProfileBIE.getReleaseId();
            Release release = (releaseId > 0L) ? releaseRepository.findOne(releaseId) : Release.WORKING_RELEASE;
            TopLevelAbie topLevelAbie = nodeService.copy(topLevelNode, release, getCurrentUser(), selectedBusinessContext, progressListener);
            long topLevelAbieId = topLevelAbie.getTopLevelAbieId();

            FacesContext.getCurrentInstance().getExternalContext().redirect(
                    "/profile_bie/" + topLevelAbieId);
        } finally {
            requestContext.execute("PF('loadingBlock').hide()");
        }
    }
}
