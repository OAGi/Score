package org.oagi.srt.repository.entity;

import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.oagi.srt.repository.entity.listener.PersistEventListener;
import org.oagi.srt.repository.entity.listener.TimestampAwareEventListener;
import org.oagi.srt.repository.entity.listener.UpdateEventListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "abie")
@org.hibernate.annotations.Cache(region = "", usage = CacheConcurrencyStrategy.READ_WRITE)
public class AggregateBusinessInformationEntity implements Serializable, TimestampAware, IdEntity, IGuidEntity {

    public static final String SEQUENCE_NAME = "ABIE_ID_SEQ";

    @Id
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1000)
    private long abieId;

    @Column(nullable = false, length = 41)
    private String guid;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "based_acc_id", nullable = false)
    private AggregateCoreComponent basedAcc;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "biz_ctx_id")
    private BusinessContext bizCtx;

    @Transient
    private String bizCtxName;

    @Lob
    @Column(length = 10 * 1024)
    private String definition;

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
    private int state;

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

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_top_level_abie_id", nullable = false)
    private TopLevelAbie ownerTopLevelAbie;

    @Override
    public long getId() {
        return getAbieId();
    }

    @Override
    public void setId(long id) {
        setAbieId(id);
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

    public AggregateCoreComponent getBasedAcc() {
        return basedAcc;
    }

    public void setBasedAcc(AggregateCoreComponent basedAcc) {
        this.basedAcc = basedAcc;
    }

    public BusinessContext getBizCtx() {
        return bizCtx;
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

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
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

    public int getState() {
        return state;
    }

    public void setState(int state) {
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

    public TopLevelAbie getOwnerTopLevelAbie() {
        return ownerTopLevelAbie;
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
        result = 31 * result + (basedAcc != null ? basedAcc.hashCode() : 0);
        result = 31 * result + (bizCtx != null ? bizCtx.hashCode() : 0);
        result = 31 * result + (bizCtxName != null ? bizCtxName.hashCode() : 0);
        result = 31 * result + (definition != null ? definition.hashCode() : 0);
        result = 31 * result + (int) (createdBy ^ (createdBy >>> 32));
        result = 31 * result + (int) (lastUpdatedBy ^ (lastUpdatedBy >>> 32));
        result = 31 * result + (creationTimestamp != null ? creationTimestamp.hashCode() : 0);
        result = 31 * result + (lastUpdateTimestamp != null ? lastUpdateTimestamp.hashCode() : 0);
        result = 31 * result + state;
        result = 31 * result + (clientId != null ? clientId.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (remark != null ? remark.hashCode() : 0);
        result = 31 * result + (bizTerm != null ? bizTerm.hashCode() : 0);
        result = 31 * result + (ownerTopLevelAbie != null ? ownerTopLevelAbie.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AggregateBusinessInformationEntity{" +
                "abieId=" + abieId +
                ", guid='" + guid + '\'' +
                ", basedAcc=" + basedAcc +
                ", bizCtx=" + bizCtx +
                ", bizCtxName='" + bizCtxName + '\'' +
                ", definition='" + definition + '\'' +
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
                ", ownerTopLevelAbie=" + ownerTopLevelAbie +
                '}';
    }

    @Transient
    private transient List<PersistEventListener> persistEventListeners;

    @Transient
    private transient List<UpdateEventListener> updateEventListeners;

    public AggregateBusinessInformationEntity() {
        TimestampAwareEventListener timestampAwareEventListener = new TimestampAwareEventListener();
        addPersistEventListener(timestampAwareEventListener);
        addUpdateEventListener(timestampAwareEventListener);
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
}
