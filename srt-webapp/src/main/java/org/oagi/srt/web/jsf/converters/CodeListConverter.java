package org.oagi.srt.web.jsf.converters;

import org.oagi.srt.repository.entity.CodeList;
import org.oagi.srt.service.CodeListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

@Component
@Scope("request")
public class CodeListConverter implements Converter {

    @Autowired
    private CodeListService codeListService;
    private static final CodeList NULL_INSTANCE = new CodeList();

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        value = value.trim();
        if (StringUtils.isEmpty(value)) {
            return NULL_INSTANCE;
        }
        long codeListId;
        try {
            codeListId = Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
        if (codeListId <= 0L) {
            return NULL_INSTANCE;
        }
        return codeListService.findOne(codeListId);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value instanceof String) {
            value = ((String) value).trim();
            if (StringUtils.isEmpty(value)) {
                return null;
            }
        }
        CodeList codeList = (CodeList) value;
        return String.valueOf(codeList.getCodeListId());
    }
}
