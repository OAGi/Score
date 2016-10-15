package org.oagi.srt.web.jsf.converters;

import org.oagi.srt.repository.entity.ContextSchemeValue;
import org.oagi.srt.service.ContextSchemeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

@Component
@Scope("request")
public class ContextSchemeValueConverter implements Converter {

    @Autowired
    private ContextSchemeService contextSchemeService;
    private static final ContextSchemeValue NULL_INSTANCE = new ContextSchemeValue();

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        value = value.trim();
        if (StringUtils.isEmpty(value)) {
            return NULL_INSTANCE;
        }
        long ctxSchemeValueId = Long.valueOf(value);
        if (ctxSchemeValueId <= 0L) {
            return NULL_INSTANCE;
        }
        return contextSchemeService.findContextSchemeValueById(ctxSchemeValueId);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value instanceof String) {
            value = ((String) value).trim();
            if (StringUtils.isEmpty(value)) {
                return null;
            }
        }
        ContextSchemeValue contextSchemeValue = (ContextSchemeValue) value;
        return String.valueOf(contextSchemeValue.getCtxSchemeValueId());
    }
}
