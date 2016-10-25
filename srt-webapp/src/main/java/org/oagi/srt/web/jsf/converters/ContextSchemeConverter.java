package org.oagi.srt.web.jsf.converters;

import org.oagi.srt.repository.entity.ContextScheme;
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
public class ContextSchemeConverter implements Converter {

    @Autowired
    private ContextSchemeService contextSchemeService;
    private static final ContextScheme NULL_INSTANCE = new ContextScheme();

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        value = value.trim();
        if (StringUtils.isEmpty(value)) {
            return NULL_INSTANCE;
        }
        long ctxSchemeId;
        try {
            ctxSchemeId = Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
        if (ctxSchemeId <= 0L) {
            return NULL_INSTANCE;
        }
        return contextSchemeService.findContextSchemeById(ctxSchemeId);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value instanceof String) {
            value = ((String) value).trim();
            if (StringUtils.isEmpty(value)) {
                return null;
            }
        }
        ContextScheme contextScheme = (ContextScheme) value;
        return String.valueOf(contextScheme.getCtxSchemeId());
    }
}
