package org.oagi.score.repo.api.businesscontext.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class CreateContextSchemeRequest extends Request {

    private String schemeId;

    private String schemeName;

    private String description;

    private String schemeAgencyId;

    private String schemeVersionId;

    private BigInteger contextCategoryId;

    private Collection<ContextSchemeValue> contextSchemeValueList;

    public CreateContextSchemeRequest(ScoreUser requester) {
        super(requester);
    }

    public String getSchemeId() {
        return schemeId;
    }

    public void setSchemeId(String schemeId) {
        this.schemeId = schemeId;
    }

    public CreateContextSchemeRequest withSchemeId(String schemeId) {
        this.setSchemeId(schemeId);
        return this;
    }

    public String getSchemeName() {
        return schemeName;
    }

    public void setSchemeName(String schemeName) {
        this.schemeName = schemeName;
    }

    public CreateContextSchemeRequest withSchemeName(String schemeName) {
        this.setSchemeName(schemeName);
        return this;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CreateContextSchemeRequest withDescription(String description) {
        this.setDescription(description);
        return this;
    }

    public String getSchemeAgencyId() {
        return schemeAgencyId;
    }

    public void setSchemeAgencyId(String schemeAgencyId) {
        this.schemeAgencyId = schemeAgencyId;
    }

    public CreateContextSchemeRequest withSchemeAgencyId(String schemeAgencyId) {
        this.setSchemeAgencyId(schemeAgencyId);
        return this;
    }

    public String getSchemeVersionId() {
        return schemeVersionId;
    }

    public void setSchemeVersionId(String schemeVersionId) {
        this.schemeVersionId = schemeVersionId;
    }

    public CreateContextSchemeRequest withSchemeVersionId(String schemeVersionId) {
        this.setSchemeVersionId(schemeVersionId);
        return this;
    }

    public BigInteger getContextCategoryId() {
        return contextCategoryId;
    }

    public void setContextCategoryId(BigInteger contextCategoryId) {
        this.contextCategoryId = contextCategoryId;
    }

    public CreateContextSchemeRequest withContextCategoryId(BigInteger contextCategoryId) {
        this.setContextCategoryId(contextCategoryId);
        return this;
    }

    public Collection<ContextSchemeValue> getContextSchemeValueList() {
        return (contextSchemeValueList == null) ? Collections.emptyList() : contextSchemeValueList;
    }

    public void setContextSchemeValueList(Collection<ContextSchemeValue> contextSchemeValueList) {
        this.contextSchemeValueList = contextSchemeValueList;
    }

    public CreateContextSchemeRequest withContextSchemeValueList(Collection<ContextSchemeValue> contextSchemeValueList) {
        this.setContextSchemeValueList(contextSchemeValueList);
        return this;
    }

    public void addContextSchemeValue(String value, String meaning) {
        if (value != null) {
            if (this.contextSchemeValueList == null) {
                this.contextSchemeValueList = new ArrayList();
            }

            ContextSchemeValue contextSchemeValue = new ContextSchemeValue();
            contextSchemeValue.setValue(value);
            contextSchemeValue.setMeaning(meaning);
            this.contextSchemeValueList.add(contextSchemeValue);
        }
    }

    public CreateContextSchemeRequest withContextSchemeValue(String value, String meaning) {
        this.addContextSchemeValue(value, meaning);
        return this;
    }
}
