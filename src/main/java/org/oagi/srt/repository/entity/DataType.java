package org.oagi.srt.repository.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "dt")
public class DataType implements Serializable {

    @Id
    @GeneratedValue(generator = "DT_ID_SEQ", strategy = GenerationType.AUTO)
    @SequenceGenerator(name = "DT_ID_SEQ", sequenceName = "DT_ID_SEQ", allocationSize = 1)
    private int dtId;

    @Column(nullable = false)
    private String guid;

    @Column(nullable = false)
    private int type;

    @Column(nullable = false)
    private String versionNum;

    @Column
    private Integer previousVersionDtId;

    @Column
    private String dataTypeTerm;

    @Column
    private String qualifier;

    @Column
    private Integer basedDtId;

    @Column(nullable = false)
    private String den;

    @Column
    private String contentComponentDen;

    @Column
    private String definition;

    @Column
    private String contentComponentDefinition;

    @Column
    private String revisionDoc;

    @Column
    private String module;

    @Column
    private int state;

    @Column(nullable = false, updatable = false)
    private int createdBy;

    @Column(nullable = false)
    private int ownerUserId;

    @Column(nullable = false)
    private int lastUpdatedBy;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTimestamp;

    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdateTimestamp;

    @Column(nullable = false)
    private int revisionNum;

    @Column(nullable = false)
    private int revisionTrackingNum;

    @Column
    private Integer revisionAction;

    @Column
    private Integer releaseId;

    @Column
    private Integer currentBdtId;

    @Column(name = "is_deprecated", nullable = false)
    private boolean deprecated;

    @PrePersist
    public void prePersist() {
        creationTimestamp = new Date();
        lastUpdateTimestamp = new Date();
    }

    @PreUpdate
    public void preUpdate() {
        lastUpdateTimestamp = new Date();
    }

    public int getDtId() {
        return dtId;
    }

    public void setDtId(int dtId) {
        this.dtId = dtId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getVersionNum() {
        return versionNum;
    }

    public void setVersionNum(String versionNum) {
        this.versionNum = versionNum;
    }

    public int getPreviousVersionDtId() {
        return (previousVersionDtId == null) ? 0 : previousVersionDtId;
    }

    public void setPreviousVersionDtId(int previousVersionDtId) {
        this.previousVersionDtId = previousVersionDtId;
    }

    public String getDataTypeTerm() {
        return dataTypeTerm;
    }

    public void setDataTypeTerm(String dataTypeTerm) {
        this.dataTypeTerm = dataTypeTerm;
    }

    public String getQualifier() {
        return qualifier;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    public int getBasedDtId() {
        return (basedDtId == null) ? 0 : basedDtId;
    }

    public void setBasedDtId(int basedDtId) {
        this.basedDtId = basedDtId;
    }

    public String getDen() {
        return den;
    }

    public void setDen(String den) {
        this.den = den;
    }

    public String getContentComponentDen() {
        return contentComponentDen;
    }

    public void setContentComponentDen(String contentComponentDen) {
        this.contentComponentDen = contentComponentDen;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getContentComponentDefinition() {
        return contentComponentDefinition;
    }

    public void setContentComponentDefinition(String contentComponentDefinition) {
        this.contentComponentDefinition = contentComponentDefinition;
    }

    public String getRevisionDoc() {
        return revisionDoc;
    }

    public void setRevisionDoc(String revisionDoc) {
        this.revisionDoc = revisionDoc;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public int getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(int ownerUserId) {
        this.ownerUserId = ownerUserId;
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

    public int getRevisionNum() {
        return revisionNum;
    }

    public void setRevisionNum(int revisionNum) {
        this.revisionNum = revisionNum;
    }

    public int getRevisionTrackingNum() {
        return revisionTrackingNum;
    }

    public void setRevisionTrackingNum(int revisionTrackingNum) {
        this.revisionTrackingNum = revisionTrackingNum;
    }

    public int getRevisionAction() {
        return (revisionAction == null) ? 0 : revisionAction;
    }

    public void setRevisionAction(int revisionAction) {
        this.revisionAction = revisionAction;
    }

    public int getReleaseId() {
        return (releaseId == null) ? 0 : releaseId;
    }

    public void setReleaseId(int releaseId) {
        this.releaseId = releaseId;
    }

    public int getCurrentBdtId() {
        return (currentBdtId == null) ? 0 : currentBdtId;
    }

    public void setCurrentBdtId(int currentBdtId) {
        this.currentBdtId = currentBdtId;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    @Override
    public String toString() {
        return "DataType{" +
                "dtId=" + dtId +
                ", guid='" + guid + '\'' +
                ", type=" + type +
                ", versionNum='" + versionNum + '\'' +
                ", previousVersionDtId=" + previousVersionDtId +
                ", dataTypeTerm='" + dataTypeTerm + '\'' +
                ", qualifier='" + qualifier + '\'' +
                ", basedDtId=" + basedDtId +
                ", den='" + den + '\'' +
                ", contentComponentDen='" + contentComponentDen + '\'' +
                ", definition='" + definition + '\'' +
                ", contentComponentDefinition='" + contentComponentDefinition + '\'' +
                ", revisionDoc='" + revisionDoc + '\'' +
                ", state=" + state +
                ", createdBy=" + createdBy +
                ", ownerUserId=" + ownerUserId +
                ", lastUpdatedBy=" + lastUpdatedBy +
                ", creationTimestamp=" + creationTimestamp +
                ", lastUpdateTimestamp=" + lastUpdateTimestamp +
                ", revisionNum=" + revisionNum +
                ", revisionTrackingNum=" + revisionTrackingNum +
                ", revisionAction=" + revisionAction +
                ", releaseId=" + releaseId +
                ", currentBdtId=" + currentBdtId +
                ", deprecated=" + deprecated +
                '}';
    }
}
