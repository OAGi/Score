package org.oagi.score.repo.api.openapidoc.model;

import org.oagi.score.repo.api.base.Auditable;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.util.Date;

public class AssignBieForOasDoc extends Auditable {
    private boolean isOasRequest;
    private BigInteger topLevelAsbiepId;
    private BigInteger oasDocId;
    private String propertyTerm;
    private String verb;
    private boolean arrayIndicator;
    private boolean suppressRootIndicator;
    private Date lastUpdateTimestamp;
    private Date creationTimestamp;
    private ScoreUser createdBy;
    private ScoreUser lastUpdatedBy;

    public boolean isOasRequest() {
        return isOasRequest;
    }

    public void setOasRequest(boolean oasRequest) {
        isOasRequest = oasRequest;
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

    public String getPropertyTerm() {
        return propertyTerm;
    }

    public void setPropertyTerm(String propertyTerm) {
        this.propertyTerm = propertyTerm;
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

    public boolean isSuppressRootIndicator() {
        return suppressRootIndicator;
    }

    public void setSuppressRootIndicator(boolean suppressRootIndicator) {
        this.suppressRootIndicator = suppressRootIndicator;
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

    public AssignBieForOasDoc(boolean isOasRequest, BigInteger topLevelAsbiepId, BigInteger oasDocId, String propertyTerm, String verb, boolean arrayIndicator, boolean suppressRootIndicator, Date lastUpdateTimestamp, Date creationTimestamp, ScoreUser createdBy, ScoreUser lastUpdatedBy) {
        this.isOasRequest = isOasRequest;
        this.topLevelAsbiepId = topLevelAsbiepId;
        this.oasDocId = oasDocId;
        this.propertyTerm = propertyTerm;
        this.verb = verb;
        this.arrayIndicator = arrayIndicator;
        this.suppressRootIndicator = suppressRootIndicator;
        this.lastUpdateTimestamp = lastUpdateTimestamp;
        this.creationTimestamp = creationTimestamp;
        this.createdBy = createdBy;
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public AssignBieForOasDoc() {

    }

    @Override
    public String toString() {
        return "AssignBieForOasDoc{" +
                "isOasRequest=" + isOasRequest +
                ", topLevelAsbiepId=" + topLevelAsbiepId +
                ", oasDocId=" + oasDocId +
                ", propertyTerm='" + propertyTerm + '\'' +
                ", verb='" + verb + '\'' +
                ", arrayIndicator=" + arrayIndicator +
                ", suppressRootIndicator=" + suppressRootIndicator +
                ", lastUpdateTimestamp=" + lastUpdateTimestamp +
                ", creationTimestamp=" + creationTimestamp +
                ", createdBy=" + createdBy +
                ", lastUpdatedBy=" + lastUpdatedBy +
                '}';
    }
}
