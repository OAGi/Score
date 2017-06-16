package org.oagi.srt.repository.entity;

import org.hibernate.annotations.GenericGenerator;
import org.oagi.srt.repository.entity.converter.AggregateBusinessInformationEntityStateConverter;
import org.oagi.srt.repository.entity.listener.PersistEventListener;
import org.oagi.srt.repository.entity.listener.TimestampAwareEventListener;
import org.oagi.srt.repository.entity.listener.UpdateEventListener;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "abie")
public class AggregateBusinessInformationEntity
        implements BusinessInformationEntity, CreatorModifierAware, TimestampAware, Serializable {

    public static final String SEQUENCE_NAME = "ABIE_ID_SEQ";

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
    private long abieId;

    @Column(nullable = false, length = 41, updatable = false)
    private String guid;

    @Column(nullable = false, updatable = false)
    private long basedAccId;
    @Transient
    private AggregateCoreComponent basedAcc;

    @Column(updatable = false)
    private long bizCtxId;
    @Transient
    private BusinessContext bizCtx;

    @Transient
    private String bizCtxName;

    @Column
    private Long definitionId;
    @Transient
    private Definition definition;

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

    @Column
    @Convert(attributeName = "state", converter = AggregateBusinessInformationEntityStateConverter.class)
    private AggregateBusinessInformationEntityState state;

    @Column
    private Long clientId;

    @Column(length = 45)
    private String version;

    @Column(length = 45)
    private String status;

    @Column(length = 225)
    private String remark;

    @Column(length = 225)
    private String bizTerm;

    @Column(nullable = false, updatable = false)
    private long ownerTopLevelAbieId;
    @Transient
    private TopLevelAbie ownerTopLevelAbie;

    @Override
    public long getId() {
        return getAbieId();
    }

    @Override
    public void setId(long id) {
        setAbieId(id);
    }

    @Override
    public String tableName() {
        return "ABIE";
    }

    public long getAbieId() {
        return abieId;
    }

    public void setAbieId(long abieId) {
        this.abieId = abieId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public long getBasedAccId() {
        return basedAccId;
    }

    public void setBasedAccId(long basedAccId) {
        this.basedAccId = basedAccId;
    }

    public void setBasedAcc(AggregateCoreComponent basedAcc) {
        this.basedAcc = basedAcc;
    }

    public long getBizCtxId() {
        return bizCtxId;
    }

    public void setBizCtxId(long bizCtxId) {
        this.bizCtxId = bizCtxId;
    }

    public void setBizCtx(BusinessContext bizCtx) {
        this.bizCtx = bizCtx;
    }

    public String getBizCtxName() {
        return bizCtxName;
    }

    public void setBizCtxName(String bizCtxName) {
        this.bizCtxName = bizCtxName;
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

    public AggregateBusinessInformationEntityState getState() {
        return state;
    }

    public void setState(AggregateBusinessInformationEntityState state) {
        this.state = state;
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

    public long getClientId() {
        return (clientId == null) ? 0L : clientId;
    }

    public void setClientId(long clientId) {
        this.clientId = clientId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getBizTerm() {
        return bizTerm;
    }

    public void setBizTerm(String bizTerm) {
        this.bizTerm = bizTerm;
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

        AggregateBusinessInformationEntity that = (AggregateBusinessInformationEntity) o;

        if (abieId != 0L && abieId == that.abieId) return true;
        if (guid != null) {
            if (guid.equals(that.guid)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = (int) (abieId ^ (abieId >>> 32));
        result = 31 * result + (guid != null ? guid.hashCode() : 0);
        result = 31 * result + (int) (basedAccId ^ (basedAccId >>> 32));
        result = 31 * result + (basedAcc != null ? basedAcc.hashCode() : 0);
        result = 31 * result + (int) (bizCtxId ^ (bizCtxId >>> 32));
        result = 31 * result + (bizCtx != null ? bizCtx.hashCode() : 0);
        result = 31 * result + (bizCtxName != null ? bizCtxName.hashCode() : 0);
        result = 31 * result + (definitionId != null ? definitionId.hashCode() : 0);
        result = 31 * result + (int) (createdBy ^ (createdBy >>> 32));
        result = 31 * result + (int) (lastUpdatedBy ^ (lastUpdatedBy >>> 32));
        result = 31 * result + (creationTimestamp != null ? creationTimestamp.hashCode() : 0);
        result = 31 * result + (lastUpdateTimestamp != null ? lastUpdateTimestamp.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (clientId != null ? clientId.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (remark != null ? remark.hashCode() : 0);
        result = 31 * result + (bizTerm != null ? bizTerm.hashCode() : 0);
        result = 31 * result + (int) (ownerTopLevelAbieId ^ (ownerTopLevelAbieId >>> 32));
        result = 31 * result + (ownerTopLevelAbie != null ? ownerTopLevelAbie.hashCode() : 0);
        result = 31 * result + (persistEventListeners != null ? persistEventListeners.hashCode() : 0);
        result = 31 * result + (updateEventListeners != null ? updateEventListeners.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AggregateBusinessInformationEntity{" +
                "abieId=" + abieId +
                ", guid='" + guid + '\'' +
                ", basedAccId=" + basedAccId +
                ", bizCtxId=" + bizCtxId +
                ", bizCtxName='" + bizCtxName + '\'' +
                ", definitionId='" + definitionId + '\'' +
                ", createdBy=" + createdBy +
                ", lastUpdatedBy=" + lastUpdatedBy +
                ", creationTimestamp=" + creationTimestamp +
                ", lastUpdateTimestamp=" + lastUpdateTimestamp +
                ", state=" + state +
                ", clientId=" + clientId +
                ", version='" + version + '\'' +
                ", status='" + status + '\'' +
                ", remark='" + remark + '\'' +
                ", bizTerm='" + bizTerm + '\'' +
                ", ownerTopLevelAbieId=" + ownerTopLevelAbieId +
                ", ownerTopLevelAbie=" + ownerTopLevelAbie +
                ", persistEventListeners=" + persistEventListeners +
                ", updateEventListeners=" + updateEventListeners +
                '}';
    }

    @Transient
    private transient List<PersistEventListener> persistEventListeners;

    @Transient
    private transient List<UpdateEventListener> updateEventListeners;

    public AggregateBusinessInformationEntity() {
        TimestampAwareEventListener timestampAwareEventListener = new TimestampAwareEventListener();
        addPersistEventListener(timestampAwareEventListener);
        addPersistEventListener(new PersistEventListener() {
            @Override
            public void onPrePersist(Object object) {
                AggregateBusinessInformationEntity abie = (AggregateBusinessInformationEntity) object;
                if (abie.basedAcc != null) {
                    abie.setBasedAccId(abie.basedAcc.getAccId());
                }
                if (abie.getBasedAccId() == 0L) {
                    throw new IllegalStateException("'basedAccId' parameter must not be null.");
                }
                if (abie.bizCtx != null) {
                    abie.setBizCtxId(abie.bizCtx.getBizCtxId());
                }
                if (abie.getBizCtxId() == 0L) {
                    throw new IllegalStateException("'bizCtxId' parameter must not be null.");
                }
                if (abie.ownerTopLevelAbie != null) {
                    abie.setOwnerTopLevelAbieId(abie.ownerTopLevelAbie.getTopLevelAbieId());
                }
                if (abie.getOwnerTopLevelAbieId() == 0L) {
                    throw new IllegalStateException("'ownerTopLevelAbieId' parameter must not be null.");
                }
            }

            @Override
            public void onPostPersist(Object object) {
                AggregateBusinessInformationEntity abie = (AggregateBusinessInformationEntity) object;
                abie.afterLoaded();

                if ((abie.definitionId == null || abie.definitionId == 0L) && abie.definition != null) {
                    abie.definition.setRefId(getId());
                    abie.definition.setRefTableName(tableName());
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
                AggregateBusinessInformationEntity abie = (AggregateBusinessInformationEntity) object;
                abie.afterLoaded();
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

    public AggregateBusinessInformationEntity clone(boolean shallowCopy) {
        AggregateBusinessInformationEntity clone = new AggregateBusinessInformationEntity();
        clone.guid = this.guid;
        clone.basedAccId = this.basedAccId;
        clone.bizCtxId = this.bizCtxId;

        if (shallowCopy) {
            clone.definitionId = this.definitionId;
        } else {
            clone.definition = (definition != null) ? definition.clone() : null;
        }

        clone.state = this.state;
        clone.clientId = this.clientId;
        clone.version = this.version;
        clone.status = this.status;
        clone.remark = this.remark;
        clone.bizTerm = this.bizTerm;
        clone.afterLoaded();
        return clone;
    }
}
