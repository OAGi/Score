package org.oagi.score.repo.api.openapidoc.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;

public class UpdateBieForOasDocRequest extends Request {
    private BigInteger bieForOasDocId;
    private BigInteger topLevelAsbiepId;
    private BigInteger oasDocId;
    private BigInteger bizCtxId;
    private String bizCtxName;
    private String access;
    private String version;
    private String state;
    private String verb;
    private boolean arrayIndicator;
    private boolean suppressRoot;
    private String messageBody;
    private String resourceName;
    private String operationId;
    private String tagName;

    public UpdateBieForOasDocRequest(ScoreUser requester) {
        super(requester);
    }

    public UpdateBieForOasDocRequest withBieForOasDocId(BigInteger bieForOasDocId) {
        this.setBieForOasDocId(bieForOasDocId);
        return this;
    }

    public BigInteger getBieForOasDocId() {
        return bieForOasDocId;
    }

    public void setBieForOasDocId(BigInteger bieForOasDocId) {
        this.bieForOasDocId = bieForOasDocId;
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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
    public String toString() {
        return "UpdateBieForOasDocRequest{" +
                "bieForOasDocId=" + bieForOasDocId +
                ", topLevelAsbiepId=" + topLevelAsbiepId +
                ", oasDocId=" + oasDocId +
                ", bizCtxId=" + bizCtxId +
                ", bizCtxName='" + bizCtxName + '\'' +
                ", access='" + access + '\'' +
                ", version='" + version + '\'' +
                ", state='" + state + '\'' +
                ", verb='" + verb + '\'' +
                ", arrayIndicator=" + arrayIndicator +
                ", suppressRoot=" + suppressRoot +
                ", messageBody='" + messageBody + '\'' +
                ", resourceName='" + resourceName + '\'' +
                ", operationId='" + operationId + '\'' +
                ", tagName='" + tagName + '\'' +
                '}';
    }
}
