package org.oagi.score.gateway.http.api.oas_management.model;

import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.common.model.base.Auditable;

import java.math.BigInteger;
import java.util.Date;

public class OasTag extends Auditable {
    private BigInteger oasTagId;
    private String guid;
    private String name;
    private String description;
    private Date lastUpdateTimestamp;
    private Date creationTimestamp;
    private UserSummaryRecord createdBy;
    private UserSummaryRecord lastUpdatedBy;
    public OasTag() {
    }
    public OasTag(BigInteger oasTagId, String guid, String name, String description, Date lastUpdateTimestamp, Date creationTimestamp) {
        this.oasTagId = oasTagId;
        this.guid = guid;
        this.name = name;
        this.description = description;
        this.lastUpdateTimestamp = lastUpdateTimestamp;
        this.creationTimestamp = creationTimestamp;
    }

    public BigInteger getOasTagId() {
        return oasTagId;
    }

    public void setOasTagId(BigInteger oasTagId) {
        this.oasTagId = oasTagId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Date getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    @Override
    public void setLastUpdateTimestamp(Date lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    @Override
    public Date getCreationTimestamp() {
        return creationTimestamp;
    }

    @Override
    public void setCreationTimestamp(Date creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    @Override
    public UserSummaryRecord getCreatedBy() {
        return createdBy;
    }

    @Override
    public void setCreatedBy(UserSummaryRecord createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public UserSummaryRecord getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    @Override
    public void setLastUpdatedBy(UserSummaryRecord lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    @Override
    public String toString() {
        return "OasTag{" +
                "oasTagId=" + oasTagId +
                ", guid='" + guid + '\'' +
                ", meaning='" + name + '\'' +
                ", description='" + description + '\'' +
                ", lastUpdateTimestamp=" + lastUpdateTimestamp +
                ", creationTimestamp=" + creationTimestamp +
                ", createdBy=" + createdBy +
                ", lastUpdatedBy=" + lastUpdatedBy +
                '}';
    }
}
