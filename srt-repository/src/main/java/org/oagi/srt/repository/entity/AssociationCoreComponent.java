package org.oagi.srt.repository.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "ascc")
public class AssociationCoreComponent extends CoreComponent implements Serializable {

    public static final String SEQUENCE_NAME = "ASCC_ID_SEQ";

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
    private int asccId;

    @Column(nullable = false)
    private String guid;

    @Column(nullable = false)
    private int cardinalityMin;

    @Column(nullable = false)
    private int cardinalityMax;

    @Column(nullable = false)
    private int seqKey;

    @Column(nullable = false)
    private int fromAccId;

    @Column(nullable = false)
    private int toAsccpId;

    @Column(nullable = false)
    private String den;

    @Lob
    @Column
    private String definition;

    @Column(name = "is_deprecated", nullable = false)
    private boolean deprecated;

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
    private int state;

    @Column(nullable = false)
    private int revisionNum;

    @Column(nullable = false)
    private int revisionTrackingNum;

    @Column
    private Integer revisionAction;

    @Column
    private Integer releaseId;

    @Column
    private Integer currentAsccId;

    @PrePersist
    public void prePersist() {
        creationTimestamp = new Date();
        lastUpdateTimestamp = new Date();
    }

    @PreUpdate
    public void preUpdate() {
        lastUpdateTimestamp = new Date();
    }

    public int getAsccId() {
        return asccId;
    }

    public void setAsccId(int asccId) {
        this.asccId = asccId;
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

    public int getSeqKey() {
        return seqKey;
    }

    public void setSeqKey(int seqKey) {
        this.seqKey = seqKey;
    }

    public int getFromAccId() {
        return fromAccId;
    }

    public void setFromAccId(int fromAccId) {
        if (fromAccId > 0) {
            this.fromAccId = fromAccId;
        }
    }

    public int getToAsccpId() {
        return toAsccpId;
    }

    public void setToAsccpId(int toAsccpId) {
        if (toAsccpId > 0) {
            this.toAsccpId = toAsccpId;
        }
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

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
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

    public int getCurrentAsccId() {
        return (currentAsccId == null) ? 0 : currentAsccId;
    }

    public void setCurrentAsccId(int currentAsccId) {
        this.currentAsccId = currentAsccId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AssociationCoreComponent that = (AssociationCoreComponent) o;

        if (asccId != that.asccId) return false;
        if (cardinalityMin != that.cardinalityMin) return false;
        if (cardinalityMax != that.cardinalityMax) return false;
        if (seqKey != that.seqKey) return false;
        if (fromAccId != that.fromAccId) return false;
        if (toAsccpId != that.toAsccpId) return false;
        if (deprecated != that.deprecated) return false;
        if (createdBy != that.createdBy) return false;
        if (ownerUserId != that.ownerUserId) return false;
        if (lastUpdatedBy != that.lastUpdatedBy) return false;
        if (state != that.state) return false;
        if (revisionNum != that.revisionNum) return false;
        if (revisionTrackingNum != that.revisionTrackingNum) return false;
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
        return currentAsccId != null ? currentAsccId.equals(that.currentAsccId) : that.currentAsccId == null;

    }

    @Override
    public int hashCode() {
        int result = asccId;
        result = 31 * result + (guid != null ? guid.hashCode() : 0);
        result = 31 * result + cardinalityMin;
        result = 31 * result + cardinalityMax;
        result = 31 * result + seqKey;
        result = 31 * result + fromAccId;
        result = 31 * result + toAsccpId;
        result = 31 * result + (den != null ? den.hashCode() : 0);
        result = 31 * result + (definition != null ? definition.hashCode() : 0);
        result = 31 * result + (deprecated ? 1 : 0);
        result = 31 * result + createdBy;
        result = 31 * result + ownerUserId;
        result = 31 * result + lastUpdatedBy;
        result = 31 * result + (creationTimestamp != null ? creationTimestamp.hashCode() : 0);
        result = 31 * result + (lastUpdateTimestamp != null ? lastUpdateTimestamp.hashCode() : 0);
        result = 31 * result + state;
        result = 31 * result + revisionNum;
        result = 31 * result + revisionTrackingNum;
        result = 31 * result + (revisionAction != null ? revisionAction.hashCode() : 0);
        result = 31 * result + (releaseId != null ? releaseId.hashCode() : 0);
        result = 31 * result + (currentAsccId != null ? currentAsccId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AssociationCoreComponent{" +
                "asccId=" + asccId +
                ", guid='" + guid + '\'' +
                ", cardinalityMin=" + cardinalityMin +
                ", cardinalityMax=" + cardinalityMax +
                ", seqKey=" + seqKey +
                ", fromAccId=" + fromAccId +
                ", toAsccpId=" + toAsccpId +
                ", den='" + den + '\'' +
                ", definition='" + definition + '\'' +
                ", deprecated=" + deprecated +
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
                ", currentAsccId=" + currentAsccId +
                '}';
    }
}
