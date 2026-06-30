package org.oagi.score.gateway.http.api.oas_management.model;

import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.base.Auditable;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

public class BieForOasDoc extends Auditable {

    private TopLevelAsbiepId topLevelAsbiepId;
    private ReleaseId releaseId;
    private OasDocId oasDocId;
    // Issue #1519: the owning OpenAPI Document's title and version, so the BIE-root "OpenAPI Document
    // Information" panel can label each binding by document (title + version) when one BIE participates in
    // several documents — two documents can share a title, so the version helps disambiguate.
    private String oasDocTitle;
    private String oasDocVersion;
    // Issue #1610: the owning OpenAPI Document's OpenAPI spec version (e.g. "3.0.3" / "3.1.1"). The BIE-root
    // panel uses it (read-only) to warn that a DELETE Request body is ignored when the document targets
    // OpenAPI 3.0.x; the version itself is changed on the OpenAPI Document screen, not the BIE screen.
    private String openAPIVersion;
    private OasResourceId oasResourceId;
    private OasOperationId oasOperationId;
    private String den;
    private String propertyTerm;
    private String displayName;
    private String releaseNum;
    private String remark;
    private String guid;
    private List<BusinessContextSummaryRecord> businessContexts;
    private UserSummaryRecord owner;
    private String version;
    private String status;
    private BieState state;
    private String access;
    private String verb;
    private String messageBody;
    private boolean arrayIndicator;
    private boolean suppressRootIndicator;
    private String resourceName;
    private String operationId;
    private String tagName;
    private Integer httpStatusCode;
    // Issue #1729: per-operation security. securityOverridden=false => inherit the document-level
    // security (no operation `security` emitted). true with empty requirements => `security: []`.
    // true with requirements => that operation's `security` array. Keyed by the oas_operation.
    private boolean securityOverridden;
    private List<OasSecurityRequirement> securityRequirements;
    // Issue #1347: per-operation error-response body type (PROBLEM_DETAILS | CONFIRM_MESSAGE | NONE,
    // default NONE) and, for CONFIRM_MESSAGE, the picked ConfirmMessage BIE (id + DEN for display).
    // Keyed by the oas_operation, so the Request and Response entries of one operation share these.
    private String errorResponseBodyType;
    private BigInteger confirmMessageTopLevelAsbiepId;
    private String confirmMessageDen;
    private Date lastUpdateTimestamp;
    private Date creationTimestamp;
    private UserSummaryRecord createdBy;
    private UserSummaryRecord lastUpdatedBy;

    public BieForOasDoc() {
    }

    public TopLevelAsbiepId getTopLevelAsbiepId() {
        return topLevelAsbiepId;
    }

    public void setTopLevelAsbiepId(TopLevelAsbiepId topLevelAsbiepId) {
        this.topLevelAsbiepId = topLevelAsbiepId;
    }

    public ReleaseId getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(ReleaseId releaseId) {
        this.releaseId = releaseId;
    }

    public OasDocId getOasDocId() {
        return oasDocId;
    }

    public void setOasDocId(OasDocId oasDocId) {
        this.oasDocId = oasDocId;
    }

    public String getOasDocTitle() {
        return oasDocTitle;
    }

    public void setOasDocTitle(String oasDocTitle) {
        this.oasDocTitle = oasDocTitle;
    }

    public String getOasDocVersion() {
        return oasDocVersion;
    }

    public void setOasDocVersion(String oasDocVersion) {
        this.oasDocVersion = oasDocVersion;
    }

    public String getOpenAPIVersion() {
        return openAPIVersion;
    }

    public void setOpenAPIVersion(String openAPIVersion) {
        this.openAPIVersion = openAPIVersion;
    }

    public String getDen() {
        return den;
    }

    public void setDen(String den) {
        this.den = den;
    }

    public String getReleaseNum() {
        return releaseNum;
    }

    public void setReleaseNum(String releaseNum) {
        this.releaseNum = releaseNum;
    }

    public String getPropertyTerm() {
        return propertyTerm;
    }

    public void setPropertyTerm(String propertyTerm) {
        this.propertyTerm = propertyTerm;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public UserSummaryRecord getOwner() {
        return owner;
    }

    public void setOwner(UserSummaryRecord owner) {
        this.owner = owner;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BieState getState() {
        return state;
    }

    public void setState(BieState state) {
        this.state = state;
    }

    public String getAccess() {
        return access;
    }

    public void setAccess(String access) {
        this.access = access;
    }

    public List<BusinessContextSummaryRecord> getBusinessContexts() {
        return businessContexts;
    }

    public void setBusinessContexts(List<BusinessContextSummaryRecord> businessContexts) {
        this.businessContexts = businessContexts;
    }

    public String getVerb() {
        return verb;
    }

    public void setVerb(String verb) {
        this.verb = verb;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public boolean isArrayIndicator() {
        return arrayIndicator;
    }

    public void setArrayIndicator(boolean arrayIndicator) {
        this.arrayIndicator = arrayIndicator;
    }

    public boolean isSuppressRootIndicator() {
        return suppressRootIndicator;
    }

    public void setSuppressRootIndicator(boolean suppressRootIndicator) {
        this.suppressRootIndicator = suppressRootIndicator;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public OasResourceId getOasResourceId() {
        return oasResourceId;
    }

    public void setOasResourceId(OasResourceId oasResourceId) {
        this.oasResourceId = oasResourceId;
    }

    public OasOperationId getOasOperationId() {
        return oasOperationId;
    }

    public void setOasOperationId(OasOperationId oasOperationId) {
        this.oasOperationId = oasOperationId;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public Integer getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(Integer httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public boolean isSecurityOverridden() {
        return securityOverridden;
    }

    public void setSecurityOverridden(boolean securityOverridden) {
        this.securityOverridden = securityOverridden;
    }

    public List<OasSecurityRequirement> getSecurityRequirements() {
        return securityRequirements;
    }

    public void setSecurityRequirements(List<OasSecurityRequirement> securityRequirements) {
        this.securityRequirements = securityRequirements;
    }

    public String getErrorResponseBodyType() {
        return errorResponseBodyType;
    }

    public void setErrorResponseBodyType(String errorResponseBodyType) {
        this.errorResponseBodyType = errorResponseBodyType;
    }

    public BigInteger getConfirmMessageTopLevelAsbiepId() {
        return confirmMessageTopLevelAsbiepId;
    }

    public void setConfirmMessageTopLevelAsbiepId(BigInteger confirmMessageTopLevelAsbiepId) {
        this.confirmMessageTopLevelAsbiepId = confirmMessageTopLevelAsbiepId;
    }

    public String getConfirmMessageDen() {
        return confirmMessageDen;
    }

    public void setConfirmMessageDen(String confirmMessageDen) {
        this.confirmMessageDen = confirmMessageDen;
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
        return "BieForOasDoc{" +
                "topLevelAsbiepId=" + topLevelAsbiepId +
                ", releaseId=" + releaseId +
                ", oasDocId=" + oasDocId +
                ", oasResourceId=" + oasResourceId +
                ", oasOperationId=" + oasOperationId +
                ", den='" + den + '\'' +
                ", propertyTerm='" + propertyTerm + '\'' +
                ", displayName='" + displayName + '\'' +
                ", releaseNum='" + releaseNum + '\'' +
                ", remark='" + remark + '\'' +
                ", guid='" + guid + '\'' +
                ", businessContexts=" + businessContexts +
                ", owner='" + owner + '\'' +
                ", version='" + version + '\'' +
                ", status='" + status + '\'' +
                ", state=" + state +
                ", access='" + access + '\'' +
                ", verb='" + verb + '\'' +
                ", messageBody='" + messageBody + '\'' +
                ", arrayIndicator=" + arrayIndicator +
                ", suppressRootIndicator=" + suppressRootIndicator +
                ", resourceName='" + resourceName + '\'' +
                ", operationId='" + operationId + '\'' +
                ", tagName='" + tagName + '\'' +
                ", httpStatusCode=" + httpStatusCode +
                ", lastUpdateTimestamp=" + lastUpdateTimestamp +
                ", creationTimestamp=" + creationTimestamp +
                ", createdBy=" + createdBy +
                ", lastUpdatedBy=" + lastUpdatedBy +
                '}';
    }
}
