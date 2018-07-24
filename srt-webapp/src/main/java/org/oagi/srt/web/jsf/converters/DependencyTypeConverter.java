package org.oagi.srt.web.jsf.converters;

import org.oagi.srt.repository.entity.ModuleDep;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

@Component
@Scope("request")
public class DependencyTypeConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        value = value.trim();

        switch (value) {
            case "Include":
                return ModuleDep.DependencyType.INCLUDE;
            default:
                return ModuleDep.DependencyType.IMPORT;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        ModuleDep.DependencyType dt = (ModuleDep.DependencyType) value;

        switch(dt) {
            case INCLUDE:
                return "Include";
            default:
                return "Import";
        }
    }
}