package org.oagi.score.repo.api.businesscontext.model;

import java.io.Serializable;
import java.math.BigInteger;

public class BusinessContextValue implements Serializable {

    private BigInteger businessContextValueId;
    private BigInteger businessContextId;

    private BigInteger contextCategoryId;
    private String contextCategoryName;

    private BigInteger contextSchemeId;
    private String contextSchemeName;

    private BigInteger contextSchemeValueId;
    private String contextSchemeValue;
    private String contextSchemeValueMeaning;

    public BigInteger getBusinessContextValueId() {
        return businessContextValueId;
    }

    public void setBusinessContextValueId(BigInteger businessContextValueId) {
        this.businessContextValueId = businessContextValueId;
    }

    public BigInteger getBusinessContextId() {
        return businessContextId;
    }

    public void setBusinessContextId(BigInteger businessContextId) {
        this.businessContextId = businessContextId;
    }

    public BigInteger getContextCategoryId() {
        return contextCategoryId;
    }

    public void setContextCategoryId(BigInteger contextCategoryId) {
        this.contextCategoryId = contextCategoryId;
    }

    public String getContextCategoryName() {
        return contextCategoryName;
    }

    public void setContextCategoryName(String contextCategoryName) {
        this.contextCategoryName = contextCategoryName;
    }

    public BigInteger getContextSchemeId() {
        return contextSchemeId;
    }

    public void setContextSchemeId(BigInteger contextSchemeId) {
        this.contextSchemeId = contextSchemeId;
    }

    public String getContextSchemeName() {
        return contextSchemeName;
    }

    public void setContextSchemeName(String contextSchemeName) {
        this.contextSchemeName = contextSchemeName;
    }

    public BigInteger getContextSchemeValueId() {
        return contextSchemeValueId;
    }

    public void setContextSchemeValueId(BigInteger contextSchemeValueId) {
        this.contextSchemeValueId = contextSchemeValueId;
    }

    public String getContextSchemeValue() {
        return contextSchemeValue;
    }

    public void setContextSchemeValue(String contextSchemeValue) {
        this.contextSchemeValue = contextSchemeValue;
    }

    public String getContextSchemeValueMeaning() {
        return contextSchemeValueMeaning;
    }

    public void setContextSchemeValueMeaning(String contextSchemeValueMeaning) {
        this.contextSchemeValueMeaning = contextSchemeValueMeaning;
    }
}
