package org.oagi.score.repo.api.base;

import org.oagi.score.repo.api.user.model.ScoreUser;

import java.io.Serializable;
import java.util.Date;

public class Auditable implements Serializable {

    private ScoreUser createdBy;

    private ScoreUser lastUpdatedBy;

    private Date creationTimestamp;

    private Date lastUpdateTimestamp;

    public ScoreUser getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(ScoreUser createdBy) {
        this.createdBy = createdBy;
    }

    public ScoreUser getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(ScoreUser lastUpdatedBy) {
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
