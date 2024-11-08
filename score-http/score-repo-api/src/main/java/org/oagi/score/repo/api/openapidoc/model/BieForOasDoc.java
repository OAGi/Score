package org.oagi.score.repo.api.openapidoc.model;

import org.oagi.score.repo.api.base.Auditable;
import org.oagi.score.repo.api.bie.model.BieState;
import org.oagi.score.repo.api.businesscontext.model.BusinessContext;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

public class BieForOasDoc extends Auditable {
    private BigInteger topLevelAsbiepId;
    private BigInteger releaseId;
    private BigInteger oasDocId;
    private BigInteger oasResourceId;
    private BigInteger oasOperationId;
    private String den;
    private String propertyTerm;
    private String displayName;
    private String releaseNum;
    private String remark;
    private String guid;
    private List<BusinessContext> businessContexts;
    private BigInteger ownerUserId;
    private String owner;
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
    private Date lastUpdateTimestamp;
    private Date creationTimestamp;
    private ScoreUser createdBy;
    private ScoreUser lastUpdatedBy;

    public BieForOasDoc() {
    }

    public BigInteger getTopLevelAsbiepId() {
        return topLevelAsbiepId;
    }

    public void setTopLevelAsbiepId(BigInteger topLevelAsbiepId) {
        this.topLevelAsbiepId = topLevelAsbiepId;
    }

    public BigInteger getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(BigInteger releaseId) {
        this.releaseId = releaseId;
    }

    public BigInteger getOasDocId() {
        return oasDocId;
    }

    public void setOasDocId(BigInteger oasDocId) {
        this.oasDocId = oasDocId;
    }

    public BigInteger getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(BigInteger ownerUserId) {
        this.ownerUserId = ownerUserId;
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

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
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

    public List<BusinessContext> getBusinessContexts() {
        return businessContexts;
    }

    public void setBusinessContexts(List<BusinessContext> businessContexts) {
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

    public BigInteger getOasResourceId() {
        return oasResourceId;
    }

    public void setOasResourceId(BigInteger oasResourceId) {
        this.oasResourceId = oasResourceId;
    }

    public BigInteger getOasOperationId() {
        return oasOperationId;
    }

    public void setOasOperationId(BigInteger oasOperationId) {
        this.oasOperationId = oasOperationId;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
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
                ", ownerUserId=" + ownerUserId +
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
                ", lastUpdateTimestamp=" + lastUpdateTimestamp +
                ", creationTimestamp=" + creationTimestamp +
                ", createdBy=" + createdBy +
                ", lastUpdatedBy=" + lastUpdatedBy +
                '}';
    }
}
