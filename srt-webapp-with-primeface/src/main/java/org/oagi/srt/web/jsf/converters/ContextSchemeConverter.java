package org.oagi.srt.web.jsf.converters;

import org.oagi.srt.repository.entity.ContextScheme;
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
public class ContextSchemeConverter implements Converter {

    @Autowired
    private ContextSchemeService contextSchemeService;

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        long ctxSchemeId = Long.valueOf(value);
        return new BusinessContextDetailBean.CS(contextSchemeService.findContextSchemeById(ctxSchemeId));
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        BusinessContextDetailBean.CS contextScheme = (BusinessContextDetailBean.CS) value;
        return String.valueOf(contextScheme.getCtxSchemeId());
    }
}
