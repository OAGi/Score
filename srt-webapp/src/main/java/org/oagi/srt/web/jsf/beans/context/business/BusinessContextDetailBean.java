package org.oagi.srt.web.jsf.beans.context.business;

import org.oagi.srt.common.util.Utility;
import org.oagi.srt.repository.AggregateBusinessInformationEntityRepository;
import org.oagi.srt.repository.entity.*;
import org.oagi.srt.service.BusinessContextService;
import org.oagi.srt.service.ContextCategoryService;
import org.oagi.srt.service.ContextSchemeService;
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
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
public class BusinessContextDetailBean extends UIHandler {

    @Autowired
    private AggregateBusinessInformationEntityRepository abieRepository;

    @Autowired
    private BusinessContextService businessContextService;

    @Autowired
    private ContextCategoryService contextCategoryService;

    @Autowired
    private ContextSchemeService contextSchemeService;

    public static class BCV {
        private BusinessContextValue businessContextValue;
        private ContextCategory contextCategory;
        private ContextScheme contextScheme;
        private ContextSchemeValue contextSchemeValue;

        public BCV() {
            setBusinessContextValue(new BusinessContextValue());
        }

        public BCV(BusinessContextValue businessContextValue) {
            this.businessContextValue = businessContextValue;
            this.contextSchemeValue = businessContextValue.getContextSchemeValue();
            this.contextScheme = this.contextSchemeValue.getContextScheme();
            this.contextCategory = this.contextScheme.getContextCategory();
        }

        public BusinessContextValue getBusinessContextValue() {
            return businessContextValue;
        }

        public void setBusinessContextValue(BusinessContextValue businessContextValue) {
            this.businessContextValue = businessContextValue;
        }

        public ContextCategory getContextCategory() {
            return contextCategory;
        }

        public void setContextCategory(ContextCategory contextCategory) {
            this.contextCategory = contextCategory;
        }

        public ContextScheme getContextScheme() {
            return contextScheme;
        }

        public void setContextScheme(ContextScheme contextScheme) {
            this.contextScheme = contextScheme;
        }

        public ContextSchemeValue getContextSchemeValue() {
            return contextSchemeValue;
        }

        public void setContextSchemeValue(ContextSchemeValue contextSchemeValue) {
            this.contextSchemeValue = contextSchemeValue;
        }
    }

    @PostConstruct
    public void init() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, String> requestParameterMap = externalContext.getRequestParameterMap();

        String paramBizCtxId = requestParameterMap.get("bizCtxId");
        if (!StringUtils.isEmpty(paramBizCtxId)) {
            Long bizCtxId = Long.parseLong(paramBizCtxId);
            BusinessContext businessContext = businessContextService.findById(bizCtxId);
            initBusinessContext(businessContext);
        } else {
            String paramBizCtxGuid = requestParameterMap.get("bizCtxGuid");
            if (!StringUtils.isEmpty(paramBizCtxGuid)) {
                BusinessContext businessContext = businessContextService.findOneByGuid(paramBizCtxGuid);
                initBusinessContext(businessContext);
            } else {
                setBusinessContext(new BusinessContext());
            }
        }

        businessContextValueModel = new ListDataModel(businessContextValues);
    }

    private void initBusinessContext(BusinessContext businessContext) {
        setBusinessContext(businessContext);

        long bizCtxId = businessContext.getBizCtxId();
        if (bizCtxId > 0L) {
            List<BusinessContextValue> businessContextValues =
                    businessContextService.findByBizCtxId(businessContext.getBizCtxId());
            setBusinessContextValues(businessContextValues.stream()
                    .map(e -> new BCV(e)).collect(Collectors.toList()));
        }
    }

    private BusinessContext businessContext;
    private LinkedList<BCV> businessContextValues = new LinkedList();
    private DataModel<BCV> businessContextValueModel;
    private BCV selectedBusinessContextValue;
    private List<BCV> deleteBusinessContextValues = new ArrayList();

    public BusinessContext getBusinessContext() {
        return businessContext;
    }

    public void setBusinessContext(BusinessContext businessContext) {
        this.businessContext = businessContext;
    }

    public List<BCV> getBusinessContextValues() {
        return businessContextValues;
    }

    public void setBusinessContextValues(List<BCV> businessContextValues) {
        this.businessContextValues = new LinkedList(businessContextValues);
    }

    public DataModel<BCV> getBusinessContextValueModel() {
        return businessContextValueModel;
    }

    public BCV getSelectedBusinessContextValue() {
        return selectedBusinessContextValue;
    }

    public void setSelectedBusinessContextValue(BCV selectedBusinessContextValue) {
        this.selectedBusinessContextValue = selectedBusinessContextValue;
    }

    public void addBusinessContextValue() {
        this.businessContextValues.addFirst(new BCV());
    }

    public List<ContextCategory> getContextCategories() {
        return contextCategoryService.findAll(Sort.Direction.ASC, "name");
    }

    public List<ContextCategory> completeContextCategory(String query) {
        List<ContextCategory> contextCategories = getContextCategories();
        if (StringUtils.isEmpty(query)) {
            return contextCategories;
        } else {
            return contextCategories.stream()
                    .filter(e -> e.getName().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
        }
    }

    public void onSelectContextCategory(SelectEvent event) {
        BCV currentBusinessContextValue = businessContextValueModel.getRowData();
        currentBusinessContextValue.setContextCategory((ContextCategory) event.getObject());
    }

    public List<ContextScheme> getContextSchemes(Long ctxCategoryId) {
        if (ctxCategoryId == null || ctxCategoryId <= 0L) {
            return Collections.emptyList();
        }
        return contextSchemeService.findByCtxCategoryId(ctxCategoryId);
    }

    public List<ContextScheme> completeContextScheme(String query) {
        FacesContext context = FacesContext.getCurrentInstance();
        Long ctxCategoryId = (Long) UIComponent.getCurrentComponent(context).getAttributes().get("ctxCategoryId");

        List<ContextScheme> contextSchemes = getContextSchemes(ctxCategoryId);
        if (StringUtils.isEmpty(query)) {
            return contextSchemes;
        } else {
            return contextSchemes.stream()
                    .filter(e -> e.getSchemeName().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
        }
    }

    public void onSelectContextScheme(SelectEvent event) {
        BCV currentBusinessContextValue = businessContextValueModel.getRowData();
        currentBusinessContextValue.setContextScheme((ContextScheme) event.getObject());
    }

    public List<ContextSchemeValue> getContextSchemeValues(Long ctxSchemeId) {
        if (ctxSchemeId == null || ctxSchemeId <= 0L) {
            return Collections.emptyList();
        }
        return contextSchemeService.findByOwnerCtxSchemeId(ctxSchemeId);
    }

    public List<ContextSchemeValue> completeContextSchemeValue(String query) {
        FacesContext context = FacesContext.getCurrentInstance();
        Long ctxSchemeId = (Long) UIComponent.getCurrentComponent(context).getAttributes().get("ctxSchemeId");

        List<ContextSchemeValue> contextSchemeValues = getContextSchemeValues(ctxSchemeId);
        if (StringUtils.isEmpty(query)) {
            return contextSchemeValues;
        } else {
            return contextSchemeValues.stream()
                    .filter(e -> e.getValue().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
        }
    }

    public void onSelectContextSchemeValue(SelectEvent event) {
        BCV currentBusinessContextValue = businessContextValueModel.getRowData();
        currentBusinessContextValue.setContextSchemeValue((ContextSchemeValue) event.getObject());
    }

    public void deleteBusinessContextValue() {
        if (selectedBusinessContextValue != null) {
            businessContextValues.remove(selectedBusinessContextValue);
            deleteBusinessContextValues.add(selectedBusinessContextValue);
        }
        selectedBusinessContextValue = null;
    }

    @Transactional(rollbackFor = Throwable.class)
    public String update() {
        String name = businessContext.getName();
        if (!businessContextService.findByName(name).stream()
                .filter(e -> e.getBizCtxId() != businessContext.getBizCtxId())
                .collect(Collectors.toList()).isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Name is already taken."));
            return null;
        }

        if (businessContext.getBizCtxId() <= 0L) {
            businessContext.setGuid(Utility.generateGUID());
            businessContext.setCreatedBy(getCurrentUser().getAppUserId());
        }
        businessContext.setLastUpdatedBy(getCurrentUser().getAppUserId());

        Map<Long, BusinessContextValue> bcvs = new HashMap();
        for (BCV bcv : businessContextValues) {
            BusinessContextValue businessContextValue = bcv.getBusinessContextValue();
            ContextSchemeValue contextSchemeValue = bcv.getContextSchemeValue();
            if (contextSchemeValue == null || StringUtils.isEmpty(contextSchemeValue.getValue())) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                                "The business context value must contain a specified context scheme value."));
                return null;
            }

            if (bcvs.containsKey(contextSchemeValue.getCtxSchemeValueId())) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                                "A duplicate context scheme value exists on the list."));
                return null;
            } else {
                businessContextValue.setContextSchemeValue(contextSchemeValue);
                bcvs.put(contextSchemeValue.getCtxSchemeValueId(), businessContextValue);
            }
        }

        businessContextService.update(businessContext, bcvs.values());

        businessContextService.delete(
                deleteBusinessContextValues.stream()
                        .map(e -> e.getBusinessContextValue())
                        .filter(e -> e.getBizCtxValueId() > 0L)
                        .collect(Collectors.toList())
        );

        return "/views/business_context/list.jsf?faces-redirect=true";
    }

    @Transactional(rollbackFor = Throwable.class)
    public String delete() {
        long bizCtxId = businessContext.getBizCtxId();
        List<AggregateBusinessInformationEntity> abies = abieRepository.findByBizCtxId(bizCtxId);
        if (!abies.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "The selected business context cannot be discarded. " +
                                    "The ABIEs with the following IDs depend on it. " +
                                    "They need to be discarded first."));
            return null;
        }

        businessContextService.deleteById(bizCtxId);

        return "/views/business_context/list.jsf?faces-redirect=true";
    }
}