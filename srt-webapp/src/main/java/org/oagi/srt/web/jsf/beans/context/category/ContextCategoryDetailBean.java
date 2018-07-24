package org.oagi.srt.web.jsf.beans.context.category;

import org.oagi.srt.repository.entity.ContextCategory;
import org.oagi.srt.service.ContextCategoryService;
import org.oagi.srt.service.ContextSchemeService;
import org.oagi.srt.web.handler.UIHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
public class ContextCategoryDetailBean extends UIHandler {

    @Autowired
    private ContextCategoryService contextCategoryService;

    @Autowired
    private ContextSchemeService contextSchemeService;

    @PostConstruct
    public void init() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, String> requestParameterMap = externalContext.getRequestParameterMap();

        String paramCtxCategoryId = requestParameterMap.get("ctxCategoryId");
        if (!StringUtils.isEmpty(paramCtxCategoryId)) {
            Long ctxCategoryId = Long.parseLong(paramCtxCategoryId);
            if (ctxCategoryId != null) {
                ContextCategory contextCategory = contextCategoryService.findById(ctxCategoryId);
                setContextCategory(contextCategory);
            }
        } else {
            String paramCtxCategoryGuid = requestParameterMap.get("ctxCategoryGuid");
            if (!StringUtils.isEmpty(paramCtxCategoryGuid)) {
                ContextCategory contextCategory = contextCategoryService.findOneByGuid(paramCtxCategoryGuid);
                setContextCategory(contextCategory);
            } else {
                setContextCategory(new ContextCategory());
            }
        }
    }

    private ContextCategory contextCategory;

    public ContextCategory getContextCategory() {
        return contextCategory;
    }

    public void setContextCategory(ContextCategory contextCategory) {
        this.contextCategory = contextCategory;
    }

    @Transactional(rollbackFor = Throwable.class)
    public String update() {
        String name = contextCategory.getName();
        if (!contextCategoryService.findByName(name).stream()
                .filter(e -> e.getCtxCategoryId() != contextCategory.getCtxCategoryId())
                .collect(Collectors.toList()).isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Name is already taken."));
            return null;
        }

        contextCategoryService.update(contextCategory);

        return "/views/context_category/list.jsf?faces-redirect=true";
    }

    @Transactional(rollbackFor = Throwable.class)
    public String delete() {
        long ctxCategoryId = contextCategory.getCtxCategoryId();

        if (!contextSchemeService.findByCtxCategoryId(ctxCategoryId).isEmpty()) {
            List<String> schemeIds = contextSchemeService.findByCtxCategoryId(getContextCategory().getCtxCategoryId()).stream()
                    .map(e -> e.getGuid()).collect(Collectors.toList());

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "The selected context category cannot be deleted. " +
                                    "The context schemes with the following GUIDs (" +
                                    schemeIds +
                                    ") depend on it. " +
                                    "They need to be deleted first."));
            return null;
        }

        contextCategoryService.deleteById(ctxCategoryId);

        return "/views/context_category/list.jsf?faces-redirect=true";
    }
}