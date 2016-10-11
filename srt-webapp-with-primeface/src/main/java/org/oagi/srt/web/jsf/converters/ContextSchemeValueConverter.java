package org.oagi.srt.web.jsf.converters;

import org.oagi.srt.repository.entity.ContextSchemeValue;
import org.oagi.srt.service.ContextSchemeService;
import org.oagi.srt.web.jsf.beans.context.business.BusinessContextDetailBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

@Component
@Scope("request")
public class ContextSchemeValueConverter implements Converter {

    @Autowired
    private ContextSchemeService contextSchemeService;

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        long ctxSchemeValueId = Long.valueOf(value);
        return new BusinessContextDetailBean.CSV(contextSchemeService.findContextSchemeValueById(ctxSchemeValueId));
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        BusinessContextDetailBean.CSV contextSchemeValue = (BusinessContextDetailBean.CSV) value;
        return String.valueOf(contextSchemeValue.getCtxSchemeValueId());
    }
}
