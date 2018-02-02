package org.oagi.srt.web.jsf.beans.cc.module;

import org.oagi.srt.repository.entity.Module;
import org.oagi.srt.repository.entity.ModuleDep;
import org.oagi.srt.repository.entity.Namespace;
import org.oagi.srt.repository.entity.User;
import org.oagi.srt.repository.entity.listener.CreatorModifierAwareEventListener;
import org.oagi.srt.service.ModuleService;
import org.oagi.srt.service.NamespaceService;
import org.oagi.srt.web.handler.UIHandler;
import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import sun.util.calendar.CalendarUtils;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
public class ModuleDetailBean extends UIHandler {

    @Autowired
    private ModuleService moduleService;
    @Autowired
    private NamespaceService namespaceService;

    private Module module;

    private List<Namespace> allNamespaces;
    private Map<String, Namespace> namespaceMap;
    private Namespace namespace;

    private List<Module> allModules;
    private Map<String, Module> moduleMap;

    private ModuleDep.DependencyType[] dependencyTypes;
    private List<ModuleDep> dependedModules;
    private ModuleDep selectedDependedModule;
    private List<ModuleDep> deletedDependedModules;

    @PostConstruct
    public void init() {
        String paramModuleId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("moduleId");
        if (StringUtils.isEmpty(paramModuleId)) {
            setModule(new Module());
        } else {
            Long moduleId = Long.parseLong(paramModuleId);
            if (moduleId != null) {
                Module module = moduleService.findById(moduleId);
                setModule(module);
                setNamespace(module.getNamespace());
            }
        }
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        allNamespaces = namespaceService.findAll(Sort.Direction.ASC, "uri");
        namespaceMap = allNamespaces.stream()
                .collect(Collectors.toMap(e -> e.getUri(), Function.identity()));

        allModules = moduleService.findAll(Sort.Direction.ASC, "module");
        moduleMap = allModules.stream()
                .collect(Collectors.toMap(e -> e.getModule(), Function.identity()));

        setDependedModules(moduleService.findDependedModules(module));
        setDeletedDependedModules(new ArrayList<>());

        this.module = module;
    }

    public Namespace getNamespace() {
        return namespace;
    }

    public void setNamespace(Namespace namespace) {
        this.namespace = namespace;
        if (module != null) {
            module.setNamespace(namespace);
        }
    }

    public List<String> completeInputForNamespace(String query) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(query)) {
            return allNamespaces.stream()
                    .map(e -> e.getUri())
                    .collect(Collectors.toList());
        }
        return allNamespaces.stream()
                .map(e -> e.getUri())
                .filter(e -> e.toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    public void onSelectNamespace(SelectEvent event) {
        setSelectedNamespaceUri(event.getObject().toString());
    }

    public List<Module> completeInputForDependedModule(String query){
        if (org.apache.commons.lang3.StringUtils.isEmpty(query)) {
            return allModules;
        }
        return allModules.stream()
                .filter(e -> e.getModule().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<ModuleDep.DependencyType> completeInputForDependencyType(String query){
        List<ModuleDep.DependencyType> types = Arrays.asList(getDependencyTypes());
        return types.stream()
                .filter(e -> e.toString().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    public String getSelectedNamespaceUri() {
        return (namespace != null) ? namespace.getUri() : null;
    }

    public void setSelectedNamespaceUri(String selectedNamespaceUri) {
        setNamespace(namespaceMap.get(selectedNamespaceUri));
    }

    public ModuleDep.DependencyType[] getDependencyTypes() {
        return ModuleDep.DependencyType.values();
    }

    public void setDependencyTypes(ModuleDep.DependencyType[] dependencyTypes) {
        this.dependencyTypes = dependencyTypes;
    }

    public List<ModuleDep> getDependedModules() {
        return dependedModules;
    }

    public void setDependedModules(List<ModuleDep> dependedModules) {
        this.dependedModules = dependedModules;
    }

    public ModuleDep getSelectedDependedModule() {
        return selectedDependedModule;
    }

    public void setSelectedDependedModule(ModuleDep selectedDependedModule) {
        this.selectedDependedModule = selectedDependedModule;
    }

    public List<ModuleDep> getDeletedDependedModules() {
        return deletedDependedModules;
    }

    public void setDeletedDependedModules(List<ModuleDep> deletedDependedModules) {
        this.deletedDependedModules = deletedDependedModules;
    }

    public void deleteModuleDependency(){
        dependedModules.removeIf(dm -> dm.equals(selectedDependedModule));
        deletedDependedModules.add(selectedDependedModule);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void addModuleDependency(){
        ModuleDep md = new ModuleDep();
        md.setDependingModule(module);

        getDependedModules().add(0, md);
    }

    @Transactional(rollbackFor = Throwable.class)
    public String update() {
        String moduleFilePath = module.getModule();

        if (!isValidModulePath(moduleFilePath)){
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Module file path is not valid."));
            return null;
        }

        if (module.getModuleId() == 0L && moduleService.isExistsModule(moduleFilePath)) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Module file path is already taken."));
            return null;
        }

        User user = getCurrentUser();

        CreatorModifierAwareEventListener eventListener = new CreatorModifierAwareEventListener(user);
        module.addPersistEventListener(eventListener);
        module.addUpdateEventListener(eventListener);

        if (module.getModuleId() == 0L) {
            module.setCreatedBy(user.getAppUserId());
            module.setOwnerUserId(user.getAppUserId());
        }

        module.setNamespace(namespace);

        try {
            module = moduleService.update(module, dependedModules);

            deletedDependedModules.stream().forEach(ddm -> moduleService.delete(ddm));
        } catch (Throwable t) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", t.getMessage()));
            throw t;
        }
        return "/views/core_component/module/list.jsf?faces-redirect=true";
    }

    private boolean isValidModulePath(String moduleFilePath) {
        if (moduleFilePath.startsWith("\\") || moduleFilePath.startsWith("-") || moduleFilePath.startsWith("_") ) return false;

        if (moduleFilePath.endsWith("\\") || moduleFilePath.endsWith("-") || moduleFilePath.endsWith("_") ) return false;

        if (!moduleFilePath.matches("^[a-zA-Z0-9\\\\_-]*$")) return false;

        return true;
    }

    @Transactional(rollbackFor = Throwable.class)
    public String delete() {
        moduleService.delete(module, dependedModules);

        return "/views/core_component/module/list.jsf?faces-redirect=true";
    }

}
