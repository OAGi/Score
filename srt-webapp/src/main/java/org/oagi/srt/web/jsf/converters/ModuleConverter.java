package org.oagi.srt.web.jsf.converters;

import org.oagi.srt.repository.entity.Module;
import org.oagi.srt.service.ModuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

@Component
@Scope("request")
public class ModuleConverter implements Converter {

    @Autowired
    private ModuleService moduleService;
    private static final Module NULL_INSTANCE = new Module();

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        value = value.trim();
        if (StringUtils.isEmpty(value)) {
            return NULL_INSTANCE;
        }
        return moduleService.findByModule(value);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value instanceof String) {
            value = ((String) value).trim();
            if (StringUtils.isEmpty(value)) {
                return null;
            }
        }
        Module module = (Module) value;
        return module.getModule();
    }
}
