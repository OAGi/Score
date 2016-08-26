package org.oagi.srt.repository.entity;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "bcc")
public class BasicCoreComponent implements CoreComponent, Serializable {

    public static final String SEQUENCE_NAME = "BCC_ID_SEQ";

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
    private long bccId;

    @Column(nullable = false, length = 41)
    private String guid;

    @Column(nullable = false)
    private int cardinalityMin;

    @Column
    private int cardinalityMax;

    @Column(nullable = false)
    private long fromAccId;

    @Column(nullable = false)
    private long toBccpId;

    @Column
    private int seqKey;

    @Column
    private int entityType;

    @Column(nullable = false)
    private String den;

    @Lob
    @Column(length = 10 * 1024)
    private String definition;

    @Column(nullable = false, updatable = false)
    private long createdBy;

    @Column(nullable = false)
    private long ownerUserId;

    @Column(nullable = false)
    private long lastUpdatedBy;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTimestamp;

    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdateTimestamp;

    @Column(nullable = false)
    private int state;

    @Column(nullable = false)
    private int revisionNum;

    @Column(nullable = false)
    private int revisionTrackingNum;

    @Column
    private Integer revisionAction;

    @Column
    private Long releaseId;

    @Column
    private Long currentBccId;

    @Column(name = "is_deprecated", nullable = false)
    private boolean deprecated;

    @Column(name = "is_nillable", nullable = false)
    private boolean nillable;

    @Column
    private String defaultValue;

    @PrePersist
    public void prePersist() {
        creationTimestamp = new Date();
        lastUpdateTimestamp = new Date();
    }

    @PreUpdate
    public void preUpdate() {
        lastUpdateTimestamp = new Date();
    }

    public long getBccId() {
        return bccId;
    }

    public void setBccId(long bccId) {
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
        if (cardinalityMin < 0) {
            throw new IllegalArgumentException("'cardinalityMin' argument must be 0 or greater: " + cardinalityMin);
        }
        this.cardinalityMin = cardinalityMin;
    }

    public int getCardinalityMax() {
        return cardinalityMax;
    }

    public void setCardinalityMax(int cardinalityMax) {
        if (cardinalityMax < -1) {
            throw new IllegalArgumentException("'cardinalityMax' argument must be -1 or greater: " + cardinalityMax);
        }
        this.cardinalityMax = cardinalityMax;
    }

    public long getFromAccId() {
        return fromAccId;
    }

    public void setFromAccId(long fromAccId) {
        if (fromAccId > 0) {
            this.fromAccId = fromAccId;
        }
    }

    public long getToBccpId() {
        return toBccpId;
    }

    public void setToBccpId(long toBccpId) {
        if (toBccpId > 0) {
            this.toBccpId = toBccpId;
        }
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
        if (!StringUtils.isEmpty(definition)) {
            this.definition = definition;
        }
    }

    public long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(long createdBy) {
        this.createdBy = createdBy;
    }

    public long getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(long ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public long getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(long lastUpdatedBy) {
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
        return (revisionAction == null) ? 0 : revisionAction;
    }

    public void setRevisionAction(int revisionAction) {
        this.revisionAction = revisionAction;
    }

    public long getReleaseId() {
        return (releaseId == null) ? 0L : releaseId;
    }

    public void setReleaseId(long releaseId) {
        this.releaseId = releaseId;
    }

    public long getCurrentBccId() {
        return (currentBccId == null) ? 0L : currentBccId;
    }

    public void setCurrentBccId(long currentBccId) {
        this.currentBccId = currentBccId;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    public boolean isNillable() {
        return nillable;
    }

    public void setNillable(boolean nillable) {
        this.nillable = nillable;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BasicCoreComponent that = (BasicCoreComponent) o;

        if (bccId != that.bccId) return false;
        if (cardinalityMin != that.cardinalityMin) return false;
        if (cardinalityMax != that.cardinalityMax) return false;
        if (fromAccId != that.fromAccId) return false;
        if (toBccpId != that.toBccpId) return false;
        if (seqKey != that.seqKey) return false;
        if (entityType != that.entityType) return false;
        if (createdBy != that.createdBy) return false;
        if (ownerUserId != that.ownerUserId) return false;
        if (lastUpdatedBy != that.lastUpdatedBy) return false;
        if (state != that.state) return false;
        if (revisionNum != that.revisionNum) return false;
        if (revisionTrackingNum != that.revisionTrackingNum) return false;
        if (deprecated != that.deprecated) return false;
        if (nillable != that.nillable) return false;
        if (guid != null ? !guid.equals(that.guid) : that.guid != null) return false;
        if (den != null ? !den.equals(that.den) : that.den != null) return false;
        if (definition != null ? !definition.equals(that.definition) : that.definition != null) return false;
        if (creationTimestamp != null ? !creationTimestamp.equals(that.creationTimestamp) : that.creationTimestamp != null)
            return false;
        if (lastUpdateTimestamp != null ? !lastUpdateTimestamp.equals(that.lastUpdateTimestamp) : that.lastUpdateTimestamp != null)
            return false;
        if (revisionAction != null ? !revisionAction.equals(that.revisionAction) : that.revisionAction != null)
            return false;
        if (releaseId != null ? !releaseId.equals(that.releaseId) : that.releaseId != null) return false;
        if (currentBccId != null ? !currentBccId.equals(that.currentBccId) : that.currentBccId != null) return false;
        return defaultValue != null ? defaultValue.equals(that.defaultValue) : that.defaultValue == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (bccId ^ (bccId >>> 32));
        result = 31 * result + (guid != null ? guid.hashCode() : 0);
        result = 31 * result + cardinalityMin;
        result = 31 * result + cardinalityMax;
        result = 31 * result + (int) (fromAccId ^ (fromAccId >>> 32));
        result = 31 * result + (int) (toBccpId ^ (toBccpId >>> 32));
        result = 31 * result + seqKey;
        result = 31 * result + entityType;
        result = 31 * result + (den != null ? den.hashCode() : 0);
        result = 31 * result + (definition != null ? definition.hashCode() : 0);
        result = 31 * result + (int) (createdBy ^ (createdBy >>> 32));
        result = 31 * result + (int) (ownerUserId ^ (ownerUserId >>> 32));
        result = 31 * result + (int) (lastUpdatedBy ^ (lastUpdatedBy >>> 32));
        result = 31 * result + (creationTimestamp != null ? creationTimestamp.hashCode() : 0);
        result = 31 * result + (lastUpdateTimestamp != null ? lastUpdateTimestamp.hashCode() : 0);
        result = 31 * result + state;
        result = 31 * result + revisionNum;
        result = 31 * result + revisionTrackingNum;
        result = 31 * result + (revisionAction != null ? revisionAction.hashCode() : 0);
        result = 31 * result + (releaseId != null ? releaseId.hashCode() : 0);
        result = 31 * result + (currentBccId != null ? currentBccId.hashCode() : 0);
        result = 31 * result + (deprecated ? 1 : 0);
        result = 31 * result + (nillable ? 1 : 0);
        result = 31 * result + (defaultValue != null ? defaultValue.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "BasicCoreComponent{" +
                "bccId=" + bccId +
                ", guid='" + guid + '\'' +
                ", cardinalityMin=" + cardinalityMin +
                ", cardinalityMax=" + cardinalityMax +
                ", fromAccId=" + fromAccId +
                ", toBccpId=" + toBccpId +
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
                ", nillable=" + nillable +
                ", defaultValue='" + defaultValue + '\'' +
                '}';
    }
}
