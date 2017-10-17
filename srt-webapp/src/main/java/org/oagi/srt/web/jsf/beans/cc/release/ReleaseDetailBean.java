package org.oagi.srt.web.jsf.beans.cc.release;

import org.oagi.srt.repository.entity.Namespace;
import org.oagi.srt.repository.entity.Release;
import org.oagi.srt.repository.entity.ReleaseState;
import org.oagi.srt.repository.entity.User;
import org.oagi.srt.repository.entity.listener.CreatorModifierAwareEventListener;
import org.oagi.srt.service.NamespaceService;
import org.oagi.srt.service.ReleaseService;
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
public class ReleaseDetailBean extends UIHandler {

    @Autowired
    private ReleaseService releaseService;
    @Autowired
    private NamespaceService namespaceService;

    private Release release;

    private List<Namespace> allNamespaces;
    private Map<String, Namespace> namespaceMap;
    private Namespace namespace;

    @PostConstruct
    public void init() {
        String paramReleaseId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("releaseId");
        if (StringUtils.isEmpty(paramReleaseId)) {
            setRelease(new Release());
        } else {
            Long releaseId = Long.parseLong(paramReleaseId);
            if (releaseId != null) {
                Release release = releaseService.findById(releaseId);
                setRelease(release);
                setNamespace(release.getNamespace());
            }
        }
    }

    public Release getRelease() {
        return release;
    }

    public void setRelease(Release release) {
        allNamespaces = namespaceService.findAll(Sort.Direction.ASC, "uri");
        namespaceMap = allNamespaces.stream()
                .collect(Collectors.toMap(e -> e.getUri(), Function.identity()));

        this.release = release;
    }

    public Namespace getNamespace() {
        return namespace;
    }

    public void setNamespace(Namespace namespace) {
        this.namespace = namespace;
        if (release != null) {
            release.setNamespace(namespace);
        }
    }

    public String getSelectedNamespaceUri() {
        return (namespace != null) ? namespace.getUri() : null;
    }

    public void setSelectedNamespaceUri(String selectedNamespaceUri) {
        setNamespace(namespaceMap.get(selectedNamespaceUri));
    }

    public String update() {
        String releaseNum = release.getReleaseNum();
        if (releaseService.isExistsReleaseNum(releaseNum, release.getReleaseId())) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Release Number is already taken."));
            return null;
        }

        User user = getCurrentUser();

        CreatorModifierAwareEventListener eventListener = new CreatorModifierAwareEventListener(user);
        release.addPersistEventListener(eventListener);
        release.addUpdateEventListener(eventListener);

        if (release.getReleaseId() == 0L) {
            release.setCreatedBy(user.getAppUserId());
            release.setState(ReleaseState.Draft);
        }

        release.setNamespace(namespace);

        try {
            releaseService.update(release);
        } catch (Throwable t) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", t.getMessage()));
            throw t;
        }
        return "/views/core_component/release/list.jsf?faces-redirect=true";
    }

    @Transactional(rollbackFor = Throwable.class)
    public String delete() {
//        releaseService.delete(release); todo: implement this method...
//
        return "/views/core_component/release/list.jsf?faces-redirect=true";
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
}
