package org.oagi.srt.web.jsf.converters;

import org.oagi.srt.repository.BusinessDataTypePrimitiveRestrictionRepository;
import org.oagi.srt.repository.entity.BusinessDataTypePrimitiveRestriction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

@Component
@Scope("request")
public class BusinessDataTypePrimitiveRestrictionConverter implements Converter {

    @Autowired
    private BusinessDataTypePrimitiveRestrictionRepository bdtPriRestriRepository;
    private static final BusinessDataTypePrimitiveRestriction NULL_INSTANCE = new BusinessDataTypePrimitiveRestriction();

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        value = value.trim();
        if (StringUtils.isEmpty(value)) {
            return NULL_INSTANCE;
        }
        long bdtPriRestriId = Long.valueOf(value);
        if (bdtPriRestriId <= 0L) {
            return NULL_INSTANCE;
        }
        return bdtPriRestriRepository.findOne(bdtPriRestriId);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value instanceof String) {
            value = ((String) value).trim();
            if (StringUtils.isEmpty(value)) {
                return null;
            }
        }
        BusinessDataTypePrimitiveRestriction bdtPriRestri = (BusinessDataTypePrimitiveRestriction) value;
        return String.valueOf(bdtPriRestri.getBdtPriRestriId());
    }
}
