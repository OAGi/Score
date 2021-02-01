package org.oagi.score.repo.api.businesscontext.model;

import org.oagi.score.repo.api.base.Response;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class CreateContextSchemeResponse extends Response {

    private final BigInteger contextSchemeId;

    private Collection<ContextSchemeValue> contextSchemeValues;

    public CreateContextSchemeResponse(BigInteger contextSchemeId) {
        this.contextSchemeId = contextSchemeId;
    }

    public BigInteger getContextSchemeId() {
        return contextSchemeId;
    }

    public Collection<ContextSchemeValue> getContextSchemeValues() {
        return (contextSchemeValues == null) ? Collections.emptyList() : contextSchemeValues;
    }

    public void setContextSchemeValues(Collection<ContextSchemeValue> contextSchemeValues) {
        this.contextSchemeValues = contextSchemeValues;
    }

    public void addContextSchemeValue(ContextSchemeValue contextSchemeValue) {
        if (this.contextSchemeValues == null) {
            this.contextSchemeValues = new ArrayList();
        }

        this.contextSchemeValues.add(contextSchemeValue);
    }
}
