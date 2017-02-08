package org.oagi.srt.web.jsf.converters;

import org.oagi.srt.repository.entity.Namespace;
import org.oagi.srt.service.NamespaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

@Component
@Scope("request")
public class NamespaceConverter implements Converter {

    @Autowired
    private NamespaceService namespaceService;
    private static final Namespace NULL_INSTANCE = new Namespace();

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        value = value.trim();
        if (StringUtils.isEmpty(value)) {
            return NULL_INSTANCE;
        }
        long namespaceId;
        try {
            namespaceId = Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
        if (namespaceId <= 0L) {
            return NULL_INSTANCE;
        }
        return namespaceService.findById(namespaceId);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value instanceof String) {
            value = ((String) value).trim();
            if (StringUtils.isEmpty(value)) {
                return null;
            }
        }
        Namespace namespace = (Namespace) value;
        return String.valueOf(namespace.getNamespaceId());
    }
}

