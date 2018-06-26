package org.oagi.srt.web.jsf.converters;

import org.oagi.srt.repository.AssociationCoreComponentPropertyRepository;
import org.oagi.srt.repository.entity.AssociationCoreComponentProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

@Component
@Scope("request")
public class AssociationCoreComponentPropertyConverter implements Converter {

    @Autowired
    private AssociationCoreComponentPropertyRepository asccpRepository;
    private static final AssociationCoreComponentProperty NULL_INSTANCE = new AssociationCoreComponentProperty();

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        value = value.trim();
        if (StringUtils.isEmpty(value)) {
            return NULL_INSTANCE;
        }
        long asccpId = Long.valueOf(value);
        if (asccpId <= 0L) {
            return NULL_INSTANCE;
        }
        return asccpRepository.findById(asccpId).orElse(null);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value instanceof String) {
            value = ((String) value).trim();
            if (StringUtils.isEmpty(value)) {
                return null;
            }
        }
        AssociationCoreComponentProperty asccp = (AssociationCoreComponentProperty) value;
        return String.valueOf(asccp.getAsccpId());
    }
}
