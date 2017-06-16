package org.oagi.srt.repository.entity;

import org.hibernate.annotations.GenericGenerator;
import org.oagi.srt.repository.JpaRepositoryDefinitionHelper;
import org.oagi.srt.repository.entity.listener.PersistEventListener;
import org.oagi.srt.repository.entity.listener.TimestampAwareEventListener;
import org.oagi.srt.repository.entity.listener.UpdateEventListener;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "asbie")
public class AssociationBusinessInformationEntity
        implements BusinessInformationEntity, CreatorModifierAware, TimestampAware, Usable, Serializable {

    public static final String SEQUENCE_NAME = "ASBIE_ID_SEQ";

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
    private long asbieId;

    @Column(nullable = false, length = 41, updatable = false)
    private String guid;

    @Column(nullable = false, updatable = false)
    private long fromAbieId;
    @Transient
    private AggregateBusinessInformationEntity fromAbie;

    @Column(nullable = false, updatable = false)
    private long toAsbiepId;

    @Transient
    private AssociationBusinessInformationEntityProperty toAsbiep;

    @Column(nullable = false, updatable = false)
    private long basedAsccId;

    @Column
    private Long definitionId;
    @Transient
    private Definition definition;

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
        return getAsbieId();
    }

    @Override
    public void setId(long id) {
        setAsbieId(id);
    }

    @Override
    public String tableName() {
        return "ASBIE";
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

    public AggregateBusinessInformationEntity getFromAbie() {
        return fromAbie;
    }

    public void setFromAbie(AggregateBusinessInformationEntity fromAbie) {
        this.fromAbie = fromAbie;
    }

    public long getToAsbiepId() {
        return toAsbiepId;
    }

    public void setToAsbiepId(long toAsbiepId) {
        this.toAsbiepId = toAsbiepId;
    }

    public AssociationBusinessInformationEntityProperty getToAsbiep() {
        return toAsbiep;
    }

    public void setToAsbiep(AssociationBusinessInformationEntityProperty toAsbiep) {
        this.toAsbiep = toAsbiep;
    }

    public long getBasedAsccId() {
        return basedAsccId;
    }

    public void setBasedAsccId(long basedAsccId) {
        this.basedAsccId = basedAsccId;
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

    public void setOwnerTopLevelAbie(TopLevelAbie ownerTopLevelAbie) {
        this.ownerTopLevelAbie = ownerTopLevelAbie;
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
        result = 31 * result + (definitionId != null ? definitionId.hashCode() : 0);
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
                ", definitionId=" + definitionId +
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
                ", ownerTopLevelAbie=" + ownerTopLevelAbie +
                '}';
    }

    @Transient
    private transient List<PersistEventListener> persistEventListeners;

    @Transient
    private transient List<UpdateEventListener> updateEventListeners;

    public AssociationBusinessInformationEntity() {
        TimestampAwareEventListener timestampAwareEventListener = new TimestampAwareEventListener();
        addPersistEventListener(timestampAwareEventListener);
        addPersistEventListener(new PersistEventListener() {
            @Override
            public void onPrePersist(Object object) {
                AssociationBusinessInformationEntity asbie = (AssociationBusinessInformationEntity) object;
                if (asbie.fromAbie != null) {
                    asbie.setFromAbieId(asbie.fromAbie.getAbieId());
                }
                if (asbie.toAsbiep != null) {
                    asbie.setToAsbiepId(asbie.toAsbiep.getAsbiepId());
                }
                if (asbie.ownerTopLevelAbie != null) {
                    asbie.setOwnerTopLevelAbieId(asbie.ownerTopLevelAbie.getTopLevelAbieId());
                }
            }
            @Override
            public void onPostPersist(Object object) {
                AssociationBusinessInformationEntity asbie = (AssociationBusinessInformationEntity) object;
                asbie.afterLoaded();

                if (asbie.definition != null) {
                    asbie.definition.setRefId(getId());
                    asbie.definition.setRefTableName(tableName());
                }
            }
        });
        addUpdateEventListener(timestampAwareEventListener);
        addUpdateEventListener(new UpdateEventListener() {
            @Override
            public void onPreUpdate(Object object) {
            }

            @Override
            public void onPostUpdate(Object object) {
                AssociationBusinessInformationEntity asbie = (AssociationBusinessInformationEntity) object;
                asbie.afterLoaded();
            }
        });
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
        hashCodeAfterLoaded = hashCode();
    }

    public boolean isDirty() {
        return hashCodeAfterLoaded != hashCode();
    }

    @Override
    public AssociationBusinessInformationEntity clone() {
        AssociationBusinessInformationEntity clone = new AssociationBusinessInformationEntity();
        clone.guid = this.guid;
        clone.fromAbieId = this.fromAbieId;
        clone.toAsbiepId = this.toAsbiepId;
        clone.basedAsccId = this.basedAsccId;
        clone.definition = JpaRepositoryDefinitionHelper.cloneDefinition(this);
        clone.cardinalityMin = this.cardinalityMin;
        clone.cardinalityMax = this.cardinalityMax;
        clone.nillable = this.nillable;
        clone.remark = this.remark;
        clone.seqKey = this.seqKey;
        clone.used = this.used;
        clone.afterLoaded();
        return clone;
    }
}
