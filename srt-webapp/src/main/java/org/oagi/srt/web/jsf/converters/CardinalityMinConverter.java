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
public class CardinalityMinConverter implements Converter, Validator {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        int cardinalityMin = Integer.MIN_VALUE;
        if (value instanceof String) {
            try {
                cardinalityMin = Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                throw new ValidatorException(
                        new FacesMessage(SEVERITY_ERROR, "Error",
                                "'CardinalityMin' must be a number between 0 and " + Integer.MAX_VALUE));
            }
        } else {
            if (value != null) {
                try {
                    cardinalityMin = (Integer) value;
                } catch (ClassCastException e) {
                    throw new ValidatorException(
                            new FacesMessage(SEVERITY_ERROR, "Error",
                                    "'CardinalityMin' must be a number between 0 and " + Integer.MAX_VALUE));
                }
            }
        }

        if (cardinalityMin < 0) {
            throw new ValidatorException(
                    new FacesMessage(SEVERITY_ERROR, "Error",
                            "'CardinalityMin' must be a number between 0 and " + Integer.MAX_VALUE));
        }
    }

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        value = value.trim();
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value instanceof String) {
            value = ((String) value).trim();
            if (StringUtils.isEmpty(value)) {
                return null;
            }

            return (String) value;
        } else if (value instanceof Integer) {
            return Integer.toString((Integer) value);
        }

        return value.toString();
    }
}
