package org.oagi.srt.web.jsf.converters;

import org.oagi.srt.repository.entity.ContextCategory;
import org.oagi.srt.service.ContextCategoryService;
import org.oagi.srt.web.jsf.beans.context.business.BusinessContextDetailBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

@Component
@Scope("request")
public class ContextCategoryConverter implements Converter {

    @Autowired
    private ContextCategoryService contextCategoryService;

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        long ctxCategoryId = Long.valueOf(value);
        return new BusinessContextDetailBean.CC(contextCategoryService.findById(ctxCategoryId));
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        BusinessContextDetailBean.CC contextCategory = (BusinessContextDetailBean.CC) value;
        return String.valueOf(contextCategory.getCtxCategoryId());
    }
}
