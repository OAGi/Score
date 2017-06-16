package org.oagi.srt.repository.entity;

import org.hibernate.annotations.GenericGenerator;
import org.oagi.srt.repository.entity.listener.PersistEventListener;
import org.oagi.srt.repository.entity.listener.TimestampAwareEventListener;
import org.oagi.srt.repository.entity.listener.UpdateEventListener;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "asbiep")
public class AssociationBusinessInformationEntityProperty
        implements BusinessInformationEntity, CreatorModifierAware, TimestampAware, Serializable {

    public static final String SEQUENCE_NAME = "ASBIEP_ID_SEQ";

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
    private long asbiepId;

    @Column(nullable = false, length = 41, updatable = false)
    private String guid;

    @Column(nullable = false, updatable = false)
    private long basedAsccpId;
    @Transient
    private AssociationCoreComponentProperty basedAsccp;

    @Column(nullable = false, updatable = false)
    private long roleOfAbieId;
    @Transient
    private AggregateBusinessInformationEntity roleOfAbie;

    @Column
    private Long definitionId;
    @Transient
    private Definition definition;

    @Column(length = 225)
    private String remark;

    @Column(length = 225)
    private String bizTerm;

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
    private long ownerTopLevelAbieId;
    @Transient
    private TopLevelAbie ownerTopLevelAbie;

    @Override
    public long getId() {
        return getAsbiepId();
    }

    @Override
    public void setId(long id) {
        setAsbiepId(id);
    }

    @Override
    public String tableName() {
        return "ASBIEP";
    }

    public long getAsbiepId() {
        return asbiepId;
    }

    public void setAsbiepId(long asbiepId) {
        this.asbiepId = asbiepId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public long getBasedAsccpId() {
        return basedAsccpId;
    }

    public void setBasedAsccpId(long basedAsccpId) {
        this.basedAsccpId = basedAsccpId;
    }

    public void setBasedAsccp(AssociationCoreComponentProperty basedAsccp) {
        this.basedAsccp = basedAsccp;
    }

    public long getRoleOfAbieId() {
        return roleOfAbieId;
    }

    public void setRoleOfAbieId(long roleOfAbieId) {
        this.roleOfAbieId = roleOfAbieId;
    }

    public AggregateBusinessInformationEntity getRoleOfAbie() {
        return roleOfAbie;
    }

    public void setRoleOfAbie(AggregateBusinessInformationEntity roleOfAbie) {
        this.roleOfAbie = roleOfAbie;
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

    public String getBizTerm() {
        return bizTerm;
    }

    public void setBizTerm(String bizTerm) {
        this.bizTerm = bizTerm;
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

        AssociationBusinessInformationEntityProperty that = (AssociationBusinessInformationEntityProperty) o;

        if (asbiepId != 0L && asbiepId == that.asbiepId) return true;
        if (guid != null) {
            if (guid.equals(that.guid)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = (int) (asbiepId ^ (asbiepId >>> 32));
        result = 31 * result + (guid != null ? guid.hashCode() : 0);
        result = 31 * result + (int) (basedAsccpId ^ (basedAsccpId >>> 32));
        result = 31 * result + (int) (roleOfAbieId ^ (roleOfAbieId >>> 32));
        result = 31 * result + (definitionId != null ? definitionId.hashCode() : 0);
        result = 31 * result + (remark != null ? remark.hashCode() : 0);
        result = 31 * result + (bizTerm != null ? bizTerm.hashCode() : 0);
        result = 31 * result + (int) (createdBy ^ (createdBy >>> 32));
        result = 31 * result + (int) (lastUpdatedBy ^ (lastUpdatedBy >>> 32));
        result = 31 * result + (creationTimestamp != null ? creationTimestamp.hashCode() : 0);
        result = 31 * result + (lastUpdateTimestamp != null ? lastUpdateTimestamp.hashCode() : 0);
        result = 31 * result + (int) (ownerTopLevelAbieId ^ (ownerTopLevelAbieId >>> 32));
        result = 31 * result + (ownerTopLevelAbie != null ? ownerTopLevelAbie.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AssociationBusinessInformationEntityProperty{" +
                "asbiepId=" + asbiepId +
                ", guid='" + guid + '\'' +
                ", basedAsccpId=" + basedAsccpId +
                ", roleOfAbieId=" + roleOfAbieId +
                ", definitionId=" + definitionId +
                ", remark='" + remark + '\'' +
                ", bizTerm='" + bizTerm + '\'' +
                ", createdBy=" + createdBy +
                ", lastUpdatedBy=" + lastUpdatedBy +
                ", creationTimestamp=" + creationTimestamp +
                ", lastUpdateTimestamp=" + lastUpdateTimestamp +
                ", ownerTopLevelAbieId=" + ownerTopLevelAbieId +
                ", ownerTopLevelAbie=" + ownerTopLevelAbie +
                '}';
    }

    @Transient
    private transient List<PersistEventListener> persistEventListeners;

    @Transient
    private transient List<UpdateEventListener> updateEventListeners;

    public AssociationBusinessInformationEntityProperty() {
        TimestampAwareEventListener timestampAwareEventListener = new TimestampAwareEventListener();
        addPersistEventListener(timestampAwareEventListener);
        addPersistEventListener(new PersistEventListener() {
            @Override
            public void onPrePersist(Object object) {
                AssociationBusinessInformationEntityProperty asbiep = (AssociationBusinessInformationEntityProperty) object;
                if (asbiep.basedAsccp != null) {
                    asbiep.setBasedAsccpId(asbiep.basedAsccp.getAsccpId());
                }
                if (asbiep.roleOfAbie != null) {
                    asbiep.setRoleOfAbieId(asbiep.roleOfAbie.getAbieId());
                }
                if (asbiep.ownerTopLevelAbie != null) {
                    asbiep.setOwnerTopLevelAbieId(asbiep.ownerTopLevelAbie.getTopLevelAbieId());
                }
            }

            @Override
            public void onPostPersist(Object object) {
                AssociationBusinessInformationEntityProperty asbiep = (AssociationBusinessInformationEntityProperty) object;
                asbiep.afterLoaded();

                if (asbiep.definition != null) {
                    asbiep.definition.setRefId(getId());
                    asbiep.definition.setRefTableName(tableName());
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
                AssociationBusinessInformationEntityProperty asbiep = (AssociationBusinessInformationEntityProperty) object;
                asbiep.afterLoaded();
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

    public AssociationBusinessInformationEntityProperty clone(boolean shallowCopy) {
        AssociationBusinessInformationEntityProperty clone = new AssociationBusinessInformationEntityProperty();
        clone.guid = this.guid;
        clone.basedAsccpId = this.basedAsccpId;
        clone.roleOfAbieId = this.roleOfAbieId;

        if (shallowCopy) {
            clone.definitionId = this.definitionId;
        } else {
            clone.definition = (definition != null) ? definition.clone() : null;
        }

        clone.remark = this.remark;
        clone.bizTerm = this.bizTerm;
        return clone;
    }
}
