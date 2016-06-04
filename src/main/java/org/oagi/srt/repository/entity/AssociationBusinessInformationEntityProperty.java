package org.oagi.srt.repository.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "asbiep")
public class AssociationBusinessInformationEntityProperty implements Serializable, IGuidEntity {

    public static final String SEQUENCE_NAME = "ASBIEP_ID_SEQ";

    @Id
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.AUTO)
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    private int asbiepId;

    @Column(nullable = false)
    private String guid;

    @Column(nullable = false)
    private int basedAsccpId;

    @Column(nullable = false)
    private int roleOfAbieId;

    @Column
    private String definition;

    @Column
    private String remark;

    @Column
    private String bizTerm;

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

    @PrePersist
    public void prePersist() {
        creationTimestamp = new Date();
        lastUpdateTimestamp = new Date();
    }

    @PreUpdate
    public void preUpdate() {
        lastUpdateTimestamp = new Date();
    }

    public int getAsbiepId() {
        return asbiepId;
    }

    public void setAsbiepId(int asbiepId) {
        this.asbiepId = asbiepId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public int getBasedAsccpId() {
        return basedAsccpId;
    }

    public void setBasedAsccpId(int basedAsccpId) {
        this.basedAsccpId = basedAsccpId;
    }

    public int getRoleOfAbieId() {
        return roleOfAbieId;
    }

    public void setRoleOfAbieId(int roleOfAbieId) {
        this.roleOfAbieId = roleOfAbieId;
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

    public String getBizTerm() {
        return bizTerm;
    }

    public void setBizTerm(String bizTerm) {
        this.bizTerm = bizTerm;
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

    @Override
    public String toString() {
        return "AssociationBusinessInformationEntityProperty{" +
                "asbiepId=" + asbiepId +
                ", guid='" + guid + '\'' +
                ", basedAsccpId=" + basedAsccpId +
                ", roleOfAbieId=" + roleOfAbieId +
                ", definition='" + definition + '\'' +
                ", remark='" + remark + '\'' +
                ", bizTerm='" + bizTerm + '\'' +
                ", createdBy=" + createdBy +
                ", lastUpdatedBy=" + lastUpdatedBy +
                ", creationTimestamp=" + creationTimestamp +
                ", lastUpdateTimestamp=" + lastUpdateTimestamp +
                '}';
    }
}
