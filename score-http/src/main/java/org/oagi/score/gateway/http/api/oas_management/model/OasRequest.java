package org.oagi.score.gateway.http.api.oas_management.model;

import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.common.model.base.Auditable;

import java.math.BigInteger;
import java.util.Date;

public class OasRequest extends Auditable {
    private OasRequestId oasRequestId;
    private OasOperationId oasOperationId;
    private String description;
    private boolean required;
    private OasMessageBodyId oasMessageBodyId;
    private boolean makeArrayIndicator;
    private boolean suppressRootIndicator;
    private BigInteger metaHeaderTopLevelAsbiepId;
    private BigInteger paginationTopLevelAsbiepId;
    private boolean isCallback;
    private Date lastUpdateTimestamp;
    private Date creationTimestamp;
    private UserSummaryRecord createdBy;
    private UserSummaryRecord lastUpdatedBy;

    public OasRequest() {

    }

    public OasRequestId getOasRequestId() {
        return oasRequestId;
    }

    public void setOasRequestId(OasRequestId oasRequestId) {
        this.oasRequestId = oasRequestId;
    }

    public OasOperationId getOasOperationId() {
        return oasOperationId;
    }

    public void setOasOperationId(OasOperationId oasOperationId) {
        this.oasOperationId = oasOperationId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public OasMessageBodyId getOasMessageBodyId() {
        return oasMessageBodyId;
    }

    public void setOasMessageBodyId(OasMessageBodyId oasMessageBodyId) {
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

    public boolean isCallback() {
        return isCallback;
    }

    public void setCallback(boolean callback) {
        isCallback = callback;
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
        return "OasRequest{" +
                "oasRequestId=" + oasRequestId +
                ", oasOperationId=" + oasOperationId +
                ", description='" + description + '\'' +
                ", required=" + required +
                ", oasMessageBodyId=" + oasMessageBodyId +
                ", makeArrayIndicator=" + makeArrayIndicator +
                ", suppressRootIndicator=" + suppressRootIndicator +
                ", metaHeaderTopLevelAsbiepId=" + metaHeaderTopLevelAsbiepId +
                ", paginationTopLevelAsbiepId=" + paginationTopLevelAsbiepId +
                ", isCallback=" + isCallback +
                ", lastUpdateTimestamp=" + lastUpdateTimestamp +
                ", creationTimestamp=" + creationTimestamp +
                ", createdBy=" + createdBy +
                ", lastUpdatedBy=" + lastUpdatedBy +
                '}';
    }
}
