package org.oagi.srt.web.jsf.beans.context.scheme;

import org.apache.commons.lang3.StringUtils;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.repository.BusinessContextValueRepository;
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
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
@Transactional(readOnly = true)
public class ContextSchemeDetailBean extends UIHandler {

    @Autowired
    private ContextCategoryService contextCategoryService;

    @Autowired
    private ContextSchemeService contextSchemeService;

    @Autowired
    private BusinessContextValueRepository businessContextValueRepository;

    @PostConstruct
    public void init() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, String> requestParameterMap = externalContext.getRequestParameterMap();

        String paramCtxSchemeId = requestParameterMap.get("ctxSchemeId");
        if (!StringUtils.isEmpty(paramCtxSchemeId)) {
            Long ctxSchemeId = Long.parseLong(paramCtxSchemeId);
            ContextScheme contextScheme = contextSchemeService.findContextSchemeById(ctxSchemeId);
            setContextScheme(contextScheme);
        } else {
            String paramCtxSchemeGuid = requestParameterMap.get("ctxSchemeGuid");
            if (!StringUtils.isEmpty(paramCtxSchemeGuid)) {
                ContextScheme contextScheme = contextSchemeService.findContextSchemeByGuid(paramCtxSchemeGuid);
                setContextScheme(contextScheme);
            } else {
                setContextScheme(new ContextScheme());
            }
        }

        String paramCtxCategoryId = requestParameterMap.get("ctxCategoryId");
        if (!StringUtils.isEmpty(paramCtxCategoryId)) {
            Long ctxCategoryId = Long.parseLong(paramCtxCategoryId);
            ContextCategory contextCategory = contextCategoryService.findById(ctxCategoryId);
            setContextCategory(contextCategory);
        }

        contextSchemeValueModels = new ListDataModel(contextSchemeValues);
    }

    private ContextScheme contextScheme;
    private List<ContextSchemeValue> contextSchemeValues = new ArrayList();
    private DataModel<ContextSchemeValue> contextSchemeValueModels;
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

    public DataModel<ContextSchemeValue> getContextSchemeValueModels() {
        return contextSchemeValueModels;
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

    public ContextSchemeValue addContextSchemeValue() {
        ContextSchemeValue contextSchemeValue = new ContextSchemeValue();
        contextSchemeValues.add(contextSchemeValue);
        return contextSchemeValue;
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

    @Transactional(rollbackFor = Throwable.class)
    public void deleteContextSchemeValue() {
        ContextSchemeValue selectedContextSchemeValue = getSelectedContextSchemeValue();
        if (selectedContextSchemeValue == null) {
            return;
        }

        long ctxSchemeValueId = selectedContextSchemeValue.getCtxSchemeValueId();
        if (businessContextValueRepository.findByCtxSchemeValueId(ctxSchemeValueId).isEmpty()) {
            contextSchemeValues.remove(selectedContextSchemeValue);
            deletedContextSchemeValues.add(selectedContextSchemeValue);

            setSelectedContextSchemeValue(null);
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "Used context scheme value can't delete."));
        }
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
        if (!validateContextSchemeValues()) {
            return null;
        }

        if (StringUtils.isEmpty(contextScheme.getGuid())) {
            contextScheme.setGuid(Utility.generateGUID());
        }
        if (contextScheme.getCtxSchemeId() == 0L) {
            contextScheme.setCreatedBy(getCurrentUser().getAppUserId());
        }
        contextScheme.setLastUpdatedBy(getCurrentUser().getAppUserId());
        contextScheme.setContextCategory(contextCategory);
        contextSchemeValues.stream().filter(e -> e.getGuid() == null).forEach(e -> e.setGuid(Utility.generateGUID()));

        contextSchemeService.update(contextScheme, contextSchemeValues);
        contextSchemeService.delete(
                deletedContextSchemeValues.stream()
                        .filter(e -> e.getCtxSchemeValueId() > 0L)
                        .collect(Collectors.toList())
        );

        return "/views/context_scheme/list.jsf?faces-redirect=true";
    }

    private boolean validateContextSchemeValues() {
        Map<String, Long> result = contextSchemeValues.stream().collect(
                Collectors.groupingBy(e -> e.getValue() == null ? "" : e.getValue(), Collectors.counting()));
        for (String value : result.keySet()) {
            if (StringUtils.isEmpty(value)) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                                "Please fill out 'Value' field."));
                return false;
            }
        }
        for (Long value : result.values()) {
            if (value > 1) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                                "It doesn't allow duplicate 'Value' fields."));
                return false;
            }
        }

        return true;
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

        return "/views/context_scheme/list.jsf?faces-redirect=true";
    }
}