package org.oagi.srt.web.jsf.converters;

import org.oagi.srt.repository.entity.ContextCategory;
import org.oagi.srt.service.ContextCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

@Component
@Scope("request")
public class ContextCategoryConverter implements Converter {

    @Autowired
    private ContextCategoryService contextCategoryService;
    private static final ContextCategory NULL_INSTANCE = new ContextCategory();

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        value = value.trim();
        if (StringUtils.isEmpty(value)) {
            return NULL_INSTANCE;
        }
        long ctxCategoryId = Long.valueOf(value);
        if (ctxCategoryId <= 0L) {
            return NULL_INSTANCE;
        }
        return contextCategoryService.findById(ctxCategoryId);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value instanceof String) {
            value = ((String) value).trim();
            if (StringUtils.isEmpty(value)) {
                return null;
            }
        }
        ContextCategory contextCategory = (ContextCategory) value;
        return String.valueOf(contextCategory.getCtxCategoryId());
    }
}
