package org.oagi.srt.web.jsf.converters;

import org.oagi.srt.repository.BasicCoreComponentPropertyRepository;
import org.oagi.srt.repository.entity.BasicCoreComponentProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

@Component
@Scope("request")
public class BasicCoreComponentPropertyConverter implements Converter {

    @Autowired
    private BasicCoreComponentPropertyRepository bccpRepository;
    private static final BasicCoreComponentProperty NULL_INSTANCE = new BasicCoreComponentProperty();

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        value = value.trim();
        if (StringUtils.isEmpty(value)) {
            return NULL_INSTANCE;
        }
        long bccpId = Long.valueOf(value);
        if (bccpId <= 0L) {
            return NULL_INSTANCE;
        }
        return bccpRepository.findOne(bccpId);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value instanceof String) {
            value = ((String) value).trim();
            if (StringUtils.isEmpty(value)) {
                return null;
            }
        }
        BasicCoreComponentProperty bccp = (BasicCoreComponentProperty) value;
        return String.valueOf(bccp.getBccpId());
    }
}
