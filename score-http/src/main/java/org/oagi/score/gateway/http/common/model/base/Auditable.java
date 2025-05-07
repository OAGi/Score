package org.oagi.score.gateway.http.common.model.base;

import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;

import java.io.Serializable;
import java.util.Date;

public class Auditable implements Serializable {

    private UserSummaryRecord createdBy;

    private UserSummaryRecord lastUpdatedBy;

    private Date creationTimestamp;

    private Date lastUpdateTimestamp;

    public UserSummaryRecord getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserSummaryRecord createdBy) {
        this.createdBy = createdBy;
    }

    public UserSummaryRecord getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(UserSummaryRecord lastUpdatedBy) {
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
