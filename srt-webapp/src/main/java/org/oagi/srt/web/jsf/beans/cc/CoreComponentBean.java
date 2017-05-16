package org.oagi.srt.web.jsf.beans.cc;

import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.oagi.srt.service.CoreComponentService;
import org.primefaces.component.api.UIColumn;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.data.SortEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
@Transactional(readOnly = true)
public class CoreComponentBean extends AbstractCoreComponentBean {

    @Autowired
    private CoreComponentService coreComponentService;

    private List<CoreComponents> coreComponents;
    private List<String> selectedTypes;
    private List<CoreComponentState> selectedStates;

    private String sortColumnHeaderText = "";
    private boolean isSortColumnAscending = false;

    private String searchTextForDen;
    private String searchTextForDefinition;
    private String searchTextForModule;

    @PostConstruct
    public void init() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, String> requestParameterMap = externalContext.getRequestParameterMap();

        String types = requestParameterMap.get("types");
        if ("null".equals(types)) {
            types = null;
        }

        if (!StringUtils.isEmpty(types)) {
            StringTokenizer tokenizer = new StringTokenizer(types, ",");
            selectedTypes = new ArrayList();
            while (tokenizer.hasMoreTokens()) {
                String type = tokenizer.nextToken();
                selectedTypes.add(type);
            }
        } else {
            selectedTypes = Arrays.asList(
                    "ACC", "ASCC", "ASCCP", "BCC", "BCCP"
            );
        }

        String states = requestParameterMap.get("states");
        if ("null".equals(states)) {
            states = null;
        }

        if (!StringUtils.isEmpty(states)) {
            StringTokenizer tokenizer = new StringTokenizer(states, ",");
            selectedStates = new ArrayList();
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                CoreComponentState state = CoreComponentState.valueOf(token);
                selectedStates.add(state);
            }
        } else {
            selectedStates = Arrays.asList(
                    CoreComponentState.Editing,
                    CoreComponentState.Candidate,
                    CoreComponentState.Published);
        }
    }

    public String[] getSelectedTypes() {
        if (selectedTypes == null || selectedTypes.isEmpty()) {
            return new String[0];
        }

        return selectedTypes.toArray(new String[selectedTypes.size()]);
    }

    public void setSelectedTypes(String[] selectedTypes) {
        if (selectedTypes != null && selectedTypes.length > 0) {
            this.selectedTypes = new ArrayList();
            for (String selectedType : selectedTypes) {
                this.selectedTypes.add(selectedType);
            }
        }
    }

    public String toStringSelectedTypes() {
        String[] selectedTypes = getSelectedTypes();
        return (selectedTypes != null) ? String.join(",", selectedTypes) : "";
    }

    public void onTypeChange() {
        reset();
    }

    public String[] getSelectedStates() {
        if (selectedStates == null || selectedStates.isEmpty()) {
            return new String[0];
        }

        String[] selectedStateStrings = new String[selectedStates.size()];
        int index = 0;
        for (CoreComponentState coreComponentState : selectedStates) {
            selectedStateStrings[index++] = coreComponentState.toString();
        }
        return selectedStateStrings;
    }

    public void setSelectedStates(String[] selectedStates) {
        if (selectedStates != null && selectedStates.length > 0) {
            this.selectedStates = new ArrayList();
            for (String selectedState : selectedStates) {
                this.selectedStates.add(CoreComponentState.valueOf(selectedState));
            }
        }
    }

    public String toStringSelectedStates() {
        String[] selectedStates = getSelectedStates();
        return (selectedStates != null) ? String.join(",", selectedStates) : "";
    }

    public void onStateChange() {
        reset();
    }

    public List<CoreComponents> getCoreComponents() {
        if (coreComponents == null) {
            String sortProperty;
            switch (sortColumnHeaderText) {
                case "Type":
                    sortProperty = "type";
                    break;
                case "DEN":
                    sortProperty = "den";
                    break;
                case "Owner":
                    sortProperty = "owner";
                    break;
                case "State":
                    sortProperty = "state";
                    break;
                case "Last Updated By":
                    sortProperty = "last_updated_user";
                    break;
                case "Last Updated Timestamp":
                default:
                    sortProperty = "last_update_timestamp";
                    break;
            }

            coreComponents = coreComponentService.getCoreComponents(selectedTypes, selectedStates,
                    new Sort.Order((isSortColumnAscending) ? Sort.Direction.ASC : Sort.Direction.DESC, sortProperty));

            coreComponents = coreComponents.stream()
                    .filter(e -> {
                        String q = getSearchTextForDen();
                        if (StringUtils.isEmpty(q)) {
                            return true;
                        }

                        String den = e.getDen().toLowerCase();
                        String[] split = q.split(" ");
                        for (String s : split) {
                            if (!den.contains(s.toLowerCase())) {
                                return false;
                            }
                        }
                        return true;
                    })
                    .filter(e -> {
                        String q = getSearchTextForDefinition();
                        if (StringUtils.isEmpty(q)) {
                            return true;
                        }

                        String definition = e.getDefinition();
                        if (StringUtils.isEmpty(definition)) {
                            return false;
                        }
                        definition = definition.toLowerCase();
                        String[] split = q.split(" ");
                        for (String s : split) {
                            if (!definition.contains(s.toLowerCase())) {
                                return false;
                            }
                        }
                        return true;
                    })
                    .filter(e -> {
                        String q = getSearchTextForModule();
                        if (StringUtils.isEmpty(q)) {
                            return true;
                        }

                        String module = e.getModule();
                        if (StringUtils.isEmpty(module)) {
                            return false;
                        }
                        module = module.toLowerCase();
                        String[] split = q.split(" ");
                        for (String s : split) {
                            if (!module.contains(s.toLowerCase())) {
                                return false;
                            }
                        }
                        return true;
                    })
                    .collect(Collectors.toList());
        }

        return coreComponents;
    }

    public String getSearchTextForDen() {
        return searchTextForDen;
    }

    public void setSearchTextForDen(String searchTextForDen) {
        this.searchTextForDen = searchTextForDen;
    }

    public String getSearchTextForDefinition() {
        return searchTextForDefinition;
    }

    public void setSearchTextForDefinition(String searchTextForDefinition) {
        this.searchTextForDefinition = searchTextForDefinition;
    }

    public String getSearchTextForModule() {
        return searchTextForModule;
    }

    public void setSearchTextForModule(String searchTextForModule) {
        this.searchTextForModule = searchTextForModule;
    }

    public void onSortEvent(SortEvent sortEvent) {
        sortColumnHeaderText = sortEvent.getSortColumn().getHeaderText();
        isSortColumnAscending = sortEvent.isAscending();
    }

    public void search() {
        reset();
    }

    private void reset() {
        coreComponents = null;
    }

    @Transactional
    public String createACC() {
        User requester = getCurrentUser();
        AggregateCoreComponent acc = coreComponentService.newAggregateCoreComponent(requester);
        String types = toStringSelectedTypes();
        String states = toStringSelectedStates();
        return "/views/core_component/acc_details.xhtml?accId=" + acc.getAccId() + "&types=" + types + "&states=" + states + "&faces-redirect=true";
    }

    public String createASCCP() {
        String types = toStringSelectedTypes();
        String states = toStringSelectedStates();
        return "/views/core_component/select_acc.jsf?types=" + types + "&states=" + states + "&faces-redirect=true";
    }

    public String createBCCP() {
        String types = toStringSelectedTypes();
        String states = toStringSelectedStates();
        return "/views/core_component/select_bdt.jsf?types=" + types + "&states=" + states + "&faces-redirect=true";
    }
}
