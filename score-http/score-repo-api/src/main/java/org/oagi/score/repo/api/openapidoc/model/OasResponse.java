package org.oagi.score.repo.api.openapidoc.model;

import org.oagi.score.repo.api.base.Auditable;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.util.Date;

public class OasResponse extends Auditable {
    private BigInteger oasResponseId;
    private BigInteger oasOperationId;
    private int httpStatusCode;
    private String description;
    private BigInteger oasMessageBodyId;
    private boolean makeArrayIndicator;
    private boolean suppressRootIndicator;
    private BigInteger metaHeaderTopLevelAsbiepId;
    private BigInteger paginationTopLevelAsbiepId;
    private boolean includeConfirmIndicator;
    private Date lastUpdateTimestamp;
    private Date creationTimestamp;
    private ScoreUser createdBy;
    private ScoreUser lastUpdatedBy;

    public OasResponse(){

    }

    public BigInteger getOasResponseId() {
        return oasResponseId;
    }

    public void setOasResponseId(BigInteger oasResponseId) {
        this.oasResponseId = oasResponseId;
    }

    public BigInteger getOasOperationId() {
        return oasOperationId;
    }

    public void setOasOperationId(BigInteger oasOperationId) {
        this.oasOperationId = oasOperationId;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigInteger getOasMessageBodyId() {
        return oasMessageBodyId;
    }

    public void setOasMessageBodyId(BigInteger oasMessageBodyId) {
        this.oasMessageBodyId = oasMessageBodyId;
    }

    public boolean isMakeArrayIndicator() {
        return makeArrayIndicator;
    }

    public void setMakeArrayIndicator(boolean makeArrayIndicator) {
        this.makeArrayIndicator = makeArrayIndicator;
    }

    public boolean isSuppressRootIndicator() {
        return suppressRootIndicator;
    }

    public void setSuppressRootIndicator(boolean suppressRootIndicator) {
        this.suppressRootIndicator = suppressRootIndicator;
    }

    public BigInteger getMetaHeaderTopLevelAsbiepId() {
        return metaHeaderTopLevelAsbiepId;
    }

    public void setMetaHeaderTopLevelAsbiepId(BigInteger metaHeaderTopLevelAsbiepId) {
        this.metaHeaderTopLevelAsbiepId = metaHeaderTopLevelAsbiepId;
    }

    public BigInteger getPaginationTopLevelAsbiepId() {
        return paginationTopLevelAsbiepId;
    }

    public void setPaginationTopLevelAsbiepId(BigInteger paginationTopLevelAsbiepId) {
        this.paginationTopLevelAsbiepId = paginationTopLevelAsbiepId;
    }

    public boolean isIncludeConfirmIndicator() {
        return includeConfirmIndicator;
    }

    public void setIncludeConfirmIndicator(boolean includeConfirmIndicator) {
        this.includeConfirmIndicator = includeConfirmIndicator;
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
        return "OasResponse{" +
                "oasResponseId=" + oasResponseId +
                ", oasOperationId=" + oasOperationId +
                ", httpStatusCode=" + httpStatusCode +
                ", description='" + description + '\'' +
                ", oasMessageBodyId=" + oasMessageBodyId +
                ", makeArrayIndicator=" + makeArrayIndicator +
                ", suppressRootIndicator=" + suppressRootIndicator +
                ", metaHeaderTopLevelAsbiepId=" + metaHeaderTopLevelAsbiepId +
                ", paginationTopLevelAsbiepId=" + paginationTopLevelAsbiepId +
                ", includeConfirmIndicator=" + includeConfirmIndicator +
                ", lastUpdateTimestamp=" + lastUpdateTimestamp +
                ", creationTimestamp=" + creationTimestamp +
                ", createdBy=" + createdBy +
                ", lastUpdatedBy=" + lastUpdatedBy +
                '}';
    }
}
