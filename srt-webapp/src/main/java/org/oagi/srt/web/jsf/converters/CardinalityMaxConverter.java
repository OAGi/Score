package org.oagi.srt.web.jsf.converters;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;

@Component
@Scope("request")
public class CardinalityMaxConverter implements Converter, Validator {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        int cardinalityMax;
        if (value instanceof String) {
            if ("unbounded".equals(((String) value).toLowerCase())) {
                cardinalityMax = -1;
            } else {
                try {
                    cardinalityMax = Integer.parseInt((String) value);
                } catch (NumberFormatException e) {
                    throw new ValidatorException(
                            new FacesMessage(SEVERITY_ERROR, "Error",
                                    "'CardinalityMax' must be a number between -1 (unbounded) and " + Integer.MAX_VALUE));
                }
            }
        } else {
            try {
                cardinalityMax = (Integer) value;
            } catch (ClassCastException e) {
                throw new ValidatorException(
                        new FacesMessage(SEVERITY_ERROR, "Error",
                                "'CardinalityMax' must be a number between -1 (unbounded) and " + Integer.MAX_VALUE));
            }
        }

        if (cardinalityMax < -1) {
            throw new ValidatorException(
                    new FacesMessage(SEVERITY_ERROR, "Error",
                            "'CardinalityMax' must be a number between -1 (unbounded) and " + Integer.MAX_VALUE));
        }
    }

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        value = value.trim();
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        if ("unbounded".equals(value.toLowerCase())) {
            return "-1";
        } else {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                context.addMessage(component.getClientId(),
                        new FacesMessage(SEVERITY_ERROR, "Error",
                                "'CardinalityMax' must be a number between -1 (unbounded) and " + Integer.MAX_VALUE));
                return null;
            }
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value instanceof String) {
            value = ((String) value).trim();
            if (StringUtils.isEmpty(value)) {
                return null;
            }

            if ("-1".equals(value)) {
                return "unbounded";
            }
            return (String) value;
        } else if (value instanceof Integer) {
            if (((Integer) value) == -1) {
                return "unbounded";
            }
            return Integer.toString((Integer) value);
        }

        return value.toString();
    }
}
