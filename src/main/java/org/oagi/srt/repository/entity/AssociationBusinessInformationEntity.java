package org.oagi.srt.repository.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "asbie")
public class AssociationBusinessInformationEntity implements Serializable, BusinessInformationEntity {

    @Id
    @GeneratedValue(generator = "ASBIE_ID_SEQ", strategy = GenerationType.AUTO)
    @SequenceGenerator(name = "ASBIE_ID_SEQ", sequenceName = "ASBIE_ID_SEQ", allocationSize = 1)
    private int asbieId;

    @Column(nullable = false)
    private String guid;

    @Column(nullable = false)
    private int fromAbieId;

    @Column(nullable = false)
    private int toAsbiepId;

    @Column(nullable = false)
    private int basedAscc;

    @Column
    private String definition;

    @Column(nullable = false)
    private int cardinalityMin;

    @Column(nullable = false)
    private int cardinalityMax;

    @Column(nullable = false)
    private boolean nillable;

    @Column
    private String remark;

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

    @Column(nullable = false)
    private int seqKey;

    @Column(name = "is_used", nullable = false)
    private boolean used;

    @PrePersist
    public void prePersist() {
        creationTimestamp = new Date();
        lastUpdateTimestamp = new Date();
    }

    @PreUpdate
    public void preUpdate() {
        lastUpdateTimestamp = new Date();
    }

    public int getAsbieId() {
        return asbieId;
    }

    public void setAsbieId(int asbieId) {
        this.asbieId = asbieId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public int getFromAbieId() {
        return fromAbieId;
    }

    public void setFromAbieId(int fromAbieId) {
        this.fromAbieId = fromAbieId;
    }

    public int getToAsbiepId() {
        return toAsbiepId;
    }

    public void setToAsbiepId(int toAsbiepId) {
        this.toAsbiepId = toAsbiepId;
    }

    public int getBasedAscc() {
        return basedAscc;
    }

    public void setBasedAscc(int basedAscc) {
        this.basedAscc = basedAscc;
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
        this.cardinalityMin = cardinalityMin;
    }

    public int getCardinalityMax() {
        return cardinalityMax;
    }

    public void setCardinalityMax(int cardinalityMax) {
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

    public int getSeqKey() {
        return seqKey;
    }

    public void setSeqKey(int seqKey) {
        this.seqKey = seqKey;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    @Override
    public String toString() {
        return "AssociationBusinessInformationEntity{" +
                "asbieId=" + asbieId +
                ", guid='" + guid + '\'' +
                ", fromAbieId=" + fromAbieId +
                ", toAsbiepId=" + toAsbiepId +
                ", basedAscc=" + basedAscc +
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
                '}';
    }
}
