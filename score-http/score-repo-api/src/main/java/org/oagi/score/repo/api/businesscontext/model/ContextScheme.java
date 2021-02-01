package org.oagi.score.repo.api.businesscontext.model;

import org.oagi.score.repo.api.base.Auditable;

import java.math.BigInteger;
import java.util.Collection;

public class ContextScheme extends Auditable {

    private BigInteger contextSchemeId;

    private BigInteger contextCategoryId;

    private String contextCategoryName;

    private BigInteger codeListId;

    private String codeListName;

    private String guid;

    private String schemeId;

    private String schemeName;

    private String description;

    private String schemeAgencyId;

    private String schemeVersionId;

    private boolean imported;

    private boolean used;

    private Collection<ContextSchemeValue> contextSchemeValueList;

    public BigInteger getContextSchemeId() {
        return contextSchemeId;
    }

    public void setContextSchemeId(BigInteger contextSchemeId) {
        this.contextSchemeId = contextSchemeId;
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

    public BigInteger getCodeListId() {
        return codeListId;
    }

    public void setCodeListId(BigInteger codeListId) {
        this.codeListId = codeListId;
    }

    public String getCodeListName() {
        return codeListName;
    }

    public void setCodeListName(String codeListName) {
        this.codeListName = codeListName;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getSchemeId() {
        return schemeId;
    }

    public void setSchemeId(String schemeId) {
        this.schemeId = schemeId;
    }

    public String getSchemeName() {
        return schemeName;
    }

    public void setSchemeName(String schemeName) {
        this.schemeName = schemeName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSchemeAgencyId() {
        return schemeAgencyId;
    }

    public void setSchemeAgencyId(String schemeAgencyId) {
        this.schemeAgencyId = schemeAgencyId;
    }

    public String getSchemeVersionId() {
        return schemeVersionId;
    }

    public void setSchemeVersionId(String schemeVersionId) {
        this.schemeVersionId = schemeVersionId;
    }

    public boolean isImported() {
        return imported;
    }

    public void setImported(boolean imported) {
        this.imported = imported;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public Collection<ContextSchemeValue> getContextSchemeValueList() {
        return contextSchemeValueList;
    }

    public void setContextSchemeValueList(Collection<ContextSchemeValue> contextSchemeValueList) {
        this.contextSchemeValueList = contextSchemeValueList;
    }
}
