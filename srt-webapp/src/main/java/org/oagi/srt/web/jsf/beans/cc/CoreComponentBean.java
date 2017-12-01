package org.oagi.srt.web.jsf.beans.cc;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.oagi.srt.repository.AggregateCoreComponentRepository;
import org.oagi.srt.repository.ReleaseRepository;
import org.oagi.srt.repository.entity.*;
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
import javax.faces.event.AjaxBehaviorEvent;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.oagi.srt.common.util.Utility.*;
import static org.oagi.srt.repository.entity.OagisComponentType.UserExtensionGroup;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
@Transactional(readOnly = true)
public class CoreComponentBean extends AbstractCoreComponentBean {

    private static final String SELECTED_RELEASE_KEY = "_core_component/selected_release";
    private static final String SELECTED_TYPES_KEY = "_core_component/selected_types";
    private static final String SELECTED_STATES_KEY = "_core_component/selected_states";
    private static final String SEARCH_TEXT_DEN_KEY = "_core_component/search_text_den";
    private static final String SEARCH_TEXT_DEFINITION_KEY = "_core_component/search_text_definition";
    private static final String SEARCH_TEXT_MODULE_KEY = "_core_component/search_text_module";

    @Autowired
    private CoreComponentService coreComponentService;

    @Autowired
    private AggregateCoreComponentRepository accRepository;

    @Autowired
    private ReleaseRepository releaseRepository;

    private Release release;

    private List<CoreComponents> allCoreComponents;
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
        searchAllCoreComponents();

        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, Object> sessionMap = externalContext.getSessionMap();

        Object selectedRelease = sessionMap.get(SELECTED_RELEASE_KEY);
        if (selectedRelease != null) {
            this.release = releaseRepository.findOne((Long) selectedRelease);
        } else {
            setRelease(Release.WORKING_RELEASE);
        }

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

        setCoreComponents(findCoreComponents());
    }

    public void invalidate() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, Object> sessionMap = externalContext.getSessionMap();

        sessionMap.remove(SELECTED_RELEASE_KEY);
        sessionMap.remove(SELECTED_TYPES_KEY);
        sessionMap.remove(SELECTED_STATES_KEY);
        sessionMap.remove(SEARCH_TEXT_DEN_KEY);
        sessionMap.remove(SEARCH_TEXT_DEFINITION_KEY);
        sessionMap.remove(SEARCH_TEXT_MODULE_KEY);
    }

    private void searchAllCoreComponents() {
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

        List<String> selectedTypes = Arrays.asList(
                "ACC", "ASCC", "ASCCP", "BCC", "BCCP"
        );
        List<CoreComponentState> selectedStates = Arrays.asList(
                CoreComponentState.Editing,
                CoreComponentState.Candidate,
                CoreComponentState.Published);

        boolean isSortColumnAscending = false;

        List<CoreComponents> coreComponents = coreComponentService.getCoreComponents(
                selectedTypes, selectedStates, release,
                new Sort.Order((isSortColumnAscending) ? Sort.Direction.ASC : Sort.Direction.DESC, sortProperty));

        Map<String, CoreComponents> ccMap = new LinkedHashMap();
        coreComponents.stream().forEachOrdered(e -> {
            String guid = e.getGuid();
            if (ccMap.containsKey(guid)) {
                CoreComponents p = ccMap.get(guid);
                Long pReleaseId = p.getReleaseId();
                if (pReleaseId == null) {
                    pReleaseId = 0L;
                }
                Long eReleaseId = e.getReleaseId();
                if (eReleaseId == null) {
                    eReleaseId = 0L;
                }

                if (pReleaseId > eReleaseId) {
                    return;
                }
            }

            ccMap.put(guid, e);
        });

        coreComponents = new ArrayList(ccMap.values());

        directory = createDirectory(coreComponents,
                new String[]{DEN_FIELD, MODULE_FIELD},
                new String[]{" ", Pattern.quote("\\")},
                CoreComponents::getDen, CoreComponents::getModule);
        definition_index = createDirectoryForText(coreComponents,
                new String[]{DEFINITION_FIELD},
                CoreComponents::getDefinition);

        allCoreComponents = coreComponents;
    }

    private Directory directory, definition_index;
    private static final String DEN_FIELD = "den";
    private static final String DEFINITION_FIELD = "definition";
    private static final String MODULE_FIELD = "module";

    public void onReleaseChange(AjaxBehaviorEvent behaviorEvent) {
        setRelease(getRelease());
        searchAllCoreComponents();
        search();
    }

    public Release getRelease() {
        return release;
    }

    public void setRelease(Release release) {
        this.release = release;

        if (release != null) {
            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            Map<String, Object> sessionMap = externalContext.getSessionMap();
            sessionMap.put(SELECTED_RELEASE_KEY, this.release.getReleaseId());
        }
    }

    public List<Release> getReleases() {
        List<Release> releases = new ArrayList();
        releases.add(Release.WORKING_RELEASE);
        releases.addAll(releaseRepository.findAll(new Sort(Sort.Direction.ASC, "releaseId")));
        return releases;
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

        search();
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

        search();
    }

    public List<CoreComponents> findCoreComponents() {
        List<CoreComponents> coreComponents = allCoreComponents;

        String den = getSearchTextForDen();
        String definition = getSearchTextForDefinition();
        String module = getSearchTextForModule();

        try {
            if (!StringUtils.isEmpty(definition)) {
                IndexReader reader = DirectoryReader.open(definition_index);
                IndexSearcher searcher = new IndexSearcher(reader);

                Query q = new QueryParser(DEFINITION_FIELD, new StandardAnalyzer()).parse(definition);
                TopDocs topDocs = searcher.search(q, coreComponents.size());
                if (topDocs.totalHits == 0L) {
                    definition = Arrays.stream(definition.split(" "))
                            .map(e -> suggestWord(e, definition_index, DEFINITION_FIELD))
                            .collect(Collectors.joining(" "));

                    q = new QueryParser(DEFINITION_FIELD, new StandardAnalyzer()).parse(definition);
                    topDocs = searcher.search(q, coreComponents.size());
                }

                List<CoreComponents> l = new ArrayList();
                for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                    Document d = searcher.doc(scoreDoc.doc);
                    l.add(toObject(d.getBinaryValue("obj").bytes, CoreComponents.class));
                }
                coreComponents = l;
            }
        } catch (IOException | ParseException e) {
            throw new IllegalStateException(e);
        }

        coreComponents = coreComponents.stream()
                .filter(e -> selectedTypes.contains(e.getType()))
                .filter(e -> selectedStates.contains(e.getState()))
                .filter(new DenSearchFilter(den))
                .filter(new ModuleSearchFilter(module))
                .sorted((a, b) -> {
                    if (!StringUtils.isEmpty(den)) {
                        return compareLevenshteinDistance(den, a, b, CoreComponents::getDen);
                    }

                    if (!StringUtils.isEmpty(module)) {
                        return compareLevenshteinDistance(module, a, b, CoreComponents::getModule, Pattern.quote("\\"));
                    }

                    return 0;
                })
                .collect(Collectors.toList());
        return coreComponents;
    }

    public void setCoreComponents(List<CoreComponents> coreComponents) {
        this.coreComponents = coreComponents;
    }

    public List<CoreComponents> getCoreComponents() {
        return coreComponents;
    }

    private interface SearchFilter extends Predicate<CoreComponents> {
    }

    private class DenSearchFilter implements SearchFilter {
        private String q;

        public DenSearchFilter(String q) {
            if (!StringUtils.isEmpty(q)) {
                this.q = Arrays.asList(q.split(" ")).stream()
                        .map(s -> suggestWord(s.toLowerCase(), directory, DEN_FIELD))
                        .collect(Collectors.joining(" "));
            } else {
                this.q = q;
            }
        }

        @Override
        public boolean test(CoreComponents e) {
            if (StringUtils.isEmpty(q)) {
                return true;
            }

            List<String> den = Arrays.asList(e.getDen().toLowerCase().split(" "));
            String[] split = q.split(" ");
            for (String s : split) {
                if (!den.contains(s)) {
                    return false;
                }
            }
            return true;
        }
    }

    private class ModuleSearchFilter implements SearchFilter {

        private String q;

        public ModuleSearchFilter(String q) {
            if (!StringUtils.isEmpty(q)) {
                this.q = Arrays.asList(q.split(Pattern.quote("\\"))).stream()
                        .map(s -> suggestWord(s.toLowerCase(), directory, MODULE_FIELD))
                        .collect(Collectors.joining("\\"));
            } else {
                this.q = q;
            }
        }

        @Override
        public boolean test(CoreComponents e) {
            if (StringUtils.isEmpty(q)) {
                return true;
            }

            if (StringUtils.isEmpty(e.getModule())) {
                return false;
            }

            List<String> module = Arrays.asList(e.getModule().toLowerCase().split(Pattern.quote("\\")));
            if (StringUtils.isEmpty(module)) {
                return false;
            }
            String[] split = q.split(Pattern.quote("\\"));
            for (String s : split) {
                if (!module.contains(s)) {
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

        onSearchTextForDenChange();
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

        onSearchTextForDefinitionChange();
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

        onSearchTextForModuleChange();
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

        search();
    }

    public void onSortEvent(SortEvent sortEvent) {
        sortColumnHeaderText = sortEvent.getSortColumn().getHeaderText();
        isSortColumnAscending = sortEvent.isAscending();
    }

    public void search() {
        setCoreComponents(findCoreComponents());
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

    public String getFullRevisionNum (CoreComponents cc) {
        return coreComponentService.getFullRevisionNum(cc, release);
    }

}
