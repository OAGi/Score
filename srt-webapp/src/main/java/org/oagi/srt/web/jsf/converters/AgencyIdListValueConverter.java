package org.oagi.srt.web.jsf.converters;

import org.oagi.srt.repository.AgencyIdListValueRepository;
import org.oagi.srt.repository.entity.AgencyIdListValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

@Component
@Scope("request")
public class AgencyIdListValueConverter implements Converter {

    @Autowired
    private AgencyIdListValueRepository agencyIdListValueRepository;
    private static final AgencyIdListValue NULL_INSTANCE = new AgencyIdListValue();

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        value = value.trim();
        if (StringUtils.isEmpty(value)) {
            return NULL_INSTANCE;
        }
        long agencyIdListValueId;
        try {
            agencyIdListValueId = Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
        if (agencyIdListValueId <= 0L) {
            return NULL_INSTANCE;
        }
        return agencyIdListValueRepository.findOne(agencyIdListValueId);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value instanceof String) {
            value = ((String) value).trim();
            if (StringUtils.isEmpty(value)) {
                return null;
            }
        }
        AgencyIdListValue agencyIdListValue = (AgencyIdListValue) value;
        return String.valueOf(agencyIdListValue.getAgencyIdListValueId());
    }
}

