package org.oagi.score.gateway.http.api.oas_management.model;

import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.common.model.base.Auditable;

import java.math.BigInteger;
import java.util.Date;

public class OasResponse extends Auditable {
    private OasResponseId oasResponseId;
    private OasOperationId oasOperationId;
    private int httpStatusCode;
    private String description;
    private OasMessageBodyId oasMessageBodyId;
    private boolean makeArrayIndicator;
    private boolean suppressRootIndicator;
    private TopLevelAsbiepId metaHeaderTopLevelAsbiepId;
    private TopLevelAsbiepId paginationTopLevelAsbiepId;
    private boolean includeConfirmIndicator;
    private Date lastUpdateTimestamp;
    private Date creationTimestamp;
    private UserSummaryRecord createdBy;
    private UserSummaryRecord lastUpdatedBy;

    public OasResponse(){

    }

    public OasResponseId getOasResponseId() {
        return oasResponseId;
    }

    public void setOasResponseId(OasResponseId oasResponseId) {
        this.oasResponseId = oasResponseId;
    }

    public OasOperationId getOasOperationId() {
        return oasOperationId;
    }

    public void setOasOperationId(OasOperationId oasOperationId) {
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

    public TopLevelAsbiepId getMetaHeaderTopLevelAsbiepId() {
        return metaHeaderTopLevelAsbiepId;
    }

    public void setMetaHeaderTopLevelAsbiepId(TopLevelAsbiepId metaHeaderTopLevelAsbiepId) {
        this.metaHeaderTopLevelAsbiepId = metaHeaderTopLevelAsbiepId;
    }

    public TopLevelAsbiepId getPaginationTopLevelAsbiepId() {
        return paginationTopLevelAsbiepId;
    }

    public void setPaginationTopLevelAsbiepId(TopLevelAsbiepId paginationTopLevelAsbiepId) {
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
