package org.oagi.srt.web.jsf.beans.context.business;

import org.oagi.srt.common.util.Utility;
import org.oagi.srt.repository.AggregateBusinessInformationEntityRepository;
import org.oagi.srt.repository.entity.*;
import org.oagi.srt.service.BusinessContextService;
import org.oagi.srt.service.ContextCategoryService;
import org.oagi.srt.service.ContextSchemeService;
import org.oagi.srt.web.handler.UIHandler;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
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
            setContextScheme(new ContextScheme());
        }

        public ContextScheme getContextScheme() {
            return contextScheme;
        }

        public void setContextScheme(ContextScheme contextScheme) {
            this.contextScheme = contextScheme;
            setContextSchemeValue(new ContextSchemeValue());
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
        String paramBizCtxId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("bizCtxId");
        if (StringUtils.isEmpty(paramBizCtxId)) {
            setBusinessContext(new BusinessContext());
        } else {
            Long bizCtxId = Long.parseLong(paramBizCtxId);
            if (bizCtxId != null) {
                BusinessContext businessContext = businessContextService.findById(bizCtxId);
                setBusinessContext(businessContext);

                List<BusinessContextValue> businessContextValues =
                        businessContextService.findByBizCtxId(businessContext.getBizCtxId());
                setBusinessContextValues(businessContextValues.stream()
                        .map(e -> new BCV(e)).collect(Collectors.toList()));
            }
        }
    }

    private BusinessContext businessContext;
    private LinkedList<BCV> businessContextValues = new LinkedList();
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

    public List<ContextScheme> getContextSchemes(Long ctxCategoryId) {
        if (ctxCategoryId == null || ctxCategoryId <= 0L) {
            return Collections.emptyList();
        }
        return contextSchemeService.findByCtxCategoryId(ctxCategoryId);
    }

    public List<ContextSchemeValue> getContextSchemeValues(Long ctxSchemeId) {
        if (ctxSchemeId == null || ctxSchemeId <= 0L) {
            return Collections.emptyList();
        }
        return contextSchemeService.findByOwnerCtxSchemeId(ctxSchemeId);
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
            businessContext.setCreatedBy(loadAuthentication().getAppUserId());
        }
        businessContext.setLastUpdatedBy(loadAuthentication().getAppUserId());

        businessContextService.update(businessContext, businessContextValues.stream()
                .map(e -> {
                    BusinessContextValue businessContextValue = e.getBusinessContextValue();
                    businessContextValue.setContextSchemeValue(e.getContextSchemeValue());
                    return businessContextValue;
                }).collect(Collectors.toList()));

        businessContextService.delete(
                deleteBusinessContextValues.stream()
                        .map(e -> e.getBusinessContextValue())
                        .filter(e -> e.getBizCtxValueId() > 0L)
                        .collect(Collectors.toList())
        );

        return "/views/business_context/list.xhtml?faces-redirect=true";
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

        return "/views/business_context/list.xhtml?faces-redirect=true";
    }
}