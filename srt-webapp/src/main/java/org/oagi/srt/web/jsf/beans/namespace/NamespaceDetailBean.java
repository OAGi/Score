package org.oagi.srt.web.jsf.beans.namespace;

import org.oagi.srt.repository.entity.Namespace;
import org.oagi.srt.repository.entity.User;
import org.oagi.srt.repository.entity.listener.CreatorModifierAwareEventListener;
import org.oagi.srt.service.NamespaceService;
import org.oagi.srt.web.handler.UIHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
public class NamespaceDetailBean extends UIHandler {

    @Autowired
    private NamespaceService namespaceService;

    private Namespace namespace;

    @PostConstruct
    public void init() {
        String paramNamespaceId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("namespaceId");
        if (StringUtils.isEmpty(paramNamespaceId)) {
            setNamespace(new Namespace());
        } else {
            Long namespaceId = Long.parseLong(paramNamespaceId);
            if (namespaceId != null) {
                Namespace namespace = namespaceService.findById(namespaceId);
                setNamespace(namespace);
            }
        }
    }

    public Namespace getNamespace() {
        return namespace;
    }

    public void setNamespace(Namespace namespace) {
        this.namespace = namespace;
    }

    public String update() {
        User user = getCurrentUser();

        CreatorModifierAwareEventListener eventListener = new CreatorModifierAwareEventListener(user);
        namespace.addPersistEventListener(eventListener);
        namespace.addUpdateEventListener(eventListener);

        if (namespace.getNamespaceId() == 0L) {
            namespace.setOwnerUserId(user.getAppUserId());
        }

        try {
            namespaceService.update(namespace);
        } catch (Throwable t) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", t.getMessage()));
            throw t;
        }
        return "/views/namespace/list.xhtml?faces-redirect=true";
    }
}
