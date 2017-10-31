package org.oagi.srt.web.jsf.converters;

import org.oagi.srt.repository.ReleaseRepository;
import org.oagi.srt.repository.entity.Release;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import static org.oagi.srt.repository.entity.Release.CURRENT_RELEASE;

@Component
@Scope("request")
public class ReleaseConverter implements Converter {

    @Autowired
    private ReleaseRepository releaseRepository;

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        value = value.trim();
        if (StringUtils.isEmpty(value)) {
            return null;
        }

        String releaseNum = value;
        if (releaseNum.equals(CURRENT_RELEASE.getReleaseNum())) {
            return CURRENT_RELEASE;
        }
        return releaseRepository.findOneByReleaseNum(releaseNum);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value instanceof String) {
            value = ((String) value).trim();
            if (StringUtils.isEmpty(value)) {
                return null;
            }
        }

        Release release = (Release) value;
        return release.getReleaseNum();
    }
}

