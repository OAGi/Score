package org.oagi.score.repo.api.openapidoc.model;

import org.oagi.score.repo.api.base.Auditable;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.util.Date;

public class OasOperation extends Auditable {
    private BigInteger oasOperationId;
    private BigInteger oasResourceId;
    private String verb;
    private String operationId;
    private String summary;
    private String description;
    private boolean deprecated;
    private Date lastUpdateTimestamp;
    private Date creationTimestamp;
    private ScoreUser createdBy;
    private ScoreUser lastUpdatedBy;

    public OasOperation(){

    }
    public OasOperation(BigInteger oasOperationId, BigInteger oasResourceId, String verb, String operationId, String summary, String description, boolean deprecated, Date lastUpdateTimestamp, Date creationTimestamp, ScoreUser createdBy, ScoreUser lastUpdatedBy) {
        this.oasOperationId = oasOperationId;
        this.oasResourceId = oasResourceId;
        this.verb = verb;
        this.operationId = operationId;
        this.summary = summary;
        this.description = description;
        this.deprecated = deprecated;
        this.lastUpdateTimestamp = lastUpdateTimestamp;
        this.creationTimestamp = creationTimestamp;
        this.createdBy = createdBy;
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public BigInteger getOasOperationId() {
        return oasOperationId;
    }

    public void setOasOperationId(BigInteger oasOperationId) {
        this.oasOperationId = oasOperationId;
    }

    public BigInteger getOasResourceId() {
        return oasResourceId;
    }

    public void setOasResourceId(BigInteger oasResourceId) {
        this.oasResourceId = oasResourceId;
    }
    public String getVerb() {
        return verb;
    }

    public void setVerb(String verb) {
        this.verb = verb;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
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
    public ScoreUser getCreatedBy() {
        return createdBy;
    }

    @Override
    public void setCreatedBy(ScoreUser createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public ScoreUser getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    @Override
    public void setLastUpdatedBy(ScoreUser lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    @Override
    public String toString() {
        return "OasOperation{" +
                "oasOperationId=" + oasOperationId +
                ", oasResourceId=" + oasResourceId +
                ", verb='" + verb + '\'' +
                ", operationId='" + operationId + '\'' +
                ", summary='" + summary + '\'' +
                ", description='" + description + '\'' +
                ", deprecated='" + deprecated + '\'' +
                ", lastUpdateTimestamp=" + lastUpdateTimestamp +
                ", creationTimestamp=" + creationTimestamp +
                ", createdBy=" + createdBy +
                ", lastUpdatedBy=" + lastUpdatedBy +
                '}';
    }
}
