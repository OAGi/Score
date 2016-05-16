package org.oagi.srt.repository.entity;

import java.util.Date;

public class ContextScheme {

    private int classificationCtxSchemeId;
    private String guid;
    private String schemeId;
    private String schemeName;
    private String description;
    private String schemeAgencyId;
    private String schemeVersionId;
    private int ctxCategoryId;
    private int createdBy;
    private int lastUpdatedBy;
    private Date creationTimestamp;
    private Date lastUpdateTimestamp;

    public int getClassificationCtxSchemeId() {
        return classificationCtxSchemeId;
    }

    public void setClassificationCtxSchemeId(int classificationCtxSchemeId) {
        this.classificationCtxSchemeId = classificationCtxSchemeId;
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

    public int getCtxCategoryId() {
        return ctxCategoryId;
    }

    public void setCtxCategoryId(int ctxCategoryId) {
        this.ctxCategoryId = ctxCategoryId;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public int getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(int lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public Date getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(Date creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public Date getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    public void setLastUpdateTimestamp(Date lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }
}
