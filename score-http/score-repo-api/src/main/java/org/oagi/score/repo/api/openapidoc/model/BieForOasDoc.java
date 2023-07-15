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
    private String guid;
    private List<BusinessContext> businessContexts;
    private BigInteger ownerUserId;
    private String owner;
    private String version;
    private String status;
    private BieState state;
    private String access;
    private List<String> verbs;
    private List<String> messageBody;
    private String _verb;
    private boolean _arrayIndicator;
    private boolean _suppressRootIndicator;
    private String _resourceName;
    private String _operationId;
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

    public List<String> getVerbs() {
        return verbs;
    }

    public void setVerbs(List<String> verbs) {
        this.verbs = verbs;
    }

    public String get_verb() {
        return _verb;
    }

    public void set_verb(String _verb) {
        this._verb = _verb;
    }

    public boolean is_arrayIndicator() {
        return _arrayIndicator;
    }

    public void set_arrayIndicator(boolean _arrayIndicator) {
        this._arrayIndicator = _arrayIndicator;
    }

    public boolean is_suppressRootIndicator() {
        return _suppressRootIndicator;
    }

    public void set_suppressRootIndicator(boolean _suppressRootIndicator) {
        this._suppressRootIndicator = _suppressRootIndicator;
    }

    public String get_resourceName() {
        return _resourceName;
    }

    public void set_resourceName(String _resourceName) {
        this._resourceName = _resourceName;
    }

    public String get_operationId() {
        return _operationId;
    }

    public void set_operationId(String _operationId) {
        this._operationId = _operationId;
    }

    public List<BusinessContext> getBusinessContexts() {
        return businessContexts;
    }

    public void setBusinessContexts(List<BusinessContext> businessContexts) {
        this.businessContexts = businessContexts;
    }

    public List<String> getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(List<String> messageBody) {
        this.messageBody = messageBody;
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

    public BieForOasDoc(BigInteger topLevelAsbiepId, BigInteger oasDocId) {
        this.topLevelAsbiepId = topLevelAsbiepId;
        this.oasDocId = oasDocId;
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
                ", guid='" + guid + '\'' +
                ", businessContexts=" + businessContexts +
                ", ownerUserId=" + ownerUserId +
                ", owner='" + owner + '\'' +
                ", version='" + version + '\'' +
                ", status='" + status + '\'' +
                ", state=" + state +
                ", access='" + access + '\'' +
                ", verbs=" + verbs +
                ", messageBody=" + messageBody +
                ", _verb='" + _verb + '\'' +
                ", _arrayIndicator=" + _arrayIndicator +
                ", _suppressRootIndicator=" + _suppressRootIndicator +
                ", _resourceName='" + _resourceName + '\'' +
                ", _operationId='" + _operationId + '\'' +
                ", tagName='" + tagName + '\'' +
                ", lastUpdateTimestamp=" + lastUpdateTimestamp +
                ", creationTimestamp=" + creationTimestamp +
                ", createdBy=" + createdBy +
                ", lastUpdatedBy=" + lastUpdatedBy +
                '}';
    }
}
