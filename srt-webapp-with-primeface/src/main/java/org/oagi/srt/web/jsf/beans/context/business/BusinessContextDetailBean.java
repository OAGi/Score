package org.oagi.srt.web.jsf.beans.context.business;

import com.google.common.base.Functions;
import org.oagi.srt.repository.entity.*;
import org.oagi.srt.service.BusinessContextService;
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
import javax.faces.event.AjaxBehaviorEvent;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
public class BusinessContextDetailBean extends UIHandler {

    @Autowired
    private BusinessContextService businessContextService;

    @Autowired
    private ContextCategoryService contextCategoryService;

    @Autowired
    private ContextSchemeService contextSchemeService;

    private LinkedList<BCV> bcvs = new LinkedList();

    public static class BCV {
        private CC contextCategory;

        public BCV(CC contextCategory) {
            this.contextCategory = contextCategory;
        }

        public CC getContextCategory() {
            return contextCategory;
        }

        public void setContextCategory(CC contextCategory) {
            this.contextCategory = contextCategory;
        }
    }

    public static class CC {
        private ContextCategory contextCategory;
        private CS contextScheme;

        public CC(ContextCategory contextCategory) {
            this.contextCategory = contextCategory;
        }

        public Long getCtxCategoryId() {
            return contextCategory.getCtxCategoryId();
        }

        public String getName() {
            return contextCategory.getName();
        }

        public CS getContextScheme() {
            return contextScheme;
        }

        public void setContextScheme(CS contextScheme) {
            this.contextScheme = contextScheme;
        }

        public CC withContextScheme(CS contextScheme) {
            setContextScheme(contextScheme);
            return this;
        }
    }

    public static class CS {
        private ContextScheme contextScheme;
        private CSV contextSchemeValue;

        public CS(ContextScheme contextScheme) {
            this.contextScheme = contextScheme;
        }

        public Long getCtxSchemeId() {
            return contextScheme.getCtxSchemeId();
        }

        public String getSchemeName() {
            return contextScheme.getSchemeName();
        }

        public CSV getContextSchemeValue() {
            return contextSchemeValue;
        }

        public void setContextSchemeValue(CSV contextSchemeValue) {
            this.contextSchemeValue = contextSchemeValue;
        }

        public CS withContextSchemeValue(CSV contextSchemeValue) {
            setContextSchemeValue(contextSchemeValue);
            return this;
        }
    }

    public static class CSV {
        private ContextSchemeValue contextSchemeValue;

        public CSV(ContextSchemeValue contextSchemeValue) {
            this.contextSchemeValue = contextSchemeValue;
        }

        public Long getCtxSchemeValueId() {
            return contextSchemeValue.getCtxSchemeValueId();
        }

        public String getValue() {
            return contextSchemeValue.getValue();
        }

        public ContextSchemeValue getContextSchemeValue() {
            return contextSchemeValue;
        }

        public void setContextSchemeValue(ContextSchemeValue contextSchemeValue) {
            this.contextSchemeValue = contextSchemeValue;
        }
    }

    private List<CC> contextCategories = new ArrayList();
    private Map<Long, List<CS>> contextSchemes = new HashMap();
    private Map<Long, List<CSV>> contextSchemeValues = new HashMap();

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
                setBusinessContextValues(new LinkedList(businessContextValues));
                for (BusinessContextValue businessContextValue : businessContextValues) {
                    ContextSchemeValue contextSchemeValue = businessContextValue.getContextSchemeValue();
                    ContextScheme contextScheme = contextSchemeValue.getContextScheme();
                    ContextCategory contextCategory = contextScheme.getContextCategory();

                    BCV bcv = new BCV(
                            new CC(contextCategory)
                                .withContextScheme(new CS(contextScheme)
                                        .withContextSchemeValue(new CSV(contextSchemeValue))));
                    bcvs.add(bcv);
                }
            }
        }

        List<ContextSchemeValue> contextSchemeValues = contextSchemeService.findAllContextSchemeValues();
        List<ContextScheme> contextSchemes = contextSchemeValues.stream()
                .map(e -> e.getContextScheme()).distinct().collect(Collectors.toList());
        List<ContextCategory> contextCategories = contextSchemes.stream()
                .map(e -> e.getContextCategory()).distinct().collect(Collectors.toList());

        Map<Long, CC> ccMap = new HashMap();
        for (ContextSchemeValue contextSchemeValue : contextSchemeValues) {
            ContextScheme contextScheme = contextSchemeValue.getContextScheme();
            ContextCategory contextCategory = contextScheme.getContextCategory();

            CSV csv = new CSV(contextSchemeValue);
            CS cs = new CS(contextScheme).withContextSchemeValue(csv);
            CC cc = new CC(contextCategory).withContextScheme(cs);

            ccMap.putIfAbsent(cc.getCtxCategoryId(), cc);
            List<CS> csList = this.contextSchemes.get(cc.getCtxCategoryId());
            if (csList == null) {
                csList = new ArrayList();
                this.contextSchemes.put(cc.getCtxCategoryId(), csList);
            }
        }

    }

    public LinkedList<BCV> getBcvs() {
        return bcvs;
    }

    private BusinessContext businessContext;
    private LinkedList<BusinessContextValue> businessContextValues = new LinkedList();

    public BusinessContext getBusinessContext() {
        return businessContext;
    }

    public void setBusinessContext(BusinessContext businessContext) {
        this.businessContext = businessContext;
    }

    public List<BusinessContextValue> getBusinessContextValues() {
        return businessContextValues;
    }

    public void setBusinessContextValues(List<BusinessContextValue> businessContextValues) {
        this.businessContextValues = new LinkedList(businessContextValues);
    }

    public void addBusinessContextValue() {
        this.businessContextValues.addFirst(new BusinessContextValue());
    }

    public void onChange(AjaxBehaviorEvent event) {
        System.out.println("##");
    }

    @Transactional(rollbackFor = Throwable.class)
    public String update() {
        String name = businessContext.getName();
        if (!businessContextService.findByName(name).isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Name is already taken."));
            return null;
        }

        businessContextService.update(businessContext);

        return "/views/context_category/list.xhtml?faces-redirect=true";
    }

    @Transactional(rollbackFor = Throwable.class)
    public String delete() {
        long bizCtxId = businessContext.getBizCtxId();

        //businessContextService.deleteById(bizCtxId);

        return "/views/context_category/list.xhtml?faces-redirect=true";
    }
}