package org.oagi.srt.repository.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "classification_ctx_scheme")
public class ContextScheme implements Serializable {

    public static final String SEQUENCE_NAME = "CTX_SCHEME_ID_SEQ";

    @Id
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.SEQUENCE)
    @GenericGenerator(
            name = SEQUENCE_NAME,
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = SEQUENCE_NAME),
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled-lo"),
                    @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
            }
    )
    private int classificationCtxSchemeId;

    @Column(nullable = false, length = 41)
    private String guid;

    @Column(nullable = false, length = 45)
    private String schemeId;

    @Column
    private String schemeName;

    @Lob
    @Column(length = 10 * 1024)
    private String description;

    @Column(nullable = false, length = 45)
    private String schemeAgencyId;

    @Column(nullable = false, length = 45)
    private String schemeVersionId;

    @Column(nullable = false)
    private int ctxCategoryId;

    @Column(nullable = false, updatable = false)
    private int createdBy;

    @Column(nullable = false)
    private int lastUpdatedBy;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTimestamp;

    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdateTimestamp;

    @PrePersist
    public void prePersist() {
        creationTimestamp = new Date();
        lastUpdateTimestamp = new Date();
    }

    @PreUpdate
    public void preUpdate() {
        lastUpdateTimestamp = new Date();
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContextScheme that = (ContextScheme) o;

        if (classificationCtxSchemeId != that.classificationCtxSchemeId) return false;
        if (ctxCategoryId != that.ctxCategoryId) return false;
        if (createdBy != that.createdBy) return false;
        if (lastUpdatedBy != that.lastUpdatedBy) return false;
        if (guid != null ? !guid.equals(that.guid) : that.guid != null) return false;
        if (schemeId != null ? !schemeId.equals(that.schemeId) : that.schemeId != null) return false;
        if (schemeName != null ? !schemeName.equals(that.schemeName) : that.schemeName != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (schemeAgencyId != null ? !schemeAgencyId.equals(that.schemeAgencyId) : that.schemeAgencyId != null)
            return false;
        if (schemeVersionId != null ? !schemeVersionId.equals(that.schemeVersionId) : that.schemeVersionId != null)
            return false;
        if (creationTimestamp != null ? !creationTimestamp.equals(that.creationTimestamp) : that.creationTimestamp != null)
            return false;
        return lastUpdateTimestamp != null ? lastUpdateTimestamp.equals(that.lastUpdateTimestamp) : that.lastUpdateTimestamp == null;

    }

    @Override
    public int hashCode() {
        int result = classificationCtxSchemeId;
        result = 31 * result + (guid != null ? guid.hashCode() : 0);
        result = 31 * result + (schemeId != null ? schemeId.hashCode() : 0);
        result = 31 * result + (schemeName != null ? schemeName.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (schemeAgencyId != null ? schemeAgencyId.hashCode() : 0);
        result = 31 * result + (schemeVersionId != null ? schemeVersionId.hashCode() : 0);
        result = 31 * result + ctxCategoryId;
        result = 31 * result + createdBy;
        result = 31 * result + lastUpdatedBy;
        result = 31 * result + (creationTimestamp != null ? creationTimestamp.hashCode() : 0);
        result = 31 * result + (lastUpdateTimestamp != null ? lastUpdateTimestamp.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ContextScheme{" +
                "classificationCtxSchemeId=" + classificationCtxSchemeId +
                ", guid='" + guid + '\'' +
                ", schemeId='" + schemeId + '\'' +
                ", schemeName='" + schemeName + '\'' +
                ", description='" + description + '\'' +
                ", schemeAgencyId='" + schemeAgencyId + '\'' +
                ", schemeVersionId='" + schemeVersionId + '\'' +
                ", ctxCategoryId=" + ctxCategoryId +
                ", createdBy=" + createdBy +
                ", lastUpdatedBy=" + lastUpdatedBy +
                ", creationTimestamp=" + creationTimestamp +
                ", lastUpdateTimestamp=" + lastUpdateTimestamp +
                '}';
    }
}
