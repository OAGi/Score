package org.oagi.score.gateway.http.api.cc_management.model;


import static org.springframework.util.StringUtils.hasLength;

public record ValueConstraint(String defaultValue, String fixedValue) {

    public ValueConstraint(String defaultValue, String fixedValue) {
        this.defaultValue = hasLength(fixedValue) ? null : defaultValue;
        this.fixedValue = fixedValue;
    }

    public boolean hasFixedValue() {
        return hasLength(fixedValue());
    }

    public boolean hasDefaultValue() {
        return hasLength(defaultValue());
    }

}
