package org.oagi.srt.repository.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "asbie")
public class AssociationBusinessInformationEntity implements Serializable, IdEntity, BusinessInformationEntity {

    public static final String SEQUENCE_NAME = "ASBIE_ID_SEQ";

    @Id
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    private long asbieId;

    @Column(nullable = false, length = 41)
    private String guid;

    @Column(nullable = false)
    private long fromAbieId;

    @Column(nullable = false)
    private long toAsbiepId;

    @Column(nullable = false)
    private long basedAsccId;

    @Lob
    @Column(length = 10 * 1024)
    private String definition;

    @Column(nullable = false)
    private int cardinalityMin;

    @Column(nullable = false)
    private int cardinalityMax;

    @Column(name = "is_nillable", nullable = false)
    private boolean nillable;

    @Column(length = 225)
    private String remark;

    @Column(nullable = false, updatable = false)
    private long createdBy;

    @Column(nullable = false)
    private long lastUpdatedBy;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTimestamp;

    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdateTimestamp;

    @Column(nullable = false)
    private double seqKey;

    @Column(name = "is_used", nullable = false)
    private boolean used;

    @Column(nullable = false)
    private long ownerTopLevelAbieId;

    @PrePersist
    public void prePersist() {
        creationTimestamp = new Date();
        lastUpdateTimestamp = new Date();
    }

    @PreUpdate
    public void preUpdate() {
        lastUpdateTimestamp = new Date();
    }

    @Override
    public long getId() {
        return getAsbieId();
    }

    @Override
    public void setId(long id) {
        setAsbieId(id);
    }

    public long getAsbieId() {
        return asbieId;
    }

    public void setAsbieId(long asbieId) {
        this.asbieId = asbieId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public long getFromAbieId() {
        return fromAbieId;
    }

    public void setFromAbieId(long fromAbieId) {
        this.fromAbieId = fromAbieId;
    }

    public long getToAsbiepId() {
        return toAsbiepId;
    }

    public void setToAsbiepId(long toAsbiepId) {
        this.toAsbiepId = toAsbiepId;
    }

    public long getBasedAsccId() {
        return basedAsccId;
    }

    public void setBasedAsccId(long basedAsccId) {
        this.basedAsccId = basedAsccId;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
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

    public boolean isNillable() {
        return nillable;
    }

    public void setNillable(boolean nillable) {
        this.nillable = nillable;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(long createdBy) {
        this.createdBy = createdBy;
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

    public double getSeqKey() {
        return seqKey;
    }

    public void setSeqKey(double seqKey) {
        this.seqKey = seqKey;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public long getOwnerTopLevelAbieId() {
        return ownerTopLevelAbieId;
    }

    public void setOwnerTopLevelAbieId(long ownerTopLevelAbieId) {
        this.ownerTopLevelAbieId = ownerTopLevelAbieId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AssociationBusinessInformationEntity that = (AssociationBusinessInformationEntity) o;

        if (asbieId != 0L && asbieId == that.asbieId) return true;
        if (guid != null) {
            if (guid.equals(that.guid)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (int) (asbieId ^ (asbieId >>> 32));
        result = 31 * result + (guid != null ? guid.hashCode() : 0);
        result = 31 * result + (int) (fromAbieId ^ (fromAbieId >>> 32));
        result = 31 * result + (int) (toAsbiepId ^ (toAsbiepId >>> 32));
        result = 31 * result + (int) (basedAsccId ^ (basedAsccId >>> 32));
        result = 31 * result + (definition != null ? definition.hashCode() : 0);
        result = 31 * result + cardinalityMin;
        result = 31 * result + cardinalityMax;
        result = 31 * result + (nillable ? 1 : 0);
        result = 31 * result + (remark != null ? remark.hashCode() : 0);
        result = 31 * result + (int) (createdBy ^ (createdBy >>> 32));
        result = 31 * result + (int) (lastUpdatedBy ^ (lastUpdatedBy >>> 32));
        result = 31 * result + (creationTimestamp != null ? creationTimestamp.hashCode() : 0);
        result = 31 * result + (lastUpdateTimestamp != null ? lastUpdateTimestamp.hashCode() : 0);
        temp = Double.doubleToLongBits(seqKey);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (used ? 1 : 0);
        result = 31 * result + (int) (ownerTopLevelAbieId ^ (ownerTopLevelAbieId >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "AssociationBusinessInformationEntity{" +
                "asbieId=" + asbieId +
                ", guid='" + guid + '\'' +
                ", fromAbieId=" + fromAbieId +
                ", toAsbiepId=" + toAsbiepId +
                ", basedAsccId=" + basedAsccId +
                ", definition='" + definition + '\'' +
                ", cardinalityMin=" + cardinalityMin +
                ", cardinalityMax=" + cardinalityMax +
                ", nillable=" + nillable +
                ", remark='" + remark + '\'' +
                ", createdBy=" + createdBy +
                ", lastUpdatedBy=" + lastUpdatedBy +
                ", creationTimestamp=" + creationTimestamp +
                ", lastUpdateTimestamp=" + lastUpdateTimestamp +
                ", seqKey=" + seqKey +
                ", used=" + used +
                ", ownerTopLevelAbieId=" + ownerTopLevelAbieId +
                '}';
    }
}
