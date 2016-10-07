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
import javax.faces.context.FacesContext;

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
        String paramCtxCategoryId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("ctxCategoryId");
        if (StringUtils.isEmpty(paramCtxCategoryId)) {
            setContextCategory(new ContextCategory());
        } else {
            Long ctxCategoryId = Long.parseLong(paramCtxCategoryId);
            if (ctxCategoryId != null) {
                ContextCategory contextCategory = contextCategoryService.findById(ctxCategoryId);
                setContextCategory(contextCategory);
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
        if (!contextCategoryService.findByName(name).isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Name is already taken."));
            return null;
        }

        contextCategoryService.update(contextCategory);

        return "/views/context_category/list.xhtml?faces-redirect=true";
    }

    @Transactional(rollbackFor = Throwable.class)
    public String delete() {
        long ctxCategoryId = contextCategory.getCtxCategoryId();
        if (!contextSchemeService.findByCtxCategoryId(ctxCategoryId).isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "The selected context category cannot be deleted. " +
                                    "The context schemes with the following IDs depend on it. " +
                                    "They need to be deleted first."));
            return null;
        }

        contextCategoryService.deleteById(ctxCategoryId);

        return "/views/context_category/list.xhtml?faces-redirect=true";
    }
}