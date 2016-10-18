package org.oagi.srt.web.jsf.beans.context.scheme;

import org.apache.commons.lang3.StringUtils;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.repository.entity.ContextCategory;
import org.oagi.srt.repository.entity.ContextScheme;
import org.oagi.srt.repository.entity.ContextSchemeValue;
import org.oagi.srt.service.ContextCategoryService;
import org.oagi.srt.service.ContextSchemeService;
import org.oagi.srt.web.handler.UIHandler;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
public class ContextSchemeDetailBean extends UIHandler {

    @Autowired
    private ContextCategoryService contextCategoryService;

    @Autowired
    private ContextSchemeService contextSchemeService;

    @PostConstruct
    public void init() {
        String paramCtxSchemeId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("ctxSchemeId");
        if (!StringUtils.isEmpty(paramCtxSchemeId)) {
            Long ctxSchemeId = Long.parseLong(paramCtxSchemeId);
            if (ctxSchemeId != null && ctxSchemeId > 0L) {
                ContextScheme contextScheme = contextSchemeService.findContextSchemeById(ctxSchemeId);
                setContextScheme(contextScheme);
            }
        }
        if (getContextScheme() == null) {
            setContextScheme(new ContextScheme());
        }

        String paramCtxCategoryId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("ctxCategoryId");
        if (!StringUtils.isEmpty(paramCtxCategoryId)) {
            Long ctxCategoryId = Long.parseLong(paramCtxCategoryId);
            ContextCategory contextCategory = contextCategoryService.findById(ctxCategoryId);
            setContextCategory(contextCategory);
        }
    }

    private ContextScheme contextScheme;
    private List<ContextSchemeValue> contextSchemeValues = new ArrayList();
    private List<ContextSchemeValue> deletedContextSchemeValues = new ArrayList();

    private List<ContextCategory> allContextCategories;
    private Map<String, ContextCategory> contextCategoryMap;
    private ContextCategory contextCategory;

    private ContextSchemeValue selectedContextSchemeValue;
    private boolean confirmDifferentNameButSameIdentity;
    private boolean confirmSameAgencyIdButDifferentIdentity;

    public ContextScheme getContextScheme() {
        return contextScheme;
    }

    public void setContextScheme(ContextScheme contextScheme) {
        allContextCategories = contextCategoryService.findAll(Sort.Direction.ASC, "name");
        contextCategoryMap = allContextCategories.stream()
                .collect(Collectors.toMap(e -> e.getName(), Function.identity()));

        this.contextScheme = contextScheme;
        if (contextScheme != null) {
            if (contextScheme.getCtxSchemeId() > 0L) {
                setContextSchemeValues(
                        contextSchemeService.findByOwnerCtxSchemeId(contextScheme.getCtxSchemeId())
                );
                setContextCategory(contextScheme.getContextCategory());
            }
        }
    }

    public List<ContextSchemeValue> getContextSchemeValues() {
        return contextSchemeValues;
    }

    public void setContextSchemeValues(List<ContextSchemeValue> contextSchemeValues) {
        this.contextSchemeValues = contextSchemeValues;
    }

    public String getSelectedContextCategoryName() {
        return (contextCategory != null) ? contextCategory.getName() : null;
    }

    public void setSelectedContextCategoryName(String selectedContextCategoryName) {
        setContextCategory(contextCategoryMap.get(selectedContextCategoryName));
    }

    public ContextCategory getContextCategory() {
        return contextCategory;
    }

    public void setContextCategory(ContextCategory contextCategory) {
        this.contextCategory = contextCategory;
    }

    public List<String> completeInputForContextCategory(String query) {
        if (StringUtils.isEmpty(query)) {
            return allContextCategories.stream()
                    .map(e -> e.getName())
                    .collect(Collectors.toList());
        }
        return allContextCategories.stream()
                .map(e -> e.getName())
                .filter(e -> e.toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    public void onSelectContextCategory(SelectEvent event) {
        setSelectedContextCategoryName(event.getObject().toString());
    }

    public void addContextSchemeValue() {
        ContextSchemeValue contextSchemeValue = new ContextSchemeValue();
        contextSchemeValues.add(contextSchemeValue);
    }

    public ContextSchemeValue getSelectedContextSchemeValue() {
        return selectedContextSchemeValue;
    }

    public void setSelectedContextSchemeValue(ContextSchemeValue selectedContextSchemeValue) {
        this.selectedContextSchemeValue = selectedContextSchemeValue;
    }

    public boolean isConfirmDifferentNameButSameIdentity() {
        return confirmDifferentNameButSameIdentity;
    }

    public void setConfirmDifferentNameButSameIdentity(boolean confirmDifferentNameButSameIdentity) {
        this.confirmDifferentNameButSameIdentity = confirmDifferentNameButSameIdentity;
    }

    public boolean isConfirmSameAgencyIdButDifferentIdentity() {
        return confirmSameAgencyIdButDifferentIdentity;
    }

    public void setConfirmSameAgencyIdButDifferentIdentity(boolean confirmSameAgencyIdButDifferentIdentity) {
        this.confirmSameAgencyIdButDifferentIdentity = confirmSameAgencyIdButDifferentIdentity;
    }

    public void deleteContextSchemeValue() {
        contextSchemeValues.remove(selectedContextSchemeValue);
        deletedContextSchemeValues.add(selectedContextSchemeValue);
        selectedContextSchemeValue = null;
    }

    @Transactional(rollbackFor = Throwable.class)
    public String update() {
        if (contextCategory == null || contextCategory.getCtxCategoryId() <= 0L) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "You have to choose valid context category."));
            return null;
        }

        if (!checkDifferentNameButSameIdentity()) {
            return null;
        }
        if (!checkSameAgencyIdButDifferentIdentity()) {
            return null;
        }

        if (StringUtils.isEmpty(contextScheme.getGuid())) {
            contextScheme.setGuid(Utility.generateGUID());
        }
        if (contextScheme.getCtxSchemeId() == 0L) {
            contextScheme.setCreatedBy(loadAuthentication().getAppUserId());
        }
        contextScheme.setLastUpdatedBy(loadAuthentication().getAppUserId());
        contextScheme.setContextCategory(contextCategory);
        contextSchemeValues.stream().filter(e -> e.getGuid() == null).forEach(e -> e.setGuid(Utility.generateGUID()));

        contextSchemeService.update(contextScheme, contextSchemeValues);
        contextSchemeService.delete(
                deletedContextSchemeValues.stream()
                        .filter(e -> e.getCtxSchemeValueId() > 0L)
                        .collect(Collectors.toList())
        );

        return "/views/context_scheme/list.xhtml?faces-redirect=true";
    }

    private boolean checkDifferentNameButSameIdentity() {
        List<ContextScheme> sameSchemeIdAndSchemeAgencyIds =
                contextSchemeService.findBySchemeIdAndSchemeAgencyId(contextScheme.getSchemeId(), contextScheme.getSchemeAgencyId());
        if (contextScheme.getCtxSchemeId() > 0L) {
            sameSchemeIdAndSchemeAgencyIds = sameSchemeIdAndSchemeAgencyIds.stream()
                    .filter(e -> e.getCtxSchemeId() != contextScheme.getCtxSchemeId())
                    .collect(Collectors.toList());
        }

        if (!sameSchemeIdAndSchemeAgencyIds.isEmpty()) {
            for (ContextScheme sameSchemeIdAndSchemeAgencyId : sameSchemeIdAndSchemeAgencyIds) {
                String a = sameSchemeIdAndSchemeAgencyId.getSchemeVersionId();
                String b = contextScheme.getSchemeVersionId();
                if (StringUtils.equals(a, b)) {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Can't create same identity of Context Scheme."));
                    return false;
                }
            }

            if (!isConfirmDifferentNameButSameIdentity()) {
                for (ContextScheme sameSchemeIdAndSchemeAgencyId : sameSchemeIdAndSchemeAgencyIds) {
                    String a = sameSchemeIdAndSchemeAgencyId.getSchemeName();
                    String b = contextScheme.getSchemeName();
                    if (!StringUtils.equals(a, b)) {
                        RequestContext.getCurrentInstance().execute("PF('confirmDifferentNameButSameIdentity').show()");
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private boolean checkSameAgencyIdButDifferentIdentity() {
        if (!isConfirmSameAgencyIdButDifferentIdentity()) {
            List<ContextScheme> sameSchemeNameAndSchemeAgencyIds =
                    contextSchemeService.findBySchemeNameAndSchemeAgencyId(contextScheme.getSchemeName(), contextScheme.getSchemeAgencyId());
            if (contextScheme.getCtxSchemeId() > 0L) {
                sameSchemeNameAndSchemeAgencyIds = sameSchemeNameAndSchemeAgencyIds.stream()
                        .filter(e -> e.getCtxSchemeId() != contextScheme.getCtxSchemeId())
                        .collect(Collectors.toList());
            }

            if (!sameSchemeNameAndSchemeAgencyIds.isEmpty()) {
                for (ContextScheme sameSchemeNameAndSchemeAgencyId : sameSchemeNameAndSchemeAgencyIds) {
                    String a = sameSchemeNameAndSchemeAgencyId.getSchemeId();
                    String b = contextScheme.getSchemeId();
                    if (!StringUtils.equals(a, b)) {
                        RequestContext.getCurrentInstance().execute("PF('confirmDifferentSchemeIdButSameIdentity').show()");
                        return false;
                    }
                }
            }
        }

        return true;
    }

    @Transactional(rollbackFor = Throwable.class)
    public String delete() {
        contextSchemeService.delete(contextScheme);

        return "/views/context_scheme/list.xhtml?faces-redirect=true";
    }
}