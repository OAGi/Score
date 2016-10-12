package org.oagi.srt.web.jsf.beans.bod;

import org.oagi.srt.repository.BusinessContextRepository;
import org.oagi.srt.repository.TopLevelConceptRepository;
import org.oagi.srt.repository.entity.BusinessContext;
import org.oagi.srt.repository.entity.TopLevelConcept;
import org.primefaces.event.FlowEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
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
public class CreateProfileBODBean {

    @Autowired
    private TopLevelConceptRepository topLevelConceptRepository;

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

    @PostConstruct
    public void init() {
        allTopLevelConcepts = topLevelConceptRepository.findAll();
        setTopLevelConcepts(allTopLevelConcepts.stream()
                .sorted((a, b) -> a.getPropertyTerm().compareTo(b.getPropertyTerm()))
                .collect(Collectors.toList()));
        setBusinessContexts(
                businessContextRepository.findAll()
        );
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

    public String onFlowProcess(FlowEvent event) {
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
                break;
        }

        return newStep;
    }
}
