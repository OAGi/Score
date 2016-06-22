package org.oagi.srt.repository.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "bbie")
public class BasicBusinessInformationEntity implements Serializable, BusinessInformationEntity, IdEntity, IGuidEntity {

    public static final String SEQUENCE_NAME = "BBIE_ID_SEQ";

    @Id
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.SEQUENCE)
    @GenericGenerator(
            name = SEQUENCE_NAME,
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = SEQUENCE_NAME),
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled-lo"),
                    @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "2000"),
            }
    )
    private int bbieId;

    @Column(nullable = false, length = 41)
    private String guid;

    @Column(nullable = false)
    private int basedBccId;

    @Column(nullable = false)
    private int fromAbieId;

    @Column(nullable = false)
    private int toBbiepId;

    @Column
    private Integer bdtPriRestriId;

    @Column
    private Integer codeListId;

    @Column(nullable = false)
    private int cardinalityMin;

    @Column
    private int cardinalityMax;

    @Column
    private String defaultValue;

    @Column(name = "is_nillable", nullable = false)
    private boolean nillable;

    @Column
    private String fixedValue;

    @Column(name = "is_null", nullable = false)
    private boolean nill;

    @Lob
    @Column(length = 10 * 1024)
    private String definition;

    @Column(length = 225)
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

    @Column(nullable = false)
    private int bodId;

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
    public int getId() {
        return getBbieId();
    }

    @Override
    public void setId(int id) {
        setBbieId(id);
    }

    public int getBbieId() {
        return bbieId;
    }

    public void setBbieId(int bbieId) {
        this.bbieId = bbieId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public int getBasedBccId() {
        return basedBccId;
    }

    public void setBasedBccId(int basedBccId) {
        this.basedBccId = basedBccId;
    }

    public int getFromAbieId() {
        return fromAbieId;
    }

    public void setFromAbieId(int fromAbieId) {
        this.fromAbieId = fromAbieId;
    }

    public int getToBbiepId() {
        return toBbiepId;
    }

    public void setToBbiepId(int toBbiepId) {
        this.toBbiepId = toBbiepId;
    }

    public int getBdtPriRestriId() {
        return (bdtPriRestriId == null) ? 0 : bdtPriRestriId;
    }

    public void setBdtPriRestriId(Integer bdtPriRestriId) {
        this.bdtPriRestriId = bdtPriRestriId;
    }

    public int getCodeListId() {
        return (codeListId == null) ? 0 : codeListId;
    }

    public void setCodeListId(Integer codeListId) {
        this.codeListId = codeListId;
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

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isNillable() {
        return nillable;
    }

    public void setNillable(boolean nillable) {
        this.nillable = nillable;
    }

    public String getFixedValue() {
        return fixedValue;
    }

    public void setFixedValue(String fixedValue) {
        this.fixedValue = fixedValue;
    }

    public boolean isNill() {
        return nill;
    }

    public void setNill(boolean nill) {
        this.nill = nill;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
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

    public int getBodId() {
        return bodId;
    }

    public void setBodId(int bodId) {
        this.bodId = bodId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BasicBusinessInformationEntity that = (BasicBusinessInformationEntity) o;

        if (bbieId != that.bbieId) return false;
        if (basedBccId != that.basedBccId) return false;
        if (fromAbieId != that.fromAbieId) return false;
        if (toBbiepId != that.toBbiepId) return false;
        if (cardinalityMin != that.cardinalityMin) return false;
        if (cardinalityMax != that.cardinalityMax) return false;
        if (nillable != that.nillable) return false;
        if (nill != that.nill) return false;
        if (createdBy != that.createdBy) return false;
        if (lastUpdatedBy != that.lastUpdatedBy) return false;
        if (seqKey != that.seqKey) return false;
        if (used != that.used) return false;
        if (bodId != that.bodId) return false;
        if (guid != null ? !guid.equals(that.guid) : that.guid != null) return false;
        if (bdtPriRestriId != null ? !bdtPriRestriId.equals(that.bdtPriRestriId) : that.bdtPriRestriId != null)
            return false;
        if (codeListId != null ? !codeListId.equals(that.codeListId) : that.codeListId != null) return false;
        if (defaultValue != null ? !defaultValue.equals(that.defaultValue) : that.defaultValue != null) return false;
        if (fixedValue != null ? !fixedValue.equals(that.fixedValue) : that.fixedValue != null) return false;
        if (definition != null ? !definition.equals(that.definition) : that.definition != null) return false;
        if (remark != null ? !remark.equals(that.remark) : that.remark != null) return false;
        if (creationTimestamp != null ? !creationTimestamp.equals(that.creationTimestamp) : that.creationTimestamp != null)
            return false;
        return lastUpdateTimestamp != null ? lastUpdateTimestamp.equals(that.lastUpdateTimestamp) : that.lastUpdateTimestamp == null;

    }

    @Override
    public int hashCode() {
        int result = bbieId;
        result = 31 * result + (guid != null ? guid.hashCode() : 0);
        result = 31 * result + basedBccId;
        result = 31 * result + fromAbieId;
        result = 31 * result + toBbiepId;
        result = 31 * result + (bdtPriRestriId != null ? bdtPriRestriId.hashCode() : 0);
        result = 31 * result + (codeListId != null ? codeListId.hashCode() : 0);
        result = 31 * result + cardinalityMin;
        result = 31 * result + cardinalityMax;
        result = 31 * result + (defaultValue != null ? defaultValue.hashCode() : 0);
        result = 31 * result + (nillable ? 1 : 0);
        result = 31 * result + (fixedValue != null ? fixedValue.hashCode() : 0);
        result = 31 * result + (nill ? 1 : 0);
        result = 31 * result + (definition != null ? definition.hashCode() : 0);
        result = 31 * result + (remark != null ? remark.hashCode() : 0);
        result = 31 * result + createdBy;
        result = 31 * result + lastUpdatedBy;
        result = 31 * result + (creationTimestamp != null ? creationTimestamp.hashCode() : 0);
        result = 31 * result + (lastUpdateTimestamp != null ? lastUpdateTimestamp.hashCode() : 0);
        result = 31 * result + seqKey;
        result = 31 * result + (used ? 1 : 0);
        result = 31 * result + bodId;
        return result;
    }

    @Override
    public String toString() {
        return "BasicBusinessInformationEntity{" +
                "bbieId=" + bbieId +
                ", guid='" + guid + '\'' +
                ", basedBccId=" + basedBccId +
                ", fromAbieId=" + fromAbieId +
                ", toBbiepId=" + toBbiepId +
                ", bdtPriRestriId=" + bdtPriRestriId +
                ", codeListId=" + codeListId +
                ", cardinalityMin=" + cardinalityMin +
                ", cardinalityMax=" + cardinalityMax +
                ", defaultValue='" + defaultValue + '\'' +
                ", nillable=" + nillable +
                ", fixedValue='" + fixedValue + '\'' +
                ", nill=" + nill +
                ", definition='" + definition + '\'' +
                ", remark='" + remark + '\'' +
                ", createdBy=" + createdBy +
                ", lastUpdatedBy=" + lastUpdatedBy +
                ", creationTimestamp=" + creationTimestamp +
                ", lastUpdateTimestamp=" + lastUpdateTimestamp +
                ", seqKey=" + seqKey +
                ", used=" + used +
                ", bodId=" + bodId +
                '}';
    }
}
