package org.oagi.srt.repository.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "v_profile_bod")
public class ProfileBOD {

    @Id
    private long topLevelAbieId;

    @Column(nullable = false)
    private long abieId;

    @Column(nullable = false)
    private long asbiepId;

    @Column(nullable = false)
    private long asccpId;

    @Column(nullable = false)
    private String propertyTerm;

    @Column(nullable = false)
    private long bizCtxId;

    @Column(length = 100)
    private String bizCtxName;

    @Column
    private int state;

    @Column(nullable = false, updatable = false)
    private long createdBy;

    @Column(length = 100)
    private String createdUsername;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTimestamp;

    public long getTopLevelAbieId() {
        return topLevelAbieId;
    }

    public void setTopLevelAbieId(long topLevelAbieId) {
        this.topLevelAbieId = topLevelAbieId;
    }

    public long getAbieId() {
        return abieId;
    }

    public void setAbieId(long abieId) {
        this.abieId = abieId;
    }

    public long getAsbiepId() {
        return asbiepId;
    }

    public void setAsbiepId(long asbiepId) {
        this.asbiepId = asbiepId;
    }

    public long getAsccpId() {
        return asccpId;
    }

    public void setAsccpId(long asccpId) {
        this.asccpId = asccpId;
    }

    public String getPropertyTerm() {
        return propertyTerm;
    }

    public void setPropertyTerm(String propertyTerm) {
        this.propertyTerm = propertyTerm;
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

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(long createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedUsername() {
        return createdUsername;
    }

    public void setCreatedUsername(String createdUsername) {
        this.createdUsername = createdUsername;
    }

    public Date getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(Date creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProfileBOD that = (ProfileBOD) o;

        if (topLevelAbieId != 0L && topLevelAbieId == that.topLevelAbieId) return true;
        return false;
    }

    @Override
    public int hashCode() {
        int result = (int) (topLevelAbieId ^ (topLevelAbieId >>> 32));
        result = 31 * result + (int) (abieId ^ (abieId >>> 32));
        result = 31 * result + (int) (asbiepId ^ (asbiepId >>> 32));
        result = 31 * result + (int) (asccpId ^ (asccpId >>> 32));
        result = 31 * result + (propertyTerm != null ? propertyTerm.hashCode() : 0);
        result = 31 * result + (int) (bizCtxId ^ (bizCtxId >>> 32));
        result = 31 * result + (bizCtxName != null ? bizCtxName.hashCode() : 0);
        result = 31 * result + state;
        result = 31 * result + (int) (createdBy ^ (createdBy >>> 32));
        result = 31 * result + (createdUsername != null ? createdUsername.hashCode() : 0);
        result = 31 * result + (creationTimestamp != null ? creationTimestamp.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ProfileBOD{" +
                "topLevelAbieId=" + topLevelAbieId +
                ", abieId=" + abieId +
                ", asbiepId=" + asbiepId +
                ", asccpId=" + asccpId +
                ", propertyTerm='" + propertyTerm + '\'' +
                ", bizCtxId=" + bizCtxId +
                ", bizCtxName='" + bizCtxName + '\'' +
                ", state=" + state +
                ", createdBy=" + createdBy +
                ", createdUsername='" + createdUsername + '\'' +
                ", creationTimestamp=" + creationTimestamp +
                '}';
    }
}
