package org.oagi.srt.web.jsf.beans.cc;

import org.oagi.srt.repository.AggregateCoreComponentRepository;
import org.oagi.srt.repository.entity.AggregateCoreComponent;
import org.oagi.srt.repository.entity.AssociationCoreComponentProperty;
import org.oagi.srt.repository.entity.OagisComponentType;
import org.oagi.srt.repository.entity.User;
import org.oagi.srt.service.CoreComponentService;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
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
public class SelectACCBean extends AbstractCoreComponentBean {

    @Autowired
    private AggregateCoreComponentRepository accRepository;

    @Autowired
    private CoreComponentService coreComponentService;

    @Autowired
    private CoreComponentBeanHelper coreComponentBeanHelper;

    private List<AggregateCoreComponent> accList;
    private Map<Long, AggregateCoreComponent> allAccMap;
    private String objectClassTerm;
    private AggregateCoreComponent selectedACC;

    @PostConstruct
    public void init() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, String> requestParameterMap = externalContext.getRequestParameterMap();
        types = requestParameterMap.get("types");
        states = requestParameterMap.get("states");

        setAccList(allACCs());
    }

    private List<AggregateCoreComponent> allACCs() {
        List<AggregateCoreComponent> allACCs =
                accRepository.findAllByRevisionNum(0).stream()
                .filter(e -> e.getOagisComponentType() != OagisComponentType.UserExtensionGroup)
                .sorted((a, b) -> b.getLastUpdateTimestamp().compareTo(a.getLastUpdateTimestamp()))
                .collect(Collectors.toList());

        /*
         * Issue #477
         *
         * The OAGi developers should be able to select only OAGi developers' components
         * when making a reference to a component
         */
        allACCs = coreComponentBeanHelper.filterByUser(allACCs, getCurrentUser(), AggregateCoreComponent.class);

        return allACCs;
    }

    public List<AggregateCoreComponent> getAccList() {
        return accList;
    }

    public void setAccList(List<AggregateCoreComponent> accList) {
        this.accList = accList;
        this.allAccMap = accList.stream()
                .collect(Collectors.toMap(AggregateCoreComponent::getAccId, Function.identity()));
    }

    public String getObjectClassTerm() {
        return objectClassTerm;
    }

    public void setObjectClassTerm(String objectClassTerm) {
        this.objectClassTerm = objectClassTerm;
    }

    public AggregateCoreComponent getSelectedACC() {
        return selectedACC;
    }

    public void setSelectedACC(AggregateCoreComponent selectedACC) {
        this.selectedACC = selectedACC;
        getAccCheckBox(selectedACC.getAccId()).setChecked(true);
    }

    public void onACCSelect(SelectEvent event) {
        setSelectedACC((AggregateCoreComponent) event.getObject());
        getAccCheckBox(((AggregateCoreComponent) event.getObject()).getAccId()).setChecked(true);
    }

    public void onACCUnselect(UnselectEvent event) {
        setSelectedACC(null);
        getAccCheckBox(((AggregateCoreComponent) event.getObject()).getAccId()).setChecked(false);
    }

    private Map<Long, Boolean> accCheckBoxes = new HashMap();

    public class AccCheckBox {
        private Long accId;

        public AccCheckBox(Long accId) {
            this.accId = accId;
        }

        public boolean isChecked() {
            return accCheckBoxes.getOrDefault(accId, false);
        }

        public void setChecked(boolean value) {
            accCheckBoxes = new HashMap();

            if (value) {
                AggregateCoreComponent previousOne = getSelectedACC();
                if (previousOne != null) {
                    accCheckBoxes.put(previousOne.getAccId(), false);
                }

                selectedACC = allAccMap.get(accId);
            } else {
                selectedACC = null;
            }

            accCheckBoxes.put(accId, value);
        }
    }

    public AccCheckBox getAccCheckBox(Long accId) {
        return new AccCheckBox(accId);
    }

    public List<String> completeInput(String query) {
        String q = (query != null) ? query.trim() : null;

        if (StringUtils.isEmpty(q)) {
            return allACCs().stream()
                    .map(e -> e.getObjectClassTerm())
                    .collect(Collectors.toList());
        } else {
            String[] split = q.split(" ");

            return allACCs().stream()
                    .map(e -> e.getObjectClassTerm())
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
        String objectClassTerm = getObjectClassTerm();
        if (StringUtils.isEmpty(objectClassTerm)) {
            setAccList(allACCs());
        } else {
            setAccList(
                    allACCs().stream()
                            .filter(e -> e.getObjectClassTerm().toLowerCase().contains(objectClassTerm.toLowerCase()))
                            .collect(Collectors.toList())
            );
        }
    }

    // To support 'back' button to go back 'list' page.
    private String types;
    private String states;

    public void back() throws IOException {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        externalContext.redirect("/core_component");
    }

    @Transactional
    public void createASCCP() throws IOException {
        User requester = getCurrentUser();
        AggregateCoreComponent roleOfAcc = getSelectedACC();
        AssociationCoreComponentProperty asccp = coreComponentService.newAssociationCoreComponentProperty(requester, roleOfAcc);

        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        externalContext.redirect("/core_component/asccp/" + asccp.getAsccpId());
    }
}
