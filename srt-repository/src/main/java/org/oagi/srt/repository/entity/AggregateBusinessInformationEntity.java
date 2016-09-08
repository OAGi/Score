package org.oagi.srt.repository.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "abie")
public class AggregateBusinessInformationEntity implements Serializable, IdEntity, IGuidEntity {

    public static final String SEQUENCE_NAME = "ABIE_ID_SEQ";

    @Id
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME)
    private long abieId;

    @Column(nullable = false, length = 41)
    private String guid;

    @Column(nullable = false)
    private long basedAccId;

    @Column
    private long bizCtxId;

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

    public long getBasedAccId() {
        return basedAccId;
    }

    public void setBasedAccId(long basedAccId) {
        this.basedAccId = basedAccId;
    }

    public long getBizCtxId() {
        return bizCtxId;
    }

    public void setBizCtxId(long bizCtxId) {
        this.bizCtxId = bizCtxId;
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

        AggregateBusinessInformationEntity that = (AggregateBusinessInformationEntity) o;

        if (abieId != that.abieId) return false;
        if (basedAccId != that.basedAccId) return false;
        if (bizCtxId != that.bizCtxId) return false;
        if (createdBy != that.createdBy) return false;
        if (lastUpdatedBy != that.lastUpdatedBy) return false;
        if (state != that.state) return false;
        if (ownerTopLevelAbieId != that.ownerTopLevelAbieId) return false;
        if (guid != null ? !guid.equals(that.guid) : that.guid != null) return false;
        if (bizCtxName != null ? !bizCtxName.equals(that.bizCtxName) : that.bizCtxName != null) return false;
        if (definition != null ? !definition.equals(that.definition) : that.definition != null) return false;
        if (creationTimestamp != null ? !creationTimestamp.equals(that.creationTimestamp) : that.creationTimestamp != null)
            return false;
        if (lastUpdateTimestamp != null ? !lastUpdateTimestamp.equals(that.lastUpdateTimestamp) : that.lastUpdateTimestamp != null)
            return false;
        if (clientId != null ? !clientId.equals(that.clientId) : that.clientId != null) return false;
        if (version != null ? !version.equals(that.version) : that.version != null) return false;
        if (status != null ? !status.equals(that.status) : that.status != null) return false;
        if (remark != null ? !remark.equals(that.remark) : that.remark != null) return false;
        return bizTerm != null ? bizTerm.equals(that.bizTerm) : that.bizTerm == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (abieId ^ (abieId >>> 32));
        result = 31 * result + (guid != null ? guid.hashCode() : 0);
        result = 31 * result + (int) (basedAccId ^ (basedAccId >>> 32));
        result = 31 * result + (int) (bizCtxId ^ (bizCtxId >>> 32));
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
        result = 31 * result + (int) (ownerTopLevelAbieId ^ (ownerTopLevelAbieId >>> 32));
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
                ", ownerTopLevelAbieId=" + ownerTopLevelAbieId +
                '}';
    }
}
