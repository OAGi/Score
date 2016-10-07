package org.oagi.srt.web.jsf.beans.context.scheme;

import org.oagi.srt.repository.entity.ContextCategory;
import org.oagi.srt.service.ContextCategoryService;
import org.oagi.srt.web.handler.UIHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
public class ContextSchemeSelectionBean extends UIHandler {

    @Autowired
    private ContextCategoryService contextCategoryService;

    private List<ContextCategory> allContextCategories;
    private List<ContextCategory> contextCategories;
    private String name;

    private String paramCtxSchemeId;

    @PostConstruct
    public void init() {
        setContextCategories(allContextCategories());

        String paramCtxSchemeId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("ctxSchemeId");
        if (StringUtils.isEmpty(paramCtxSchemeId)) {
            paramCtxSchemeId = "";
        }
        setParamCtxSchemeId(paramCtxSchemeId);
    }

    public List<ContextCategory> allContextCategories() {
        allContextCategories = contextCategoryService.findAll(Sort.Direction.DESC, "ctxCategoryId");
        return allContextCategories;
    }

    public List<ContextCategory> getContextCategories() {
        return contextCategories;
    }

    public void setContextCategories(List<ContextCategory> contextCategories) {
        this.contextCategories = contextCategories;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParamCtxSchemeId() {
        return paramCtxSchemeId;
    }

    public void setParamCtxSchemeId(String paramCtxSchemeId) {
        this.paramCtxSchemeId = paramCtxSchemeId;
    }

    public List<String> completeInput(String query) {
        return allContextCategories().stream()
                .map(e -> e.getName())
                .filter(e -> e.toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    public void search() {
        if (StringUtils.isEmpty(name)) {
            setContextCategories(allContextCategories());
        } else {
            setContextCategories(
                    allContextCategories().stream()
                            .filter(e -> e.getName().toLowerCase().contains(name.toLowerCase()))
                            .collect(Collectors.toList())
            );
        }
    }
}