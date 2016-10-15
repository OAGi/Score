package org.oagi.srt.web.jsf.converters;

import org.oagi.srt.repository.TopLevelConceptRepository;
import org.oagi.srt.repository.entity.ContextCategory;
import org.oagi.srt.repository.entity.TopLevelConcept;
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
public class TopLevelConceptConverter implements Converter {

    @Autowired
    private TopLevelConceptRepository topLevelConceptRepository;
    private static final TopLevelConcept NULL_INSTANCE = new TopLevelConcept();

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
        return topLevelConceptRepository.findOne(asccpId);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value instanceof String) {
            value = ((String) value).trim();
            if (StringUtils.isEmpty(value)) {
                return null;
            }
        }
        TopLevelConcept topLevelConcept = (TopLevelConcept) value;
        return String.valueOf(topLevelConcept.getAsccpId());
    }
}
