package org.oagi.srt.repository.entity;

import org.hibernate.annotations.GenericGenerator;
import org.oagi.srt.repository.entity.listener.PersistEventListener;
import org.oagi.srt.repository.entity.listener.TimestampAwareEventListener;
import org.oagi.srt.repository.entity.listener.UpdateEventListener;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

import static org.oagi.srt.repository.entity.BasicBusinessInformationEntityRestrictionType.*;

@Entity
@Table(name = "bbie")
public class BasicBusinessInformationEntity
        implements BusinessInformationEntity, CreatorModifierAware, TimestampAware, Usable, Serializable {

    public static final String SEQUENCE_NAME = "BBIE_ID_SEQ";

    @Id
    @GenericGenerator(
            name = SEQUENCE_NAME,
            strategy = "org.oagi.srt.repository.support.jpa.ByDialectIdentifierGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = SEQUENCE_NAME),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "initial_value", value = "1")
            }
    )
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.AUTO)
    private long bbieId;

    @Column(nullable = false, length = 41, updatable = false)
    private String guid;

    @Column(nullable = false, updatable = false)
    private long basedBccId;

    @Column(nullable = false, updatable = false)
    private long fromAbieId;
    @Transient
    private AggregateBusinessInformationEntity fromAbie;

    @Column(nullable = false, updatable = false)
    private long toBbiepId;
    @Transient
    private BasicBusinessInformationEntityProperty toBbiep;

    @Transient
    private BasicBusinessInformationEntityRestrictionType restrictionType;

    @Column
    private Long bdtPriRestriId;

    @Column
    private Long codeListId;

    @Column
    private Long agencyIdListId;

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

    @Column
    private Long definitionId;
    @Transient
    private Definition definition;

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

    @Column(nullable = false, updatable = false)
    private double seqKey;

    @Column(name = "is_used", nullable = false)
    private boolean used;

    @Column(nullable = false, updatable = false)
    private long ownerTopLevelAbieId;
    @Transient
    private TopLevelAbie ownerTopLevelAbie;

    @Override
    public long getId() {
        return getBbieId();
    }

    @Override
    public void setId(long id) {
        setBbieId(id);
    }

    @Override
    public String tableName() {
        return "BBIE";
    }

    public long getBbieId() {
        return bbieId;
    }

    public void setBbieId(long bbieId) {
        this.bbieId = bbieId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public long getBasedBccId() {
        return basedBccId;
    }

    public void setBasedBccId(long basedBccId) {
        this.basedBccId = basedBccId;
    }

    public long getFromAbieId() {
        return fromAbieId;
    }

    public void setFromAbieId(long fromAbieId) {
        this.fromAbieId = fromAbieId;
    }

    public AggregateBusinessInformationEntity getFromAbie() {
        return fromAbie;
    }

    public void setFromAbie(AggregateBusinessInformationEntity fromAbie) {
        this.fromAbie = fromAbie;
    }

    public long getToBbiepId() {
        return toBbiepId;
    }

    public void setToBbiepId(long toBbiepId) {
        this.toBbiepId = toBbiepId;
    }

    public BasicBusinessInformationEntityProperty getToBbiep() {
        return toBbiep;
    }

    public void setToBbiep(BasicBusinessInformationEntityProperty toBbiep) {
        this.toBbiep = toBbiep;
    }

    public BasicBusinessInformationEntityRestrictionType getRestrictionType() {
        return restrictionType;
    }

    public void setRestrictionType(BasicBusinessInformationEntityRestrictionType restrictionType) {
        this.restrictionType = restrictionType;
    }

    public long getBdtPriRestriId() {
        return (bdtPriRestriId == null) ? 0L : bdtPriRestriId;
    }

    public void setBdtPriRestriId(Long bdtPriRestriId) {
        if (bdtPriRestriId != null && bdtPriRestriId > 0L) {
            this.bdtPriRestriId = bdtPriRestriId;
            if (codeListId == null && agencyIdListId == null) {
                setRestrictionType(Primitive);
            }
        } else {
            this.bdtPriRestriId = null;
        }
    }

    public void setBdtPriRestri(BusinessDataTypePrimitiveRestriction bdtPriRestri) {
        if (bdtPriRestri != null) {
            setBdtPriRestriId(bdtPriRestri.getBdtPriRestriId());
        }
    }

    public long getCodeListId() {
        return (codeListId == null) ? 0L : codeListId;
    }

    public void setCodeListId(Long codeListId) {
        if (codeListId != null && codeListId > 0L) {
            this.codeListId = codeListId;
            if (bdtPriRestriId == null && agencyIdListId == null) {
                setRestrictionType(Code);
            }
        } else {
            this.codeListId = null;
        }
    }

    public void setCodeList(CodeList codeList) {
        if (codeList != null) {
            setCodeListId(codeList.getCodeListId());
        }
    }

    public long getAgencyIdListId() {
        return (agencyIdListId == null) ? 0L : agencyIdListId;
    }

    public void setAgencyIdListId(Long agencyIdListId) {
        if (agencyIdListId != null && agencyIdListId > 0L) {
            this.agencyIdListId = agencyIdListId;
            if (bdtPriRestriId == null && codeListId == null) {
                setRestrictionType(Agency);
            }
        } else {
            this.agencyIdListId = null;
        }
    }

    public void setAgencyIdList(AgencyIdList agencyIdList) {
        if (agencyIdList != null) {
            setAgencyIdListId(agencyIdList.getAgencyIdListId());
        }
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

    public Long getDefinitionId() {
        return definitionId;
    }

    public void setDefinitionId(Long definitionId) {
        this.definitionId = definitionId;
    }

    public String getDefinition() {
        return (this.definition != null) ? this.definition.getDefinition() : null;
    }

    public Definition getRawDefinition() {
        return this.definition;
    }

    public void setRawDefinition(Definition definition) {
        this.definition = definition;
    }

    public void setDefinition(String definition) {
        if (definition != null) {
            definition = definition.trim();
        }
        if (StringUtils.isEmpty(definition)) {
            return;
        }

        if (this.definition == null) {
            this.definition = new Definition();
        }
        this.definition.setDefinition(definition);
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

    public void setOwnerTopLevelAbie(TopLevelAbie ownerTopLevelAbie) {
        this.ownerTopLevelAbie = ownerTopLevelAbie;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BasicBusinessInformationEntity that = (BasicBusinessInformationEntity) o;

        if (bbieId != 0L && bbieId == that.bbieId) return true;
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
        result = (int) (bbieId ^ (bbieId >>> 32));
        result = 31 * result + (guid != null ? guid.hashCode() : 0);
        result = 31 * result + (int) (basedBccId ^ (basedBccId >>> 32));
        result = 31 * result + (int) (fromAbieId ^ (fromAbieId >>> 32));
        result = 31 * result + (int) (toBbiepId ^ (toBbiepId >>> 32));
        if (restrictionType == Primitive) {
            if (bdtPriRestriId != null) {
                result = 31 * result + bdtPriRestriId.hashCode();
            } else {
                if (codeListId != null) {
                    result = 31 * result + codeListId.hashCode();
                } else if (agencyIdListId != null) {
                    result = 31 * result + agencyIdListId.hashCode();
                } else {
                    result = 31 * result + 0;
                }
            }
        }
        if (restrictionType == Code) {
            if (codeListId != null) {
                result = 31 * result + codeListId.hashCode();
            } else {
                if (bdtPriRestriId != null) {
                    result = 31 * result + bdtPriRestriId.hashCode();
                } else if (agencyIdListId != null) {
                    result = 31 * result + agencyIdListId.hashCode();
                } else {
                    result = 31 * result + 0;
                }
            }
        }
        if (restrictionType == Agency) {
            if (agencyIdListId != null) {
                result = 31 * result + agencyIdListId.hashCode();
            } else {
                if (bdtPriRestriId != null) {
                    result = 31 * result + bdtPriRestriId.hashCode();
                } else if (codeListId != null) {
                    result = 31 * result + codeListId.hashCode();
                } else {
                    result = 31 * result + 0;
                }
            }
        }
        result = 31 * result + cardinalityMin;
        result = 31 * result + cardinalityMax;
        result = 31 * result + (defaultValue != null ? defaultValue.hashCode() : 0);
        result = 31 * result + (nillable ? 1 : 0);
        result = 31 * result + (fixedValue != null ? fixedValue.hashCode() : 0);
        result = 31 * result + (nill ? 1 : 0);
        result = 31 * result + (definitionId != null ? definitionId.hashCode() : 0);
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
        return "BasicBusinessInformationEntity{" +
                "bbieId=" + bbieId +
                ", guid='" + guid + '\'' +
                ", basedBccId=" + basedBccId +
                ", fromAbieId=" + fromAbieId +
                ", toBbiepId=" + toBbiepId +
                ", bdtPriRestriId=" + bdtPriRestriId +
                ", codeListId=" + codeListId +
                ", agencyIdListId=" + agencyIdListId +
                ", cardinalityMin=" + cardinalityMin +
                ", cardinalityMax=" + cardinalityMax +
                ", defaultValue='" + defaultValue + '\'' +
                ", nillable=" + nillable +
                ", fixedValue='" + fixedValue + '\'' +
                ", nill=" + nill +
                ", definitionId='" + definitionId + '\'' +
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

    @Transient
    private transient List<PersistEventListener> persistEventListeners;

    @Transient
    private transient List<UpdateEventListener> updateEventListeners;

    public BasicBusinessInformationEntity() {
        TimestampAwareEventListener timestampAwareEventListener = new TimestampAwareEventListener();
        addPersistEventListener(timestampAwareEventListener);
        addPersistEventListener(new PersistEventListener() {
            @Override
            public void onPrePersist(Object object) {
                BasicBusinessInformationEntity bbie = (BasicBusinessInformationEntity) object;
                if (bbie.fromAbie != null) {
                    bbie.setFromAbieId(bbie.fromAbie.getAbieId());
                }
                if (bbie.toBbiep != null) {
                    bbie.setToBbiepId(bbie.toBbiep.getBbiepId());
                }
                if (bbie.ownerTopLevelAbie != null) {
                    bbie.setOwnerTopLevelAbieId(bbie.ownerTopLevelAbie.getTopLevelAbieId());
                }

                ensureRestrictionType();
            }
            @Override
            public void onPostPersist(Object object) {
                BasicBusinessInformationEntity bbie = (BasicBusinessInformationEntity) object;
                bbie.afterLoaded();

                if (bbie.definition != null) {
                    bbie.definition.setRefId(getId());
                    bbie.definition.setRefTableName(tableName());
                }
            }
        });
        addUpdateEventListener(timestampAwareEventListener);
        addUpdateEventListener(new UpdateEventListener() {
            @Override
            public void onPreUpdate(Object object) {
                ensureRestrictionType();
            }

            @Override
            public void onPostUpdate(Object object) {
                BasicBusinessInformationEntity bbie = (BasicBusinessInformationEntity) object;
                bbie.afterLoaded();
            }
        });
    }

    private void ensureRestrictionType() {
        switch (restrictionType) {
            case Primitive:
                if (bdtPriRestriId != null && bdtPriRestriId > 0L) {
                    codeListId = null;
                    agencyIdListId = null;
                }

                break;
            case Code:
                if (codeListId != null && codeListId > 0L) {
                    bdtPriRestriId = null;
                    agencyIdListId = null;
                }

                break;
            case Agency:
                if (agencyIdListId != null && agencyIdListId > 0L) {
                    bdtPriRestriId = null;
                    codeListId = null;
                }

                break;
        }
    }

    public void addPersistEventListener(PersistEventListener persistEventListener) {
        if (persistEventListener == null) {
            return;
        }
        if (persistEventListeners == null) {
            persistEventListeners = new ArrayList();
        }
        persistEventListeners.add(persistEventListener);
    }

    private Collection<PersistEventListener> getPersistEventListeners() {
        return (persistEventListeners != null) ? persistEventListeners : Collections.emptyList();
    }

    public void addUpdateEventListener(UpdateEventListener updateEventListener) {
        if (updateEventListener == null) {
            return;
        }
        if (updateEventListeners == null) {
            updateEventListeners = new ArrayList();
        }
        updateEventListeners.add(updateEventListener);
    }

    private Collection<UpdateEventListener> getUpdateEventListeners() {
        return (updateEventListeners != null) ? updateEventListeners : Collections.emptyList();
    }

    @PrePersist
    public void prePersist() {
        for (PersistEventListener persistEventListener : getPersistEventListeners()) {
            persistEventListener.onPrePersist(this);
        }
    }

    @PostPersist
    public void postPersist() {
        for (PersistEventListener persistEventListener : getPersistEventListeners()) {
            persistEventListener.onPostPersist(this);
        }
    }

    @PreUpdate
    public void preUpdate() {
        for (UpdateEventListener updateEventListener : getUpdateEventListeners()) {
            updateEventListener.onPreUpdate(this);
        }
    }

    @PostUpdate
    public void postUpdate() {
        for (UpdateEventListener updateEventListener : getUpdateEventListeners()) {
            updateEventListener.onPostUpdate(this);
        }
    }

    @Transient
    private int hashCodeAfterLoaded;

    @PostLoad
    public void afterLoaded() {
        if (bdtPriRestriId != null && bdtPriRestriId > 0L && (codeListId == null && agencyIdListId == null)) {
            setRestrictionType(Primitive);
        } else if (codeListId != null && codeListId > 0L && (bdtPriRestriId == null && agencyIdListId == null)) {
            setRestrictionType(Code);
        } else if (agencyIdListId != null && agencyIdListId > 0L && (bdtPriRestriId == null && codeListId == null)) {
            setRestrictionType(Agency);
        } else {
            throw new IllegalStateException();
        }

        hashCodeAfterLoaded = hashCode();
    }

    public boolean isDirty() {
        return hashCodeAfterLoaded != hashCode();
    }

    public BasicBusinessInformationEntity clone(boolean shallowCopy) {
        BasicBusinessInformationEntity clone = new BasicBusinessInformationEntity();
        clone.guid = this.guid;
        clone.basedBccId = this.basedBccId;
        clone.fromAbieId = this.fromAbieId;
        clone.toBbiepId = this.toBbiepId;
        clone.bdtPriRestriId = this.bdtPriRestriId;
        clone.codeListId = this.codeListId;
        clone.agencyIdListId = this.agencyIdListId;
        clone.cardinalityMin = this.cardinalityMin;
        clone.cardinalityMax = this.cardinalityMax;
        clone.defaultValue = this.defaultValue;
        clone.nillable = this.nillable;
        clone.fixedValue = this.fixedValue;
        clone.nill = this.nill;

        if (shallowCopy) {
            clone.definitionId = this.definitionId;
        } else {
            clone.definition = (definition != null) ? definition.clone() : null;
        }

        clone.remark = this.remark;
        clone.seqKey = this.seqKey;
        clone.used = this.used;
        clone.afterLoaded();
        return clone;
    }
}
