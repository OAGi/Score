package org.oagi.srt.web.jsf.beans.cc;

import org.oagi.srt.repository.AggregateCoreComponentRepository;
import org.oagi.srt.repository.entity.AggregateCoreComponent;
import org.oagi.srt.repository.entity.CoreComponentState;
import org.oagi.srt.repository.entity.CoreComponents;
import org.oagi.srt.repository.entity.User;
import org.oagi.srt.service.CoreComponentService;
import org.primefaces.event.data.SortEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.oagi.srt.repository.entity.OagisComponentType.UserExtensionGroup;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
@Transactional(readOnly = true)
public class CoreComponentBean extends AbstractCoreComponentBean {

    private static final String SELECTED_TYPES_KEY = "_core_component/selected_types";
    private static final String SELECTED_STATES_KEY = "_core_component/selected_states";
    private static final String SEARCH_TEXT_DEN_KEY = "_core_component/search_text_den";
    private static final String SEARCH_TEXT_DEFINITION_KEY = "_core_component/search_text_definition";
    private static final String SEARCH_TEXT_MODULE_KEY = "_core_component/search_text_module";

    @Autowired
    private CoreComponentService coreComponentService;

    @Autowired
    private AggregateCoreComponentRepository accRepository;

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

        Map<String, Object> sessionMap = externalContext.getSessionMap();
        Object selectedTypes = sessionMap.get(SELECTED_TYPES_KEY);

        if (selectedTypes != null) {
            this.selectedTypes = (List<String>) selectedTypes;
        } else {
            this.selectedTypes = Arrays.asList(
                    "ACC", "ASCC", "ASCCP", "BCC", "BCCP"
            );
        }

        Object selectedStates = sessionMap.get(SELECTED_STATES_KEY);
        if (selectedStates != null) {
            this.selectedStates = (List<CoreComponentState>) selectedStates;
        } else {
            this.selectedStates = Arrays.asList(
                    CoreComponentState.Editing,
                    CoreComponentState.Candidate,
                    CoreComponentState.Published);
        }

        searchTextForDen = (String) sessionMap.get(SEARCH_TEXT_DEN_KEY);
        searchTextForDefinition = (String) sessionMap.get(SEARCH_TEXT_DEFINITION_KEY);
        searchTextForModule = (String) sessionMap.get(SEARCH_TEXT_MODULE_KEY);
    }

    public void invalidate() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, Object> sessionMap = externalContext.getSessionMap();

        sessionMap.remove(SELECTED_TYPES_KEY);
        sessionMap.remove(SELECTED_STATES_KEY);
        sessionMap.remove(SEARCH_TEXT_DEN_KEY);
        sessionMap.remove(SEARCH_TEXT_DEFINITION_KEY);
        sessionMap.remove(SEARCH_TEXT_MODULE_KEY);
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

    public void onTypeChange() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, Object> sessionMap = externalContext.getSessionMap();
        sessionMap.put(SELECTED_TYPES_KEY, selectedTypes);

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

    public void onStateChange() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, Object> sessionMap = externalContext.getSessionMap();
        sessionMap.put(SELECTED_STATES_KEY, selectedStates);

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
                    .filter(new DenSearchFilter())
                    .filter(new DefinitionSearchFilter())
                    .filter(new ModuleSearchFilter())
                    .collect(Collectors.toList());
        }

        return coreComponents;
    }

    private interface SearchFilter extends Predicate<CoreComponents> {
    }

    private class DenSearchFilter implements SearchFilter {

        @Override
        public boolean test(CoreComponents e) {
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
        }
    }

    private class DefinitionSearchFilter implements SearchFilter {

        @Override
        public boolean test(CoreComponents e) {
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
        }
    }

    private class ModuleSearchFilter implements SearchFilter {

        @Override
        public boolean test(CoreComponents e) {
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
        }
    }

    public String getSearchTextForDen() {
        return searchTextForDen;
    }

    public void setSearchTextForDen(String searchTextForDen) {
        if (StringUtils.isEmpty(searchTextForDen)) {
            this.searchTextForDen = null;
        } else {
            this.searchTextForDen = StringUtils.trimWhitespace(searchTextForDen);
        }
    }

    public void onSearchTextForDenChange() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, Object> sessionMap = externalContext.getSessionMap();
        sessionMap.put(SEARCH_TEXT_DEN_KEY, this.searchTextForDen);
    }

    public String getSearchTextForDefinition() {
        return searchTextForDefinition;
    }

    public void setSearchTextForDefinition(String searchTextForDefinition) {
        if (StringUtils.isEmpty(searchTextForDefinition)) {
            this.searchTextForDefinition = null;
        } else {
            this.searchTextForDefinition = StringUtils.trimWhitespace(searchTextForDefinition);
        }
    }

    public void onSearchTextForDefinitionChange() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, Object> sessionMap = externalContext.getSessionMap();
        sessionMap.put(SEARCH_TEXT_DEFINITION_KEY, this.searchTextForDefinition);
    }

    public String getSearchTextForModule() {
        return searchTextForModule;
    }

    public void setSearchTextForModule(String searchTextForModule) {
        if (StringUtils.isEmpty(searchTextForModule)) {
            this.searchTextForModule = null;
        } else {
            this.searchTextForModule = StringUtils.trimWhitespace(searchTextForModule);
        }
    }

    public void onSearchTextForModuleChange() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, Object> sessionMap = externalContext.getSessionMap();
        sessionMap.put(SEARCH_TEXT_MODULE_KEY, this.searchTextForModule);
    }

    public boolean canBeDiscarded(CoreComponents coreComponents) {
        if (getCurrentUser().getAppUserId() != coreComponents.getOwnerUserId()) {
            return false;
        }
        if (coreComponents.getState() == CoreComponentState.Published) {
            return false;
        }

        switch (coreComponents.getType()) {
            case "ACC":
                AggregateCoreComponent acc = accRepository.findOne(coreComponents.getId());
                if (acc.getOagisComponentType() == UserExtensionGroup) {
                    return false;
                } else {
                    return true;
                }
            case "ASCC":
                return false;
            case "ASCCP":
                return true;
            case "BCC":
                return false;
            case "BCCP":
                return true;
        }

        return false;
    }

    @Transactional(rollbackFor = Throwable.class)
    public void discard(CoreComponents coreComponents) {
        User requester = getCurrentUser();

        try {
            coreComponentService.discard(coreComponents, requester);
        } catch (Throwable t) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", t.getMessage()));
            throw t;
        }

        reset();
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
    public void createACC() throws IOException {
        User requester = getCurrentUser();
        AggregateCoreComponent acc = coreComponentService.newAggregateCoreComponent(requester);

        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        externalContext.redirect("/core_component/acc/" + acc.getAccId());
    }

    public void createASCCP() throws IOException {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        externalContext.redirect("/core_component/asccp/create");
    }

    public void createBCCP() throws IOException {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        externalContext.redirect("/core_component/bccp/create");
    }

    public String getDiscardIcon(CoreComponents coreComponents) {
        if (hasMultipleRevisions(coreComponents)) {
            return "fa fa-white fa-undo";
        } else {
            return "fa fa-white fa-times";
        }
    }

    public boolean hasMultipleRevisions(CoreComponents coreComponents) {
        return coreComponentService.hasMultipleRevisions(coreComponents);
    }

}
