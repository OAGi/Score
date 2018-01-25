package org.oagi.srt.web.jsf.beans.cc.module;

import org.oagi.srt.repository.entity.Module;
import org.oagi.srt.repository.entity.Namespace;
import org.oagi.srt.repository.entity.Release;
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

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.util.List;
import java.util.Map;
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

    public String getSelectedNamespaceUri() {
        return (namespace != null) ? namespace.getUri() : null;
    }

    public void setSelectedNamespaceUri(String selectedNamespaceUri) {
        setNamespace(namespaceMap.get(selectedNamespaceUri));
    }

    @Transactional(rollbackFor = Throwable.class)
    public String update() {
        String moduleFilePath = module.getModule();

        if (moduleService.isExistsModule(moduleFilePath)) {
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
            module = moduleService.update(module);

        } catch (Throwable t) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", t.getMessage()));
            throw t;
        }
        return "/views/core_component/module/list.jsf?faces-redirect=true";
    }

    @Transactional(rollbackFor = Throwable.class)
    public String delete() {
        moduleService.delete(module);

        return "/views/core_component/module/list.jsf?faces-redirect=true";
    }
}
