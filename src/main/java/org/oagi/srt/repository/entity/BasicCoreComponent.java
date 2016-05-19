package org.oagi.srt.repository.entity;

import java.io.Serializable;
import java.util.Date;

public class BasicCoreComponent extends CoreComponent implements Serializable {

    private int bccId;
    private String guid;
    private int cardinalityMin;
    private int cardinalityMax;
    private int toBccpId;
    private int fromAccId;
    private int seqKey;
    private int entityType;
    private String den;
    private String definition;
    private int createdBy;
    private int ownerUserId;
    private int lastUpdatedBy;
    private Date creationTimestamp;
    private Date lastUpdateTimestamp;
    private int state;
    private int revisionNum;
    private int revisionTrackingNum;
    private int revisionAction;
    private int releaseId;
    private int currentBccId;
    private boolean deprecated;

    public int getBccId() {
        return bccId;
    }

    public void setBccId(int bccId) {
        this.bccId = bccId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public int getCardinalityMin() {
        return cardinalityMin;
    }

    public void setCardinalityMin(int cardinalityMin) {
        this.cardinalityMin = cardinalityMin;
    }

    public int getCardinalityMax() {
        return cardinalityMax;
    }

    public void setCardinalityMax(int cardinalityMax) {
        this.cardinalityMax = cardinalityMax;
    }

    public int getToBccpId() {
        return toBccpId;
    }

    public void setToBccpId(int toBccpId) {
        this.toBccpId = toBccpId;
    }

    public int getFromAccId() {
        return fromAccId;
    }

    public void setFromAccId(int fromAccId) {
        this.fromAccId = fromAccId;
    }

    public int getSeqKey() {
        return seqKey;
    }

    public void setSeqKey(int seqKey) {
        this.seqKey = seqKey;
    }

    public int getEntityType() {
        return entityType;
    }

    public void setEntityType(int entityType) {
        this.entityType = entityType;
    }

    public String getDen() {
        return den;
    }

    public void setDen(String den) {
        this.den = den;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
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

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
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

    public int getCurrentBccId() {
        return currentBccId;
    }

    public void setCurrentBccId(int currentBccId) {
        this.currentBccId = currentBccId;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    @Override
    public String toString() {
        return "BasicCoreComponent{" +
                "bccId=" + bccId +
                ", guid='" + guid + '\'' +
                ", cardinalityMin=" + cardinalityMin +
                ", cardinalityMax=" + cardinalityMax +
                ", toBccpId=" + toBccpId +
                ", fromAccId=" + fromAccId +
                ", seqKey=" + seqKey +
                ", entityType=" + entityType +
                ", den='" + den + '\'' +
                ", definition='" + definition + '\'' +
                ", createdBy=" + createdBy +
                ", ownerUserId=" + ownerUserId +
                ", lastUpdatedBy=" + lastUpdatedBy +
                ", creationTimestamp=" + creationTimestamp +
                ", lastUpdateTimestamp=" + lastUpdateTimestamp +
                ", state=" + state +
                ", revisionNum=" + revisionNum +
                ", revisionTrackingNum=" + revisionTrackingNum +
                ", revisionAction=" + revisionAction +
                ", releaseId=" + releaseId +
                ", currentBccId=" + currentBccId +
                ", deprecated=" + deprecated +
                '}';
    }
}
