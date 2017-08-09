package org.oagi.srt.web.jsf.beans.cc;

import org.oagi.srt.repository.AggregateCoreComponentRepository;
import org.oagi.srt.repository.entity.AggregateCoreComponent;
import org.oagi.srt.repository.entity.AssociationCoreComponentProperty;
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
import java.util.List;
import java.util.Map;
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

    private List<AggregateCoreComponent> accList;
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
        return accRepository.findAllByRevisionNum(0).stream()
                .filter(e -> e.getOagisComponentType() != O)
                .sorted((a, b) -> b.getLastUpdateTimestamp().compareTo(a.getLastUpdateTimestamp()))
                .collect(Collectors.toList());
    }

    public List<AggregateCoreComponent> getAccList() {
        return accList;
    }

    public void setAccList(List<AggregateCoreComponent> accList) {
        this.accList = accList;
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
    }

    public void onACCSelect(SelectEvent event) {
        setSelectedACC((AggregateCoreComponent) event.getObject());
    }

    public void onACCUnselect(UnselectEvent event) {
        setSelectedACC(null);
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
