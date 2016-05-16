package org.oagi.srt.repository.entity;

import java.util.Date;

public class AssociationBusinessInformationEntityProperty {

    private int asbiepId;
    private String guid;
    private int basedAsccpId;
    private int roleOfAbieId;
    private String definition;
    private String remark;
    private String bizTerm;
    private int createdBy;
    private int lastUpdatedBy;
    private Date creationTimestamp;
    private Date lastUpdateTimestamp;

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
}
