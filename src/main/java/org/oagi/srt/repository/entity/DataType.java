package org.oagi.srt.repository.entity;

import java.io.Serializable;
import java.util.Date;

public class DataType implements Serializable {

    private int dtId;
    private String guid;
    private int type;
    private String versionNum;
    private int previousVersionDtId;
    private String dataTypeTerm;
    private String qualifier;
    private int basedDtId;
    private String den;
    private String contentComponentDen;
    private String definition;
    private String contentComponentDefinition;
    private String revisionDoc;
    private int state;
    private int createdBy;
    private int ownerUserId;
    private int lastUpdatedBy;
    private Date creationTimestamp;
    private Date lastUpdateTimestamp;
    private int revisionNum;
    private int revisionTrackingNum;
    private int revisionAction;
    private int releaseId;
    private int currentBdtId;
    private boolean deprecated;

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
        return previousVersionDtId;
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
        return basedDtId;
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
        return revisionAction;
    }

    public void setRevisionAction(int revisionAction) {
        this.revisionAction = revisionAction;
    }

    public int getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(int releaseId) {
        this.releaseId = releaseId;
    }

    public int getCurrentBdtId() {
        return currentBdtId;
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
