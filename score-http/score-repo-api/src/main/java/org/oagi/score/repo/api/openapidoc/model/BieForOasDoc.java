package org.oagi.score.repo.api.openapidoc.model;

import org.oagi.score.repo.api.base.Auditable;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.util.Date;

public class BieForOasDoc extends Auditable {
    private BigInteger topLevelAsbiepId;
    private BigInteger oasDocId;
    private String den;
    private String propertyTerm;
    private String guid;
    private String releaseNum;
    private BigInteger bizCtxId;
    private String bizCtxName;
    private String access;
    private String owner;
    private String version;
    private String status;
    private String state;
    private String verb;
    private boolean arrayIndicator;
    private boolean suppressRoot;
    private String messageBody;
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

    public BigInteger getOasDocId() {
        return oasDocId;
    }

    public void setOasDocId(BigInteger oasDocId) {
        this.oasDocId = oasDocId;
    }

    public String getDen() {
        return den;
    }

    public void setDen(String den) {
        this.den = den;
    }

    public String getPropertyTerm() {
        return propertyTerm;
    }

    public void setPropertyTerm(String propertyTerm) {
        this.propertyTerm = propertyTerm;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getReleaseNum() {
        return releaseNum;
    }

    public void setReleaseNum(String releaseNum) {
        this.releaseNum = releaseNum;
    }

    public BigInteger getBizCtxId() {
        return bizCtxId;
    }

    public void setBizCtxId(BigInteger bizCtxId) {
        this.bizCtxId = bizCtxId;
    }

    public String getBizCtxName() {
        return bizCtxName;
    }

    public void setBizCtxName(String bizCtxName) {
        this.bizCtxName = bizCtxName;
    }

    public String getAccess() {
        return access;
    }

    public void setAccess(String access) {
        this.access = access;
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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getVerb() {
        return verb;
    }

    public void setVerb(String verb) {
        this.verb = verb;
    }

    public boolean isArrayIndicator() {
        return arrayIndicator;
    }

    public void setArrayIndicator(boolean arrayIndicator) {
        this.arrayIndicator = arrayIndicator;
    }

    public boolean isSuppressRoot() {
        return suppressRoot;
    }

    public void setSuppressRoot(boolean suppressRoot) {
        this.suppressRoot = suppressRoot;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
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

    public BieForOasDoc(BigInteger topLevelAsbiepId, BigInteger oasDocId, String den, String propertyTerm, String guid, String releaseNum, BigInteger bizCtxId, String bizCtxName, String access, String owner, String version, String status, String state, String verb, boolean arrayIndicator, boolean suppressRoot, String messageBody, String resourceName, String operationId, String tagName, Date lastUpdateTimestamp, Date creationTimestamp, ScoreUser createdBy, ScoreUser lastUpdatedBy) {
        this.topLevelAsbiepId = topLevelAsbiepId;
        this.oasDocId = oasDocId;
        this.den = den;
        this.propertyTerm = propertyTerm;
        this.guid = guid;
        this.releaseNum = releaseNum;
        this.bizCtxId = bizCtxId;
        this.bizCtxName = bizCtxName;
        this.access = access;
        this.owner = owner;
        this.version = version;
        this.status = status;
        this.state = state;
        this.verb = verb;
        this.arrayIndicator = arrayIndicator;
        this.suppressRoot = suppressRoot;
        this.messageBody = messageBody;
        this.resourceName = resourceName;
        this.operationId = operationId;
        this.tagName = tagName;
        this.lastUpdateTimestamp = lastUpdateTimestamp;
        this.creationTimestamp = creationTimestamp;
        this.createdBy = createdBy;
        this.lastUpdatedBy = lastUpdatedBy;
    }

    @Override
    public String toString() {
        return "BieListForOasDoc{" +
                "topLevelAsbiepId=" + topLevelAsbiepId +
                ", oasDocId=" + oasDocId +
                ", den='" + den + '\'' +
                ", propertyTerm='" + propertyTerm + '\'' +
                ", guid='" + guid + '\'' +
                ", releaseNum='" + releaseNum + '\'' +
                ", bizCtxId=" + bizCtxId +
                ", bizCtxName='" + bizCtxName + '\'' +
                ", access='" + access + '\'' +
                ", owner='" + owner + '\'' +
                ", version='" + version + '\'' +
                ", status='" + status + '\'' +
                ", state='" + state + '\'' +
                ", verb='" + verb + '\'' +
                ", arrayIndicator=" + arrayIndicator +
                ", suppressRoot=" + suppressRoot +
                ", messageBody='" + messageBody + '\'' +
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
