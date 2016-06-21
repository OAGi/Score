package org.oagi.srt.repository.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "abie")
public class AggregateBusinessInformationEntity implements Serializable, IdEntity, IGuidEntity {

    public static final String SEQUENCE_NAME = "ABIE_ID_SEQ";

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
    private int abieId;

    @Column(nullable = false)
    private String guid;

    @Column(nullable = false)
    private int basedAccId;

    @Transient
    private String bizCtxName;

    @Lob
    @Column
    private String definition;

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

    @Column
    private Integer clientId;

    @Column
    private String version;

    @Column
    private String status;

    @Column
    private String remark;

    @Column
    private String bizTerm;

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
        return getAbieId();
    }

    @Override
    public void setId(int id) {
        setAbieId(id);
    }

    public int getAbieId() {
        return abieId;
    }

    public void setAbieId(int abieId) {
        this.abieId = abieId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public int getBasedAccId() {
        return basedAccId;
    }

    public void setBasedAccId(int basedAccId) {
        this.basedAccId = basedAccId;
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

    public int getClientId() {
        return (clientId == null) ? 0 : clientId;
    }

    public void setClientId(int clientId) {
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

        AggregateBusinessInformationEntity that = (AggregateBusinessInformationEntity) o;

        if (abieId != that.abieId) return false;
        if (basedAccId != that.basedAccId) return false;
        if (createdBy != that.createdBy) return false;
        if (lastUpdatedBy != that.lastUpdatedBy) return false;
        if (bodId != that.bodId) return false;
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
        int result = abieId;
        result = 31 * result + (guid != null ? guid.hashCode() : 0);
        result = 31 * result + basedAccId;
        result = 31 * result + (bizCtxName != null ? bizCtxName.hashCode() : 0);
        result = 31 * result + (definition != null ? definition.hashCode() : 0);
        result = 31 * result + createdBy;
        result = 31 * result + lastUpdatedBy;
        result = 31 * result + (creationTimestamp != null ? creationTimestamp.hashCode() : 0);
        result = 31 * result + (lastUpdateTimestamp != null ? lastUpdateTimestamp.hashCode() : 0);
        result = 31 * result + (clientId != null ? clientId.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (remark != null ? remark.hashCode() : 0);
        result = 31 * result + (bizTerm != null ? bizTerm.hashCode() : 0);
        result = 31 * result + bodId;
        return result;
    }

    @Override
    public String toString() {
        return "AggregateBusinessInformationEntity{" +
                "abieId=" + abieId +
                ", guid='" + guid + '\'' +
                ", basedAccId=" + basedAccId +
                ", bizCtxName='" + bizCtxName + '\'' +
                ", definition='" + definition + '\'' +
                ", createdBy=" + createdBy +
                ", lastUpdatedBy=" + lastUpdatedBy +
                ", creationTimestamp=" + creationTimestamp +
                ", lastUpdateTimestamp=" + lastUpdateTimestamp +
                ", clientId=" + clientId +
                ", version='" + version + '\'' +
                ", status='" + status + '\'' +
                ", remark='" + remark + '\'' +
                ", bizTerm='" + bizTerm + '\'' +
                ", bodId=" + bodId +
                '}';
    }
}
